WBStartup
DEFTYPE .w

NEWTYPE .Typ_Time
	Reserved.b ;Reserved (always zero)
	Minute.b ;Minutes (0-72ish) 
	Second.b ;Seconds (0-59)
	Frame.b ;Frame   (0-74)
End NEWTYPE

NEWTYPE .Typ_TOCSummary
	FirstTrack.b ;First track on disk (always 1)
	LastTrack.b ;Last track on disk
	LeadOut.Typ_Time ;Beginning of lead-out track
End NEWTYPE

NEWTYPE .Typ_TOCEntry
	CtlAdr.b ;Q-Code info  
	Track.b ;Track number
	Position.Typ_Time ;Start position of this track
End NEWTYPE

NEWTYPE .Typ_TOCData
	Summary.Typ_TOCSummary
	Tracks.Typ_TOCEntry[100]
End NEWTYPE

*TocPointer.Typ_TOCData

if InitCD32
	CDType = ExamineCD32
	*TocPointer = TocCD32
	NPrint "Number of tracks: " + Str$(*TocPointer\Summary\LastTrack)
	
	if *TocPointer\Summary\LastTrack > 0
		for i = *TocPointer\Summary\FirstTrack to *TocPointer\Summary\LastTrack
		
			;The starting position of this track
			Minutes$ = Str$(*TocPointer\Tracks[i-1]\Position\Minute)
			Format "00"
			Seconds$ = Str$(*TocPointer\Tracks[i-1]\Position\Second)		
			Format ""
			
			;Calculate the time of this track
			StartTime = *TocPointer\Tracks[i-1]\Position\Minute*60 + *TocPointer\Tracks[i-1]\Position\Minute
			if i = *TocPointer\Summary\LastTrack
				EndTime = *TocPointer\Summary\LeadOut\Minute*60 + *TocPointer\Summary\LeadOut\Second				
			else
				EndTime = *TocPointer\Tracks[i]\Position\Minute*60 + *TocPointer\Tracks[i]\Position\Minute
			endif
			
			TotalTime = EndTime - StartTime
			Minutes2$ = Str$(TotalTime / 60)
			Format "00"			
			Seconds2$ = Str$(TotalTime mod 60)
			Format ""
			NPrint "Track " + Str$(i) + " Offset: " + Minutes$ + ":" + Seconds$ + " Time: " + Minutes2$ + ":" + Seconds2$
			
		next
	endif
else
	NPrint "Cannot initialise CD drive"
endif

End
