import java.util.*;
public class WholeTone extends Scale
{
   private int [] intervals = {0,2,4,6,8,10,12};
   			        	        //1,3,5,7,9,11,13   
   public WholeTone()
   {
      super();
      name = "Whole Tone";
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
   	
   public WholeTone(int k)
   {
      super(k);
      name = "Whole Tone";
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
      for(int i = 0; i<6; i++)
      {   
      //chords A
         chordSetsA[i].add(new Chord(k+intervals[i], "aug"));
      //chords B
         chordSetsB[i].add(new Chord(k+intervals[i], "7b5"));
         chordSetsB[i].add(new Chord(k+intervals[i], "7#5"));
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


