//Rev Dr. Douglas R Oberle, Washington DC, 2015
public class Note
{
   private int note;                   //pitch to be played
   private int track;                  //which track it is played on
   private Chord chord;                //used for placing chords
   protected int inversionType;        //inversion type for the chord 0, 1 or 2
   private int chordIndex;             //index of where the chord is found in the scale array allChords
   private int mod;                    //modification of note (sharp/flat)
   private double durration;           //how many frames is the note to play?
   private int row, col;               //where is it placed in the chart
   public static final int OCTAVE = 12;
   
   public String allInfo()
   {
      return  note + " " + track + " "+ chord + " " + inversionType + " " + chordIndex + " " + mod + " " + durration + " " + row + " " + col;
   }
   
   public Note(int n, int t, double d, int r, int c)
   {
      note = n;
      track = t;
      chord = null;
      inversionType = 0;
      chordIndex = 0;
      mod = 0;
      durration = d;
      row = r;
      col = c;
   }
     
   public Note(Chord c, int t, double d, int r, int co)
   {
      this(c.getRoot(),t, d, r, co);
      chord = c;
      chordIndex = 0;
   }

   public Note(Chord c, int t, int ci, double d, int r, int co)
   {
      this(c.getRoot(),t, d, r, co);
      chord = c;
      chordIndex = ci;
   }

//returns a copy of the current note
   public Note copy()
   {
      Note ans =  new Note(this.note, this.track, this.durration, this.row, this.col);
      if(this.chord != null)
      {
         Chord newChord = new Chord(this.chord.getRoot(), this.chord.getRawName(), this.chord.getOrigNotes());
         ans.inversionType = this.inversionType;
         ans.chord = newChord;
         ans.chordIndex = this.chordIndex;
      }
      ans.mod = this.mod;
      return ans;
   }
   
   public int getNote()
   {
      return note + mod;
   }

   public double getDurration()
   {
      return durration;
   }

   public int getTrack()
   {
      return track;
   }

   public Chord getChord()
   {
      if(chord == null)
         return null;
      int [] chNotes = new int[chord.getNotes().length];
      for(int i=0; i<chNotes.length; i++)
         chNotes[i] = chord.getNotes()[i] + mod;
      Chord ans = new Chord(chord.getRoot(), chord.getRawName(), chNotes);
      return ans;
   }

   public int getChordIndex()
   {
      return chordIndex;
   }

   public int getMod()
   {
      return mod;
   }

   public int getRow()
   {
      return row;
   }

   public int getCol()
   {
      return col;
   }
   
   public void setRow(int r)
   {
      row = r;
   }

   
   public void setCol(int c)
   {
      col = c;
   }


   public void setNote(int n)
   {
      if(chord==null)
         note = n;
   }
   
   public void setChord(Chord c)
   {
      chord = c;
   }


   public void setTrack(int t)
   {
      track = t;
   }
   
   public void setMod(int m)
   {
      mod = m;
   }
   
   public void setDurration(double d)
   {
      durration = d;
   }

   public void sharp()
   {
      if(chord==null && getNote() < 108)
         mod++;
   }
   
   public void flat()
   {
      if(chord==null && getNote() > 22)
         mod--;
   }

   public void octaveUp()
   {
      if(chord!=null)
      {
         int [] chNotes = getChord().getNotes();
         if(chNotes[chNotes.length-1]+OCTAVE <= 108)
            mod+=OCTAVE;
      }
      else 
      {
         if(getNote()+OCTAVE <= 108)
            mod+=OCTAVE;
      }
   }
   
   public void octaveDown()
   {
      if(chord!=null)
      {
         int [] chNotes = getChord().getNotes();
         if(chNotes[0]-OCTAVE >= 22)
            mod-=OCTAVE;
      }
      else
      {
         if(getNote()-OCTAVE >= 22)
            mod-=OCTAVE;
      }
   }

   public void inversion()
   {
      if(chord!=null)
      {
         if(inversionType == 2)
         {
            inversionType = 0;
            int [] chNotes = chord.getOrigNotes();
            if(chNotes[0]-OCTAVE >= 22)
            {
               for(int i=0; i<chNotes.length; i++)
                  chNotes[i] -= OCTAVE;
            }
            chord = new Chord(chord.getRoot(), chord.getRawName(), chNotes);
         }
         else
         {
            inversionType = (inversionType + 1) % 3;
            Chord newChord = Chord.inversion(chord, inversionType);
            if(newChord!=null)
            {
               chord = newChord;
            }
         }
      }
   
   }
   
   public int getInversionType()
   {
      return inversionType;
   }
   
   public void setInversionType(int it)
   {
      inversionType = it;
   }
   
   public void setChordIndex(int ci)
   {
      chordIndex = ci;
   }

}