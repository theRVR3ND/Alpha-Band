import java.util.*;
 
public class Japanese extends Scale
{                            
   private int [] intervals = {0,1,5,7,10,12};
								 	  //1,2,6,8,11,13
								 
   public Japanese()
   {
      super();
      name = "Japanese";
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
    
   public Japanese(int k)
   {
      super(k);
      name = "Japanese";
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
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "oct"));
      //ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      //iii chords A
      i = 2;
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
      //IV chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
   	//IV chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
   
   	//V chords A
      i = 4;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
   	//V chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
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