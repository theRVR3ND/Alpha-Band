import java.util.*;
 
public class Pelog extends Scale
{                            
   private int [] intervals = {0,1,3,7,10,12};
								 	  //1,2,4,8,11,13
								 
   public Pelog()
   {
      super();
      name = "Pelog";
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
    
   public Pelog(int k)
   {
      super(k);
      name = "Pelog";
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
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      //I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
   	//ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7-6"));
      //IV chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
   	//IV chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   	//V chords A
      i = 4;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
   	//V chords B
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