//Rev Dr. Douglas R Oberle, Washington DC, 2015
import java.util.ArrayList;
   
public abstract class Scale
{
   protected int [] notes;
   protected ArrayList<Chord> [] chordSetsA;	//array of Sets of common chords I - vii
   protected ArrayList<Chord> [] chordSetsB;	//array of Sets of uncommon chords I - vii
   protected ArrayList<Chord> [] allChords;  //array of all chords from setsA and B
   protected int key;
   protected String name;
   public static final int OCTAVE = 12;
   /*
      protected final int C0=0; 		//24;
      protected final int Db0=1;		//25;
      protected final int D0=2;		//26;
      protected final int Eb0=3;		//27;
      protected final int E0=4;		//28;
      protected final int F0=5;		//29;
      protected final int Gb0=6;		//30;
      protected final int G0=7;		//31;
      protected final int Ab0=8;		//32;
      protected final int A0=9;		//33;
      protected final int Bb0=10;	//34;
      protected final int B0=11;		//35;
      
      protected final int C1=C0+OCTAVE;	//36;
      protected final int Db1=Db0+OCTAVE;	//37;
      protected final int D1=D0+OCTAVE;	//38;
      protected final int Eb1=Eb0+OCTAVE;	//39;
      protected final int E1=E0+OCTAVE;	//40;
      protected final int F1=F0+OCTAVE;	//41;
      protected final int Gb1=Gb0+OCTAVE;	//42;
      protected final int G1=G0+OCTAVE;	//43;
      protected final int Ab1=Ab0+OCTAVE;	//44;
      protected final int A1=A0+OCTAVE;	//45
      protected final int Bb1=Bb0+OCTAVE;	//46
      protected final int B1=B0+OCTAVE;	//47
   
      protected final int C2=C1+OCTAVE;	//36;
      protected final int Db2=Db1+OCTAVE;	//37;
      protected final int D2=D1+OCTAVE;	//38;
      protected final int Eb2=Eb1+OCTAVE;	//39;
      protected final int E2=E1+OCTAVE;	//40;
      protected final int F2=F1+OCTAVE;	//41;
      protected final int Gb2=Gb1+OCTAVE;	//42;
      protected final int G2=G1+OCTAVE;	//43;
      protected final int Ab2=Ab1+OCTAVE;	//44;
      protected final int A2=A1+OCTAVE;	//45
      protected final int Bb2=Bb1+OCTAVE;	//46
      protected final int B2=B1+OCTAVE;	//47
      
      protected final int C3=C2+OCTAVE;	//48
      protected final int Db3=Db2+OCTAVE;
      protected final int D3=D2+OCTAVE;
      protected final int Eb3=Eb2+OCTAVE;
      protected final int E3=E2+OCTAVE;
      protected final int F3=F2+OCTAVE;
      protected final int Gb3=Gb2+OCTAVE;
      protected final int G3=G2+OCTAVE;
      protected final int Ab3=Ab2+OCTAVE;
      protected final int A3=A2+OCTAVE;
      protected final int Bb3=Bb2+OCTAVE;
      protected final int B3=B2+OCTAVE;
      
      protected final int C4=C3+OCTAVE;	//60
      protected final int Db4=Db3+OCTAVE;
      protected final int D4=D3+OCTAVE;
      protected final int Eb4=Eb3+OCTAVE;
      protected final int E4=E3+OCTAVE;
      protected final int F4=F3+OCTAVE;
      protected final int Gb4=Gb3+OCTAVE;
      protected final int G4=G3+OCTAVE;
      protected final int Ab4=Ab3+OCTAVE;
      protected final int A4=A3+OCTAVE;
      protected final int Bb4=Bb3+OCTAVE;
      protected final int B4=B3+OCTAVE;
      
      protected final int C5=C4+OCTAVE;	//72
      protected final int Db5=Db4+OCTAVE;
      protected final int D5=D4+OCTAVE;
      protected final int Eb5=Eb4+OCTAVE;
      protected final int E5=E4+OCTAVE;
      protected final int F5=F4+OCTAVE;
      protected final int Gb5=Gb4+OCTAVE;
      protected final int G5=G4+OCTAVE;
      protected final int Ab5=Ab4+OCTAVE;
      protected final int A5=A4+OCTAVE;
      protected final int Bb5=Bb4+OCTAVE;
      protected final int B5=B4+OCTAVE;
   */
   public Scale()
   {
      key = 60;		//60
   }
      
   public Scale(int k)
   {
      key = k;
   }
      
   public void setKey(int k)
   {
      key = k;
   }
    
   public int getKey()
   {
      return key;
   }
   
   public abstract int getNumNotes();  //returns the number of notes in the raw scale (# intervals)
   
   
   public void buildChordSets()
   {
      allChords = new ArrayList[Math.max(chordSetsA.length, chordSetsB.length)];
      int index = 0;
      for(int i=0; i<allChords.length; i++)
      {
         ArrayList<Chord> temp = new ArrayList();
         for(int j=0; j<chordSetsA[i].size(); j++)
            temp.add(chordSetsA[i].get(j));
         for(int j=0; j<chordSetsB[i].size(); j++)
            temp.add(chordSetsB[i].get(j));
      
         allChords[index++] = temp;
      }
   }
   
   public ArrayList<Chord>[] getChordSets()
   {
      buildChordSets();
      return allChords;
   }

   	
   public ArrayList<Chord>[] getChordSetsA()
   {
      return chordSetsA;
   }
   	
   public ArrayList<Chord>[] getChordSetsB()
   {
      return chordSetsB;
   }
   	
   public abstract int [] getNotes();
                   
   public int getRoot()
   {
      return notes[0];
   }
      
   public String getName()
   {
      return name;
   }
       
   public void octaveUp()
   {
      //if(notes[notes.length-1] + OCTAVE <= 108)
         for(int i = 0; i<notes.length; i++)
            notes[i]+=OCTAVE; 
   }    
     
   public void octaveDown()
   {
      //if(notes[0] - OCTAVE >= 22)
         for(int i = 0; i<notes.length; i++)
            notes[i]-=OCTAVE; 
   }      

       
   public static int [] addToScales(int[]scales, int value)
   {
      for(int i = 0; i<scales.length; i++)
         scales[i]+=value-12;   
      return scales;
   }
   		
   public static int [] addOctave(int [] scale)	   //build scale to have 16 notes
   {		
      int [] tempScale = new int[scale.length-1];  //the last note of scale is an octave of the root, so drop it
      for(int i = 0; i<tempScale.length; i++)
         tempScale[i] = scale[i];
                  						   
      int [] newScale = new int[32];
      int octave = -1;
      for(int i = 0; i<newScale.length; i++)
      {
         if(i%tempScale.length == 0)
            octave++;
         newScale[i] = tempScale[i%tempScale.length] + (octave * OCTAVE);
      }
      if(newScale[newScale.length-1] > 108)        //drop an octave if highest note is above max note on keyboard
         for(int i = 0; i<newScale.length; i++)
            newScale[i]-=OCTAVE; 
      return newScale;
   }
          
   public static void showScale(int[]scale)
   {
      for(int i = 0; i<scale.length; i++)
         System.out.print(scale[i]+",");
   }
   
}