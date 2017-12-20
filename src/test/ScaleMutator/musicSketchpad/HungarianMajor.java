import java.util.*;

public class HungarianMajor extends Scale
{
   private int [] intervals = {0,3,4,6,7, 9,10,12};
								     //1,4,5,7,8,10,11,13

   public HungarianMajor()
   {
      super();
      name = "Hungarian Major";
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
   	
   public HungarianMajor(int k)
   {
      super(k);   
      name = "Hungarian Major";
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
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
   	//I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5#9"));
   
   	//II chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
   
   	//II chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      
   	//iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
   	//iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   
      //iv chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
        	//iv chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5#9"));
   
      i = 4;   
      //V chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
   	//V chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   
   	//vi chords A
      i = 5;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
   
   	      	//vi chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
      
               //vii chords A
      i = 6;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
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