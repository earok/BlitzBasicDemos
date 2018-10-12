;Rudimentary demo for interleaved bitmaps (including converting a regular bitmap) and blitting
;Advantage of interleaved mode is a reduced blit time (one longer blit as opposed to X number of shorter ones)
;Disadvantage is much larger memory consumption by cookies

DEFTYPE .w
WBStartup

#Bitmap_BG = 0
#Bitmap_Temp = 1
#Bitmap_Virtual = 2

#BOB_Normal = 0
#BOB_Temp = 1

BitMap #Bitmap_BG,640,512,4
ILBMInfo "background.iff"
LoadBitMap #Bitmap_BG,"background.iff",0

InitCopList 0,44,256,$10 + $4,8,32,0
DisplayPalette 0,0

;Make a copy of the bitmap so we can correct the bitplanes from the source
CopyBitMap #Bitmap_BG,#Bitmap_Temp

*b.bitmap = Addr Bitmap(#Bitmap_BG)
*b2.bitmap = Addr Bitmap(#Bitmap_Temp)

;Bit difference between lines
*b\_linemod = (ILBMWidth / 8) * (ILBMDepth)

;Interleave flag (unused?)
*b\_flags = -1
*b\_bitplanemod = (ILBMWidth / 8)

;Correct position of bitplanes
for i = 1 to ILBMDepth-1
    *b\_data[i] = *b\_data[i-1] + *b\_bitplanemod
next

;Finally, copy from the temp copy back to the normal copy line by line
for i = 0 to ILBMDepth-1
    for j = 0 to ILBMHeight-1
        Source.l = *b2\_data[i] + *b2\_linemod * j
        Dest.l = *b\_data[i] + *b\_linemod * j
        CopyMem_ Source, Dest, ILBMWidth / 8
    next
next

;Create a "virtual" one plane bitmap for blitting to
CludgeBitMap #Bitmap_Virtual, 640, 512 * ILBMDepth, 1, *b\_data[0]

;We need to convert our shape into a shape with only one bitplane, but a repeated cookie
LoadShape #BOB_Temp, "shape.iff"
InitShape #BOB_Normal, ShapeWidth(#BOB_Temp), ShapeHeight(#BOB_Temp) * 4, 1

;Establish an empty cookie
MakeCookie #BOB_Normal

*s.shape = Addr Shape(#BOB_Normal)
*s2.shape = Addr Shape(#BOB_Temp)

;Copy the shape
Dest.l = *s\_data
for j = 0 to ShapeHeight(#BOB_Temp)-1
    for i = 0 to ILBMDepth-1
        Source.l = *s2\_data + (*s2\_onebpmem * i) + ((*s2\_pixwidth / 8) * j)
        CopyMem_ Source, Dest, *s2\_pixwidth / 8
        Dest + (*s2\_pixwidth / 8)
    next
next

;Copy the cookie
Source.l = *s2\_cookie
Dest.l = *s\_cookie
for j = 0 to ShapeHeight(#BOB_Temp)-1
    for i = 0 to ILBMDepth-1
        CopyMem_ Source, Dest, *s2\_pixwidth / 8
        Dest + (*s2\_pixwidth / 8)
    next
    Source + (*s2\_pixwidth / 8)
next

BLITZ
CreateDisplay 0

Use BitMap #Bitmap_Virtual
;Need to multiply the Y position by the number of bitplanes
Blit 0,100,100 * 4

While Joyb(1)=0 
    VWait
    DisplayBitMap 0,#Bitmap_BG,X,Y
    X = QLimit (X + JoyX(1),0,320)
    Y = QLimit (Y + JoyY(1),0,256)    
Wend

End
