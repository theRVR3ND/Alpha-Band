Rev. Dr. Douglas R Oberle, Washington DC, Aug 2015
This is a sketchpad for composing short melodies and or chord progressions.

//TO DO: a long chord and a short note in the same column clips the chord in playback (but not MIDI)
//       calculate processor speed to adjust timer DELAY so real-time playback matches speed when written to MIDI file
//       harmonize, or countermelody option that takes last composed melody line (V) and creates a harmony or countermelody for it  

Controls:

KEY OF THE SONG:
To change the key of the song, hit the letter that corresponds with that key, with SHIFT to sharp.
Typing 'D' will put the key in D, where typing 'SHIFT-D' will put the key in D#.  Default is C.

SCALE OF THE SONG:
To change the scale of the song, scroll through options with the < and > keys.  Default is Major.
Hit 'X' to toggle whether only popular scales are available  (default), or all scales are available

SPEED OF THE SONG:
To change the note length to speed up or slow down the song, use the UP and DOWN arrows.  Default is 96.
A smaller value makes for a faster song, where larger values make for a slower song.
This effects both real-time playback (SPACE) and what is written to a MIDI file.  
To change the playback delay, type SHIFT-UP or SHIFT-DOWN.  Default is 20.
This only affects real-time playback (SPACE).  It will not affect what is written to a MIDI file.
This is to be used to tweak the playback speed in the event that it is different with what writes to a MIDI file.

INSTRUMENT:
To change the MIDI instrument, it the LEFT and RIGHT arrow keys.  Default is PIANO.

FREEPLAY NOTES:
To freeplay notes, hit the number keys (1-9 and 0).
To silence notes being played, hit the 'S' key.  This will be important if you chose a MIDI instrument that does not decay.

PLAY THE SONG:
To start or stop the song being played, hit the SPACE bar.
To play the selected scale, hit the 'P' key.

SIZE OF THE CHART:
To change the number of columns to compose in, type the '[' and ']' keys.
To toggle the chart resolution between 16 rows and 32 rows, hit the 'T' key.

UNDO AND CLEAR THE CHART:
To undo the last change made to the chart, hit the BACKSPACE key.
To reset the chart and clear it of notes, hit the 'R' key.
To only remove chords, hit 'SHIFT-R'.

RANDOM SONG:
To place a random score of notes, hit the 'V' key.
To place a random score of chords, hit the 'SHIFT-V' key.
The way it will compose will be different if you place chords before notes, or notes before chords.

PLACE NOTE OR CHORD:
To draw a note on the current cell in the chart, press the LEFT mouse button.
To draw a chord on the current cell in the chart, press the RIGHT mouse button.
To scroll through the chords available on the current cell, hit the 'N' or 'M' key.
Hit 'X' to toggle whether only popular chords are available  (default), or all chords are available
If there is a collection of notes in a column for which there is no common chord that it matches, the column will be marked red to show dissonance.

CHANGE THE CURRENT NOTE OR CHORD:
To make the current note flat or sharp, hit the '-' or '+' keys.
To make the current note or chord move an octave up, hit the 'O' key.
To make the current note or chord move an octave down, hit the 'SHIFT-O' key.
To increase or decrease the length of the current note or chord, hit the PAGE_UP or PAGE_DOWN keys.
To scroll through 3 inversions of the current chord, hit the 'I' key.

LOAD AND WRITE TO FILES:
To load a song saved as a text file in the /songs folder, hit the 'L' key and type in the file name.
To write the song out to a text and MIDI file in the /songs folder, hit the 'W' key and type in the file name you want.
NOTE:  You can not write out to a MIDI file if one of the same name is already opened in a media player.

CHART TRANSFORMATIONS
F1      : flip the chart horizontally
SHIFT-F1: flip the chart vertically
F2      : any note that is dissonant with a chord in the same column will be shifted to the closest note that fits the chord
SHIFT-F2: any chord that is dissonant with notes in the same column will be changed to a chord that fits (if there is one)
H       : toggle hard fit notes - as true, if a note shares a column with a chord, F2 will shift it to a chord note if it is not one.
          as false, it will not shift the note if it can find a common chord that is comprised of all of the notes combined.
F3      : move the entire scale up one octave
SHIFT-F3: move the entire scale down one octave
F4      : shift all chart notes up one octave, but keep the scale the same
SHIFT-F4: shift all chart notes down one octave, but keep the scale the same
F5      : shift all chart notes up one step in the scale
SHIFT-F5: shift all chart notes down one step in the scale