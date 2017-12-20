import java.util.*;
 
public class Kumoi extends Scale
{                            
   private int [] intervals = {0,1,5,7,8,12};
								 	  //1,2,6,8,9,13
								 
   public Kumoi()
   {
      super();
      name = "Kumoi";
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
    
   public Kumoi(int k)
   {
      super(k);
      name = "Kumoi";
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
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      //ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      //IV chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
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