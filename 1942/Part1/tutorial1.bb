;===================
;PART 1: HELLO WORLD
;===================
;This program supports Work Bench
WBStartup
;Use WORD as the default data type rather than QUICK
;This is faster and better for any time you don't need
;Floating point numbers
DEFTYPE .w

;While Joyb(0)=0 means repeat while no mouse button is down
;Joyb(1) would return the joystick state
;The function returns 1 for left button, 2 for right, 3 for both
While Joyb(0)=0

	;NPrint prints the string on a NEW line
	;As opposed to "Print" which uses the current line
	NPrint "Hello Amiten"
	
	;Wait for 10 frames. This is 1/5th of a second in PAL
	;And 1/6th of a second in NTSC
	VWait 10
	
Wend

;Quit the program
End
