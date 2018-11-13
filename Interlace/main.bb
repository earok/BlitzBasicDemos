WBStartup
DEFTYPE .w

;The interlaced bitmap
BitMap 0,640,512,8

;The original bitmap
BitMap 1,640,512,8
LoadBitMap 1,"image.iff",0

;Convert the original bitmap to interlaced
Use BitMap 0
for i = 0 to 255
	BlockScroll 0,i*2,640,1,0,i,1 
	BlockScroll 0,1+i*2,640,1,0,i+256,1 
next

;Configure two copper lists
for i = 0 to 1

	InitCopList i,44,DispHeight,$10000 + $1000 + $100 + $8,8,256,0
	DisplayBitMap i,0,0,i*256
	DisplayPalette i,0
		
next
	
BLITZ

;Repeatedly recreate the display and toggle the LOF bit
SetInt 5

	VP = Peek.w($DFF004)

	X=1-X
	if X=0
		CreateDisplay 0
		VP = VP BITSET 15				
	else
		CreateDisplay 1
		VP = VP BITCLR 15						
	endif
	
	Poke.w $DFF02A,VP

	
End SetInt

While Joyb(0)+Joyb(1) = 0
Wend

End
