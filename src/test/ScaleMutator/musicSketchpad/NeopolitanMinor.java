import java.util.*;

public class NeopolitanMinor extends Scale
{
   private int [] intervals = {0,1,3,5,7,8,11,12};
								     //1,2,4,6,8,9,12,13

   public NeopolitanMinor()
   {
      super();
      name = "Neopolitan Minor";
      notes = addToScales(addOctave(intervals),key);
      chordSetsA=new ArrayList[intervals.length-1];
      chordSetsB=new ArrayList[intervals.length-1];
      for(int i=0; i<chordSetsA.length; i++)
      {
         chordSetsA[i] = new ArrayList<Chord>();
         chordSetsB[i] = new ArrayList<Chord>();
      }
      assignChordSets(key);
   }
   	
   public NeopolitanMinor(int k)
   {
      super(k);   
      name = "Neopolitan Minor";
      notes = addToScales(addOctave(intervals),k);
      chordSetsA=new ArrayList[intervals.length-1];
      chordSetsB=new ArrayList[intervals.length-1];
      for(int i=0; i<chordSetsA.length; i++)
      {
         chordSetsA[i] = new ArrayList<Chord>();
         chordSetsB[i] = new ArrayList<Chord>();
      }
      assignChordSets(k);
   }
   
   public int getNumNotes()
   {
      return intervals.length;
   }

   private void assignChordSets(int k)
   {  
   	//I chords A
      int i = 0;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
   
   	//II chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
   
   	//II chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj9#11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9b5"));
   
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9#5"));
      //iv chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
   	//iv chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
   
      i = 4;   
      //V chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//V chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5b9"));
   
   	//vi chords A
      i = 5;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   
   	      	//vi chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj6-7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
      
               //vii chords A
      i = 6;
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
               //vii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   }

           
   public int [] getNotes()
   {
      return notes;
   }
      
   public int [] getIntervals()
   {
      return intervals;
   }

}