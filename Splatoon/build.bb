;We want this to run on Workbench...
WBStartup

;I do this on every project - it makes sense to work with whole numbers as a
;default and only use floating points when we actually need them
DEFTYPE .w

;Make sure we cleanly quit if we run into an error
SetErr
    AMIGA
    NPrint "Unknown error: enough free Chipram?"
    End
End SetErr

;A statement I came up with for resizing DisplayLibrary displays
;Doesn't work perfectly!
Statement ResizeWidth{clist.l,amount.l,xadjust.l}
	
	diwstrt.l = xadjust*8
	diwstop.l = diwstrt + amount*8
	
	dUfstrt.l = Int(diwstrt/2)
	dUfstop.l = Int(diwstop/2)
	
	DisplayAdjust clist,amount,dUfstrt,dUfstop,diwstrt,diwstop
	
End Statement

;Initialise a storage area in chipram - this is VERY heavy (around 1.5mb) 
;but enough to contain all of our art assets
InitBank 0,224*6720,2

;Just initialise a completely blank palette
InitPalette 1,255

;Use one bitmap to cover the entire bank
CludgeBitMap 0, 224,6720,8,Bank(0)

;Set this to 1 if we want to recompile our assets
;This essentially loads the source IFFs, converts them and spits out raw data
#COMPILEASSETS=0
CNIF #COMPILEASSETS=1
LoadBitMap 0,"source/sheet1.iff",0
SavePalette 0,"images/sheet1.pal"
SaveBank 0,"images/sheet1.bank"
LoadBitMap 0,"source/sheet2.iff",0
SavePalette 0,"images/sheet2.pal"
SaveBank 0,"images/sheet2.bank"
CEND

;Initailise our one and only copper list
InitCopList 0,44+68,120,$10000 + $8,8,256,0

;Use our blank palette by default
DisplayPalette 0,1

;All we want to do here is set the "border blank" bit, so that our borders
;Are always black even if palette #0 is not
DisplayControls 0, 0, $20, 0

;Shrink the size of our display to match our animation viewport
ResizeWidth{0,-10,0}

;We need to do certain things almost every single frame, such as changing the 
;animation frame. However, loading sounds etc takes longer than one frame
;By putting code in the VBlank interrupt, we can be assured it'll run every
;frame

SetInt 5

    if Playing

		;Our music is split into five second intervals (50 frames per second)
        MusicCounter = MusicCounter Mod 250
        if MusicCounter = 0 and MusicPlayReady=true
            Sound 1-SoundBackBuffer,$f
            MusicLoadReady=true
            MusicPlayReady=false
        endif
        MusicCounter+1

		;We're handling our first part, or..
        if PartNumber = 1

            ;First animation is looping linear
            if FrameCounter = 4
                FrameCounter = 0
                Frame + 1
                if Frame > 55
                    Frame = 0
                endif
            endif

        else

		;This is our second part
            if FrameCounter = 5
                FrameCounter = 0
                Frame + 1
                TotalFrameCounter + 1

                ;Silhouette dance
                if TotalFrameCounter > 8 and TotalFrameCounter <= 118
                    Frame = QWrap(Frame,8,12)
                endif
                ;Strobe lights
                if TotalFrameCounter > 118 and TotalFrameCounter <= 138
                    Frame = QWrap(Frame,12,16)
                endif
                ;Silhouette again
                if TotalFrameCounter > 138 and TotalFrameCounter <= 145
                    Frame = QWrap(Frame,8,12)
                endif
                ;Main dance
                if TotalFrameCounter > 145 and TotalFrameCounter <= 634
                    Frame = QWrap(Frame,16,16+15)
                endif
                ;End dance
                if TotalFrameCounter > 634
                    Frame = QWrap(Frame,31,31+18)
                endif

            endif

        endif

		;Increae the frame counter and display the frame
        FrameCounter + 1
        DisplayBitMap 0,0,0,120*Frame   

    endif     

End SetInt

;Statement for playing either of the parts
Statement PlayPart {part.b}
    
	;Shared variables. In practice I normally put global variables into a macro
	;But I haven't needed to here
    Shared PartNumber, Playing, SoundBackBuffer, MusicCounter, MusicPlayReady, MusicLoadReady
    Shared FrameCounter, TotalFrameCounter, Frame

	;Our sound files use this format
    Format "000"
    PartNumber = part
    TrackNumber = 1

	;Load our bank and our palette
    LoadBank 0,"images/sheet"+UStr$(PartNumber)+".bank"
    LoadPalette 0,"images/sheet"+UStr$(PartNumber)+".pal"    

	;Load the first sound file and prepare to play music
    LoadSound SoundBackBuffer,"track"+UStr$(PartNumber)+"/"+Str$(TrackNumber)+".8svx" 
    TrackNumber = 2
    SoundBackBuffer = 1-SoundBackBuffer
    MusicCounter = 0
    MusicPlayReady = true
    MusicLoadReady = false

    ;Wait for joystick release
    While JoyB(0)+JoyB(1)>0 : Wend

	;Reset the, and display our new palette
    Frame=0
    FrameCounter=0
    TotalFrameCounter=0    
    Playing=True
    DisplayPalette 0,0

	;Repeat until we're finished
    While not PartDone

		;Quit if we push any button on either joystick or mouse
        if JoyB(0) + JoyB(1) > 0
            ForceQuit=true
            PartDone = true
        endif
		
        if MusicLoadReady
			;Check that the next five second interfal of music exists, and if it does, load it
            if Exists("track"+UStr$(PartNumber)+"/"+Str$(TrackNumber)+".8svx" )

			LoadSound SoundBackBuffer,"track"+UStr$(PartNumber)+"/"+Str$(TrackNumber)+".8svx" 
				
				;Swap the buffers - we want to play from one while we're loading the other
                SoundBackBuffer = 1-SoundBackBuffer
				
                MusicLoadReady = false
                MusicPlayReady = true
                TrackNumber+1
            Else
				;There's no more tracks, so we're finished on this part
                PartDone=true
            endif
        endif

    Wend

    if part=2
		;If we've quit early, skip ahead to the end part
        if TotalFrameCounter < 634
            TotalFrameCounter = 634
        endif
		
		;Wait four seconds to make sure the audio has finished
        VWait 200
    endif

	;Use the blank palette again
    DisplayPalette 0,1
    Playing=False

End Statement

;Quit if we don't have AGA
if (Peek.w($dff004) & (1 LSL 9))=0
    NPrint "Sorry, you need AGA!"
    End
endif

;Wait for the joystick to be released
While JoyB(0)+JoyB(1)>0 : Wend

;Create a screen in the background so we can't interfere with workbench
Screen 0,1
Window 0, 0, 0, 320, 256, $800, "", 0, 0 

;Switch to blitz mode JUST to create the display
BLITZ
CreateDisplay 0

;Go back to system friendly mode
QAMIGA

PlayPart{1}
PlayPart{2}

;We're finished
End
