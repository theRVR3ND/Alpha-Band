import java.util.*;
 
public class Chromatic extends Scale
{                          
   private int [] intervals = {0,1,2,3,4,5,6,7,8,9,10,11,12};
								 	  							 
   public Chromatic()
   {
      super();
      name = "Chromatic";
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
    
   public Chromatic(int k)
   {
      super(k);
      name = "Chromatic";
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
      for(int i = 0; i < 12; i++)
      {   
      //chords A
         chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
         chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
         chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
         chordSetsA[i].add(new Chord(k+intervals[i], "m"));
         chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
         chordSetsA[i].add(new Chord(k+intervals[i], "7"));
         chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
         chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
         chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
         chordSetsA[i].add(new Chord(k+intervals[i], "5"));
         chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
         chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //chords B
         chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
         chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "add2add4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
         chordSetsB[i].add(new Chord(k+intervals[i], "6-9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj6-7"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj9#11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj13"));
         chordSetsB[i].add(new Chord(k+intervals[i], "maj13#11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
         chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "madd2add4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m6-9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
         chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m7add4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
         chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "mmaj9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "m13"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7add4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7-6"));
         chordSetsB[i].add(new Chord(k+intervals[i], "9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "11b9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "13"));
         chordSetsB[i].add(new Chord(k+intervals[i], "13#11"));
         chordSetsB[i].add(new Chord(k+intervals[i], "13b9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "13sus4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "9sus4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7b9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7#9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7b5b9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7b5#9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7#5b9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7#5#9"));
         chordSetsB[i].add(new Chord(k+intervals[i], "9b5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "9#5"));
      }
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