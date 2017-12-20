import java.util.*;

public class Aeolian extends Scale
{
   private int [] intervals= {0,2,3,5,7,8,10,12};
					   		    //1,3,4,6,8,9,11,13
     	
   public Aeolian()
   {
      super();
      name = "Aeolian";
      notes =  addToScales(addOctave(intervals),key);
      chordSetsA=new ArrayList[intervals.length-1];
      chordSetsB=new ArrayList[intervals.length-1];
      for(int i=0; i<chordSetsA.length; i++)
      {
         chordSetsA[i] = new ArrayList<Chord>();
         chordSetsB[i] = new ArrayList<Chord>();
      }
      assignChordSets(key);
   }
   	
   public Aeolian(int k)
   {
      super(k);
      name = "Aeolian";
      notes =  addToScales(addOctave(intervals),k);
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
      int i = 2;   
      //III chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//III chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj6-7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj13"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
   	//iv chords A
      i = 3;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//iv chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m13"));
      chordSetsB[i].add(new Chord(k+intervals[i], "13sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
      //v chords A
      i = 4;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //v chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
   	//VI chords A
      i = 5;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
   	//VI chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj6-7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj9#11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj13"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj13#11"));
   	//VII chords A
      i = 6;
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//VII chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "13"));
      chordSetsB[i].add(new Chord(k+intervals[i], "13sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
   	//i chords A
      i = 0;
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//i chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
   	//ii chords A
      i = 1;
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
   	//ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
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


