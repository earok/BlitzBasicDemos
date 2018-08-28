;===================
;PART 2: Copper List
;===================
;This program supports Work Bench
WBStartup
;Use WORD as the default data type rather than QUICK
;This is faster and better for any time you don't need
;Floating point numbers
DEFTYPE .w

;Various constants are defined in order to avoid the use of 'magic numbers'
;later in the code
#COPPERLIST_MAIN = 0
#BITMAP_BACKGROUND = 0
#PALETTE_MAIN = 0

;Prepare our background bitmap
BitMap #BITMAP_BACKGROUND,224,768,4
;224 - width of the bitmap
;768 - height of the bitmap
;4 - depth of the bitmap. 4 bitplanes = 16 colours
;Memory used = 224*768*4 = 688,128 bits = 86,016 bytes
LoadBitMap #BITMAP_BACKGROUND,"background.iff"

;Prepare our palette
InitPalette #PALETTE_MAIN,16
;16 - 16 colours
LoadPalette #PALETTE_MAIN,"background.iff",0

;Configure our copper list settings
;Use a longword (.l) as this variable is too big to fit in a word
COPPERLISTTYPE.l=$4 ;4 bitplanes
COPPERLISTTYPE+$10 ;Smooth scrolling
COPPERLISTTYPE+$10000 ;AGA colours

;This configures our copper list for the display
InitCopList #COPPERLIST_MAIN,44,256,COPPERLISTTYPE,8,16,0
;44 - starting height from beginning of display 
;256 - height of the display (same as 1942 arcade)
;TYPE - our custom settings
;8 - Use 8 sprites
;16 - Use 16 colours
;0 - Use no custom copper commands (such as gradients etc)

;Assign our palette to the display
DisplayPalette #COPPERLIST_MAIN,#PALETTE_MAIN

;Resize the width to 224.
;This isn't pretty and required some experimentation to get to work correctly
DisplayAdjust #COPPERLIST_MAIN,-12,32,-16,64,-32

;Switch to Blitz mode. From here on out we're no longer "system friendly"
BLITZ

;This configures the display.
;You can have several copper lists here for split screen multiplayer etc
CreateDisplay #COPPERLIST_MAIN

;While Joyb(0)=0 means repeat while no mouse button is down
;Joyb(1) would return the joystick state
;The function returns 1 for left button, 2 for right, 3 for both
While Joyb(0)=0

	;Display the bitmap on screen
	DisplayBitMap #COPPERLIST_MAIN,#BITMAP_BACKGROUND

	;Wait for 1 frame. This is 1/50th of a second in PAL
	;And 1/60th of a second in NTSC
	VWait
	
Wend

;Quit the program
End
