import java.util.*;
 
public class Augmented extends Scale
{                          
   private int [] intervals = {0,3,4,7,8,11,12};
   			        	        //1,4,5,8,9,12,13   
   public Augmented()
   {
      super();
      name = "Augmented";
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
    
   public Augmented(int k)
   {
      super(k);
      name = "Augmented";
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
      int i = 0;   
      //I chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
   	//I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      //ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
      //IV chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//IV chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   	//V chords A
      i = 4;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
   	//V chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
        //VI chords A
      i = 5;
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//VI chords B
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