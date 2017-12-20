//Rev Dr. Douglas R Oberle, Washington DC, 2015
public class Chord
{
   private int root;
   private String name;		//maj, maj7, add9, sus2, sus4, min, min7, 7, dim, dim7, aug, 5, etc
   private int[]degrees;	//formula in degrees of chromatic scale (root is 1)
   private int[] notes;
   private int[] origNotes;//original chord notes to return back if we cycled through inversions
   public static final int OCTAVE = 12;

   public Chord()
   {
      name = "not assigned";
      root = -1;
      degrees = null;
      notes = null;
      origNotes = null;
   }
     
   public Chord(int r, String n)
   {
      root = r;
      name = n.trim().toLowerCase();
      chordBuilder();
      if(notes!=null)
         origNotes = notes.clone();
   }
   
   public Chord(int r, String n, int [] nts)
   {
      root = r;
      name = n.trim().toLowerCase();
      notes = nts;
      origNotes = notes.clone();
   }

  
   public int getRoot()
   {
      return root;
   }
  
   public String getName()
   {
      if (name.equals("not assigned"))
         return name;
      return intToKey(root) + name;
   }
   
   public String getRawName()
   {
      return name;
   }

   
   public String toString()
   {
      return ""+intToKey(root) + name;
   }

   public int[] getNotes()
   {
      return notes;
   }
   
   public int[] getOrigNotes()
   {
      return origNotes;
   }

   
   public void setNotes(int [] n)
   {
      notes = n;
   }
     
   public int[] getDegrees()
   {
      return degrees;
   }
  
