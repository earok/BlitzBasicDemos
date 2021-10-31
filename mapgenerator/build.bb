WBStartup
DEFTYPE .w

#TILES=100
#OCTAVES=10

#GRASS=0
#DIRT=1
#INNERDIRT=2
#STONE=3
#WATER=4
#INNERWATER=5

#STONECENTER=6
#WATERCENTER=7
#DIRTCENTER=8

#BC_STONELEFT=1
#BC_STONERIGHT=2
#BC_WATERBRIDGE_TOP=3
#BC_WATERBRIDGE_BOTTOM=4
#BC_WATERBRIDGE_LEFT=3
#BC_WATERBRIDGE_RIGHT=4

NEWTYPE .TObstacle

	Offset.w
	Height.w
	Width.w
	T.w
	Special.w
	
End NEWTYPE

Macro DeclareObstacle
	ObstacleList(OBSTACLES)\Offset=`1
	ObstacleList(OBSTACLES)\Width=`2
	ObstacleList(OBSTACLES)\Height=`3
	ObstacleList(OBSTACLES)\T=`4
	
	CNIF `0=5
		ObstacleList(OBSTACLES)\Special=`5
	CEND
	
	OBSTACLES+1
End Macro

OBSTACLES=0

Dim ObstacleList.TObstacle(1024)
!DeclareObstacle{59,1,1,#WATER}
!DeclareObstacle{79,1,2,#WATER}
!DeclareObstacle{119,1,1,#WATER}
!DeclareObstacle{139,1,1,#WATER}
!DeclareObstacle{159,1,1,#WATER}
!DeclareObstacle{338,2,2,#WATER}
;!DeclareObstacle{290,1,1,#WATER} ;SIGN!

!DeclareObstacle{133,4,4,#GRASS}
!DeclareObstacle{213,4,4,#GRASS}
!DeclareObstacle{137,2,2,#GRASS}
!DeclareObstacle{177,2,2,#GRASS}
!DeclareObstacle{310,2,2,#GRASS}
!DeclareObstacle{296,2,2,#GRASS}
!DeclareObstacle{217,1,1,#GRASS}
!DeclareObstacle{218,1,1,#GRASS}
!DeclareObstacle{237,1,1,#GRASS}
!DeclareObstacle{257,1,1,#GRASS}
!DeclareObstacle{277,1,1,#GRASS}
!DeclareObstacle{219,1,2,#GRASS}

!DeclareObstacle{298,2,2,#DIRT}
!DeclareObstacle{133,4,4,#DIRT,5}
!DeclareObstacle{213,4,4,#DIRT,2}
!DeclareObstacle{238,1,1,#DIRT}
!DeclareObstacle{258,1,1,#DIRT}
!DeclareObstacle{278,1,1,#DIRT}
!DeclareObstacle{199,1,1,#DIRT}
!DeclareObstacle{259,1,2,#DIRT}

Dim BaseNoise.q(#TILES,#TILES)
Dim SmoothNoise.q(#OCTAVES,#TILES,#TILES)
Dim CombinedNoise.q(#TILES,#TILES)
Dim IslandMap.b(#TILES,#TILES)
Dim MapData.w(#TILES,#TILES)
Dim BridgeConnectors.w(#TILES,#TILES)

Function.w RndTile2{t1,t2}
	
	Select Int(Rnd(2))
		Case 0
			Function Return t1
		Case 1
			Function Return t2				
	End Select
		
End Function

Function.w RndTile3{t1,t2,t3}
	
	Select Int(Rnd(3))
		Case 0
			Function Return t1
		Case 1
			Function Return t2	
		Case 2
			Function Return t3			
	End Select
		
End Function

Function.w RndTile4{t1,t2,t3,t4}
	
	Select Int(Rnd(4))
		Case 0
			Function Return t1
		Case 1
			Function Return t2		
		Case 2
			Function Return t3		
		Case 3
			Function Return t4			
	End Select
		
End Function

Function.w RndTile5{t1,t2,t3,t4,t5}
	
	Select Int(Rnd(5))
		Case 0
			Function Return t1
		Case 1
			Function Return t2		
		Case 2
			Function Return t3		
		Case 3
			Function Return t4		
		Case 4
			Function Return t5		
	End Select
		
End Function

Function.w RndTile6{t1,t2,t3,t4,t5,t6}
	
	Select Int(Rnd(6))
		Case 0
			Function Return t1
		Case 1
			Function Return t2		
		Case 2
			Function Return t3		
		Case 3
			Function Return t4		
		Case 4
			Function Return t5		
		Case 5
			Function Return t6
	End Select
		
End Function

Function.w CountNeighbour{x,y}

	Shared IslandMap()
	Shared Threshold.q
	
	;Bounds checks
	if x < 0 or y < 0 or x >= #TILES or y >= #TILES
		Function Return 0
	endif
	
	Function Return IslandMap(x,y)
	
End Function

Statement Flood{x,y,value,valuefrom}

	Shared IslandMap()

	IslandMap(x,y)=value
	
	.FloodLoop
	AnyFloodDirty=false	
	
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
		
			;This is a floodable tile, check if connected
			if CountNeighbour{i,j} = valuefrom
				for x = -1 to 1
					for y = -1 to 1
						
						if (x <> 0 or y <> 0) and CountNeighbour{i+x,j+y}=value
							IslandMap(i,j)=value
							x = 2
							y = 2
							AnyFloodDirty=true
						endif
						
					next
				next
			endif
		next
	next
	
	if AnyFloodDirty
		Goto FloodLoop
	endif
	
End Statement

Function.q Interpolate{x0.q, x1.q, alpha.q}
   Function Return x0 * (1 - alpha) + alpha * x1
End Function

Statement GenerateSmoothNoise{Octave.b}

	Shared BaseNoise()
	Shared SmoothNoise()

	samplePeriod = 1 LSL Octave
	sampleFrequency.q = 1 / samplePeriod
	
   for i = 0 to #TILES - 1
   
	  ;calculate the horizontal sampling indices
	  sample_i0 = (i / samplePeriod) * samplePeriod;
	  sample_i1 = (sample_i0 + samplePeriod) mod #TILES; //wrap around
	  horizontal_blend.q = (i - sample_i0) * sampleFrequency;
 
	  for j = 0 to #TILES - 1
	  
		 ;calculate the vertical sampling indices
		 sample_j0 = (j / samplePeriod) * samplePeriod;
		 sample_j1 = (sample_j0 + samplePeriod) mod #TILES; //wrap around
		 vertical_blend.q = (j - sample_j0) * sampleFrequency;
 
		 ;blend the top two corners
		 top.q = Interpolate{BaseNoise(sample_i0,sample_j0), BaseNoise(sample_i1,sample_j0), horizontal_blend}
 
		 ;blend the bottom two corners
		 bottom.q = Interpolate{BaseNoise(sample_i0,sample_j1),BaseNoise(sample_i1,sample_j1), horizontal_blend}
 
		 ;final blend
		 SmoothNoise(Octave,i,j) = Interpolate{top, bottom, vertical_blend}
		 
	  next
	  
   next
	
End Statement

Statement GenerateNoise{}

	Shared BaseNoise()
	Shared CombinedNoise()
	Shared SmoothNoise()

	;Generate the base noise
	For x = 0 to #TILES-1
		for y = 0 to #TILES-1
			BaseNoise(x,y) = Rnd
		next
	next

	;Generate the smooth noise at each octave
	for i = 0 to #OCTAVES-1
		GenerateSmoothNoise{i}
	next

	;Combine the noise
	Amplitude.q=1
	TotalAmplitude.q=0
	for k = 0 to #OCTAVES-1

		Amplitude * 0.5
		TotalAmplitude + Amplitude

		for i = 0 to #TILES-1
			for j = 0 to #TILES-1
			
				if k = 0
					CombinedNoise(i,j) = SmoothNoise(k,i,j) * Amplitude
				else
					CombinedNoise(i,j) + SmoothNoise(k,i,j) * Amplitude				
				endif
			next
		next
		
	next
	
	;Finally, normalise
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			CombinedNoise(i,j) / TotalAmplitude
		next
	next	

End Statement

#MAPBITMAP=0 : BitMap #MAPBITMAP,#TILES*16,#TILES*16,4
#TILEMAPBITMAP=1 : BitMap #TILEMAPBITMAP,320,640,4 : LoadBitMap #TILEMAPBITMAP,"Tileset.iff",0

NPrint "Enter Threshold Level for dirt"
NPrint "Lower = more dirt"
NPrint "Values around .7 recommended"
Threshold.q = Edit(10)

NPrint "Enter number of maps to generate"
total = Edit(2)

Use BitMap #TILEMAPBITMAP
k=0
for j = 0 to 40-1
	for i = 0 to 20-1
		GetaShape k,i*16,j*16,16,16
		k+1
	next
next

While loop < total

	NPrint "Processing map " + UStr$(loop+1) + " of " + UStr$(total)

	Use BitMap #MAPBITMAP
	Cls
	
	GenerateNoise{}

	;Move to island map
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			if CombinedNoise(i,j) >= Threshold
				IslandMap(i,j) = 1
			else
				IslandMap(i,j) = 0
			endif
		next
	next
	
	;Use cellular automata to close the gaps
	.Automata
	AnyDirty=false

	NPrint "Tidying up dirt islands"
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1		
		
			LeftDirt = CountNeighbour{i-1,j}
			RightDirt  = CountNeighbour{i+1,j}
			TopDirt = CountNeighbour{i,j-1}
			BottomDirt  = CountNeighbour{i,j+1}
				
			TopLeftDirt = CountNeighbour{i-1,j-1}
			TopRightDirt = CountNeighbour{i+1,j-1}				
			BottomLeftDirt = CountNeighbour{i-1,j+1}
			BottomRightDirt = CountNeighbour{i+1,j+1}		
		
			;Not dirt, but fill in if necessary
			if CountNeighbour{i,j} = 0

				if (LeftDirt and RightDirt)
					AnyDirty=true
					IslandMap(i,j)=1
				endif		
				
				if TopDirt and BottomDirt
					AnyDirty=true
					IslandMap(i,j)=1			
				endif
				
				if (TopLeftDirt and BottomRightDirt) or (TopRightDirt and BottomLeftDirt)
					AnyDirty=true
					IslandMap(i,j)=1		
				endif

				if TopRightDirt and (BottomDirt)
					AnyDirty=true
					IslandMap(i,j)=1				
				endif				
						
			else
						
				Count = 0
				for x = -1 to 1
					for y = -1 to 1
						Count + CountNeighbour{i+x,j+y}
					next
				next
				
				if Count < 2
					IslandMap(i,j)=0			
					AnyDirty=true
				endif		
				
			endif
			
		next
	next
	
	if AnyDirty
		Goto Automata
	endif
	
	NPrint "Flood filling islands"
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			if IslandMap(i,j) > 0
				IslandMap(i,j) = -1
			endif
		next
	next	
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			if IslandMap(i,j) = -1
			
			Select Int(Rnd(3))
				Case 0
					Flood{i,j,#DIRT,-1} ;This is Dirt				
				Case 1
					Flood{i,j,#STONE,-1} ;This is Stone								
				Case 2
					Flood{i,j,#WATER,-1} ;This is Water						
			End Select
							
			endif
		next
	next
		
	NPrint "Generating inner dirt"
	;Use the combined noise array to store temporary values
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			
			CombinedNoise(i,j) = IslandMap(i,j)
			
			Miss = false
			ThisValue = CountNeighbour{i,j}
			
			for x = -2 to 2
				for y = -2 to 2
					if ThisValue <> CountNeighbour{i+x,j+y}
						Miss = true
					endif
				next
			next
			
			if Miss = false
				Select ThisValue
					Case #DIRT ;Dirt, make this inner dirt
						CombinedNoise(i,j)=#INNERDIRT
					Case #WATER
						CombinedNoise(i,j)=#WATERCENTER
					Case #STONE
						CombinedNoise(i,j)=#STONECENTER
				End Select
			endif
			
		next
	next
	
	;Apply the island map back to the regular map
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
			IslandMap(i,j)=CombinedNoise(i,j)
		next
	next
	
	NPrint "Flood filling inner dirt"
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
		
			Select CombinedNoise(i,j)
				Case #INNERDIRT
					IslandMap(i,j) = -1
				Case #WATERCENTER
					IslandMap(i,j) = -2
				Case #STONECENTER
					IslandMap(i,j) = -3			
			End Select
			
		next
	next		
	for i = 0 to #TILES-1
		for j = 0 to #TILES-1
				
			Select IslandMap(i,j)
			
				Case -1
				
					Select Int(Rnd(3))
						Case 0
							Flood{i,j,#INNERDIRT,-1} ;This is Inner Dirt				
						Case 1
							Flood{i,j,#INNERWATER,-1} ;This is Inner Water						
						Case 2
							Flood{i,j,#DIRTCENTER,-1} ;This is inner grass						
					End Select
					
				Case -2
				
					;Select Int(Rnd(2))
					;	Case 0
					;		Flood{i,j,#WATER,-2}				
					;	Case 1
							Flood{i,j,#WATERCENTER,-2} 					
					;End Select					
					
				Case -3
				
					;Select Int(Rnd(2))
					;	Case 0
					;		Flood{i,j,#STONE}				
					;	Case 1
							Flood{i,j,#STONECENTER,-3} 					
					;End Select									
					
			End Select
										
		next
	next	
	
	;Regenerate noise for detail
	NPrint("Rendering background")
	GenerateNoise{}

	for x = 0 to #TILES-1
		for y = 0 to #TILES-1
			BridgeConnectors(x,y)=0
		next
	next		
	
	;Test output
	for i = 0 to #TILES-1	
		for j = 0 to #TILES-1
		
			;Select tile
		
			Select IslandMap(i,j) 
						
			Case #DIRTCENTER
			
				Tile=20
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					if Rnd < .5
						Tile=RndTile5{100,120,140,160,180}
					else
						Tile=RndTile5{348,349,350,351,352}				
					endif				
				endif							
						
			Case #WATERCENTER
			
				Tile=20
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					if Rnd < .5
						Tile=RndTile5{100,120,140,160,180}
					else
						Tile=RndTile5{348,349,350,351,352}				
					endif				
				endif				
						
			Case #STONECENTER
			
				Tile=20
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					if Rnd < .5
						Tile=RndTile5{100,120,140,160,180}
					else
						Tile=RndTile5{348,349,350,351,352}				
					endif				
				endif				
						
			Case #INNERWATER
			
				Tile=80			
			
			Case #WATER
			
				Tile=80
			
				;Double neighbour
				if CountNeighbour{i-1,j}=7 and CountNeighbour{i,j-1}=7 : Tile=RndTile2{62+120,1+120} : goto FinishedNeighboursW : endif
				if CountNeighbour{i+1,j}=7 and CountNeighbour{i,j-1}=7 : Tile=RndTile2{63+120,6+120} : goto FinishedNeighboursW : endif
				if CountNeighbour{i-1,j}=7 and CountNeighbour{i,j+1}=7 : Tile=RndTile2{82+120,101+120} : goto FinishedNeighboursW : endif
				if CountNeighbour{i+1,j}=7 and CountNeighbour{i,j+1}=7 : Tile=RndTile2{83+120,106+120} : goto FinishedNeighboursW : endif
		
				;Single neighbour?
				if CountNeighbour{i-1,j}=7 
					Tile=RndTile4{21+120,41+120,61+120,81+120} 
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_LEFT
					goto FinishedNeighboursW 					
				endif
				
				if CountNeighbour{i+1,j}=7 
					Tile=RndTile4{26+120,46+120,66+120,86+120} 
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_RIGHT					
					goto FinishedNeighboursW
				endif				
				
				if CountNeighbour{i,j-1}=7 				
					Tile=RndTile4{2+120,3+120,4+120,5+120}
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_TOP				
					goto FinishedNeighboursW
				endif
				
				if CountNeighbour{i,j+1}=7 
					Tile=RndTile4{102+120,103+120,104+120,105+120}
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_BOTTOM					
					goto FinishedNeighboursW
				endif					
			
				if CountNeighbour{i+1,j+1}=7 : Tile=22+120 : goto FinishedNeighboursW : endif
				if CountNeighbour{i-1,j+1}=7 : Tile=23+120 : goto FinishedNeighboursW : endif
				if CountNeighbour{i+1,j-1}=7 : Tile=42+120 : goto FinishedNeighboursW : endif
				if CountNeighbour{i-1,j-1}=7 : Tile=43+120 : goto FinishedNeighboursW : endif			
			
				Goto SkipWaterReset
						
				.FinishedNeighboursW
				;Flag this as being an "inner grass" tile for road bridges
				IslandMap(i,j)=#GRASS			
			
				.SkipWaterReset
			
			Case #STONE
						
				;Double neighbour
				if CountNeighbour{i-1,j}=6 and CountNeighbour{i,j-1}=6 : Tile=RndTile2{62+12,1+12} : goto FinishedNeighboursS : endif
				if CountNeighbour{i+1,j}=6 and CountNeighbour{i,j-1}=6 : Tile=RndTile2{63+12,6+12} : goto FinishedNeighboursS : endif
				if CountNeighbour{i-1,j}=6 and CountNeighbour{i,j+1}=6 : Tile=RndTile2{82+12,101+12} : goto FinishedNeighboursS : endif
				if CountNeighbour{i+1,j}=6 and CountNeighbour{i,j+1}=6 : Tile=RndTile2{83+12,106+12} : goto FinishedNeighboursS : endif
		
				;Single neighbour?
				if CountNeighbour{i-1,j}=6 
					Tile=RndTile4{21+12,41+12,61+12,81+12}
					BridgeConnectors(i,j)=#BC_STONELEFT					
					goto FinishedNeighboursS
				endif
					
				if CountNeighbour{i+1,j}=6 
					Tile=RndTile4{26+12,46+12,66+12,86+12} 
					BridgeConnectors(i,j)=#BC_STONERIGHT					
					goto FinishedNeighboursS 
				endif				
				
				if CountNeighbour{i,j-1}=6 : Tile=RndTile4{2+12,3+12,4+12,5+12} : goto FinishedNeighboursS : endif
				if CountNeighbour{i,j+1}=6 : Tile=RndTile4{102+12,103+12,104+12,105+12} : goto FinishedNeighboursS : endif					
			
				if CountNeighbour{i+1,j+1}=6 : Tile=22+12 : goto FinishedNeighboursS : endif
				if CountNeighbour{i-1,j+1}=6 : Tile=23+12 : goto FinishedNeighboursS : endif
				if CountNeighbour{i+1,j-1}=6 : Tile=42+12 : goto FinishedNeighboursS : endif
				if CountNeighbour{i-1,j-1}=6 : Tile=43+12 : goto FinishedNeighboursS : endif				
						
				if CombinedNoise(i,j) > 0.66				
					Tile=RndTile3{76,77,97}								
				else
					Tile=RndTile5{36,37,56,57,96}					
				endif
				
				Goto SkipStoneReset
				
				.FinishedNeighboursS				
				;Flag this as being an "inner grass" tile for road bridges
				IslandMap(i,j)=#GRASS
				
				.SkipStoneReset
				
			Case #INNERDIRT
			
				Tile=60 ;Inner dirt
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					Tile=RndTile4{64+6,65+6,84+6,85+6}
				endif
			
			Case #DIRT
			
				Tile=40 ;Dirt
				
				;INNER GRASS
				;Double neighbour
				if CountNeighbour{i-1,j}=8 and CountNeighbour{i,j-1}=8 : Tile=RndTile2{62,1} : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j}=8 and CountNeighbour{i,j-1}=8 : Tile=RndTile2{63,6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j}=8 and CountNeighbour{i,j+1}=8 : Tile=RndTile2{82,101} : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j}=8 and CountNeighbour{i,j+1}=8 : Tile=RndTile2{83,106} : goto FinishedNeighboursD : endif
		
				;Single neighbour?
				if CountNeighbour{i-1,j}=8 : Tile=RndTile4{21,41,61,81} : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j}=8 : Tile=RndTile4{26,46,66,86} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j-1}=8 : Tile=RndTile4{2,3,4,5} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j+1}=8 : Tile=RndTile4{102,103,104,105} : goto FinishedNeighboursD : endif					
			
				if CountNeighbour{i+1,j+1}=8 : Tile=22 : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j+1}=8 : Tile=23 : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j-1}=8 : Tile=42 : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j-1}=8 : Tile=43 : goto FinishedNeighboursD : endif					
				
				
				;INNER WATER
				if CountNeighbour{i+1,j}=5 and CountNeighbour{i,j+1}=5 : Tile=43+126 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i+1,j}=5 and CountNeighbour{i,j-1}=5 : Tile=23+126 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i-1,j}=5 and CountNeighbour{i,j+1}=5 : Tile=42+126 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i-1,j}=5 and CountNeighbour{i,j-1}=5 : Tile=22+126 : goto FinishedNeighboursD : endif					
				
				;Single neighbour?
				if CountNeighbour{i+1,j}=5 : Tile=RndTile4{21+126,41+126,61+126,81+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j}=5 : Tile=RndTile4{26+126,46+126,66+126,86+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j+1}=5 : Tile=RndTile4{2+126,3+126,4+126,5+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j-1}=5 : Tile=RndTile4{102+126,103+126,104+126,105+126} : goto FinishedNeighboursD : endif
				
				if CountNeighbour{i+1,j+1}=5 : Tile=RndTile2{62+126,1+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j+1}=5 : Tile=RndTile2{63+126,6+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j-1}=5 : Tile=RndTile2{82+126,101+126} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j-1}=5 : Tile=RndTile2{83+126,106+126} : goto FinishedNeighboursD : endif				
								
				;INNER DIRT
				;Double neighbour
				if CountNeighbour{i+1,j}=2 and CountNeighbour{i,j+1}=2 : Tile=43+6 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i+1,j}=2 and CountNeighbour{i,j-1}=2 : Tile=23+6 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i-1,j}=2 and CountNeighbour{i,j+1}=2 : Tile=42+6 : goto FinishedNeighboursD : endif			
				if CountNeighbour{i-1,j}=2 and CountNeighbour{i,j-1}=2 : Tile=22+6 : goto FinishedNeighboursD : endif					
				
				;Single neighbour?
				if CountNeighbour{i+1,j}=2 : Tile=RndTile4{21+6,41+6,61+6,81+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j}=2 : Tile=RndTile4{26+6,46+6,66+6,86+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j+1}=2 : Tile=RndTile4{2+6,3+6,4+6,5+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i,j-1}=2 : Tile=RndTile4{102+6,103+6,104+6,105+6} : goto FinishedNeighboursD : endif
				
				if CountNeighbour{i+1,j+1}=2 : Tile=RndTile2{62+6,1+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j+1}=2 : Tile=RndTile2{63+6,6+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i+1,j-1}=2 : Tile=RndTile2{82+6,101+6} : goto FinishedNeighboursD : endif
				if CountNeighbour{i-1,j-1}=2 : Tile=RndTile2{83+6,106+6} : goto FinishedNeighboursD : endif
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					Tile=RndTile6{44,45,64,65,84,85}
				endif
				
				.FinishedNeighboursD				
				
			Case #GRASS
			
				Tile=20 ;Grass		
				
				;WATER NEIGHBOUR
				;Double neighbour
				if CountNeighbour{i+1,j}=4 and CountNeighbour{i,j+1}=4 : Tile=43+120 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i+1,j}=4 and CountNeighbour{i,j-1}=4 : Tile=23+120 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=4 and CountNeighbour{i,j+1}=4 : Tile=42+120 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=4 and CountNeighbour{i,j-1}=4 : Tile=22+120 : goto FinishedNeighboursG : endif	
				
				;Single neighbour?
				if CountNeighbour{i+1,j}=4 
					Tile=RndTile4{21+120,41+120,61+120,81+120}
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_LEFT					
					goto FinishedNeighboursG
				endif
				
				if CountNeighbour{i-1,j}=4
					Tile=RndTile4{26+120,46+120,66+120,86+120}
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_RIGHT					
					goto FinishedNeighboursG 
				endif
				
				if CountNeighbour{i,j+1}=4 
					Tile=RndTile4{2+120,3+120,4+120,5+120} 
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_TOP
					goto FinishedNeighboursG 
				endif
				
				if CountNeighbour{i,j-1}=4 
					Tile=RndTile4{102+120,103+120,104+120,105+120} 
					BridgeConnectors(i,j)=#BC_WATERBRIDGE_BOTTOM					
					goto FinishedNeighboursG
				endif

				if CountNeighbour{i+1,j+1}=4 : Tile=RndTile2{62+120,1+120} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j+1}=4 : Tile=RndTile2{63+120,6+120} : goto FinishedNeighboursG : endif
				if CountNeighbour{i+1,j-1}=4 : Tile=RndTile2{82+120,101+120} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j-1}=4 : Tile=RndTile2{83+120,106+120} : goto FinishedNeighboursG : endif					
				
				;STONE NEIGHBOUR
				;Double neighbour
				if CountNeighbour{i+1,j}=3 and CountNeighbour{i,j+1}=3 : Tile=43+12 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i+1,j}=3 and CountNeighbour{i,j-1}=3 : Tile=23+12 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=3 and CountNeighbour{i,j+1}=3 : Tile=42+12 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=3 and CountNeighbour{i,j-1}=3 : Tile=22+12 : goto FinishedNeighboursG : endif	
				
				;Single neighbour?
				if CountNeighbour{i+1,j}=3 
					Tile=RndTile4{21+12,41+12,61+12,81+12}
					BridgeConnectors(i,j)=#BC_STONELEFT
					goto FinishedNeighboursG
				endif
				
				if CountNeighbour{i-1,j}=3
					Tile=RndTile4{26+12,46+12,66+12,86+12}
					BridgeConnectors(i,j)=#BC_STONERIGHT
					goto FinishedNeighboursG
				endif								
				
				if CountNeighbour{i,j+1}=3 : Tile=RndTile4{2+12,3+12,4+12,5+12} : goto FinishedNeighboursG : endif
				if CountNeighbour{i,j-1}=3 : Tile=RndTile4{102+12,103+12,104+12,105+12} : goto FinishedNeighboursG : endif

				if CountNeighbour{i+1,j+1}=3 : Tile=RndTile2{62+12,1+12} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j+1}=3 : Tile=RndTile2{63+12,6+12} : goto FinishedNeighboursG : endif
				if CountNeighbour{i+1,j-1}=3 : Tile=RndTile2{82+12,101+12} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j-1}=3 : Tile=RndTile2{83+12,106+12} : goto FinishedNeighboursG : endif				
				
				;DIRT NEIGHBOUR
				;Double neighbour
				if CountNeighbour{i+1,j}=1 and CountNeighbour{i,j+1}=1 : Tile=43 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i+1,j}=1 and CountNeighbour{i,j-1}=1 : Tile=23 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=1 and CountNeighbour{i,j+1}=1 : Tile=42 : goto FinishedNeighboursG : endif			
				if CountNeighbour{i-1,j}=1 and CountNeighbour{i,j-1}=1 : Tile=22 : goto FinishedNeighboursG : endif	
				
				;Single neighbour?
				if CountNeighbour{i+1,j}=1 : Tile=RndTile4{21,41,61,81} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j}=1 : Tile=RndTile4{26,46,66,86} : goto FinishedNeighboursG : endif
				if CountNeighbour{i,j+1}=1 : Tile=RndTile4{2,3,4,5} : goto FinishedNeighboursG : endif
				if CountNeighbour{i,j-1}=1 : Tile=RndTile4{102,103,104,105} : goto FinishedNeighboursG : endif

				if CountNeighbour{i+1,j+1}=1 : Tile=RndTile2{62,1} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j+1}=1 : Tile=RndTile2{63,6} : goto FinishedNeighboursG : endif
				if CountNeighbour{i+1,j-1}=1 : Tile=RndTile2{82,101} : goto FinishedNeighboursG : endif
				if CountNeighbour{i-1,j-1}=1 : Tile=RndTile2{83,106} : goto FinishedNeighboursG : endif
				
				;Pick a detail tile
				if CombinedNoise(i,j) > 0.66				
					if Rnd < .5
						Tile=RndTile5{100,120,140,160,180}
					else
						Tile=RndTile5{348,349,350,351,352}				
					endif				
				endif
				
				.FinishedNeighboursG
				
			End Select
			
			Blit Tile,i*16,j*16
			
		next		
	next

	for x = 0 to #TILES-1
		for y = 0 to #TILES-1
			MapData(x,y)=0
		next
	next	
	
	;Do bridges where-ever possible
	NPrint("Generating bridges")
	
	;Road bridges
	for i = 0 to #TILES-4
		for j = 0 to #TILES-2
			
			;Any collisions?
			hit=true
			for x = 0 to 3
				for y = 0 to 1
					if MapData(i+x,j+y)
						hit=false
					endif
				next
			next
			
			if BridgeConnectors(i,j) <> #BC_STONELEFT : hit = false : endif
			if BridgeConnectors(i,j+1) <> #BC_STONELEFT : hit = false : endif
			if BridgeConnectors(i+3,j) <> #BC_STONERIGHT : hit = false : endif
			if BridgeConnectors(i+3,j+1) <> #BC_STONERIGHT : hit = false : endif
			
			if hit
				NPrint "Drawing road bridge!"
				for x = 0 to 3
					for y = 0 to 1
						MapData(i+x,j+y)=1
						Blit 371 + x + y*20,(i+x) * 16,(j+y) * 16
					next
				next
			endif
			
		next
	next
	
	;Vertical water bridges
	for i = 0 to #TILES-4
		for j = 0 to #TILES - 5 - 5
												
			hit=true
			
			;Check that the connectors are in place for the top
			if BridgeConnectors(i  ,j+1) <> #BC_WATERBRIDGE_TOP : hit = false : endif
			if BridgeConnectors(i+3,j+1) <> #BC_WATERBRIDGE_TOP : hit = false : endif
			
			if hit = false
				Goto CancelBridge
			endif
			
			;Try to connect to the bottom
			for BridgeLength = 1 to 5
			
				hit = true
				if BridgeConnectors(i  ,j+BridgeLength+2) <> #BC_WATERBRIDGE_BOTTOM : hit = false : endif
				if BridgeConnectors(i+3,j+BridgeLength+2) <> #BC_WATERBRIDGE_BOTTOM : hit = false : endif				

				;Double check entire bridge length
				for x = 0 to 3
					for y = 0 to 4 + BridgeLength
						if MapData(i+x,j+y)
							hit = false
						endif
					next
				next				
				
				if hit
				
					NPrint "Drawing vertical water bridge"
										
					;Top segment
					for x = 0 to 3
						for y = 0 to 1
							MapData(i+x,j+y)=1
							Blit 367 + x + y*20,(i+x) * 16,(j+y) * 16							
						next
					next
					
					;Middle segment
					BridgeOffset=0
					for y = 2 to 2 + BridgeLength - 1
						for x = 0 to 3
							MapData(i+x,j+y)=1
							Blit 367 + x + (BridgeOffset+2)*20,(i+x) * 16,(j+y) * 16							
						next
						BridgeOffset = 1 - BridgeOffset
					next
					
					;Bottom segment
					BridgeOffset=0
					for y = 2 + BridgeLength to 3 + BridgeLength
						for x = 0 to 3
							MapData(i+x,j+y)=1
							Blit 367 + x + (BridgeOffset+4)*20,(i+x) * 16,(j+y) * 16							
						next
						BridgeOffset + 1
					next
					
					Pop For
					Goto CancelBridge
					
				endif				
				
			next
			
			.CancelBridge
			
		next
	next
	
	;Horizontal water bridges
	for i = 0 to #TILES - 3 - 5
		for j = 0 to #TILES - 4
												
			hit=true
			
			;Check that the connectors are in place for the top
			if BridgeConnectors(i,j) <> #BC_WATERBRIDGE_LEFT : hit = false : endif
			if BridgeConnectors(i,j+3) <> #BC_WATERBRIDGE_LEFT : hit = false : endif
			
			if hit = false
				Goto CancelBridgeH
			endif
			
			;Try to connect to the right
			for BridgeLength = 1 to 5
			
				hit = true
				if BridgeConnectors(i+1+BridgeLength,j) <> #BC_WATERBRIDGE_RIGHT : hit = false : endif
				if BridgeConnectors(i+1+BridgeLength,j+3) <> #BC_WATERBRIDGE_RIGHT : hit = false : endif				

				;Double check entire bridge length
				for x = 0 to 1 + BridgeLength
					for y = 0 to 3
						if MapData(i+x,j+y)
							hit = false
						endif
					next
				next				
				
				if hit
				
					NPrint "Drawing horziontal water bridge"
										
					;Left segment
					for x = 0 to 0
						for y = 0 to 3
							MapData(i+x,j+y)=1
							Blit 360 + x + y*20,(i+x) * 16,(j+y) * 16							
						next
					next
					
					;Middle segment
					BridgeOffset=0
					for x = 1 to BridgeLength				
						for y = 0 to 3
							MapData(i+x,j+y)=1
							Blit 360 + 1 + BridgeOffset + y*20,(i+x) * 16,(j+y) * 16							
						next
						BridgeOffset = 1 - BridgeOffset
					next
					
					;Right segment
					for x = BridgeLength+1 to BridgeLength+1
						for y = 0 to 3
							MapData(i+x,j+y)=1
							Blit 363 + y * 20,(i+x) * 16,(j+y) * 16							
						next
						BridgeOffset + 1
					next
					
					Pop For
					Goto CancelBridgeH
					
				endif				
				
			next
			
			.CancelBridgeH
			
		next
	next	
	
	;Regenerate noise for obstacles
	NPrint("Generating Obstacles")
	GenerateNoise{}	
	
	;Finally, do obstacles
	o = Rnd(OBSTACLES)	
	fails=0
	while fails < 100
				
		if Rnd > .5
			o = Rnd(OBSTACLES)
		endif
		
		besthit.q = -1
		
		for innerattempts = 0 to (ObstacleList(o)\Height * ObstacleList(o)\Width) + 8
		
			i = Int(Rnd(#TILES-ObstacleList(o)\Height)) + 1
			j = Int(Rnd(#TILES-ObstacleList(o)\Width)) + 1
			
			hit = true
			
			for x = -1 to ObstacleList(o)\Height
				for y = -1 to ObstacleList(o)\Width		
				
					if(MapData(i+x,j+y))
						hit = false
					endif
				
					Select ObstacleList(o)\T
						Case #WATER					
							if CountNeighbour{i+x,j+y} <> #WATER and CountNeighbour{i+x,j+y} <> #INNERWATER
								hit = false
							endif
						Case #GRASS
							if CountNeighbour{i+x,j+y} <> #GRASS and CountNeighbour{i+x,j+y} < #STONECENTER
								hit = false
							endif		
						Case #DIRT
							if CountNeighbour{i+x,j+y} <> #DIRT and CountNeighbour{i+x,j+y} <> #INNERDIRT
								hit = false
							endif								
					End Select
							
				next
			next
			
			if hit and (besthit = -1 or besthit < CombinedNoise(i,j))
				besthit = CombinedNoise(i,j)
				bestx=i
				besty=j
			endif
			
		next
		
		if besthit > -1			
			for x = 0 to ObstacleList(o)\Width - 1
				for y = 0 to ObstacleList(o)\Height - 1		

					;Special case for dirt trees
if y = ObstacleList(o)\Height - 1 and ObstacleList(o)\Special > 0 and x > 0 and x < 3
					Blit ObstacleList(o)\Offset + x + (y*20) + ObstacleList(o)\Special*20,(bestx+x)*16,(besty+y)*16
else
					Blit ObstacleList(o)\Offset + x + (y*20),(bestx+x)*16,(besty+y)*16
endif
					MapData(bestx+x,besty+y)=1
				next
			next
		else
			fails+1
		endif
		
	wend
		
	i=0
	while Exists("output_" + UStr$(i) + ".iff")
		i+1
	wend
	SaveBitMap #MAPBITMAP,"output_" + UStr$(i) + ".iff",0

	loop+1
	
	
Wend

End


