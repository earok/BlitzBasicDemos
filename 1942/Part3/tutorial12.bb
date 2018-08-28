;==============
;PART 12: MUSIC
;==============

;This program supports Work Bench
WBStartup
;Use WORD as the default data type rather than QUICK
;This is faster and better for any time you don't need
;Floating point numbers
DEFTYPE .w

#BLITKIND=2
;BLIT = 0
;QBLIT = 1
;BBLIT = 2

;Various constants are defined in order to avoid the use of 'magic numbers'
;later in the code
#COPPERLIST_MAIN = 0
#BITMAP_BACKGROUND = 0
#BITMAP_FOREGROUND = #BITMAP_BACKGROUND+1
#BITMAP_SPRITES = #BITMAP_FOREGROUND+1
#PALETTE_MAIN = 0
#SHAPE_FIGHTER = 0
#SHAPE_SHOT = #SHAPE_FIGHTER+1
#SHAPE_ENEMY = #SHAPE_SHOT+1
#SPRITE_SHOT = 0
#QUEUEID = 0
#BUFFERID = 0
#MAX_SHOTS = 8
#MAX_ENEMIES = 4
#SOUND_SHOT = 0
#SOUND_EXPLOSION = 1

;Spawn an enemy after 100 frames = 2 seconds in real time
#SPAWN_ENEMY_EVERY = 100
#SCROLL_HEIGHT=32

;This is our new type for managing player shots
NEWTYPE .Shot
	X.q
	Y.q
