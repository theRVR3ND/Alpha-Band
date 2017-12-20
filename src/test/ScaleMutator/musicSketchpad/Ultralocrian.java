import java.util.*;
public class Ultralocrian extends Scale
{
   private int [] intervals = {0,1,3,4,6,8,9,12};
							 	 //1,2,4,5,7,9,10,13
   
   public Ultralocrian()
   {
      super();
      name = "Ultralocrian";
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
   	
   public Ultralocrian(int k)
   {
      super(k);
      name = "Ultralocrian";
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
      int i = 1;   
      //ii chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //ii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "sus2sus4"));
      i = 2;   
      //iii chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      //iii chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      i = 3;
   	//iv chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
   	//iv chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7#5"));
      i = 4;
   	//V chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus2"));
    	//V chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "madd2"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6-9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m7-6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "madd9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus2"));
      i = 5;
   	//VI chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      chordSetsA[i].add(new Chord(k+intervals[i], "sus4"));
      //VI chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7add4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "11b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7sus4"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7b9"));
      chordSetsB[i].add(new Chord(k+intervals[i], "7#5b9"));
      i = 6;
   	//VII chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "maj"));
      chordSetsA[i].add(new Chord(k+intervals[i], "maj7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "m"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "5"));
      //VII chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "add6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj6-7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "maj7b5"));
      chordSetsB[i].add(new Chord(k+intervals[i], "m6"));
      chordSetsB[i].add(new Chord(k+intervals[i], "mmaj7"));
      i = 0;
   	//i chords A
      chordSetsA[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsA[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      //i chords B
      chordSetsB[i].add(new Chord(k+intervals[i], "dim"));
      chordSetsB[i].add(new Chord(k+intervals[i], "dim7"));
      chordSetsB[i].add(new Chord(k+intervals[i], "aug"));
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


