import java.util.*;
 
public class DominantPentatonic extends Scale
{                           
   private int [] intervals = {0,2,4,7,10,12};
							        //1,3,5,8,11,13

   public DominantPentatonic()
   {
      super();
      name = "Dominant Pentatonic";
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
    
   public DominantPentatonic(int k)
   {
      super(k);
      name = "Dominant Pentatonic";
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
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
   	//I chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
     	//ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "oct"));
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      //IV chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//IV chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
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