End NEWTYPE
;This array holds all of our shot data
Dim Shots.Shot(#MAX_SHOTS)

;By default, we set the Y value to -100, so it's off screen
;Like in most programming languages, arrays start from an index of 0
for i = 0 to #MAX_SHOTS-1
	Shots(0)\Y = -100
next

;This is our new type for managing on screen enemies
NEWTYPE .Enemy
	X.q
	Y.q
	IsAlive.w
End NEWTYPE
Dim Enemies.Enemy(#MAX_ENEMIES)

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
LoadBitMap #BITMAP_BACKGROUND,"background2.iff"

;Prepare our foreground bitmap
BitMap #BITMAP_FOREGROUND,327,1056,4
LoadBitMap #BITMAP_FOREGROUND,"foreground.iff"

;Prepare our sprite bitmap. Note that this is only two bitplanes = four colours
BitMap #BITMAP_SPRITES,96,16,2
LoadBitMap #BITMAP_SPRITES,"sprites.iff"
;We need to 'get' the shot sprite from the bitmap before converting it to a sprite
Use BitMap #BITMAP_SPRITES
GetaShape #SHAPE_SHOT,32,0,16,16
;X, Y, Width, Height
GetaSprite #SPRITE_SHOT, #SHAPE_SHOT

;Rip the fighter from the image as a shape
Use BitMap #BITMAP_FOREGROUND
GetaShape #SHAPE_FIGHTER, 5, 6, 25, 16
;X, Y, Width, Height

;Rip the enemy fighter from the image as a shape
GetaShape #SHAPE_ENEMY, 5, 199, 16, 16

;Prepare our palette
InitPalette #PALETTE_MAIN,48
;48 - 48 colours
LoadPalette #PALETTE_MAIN,"background2.iff",16 ;Load at a palette offset of 16
LoadPalette #PALETTE_MAIN,"foreground.iff",0 ;First 16 colours

;The last 16 colours are just the four sprite colours repeated four times 
;(one for each pair of 4 colour sprites)
;This also demonstrates a 'for loop'
for i = 0 to 3
	LoadPalette #PALETTE_MAIN,"sprites.iff",32 + i * 4
next

;Manually set the first colour to black
AGAPalRGB #PALETTE_MAIN,0,0,0,0
;Index, Red, Green, Blue

;Configure our copper list settings
;Use a longword (.l) as this variable is too big to fit in a word
COPPERLISTTYPE.l=$8 ;8 bitplanes
COPPERLISTTYPE+$10 ;Smooth scrolling
COPPERLISTTYPE+$20 ;Dual playfield
;COPPERLISTTYPE+$1000 ;Fetch mode 1 (faster) - disabled as doesn't work with the screen setup
COPPERLISTTYPE+$10000 ;AGA colours

;This configures our copper list for the display
InitCopList #COPPERLIST_MAIN,44,256,COPPERLISTTYPE,8,48,0
;44 - starting height from beginning of display 
;256 - height of the display (same as 1942 arcade)
;TYPE - our custom settings
;8 - Use 8 sprites
;48 - Use 48 colours
;0 - Use no custom copper commands (such as gradients etc)

;Assign our palette to the display
DisplayPalette #COPPERLIST_MAIN,#PALETTE_MAIN

;Resize the width to 224.
;This isn't pretty and required some experimentation to get to work correctly
DisplayAdjust #COPPERLIST_MAIN,-12,32,-16,64,-32

;Another "ugly" fix. $1C00 Sets the palette index for playfield 2 to the right 
;place. This is because on OCS, playfield 2 uses colours 8-15
;But we want to use 16-31 in AGA
;$33 makes the Amiga use colours 32-47 for sprites, not 16-31
DisplayControls #COPPERLIST_MAIN,0,$1C00,$33

;Load our two sounds
LoadSound #SOUND_SHOT,"shot.8svx"
LoadSound #SOUND_EXPLOSION,"explosion.8svx"

;Load our music and play it
if not LoadTrackerModule(0,"song.mod")
	Print "Cannot load tracker module"
	End
endif
if not StartTracker(0)
	Print "Cannot start tracker module"
	End
endif

;By using this command, we're making sure that the tracker only uses the first two channels
;Leaving channels 3+4 available for our sound effects
ChangeTrackerMask 1+2

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

;Offsets for the sprites
#XOFFSPRITE=64

;The first shot we want to fire is the '0th' shot
NextShot.w=0

;The first enemy we want to spawn is the '0th' enemy
NextEnemy.w=0
SpawnTimer=#SPAWN_ENEMY_EVERY

;Set the score to 0
Score.w = 0
;The score will be formatted to have at least five digits
Format "000000"
;Tell Blitz we want to write the text on the "foreground" bitmap
BitMapOutput #BITMAP_FOREGROUND

;Initialise "YSCROLL" as a Q, so we can scroll at half a pixel per frame
;Also demonstrating AGA's smooth scrolling
YSCROLL.q = 512

;Scroll blocks of height 32 every time 32 pixels are scrolled
YSCROLLCountDown.q=#SCROLL_HEIGHT

;For this tutorial, we will quit when the enemy has hit a player
PlayerIsAlive = True
While PlayerIsAlive

	;Display the bitmap on screen
	DisplayBitMap #COPPERLIST_MAIN,#BITMAP_FOREGROUND,#XOFF,#YOFF,#BITMAP_BACKGROUND,0,YSCROLL

	;Scroll the game
	YSCROLL - 0.5
	YSCROLL = QWrap(YSCROLL,0,512)
	
	;Adjust X and Y by the joystick position
	;Have introduced a multiplier here to increase the sensitivity
	X + JoyX(1) * 2
	Y + JoyY(1) * 2
		
	;Limit the value around so we don't go out of bounds
	X = QLimit(X,0,224-25)
	Y = QLimit(Y,0,256-16)
	
	;Draw to the foreground bitmap
	Use BitMap #BITMAP_FOREGROUND
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
	
	;Handle each of the shots sprite for shot. We don't need to move it if it's already off screen
	;Move it by 5.5 pixels per frame
	for i = 0 to #MAX_SHOTS-1
		if Shots(i)\Y > -100
			Shots(i)\Y - 5.5		
		endif
		;Draw the sprite on screen
		DisplaySprite #COPPERLIST_MAIN,#SPRITE_SHOT,#XOFFSPRITE+Shots(i)\X,Shots(i)\Y,i
		;The above should be self explanatory, except 'i' is a reference to the
		;sprite channel
		;We can use eight at once before copper tricks come into play		
	next
	
	;Spawn an enemy once the timer has expired
	SpawnTimer - 1
	if SpawnTimer = 0
		
		;Set that enemy is alive
		Enemies(NextEnemy)\IsAlive = true
		
		;Place the enemy just above the screen
		Enemies(NextEnemy)\Y = -16
		
		;Place the enemy at a random position on the X Axis
		Enemies(NextEnemy)\X = Rnd(224-16)
		
		;Reset the count down
		SpawnTimer = #SPAWN_ENEMY_EVERY
		
		;Increase the "NextEnemy" variable, but wrap it around if it reaches 8
		NextEnemy = QWrap(NextEnemy+1,0,#MAX_ENEMIES)
		
	endif
	
	;Handle each of the enemies
	for i = 0 to #MAX_ENEMIES-1
		if Enemies(i)\IsAlive
									
			;Move the enemy down the screen
			Enemies(i)\Y + 2
			
			;Blit the enemy to screen
			Select #BLITKIND
				Case 0
					Blit #SHAPE_ENEMY,#XOFF+Enemies(i)\X,#YOFF+Enemies(i)\Y
				Case 1
					QBlit #QUEUEID,#SHAPE_ENEMY,#XOFF+Enemies(i)\X,#YOFF+Enemies(i)\Y
				Case 2
					BBlit #BUFFERID,#SHAPE_ENEMY,#XOFF+Enemies(i)\X,#YOFF+Enemies(i)\Y		
			End Select
						
			;Did the enemy plane hit the player?
			if ShapesHit(#SHAPE_ENEMY,Enemies(i)\X,Enemies(i)\Y,#SHAPE_FIGHTER,X,Y)
				PlayerIsAlive = false
			endif
			
			;Did the shot hit this enemy?
			;We are doing an 'inner loop' here, which can be slow
			for j = 0 to #MAX_SHOTS-1
				if ShapeSpriteHit(#SHAPE_ENEMY,Enemies(i)\X,Enemies(i)\Y,#SPRITE_SHOT,Shots(j)\X,Shots(j)\Y)
				
					;Successful hit!
					;Destroy enemy
					Enemies(i)\IsAlive = false
					
					;Move this shot off screen
					Shots(j)\Y = -100
					
					;Increase our score by 10
					Score + 10
					
					;Play the explosion sound on the RIGHT channels not used by music
					Sound #SOUND_EXPLOSION,4
					
				endif
			next
			
			;Destroy the enemy once it reaches the bottom of the screen
			if Enemies(i)\Y > 255
				Enemies(i)\IsAlive = false
			endif
			
		endif
	next
	
	;If the joystick button has been pressed, position the shot on the fighter
	if Joyb(1)=1 and OldJoyb=0
	
		;Play the shot sound on the LEFT channels not used by music
		Sound #SOUND_SHOT,8
	
		;3 and 5 positions the shots directly on the propellors
		Shots(NextShot)\X = X + 3
		Shots(NextShot)\Y = Y - 5
		
		;Increase the "NextShot" variable, but wrap it around if it reaches 8
		NextShot = QWrap(NextShot+1,0,#MAX_SHOTS)
		
	endif
	
	;Remember what the current joystick button state is for next time
	;(So we don't shoot repeatedly)
	OldJoyb = Joyb(1)
	
	;Draw the score on Screen
	;Move the text 'cursor' to this location
	;(X and Y locations divided by 8)
	Locate 24 / 8,792 / 8
	;Finally, write the score, formatted to be six digits
	;The 'Str$' command converts a number to a string
	Print Str$(Score)
	
	;Do a "block scroll" every 16 pixels
	YSCROLLCountDown - .5
	if YSCROLLCountDown=0
		
		;Draw to the background bitmap
		Use BitMap #BITMAP_BACKGROUND
	
		;Copy the water from the very top of the background image
		;To just after the viewable area (the part we've passed)
		;Wrap around so we don't blit outside of the bitmap
		BottomArea = QWrap(YSCROLL+256,0,768 - #SCROLL_HEIGHT)
		BlockScroll 0,0,224,#SCROLL_HEIGHT,0,BottomArea
		YSCROLLCountDown = #SCROLL_HEIGHT
		
	endif
	
	;Wait for 1 frame. This is 1/50th of a second in PAL
	;And 1/60th of a second in NTSC
	VWait
	
Wend

;Quit the program
End
