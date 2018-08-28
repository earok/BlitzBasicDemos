;==================
;PART 5: FOREGROUND
;==================
;This program supports Work Bench
WBStartup
;Use WORD as the default data type rather than QUICK
;This is faster and better for any time you don't need
;Floating point numbers
DEFTYPE .w

#BLITKIND=1
;BLIT = 0
;QBLIT = 1
;BBLIT = 2

;Various constants are defined in order to avoid the use of 'magic numbers'
;later in the code
#COPPERLIST_MAIN = 0
#BITMAP_BACKGROUND = 0
#BITMAP_FOREGROUND = #BITMAP_BACKGROUND+1
#PALETTE_MAIN = 0
#SHAPE_FIGHTER = 0
#QUEUEID = 0
#BUFFERID = 0

;Prepare the queue and buffer for blitting
Queue #QUEUEID,16
;16 = max items in the queue
Buffer #BUFFERID,16384
;16384 = 16kb of memory

;Prepare our background bitmap
BitMap #BITMAP_BACKGROUND,224,768,4
;224 - width of the bitmap
;768 - height of the bitmap
;4 - depth of the bitmap. 4 bitplanes = 16 colours
;Memory used = 224*768*4 = 688,128 bits = 86,016 bytes
LoadBitMap #BITMAP_BACKGROUND,"background.iff"

;Prepare our foreground bitmap
BitMap #BITMAP_FOREGROUND,327,1056,4
LoadBitMap #BITMAP_FOREGROUND,"foreground.iff"

;Rip the fighter from the image as a shape
Use BitMap #BITMAP_FOREGROUND
GetaShape #SHAPE_FIGHTER, 5, 6, 25, 16
;X, Y, Width, Height

;Prepare our palette
InitPalette #PALETTE_MAIN,32
;32 - 32 colours
LoadPalette #PALETTE_MAIN,"background.iff",16 ;Load at a palette offset of 16
LoadPalette #PALETTE_MAIN,"foreground.iff",0 ;First 16 colours

;Manually set the first colour to black
AGAPalRGB #PALETTE_MAIN,0,0,0,0
;Index, Red, Green, Blue

;Configure our copper list settings
;Use a longword (.l) as this variable is too big to fit in a word
COPPERLISTTYPE.l=$8 ;8 bitplanes
COPPERLISTTYPE+$10 ;Smooth scrolling
COPPERLISTTYPE+$20 ;Dual playfield
COPPERLISTTYPE+$10000 ;AGA colours

;This configures our copper list for the display
InitCopList #COPPERLIST_MAIN,44,256,COPPERLISTTYPE,8,32,0
;44 - starting height from beginning of display 
;256 - height of the display (same as 1942 arcade)
;TYPE - our custom settings
;8 - Use 8 sprites
;32 - Use 32 colours
;0 - Use no custom copper commands (such as gradients etc)

;Assign our palette to the display
DisplayPalette #COPPERLIST_MAIN,#PALETTE_MAIN

;Resize the width to 224.
;This isn't pretty and required some experimentation to get to work correctly
DisplayAdjust #COPPERLIST_MAIN,-12,32,-16,64,-32

;Another "ugly" fix. Sets the palette index for playfield 2 to the right place
;This is because on OCS, playfield 2 uses colours 8-15
;But we want to use 16-31 in AGA
DisplayControls #COPPERLIST_MAIN,0,$1C00,0

;Switch to Blitz mode. From here on out we're no longer "system friendly"
BLITZ

;This configures the display.
;You can have several copper lists here for split screen multiplayer etc
CreateDisplay #COPPERLIST_MAIN

;Default position for the fighter
X=100
Y=100

;Offsets for the foreground
#XOFF=16
#YOFF=784

;Initialise "YSCROLL" as a Q, so we can scroll at half a pixel per frame
;Also demonstrating AGA's smooth scrolling
YSCROLL.q

;While Joyb(0)=0 means repeat while no mouse button is down
;Joyb(1) would return the joystick state
;The function returns 1 for left button, 2 for right, 3 for both
While Joyb(0)=0

	;Display the bitmap on screen
	DisplayBitMap #COPPERLIST_MAIN,#BITMAP_FOREGROUND,#XOFF,#YOFF,#BITMAP_BACKGROUND,0,YSCROLL

	;Scroll the game
	YSCROLL - 0.5
	YSCROLL = QWrap(YSCROLL,0,512)
	
	;Adjust X and Y by the joystick position
	X + JoyX(1)
	Y + JoyY(1)
		
	;Limit the value around so we don't go out of bounds
	X = QLimit(X,0,224-25)
	Y = QLimit(Y,0,256-16)
	
	Select #BLITKIND
		Case 0
			Blit #SHAPE_FIGHTER,#XOFF+X,#YOFF+Y
		Case 1
			UnQueue #QUEUEID
			QBlit #QUEUEID,#SHAPE_FIGHTER,#XOFF+X,#YOFF+Y
		Case 2
			UnBuffer #BUFFERID
			BBlit #BUFFERID,#SHAPE_FIGHTER,#XOFF+X,#YOFF+Y		
	End Select
	
	;Wait for 1 frame. This is 1/50th of a second in PAL
	;And 1/60th of a second in NTSC
	VWait
	
Wend

;Quit the program
End