   private void chordBuilder()
   {
       //MAJOR CHORDS
      if (name.equals("maj"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
      }
      else if (name.equals("add2") || name.equals("add9"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
      }
      else if (name.equals("add4"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 6;
         degrees[3] = 8;
      }
      else if (name.equals("add2add4"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 6;
         degrees[4] = 8;
      }
      else if (name.equals("add6"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
         degrees[3] = 10;
      }
      else if (name.equals("6-9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 10;
      }
      else if (name.equals("maj7"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
         degrees[3] = 12;
      }
      else if (name.equals("maj6-7"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
         degrees[3] = 10;
         degrees[4] = 12;
      }
      else if (name.equals("maj7b5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 7;
         degrees[3] = 12;
      }
      else if (name.equals("maj7#5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 9;
         degrees[3] = 12;
      }
      else if (name.equals("maj9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 12;
      }
      else if (name.equals("maj9#11"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 8;
         degrees[5] = 12;
      }
      else if (name.equals("maj11"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 6;
         degrees[4] = 8;
         degrees[5] = 12;
      }
      else if (name.equals("maj13"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 10;
         degrees[5] = 12;
      }
      else if (name.equals("maj13#11"))
      {
         degrees = new int[7];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 8;
         degrees[5] = 10;
         degrees[6] = 12;
      }
      //MINOR CHORDS
      else if (name.equals("m"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 8;
      }
      else if (name.equals("madd2") || name.equals("madd9"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 8;
      }
      else if (name.equals("madd4"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 6;
         degrees[3] = 8;
      }
      else if (name.equals("madd2add4"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 6;
         degrees[4] = 8;
      }
      else if (name.equals("m6"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 8;
         degrees[3] = 10;
      }
      else if (name.equals("m6-9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 8;
         degrees[4] = 10;
      }
      else if (name.equals("m7"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 8;
         degrees[3] = 11;
      }
      else if (name.equals("mmaj7"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 8;
         degrees[3] = 12;
      }
      else if (name.equals("m7add4"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 6;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("m7b5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 7;
         degrees[3] = 11;
      }
      else if (name.equals("m7#5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 9;
         degrees[3] = 11;
      }
      else if (name.equals("m7-6"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 8;
         degrees[3] = 10;
         degrees[4] = 11;
      }
      else if (name.equals("mmaj9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 8;
         degrees[4] = 12;
      }
      else if (name.equals("m9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("m11"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 6;
         degrees[4] = 8;
         degrees[5] = 11;
      }
      else if (name.equals("m13"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 4;
         degrees[3] = 8;
         degrees[4] = 10;
         degrees[5] = 11;
      }
      
      //DOMINANT CHORDS
      else if (name.equals("7"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
         degrees[3] = 11;
      }
      else if (name.equals("7add4"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 6;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("7-6"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 8;
         degrees[3] = 10;
         degrees[4] = 11;
      }
      else if (name.equals("9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("11"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 6;
         degrees[4] = 8;
         degrees[5] = 11;
      }
      else if (name.equals("11b9"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 2;
         degrees[2] = 5;
         degrees[3] = 6;
         degrees[4] = 8;
         degrees[5] = 11;
      }
      else if (name.equals("13"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 10;
         degrees[5] = 11;
      }
      else if (name.equals("13#11"))
      {
         degrees = new int[7];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 8;
         degrees[5] = 10;
         degrees[6] = 11;
      }
      else if (name.equals("13b9"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 2;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 10;
         degrees[5] = 11;
      }
      else if (name.equals("13sus4"))
      {
         degrees = new int[6];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 6;
         degrees[3] = 8;
         degrees[4] = 10;
         degrees[5] = 11;
      }
      else if (name.equals("7sus2"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 8;
         degrees[3] = 11;
      }
      else if (name.equals("7sus4"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 6;
         degrees[2] = 8;
         degrees[3] = 11;
      }
      else if (name.equals("9sus4"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 6;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      //OTHER CHORDS
      else if (name.equals("5"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 8;
         degrees[2] = 13;
      }
      else if (name.equals("aug"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 9;
      }
      else if (name.equals("dim"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 7;
      }
      else if (name.equals("dim7"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 7;
         degrees[3] = 10;
      }
      else if (name.equals("sus2"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 8;
      }
      else if (name.equals("sus4"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 6;
         degrees[2] = 8;
      }
      else if (name.equals("sus2sus4"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 6;
         degrees[3] = 8;
      }
      //ALTERED CHORDS
      else if (name.equals("7b5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 7;
         degrees[3] = 11;
      }
      else if (name.equals("7#5"))
      {
         degrees = new int[4];
         degrees[0] = 1;
         degrees[1] = 5;
         degrees[2] = 9;
         degrees[3] = 11;
      }
      else if (name.equals("7b9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 2;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("7#9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 5;
         degrees[3] = 8;
         degrees[4] = 11;
      }
      else if (name.equals("7b5b9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 2;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 11;
      }
      else if (name.equals("7b5#9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 11;
      }
      else if (name.equals("7#5b9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 2;
         degrees[2] = 5;
         degrees[3] = 9;
         degrees[4] = 11;
      }
      else if (name.equals("7#5#9"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 4;
         degrees[2] = 5;
         degrees[3] = 9;
         degrees[4] = 11;
      }
      else if (name.equals("9b5"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 7;
         degrees[4] = 11;
      }
      else if (name.equals("9#5"))
      {
         degrees = new int[5];
         degrees[0] = 1;
         degrees[1] = 3;
         degrees[2] = 5;
         degrees[3] = 9;
         degrees[4] = 11;
      }
      else if (name.equals("oct"))
      {
         degrees = new int[3];
         degrees[0] = 1;
         degrees[1] = 13;
         degrees[2] = 25;
      }
      else
      {
         System.out.println("chord " + name+" not found");
         return;
      }
      notes = new int[degrees.length];
      for(int i=0; i<notes.length; i++)
         notes[i] = root + (degrees[i]) - 1;	//subt 1 because a degree value of 1 is the root itself
   }
   
 //given a ourNote, return its normalized value where 0 is the first ourNote in the scale (C)
   public static int normalize(int ourNote)
   {
      while(ourNote>=OCTAVE)			//strip out any octaves
         ourNote-=OCTAVE;   	
      return ourNote;
   }  
   
   public int[] getNormalizedNotes()
   {
      int [] ans = new int[notes.length];
      for(int i=0; i < ans.length; i++)
         ans[i] = normalize(notes[i]);
      return ans;
   }
   
//given a MIDI note value, return its corresponding key (multiples of 12 are C)
//returns "?" if it is not found
   public static String intToKey(int num)
   {
      num=normalize(num);   	
      switch(num)
      {
         case 0: 
            return "C";
         case 1: 
            return "C#";
         case 2: 
            return "D";
         case 3: 
            return "D#";
         case 4: 
            return "E";
         case 5: 
            return "F";
         case 6: 
            return "F#";
         case 7: 
            return "G";
         case 8: 
            return "G#";
         case 9: 
            return "A";
         case 10: 
            return "A#";
         case 11: 
            return "B";
      }
      return "?";			//unknown note value sent
   }
   
   public static Chord inversion(Chord orig, int inversionType)
   {
      if(orig.getOrigNotes() == null)
         return null;
      if(inversionType == 0)      //restore original chord
      {
         Chord ans =  new Chord(orig.getRoot(), orig.getRawName(), orig.getOrigNotes());
         return ans;
      }
      else
      {
         int[]chord = new int[orig.getOrigNotes().length];
         for(int i=0; i<chord.length; i++)
            chord[i] = orig.getOrigNotes()[i];
         if(chord.length < 3)		//if our chord is too small to do an inversion,
         {             				//make it a 3 note chord with some octaves, son...
            int[]octaves = new int[3];
            octaves[0] = chord[0];
            if(chord.length == 1)
               octaves[1] = octaves[0] + OCTAVE;
            else
               octaves[1] = chord[1];
            octaves[2] = octaves[1] + OCTAVE;
            octaves = forceChordInRange(octaves);
            Chord ans =  new Chord(orig.getRoot(), orig.getRawName(), octaves);
            return ans;
         }
         int[] newChord = new int[chord.length];
         if(inversionType == 1)//1st inversion
         {
            for(int i=1; i<chord.length; i++)					//copy the 2nd->last notes in the new chord
               newChord[i-1] = chord[i];
            newChord[newChord.length-1] = chord[0]+OCTAVE;	//add the 1st note to the end + an octave
         }
         else							//2nd inversion
         {
            for(int i=2; i<chord.length; i++)					//copy the 3rd->last notes in the new chord
               newChord[i-2] = chord[i];
            newChord[newChord.length-2] = chord[0]+OCTAVE;	//add the 1st & 2nd notes to the end + octave
            newChord[newChord.length-1] = chord[1]+OCTAVE;
         }
         Chord ans =  new Chord(orig.getRoot(), orig.getRawName(), newChord);
         return ans;
      }
   }

   //will adjust a chord by adding or subtracting octaves until it is in an acceptable range
//a piano range is from 22(A0) to 108(C8)
   public static int[] forceChordInRange(int[] chord)
   {
      int[]newChord = new int[chord.length];
      for(int i=0; i<newChord.length; i++)
         newChord[i] = chord[i];
   
      boolean haveWeForcedUp = false;
      boolean haveWeForcedDown = false;
      boolean forceUp=false;
      boolean forceDown = false;
      do
      {
         forceUp=false;
         forceDown = false;
         for(int i=0; i<newChord.length; i++)
         {
            if(newChord[i] != 0)
            {
               if(newChord[i]<22)
                  forceUp=true;
               else
                  if(newChord[i] > 108)
                     forceDown = true;
            }
         }
         if(forceUp && forceDown)
            break;					//whoops!
         if(forceUp)
         {
            newChord = riffOctaveSimple(newChord, 1);
            haveWeForcedUp = true; 
         }
         else     
            if(forceDown)
            {
               newChord = riffOctaveSimple(newChord, -1);
               haveWeForcedDown = true;     
            }
         if(haveWeForcedUp && haveWeForcedDown)	//avoid ping-ponging back and forth - just adjust individual notes
            for(int i=0; i<newChord.length; i++)
               newChord[i] = forceNoteInRange(newChord[i]); 
      }
      while (forceUp || forceDown);
      return newChord;
   }
   
   public static int[] riffOctaveSimple(int[]riffNotes, int octave)
   {
      int[] riffShift = new int[riffNotes.length];
      for(int i=0; i < riffNotes.length; i++)
      {
         if (riffNotes[i] > 0)			//if our note is not a rest, add an octave of our note
            riffShift[i] = riffNotes[i] + (OCTAVE*octave);
         else
            riffShift[i] = 0;				//if our note is a rest, add a rest
      }
      return riffShift;
   }

//will adjust a note by adding or subtracting octaves until it is in an acceptable range
//a piano range is from 22(A0) to 108(C8)
   public static int forceNoteInRange(int ourNote)
   {
      if (ourNote == 0)
         return 0;
      if(ourNote > 108)					//if ourNote is too high in frequency, drop it octaves until it is in range
      {
         while(ourNote > 108)
            ourNote -= OCTAVE;
         return ourNote;
      }
      if(ourNote > 0 && ourNote < 22)	//figure out what ourNote is too low to hear	
      {
         while(ourNote > 0 && ourNote < 22)
            ourNote += OCTAVE;
         return ourNote;
      }
      return ourNote;
   }

}