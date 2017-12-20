import java.util.*;

public class Dorianb2 extends Scale
{
   private int [] intervals = {0,1,3,5,7,9,10,12};
   							     //1,2,4,6,8,10,11,13
   
   public Dorianb2()
   {
      super();
      name = "Dorianb2";
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
   	
   public Dorianb2(int k)
   {
      super(k);
      name = "Dorianb2";
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
      int i = 6;   
      //vii chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
   	//vii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
      i = 0;   
      //i chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //i chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      i = 1;
   	//ii chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
      i = 2;
   	//III chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      //III chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "13"));
      chordSetsB[i].add(new Chord(k+intervals[i], "13#11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9b5"));
      i = 3;
   	//IV chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "add9"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //IV chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "add2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "11"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "9#5"));
      i = 4;
   	//v chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      //v chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      i = 5;
   	//vi chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      //vi chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b5#9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5#9"));
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

