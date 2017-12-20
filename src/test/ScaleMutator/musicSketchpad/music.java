 /*TO DO:  	 	 
check velocity of bass notes -
find where things get too busy
occasional gaps in some songs (back to back rests?)
check makeMelodyNotes for differences between previousNotes, riffNotes[i] and scale[j] - make sure it is right
add new rolling chord type that traverses just down (or up), changing chords to the next closest note from the last chord to the next one
make it so that when the last chord ends, find the next closest note in the same octave in the next chord
*/
import javax.sound.midi.*;
import java.util.Vector;
import java.io.*;
import java.util.*;

//chanel 0 - melody
//chanel 1 - harmony
//chanel 2 - chords
//chanel 3 - bass
public class music
{  //**********USER INPUT VARIABLES************************************************************
   public static Scale specificScale = null;	//the scale or mode the user picks
   public static Scale altScale = null;		//the relative,parallel,similaror expanded scale of specificScale
   public static int tempo = 180;				//tempo of the song
   public static int melodyInst = 0;			//the melody instrument the user picks (piano is 0)
   public static int chordInst = 0;				//the chord instrument the user picks
   public static int harmonyInst = 0;			//the harmony instrument the user picks
   public static int bassInst = 0;				//the bass instrument the user picks
   public static double tryStructure = .15;	//% of time to play a scale run, triads, rolling chords, chord sequence with melody or resurfacing chord pattern with or w/o melody
//********if tryStructure is > 0, the variables below may be set:***************************
   public static double trySequence = .10;	//% of time a structure will play a sequence of chords and melody (and maybe countermelody) in either 2/4, 3/4 or 4/4
//if trySequence is > 0, the following variable may be set:
   public static int timeSig = -1;				//time signature for a sequence (2->2/4, 3->3/4, 4->4/4, 5->4/4 waltz or -1 for random)
   public static double tryChordTheme = .15;		//% of time a structure will play a chord progression that can resurface in the song (w or w/o a new melody or our resurfacing melody)
//********CHORD THEME RULES (if tryChordTheme is > 0)****************************************
   public static double limitChord = .75;		//% of time we should limit a chor to 3 notes		  
   public static int richness = 7;				//the number of chords to draw from (1-7)
   public static double chordThemeRules = 1;			//% of time we follow the chord sequence rules below
   public static double startChordThemeOnI = 1;		//% of time chord phrases start on I chord
   public static double endChordThemeOnI_IVorV = 0;	//% of time we end chord phrases on I, IV or V chord
   public static double majChordProgressionRules = 0;	//% of time to use the major chord progrssion flow chart for picking interrior chords
//********END CHORD THEME RULES**********************************************************
   public static double tryScaleRun = .15;		//% of time a structure will play a run through the scale
   public static double tryTriads =.15;			//% of time a structure will play a series of triads
   public static double tryRollingChords = .1;	//% of time a structure will play a set of rolling chords
//********END if tryStructure is > 0********************************************************
   public static double tryMelodyTheme = .2;		//revisit a melody
//********MELODY RULES (if tryMelodyTheme is > 0)************************************************
   public static int melodyBuildStrat = 0;		//strategy for how melodies are built.  0-choose random, 1-build from chord theme, 2-build using repeating substructures, 3-build loosly
   public static double melodyRules = 1;			//% of time we follow standard melody rules listed below
   public static double durationFormat = .5;		//% of time that when selecting the shortest duration note, it groups them in even numbers
   public static double startOnWiseNote = .90;	//% of time we start a melody on (by priority) I, iii or V, ii or IV 
   public static double disjunctMotion = .20;	//% time to use disjunct motion (non-step motion)
   public static double resolveDisjunct = 1;		//resolve disjunct motion with conjunct motion in the opposite direction
   public static double ascendMin6th = 1;			//if we are a min 6th below the root, ascend up to the root
   public static double ascendMin7th = 1;			//allow a leap of a 7th between the dominant and subdominant
   public static double allowTritone = .05;		//% of time that a 4th is augmented or 5th is diminished to the tritone  
//********END MELODYCHNL RULES******************************************************************
   public static boolean allowRelativeScale = true;	//allow a switch to a relative scale
   public static boolean allowParallelScale = false;	//allow a switch to a parallel scale (maj->min)
   public static boolean allowSimilarScale = false;	//allow a switch to a similar scale
   public static boolean allowExpandedScale = false;	//allow a switch to an expanded scale
   public static double counterMelody = .5;	//% of time a sequence plays a counter melody with the melody
   public static double tryPhrase = .15;		//play a repeating phrase 
   public static double tryImprov = .85;		//within improvise, play around with notes and chords 
   public static double busyNess = .75;		//% of note events to rest events (i.e, 90% notes to 10% rests)
   public static double harmonize = .5;		//% of time melody note is harmonized or octaved
   public static double noteRange = .80;		//% time the next melody note is within 5 notes of the last one
   public static double popularChords = .75;	//% of time we should use popular chords (maj, m, maj7, m7, 7, 5) from chordSetsA if possible		  
   public static double altChords = .25;		//% of time alternate chords are selected from chordSetsB (6, 6-7, add2, etc)
   public static double doChordInversions = .25;	//% of time chords are 1st or 2nd inversion
   public static double chordBusyNess = 1;	//% time a chord plays when there is a rest for the melody
   public static double freeTime = .5;			//% time to allow tempo changes when playing structures
   public static double flair = .25;			//% of the time that chords are rolled instead of every note in the chord hit at once
   public static int chordTime = 0;				//if chords should be played according to the tracking timer (1) or number of events played (2) or played on the beat (0)
   public static int chordInterval = 8000;	//the tracking intervals in which chords will be played if freeTime is 0
   public static int chordTimeSeed = 4;			//used if freeTime is 1 or 2 (should be a random 1-10)
   public static int chordTimeSeed2 = 15;		//used if freeTime is 1 or 2 (should be a random 11-20)
   public static int forceChordLength = -1;	//should we force a chord to a certain duration, -1 if not	
   public static double forceArp = 0.1;	//should we force a chord to be rolled rather than strike all notes at the same time
   public static int rollDelay = 100;			//if we are rolling a chord, this is the time between each note of the chord being struck
   public static Long seed = new Long(-999);	//the seed for Random Object.  If its < 0, it will autoseed and not be repeatable
//**********END USERINPUT VARIABLES************************************************************

   public static final int TEXT = 0x01;
   public static final int  MELODYCHNL = 0;	//melody chanel
   public static final int  HARMONYCHNL = 1;	//chanel 1 - harmony
   public static final int  CHORDSCHNL = 2;	//chanel 2 - chords
   public static final int  BASSCHNL = 3;	//chanel 3 - bass

   public static int wholeNote=96;//24*4;	//resolution of a whole note 
   public static int velocity=100;			//the velocity of the note (changed randomly in the loop)
   public static int [] scale;				//get the scale of notes from the scale chosen
   public static ArrayList[] chordSetsA;	//array (I-vii) of sets of popular chords (maj, m, 7, maj7, min7, etc)
   public static ArrayList[] chordSetsB;	//array (I-vii) of sets of alternate chords (6, 11, 13, etc)
   public static int note=0;					//a single note from the scale  			   
   public static Map<Integer, int[]> chordPlayed = new HashMap();	
//Map of tracking values(key) where chords have been played(value) - used to see if we can play another chord
   public static Map<Integer, int[]> allPlayed = new HashMap();	
//Map of all notes played at tracking (key) and int[] (value) of what the note is (at index 0) on which chanel (at index 1)
   public static Map<Integer, Integer> melodyNotesPlayed = new HashMap();
   public static Map<Integer, Integer> harmonyNotesPlayed = new HashMap();
   public static Map<Integer, Integer> bassNotesPlayed = new HashMap();
   public static final int OCTAVE = 12;
   public static Random rand;					//random number generator
  
//Post: sets specific key, scale, structure style, tempo, and instrument choices		O(1)
   private static void userInput() throws IOException
   {
      Map MIDIinstr = buildMap();
      String speedWord = "";	//used for build info output to let you know how fast or slow a song is
   
   //********BASIC OPTIONS*******************************************************
      int TONE = 60;										//this will be the root value for the key chosen
      specificScale = new Major(TONE);				//the scale the song uses to use notes and chords from
      tempo=180;											//tempo of song
   //*********END BASIC OPTIONS**************************************************  
    
   //********INSTRUMENT SELECTION************************************************
      melodyInst = 0;	//chanel 0 - melody
      harmonyInst = 0;	//chanel 1 - harmony
      chordInst = 0;		//chanel 2 - chords
      bassInst = 0;		//chanel 3 - bass
   //********END INSTRUMENT SELECTION********************************************
     
   //********STRUCTURE***********************************************************************
      tryStructure = 0.10;			//% of time to play a scale run, triads, rolling chords, chord sequence with melody or resurfacing chord pattern with or w/o melody
      freeTime = .35;				//% time to allow tempo changes when playing structures
   //if tryStructure is > 0, the variables below may be set:
      {//tryMelodyTheme, tryPhrase, trySequence, tryChordTheme, tryScaleRun, tryTriads and tryRollingChords MUST add up to 1.0 (100%)
         melodyBuildStrat = 0;		//strategy for how melodies are built.  0-choose random, 1-build from chord theme, 2-build using repeating substructures, 3-build loosly
         tryMelodyTheme = .2;		//revisit a melody
         {  //********MELODYCHNL RULES (if tryMelodyTheme is > 0)************************************************
            melodyRules = 0.8;	//% of time we follow standard melody rules listed below
            durationFormat = .6;	//% of time that when selecting the shortest duration note, it groups them in even numbers
            startOnWiseNote = .9;//% of time we start a melody on (by priority) I, iii or V, ii or IV            
            resolveDisjunct = .9;//resolve disjunct motion with conjunct motion in the opposite direction
            ascendMin6th = 0.1;	//if we are a min 6th below the root, ascend up to the root
            ascendMin7th = 0.1;	//allow a leap of a 7th between the dominant and subdominant
            allowTritone = 0;		//% of time that a 4th is augmented or 5th is diminished to the tritone
            disjunctMotion = .20;//% time to use disjunct motion (non-step motion)
         //********END MELODYCHNL RULES******************************************************************
         }
         tryPhrase = .15;		//play a repeating phrase 
         trySequence = .10;	//% of time a structure will play a sequence of chords and melody (and maybe countermelody) in either 2/4, 3/4 or 4/4
         {//if trySequence is > 0, the following variable may be set:
            timeSig = -1;		//time signature for a sequence (2->2/4, 3->3/4, 4->4/4, 5->4/4 waltz or -1 for random)
         }
         tryChordTheme = .2;	//% of time a structure will play a chord progression that can resurface in the song (w or w/o a new melody or our resurfacing melody)
         {  //********CHORD THEME RULES (if tryChordTheme is > 0)****************************************
            limitChord = .75;	//% of time we limit a chord to 3 notes		  
            richness = 7;		//the number of chords to draw from (1-7).  A low value is verse like, a high value is chorus like
         //if richness == 7, then the following rules may be applied:
            {
               chordThemeRules = 0.75;			//% of time we follow the chord progression rules below
               startChordThemeOnI = 0.8;		//% of time chord phrases start on I chord
               majChordProgressionRules=0.8;	//% of time to use the major chord progrssion flow chart for picking interrior chords
               endChordThemeOnI_IVorV = 0.8;	//% of time we end chord phrases on I, IV or V chord
            }
         //********END CHORD THEME RULES**********************************************************  
         }
         tryScaleRun = .10;		//% of time a structure will play a run through the scale
         tryTriads = .15;			//% of time a structure will play a series of triads
         tryRollingChords = .10;	//% of time a structure will play a set of rolling chords
      //******END STRUCTURE*********************************************************************
      }
   
   //********IMPROVISE************************************************************************
   //tryImprov is 1 - (tryStructure)
      tryImprov = 1-tryStructure;//.35;		//within improvise, play around with notes and chords           
      counterMelody = .5;	//% of time a sequence plays a counter melody with the melody
      busyNess = .75;		//% of note events to rest events (i.e, 90% notes to 10% rests)
      harmonize = .5;		//% of time melody note is harmonized or octaved
      noteRange = .80;		//% time the next melody note is within 5 notes of the last one
   //********END IMPROVISE*******************************************************************
     	
   //********CHORD RULES************************************************************************
      popularChords = 0.5;	//% of time we should use popular chords (maj, m, maj7, m7, 7, 5) from chordSetsA if possible
      altChords = 0.1;		//% of time alternate chords are selected from chordSetsB (6, 6-7, add2, etc)
      doChordInversions = .25;	//% of time chords are 1st or 2nd inversion
      chordBusyNess = 1;	//% time a chord plays when there is a rest for the melody
      flair = .25;			//% of the time that chords are rolled instead of every note in the chord hit at once
      chordTime = 0;			//if chords should be played according to the tracking timer (1) or number of events played (2) or played on the beat (0)
   //if chordTime is 0, the following variable may be set:
      {
         chordInterval = wholeNote*2;	//the tracking intervals in which chords will be played if chordTime is 0
      }
   //if chordTime is 1 or 2, the following variables may be set:
      {
         chordTimeSeed = 4;		//used if chordTime is 1 or 2 (should be a random 1-10)
         chordTimeSeed2 = 15;		//used if chordTime is 1 or 2 (should be a random 11-20)
      }
      forceChordLength = -1;		//should we force a chord to a certain duration, -1 if not	
      forceArp = 0.1;				//should we force a chord to be rolled rather than strike all notes at the same time
      rollDelay = wholeNote/32;	//if we are rolling a chord, this is the time between each note of the chord being struck (must be a mult of 2, 2,4,8,16,or 32
   //********END CHORD RULES********************************************************************
   
      Scanner input = new Scanner(System.in);
   
      System.out.println("What key: C,C#, D,D#, E, F,F#, G,G#, A,A#, B or (R)andom");
      String temp = input.next().toUpperCase();
      if(temp.charAt(0)=='R')
         TONE = (int)(rand.nextDouble()*12) + 60;		//so song can be in any key from A,B,C,D,E,F,G,A
      else
         TONE = keyToInt(temp);		//C is 36,48,60
   
      System.out.println(ScaleList());
      temp = input.next().toUpperCase();
      int scaleChoice = 15;
      if(!isNumber(temp))
         scaleChoice = (int)(rand.nextDouble()*68)+1;
      else
         scaleChoice = Integer.parseInt(temp);
      Map <Integer, Scale> allScales = buildScaleMap(TONE);    
      specificScale = allScales.get(scaleChoice);
      if(specificScale==null)
         specificScale = new Major(TONE);
      allowRelativeScale = false;
      allowParallelScale = false;
      allowSimilarScale = false; 
      allowExpandedScale = false; 
      if(allowRelativeScale)
         altScale = findRelativeScale(specificScale);
      else if(allowParallelScale)
         altScale = findParallelScale(specificScale);
      else if(allowSimilarScale)
         altScale = findSimilarScale(specificScale);
      else if(allowExpandedScale)
         altScale = findExpandedScale(specificScale);	
      if(allowRelativeScale && altScale==null)
         allowRelativeScale = false;	//allow a switch to a relative scale
      if(allowParallelScale && altScale==null)
         allowParallelScale = false;	//allow a switch to a parallel scale (maj->min)
      if(allowSimilarScale && altScale==null)
         allowSimilarScale = false;	//allow a switch to a similar scale
      if(allowExpandedScale && altScale==null)
         allowExpandedScale = false;	//allow a switch to an expanded scale
   /*
   System.out.println("What tempo (100-slow, 180-medium,  300-fast, R for random)");
   temp = input.next().toUpperCase();
   if(isNumber(temp))
      tempo = (int)(Double.parseDouble(temp));
   else
      tempo = (int)(rand.nextDouble()*201)+100;
   if(tempo <= 100)
      speedWord = "very slow ";
   else
      if(tempo < 140)
         speedWord = "slow ";
      else
         if(tempo >200)
            speedWord = "fast ";
         else
            if(tempo >=300)
               speedWord = "very fast ";
          	  
      	   
   System.out.println("Octave shift: (U)p, (D)own, (N)one or (R)andom");
   temp = input.next().toUpperCase();
   double oct = 1;
   if(temp.charAt(0)=='R')
      oct=rand.nextDouble();
   if(temp.charAt(0)=='U' || (oct>=0 && oct<.33))
   {
      TONE+=OCTAVE;
      System.out.println("Octave raised");   
   }
   else
      if(temp.charAt(0)=='D' || (oct>=.33 && oct<.66))
      {
         TONE-=OCTAVE;
         System.out.println("Octave lowered");   
      }
      else
         System.out.println("No octave shift");
      
        
   System.out.println("What % of events should be scale runs, triad structures, sequence of rolled chords or chord progression with melody (2-few, 5-moderate, 10-many, R-random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 0 and 20 scale-runs and triad structure
      tryStructure = ((int)(rand.nextDouble()*21))/100.0;
   else
      tryStructure = Double.parseDouble(temp)/100.0;
   if(tryStructure > 0)
   {  
      System.out.println("What % of structures should be scale runs (0-100, R-random)");
      temp = input.next().toUpperCase();
      if(!isNumber(temp))				
         tryScaleRun = ((int)(rand.nextDouble()*101))/100.0;
      else
         tryScaleRun = Double.parseDouble(temp)/100.0;
      
      double percentLeft = 100-tryScaleRun;
      if(percentLeft > 0)
      {
         System.out.println("What % of structures should be triads (0-" + percentLeft +", R-random)");
         temp = input.next().toUpperCase();
         if(!isNumber(temp) || Double.parseDouble(temp) < 0 || Double.parseDouble(temp) > percentLeft)			
            tryTriads = ((int)(rand.nextDouble()*(percentLeft+1)))/100.0;
         else
            tryTriads = Double.parseDouble(temp)/100.0;    
      	
         percentLeft = 100-tryScaleRun-tryTriads;
         if(percentLeft > 0)
         {
            System.out.println("What % of structures should be rolling chords (0-" + percentLeft +", R-random)");
            temp = input.next().toUpperCase();
            if(!isNumber(temp) || Double.parseDouble(temp) < 0 || Double.parseDouble(temp) > percentLeft)			
               tryRollingChords = ((int)(rand.nextDouble()*(percentLeft+1)))/100.0;
            else
               tryRollingChords = Double.parseDouble(temp)/100.0;  
         } 
         
         percentLeft = 100-tryScaleRun-tryTriads-tryRollingChords;
         if(percentLeft > 0)
         {
            System.out.println("What % of events should be a sequence of static chord progressions and melodies (0-" + percentLeft +", R-random)");
            temp = input.next().toUpperCase();
            if(!isNumber(temp) || Double.parseDouble(temp) < 0 || Double.parseDouble(temp) > percentLeft)			
               trySequence = ((int)(rand.nextDouble()*(percentLeft+1)))/100.0;
            else
               trySequence = Double.parseDouble(temp)/100.0;
            if(trySequence > 0)
            {
               System.out.println("What % of sequence melodies should have a counter-melody (R for random)");
               temp = input.next().toUpperCase();
               if(!isNumber(temp))					//between 0 and 100
                  counterMelody = ((int)(rand.nextDouble()*101))/100.0;
               else
                  counterMelody = Double.parseDouble(temp)/100.0;
               System.out.println("What time signature: 2 for 2/4, 3 for 3/4, 4 for 4/4, 5 for 4/4/ waltz (R for random)");
               String timeChoice =input.next().toUpperCase();
               if(!isNumber(timeChoice) || Integer.parseInt(timeChoice) < 2 || Integer.parseInt(timeChoice) > 5)
                  timeSig = (int)(rand.nextDouble()*4) + 2;  
               else
                  timeSig = Integer.parseInt(timeChoice);	
            }
         }
      }
   }
   
   System.out.println("What % of improvised events should be a variation on the same melody (10-low, 25-medium, 40-high) (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 0 and 100
      tryMelodyTheme = ((int)(rand.nextDouble()*101))/100.0;
   else
      tryMelodyTheme = Double.parseDouble(temp)/100.0;
   
   if (tryMelodyTheme > 0)
   {
      System.out.println("What % of melody events should follow standard melody rules (R for random)");
      temp = input.next().toUpperCase();
      if(!isNumber(temp))					//between 0 and 100
         melodyRules = ((int)(rand.nextDouble()*101))/100.0;
      else
         melodyRules = Double.parseDouble(temp)/100.0;
   }
   
               System.out.println("What % of improvised events should be a newly generated phrase (5-low, 15-medium, 30-high, must be <="+ percentLeft +") (R for random)");
      temp = input.next().toUpperCase();
      if(!isNumber(temp) || Double.parseDouble(temp) < 0 || Double.parseDouble(temp) > percentLeft)	
         tryPhrase = ((int)(rand.nextDouble()*(percentLeft+1)))/100.0;
      else
         tryPhrase = Double.parseDouble(temp)/100.0;
   
          
   System.out.println("How often do you want to use alternate chords like 6th and 11th (0%-100%), R for random");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))
      altChords = (int)(rand.nextDouble()*101)/100.0;
   else
      altChords = Integer.parseInt(temp)/100.0;
   
   System.out.println("How often do you want to use chord inversions (0%-100%), R for random");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))
      doChordInversions = (int)(rand.nextDouble()*101)/100.0;
   else
      doChordInversions = Integer.parseInt(temp)/100.0;
   
   
   System.out.println("Do you want to force the chords to be arpeggios (y)es, (n)o (R for random)");
   temp = input.next().toUpperCase();
   forceArp = false;	
   double forceCh = rand.nextDouble();
   if(temp.charAt(0)=='Y' || (temp.charAt(0)=='R' && forceCh < .5))
   {
      forceArp = true; 
      flair = 0; 
   }  
   else
   {
      System.out.println("What % of the time should chords be rolled instead of struck (R for random)");
      temp = input.next().toUpperCase();
      if(!isNumber(temp))				
         flair = ((int)(rand.nextDouble()*81))/100.0;
      else
         flair = Double.parseDouble(temp)/100.0;
   } 
   int flairRangeMin=2;    
   if(flair > 0)
   {
      System.out.println("What duration of notes do you want to have for rolled chords (16, 32, 64) (R for random)");
      flairRangeMin=16;
   }
   else
      System.out.println("What duration of notes do you want to have for rolled chords (2, 4, 8) (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp) || Integer.parseInt(temp)%2 != 0)					
      rollDelay = wholeNote/(int)(Math.pow(2,(int)(rand.nextDouble()*3)+flairRangeMin));
   else
      rollDelay = wholeNote/Integer.parseInt(temp);
   
   
   System.out.println("Do you want to force the chord durations (y)es, (n)o (R for random)");
   temp = input.next().toUpperCase();
   forceChordLength = -1;
   forceCh = rand.nextDouble();
   if(temp.charAt(0)=='Y' || (temp.charAt(0)=='R' && forceCh < .5))
   {
      System.out.println("(1)whole note, (2)half-note, (4)quarter-note, (8)eighth note (R for random)");
      temp = input.next().toUpperCase();
      if(!isNumber(temp) || Integer.parseInt(temp)%2 != 0)					
         forceChordLength = wholeNote/(int)(Math.pow(2,(int)(rand.nextDouble()*4)+1));
      else
         forceChordLength = wholeNote/Integer.parseInt(temp);
   }     
   
   System.out.println("What % of melody notes should have a harmony (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 0 and 100
      harmonize = ((int)(rand.nextDouble()*101))/100.0;
   else
      harmonize = Double.parseDouble(temp)/100.0;
   
   System.out.println("What % of business - melody events will be notes and not rests (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 60 and 100
      busyNess = ((int)(rand.nextDouble()*41)+60)/100.0;
   else
      busyNess = Double.parseDouble(temp)/100.0;
   
   System.out.println("What % of rests should have a chord played at its chord interval (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 0 and 100
      chordBusyNess = ((int)(rand.nextDouble()*101))/100.0;
   else
      chordBusyNess = Double.parseDouble(temp)/100.0;
   
   System.out.println("What % of the time should the next melody note be within 5 notes of the current one (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp))					//between 0 and 100
      noteRange = ((int)(rand.nextDouble()*101))/100.0;
   else
      noteRange = Double.parseDouble(temp)/100.0;
    
   System.out.println("Would you prefer chord placement by wholeNote multiples (-1), number of notes (0) or time intervals (1) (R for random)");
   temp = input.next().toUpperCase();
   if(!isNumber(temp) || Integer.parseInt(temp)>1 || Integer.parseInt(temp)<-1)					
      chordTime = (int)(rand.nextDouble()*3)-1;							//between -1 and 1
   else
      chordTime = Integer.parseInt(temp);
   if(freeTime==1)
   {
      chordTimeSeed = (int)(rand.nextDouble()*5)+1;		//1-5
      chordTimeSeed2 = (int)(rand.nextDouble()*5)+6;		//6-10
   }
   else
      if(chordTime==0)
      {
         chordTimeSeed = (int)(rand.nextDouble()*10)+1;		//1-10
         chordTimeSeed2 = (int)(rand.nextDouble()*10)+11;	//11-20
      }
   if(chordTime != -1)
   {
      System.out.println("First chord interval (1-20) (R for Random)");
      String InstChoice =input.next().toUpperCase();
      if(isNumber(InstChoice))			
         chordTimeSeed = Integer.parseInt(InstChoice);
      System.out.println("Second chord interval (1-20) (R for Random) (999) for no second chord interval");
      InstChoice =input.next().toUpperCase();
      if(isNumber(InstChoice))			
         chordTimeSeed2 = Integer.parseInt(InstChoice);
   }
   else	//chordTime == -1
   {
      System.out.println("At how many multiples of whole notes would you like a chord to hit (1-4) (R for Random)");
      String InstChoice =input.next().toUpperCase();
      int multiple = 2;
      if(isNumber(InstChoice))			
         chordInterval = wholeNote*(Integer.parseInt(InstChoice));
   }
   	
   System.out.println(InstrumentList());
   System.out.println("What instrument would you like for the melody?");
   String InstChoice =input.next().toUpperCase();
   if(!isNumber(InstChoice))			//between 0 and 127
      melodyInst = (int)(rand.nextDouble()*128);  
   else
      melodyInst = Integer.parseInt(InstChoice);
   
   System.out.println("What instrument would you like for the accompaniment (chords)?");
   InstChoice =input.next().toUpperCase();
   if(!isNumber(InstChoice))			//between 0 and 127
      chordInst = (int)(rand.nextDouble()*128);  
   else
      chordInst = Integer.parseInt(InstChoice);
   
   System.out.println("What instrument would you like for the bass?");
   InstChoice =input.next().toUpperCase();
   if(!isNumber(InstChoice))			//between 0 and 127
      bassInst = (int)(rand.nextDouble()*128);  
   else
      bassInst = Integer.parseInt(InstChoice);
      
   InstChoice="0";
   if(harmonize>0)
   {
      System.out.println("What instrument would you like for the harmony?");
      InstChoice =input.next().toUpperCase();
   }
   if(!isNumber(InstChoice))			//between 0 and 127
      harmonyInst = (int)(rand.nextDouble()*128);  
   else
      harmonyInst = Integer.parseInt(InstChoice);
   */
   
      String seedInfo = "";
      if(seed < 0)
         seedInfo+="none";
      else
         seedInfo+=""+seed;
      String songInfo = "";     
      songInfo+=("You have chosen to compose a "+speedWord+"song in the "+specificScale.getName()+" scale in the key of "+ intToKey(TONE)+"\n");
      songInfo+=("Please open and run '"+specificScale.getName()+".mid' in this folder"+"\n");      
      songInfo+=("TONE:"+TONE+"("+intToKey(TONE)+")"+"			tempo:"+tempo+"			seed:"+seedInfo+"			noteRange:"+noteRange+"\n");
      songInfo+=("chordInst:"+MIDIinstr.get(chordInst)+"	melodyInstr:"+MIDIinstr.get(melodyInst)+"	harmonyInst:"+MIDIinstr.get(harmonyInst)+"	bassInst:"+MIDIinstr.get(bassInst)+"\n");
      songInfo+=("busyNess:"+busyNess+"		altChords:"+altChords+"			forceChordLength:"+forceChordLength+"		popularChords:"+popularChords+"\n");			
      songInfo+=("chordTime:"+chordTime+"			chordTimeSeed:"+chordTimeSeed+"			chordTimeSeed2:"+chordTimeSeed2+"		chordInterval:"+chordInterval+"\n");
      songInfo+=("rollDelay:"+rollDelay+"			flair:"+flair+"			chordBusyNess:"+chordBusyNess+"		forceArp:"+forceArp+"\n");
      songInfo+=("tryStructure:"+tryStructure+"		tryScaleRun:"+tryScaleRun+"			tryTriads:"+tryTriads+"			tryRollingChords:"+tryRollingChords+"\n");
      songInfo+=("trySequence:"+trySequence+"		timeSig:"+timeSig+"			tryImprov:"+tryImprov+"			tryPhrase:"+tryPhrase+"\n");
      songInfo+=("allowTritone:"+allowTritone+"		freeTime:"+freeTime+"			tryChordTheme:"+tryChordTheme+"\n");
      songInfo+=("chordThemeRules:"+chordThemeRules+"	startChordThemeOnI:"+startChordThemeOnI+"		endChordThemeOnI_IVorV:"+endChordThemeOnI_IVorV+" 	majChordProgressionRules:"+majChordProgressionRules+"\n");
      songInfo+=("tryMelodyTheme:"+tryMelodyTheme+"		melodyRules:"+melodyRules+"			durationFormat:"+durationFormat+"		startOnWiseNote:"+startOnWiseNote+"\n");
      songInfo+=("resolveDisjunct:"+resolveDisjunct+"		ascendMin6th:"+ascendMin6th+"		ascendMin7th:"+ascendMin7th+"		counterMelody:"+counterMelody+"\n");
      songInfo+=("allowRelativeScale:"+allowRelativeScale+"	allowParallelScale:"+allowParallelScale+"	allowSimilarScale:"+allowSimilarScale+"		allowExpandedScale:"+allowExpandedScale+"\n");
      songInfo+=("harmonize:"+harmonize+"		melodyBuildStrat:"+melodyBuildStrat+"\n");
      if(altScale!=null)  
         songInfo+=("scale/key switch:"+altScale.getName()+" in the key of "+intToKey(altScale.getKey())+"\n");
      System.out.println(songInfo);
      String filename = specificScale.getName()+".txt";
      System.setOut(new PrintStream(new FileOutputStream(filename)));
      System.out.println(songInfo);
   }

   public static void open(Sequencer sequencer) 
   {
      try {
         sequencer = MidiSystem.getSequencer();
         sequencer.open();
      } 
      catch (Exception e) { e.printStackTrace(); }
   }

   private static void createEvent(Track track, int type, int chan, int num, long tick, int inst) {
      ShortMessage message = new ShortMessage();
      try {
         message.setMessage(type, chan, inst, num); 
         track.add(new MidiEvent(message,tick));
      } 
      catch (Exception ex) { ex.printStackTrace(); }
   }

//pre: 0<=inst<=128
//post: creates and returns a new ShortMessage (PROGRAM_CHANGE) with the given instrument
   private static ShortMessage ChangeInstrument(int inst, int channel) throws InvalidMidiDataException, MidiUnavailableException
   {
      ShortMessage temp=new ShortMessage();
      temp.setMessage(ShortMessage.PROGRAM_CHANGE, channel, inst, 100);
      return temp;
   }

//adds a tempo meta event to set the speed of the song
   private static void setTempo(int newTempo, int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
   //****  set tempo (meta event)  ****
      MetaMessage mmessage = new MetaMessage();
      int l = 60*1000000/newTempo;
      mmessage.setMessage(0x51,new byte[]{(byte)(l/65536), (byte)(l%65536/256), (byte)(l%256)}, 3);
      music.add(new MidiEvent(mmessage, where));
   }

   private static void addEvent(Track track, int type, byte[] data, long where)
   {
      MetaMessage message = new MetaMessage();
      try
      {
         message.setMessage(type, data, data.length);
         MidiEvent event = new MidiEvent( message, where );
         track.add(event);
      }
      catch (InvalidMidiDataException e)
      {
         e.printStackTrace();
      }
   }

//shows list contents in {42, 58, 46} format given list, false, false
//shows list contents in {B, -, D#} format given list, true, false
//shows list contents in {wholeNote/4, wholeNote/8, wholeNote/1} format given list, false, true
   public static String printArray(int[]list, boolean noteFormat, boolean durationFormat)
   {
      String ret = "{";
      for(int i=0; i<list.length; i++)
      {
         if(noteFormat==true && durationFormat==false)
         {
            if(list[i]==0)
               ret += "-";							//denotes a rest
            else
               ret += ""+intToKey(list[i]);
         }
         else
            if(noteFormat==false && durationFormat==true)
            {
               if(wholeNote >= Math.abs(list[i]))
                  ret += "wholeNote/"+(wholeNote/(0.0+list[i]));
               else
                  ret += "wholeNote*"+((0.0+list[i])/wholeNote); 
            }   
            else
               ret += ""+list[i];
         if(i < list.length-1)
            ret += ", ";
      }
      return ret + "}";
   }

   private static String printChords(int[]groupIndexes,int[]chordIndexes)
   {
      String ret = "{";
      for(int i=0; i<groupIndexes.length; i++)
      {
         ret += ((Chord)(chordSetsA[groupIndexes[i]].get(chordIndexes[i]))).getName();
         if(i < groupIndexes.length-1)
            ret += ", ";
      }
      return ret + "}";
   
   }

//map of MIDI instrument int values to their String names
   private static Map buildMap()
   {
      Map MIDIinstr = new HashMap();
      MIDIinstr.put(0,"Acoustic Grand");
      MIDIinstr.put(8,"Celesta");
      MIDIinstr.put(1,"Bright Acoustic");
      MIDIinstr.put(9,"Glockenspiel");
      MIDIinstr.put(2,"Electric Grand");
      MIDIinstr.put(10,"Music Box");
      MIDIinstr.put(3,"Honky-Tonk");
      MIDIinstr.put(11,"Vibraphone");
      MIDIinstr.put(4,"Electric Piano");
      MIDIinstr.put(12,"Marimba");
      MIDIinstr.put(5,"Electric Piano");
      MIDIinstr.put(13,"Xylophone");
      MIDIinstr.put(6,"Harpsichord");
      MIDIinstr.put(14,"Tubular Bells");
      MIDIinstr.put(7,"Clavinet");
      MIDIinstr.put(15,"Dulcimer");
      MIDIinstr.put(16,"Drawbar Organ");
      MIDIinstr.put(24,"Nylon String Guitar");
      MIDIinstr.put(17,"Percussive Organ");
      MIDIinstr.put(25,"Steel String Guitar");
      MIDIinstr.put(18,"Rock Organ");
      MIDIinstr.put(26,"Electric Jazz Guitar");
      MIDIinstr.put(19,"Church Organ");
      MIDIinstr.put(27,"Electric Clean Guitar");
      MIDIinstr.put(20,"Reed Organ");
      MIDIinstr.put(28,"Electric Muted Guitar");
      MIDIinstr.put(21,"Accoridan");
      MIDIinstr.put(29,"Overdriven Guitar");
      MIDIinstr.put(22,"Harmonica");
      MIDIinstr.put(30,"Distortion Guitar");
      MIDIinstr.put(23,"Tango Accordian");
      MIDIinstr.put(31,"Guitar Harmonics");
      MIDIinstr.put(32,"Acoustic Bass");
      MIDIinstr.put(40,"Violin");
      MIDIinstr.put(33,"Electric Bass(finger)");
      MIDIinstr.put(41,"Viola");
      MIDIinstr.put(34,"Electric Bass(pick)");
      MIDIinstr.put(42,"Cello");
      MIDIinstr.put(35,"Fretless Bass");
      MIDIinstr.put(43,"Contrabass");
      MIDIinstr.put(36,"Slap Bass 1");
      MIDIinstr.put(44,"Tremolo Strings");
      MIDIinstr.put(37,"Slap Bass 2");
      MIDIinstr.put(45,"Pizzicato Strings");
      MIDIinstr.put(38,"Synth Bass 1");
      MIDIinstr.put(46,"Orchestral Strings");
      MIDIinstr.put(39,"Synth Bass 2");
      MIDIinstr.put(47,"Timpani");
      MIDIinstr.put(48,"String Ensemble 1");
      MIDIinstr.put(56,"Trumpet");
      MIDIinstr.put(49,"String Ensemble 2");
      MIDIinstr.put(57,"Trombone");
      MIDIinstr.put(50,"SynthStrings 1");
      MIDIinstr.put(58,"Tuba");
      MIDIinstr.put(51,"SynthStrings 2");
      MIDIinstr.put(59,"Muted Trumpet");
      MIDIinstr.put(52,"Choir Aahs");
      MIDIinstr.put(60,"French Horn");
      MIDIinstr.put(53,"Voice Oohs");
      MIDIinstr.put(61,"Brass Section");
      MIDIinstr.put(54,"Synth Voice");
      MIDIinstr.put(62,"SynthBrass 1");
      MIDIinstr.put(55,"Orchestra Hit");
      MIDIinstr.put(63,"SynthBrass 2");
      MIDIinstr.put(64,"Soprano Sax");
      MIDIinstr.put(72,"Piccolo");
      MIDIinstr.put(65,"Alto Sax");
      MIDIinstr.put(73,"Flute");
      MIDIinstr.put(66,"Tenor Sax");
      MIDIinstr.put(74,"Recorder");
      MIDIinstr.put(67,"Baritone Sax");
      MIDIinstr.put(75,"Pan Flute");
      MIDIinstr.put(68,"Oboe");
      MIDIinstr.put(76,"Blown Bottle");
      MIDIinstr.put(69,"English Horn");
      MIDIinstr.put(77,"Skakuhachi");
      MIDIinstr.put(70,"Bassoon");
      MIDIinstr.put(78,"Whistle");
      MIDIinstr.put(71,"Clarinet");
      MIDIinstr.put(79,"Ocarina");
      MIDIinstr.put(80,"Lead 1 (square)");
      MIDIinstr.put(88,"Pad 1 (new age)");
      MIDIinstr.put(81,"Lead 2 (sawtooth)");
      MIDIinstr.put(89,"Pad 2 (warm)");
      MIDIinstr.put(82,"Lead 3 (calliope)");
      MIDIinstr.put(90,"Pad 3 (polysynth)");
      MIDIinstr.put(83,"Lead 4 (chiff)");
      MIDIinstr.put(91,"Pad 4 (choir)");
      MIDIinstr.put(84,"Lead 5 (charang)");
      MIDIinstr.put(92,"Pad 5 (bowed)");
      MIDIinstr.put(85,"Lead 6 (voice)");
      MIDIinstr.put(93,"Pad 6 (metallic)");
      MIDIinstr.put(86,"Lead 7 (fifths)");
      MIDIinstr.put(94,"Pad 7 (halo)");
      MIDIinstr.put(87,"Lead 8 (bass+lead)");
      MIDIinstr.put(95,"Pad 8 (sweep)");
      MIDIinstr.put(96,"FX 1 (rain)");
      MIDIinstr.put(104,"Sitar");
      MIDIinstr.put(97,"FX 2 (soundtrack)");
      MIDIinstr.put(105,"Banjo");
      MIDIinstr.put(98,"FX 3 (crystal)");
      MIDIinstr.put(106,"Shamisen");
      MIDIinstr.put(99,"FX 4 (atmosphere)");
      MIDIinstr.put(107,"Koto");
      MIDIinstr.put(100,"FX 5 (brightness)");
      MIDIinstr.put(108,"Kalimba");
      MIDIinstr.put(101,"FX 6 (goblins)");
      MIDIinstr.put(109,"Bagpipe");
      MIDIinstr.put(102,"FX 7 (echoes)");
      MIDIinstr.put(110,"Fiddle");
      MIDIinstr.put(103,"FX 8 (sci-fi)");
      MIDIinstr.put(111,"Shanai");
      MIDIinstr.put(112,"Tinkle Bell");
      MIDIinstr.put(120,"Guitar Fret Noise");
      MIDIinstr.put(113,"Agogo");
      MIDIinstr.put(121,"Breath Noise");
      MIDIinstr.put(114,"Steel Drums");
      MIDIinstr.put(122,"Seashore");
      MIDIinstr.put(115,"Woodblock");
      MIDIinstr.put(123,"Bird Tweet");
      MIDIinstr.put(116,"Taiko Drum");
      MIDIinstr.put(124,"Telephone Ring");
      MIDIinstr.put(117,"Melodic Tom");
      MIDIinstr.put(125,"Helicopter");
      MIDIinstr.put(118,"Synth Drum");
      MIDIinstr.put(126,"Applause");
      MIDIinstr.put(119,"Reverse Cymbal");
      MIDIinstr.put(127,"Gunshot");
      return MIDIinstr;
   }

   private static Map buildScaleMap(int TONE)
   {
      Map<Integer, Scale> allScales = new HashMap();
      allScales.put(0,null);
      allScales.put(1,new Major(TONE));
      allScales.put(2,new Dorian(TONE));
      allScales.put(3,new Phrygian(TONE));
      allScales.put(4,new Lydian(TONE)); 
      allScales.put(5,new Mixolydian(TONE));			
      allScales.put(6,new Aeolian(TONE));	
      allScales.put(7, new Locrian(TONE));
      allScales.put(8,new Minor(TONE));
      allScales.put(9,new Dorianb2(TONE));	
      allScales.put(10,new LydianAug(TONE));			
      allScales.put(11,new LydianDom(TONE));			
      allScales.put(12,new Mixolydianb6(TONE));			
      allScales.put(13,new LocrianS2(TONE));			
      allScales.put(14,new Altered(TONE));			
      allScales.put(15,new HarmonicMinor(TONE)); 
      allScales.put(16,new Locrian6(TONE));
      allScales.put(17,new IonianS5(TONE)); 
      allScales.put(18,new DorianS4(TONE));	
      allScales.put(19,new PhrygianMaj(TONE));	
      allScales.put(20,new LydianS2(TONE)); 
      allScales.put(21,new Ultralocrian(TONE)); 
      allScales.put(22,new Augmented(TONE));
      allScales.put(23,new Chromatic(TONE));
      allScales.put(24,new DiminishedDominant(TONE));
      allScales.put(25,new DiminishedMinor(TONE));
      allScales.put(26,new HalfWholeDiminished(TONE));
      allScales.put(27,new WholeHalfDiminished(TONE));
      allScales.put(28,new WholeTone(TONE));
      allScales.put(29,new MajorPentatonic(TONE));		
      allScales.put(30,new MinorPentatonic(TONE));				
      allScales.put(31,new DominantPentatonic(TONE));				
      allScales.put(32,new Balinese(TONE)); 
      allScales.put(33,new Chinese(TONE));
      allScales.put(34,new Chinese2(TONE)); 
      allScales.put(35,new Egyptian(TONE));	
      allScales.put(36,new Hirajoshi(TONE));
      allScales.put(37,new Hirajoshi2(TONE));
      allScales.put(38,new Iwato(TONE));
      allScales.put(39,new Japanese(TONE));
      allScales.put(40,new Kumoi(TONE));
      allScales.put(41,new Mongolian(TONE));
      allScales.put(42,new Pelog(TONE));
      allScales.put(43,new Blues(TONE));
      allScales.put(44,new BluesVar1(TONE));
      allScales.put(45,new BluesVar2(TONE));
      allScales.put(46,new BluesVar3(TONE));
      allScales.put(47,new MajorBlues(TONE));
      allScales.put(48,new BebopMajor(TONE));
      allScales.put(49,new BebopMinor(TONE));
      allScales.put(50,new BebopDominant(TONE));
      allScales.put(51,new BebopHalfDim(TONE));
      allScales.put(52,new BebopDorian(TONE));
      allScales.put(53,new EightToneSpanish(TONE));
      allScales.put(54,new Algerian(TONE));
      allScales.put(55,new Arabian(TONE));
      allScales.put(56,new Byzantine(TONE));
      allScales.put(57,new Enigmatic(TONE));
      allScales.put(58,new Flamenco(TONE));
      allScales.put(59,new HarmonicMajor(TONE));
      allScales.put(60,new HungarianGypsy(TONE));
      allScales.put(61,new HungarianMajor(TONE));
      allScales.put(62,new LeadingWholeTone(TONE));
      allScales.put(63,new LydianMinor(TONE));
      allScales.put(64,new NeopolitanMajor(TONE));
      allScales.put(65,new NeopolitanMinor(TONE));
      allScales.put(66,new Oriental(TONE));
      allScales.put(67,new Persian(TONE));
      allScales.put(68,new Spanish(TONE));	
      return allScales;
   }

//returns a String of all MIDI instruments 
   private static String InstrumentList()
   {
      String instruments="MIDI Instrument Numbers\n";
      instruments+="      PERCUSSIVE                     SOUND EFFECTS\n";
      instruments+=" 112  Tinkle Bell               120   Guitar Fret Noise\n";
      instruments+=" 113  Agogo                     121   Breath Noise\n";
      instruments+=" 114  Steel Drums               122   Seashore\n";
      instruments+=" 115  Woodblock                 123   Bird Tweet\n";
      instruments+=" 116  Taiko Drum                124   Telephone Ring\n";
      instruments+=" 117  Melodic Tom               125   Helicopter\n";
      instruments+=" 118  Synth Drum                126   Applause\n";
      instruments+=" 119  Reverse Cymbal            127   Gunshot\n";
   
      instruments+="      SYNTH EFFECTS                  ETHNIC\n";
      instruments+="  96  FX 1 (rain)               104   Sitar\n";
      instruments+="  97  FX 2 (soundtrack)         105   Banjo\n";
      instruments+="  98  FX 3 (crystal)            106   Shamisen\n";
      instruments+="  99  FX 4 (atmosphere)         107   Koto\n";
      instruments+=" 100  FX 5 (brightness)         108   Kalimba\n";
      instruments+=" 101  FX 6 (goblins)            109   Bagpipe\n";
      instruments+=" 102  FX 7 (echoes)             110   Fiddle\n";
      instruments+=" 103  FX 8 (sci-fi)             111   Shanai\n\n";
   
      instruments+="      SYNTH LEAD                     SYNTH PAD\n";
      instruments+=" 80   Lead 1 (square)           88   Pad 1 (new age)\n";
      instruments+=" 81   Lead 2 (sawtooth)         89   Pad 2 (warm)\n";
      instruments+=" 82   Lead 3 (calliope)         90   Pad 3 (polysynth)\n";
      instruments+=" 83   Lead 4 (chiff)            91   Pad 4 (choir)\n";
      instruments+=" 84   Lead 5 (charang)          92   Pad 5 (bowed)\n";
      instruments+=" 85   Lead 6 (voice)            93   Pad 6 (metallic)\n";
      instruments+=" 86   Lead 7 (fifths)           94   Pad 7 (halo)\n";
      instruments+=" 87   Lead 8 (bass+lead)        95   Pad 8 (sweep)\n\n";
   
      instruments+="      REED                           PIPE\n";
      instruments+=" 64   Soprano Sax               72   Piccolo\n";
      instruments+=" 65   Alto Sax                  73   Flute\n";
      instruments+=" 66   Tenor Sax                 74   Recorder\n";
      instruments+=" 67   Baritone Sax              75   Pan Flute\n";
      instruments+=" 68   Oboe                      76   Blown Bottle\n";
      instruments+=" 69   English Horn              77   Skakuhachi\n";
      instruments+=" 70   Bassoon                   78   Whistle\n";
      instruments+=" 71   Clarinet                  79   Ocarina\n\n";
   
      instruments+="      ENSEMBLE                       BRASS\n";
      instruments+=" 48   String Ensemble 1         56   Trumpet\n";
      instruments+=" 49   String Ensemble 2         57   Trombone\n";
      instruments+=" 50   SynthStrings 1            58   Tuba\n";
      instruments+=" 51   SynthStrings 2            59   Muted Trumpet\n";
      instruments+=" 52   Choir Aahs                60   French Horn\n";
      instruments+=" 53   Voice Oohs                61   Brass Section\n";
      instruments+=" 54   Synth Voice               62   SynthBrass 1\n";
      instruments+=" 55   Orchestra Hit             63   SynthBrass 2\n\n";
   
      instruments+="      BASSCHNL                           SOLO STRINGS\n";
      instruments+=" 32   Acoustic Bass             40   Violin\n";
      instruments+=" 33   Electric Bass(finger)     41   Viola\n";
      instruments+=" 34   Electric Bass(pick)       42   Cello\n";
      instruments+=" 35   Fretless Bass             43   Contrabass\n";
      instruments+=" 36   Slap Bass 1               44   Tremolo Strings\n";
      instruments+=" 37   Slap Bass 2               45   Pizzicato Strings\n";
      instruments+=" 38   Synth Bass 1              46   Orchestral Strings\n";
      instruments+=" 39   Synth Bass 2              47   Timpani\n\n";
   
      instruments+="      ORGAN                          GUITAR\n";
      instruments+=" 16   Drawbar Organ             24   Nylon String Guitar\n";
      instruments+=" 17   Percussive Organ          25   Steel String Guitar\n";
      instruments+=" 18   Rock Organ                26   Electric Jazz Guitar\n";
      instruments+=" 19   Church Organ              27   Electric Clean Guitar\n";
      instruments+=" 20   Reed Organ                28   Electric Muted Guitar\n";
      instruments+=" 21   Accoridan                 29   Overdriven Guitar\n";
      instruments+=" 22   Harmonica                 30   Distortion Guitar\n";
      instruments+=" 23   Tango Accordian           31   Guitar Harmonics\n\n";
   
      instruments+="      PIANO                          CHROMATIC PERCUSSION\n";
      instruments+=" 0    Acoustic Grand             8   Celesta\n";
      instruments+=" 1    Bright Acoustic            9   Glockenspiel\n";
      instruments+=" 2    Electric Grand            10   Music Box\n";
      instruments+=" 3    Honky-Tonk                11   Vibraphone\n";
      instruments+=" 4    Electric Piano 1          12   Marimba\n";
      instruments+=" 5    Electric Piano 2          13   Xylophone\n";
      instruments+=" 6    Harpsichord               14   Tubular Bells\n";
      instruments+=" 7    Clavinet                  15   Dulcimer\n\n";
      
      instruments+=" R    Random Instrument\n";
      return instruments;
   }

//returns a String of available scales to pick from
   private static String ScaleList()
   {
      String scales="Scales:\n";
      scales+="	MAJOR					MELODIC MINOR\n";
      scales+=" 1	Major (Ionian)			8	Melodic Minor (Jazz Min.)\n";
      scales+=" 2	Dorian				9	Dorian b2 (Javanese)\n";
      scales+=" 3	Phrygian			10	Lydian Augmented\n";
      scales+=" 4	Lydian				11	Lydian Dominant (Overtone)\n";
      scales+=" 5	Mixolydian			12	Mixolydian b6 (Hindu)\n";
      scales+=" 6	Aeolian (Nat. Min.)		13	Locrian #2\n";
      scales+=" 7	Locrian				14	Altered\n";
      scales+="\n";
      scales+="	HARMONIC MINOR				SYMMETRICAL\n";
      scales+=" 15	Harmonic Minor(Mohammedan)	22	Augmented\n";
      scales+=" 16	Locrian 6			23	Chromatic\n";
      scales+=" 17	Ionian #5 (Jewish)		24	Diminished Dominant\n";
      scales+=" 18	Dorian #4 (Jewish2)		25	Diminished Minor\n";
      scales+=" 19	Phrygian Major			26	Half-Whole Diminished\n";
      scales+=" 20	Lydian #2			27	Whole-Half Diminished\n";
      scales+=" 21	Ultralocrian			28	Whole Tone\n";
      scales+="\n";
      scales+="	PENTATONIC\n";
      scales+=" 29	Major Pentatonic (Mongolian)	36	Hirajoshi\n";		
      scales+=" 30	Minor Pentatonic		37	Hirajoshi 2\n";
      scales+=" 31	Dominant Pentatonic		38	Iwato\n";
      scales+=" 32	Balinese			39	Japanese\n";
      scales+=" 33	Chinese				40	Kumoi\n";
      scales+=" 34	Chinese 2			41	Mongolian\n";
      scales+=" 35	Egyptian			42	Pelog\n";
      scales+="\n";
   
      scales+="	BLUES					BEBOP\n";
      scales+=" 43	Blues				48	Bebop Major\n";
      scales+=" 44	Blues Variation 1		49	Bebop Minor\n";
      scales+=" 45	Blues Variation 2		50	Bebop Dominant\n";
      scales+=" 46	Blues Variation 3		51	Bebop Half-Diminished\n";
      scales+=" 47	Major Blues			52	Bebop Dorian\n";
   
      scales+="\n";
      scales+="	EXOTIC\n";
      scales+=" 53	8 Tone Spanish			61	Hungarian Major\n";
      scales+=" 54	Algerian (Hungarian Min.)	62	Leading Whole Tone\n";
      scales+=" 55	Arabian (Maj.Locrian)		63	Lydian Minor\n";
      scales+=" 56	Byzantine (Double Harmonic)	64	Neopolitan Major\n";
      scales+=" 57	Enigmatic			65	Neopolitan Minor\n";
      scales+=" 58	Flamenco			66	Oriental\n";
      scales+=" 59	Harmonic Major			67	Persian\n";
      scales+=" 60	Hungarian Gypsy			68	Spanish\n";
   
      return scales;
   }

//pre:  a and b are valid index #s of nums
//post: swaps nums[a] with nums[b]
   public static void swap(int[] nums, int a, int b)
   {
      int temp = nums[a];
      nums[a] = nums[b];
      nums[b] = temp;
   }

//post: nums is sorted in ascending order              
   public static void selSort(int[] nums)
   {
      int min, size = nums.length;
      for (int i=0; i < size; i++)
      {
         min = i;
         for (int j = i + 1; j < size; j++)
         {
            if (nums[j] < nums[min])
               min = j;
         }
         swap (nums, i, min);
      }
   }

//post: returns the min element in the array 'nums' that is not a rest (< 0)            
   public static int min(int[] nums)
   {
      if (nums == null)
         return -999;
      int firstNonRest = 0;
      for (int i=0; i < nums.length; i++)
         if (nums[i] >= 0 )
         {
            firstNonRest = nums[i];
            break;     
         }  
      int min=firstNonRest;
      for (int i=0; i < nums.length; i++)
         if (nums[i] < min && nums[i] >= 0)
            min = nums[i];
      return min;
   }

//post: returns the max element in the array 'nums', considering rests as possible max values            
   public static int max(int[] nums)
   {
      if (nums == null)
         return -999;
      int max=-999;
      for (int i=0; i < nums.length; i++)
         if (Math.abs(nums[i]) > max)
            max = Math.abs(nums[i]);
      return max;
   }

//post:  scrambles the elements in list (puts in random order)
   public static void scramble(int[]list)
   {
      if(list.length > 1)
         for(int i=0; i < 1000; i++)
         {
            int a = (int)(rand.nextDouble() * list.length);
            int b = (int)(rand.nextDouble() * list.length);
            while (a==b)
               b = (int)(rand.nextDouble() * list.length);
            swap(list, a, b);   
         }
   }

//post:  reverses the elements in list
   public static int[] reverse(int[]list)
   {
      int[] newList = new int[list.length];
      int index = 0;
      for(int i=list.length-1; i>=0; i--)
         newList[index++] = list[i];
      return newList;
   }

 //post: shuffles the elements in list
 //[0,1,2,3,4,5,6,7] -> [0,7,1,6,2,5,3,4]
   public static int[] shuffle(int[]list)
   {
      int[] newList = new int[list.length];
      int index = 0;
      for(int i=0; i<newList.length; i+=2)
         newList[i] = list[index++];
      index = 1;
      for(int i=list.length-1; i>=0 && index < newList.length; i--)
      {
         newList[index] = list[i];
         index+=2;   
      }
      return newList;
   }

//post:	shuffles the elements of two lists
//a:[0,1,2,3], b:[9,8,7,6] -> return:[0,9,1,8,2,7,3,6]
   public static int[] shuffle(int[]a, int[]b)
   {
      int[]retVal = new int[a.length + b.length];
      int ai = 0;		//index of list a
      int bi = 0;		//index of list b
      int i = 0;		//index for retVal
      while(i<retVal.length)
      {
         if(ai < a.length && i<retVal.length)
            retVal[i++] = a[ai];
         if(bi < b.length && i<retVal.length)
            retVal[i++] = b[bi];
      }
      return retVal;
   }

//returns true if 'key' is found in the 'list'
   public static boolean search(int[]list, int key)
   {
      for(int i=0; i < list.length; i++)
         if(list[i] == key)
            return true;
      return false;
   }

//returns the sum of elements in a
   public static int sum(int[] a)
   {
      int total=0;
      for(int i:a)
         total += i;
      return total;
   }

//adds 'scalar' to every element of 'array'
   public static int[] addScalar(int[]array, int scalar)
   {
      int[] retVal = array.clone();
      for(int i=0; i<retVal.length; i++)
      {
         if(retVal[i] >= 0)
            retVal[i] += scalar;
      }
      return retVal;
   }

//multiplies 'scalar' to every element of 'array'
   public static int[] multScalar(int[]array, double scalar)
   {
      int[] retVal = array.clone();
      for(int i=0; i<retVal.length; i++)
      {
         if(retVal[i] >= 0)
            retVal[i] = (int)(retVal[i]*scalar);
      }
      return retVal;
   }


//returns an int array of all multiples of n, not including 1
   public static int[] multiples (int n)
   {
      if(n<=0)
         return null;
      ArrayList<Integer> mult = new ArrayList();
      for(int i=2; i<=n; i++)
         if(n%i == 0)
            mult.add(i);
      int[]retVal = new int[mult.size()];
      int index=0;
      for(Integer i:mult)
         retVal[index++] = i;
      return retVal;      
   }

//returns a normalized scale with no octaves built in
   public static int[] primativeScale(int[] notes)
   {
      ArrayList<Integer> primScale = new ArrayList();
      int firstNote = normalize(notes[0]);
      primScale.add(firstNote);
      for(int i=1; i<notes.length; i++)
      {
         int ourNote = normalize(notes[i]);
         if(ourNote!=firstNote)
            primScale.add(ourNote);
         else
            break;
      }
      int[]newNotes = new int[primScale.size()];
      int index = 0;
      for(Integer num:primScale)
         newNotes[index++] = num;
      return newNotes;
   }

//returns the # notes in common bewteen A and B.
//used to find relative and similar scales
   public static int notesInCommon(int[] A, int[]B)
   {
      int[]scaleA = primativeScale(A);
      int[]scaleB = primativeScale(B);
      int common = 0;
      for(int i=0; i<scaleA.length; i++)
         for(int j=0; j<scaleB.length; j++)
            if(normalize(scaleA[i])==normalize(scaleB[j]))
               common++;
      return common;
   }

//returns a scale with the same notes as orig, but a different tonic
//if no such scale exists, return null
   public static Scale findRelativeScale(Scale orig)
   {
      int maxCommon = 0;
   //find the max number of notes in common between orig and any scale with the same number of notes
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null && primativeScale(orig.getNotes()).length==primativeScale(ourScale.getNotes()).length)
            {	//they are only relative scales if they have the same number of notes
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes > maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  maxCommon = commonNotes;
            } 
         }
      }
   //now, make a set of all relative scales
      ArrayList<Scale>relScales = new ArrayList();
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null && primativeScale(orig.getNotes()).length==primativeScale(ourScale.getNotes()).length)
            {	//they are only relative scales if they have the same number of notes
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes == maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  if(maxCommon==primativeScale(ourScale.getNotes()).length)//if we have the same number of common notes as the number of notes in the scale
                     relScales.add(ourScale);										//then we found a relative scale
            } 
         }
      }                
   //return a random relative scale out of relScales
      if(relScales.size() > 0)
         return relScales.get((int)(rand.nextDouble()*relScales.size()));							         
      return null;
   }

//returns a scale with the most similar notes as orig, but not relative
//if no such scale exists, return null
   public static Scale findSimilarScale(Scale orig)
   {
      int maxCommon = 0;
      int numOrigNotes = primativeScale(orig.getNotes()).length;	//we want a similar scale, not a relative.
   																				//so maxNotes can not be == to numOrigNotes
   //find the max number of notes in common between orig and any scale with the same number of notes
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null && primativeScale(orig.getNotes()).length==primativeScale(ourScale.getNotes()).length)
            {	//they are only similar scales if they have the same number of notes
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes > maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  if(commonNotes != numOrigNotes)
                     maxCommon = commonNotes;
            } 
         }
      }
   //now, make a set of all similar scales
      ArrayList<Scale>simScales = new ArrayList();
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null && primativeScale(orig.getNotes()).length==primativeScale(ourScale.getNotes()).length)
            {	//they are only similar scales if they have the same number of notes
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes == maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  simScales.add(ourScale);	
            } 
         }
      }                
   //return a random relative scale out of relScales
      if(simScales.size() > 0)
         return simScales.get((int)(rand.nextDouble()*simScales.size()));							         
      return null;
   }

//returns a scale with the same notes as orig, plus some more
//if no such scale exists, returns the chromatic scale
   public static Scale findExpandedScale(Scale orig)
   {
      int maxCommon = 0;
   //find the max number of notes in common between orig and any scale with the same number of notes
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null)
            {
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes > maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  maxCommon = commonNotes;
            } 
         }
      }
   //now, make a set of all expanded scales
      ArrayList<Scale>expScales = new ArrayList();
      for(int TONE=60; TONE<72; TONE++)	//traverse all keys from C to B
      {
         Map<Integer,Scale> allScales =  buildScaleMap(TONE);
         for(Integer num:allScales.keySet())
         {		
            Scale ourScale = allScales.get(num);
            if(ourScale != null)
            {
               int commonNotes = notesInCommon(orig.getNotes(),ourScale.getNotes());
               if(commonNotes == maxCommon && !(orig.getName()).equals(ourScale.getName()) && orig.getKey()!=ourScale.getKey())
                  if(maxCommon==primativeScale(orig.getNotes()).length)//if we have the same number of common notes as the number of notes in the scale
                     if(primativeScale(orig.getNotes()).length < primativeScale(ourScale.getNotes()).length)	//and orig is a subset of ourscale
                        if(!expScales.contains(ourScale) && !ourScale.getName().equals("Chromatic"))
                           expScales.add(ourScale);								  //then we found an expanded scale
            } 
         }
      }                
   //return a random relative scale out of relScales
      if(expScales.size() > 0)
         return expScales.get((int)(rand.nextDouble()*expScales.size()));
      else
         return (new Chromatic(orig.getKey()));   							         
   }

//pre:orig is either major, minor, harmonic minor, harmonic major,
//							major pentatonic, minor pentatonic, 
//							hungarian major, algerian,
//							locrian, arabian, lydian, lydian Minor,
//							Neopolitan Major, Neopolitan Minor,
//							Bebop Major, Bebop Minor,
//							Phrygian Major, Phrygian 	
//returns a scale that is parallel to orig (maj<->min)
   public static Scale findParallelScale(Scale orig)
   {
      int key = orig.getKey();
      if((orig.getName()).equals("Major"))	//major parallel can be minor or harmonic minor
         if(rand.nextDouble() < .75)
            return (new Minor(key));
         else
            return (new HarmonicMinor(key));
      if((orig.getName()).equals("Minor"))
         return (new Major(key));
      if((orig.getName()).equals("Harmonic Major"))
         return (new HarmonicMinor(key));
      if((orig.getName()).equals("Harmonic Minor"))
         if(rand.nextDouble() < .75)
            return (new HarmonicMajor(key));
         else
            return (new Major(key));
      if((orig.getName()).equals("Major Pentatonic"))
         return (new MinorPentatonic(key));
      if((orig.getName()).equals("Minor Pentatonic"))
         return (new MajorPentatonic(key));
      if((orig.getName()).equals("Hungarian Major"))
         return (new Algerian(key));
      if((orig.getName()).equals("Algerian"))
         return (new HungarianMajor(key));
      if((orig.getName()).equals("Arabian"))
         return (new Locrian(key));
      if((orig.getName()).equals("Locrian"))
         return (new Arabian(key));
      if((orig.getName()).equals("Lydian"))
         return (new LydianMinor(key));
      if((orig.getName()).equals("Lydian Minor"))
         return (new Lydian(key));
      if((orig.getName()).equals("Neopolitan Major"))
         return (new NeopolitanMinor(key));
      if((orig.getName()).equals("Neopolitan Minor"))
         return (new NeopolitanMajor(key));
      if((orig.getName()).equals("Bebop Major"))
         return (new BebopMinor(key));
      if((orig.getName()).equals("Bebop Minor"))
         return (new BebopMajor(key));
      if((orig.getName()).equals("Phrygian Major"))
         return (new Phrygian(key));
      if((orig.getName()).equals("Phrygian"))
         return (new PhrygianMaj(key));
      return null;
   }

   public static int booleanToInt(boolean key)
   {
      if(key)
         return 1;
      return 0;
   }

   public static boolean intToBoolean(int key)
   {
      if(key==1)
         return true;
      return false;
   }

//given the key of the song, return its corresponding MIDI note value
//(returns 60->71 given C->B)
   public static int keyToInt(String key)
   {
      if(key.equals("C"))
         return 60;
      if(key.equals("C#"))
         return 61;
      if(key.equals("D"))
         return 62;
      if(key.equals("D#"))
         return 63;
      if(key.equals("E"))
         return 64;
      if(key.equals("F"))
         return 65;
      if(key.equals("F#"))
         return 66;
      if(key.equals("G"))
         return 67;
      if(key.equals("G#"))
         return 68;
      if(key.equals("A"))
         return 69;
      if(key.equals("A#"))
         return 70;
      if(key.equals("B"))
         return 71;
      return 60;				//if key is not recognized, make it a C
   }

//given a MIDI note value, return its corresponding key (multiples of 12 are C)
//returns "?" if it is not found
   public static String intToKey(int num)
   {
      num=normalize(num);   	
      switch(num)
      {
         case 0: 
            return "C";
         case 1: 
            return "C#";
         case 2: 
            return "D";
         case 3: 
            return "D#";
         case 4: 
            return "E";
         case 5: 
            return "F";
         case 6: 
            return "F#";
         case 7: 
            return "G";
         case 8: 
            return "G#";
         case 9: 
            return "A";
         case 10: 
            return "A#";
         case 11: 
            return "B";
      }
      return "?";			//unknown note value sent
   }

//returns the index of where 'ourNote' is in the 'scale', -1 if it is not in the scale
   public static int indexOfNote(int ourNote, int[] scale)
   {
      for(int i=0; i<scale.length; i++)
         if(normalize(ourNote)==normalize(scale[i]))
            return i;
      return -1;
   }

//given a ourNote, return its normalized value where 0 is the first ourNote in the scale (C)
   public static int normalize(int ourNote)
   {
      while(ourNote>=12)			//strip out any octaves
         ourNote-=12;   	
      return ourNote;
   }

//normalizes a full riff
   public static int[] normalize(int[] ourNotes)
   {
      if(ourNotes==null)
         return null;
      int[] retVal = ourNotes.clone();
      for(int i=0; i<retVal.length; i++)
         retVal[i] = normalize(retVal[i]);
      return retVal;
   }

//given the index of a note in the scale, this finds the highest octave index of that note
   public static int highestNoteIndex(int x)
   {
      for(int i=scale.length-1; i>=0; i--)
      { 
         if(normalize(scale[i]) == normalize(scale[x]))
            return i;
      }
      return -1;
   }

//returns true if ourNote is within the first 'range' notes in that chord	
   public static boolean noteInChord(int ourNote, Chord chord, int range)
   {
      int[] chordArray = chord.getNotes();
      ourNote = normalize(ourNote);
      for(int i=0; i<range; i++)
         if (ourNote==normalize(chordArray[i]))
            return true;
      return false;
   }

//returns true if ourNote is within the first 'range' notes in that chord	
   public static boolean noteInChord(int ourNote, int[]chord, int range)
   {
      ourNote = normalize(ourNote);
      for(int i=0; i<range; i++)
         if (ourNote==normalize(chord[i]))
            return true;
      return false;
   }

//returns true if chord is maj, maj7, m, m7, 7, 5 or oct
   public static boolean isPopularChord(Chord chord)
   {
      String name = chord.getName();
      return ((name.endsWith("maj") || name.endsWith("maj7") || name.endsWith("m") || name.endsWith("m7") || name.endsWith("7") || name.endsWith("5") || name.endsWith("oct"))
         && !name.endsWith("maj6-7") && !name.endsWith("maj7b5") && !name.endsWith("maj7#5") && !name.endsWith("mmaj7") && !name.endsWith("m7b5")
         && !name.endsWith("m7#5") && !name.endsWith("dim7") && !name.endsWith("7b5") && !name.endsWith("7#5") && !name.endsWith("9b5") && !name.endsWith("9#5"));
   }

//given an array of chords, returns a subset of popular chords (maj, maj7, m, m7, 7, 5, oct)
   public static ArrayList<Chord> getPopularChords(ArrayList<Chord> chords)
   {
      ArrayList<Chord> retVal = new ArrayList();
      for(Chord i: chords)
         if(isPopularChord(i))
            retVal.add(i);
      if(retVal.size() == 0)
         return null;
      return retVal;
   }

//returns the chord that has ourNote in it, null of no such chord exists
   public static Chord getChordThatHasNote(int ourNote, ArrayList[]chordSets)
   {
      ArrayList chordsThatHaveNote = getAllChordsThatHasNote(ourNote, chordSets);
      if(chordsThatHaveNote.size() == 0)									//we couldn't find a chord that has that ourNote
         return null;
      ArrayList<Chord> popChords = getPopularChords(chordsThatHaveNote);   
      if(rand.nextDouble() < popularChords && popChords != null)
      {
         int index = (int)(rand.nextDouble()*popChords.size());	//random index of chord that has that ourNote in it   
         return ((Chord)(popChords.get(index)));
      }  
      int index = (int)(rand.nextDouble()*chordsThatHaveNote.size());	//random index of chord that has that ourNote in it   
      return ((Chord)(chordsThatHaveNote.get(index)));
   }

//returns the chord group [element 0] and index [element 1]that has ourNote in it, null of no such chord exists
   public static int[] getChordInfoThatHasNote(int ourNote, ArrayList[]chordSets)
   {
      boolean popChord = (rand.nextDouble() < popularChords);	//do we want a popular chord (maj, maj7, m, m7, 7, 5)
      ArrayList chordInfosThatHaveNote = new ArrayList();
      for(int i=0; i<chordSets.length; i++)
      {
         ArrayList ourChords = chordSets[i];
         for(int j=0; j<ourChords.size(); j++)
         {
            Chord chord = (Chord)(ourChords.get(j));
            if((popChord && isPopularChord(chord) && noteInChord(ourNote, chord, chord.getNotes().length))
            || (!popChord && noteInChord(ourNote, chord, chord.getNotes().length)))
            {
               int [] temp = new int[2];
               temp[0] = i;						//chord group
               temp[1] = j;						//chord index	
               chordInfosThatHaveNote.add(temp);
            }
         }
      }
      if (chordInfosThatHaveNote.size() == 0)//we couldn't find a chord that has that ourNote  
      {													//so do it again and ignore popChord
         for(int i=0; i<chordSets.length; i++)
         {
            ArrayList ourChords = chordSets[i];
            for(int j=0; j<ourChords.size(); j++)
            {
               Chord chord = (Chord)(ourChords.get(j));
               if(noteInChord(ourNote, chord, chord.getNotes().length))
               {
                  int [] temp = new int[2];
                  temp[0] = i;					//chord group
                  temp[1] = j;					//chord index	
                  chordInfosThatHaveNote.add(temp);
               }
            }
         }
      }
      if(chordInfosThatHaveNote.size() == 0)	//we still couldn't find a chord that has that ourNote
         return null;
      int index = (int)(rand.nextDouble()*chordInfosThatHaveNote.size());	//random index of chord that has that ourNote in it   
      return ((int[])chordInfosThatHaveNote.get(index));
   }

 //given ourNote, returns an ArrayList of chords that contains ourNote
   public static ArrayList getAllChordsThatHasNote(int ourNote, ArrayList[]chordSets)
   {
      ArrayList chordsThatHaveNote = new ArrayList();
      for(int i=0; i<chordSets.length; i++)
      {
         ArrayList ourChords = chordSets[i];
         for(int j=0; j<ourChords.size(); j++)
         {
            Chord chord = (Chord)(ourChords.get(j));
            if(noteInChord(ourNote, chord, chord.getNotes().length))
               chordsThatHaveNote.add(chord);
         }
      }
      return chordsThatHaveNote;
   }

 //given a note, returns an array of chords that contains noteA and noteB
   public static ArrayList getAllChordsThatHasNotes(int noteA, int noteB, ArrayList[]chordSets)
   {
      ArrayList chordsThatHaveNote = new ArrayList();
      for(int i=0; i<chordSets.length; i++)
      {
         ArrayList ourChords = chordSets[i];
         for(int j=0; j<ourChords.size(); j++)
         {
            Chord chord = (Chord)(ourChords.get(j));
            if(noteInChord(noteA, chord, chord.getNotes().length) && noteInChord(noteB, chord, chord.getNotes().length))
               chordsThatHaveNote.add(chord);
         }
      }
      if(chordsThatHaveNote.size()==0)
         return null;
      return chordsThatHaveNote;
   }

//returns random chord group [element 0] and index [element 1]
   public static int[] getRandChordInfo(ArrayList[]chordSets)
   {
      boolean popChord = (rand.nextDouble() < popularChords);	//do we want a popular chord (maj, maj7, m, m7, 7, 5)
      if(!popChord)
      {
         int [] temp = new int[2];
         int randGroupIndex = (int)(rand.nextDouble()*chordSets.length);	//I, ii, iii...vii
         temp[0] = randGroupIndex;
         ArrayList ourGroup = chordSets[randGroupIndex];
         int randChordIndex = (int)(rand.nextDouble()*ourGroup.size());
         temp[1] = randChordIndex; 
         return temp;
      }
      ArrayList chordInfos = new ArrayList();
      for(int i=0; i<chordSets.length; i++)
      {
         ArrayList ourChords = chordSets[i];
         for(int j=0; j<ourChords.size(); j++)
         {
            Chord chord = (Chord)(ourChords.get(j));
            if((popChord && isPopularChord(chord)))
            {
               int [] temp = new int[2];
               temp[0] = i;					//chord group
               temp[1] = j;					//chord index	
               chordInfos.add(temp);
            }
         }
      }
      if (chordInfos.size() == 0)			//we couldn't find a chord   
      {												//so do it again and ignore popChord
         int [] temp = new int[2];
         int randGroupIndex = (int)(rand.nextDouble()*chordSets.length);	//I, ii, iii...vii
         temp[0] = randGroupIndex;
         ArrayList ourGroup = chordSets[randGroupIndex];
         int randChordIndex = (int)(rand.nextDouble()*ourGroup.size());
         temp[1] = randChordIndex; 
         return temp;
      }
      int index = (int)(rand.nextDouble()*chordInfos.size());	//random index of chord that has that ourNote in it   
      return ((int[])chordInfos.get(index));
   }

//returns random chord index given a chordSet and an index of a chordGroup from that chordSet
   public static int getRandChordInfo(ArrayList[]chordSets, int chordGroupIndex)
   {
      boolean popChord = (rand.nextDouble() < popularChords);	//do we want a popular chord (maj, maj7, m, m7, 7, 5)
      if(!popChord)
      {
         ArrayList ourGroup = chordSets[chordGroupIndex];
         int randChordIndex = (int)(rand.nextDouble()*ourGroup.size());
         return randChordIndex; 
      }
      ArrayList ourChords = chordSets[chordGroupIndex];
      ArrayList <Integer> chordInfos = new ArrayList();
      for(int j=0; j<ourChords.size(); j++)
      {
         Chord chord = (Chord)(ourChords.get(j));
         if((popChord && isPopularChord(chord)))
            chordInfos.add(j);
      }
      if (chordInfos.size() == 0)			//we couldn't find a chord   
      {												//so do it again and ignore popChord
         ArrayList ourGroup = chordSets[chordGroupIndex];
         int randChordIndex = (int)(rand.nextDouble()*ourGroup.size());
         return randChordIndex;
      }
      int index = (int)(rand.nextDouble()*chordInfos.size());	//random index of chord that has that ourNote in it   
      return (chordInfos.get(index));
   }

//returns the ArrayList of the chords that have the most notes in common with the sent riff
   public static ArrayList getRiffChords(int[]riffNotes, ArrayList[]chordSets)
   {
      ArrayList ourChords = new ArrayList();
      int count = 0;
      int maxCount = -1;
      for(int i=0; i<chordSets.length; i++)	//finds the maximum number of notes that any chord has in common with riffNotes
      {
         ArrayList chordSet = chordSets[i];
         for(int j=0; j<chordSet.size(); j++)
         {
            Chord chord = ((Chord)chordSet.get(j));
            count = 0;
            for(int k=0; k<riffNotes.length; k++)
               if(noteInChord(riffNotes[k], chord, chord.getNotes().length))
                  count++;
            if(count > maxCount)
               maxCount = count;
         }
      }
   
      for(int i=0; i<chordSets.length; i++)	//adds any chord that has the same number of notes in common with maxCount
      {
         ArrayList chordSet = chordSets[i];
         for(int j=0; j<chordSet.size(); j++)
         {
            Chord chord = ((Chord)chordSet.get(j));
            count = 0;
            for(int k=0; k<riffNotes.length; k++)
               if(noteInChord(riffNotes[k], chord, chord.getNotes().length))
                  count++;
            if(count == maxCount)
               ourChords.add(chord);
         }
      }
      ArrayList<Chord> popChords = getPopularChords(ourChords);
      if(rand.nextDouble() < popularChords && popChords != null && popChords.size() > 0)
         return popChords;
      return ourChords;
   }

//returns a set of the notes in the riff that are also in the chord
   public static int[] getRiffNotesInChord(int[]riffNotes, int[]chord)
   {
      Set<Integer> commonNotes = new HashSet();
      for(int i=0; i < riffNotes.length; i++)
         for(int j=0; j < chord.length; j++)
            if(normalize(riffNotes[i]) == normalize(chord[j]) && riffNotes[i]!=0)
               commonNotes.add(riffNotes[i]);
      if(commonNotes.size()==0)
         return null;         
      int index = 0;
      int[] ourNotes = new int[commonNotes.size()];
      for(Integer x:commonNotes)
         ourNotes[index++] = x;
      return ourNotes;
   }

 //returns array of all ourNotes that can be harmonized with ourNote
   public static int[] getHarmonizeNotes(int ourNote, int[]ourNotes)
   {
   //[3,4,5,6,7,8,9,12] => prioritize [3,4,5,7,12]
      int [] harmPriority = {3+ourNote,4+ourNote,5+ourNote,7+ourNote};
      int [] harmRest = {2+ourNote, 6+ourNote,8+ourNote,9+ourNote, 10+ourNote, 12+ourNote};
   
      Set<Integer> noteSet = new TreeSet();
      for(int i=0; i<harmPriority.length; i++)
         if(isNoteInScale(harmPriority[i], ourNotes))	//see if that ourNote is within the scale
            noteSet.add(harmPriority[i]);
      if(noteSet.size()==0)
         for(int i=0; i<harmRest.length; i++)
            if(isNoteInScale(harmRest[i], ourNotes))	//see if that ourNote is within the scale
               noteSet.add(harmRest[i]);
      if(noteSet.size()==0)
      {
         ArrayList allChords = getAllChordsThatHasNote(ourNote, chordSetsA);
      //int[]chordIndexes = getAllChordIndexesThatHasNote(ourNote, chords);
         for(int eachChord=0; eachChord<allChords.size(); eachChord++)
         {
            int[] chord = ((Chord)(allChords.get(eachChord))).getNotes();
            for(int eachNote=0; eachNote<chord.length; eachNote++)
               if(isNoteInScale(chord[eachNote], ourNotes))	//see if that ourNote is within the scale
                  noteSet.add(chord[eachNote]);
         }
      }
      int[]theNotes = new int[noteSet.size()];
      int i=0;
      for(Integer n:noteSet)
         theNotes[i++]=n.intValue();
      selSort(theNotes);
      return theNotes;
   }

//if higher is true, returns the next higher value in notes as compared to note
//if higher is false, returns the next lower value in notes as compared to note
//ie, 	getNextClosestNote(63, [52, 58, 63, 67, 70], true) returns 67
//		   getNextClosestNote(63, [52, 58, 63, 67, 70], false) returns 58
   public static int getNextClosestNote(int ourNote, int[]notes, boolean higher)
   {
      selSort(notes);
      if(notes != null && ourNote > 0)
      {
         for(int i=0; i<notes.length; i++)
         {
            if(higher==true)
            {
               if(i==0)
               {
                  if(ourNote <= notes[i])					//ourNote <= everything in notes
                     return notes[i];
               }
               else   
                  if(i==notes.length-1)
                  {
                     if(ourNote >= notes[i])	//ourNote > everything in notes
                        return ourNote;
                  }
                  else 
                     if(i>0)
                     {
                        if(ourNote==notes[i])
                           return notes[i+1];
                        if(ourNote > notes[i-1] && ourNote < notes[i])
                           return notes[i];
                     }
            }
            else	//if(higher==false)
            {
               if(i==0)
               {
                  if(ourNote <= notes[i])					//ourNote <= everything in notes
                     return ourNote;
               }
               else   
                  if(i==notes.length-1)
                  {
                     if(ourNote >= notes[i])				//ourNote > everything in notes
                        return notes[i];
                  }
                  else
                     if(i<notes.length-1)
                     {
                        if(ourNote==notes[i])
                           return notes[i-1];
                        if(notes[i] < ourNote && notes[i+1] >= ourNote)
                           return notes[i];
                     }
            }
         
         }
      }
      return ourNote;
   }

//returns the closest note to 'target' between noteA and noteB
   public static int getClosestBetween(int noteA, int noteB, int target)
   {
      if(Math.abs(normalize(noteA)- normalize(target)) < Math.abs(normalize(noteB) - normalize(target)))
         return noteA;
      return noteB;
   }

//given a riff, returns an array of the index interval jumps from the first note
//an array value of -1 denotes a rest
   public static int[] getIndexIntervalsInRiff(int[] riffNotes, boolean justFirstHalf)
   {
      int[] retVal = null;
      if (justFirstHalf == false)
         retVal =new int[riffNotes.length];
      else
         retVal =new int[riffNotes.length/2];
      for(int i=0; i<retVal.length; i++)
      {
         if(riffNotes[i] > 0)
            retVal[i] = indexOfNote(riffNotes[i], scale);
         else
            retVal[i] = -1;		//rest
      }
      return retVal;
   }

//lets us know how many notes are in the scale - i.e., 5 (pentatonic), 7 (normal), 12 (chormatic)
   public static int numNotesInScale()
   {
      int count=1;
      int firstNote = scale[0];
      for(int i=1; i<scale.length; i++)
      {
         if(normalize(firstNote) == normalize(scale[i]))
            break;
         count++;
      }
      return count;
   }

//returns whether or not our scale has a dominant (V) and subdominant (IV)
   public static boolean scaleHasIVandV()
   {
      return (scaleHasIV() && scaleHasV());
   }

//returns whether or not our scale has a subdominant (IV)
   public static boolean scaleHasIV()
   {
      boolean hasIV = false;
      for(int i=0; i<scale.length; i++)
      {
         if(isFourth(scale[i],scale))
            hasIV = true;        
      }
      return (hasIV);
   }

//returns whether or not our scale has a dominant (V) 
   public static boolean scaleHasV()
   {
      boolean hasV = false;
      for(int i=0; i<scale.length; i++)
      {
         if(isFifth(scale[i],scale))
            hasV = true;
      }
      return (hasV);
   }

//given a String, returns true if it is comprised of only digits
   public static boolean isNumber(String word)
   {
      for(int i=0; i<word.length(); i++)
         if(!Character.isDigit(word.charAt(i)))
            return false;
      return true;
   }

//returns true if that 'note' is an element of that 'scale'
   public static boolean isNoteInScale(int ourNote, int[] notes)
   {
      for(int i=0; i<notes.length; i++)
         if(normalize(ourNote) == normalize(notes[i]))
            return true;
      return false;
   }

//returns true if the sent scale contains a tritone (aug 4 or dim 5) which is half an octave
   public static boolean isTritoneInScale(int[] scale)
   {
      int[] normalizedScale = new int[12];
      for(int i=0; i<normalizedScale.length; i++)
         normalizedScale[i] = normalize(scale[i]);
      if(search(normalizedScale, 6))
         return true;   
      return false;
   }

//whichBeat is 4,2 or 1
   public static boolean isOnBeat(int firstTracking, int where, int whichBeat)
   {
      if ((where - firstTracking) <= 0)
         return true;
      return ((where - firstTracking) % (wholeNote/whichBeat) == 0);
   }

//returns whether or not the note is in the range of a piano
   public static boolean isNoteInRange(int ourNote)
   {
      return (ourNote>=22 && ourNote<=108);
   }

//returns true if the 'note' happens to be the root of the scale	
   public static boolean isRoot(int ourNote, int[] scale)
   {
      return (normalize(scale[0]) == normalize(ourNote));
   }

//returns true if the 'note' is the min 2nd of the scale
   public static boolean isMin2(int ourNote, int[] scale)
   {
      int sec = scale[0] + 1;
      return (normalize(sec) == normalize(ourNote));
   }

//returns true if the 'note' is the major 2nd of the scale
   public static boolean isMaj2(int ourNote, int[] scale)
   {
      int sec = scale[0] + 2;
      return (normalize(sec) == normalize(ourNote));
   }

//returns true if the 'note' is the min third of the scale
   public static boolean isMin3(int ourNote, int[] scale)
   {
      int third = scale[0] + 3;
      return (normalize(third) == normalize(ourNote));
   }

//returns true if the 'note' is the major third of the scale
   public static boolean isMaj3(int ourNote, int[] scale)
   {
      int third = scale[0] + 4;
      return (normalize(third) == normalize(ourNote));
   }

//returns true if the 'note' is the fourth (subdominant) of the scale
   public static boolean isFourth(int ourNote, int[] scale)
   {
      int fourth = scale[0] + 5;
      return (normalize(fourth) == normalize(ourNote));
   }

//returns true if note is half of an octave (the tritone)
   public static boolean isTritone(int ourNote, int[] scale)
   {
      int tri = scale[0] + 6;
      return (normalize(tri) == normalize(ourNote));
   }

//returns true if the 'note' is the fifth (dominant) of the scale
   public static boolean isFifth(int ourNote, int[] scale)
   {
      int fifth = scale[0] + 7;
      return (normalize(fifth) == normalize(ourNote));
   }

//returns true if the 'note' is the min 6th of the scale
   public static boolean isMin6(int ourNote, int[] scale)
   {
      int sixth = scale[0] + 8;
      return (normalize(sixth) == normalize(ourNote));
   }

//returns true if the 'note' is the major 6th of the scale
   public static boolean isMaj6(int ourNote, int[] scale)
   {
      int sixth = scale[0] + 9;
      return (normalize(sixth) == normalize(ourNote));
   }

//returns true if the 'note' is the min 7th of the scale
   public static boolean isMin7(int ourNote, int[] scale)
   {
      int sev = scale[0] + 10;
      return (normalize(sev) == normalize(ourNote));
   }

//returns true if the 'note' is the major 7th of the scale
   public static boolean isMaj7(int ourNote, int[] scale)
   {
      int sev = scale[0] + 11;
      return (normalize(sev) == normalize(ourNote));
   }
    
//returns a chord if a chord has been played within a 16th note of the sent tracking time - null otherwise
   private static int[] chordHasBeenPlayed(int where, Map<Integer, int[]> chordPlayed)
   {
      Set<Integer> trackingValues = chordPlayed.keySet();
      for(Integer x:trackingValues)
         if(Math.abs(x - where) <= wholeNote/16)
            return chordPlayed.get(where);
      return null;
   } 

//returns the note that has been played at the sent tracking value, 0 if no note was played there
   private static int noteHasBeenPlayed(int where, Map<Integer, Integer> alreadyPlayed)
   {
      Set<Integer> trackingValues = alreadyPlayed.keySet();
      for(Integer x:trackingValues)
      {
         Integer noteThatsThere = alreadyPlayed.get(x);
         if(x == where && noteThatsThere > 0)
            return noteThatsThere;
      }
      return 0;
   }

//returns if the note at that tracking time on that chanel has been played
   private static boolean noteHasBeenPlayed(int ourNote, int where, int chnl, Map<Integer, int[]> allPlayed)
   {
      Set<Integer> trackingValues = allPlayed.keySet();
      for(Integer x:trackingValues)
      {
         int[] noteInfo = allPlayed.get(x);
         int notePlayed = noteInfo[0];
         int chanelPlayed = noteInfo[1];
         if(x == where && notePlayed == ourNote && chanelPlayed == chnl)
            return true;
      }
      return false;
   }

//send a note and a chord and it returns the next closest note in the first range notes of the chord
//if range <=0, then it uses the full range of the chord	
   private static int closestNoteInChord(int tryNote, int []chord, int range)
   {
      if(tryNote <= 0 || chord==null || chord.length == 0)
         return 0;
      if(range <=0)
         range = chord.length;
      int difference=0;
      int minDiff = 99;
      for(int i=0; i<range; i++)
      {
         difference = Math.abs(normalize(tryNote) - normalize(chord[i]));
         if(difference < minDiff)
            minDiff = difference;
      }
      for(int i=0; i<range; i++)
      {
         if (normalize(tryNote+minDiff) == normalize(chord[i]))
            return tryNote + minDiff;
         if (normalize(tryNote-minDiff) == normalize(chord[i]))
            return tryNote - minDiff;
      }
      return forceNoteInRange(tryNote, scale);
   }

//send a note and a chord and it returns the next closest note in the first range notes of the chord
//if range <=0, then it uses the full range of the chord
   private static int closestNoteInChord(int tryNote, Chord myChord, int range)
   {
      if(tryNote <= 0 || myChord == null)
         return 0;
      int[] chord = myChord.getNotes();
      if(range <=0)
         range = chord.length;
      int difference=0;
      int minDiff = 99;
      for(int i=0; i<range; i++)
      {
         difference = Math.abs(normalize(tryNote) - normalize(chord[i]));
         if(difference < minDiff)
            minDiff = difference;
      }
      for(int i=0; i<range; i++)
      {
         if (normalize(tryNote+minDiff) == normalize(chord[i]))
            return tryNote + minDiff;
         if (normalize(tryNote-minDiff) == normalize(chord[i]))
            return tryNote - minDiff;
      }
   
      return forceNoteInRange(tryNote, scale);
   }

   private static int[] chordInversion(int[]chord)
   {
      if(chord.length < 3)		//if our chord is too small to do an inversion,
      {             				//make it a 3 note chord with some octaves, son...
         int[]octaves = new int[3];
         octaves[0] = chord[0];
         if(chord.length == 1)
            octaves[1] = octaves[0] + OCTAVE;
         else
            octaves[1] = chord[1];
         octaves[2] = octaves[1] + OCTAVE;
         octaves = forceChordInRange(octaves);
         return octaves;
      }
      int[] newChord = new int[chord.length];
      if(rand.nextDouble()<.5)//1st inversion
      {
         for(int i=1; i<chord.length; i++)					//copy the 2nd->last notes in the new chord
            newChord[i-1] = chord[i];
         newChord[newChord.length-1] = chord[0]+OCTAVE;	//add the 1st note to the end + an octave
      }
      else							//2nd inversion
      {
         for(int i=2; i<chord.length; i++)					//copy the 3rd->last notes in the new chord
            newChord[i-2] = chord[i];
         newChord[newChord.length-2] = chord[0]+OCTAVE;	//add the 1st & 2nd notes to the end + octave
         newChord[newChord.length-1] = chord[1]+OCTAVE;
      }
      return forceChordInRange(newChord);
   }

//writes a sound of pitch 'note', durartion 'noteLength', volume 'velocity' at location 'where' in chanel 'chnl' in the Track 'music'
//uses the Map 'alreadyPlayed' to make sure that it dosn't put a note where there already is one - updates 'alreadyPlayed' if a note is played
//returns the updated tracking position - doesn't add a sound if 'note' is <= 0 but places a rest
   private static int playNote(int note, int noteLength, int myVelocity, int where, int chnl, Map<Integer, Integer> alreadyPlayed, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {	//only place a note if the one sent is valid and a note hasn't already been played there
      if(note!=0 && !isNoteInRange(note))
      {
         System.out.println("bad note "+note+" at tracking position "+where);
         return where;
      }
      if(noteLength < 0)
         noteLength = Math.abs(noteLength);
      if(note>0 && noteHasBeenPlayed(where, alreadyPlayed)==0 && !noteHasBeenPlayed(note, where, chnl, allPlayed))
      {	
         int ourNote = forceNoteInRange(note, scale);
      
         int[]ourChord = chordHasBeenPlayed(where, chordPlayed);			//if we played a chord at this tracking time
         if(ourChord!=null)																//and our note is not in the chord 
         {																						//then force it to be a note in the chord
            if(ourChord.length > 1 && !noteInChord(ourNote, ourChord, ourChord.length)) 	
               ourNote = closestNoteInChord(note, ourChord, ourChord.length);	
            else																				//we have a rolled chord note here
               if (ourChord.length == 1 && !noteInChord(ourNote, ourChord, ourChord.length))
               {
                  int[]harmNotes = getHarmonizeNotes(ourNote, scale);
                  ourNote = closestNoteInChord(ourNote, harmNotes, harmNotes.length);	
               }   	
         }
         ShortMessage on = new ShortMessage();
         ShortMessage off = new ShortMessage();
         on.setMessage(ShortMessage.NOTE_ON, chnl, ourNote, myVelocity);
         off.setMessage(ShortMessage.NOTE_OFF,chnl, ourNote, myVelocity);   
         music.add(new MidiEvent(on, where));
         music.add(new MidiEvent(off, where+noteLength));
      
         alreadyPlayed.put(where, ourNote);
         int[]noteInfo = new int[2];
         noteInfo[0] = ourNote;
         noteInfo[1] = chnl;
         allPlayed.put(where, noteInfo);
      }
      return where+noteLength;
   }

//writes a sound of pitch 'note', durartion 'noteLength', volume 'velocity' at location 'where' in chanel 'chnl' in the Track 'music'
//returns the updated tracking position - doesn't add a sound if 'note' is <= 0 but places a rest
   private static int playNote(int note, int noteLength, int myVelocity, int where, int chnl, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {	//only place a note if the one sent is valid
      if(note!=0 && !isNoteInRange(note))
      {
         System.out.println("bad note "+note+" at tracking position "+where);
         return where;
      }
      if(noteLength < 0)
         noteLength = Math.abs(noteLength);
      if(note>0)
      {
         int ourNote = forceNoteInRange(note, scale);
         ShortMessage on = new ShortMessage();
         ShortMessage off = new ShortMessage();
         on.setMessage(ShortMessage.NOTE_ON, chnl, ourNote, myVelocity);
         off.setMessage(ShortMessage.NOTE_OFF,chnl, ourNote, myVelocity);   
         music.add(new MidiEvent(on, where));
         music.add(new MidiEvent(off, where+noteLength));
      
         int[]noteInfo = new int[2];
         noteInfo[0] = ourNote;
         noteInfo[1] = chnl;
         allPlayed.put(where, noteInfo);
      }  
      return where+noteLength;
   }

//plays a chord containing a specific note at position 'where' (but doesn't alter tracking) if forceChordIndex is -1
//will force the duration of the chord to forceChordLength, but will pick a random duration if forceChordLength is -1
//if note is 0, it will pick a random chord.  If forceChordIndex>=0, it will use that specific chord
//if forceArp is true, it will arpeggiate the chord rather than strike all the notes at once
//octave==0 will add the chord normally.  Negative octave will drop it.  Positive octave will raise it so many steps.
   public static int playChord(int chnl, Chord chordObject, int[] forceChord, int note, int where, int forceChordGroup, int forceChordIndex, int forceChordLength, int forceVelocity, double forceArp, int octave, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int ourVelocity = velocity;
      if(forceVelocity > 0)
         ourVelocity = forceVelocity;
      if(chordHasBeenPlayed(where, chordPlayed)!=null)	//if a chord has already been played there, stop
         return where;
   
      boolean rollChord = false;
      int arpChord = 0;				//0-strike chord all notes at once, 1-roll chord and sustain notes
      int chordlength = wholeNote;
           
      if(forceChordLength!= -1)
         chordlength = forceChordLength;
      else
      {
         double pickChordLength = rand.nextDouble();
         if(pickChordLength<.10)           	//stacatto chord 10% of the time
            chordlength = wholeNote/16;
         else
            if(pickChordLength<.50)				//then, either 40% of the time a whole-note chord
               chordlength = wholeNote;
            else
               if(pickChordLength<.75)			//25% of the time a half-note chord
                  chordlength = wholeNote/2;
               else
                  chordlength = wholeNote/4;	//25% a quarter note length chords
      }
      //n% of the time with stacatto chords or 1/4 note chords, roll them instead of striking the notes at one time
      double playWithFlair = rand.nextDouble();			//will we play a fast rolled arpeggio or start the sustained chord with a roll
      if(playWithFlair < flair && chordlength <=wholeNote/4)	//only arpeggiate chord with quarter notes or less
         rollChord=!rollChord;
      else
         if(playWithFlair < flair || rand.nextDouble() < forceArp) 					//for longer chords, if we are playing with flair this time
            arpChord = 1;           
      ArrayList[] chordSet;
      if (rand.nextDouble() < altChords && forceChordGroup == -1)						//if the chord has an optional addition like a 9th
         chordSet = chordSetsB;
      else
         chordSet = chordSetsA;
      int [] chord = null;
      Chord theChord = null;
      if(forceChord != null)
      {
         chord = forceChord;
      //find which Chord object is the same as chord and set it to theChord
         if(chordObject != null)
            theChord = chordObject;
      }   
      else
         if(forceChordGroup>=0 && forceChordGroup< chordSet.length && forceChordIndex >= 0 && forceChordIndex < chordSet[forceChordGroup].size())
         {
            ArrayList ourGroup =  chordSet[forceChordGroup];
            theChord = ((Chord)(ourGroup.get(forceChordIndex)));
            chord = theChord.getNotes();
         }
         else
            if(note==0 &&  rand.nextDouble()<chordBusyNess)				//rest - pick a random chord here
            {
               int[] chInfo =  getRandChordInfo(chordSet);
               ArrayList ourChordSet = chordSet[chInfo[0]];
               theChord = ((Chord)(ourChordSet.get(chInfo[1])));
               chord = theChord.getNotes();
            }
            else
               if(forceChordGroup>=0 && forceChordGroup< chordSet.length && forceChordIndex < 0)
               {		//we are trying to force the chord group but not the specific chord, so pick a random one
                  ArrayList ourChordSet = chordSet[forceChordGroup];				//so pick a random chord from that particular group
                  int chordIndex = getRandChordInfo(chordSet, forceChordGroup); 
                  theChord = ((Chord)(ourChordSet.get(chordIndex)));
                  chord = theChord.getNotes();
               }
               else			//pick a random chord that has that note in it
                  if(note > 0 && noteInChord(note, scale, scale.length))
                  {			//make sure the note is in the scale (unlike a tritone)
                     theChord = getChordThatHasNote(note, chordSet);
                     if(theChord != null)
                        chord = theChord.getNotes();  
                  }	
                  else		
                     if(chordObject != null)
                     {
                        theChord = chordObject;
                        chord = theChord.getNotes();
                     }
                     else		//something went wrong
                        return where;
      if(theChord != null)
      { 							//write chord name into MIDI file              
         String text = theChord.getName();
         addEvent(music, TEXT, text.getBytes(), where);
      }       
      chordPlayed.put(where, chord);	//we are playing a chord, so add it to the Map of chords that have been played   
      if(rand.nextDouble() < limitChord && !rollChord)  
         chord = limitChord(chord, (int)(rand.nextDouble()*3)+1);                   
      if (rand.nextDouble() < doChordInversions)		//do a chord inversion
         chord = 	chordInversion(chord);
      chord = forceChordInRange(chord);		
      //if we will roll a chord, pick a random note from that chord (dropped an octave) to hold as the chord is rolled
      //System.out.println("Picked chord "+chordIndex+" of "+chords.length);	
      int lowChordNote =  chord[(int)(rand.nextDouble()*chord.length)]-12;
      if (lowChordNote < 0)
         lowChordNote =  chord[(int)(rand.nextDouble()*chord.length)];
      lowChordNote = forceNoteInRange(lowChordNote, scale);
      if(rollChord)												//play bass note
         playNote(lowChordNote, chordlength, ourVelocity, where, chnl, music);  
      
      int[] chordNoteOrder = new int[chord.length];	//the order in which we play the notes in an arpeggiated or rolled chord
      for(int y=0; y< chordNoteOrder.length; y++)
         chordNoteOrder[y] = y;								//add each index of the chord we will play
      if(!rollChord && arpChord==1 && rand.nextDouble() < .5) 
      { 
         int whichOrder = (int)(rand.nextDouble()*4);
         switch (whichOrder)
         {
            case 0:	scramble(chordNoteOrder);			//play an arpeggiated chord in any note order
               break;
            case 1:	chordNoteOrder = shuffle(chordNoteOrder);
               break;
            case 2:	chordNoteOrder = reverse(chordNoteOrder);	
         } 
      }
      else
         if(rollChord && rand.nextDouble() < .5)		//roll chord from high to low
            chordNoteOrder = reverse(chordNoteOrder);
      int orderIndex = 0;							//index of the chord note indexes to play
      for(int y = 0; y<chord.length; y++)		//4 notes of the chord 
      {    
         if (!rollChord && arpChord==1)
         {
            int[]temp = new int[1];		//because adding to the chordPlayed map requires an array
         									//and we are sending it our single chord note (arpeggiated)
            int ourIndex = chordNoteOrder[orderIndex++];
            temp[0] = forceNoteInRange(chord[ourIndex]+OCTAVE*octave, scale);
            chordPlayed.put(where+(y*rollDelay), temp);			//we are playing a chord, so add it to the Map of chords that have been played 
            playNote(temp[0], chordlength, ourVelocity, where+(y*rollDelay), chnl, music);
         }
         else
            if(rollChord && arpChord==0)
            {
               int ourIndex = chordNoteOrder[orderIndex++];
               int chordNote = forceNoteInRange(chord[ourIndex]+OCTAVE*octave, scale); 
               where = playNote(chordNote, chordlength, ourVelocity, where, chnl, music);  
            }
            else							//regular chord - play all notes at once
            {
               int chordNote = forceNoteInRange(chord[y]+OCTAVE*octave, scale);
               playNote(chordNote, chordlength, ourVelocity, where, chnl, music);  
            }
      } 
      if(rollChord && arpChord==0)	//advance where for rolled arpeggio so that it is its own time event
         return where; 
      int retValue = where + chordlength;   
      return retValue;
   }

 //plays 'chord' of duration 'chordLength' with volume 'myVelocity' at tracking time 'where' in chanel 'chnl'
   public static int playChord(Chord chordObject, int chordLength, int myVelocity, int where, int chnl, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {		
      if(chordObject==null)
         return where;
      if(chordHasBeenPlayed(where, chordPlayed)!=null)	//if a chord has already been played there, stop
         return where;
      int[] chord = chordObject.getNotes();
   //write chord name into MIDI file              
      String text = chordObject.getName();
      addEvent(music, TEXT, text.getBytes(), where);
   
      chordPlayed.put(where, chord);   						//add our chord to chordPlayed Map
      if(rand.nextDouble() < limitChord)  
         chord = limitChord(chord, (int)(rand.nextDouble()*3)+1);
      for(int i=0; i<chord.length; i++)						//play each note of the chord at tracking position 'where'
         playNote(chord[i], chordLength, myVelocity, where, chnl, music);
      return where + chordLength;								//return updated tracking position
   }

//pre:	group >=1 and group <=7
//returns the index of which chord group in a scale is a particualr value (I,ii,iii,IV,V,vi,vii)
   public static int chordGroupIndex(int group)
   {//isNoteInScale(int note, int[] notes)
      int semitones = 0;
      switch(group)
      {
         case 1:	semitones = 0;
            break;
         case 2:						//Major seconds are two semitones,
            if(isNoteInScale(scale[0]+2, scale))
               semitones = 2;
            else						//minor seconds are one semitone
               if(isNoteInScale(scale[0]+1, scale))
                  semitones = 1;
               else					//there is no second group
                  return -1;
            break;
         case 3:						//Major thirds are four semitones, 
            if(isNoteInScale(scale[0]+4, scale))
               semitones = 4;
            else						//minor thirds are three semitones
               if(isNoteInScale(scale[0]+3, scale))
                  semitones = 3;
               else					//there is no third group
                  return -1;
         
            break;
         case 4:						//A perfect fourth is five semitones
            if(isNoteInScale(scale[0]+5, scale))
               semitones = 5;
            else						//there is no third group
               return -1;
         
            break;
         case 5:						//A perfect fifth is seven semitones
            if(isNoteInScale(scale[0]+7, scale))
               semitones = 7;
            else						//there is no third group
               return -1;
            break;
         case 6:						//Major sixths are nine semitones,
            if(isNoteInScale(scale[0]+9, scale))
               semitones = 9;
            else						//minor sixths are eight semitones
               if(isNoteInScale(scale[0]+8, scale))
                  semitones = 8;
               else					//there is no third group
                  return -1;
         
            break;
         case 7:						//Major sevenths are eleven semitones, 
            if(isNoteInScale(scale[0]+11, scale))
               semitones = 11;
            else						//minor sevenths are ten semitones
               if(isNoteInScale(scale[0]+10, scale))
                  semitones = 10;
               else					//there is no third group
                  return -1;
            break;
         default:	
            return -1;
      }
      int ourNote = scale[0] + semitones;
      for(int i=0; i<chordSetsA.length; i++)
      {
         ArrayList chordSet = chordSetsA[i];
         for(int j=0; j<chordSet.size(); j++)
         {
            int[] ourChord = ((Chord)(chordSet.get(0))).getNotes();
            if(normalize(ourChord[0])==normalize(ourNote))
               return i;
         }
      }
      return -1;
   }
  
//returns a subchord of the sent chord
//limitType of 0 is the original chord
//limitType of 1 is the first three notes
//limitType of 2 is the set of three notes starting at the second note
//limitType of 3 is the last three notes
//otherwise, reutrns the same chord
   public static int[] limitChord(int [] ourChord, int limitType)
   {
      if (limitType < 1 || limitType > 3 || ourChord.length <=3 )	//a 3 note chord is already limited
         return ourChord;
      int[]chord = new int[3];
      int start=0, end=3;
      if(ourChord.length==4)								//a 4 note chord can only have limitType 1 or 2
         if(limitType == 3)
            if(rand.nextDouble()<.5)
               limitType=2;
            else
               limitType=1;
      if(limitType == 1)
         end = 3;
      else
         if(limitType == 2)
         {
            start=1;
            end=4;
         }
         else
            if(limitType == 3 && ourChord.length>=6)
            {
               start = 2;
               end = 5;       
            }
      int index = 0;
      for(int i=start; i<end; i++)
         chord[index++] = ourChord[i];
      return chord;
   }

//returns a new set of melody Durations and notes that has a total duration time of 'total'
//clips the array if it is too long, adds to it if it is too short
   public static ArrayList<int[]> equalizeDurations(int[] melodyDurations, int[] melodyNotes, int total)
   {
      ArrayList<Integer>myDurations = new ArrayList();
      ArrayList<Integer>myNotes = new ArrayList();
      if(melodyDurations.length == 1)
      {  
         melodyDurations[0] = total;
         ArrayList<int[]> result = new ArrayList();
         result.add(melodyDurations);
         result.add(melodyNotes);	
         return result;
      }
      int sum = 0;
      int index = 0;
      while (sum < total && index  < melodyDurations.length)
      {
         if(sum + melodyDurations[index] >= total && myDurations.size()>=1)
         {
            int lastIndex = myDurations.size()-1;
            int whatWeAdd = (myDurations.get(lastIndex) + (total - sum));
            myDurations.set(lastIndex, whatWeAdd);
            sum += whatWeAdd;
            myNotes.add(melodyNotes[index++]);
         }
         else
         {
            myNotes.add(melodyNotes[index]);
            int whatWeAdd = melodyDurations[index++];
            myDurations.add(whatWeAdd);
            sum += whatWeAdd;   
         }
      }
      if (sum < total)
      {  
         int[] fillIn =  makeRiffDurations(total-sum, false, -1, .25, -1);
         int prevNote = -1;									//last note of the melody, which we are now going to add on to
         if(myNotes!=null && myNotes.size() > 0)		
            prevNote = myNotes.get(myNotes.size()-1);
         ArrayList<int[]> melodySets = makeMelodyNotes(fillIn, -1, -1, -1, prevNote);
         int[] newNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));	//the last index is excluded because it is durations (in case they change)
         for(int i=0; i<fillIn.length; i++)
         {
            myDurations.add(fillIn[i]);
            myNotes.add(newNotes[i]);
         }
      }
      index = 0;
      int[]myDur = new int[myDurations.size()];
      int[]myNewNotes = new int[myDurations.size()];
   
      for(int i=0; i < myDurations.size(); i++)
      {
         myNewNotes[index] = myNotes.get(i);
         myDur[index++] = myDurations.get(i);
      }	
      ArrayList<int[]> result = new ArrayList();
      result.add(myDur);
      result.add(myNewNotes);	
      return result;
   }

//returns a new set of melody Durations that has a total duration time of 'total'
//clips the array if it is too long, adds to it if it is too short
   public static int[] equalizeDurations(int[] melodyDurations, int total)
   {
      ArrayList<Integer>myDurations = new ArrayList();
      int sum = 0;
      int index = 0;
      while (sum < total && index  < melodyDurations.length)
      {
         if(sum + melodyDurations[index] >= total && myDurations.size()>=1)
         {
            int lastIndex = myDurations.size()-1;
            int whatWeAdd = (myDurations.get(lastIndex) + (total - sum));
            myDurations.set(lastIndex, whatWeAdd);
            sum += whatWeAdd;
         }
         else
         {
            int whatWeAdd = melodyDurations[index++];
            myDurations.add(whatWeAdd);
            sum += whatWeAdd;   
         }
      }
      if (sum < total)
      {  
         int[] fillIn =  makeRiffDurations(total-sum, false, -1, .25, -1);
         ArrayList<int[]> melodySets = makeMelodyNotes(fillIn, -1, -1, -1, -1);
         int[] newNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));
         for(int i=0; i<fillIn.length; i++)
            myDurations.add(fillIn[i]);
      }
      index = 0;
      int[]myDur = new int[myDurations.size()];  
      for(int i=0; i < myDurations.size(); i++)
         myDur[index++] = myDurations.get(i);
      return myDur;
   }


//given a tracking value and an interval, returns the next closest interval after the tracking value	
   public static int nextMultOfInterval(int where, int interval)
   {
      return where + (interval-(where%interval));
   }

//returns an array of durations that has values that are twice as long
   public static int[] doubleDurations(int[] durations)
   {
      for(int i=0; i<durations.length; i++)
         durations[i] *= 2;
      return durations;
   }

//returns an array twice the size with two copies of the original back to back
//if isItNotes is false, the copy is the same as the original
//if isItNotes is true, there is no repeat notes and the second half is an octave higher
   public static int[] doubleArray(int[]array, boolean isItNotes)
   {
      int[]ans=null;
      if(!isItNotes)
      {
         ans = new int[array.length*2];
         int index=0;
         for(int times=0; times<2; times++)
            for(int i=0; i<array.length; i++)
               ans[index++] = array[i];
      }
      else
      {
         ans=new int[array.length*2 - 1];	
         int index=0;	
         for(int i=0; i<array.length; i++)
            ans[index++] = array[i];
         for(int i=1; i<array.length; i++)
            ans[index++] = array[i]+OCTAVE;
      }
      return ans;
   }

//returns an array of all elements of a and b
   public static int[] append(int[]a, int[]b)
   {
      int[]retVal = new int[a.length + b.length];
      int index = 0;
      for(int i:a)
         retVal[index++] = i;
      for(int i:b)
         retVal[index++] = i;
      return retVal;
   }

//given an array of durations, looks for two rests (negative values) in a row, and makes one of the two positive
   public static void removeDoubleRests(int[]durations)
   {
      for(int i=0; i<durations.length-1; i++)
      {
         if(durations[i]<0 && durations[i+1]<0)	//side by side rests
         {	//pick one of the two rests to become non-rests
            if(rand.nextDouble() < .5)
               durations[i] *= -1;
            else
               durations[i+1] *= -1;
            i--;   
         }
      }
   }

//returns whether or not an array has the same second half as the first
   public static boolean hasBeenDoubled(int[] array)
   {
      if(array.length%2 != 0)
         return false;
      int j=array.length/2;
      for(int i=0; i<array.length/2; i++)
         if(array[i] != array[j++])
            return false;
      return true;
   }

//returns the # if interval steps between two notes in a scale
//a positive means that note1 > note2.  A negative means that note1 < note2
//returns -999 if one or both notes are not in the scale
   public static int difference (int note1, int note2)
   {
      int index1=indexOfNote(note1, scale);
      int index2=indexOfNote(note2, scale);
      if(index1>=0 && index2>=0)
         return index1-index2;
      return -999;
   }

//returns the array of scale intervals given an array of notes
//where the first note has a value of 0 and each successive note is
//the number of steps in the scale away from the first note
//since 0 denotes the first note, -999 denotes a rest
   public static int[] intervalArray (int[] notes)
   {
      int[] retVal = new int[notes.length];
      int firstNonRest = notes[0];
      for(int i=0; i<notes.length; i++)
         if(notes[i] > 0)
         {
            firstNonRest = notes[i];
            break;
         }
      for(int i=0; i<notes.length; i++)
         if(notes[i] > 0)
            retVal[i] = difference(notes[i],firstNonRest);
         else
            retVal[i] = -999;		//rest   
      return retVal;
   }

//will return an array of notes of the same interval as those sent
//but starting up or down a number of steps as sent by 'jump'
   public static int[] transposeInScale(int[] notes, int jump)
   {
      int[] retVal = new int[notes.length];
      for(int i=0; i < retVal.length; i++)
      {
         if(notes[i] > 0)
         {
            int index = indexOfNote(notes[i], scale);
            while(index + jump < 0)
               jump += intervalsToOctave(scale);
            if(index + jump > scale.length)
               jump -= intervalsToOctave(scale); 
            if(index+jump >=0 && index+jump < scale.length)   
               retVal[i] = scale[index + jump];
            else
               retVal[i] = scale[index];
         }
         else
            retVal[i] = 0;
      }
      return forceChordInRange(retVal);
   }

//returns the number of intervals (array indexes) in one octave in 'myScale'
   public static int intervalsToOctave(int[] myScale)
   {
      int count = 1;
      while(normalize(myScale[0]) != normalize(myScale[count]))
         count++;
      return count;
   }

//shift a riff up or down so many octaves such that it is in a piano's range
   public static int[] riffOctave(int[]riffNotes, int octave)
   {
      int[] riffShift = new int[riffNotes.length];
      for(int i=0; i < riffNotes.length; i++)
      {
         if (riffNotes[i] > 0)			//if our note is not a rest, add an octave of our note
            riffShift[i] = riffNotes[i] + (OCTAVE*octave);
         else
            riffShift[i] = 0;				//if our note is a rest, add a rest
      }
      return forceChordInRange(riffShift);
   }

//shift a riff up or down so many octaves
   public static int[] riffOctaveSimple(int[]riffNotes, int octave)
   {
      int[] riffShift = new int[riffNotes.length];
      for(int i=0; i < riffNotes.length; i++)
      {
         if (riffNotes[i] > 0)			//if our note is not a rest, add an octave of our note
            riffShift[i] = riffNotes[i] + (OCTAVE*octave);
         else
            riffShift[i] = 0;				//if our note is a rest, add a rest
      }
      return riffShift;
   }


//returns the chord group [element 0] and index within that group [element 1] of the 'chord' in that 'chordSet'
   public static int[] findChordPosition(Chord chord, ArrayList[]chordSets)
   {
      int[]info = {-1, -1};
      for(int i=0; i<chordSets.length; i++)
         for(int j=0; j<chordSets[i].size(); j++)
            if(chord.equals((Chord)(chordSets[i].get(j))))
            {
               info[0] = i;
               info[1] = j;
            }
      if(info[0] != -1 && info[1] != -1)      
         return info;
      return null;
   }

//expands a chord (1x,2x,3x) or of forceSize # of notes
   public static int[] expandChord(int[]chord, int forceSize)
   {
      int [] newChord;
      if (forceSize == -1)
      {
         int n = (int)(rand.nextDouble()*3) + 1;				//expand chord to 1x, 2x or 3x
         newChord = new int[chord.length*n];
      }
      else
         newChord = new int[forceSize];
      int index = 0;
      int octaveCount = 0;
      for(int i=0; i < newChord.length; i++)
      {
         if(index>=chord.length)
         {
            index = 0;
            octaveCount++;
         }
         newChord[i] = chord[index++] + (OCTAVE*octaveCount);
      }
      boolean octaveDrop = false;
      for(int i=0; i < newChord.length; i++)
         if(newChord[i] > 120)
            octaveDrop = true;
      if(octaveDrop)
         for(int i=0; i < newChord.length; i++)
            newChord[i] -= OCTAVE;
      return newChord;
   }

//returns the number of octaves we would have to add to get note into the same range as the center of the array notes
   public static int numOctavesToCenter(int noteA, int[] notes)
   {
      int num=0;
      int center = notes[notes.length/2];
      while (Math.abs(noteA-center) > OCTAVE/2)
      {
         noteA += OCTAVE;
         num++;
      }
      return num;
   }

//returns the number of octaves needed to get noteA in the same octave as noteB 
   public static int numOctavesToNote(int noteA, int noteB)
   {
      int num=0;
      while (Math.abs(noteA-noteB) > OCTAVE/2)
      {
         noteA += OCTAVE;
         num++;
      }
      return num;
   }


//returns noteA in the same octave range as noteB
   public static int getInSameOctave(int noteA, int noteB)
   {
      if(noteA == noteB)
         return noteA;
      if(noteA < noteB)
         while (Math.abs(noteA-noteB) > OCTAVE/2)
            noteA += OCTAVE;
      else
         while (Math.abs(noteA-noteB) > OCTAVE/2)
            noteA -= OCTAVE;
      return noteA;
   }

//returns chord where the lowest note is one that is closest to noteB and in the same octave range as noteB
   public static int[] getInSameOctave(int[] chord, int noteB)
   {
      int [] myChord = chord;
      for(int i=0; i<myChord.length; i++)
         myChord[i] = getInSameOctave(myChord[i], noteB);  
   
   //	int nextNote = closestNoteInChord(noteB, myChord, 0);	//this will be the first note of the chord we return
   
      return  forceChordInRange(myChord);
   }


//given two notes (at two intervals apart), returns the one inbetween them
   public static int noteInMiddle(int noteA, int noteB)
   {
      int noteAIndex = indexOfNote(noteA, scale);
      int noteBIndex = indexOfNote(noteB, scale);
      if(Math.abs(noteAIndex - noteBIndex) == 2)
      {
         int middleIndex = 0;
         if(noteAIndex < noteBIndex)
            middleIndex = noteAIndex + 1;
         else
            middleIndex = noteBIndex + 1;
         return getInSameOctave(scale[middleIndex], noteA);
      }
      return -1;
   }

//given an array, returns it almost doubled as a palindrome.
//i.e., given [1,2,3,4] returns [1,2,3,4,3,2,1]
   public static int[] makePalindrome(int[]array)
   {
      int[]ans=new int[array.length*2 - 1];
      int index=0;
      for(int i=0; i<array.length; i++)
         ans[index++] = array[i];
      for(int i=array.length-2; i>=0; i--)
         ans[index++] = array[i];   
      return ans;
   }

//will adjust a chord by adding or subtracting octaves until it is in an acceptable range
//a piano range is from 22(A0) to 108(C8)
   public static int[] forceChordInRange(int[] chord)
   {
      int[]newChord = chord.clone();
      boolean haveWeForcedUp = false;
      boolean haveWeForcedDown = false;
      boolean forceUp=false;
      boolean forceDown = false;
      do
      {
         forceUp=false;
         forceDown = false;
         for(int i=0; i<newChord.length; i++)
         {
            if(newChord[i] != 0)
            {
               if(newChord[i]<22)
                  forceUp=true;
               else
                  if(newChord[i] > 108)
                     forceDown = true;
            }
         }
         if(forceUp && forceDown)
            break;					//whoops!
         if(forceUp)
         {
            newChord = riffOctaveSimple(newChord, 1);
            haveWeForcedUp = true; 
         }
         else     
            if(forceDown)
            {
               newChord = riffOctaveSimple(newChord, -1);
               haveWeForcedDown = true;     
            }
         if(haveWeForcedUp && haveWeForcedDown)	//avoid ping-ponging back and forth - just adjust individual notes
            for(int i=0; i<newChord.length; i++)
               newChord[i] = forceNoteInRange(newChord[i], scale); 
      }
      while (forceUp || forceDown);
      return newChord;
   }

//returns a version of chord that is the highest possible octave in a pianos range
   public static int[] highestOctave(int[] chord)
   {
      int[] ourChord = chord;
      selSort(ourChord);
      while(isNoteInRange(ourChord[ourChord.length-1]))	//keep raising chord an octave until it is out of range
         ourChord = riffOctaveSimple(ourChord, 1);
      return forceChordInRange(ourChord);
   }

//returns a chord in an octave that is the same octave as the last note in lastChord
   public static int[] nextOctave(int[] chord, int[]lastChord)
   {
      int[] ourChord = lastChord;
      selSort(ourChord);
      ourChord = getInSameOctave(chord, ourChord[0]);
      return forceChordInRange(ourChord);
   }
//returns an arraylist where the first element is an int[] of counter melody durations
//and the second element is an int[] of counter melody notes
//can specify a startNote or an endNote, or values of -1 to not specify them
   public static ArrayList<int[]> makeCounterMelody(int[]melodyDuration, int startNote, int endNote, int timeSig)
   {
      int[]riffDuration2=null; 
      int[]melodyNotes2=null;
      int totalDuration = 0;			//total running time of the sent set of durations
      for(int i=0; i<melodyDuration.length; i++)
         totalDuration += Math.abs(melodyDuration[i]);
      double avgDuration = totalDuration / melodyDuration.length;  
      double longerNotes = .25;
      if (avgDuration < wholeNote/4 && rand.nextDouble() < .75)
         longerNotes = 0;				//if the melody that we are countering is comprised of shorter notes, make our counter melody usually have longer notes
      if(rand.nextDouble() < .5)		//counter-melody is built with same rules as a melody
      {										//50% of the time it will be a melody as opposed to a scale run
         riffDuration2 = makeRiffDurations(totalDuration, false, -1, longerNotes, timeSig);	//for counterMelody
      //melodyNotes2 = makeMelodyNotes(riffDuration2, startNote, endNote);
         ArrayList<int[]> melodySets = makeMelodyNotes(riffDuration2, -1, startNote, endNote, -1);
         melodyNotes2 = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));	//the last index is excluded because it is durations (in case they change)
      }
      else
      {
         int noteTime;					//counter-melody is an scale-run where all note durations are the same
         double pickNoteTime = rand.nextDouble();
         if (pickNoteTime < .33)
            noteTime = wholeNote/2;
         else
            if (pickNoteTime < .66)
               noteTime = wholeNote/4;				//50% of the time, 4th note for arp
            else
               noteTime = wholeNote/8;				//50% of the time, 8th note arp
         riffDuration2 = makeRiffDurations(totalDuration, false, noteTime, .25, timeSig);
         melodyNotes2 = makeScaleRun(riffDuration2.length, startNote, null);
      } 
      int octChange = 1;								//counter melody could be lowered 1 or raised 1
      if(rand.nextDouble() < .5)						//octChange will have the value -1 or 1
         octChange *= -1;
      melodyNotes2 =riffOctave(melodyNotes2, octChange);
      ArrayList<int[]>counterMelody = new ArrayList();
      counterMelody.add(riffDuration2);
      counterMelody.add(melodyNotes2);
      return counterMelody;
   }

//where is the starting tracking ticks of where the bass line will go
//howLong is the duration of the bass line in tracking ticks
//returns ArrayList of int[] where [index 0] are the bass notes and [index 1] are the durations
//limitStrat and limitExp are values for bassStrat and resolution that you don't want picked 
//they are sent as -1 if all values are ok
   public static ArrayList<int[]> makeBassLine(int where, int howLong, int limitStrat, int limitExp, boolean dottedDurations)
   {
      int bassStrat = (int)(rand.nextDouble()*6);	//bass strategy
   					//0-pick the same note as previous when possible
   					//1-pick oscilating octaves when possible
   					//2-ascend bass line if possible
   					//3-descend bass line if possible
   					//4-random notes that work
   					//5-oscilate between ascending and descending
      boolean oscilate = false;
      int oscilateOn = (int)(rand.nextDouble()*8)+2;	//for strat 5, this tells on which note we change from up to down or down to up
      if(bassStrat == 4 && rand.nextDouble() < .5)
         bassStrat = (int)(rand.nextDouble()*6);//less likely strategy 4		
      int exp = (int)(rand.nextDouble()*4);		//0,1,2,3 - exponents for picking the resolution of notes below
      if(exp <= 2 && rand.nextDouble() < .5)
         exp = (int)(rand.nextDouble()*4);		//but less likely 2 or 3 (8th note)
      if(limitStrat != -1 && limitExp != -1)
      {
         while (bassStrat == limitStrat && exp == limitExp)
         {													//pick new bassStrat and exp
            bassStrat = (int)(rand.nextDouble()*5);
            exp = (int)(rand.nextDouble()*4);
         }
      }
      else
         if(limitStrat != -1 && limitExp == -1)
            while (bassStrat == limitStrat)		//pick a new bassStrat
               bassStrat = (int)(rand.nextDouble()*6);
         else
            if(limitStrat == -1 && limitExp != -1)
               while (exp == limitExp)				//pick a new exp
                  exp = (int)(rand.nextDouble()*4);
      if(bassStrat == 5)								//5-oscilate between ascending and descending
      {
         oscilate = true;								//start either ascending (2) or descending (3)
         bassStrat = (int)(rand.nextDouble()*2)+2;		//2 or 3
      }   		              
      int resolution = (int)(Math.pow(2,exp));		//1-whole note, 2-half note, 4-quarter note, 8-eigth note
      int octaveDrop = ((int)(rand.nextDouble()*2)+1);	//drop bass 1 or 2 octaves
      if(rand.nextDouble() < .5 && octaveDrop == 2)
         octaveDrop = ((int)(rand.nextDouble()*2)+1);		//usually only 1 octave
      int bassDrop = OCTAVE * octaveDrop;
      boolean toggleOctave = false;						//for bassStrat 1
      int noteLength = (wholeNote/resolution);
      if (dottedDurations)
         noteLength += (noteLength/2);					//make durations dotted durations
      int resolutionNotes = howLong/noteLength;
      int [] noteTimes = new int[resolutionNotes];	//tracking ticks every 'resolution' increments
      for(int i=0; i<noteTimes.length; i++)
         noteTimes[i] = where + (noteLength*i);
      ArrayList<Integer> bassNotes = new ArrayList();
      ArrayList<Integer> bassDurations = new ArrayList();
      int prevNote = 0;
      int currNote = 0;
   //if higher is true, returns the next higher value in notes as compared to note
   //if higher is false, returns the next lower value in notes as compared to note
   //ie, 	getNextClosestNote(63, [52, 58, 63, 67, 70], true) returns 67
   //			 getNextClosestNote(63, [52, 58, 63, 67, 70], false) returns 58
   //public static int getNextClosestNote(int note, int[]notes, boolean higher)
   
   //int closestNoteInChord(int tryNote, int []chord, int range)
      for(int i=0; i<noteTimes.length; i++)
      {   
         if(oscilate && i % oscilateOn == 0)
         {
            if(bassStrat == 2)
               bassStrat = 3;
            else
               if(bassStrat == 3)
                  bassStrat = 2;
         }
         noteLength = (wholeNote/resolution);
         if (dottedDurations)
            noteLength += (noteLength/2);	//make durations dotted durations
         currNote = 0;
         if (i > 0)
            prevNote = bassNotes.get(i-1);
         int tick = noteTimes[i];
         int[] chord = chordHasBeenPlayed(tick, chordPlayed);
         int melNote = noteHasBeenPlayed(tick, melodyNotesPlayed);
         int harmNote = noteHasBeenPlayed(tick, harmonyNotesPlayed);
         int bassNote = noteHasBeenPlayed(tick, bassNotesPlayed);
         int nextBassNoteTick = -1;
         if (bassNote != 0)					//bass note already there, so add a rest
         {
            bassNotes.add(0);
            bassDurations.add(noteLength);
         }
         else //if(bassNote == 0)//no bass note there, so add one
         {		
            if(chord == null)		//these no chord there
            {	//find a chord if possible
               ArrayList chords = null;
               if(melNote!=0 && harmNote!=0)
               {
                  chords = getAllChordsThatHasNotes(melNote, harmNote, chordSetsA);
                  if (chords == null && rand.nextDouble() < altChords)
                     chords = getAllChordsThatHasNotes(melNote, harmNote, chordSetsB);
               }
               else if(melNote != 0 && harmNote == 0)	//we have a melNote and no harmNote	
               {
                  chords = getAllChordsThatHasNote(melNote, chordSetsA);
                  if (chords == null && rand.nextDouble() < altChords)
                     chords = getAllChordsThatHasNote(melNote, chordSetsB);  
               }
               else if(melNote == 0 && harmNote != 0)	//we have a harmNote and no melNote	
               {
                  chords = getAllChordsThatHasNote(harmNote, chordSetsA);
                  if (chords == null && rand.nextDouble() < altChords)
                     chords = getAllChordsThatHasNote(harmNote, chordSetsB);  
               }
               else if(melNote == 0 && harmNote == 0)	//no melNotes nor harmNotes
               {	//at this spot, there is no chord, melody note or harmony note
                  if(bassStrat <= 1)	//0-pick the same note as previous when possible   
                  {							//1-pick oscilating octaves when possible
                     if(prevNote != 0)
                        currNote = prevNote;
                     else
                        currNote = scale[(int)(rand.nextDouble()*scale.length)];
                     currNote = forceNoteInRange(currNote - bassDrop, scale);   
                     if(bassStrat == 1)
                     {
                        if(toggleOctave)
                           currNote += OCTAVE;
                        else
                           currNote -= OCTAVE;
                        toggleOctave = !toggleOctave;
                     }
                  }
                  else if(bassStrat == 2 || bassStrat == 3)	//ascend/descend bass line if possible
                  {
                     if(prevNote == 0)	//we have no previous bass note, so pick a random one
                     {
                        currNote = scale[(int)(rand.nextDouble()*scale.length)];
                        currNote = forceNoteInRange(currNote - bassDrop, scale);   
                     }
                     else //if(prevNote != 0)	//we have a previous note
                     {
                        int prevIndex = indexOfNote(prevNote, scale);
                        if(bassStrat == 2 && prevIndex + 1 < scale.length)
                        {						//get next highest note
                           currNote = scale[prevIndex+1];
                           currNote = forceNoteInRange(currNote, scale);
                        }
                        else if(bassStrat == 3 && prevIndex - 1 >= 0)
                        {						//get next lowest note
                           currNote = scale[prevIndex-1];
                           currNote = forceNoteInRange(currNote, scale);
                        }
                        else					//something went wrong - get random note
                        {
                           currNote = scale[(int)(rand.nextDouble()*scale.length)];
                           currNote = forceNoteInRange(currNote - bassDrop, scale);   
                        }
                     }
                  }
                  else //if(bassStrat == 4)	//random notes that work
                  {
                     currNote = scale[(int)(rand.nextDouble()*scale.length)];
                     currNote = forceNoteInRange(currNote - bassDrop, scale);                    
                  }
               }	
               if (chords == null)	//there are no chords
               {										
                  if(prevNote == 0)	//we have no previous bass note, so pick a random one
                  {
                     currNote = scale[(int)(rand.nextDouble()*scale.length)];
                     currNote = forceNoteInRange(currNote - bassDrop, scale);   
                  }						//we have a melNote and harmNote
                  else if(melNote!=0 && harmNote != 0)
                  {						//so pick the smarter between the two notes - OCTAVE
                     currNote = getClosestBetween(melNote, harmNote, prevNote);
                     currNote = forceNoteInRange(currNote - bassDrop, scale);
                     if(bassStrat == 1)
                     {
                        if(toggleOctave)
                           currNote += OCTAVE;
                        else
                           currNote -= OCTAVE;
                        toggleOctave = !toggleOctave;
                     }
                  } 
               } 
               else	//we have chords					
               {		//go through the notes in all the chords and pick the smartest one
                  Set<Integer> allChordNotes = new TreeSet();
                  for(int j=0; j<chords.size(); j++)
                  {
                     chord = ((Chord)chords.get(j)).getNotes();
                     for (int k=0; k<chord.length; k++)
                        allChordNotes.add(chord[k]);
                  }
                  chord = new int[allChordNotes.size()];
                  int index = 0;
                  for(Integer val:allChordNotes)
                     chord[index++] = val;
               }
            }
            if(chord != null && chord.length > 0)	//make the bass note a note in the chord 
            {													//such that it makes sense according to the note before it
               chord =  riffOctave(expandChord(chord, -1), -1);
               if(prevNote==0)		//1st bass note - pick a random from chordNotes
               {
                  currNote = chord[(int)(rand.nextDouble()*chord.length)];
                  currNote = forceNoteInRange(currNote - bassDrop, scale);
               }
               else if(bassStrat <= 1)	//0-pick the same note as previous when possible   
               {							//1-pick oscilating octaves when possible
                  if (noteInChord(prevNote, chord, chord.length))
                     currNote = prevNote;
                  else					//pick the next closest note as prevNote
                  {
                     if(rand.nextDouble() < .5)	//get next higher note
                     {
                        int oct = numOctavesToCenter(prevNote, chord);
                        int centeredNote = prevNote + (oct*OCTAVE);
                        currNote = getNextClosestNote(centeredNote, chord, true);
                        currNote -= (oct*OCTAVE);
                        currNote = forceNoteInRange(currNote, scale);
                     }
                     else								//get next lower note
                     {
                        int oct = numOctavesToCenter(prevNote, chord);
                        int centeredNote = prevNote + (oct*OCTAVE);
                        currNote = getNextClosestNote(centeredNote, chord, false);
                        currNote -= (oct*OCTAVE);
                        currNote = forceNoteInRange(currNote, scale);
                     }                         
                  }
                  if(bassStrat == 1)
                  {
                     if(toggleOctave)
                        currNote += OCTAVE;
                     else
                        currNote -= OCTAVE;
                     toggleOctave = !toggleOctave;
                  }
               
               }
               else if(bassStrat == 2)	//ascend bass line if possible
               {
                  int oct = numOctavesToCenter(prevNote, chord);
                  int centeredNote = prevNote + (oct*OCTAVE);
                  currNote = getNextClosestNote(centeredNote, chord, true);
                  currNote -= (oct*OCTAVE);
                  currNote = forceNoteInRange(currNote, scale);
               }
               else if(bassStrat == 3)	//descend bass line if possible
               {
                  int oct = numOctavesToCenter(prevNote, chord);
                  int centeredNote = prevNote + (oct*OCTAVE);
                  currNote = getNextClosestNote(centeredNote, chord, false);
                  currNote -= (oct*OCTAVE);
                  currNote = forceNoteInRange(currNote, scale);
               }
               else if(bassStrat == 4)	//random notes that work
               {
                  currNote = chord[(int)(rand.nextDouble()*chord.length)];
                  currNote = forceNoteInRange(currNote - bassDrop, scale);
               }
            }	
         }
         //look to see if another bass note starts soon after we added one
         for(int a=tick+1; a < tick+noteLength; a++)
         {
            int nextBassNote = noteHasBeenPlayed(a, bassNotesPlayed);
            if(nextBassNote != 0)
            {
               noteLength = a - tick;	//clip our bass note so that it doesnt run over the one that is already there
               break;
            }  
         }
         bassNotes.add(currNote);
         bassDurations.add(noteLength);
         if(noteLength != (wholeNote/resolution))
         {  //since we clipped our bass note so that it wouldnt run over one already there,
         //add a rest to fill out the rest of the time until the next bass note
            bassNotes.add(0);
            bassDurations.add((wholeNote/resolution)-noteLength);
         }
      }
      if(bassNotes == null || bassDurations == null)
         return null;  
      int [] notes = new int[bassNotes.size()];
      int index = 0;
      for(int i=0; i<notes.length; i++)
         notes[index++] = forceNoteInRange(bassNotes.get(i),scale);
      int [] durations = new int[bassDurations.size()];
      index = 0;
      for(int i=0; i<durations.length; i++)
         durations[index++] = bassDurations.get(i);
      ArrayList<int[]> retVal = new ArrayList();
      retVal.add(notes);
      retVal.add(durations);
      return retVal;
   }

//returns an array of chord durations with a total duration of 4 whole notes
   public static int[] makeChordDurations()
   {									//each string below represents 4 beats (1 whole note)
      String[]chordPatterns = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
   								//'0' refer to no chord being hit here.  '1' refers to a chord being hit here
      int chordIndex1 = (int)(rand.nextDouble()*(chordPatterns.length-1))+1;	//pick any except "0000"
      int chordIndex2 = (int)(rand.nextDouble()*chordPatterns.length);
      while(chordIndex1==chordIndex2)
         chordIndex2 = (int)(rand.nextDouble()*chordPatterns.length);
      int chordIndex3 = (int)(rand.nextDouble()*chordPatterns.length);
      while(chordIndex1==chordIndex3 || chordIndex2==chordIndex3)
         chordIndex3 = (int)(rand.nextDouble()*chordPatterns.length);
      String chordPattern1 = chordPatterns[chordIndex1];
      String chordPattern2 = chordPatterns[chordIndex2];
      String chordPattern3 = chordPatterns[chordIndex3];
   
      String chordPattern = "";					//a 16 beat measure	(4 whole notes)
      int whichPattern =  (int)(rand.nextDouble()*13);
      if(whichPattern == 0)						//A-A-A-A
         chordPattern = chordPattern1+chordPattern1+chordPattern1+chordPattern1;
      else if(whichPattern == 1)					//A-A-A-B
         chordPattern = chordPattern1+chordPattern1+chordPattern1+chordPattern2;
      else if(whichPattern == 2)					//A-A-B-A
         chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern1;
      else if(whichPattern == 3)					//A-A-B-B
         chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern2;
      else if(whichPattern == 4)					//A-B-A-A
         chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern1;
      else if(whichPattern == 5)					//A-B-A-B
         chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern2; 
      else if(whichPattern == 6)					//A-B-B-A
         chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern1;
      else if(whichPattern == 7)					//A-B-B-B
         chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern2;
      else if(whichPattern == 8)					//A-A-B-C
         chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern3;
      else if(whichPattern == 9)					//A-B-B-C
         chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern3;
      else if(whichPattern == 10)				//A-B-C-C
         chordPattern = chordPattern1+chordPattern2+chordPattern3+chordPattern3;
      else if(whichPattern == 11)				//A-B-A-C
         chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern3;
      else if(whichPattern == 12)				//A-B-C-B
         chordPattern = chordPattern1+chordPattern2+chordPattern3+chordPattern2;
      else 												//B-B-B-B
         chordPattern = chordPattern2+chordPattern2+chordPattern2+chordPattern2;                  
      int numOnes = 0;								//count the number of chord hits
      for(int i=0; i<chordPattern.length(); i++)
         if(chordPattern.charAt(i)=='1')
            numOnes++;
      if(chordPattern.startsWith("0"))			//starts with a rest      
         numOnes++;									//so the first 'chord' is a rest
      int [] chordDurations = new int[numOnes];
      int index = 0;									//index for chordDurations as we add elements
      for(int i=0; i<chordPattern.length(); i++)
      {
         if(index==0 && chordPattern.startsWith("0"))	//starts with a rest
         {
            int numZeros = 0;						//what duration to make the rest
            for(int j=0; j<chordPattern.length(); j++)
            {				
               if(chordPattern.charAt(j)=='0')
                  numZeros++;
               else
                  break; 
            }				
            chordDurations[index++]=(wholeNote/4)*(numZeros)*(-1);
         }
         else
            if(chordPattern.charAt(i)=='1')		//after each chord hit, count the number of '0's 
            {												//before there is another chord hit so that we know
               int numZeros = 0;						//what duration to make the chord
               for(int j=i+1; j<chordPattern.length(); j++)
               {				
                  if(chordPattern.charAt(j)=='0')
                     numZeros++;
                  else
                     break; 
               }											//each '0' after a chord will add a 1/4 note to the duration
               chordDurations[index++]=(wholeNote/4)*(1+numZeros);
            }
      } 
      removeDoubleRests(chordDurations); 
      return chordDurations;
   }

//returns an ArrayList where the first element is an int[] of chord durations
//and the second element is an int[] (I, ii, iii, ...vii) )of chord Set to play in sequence
//and the third element is an int[] of which chord index  from the chord set to play
//if the first element of chordDurations is negative, then the rhythm starts with a half-note rest (starts on the off beat)
//can specify a startNote and/or endNote such that the first and/or last chords must contain that note,
//or send -1 to not specify them
//if melodyNotes is null, then it will pick random chords
//otherwise, it uses chords that contains notes in melodyNotes
   public static ArrayList<int[]> makeChordTheme(int[]melodyNotes, int startNote, int endNote)
   {	
      int[]ourChordIndexes = null;						//will contain chord indexes of chords we can draw from
      if(richness > chordSetsA.length)
         richness = chordSetsA.length;
      if (richness >=1 && richness<=chordSetsA.length)				
      {															//this picks a collection of the group values of the 
         ourChordIndexes = new int[richness];		//chords that we will draw from.
         Set <Integer> chordTemp = new HashSet();	//ie, if outChordIndexes contains {0,3,4,6}, then the
         if(richness == chordSetsA.length)  			//chord theme that is built will only draw from the cord
            for(int i=0; i<chordSetsA.length; i++)	//groups I,IV,V and vii. 
               chordTemp.add(i);
         else
         {
            if(rand.nextDouble() < .75)					//most of the time, make sure it includes the root
               chordTemp.add(0);
            if(richness >=3 && rand.nextDouble() < .5)//half the time, make sure it has I, IV, and V
            {
               if(numNotesInScale() == 7)					//only need ot do this if we have a normal 7 note scale
               {
                  chordTemp.add(0);
                  chordTemp.add(3);
                  chordTemp.add(4);
               }
            }
            for(int i=0; chordTemp.size() < richness; i++)
               chordTemp.add((int)(rand.nextDouble()*chordSetsA.length));
         }
         int index=0;   
         for(Integer x:chordTemp)							//these are the chord groups (I, ii, iii...vii) that we will draw from
            ourChordIndexes[index++] = x;	
      }
   					         
      int[]chordDurations =  makeChordDurations();
      int[]chordGroups = new int[chordDurations.length];
      int[]chordIndexes = new int[chordDurations.length];
      int second = chordGroupIndex(2);
      int third = chordGroupIndex(3);
      int fourth = chordGroupIndex(4);
      int fifth = chordGroupIndex(5);
      int sixth = chordGroupIndex(6);
      int seventh = chordGroupIndex(7);
      int index=0;									//index for melodyNotes
      for(int i=0; i<chordGroups.length; i++)
      {
         int prevChordGroup = -1;		 
         if(i > 0)
         {  //get to the first previous chord group that is not a rest
            int pcgIndex = i-1;	//previous chord group index
            while (pcgIndex >= 0 && chordDurations[pcgIndex] < 0)
               pcgIndex--;
            if(pcgIndex >= 0)   
               prevChordGroup = chordGroups[pcgIndex];
         }							
         if(richness >= 7  && numNotesInScale() >= 7)	//only if we will allow all chords in a normal 7 note scale or more
         {															//ie, we may want to use major progression rules with a major bebop scale
         //if we haven't sent any melody notes and we are not using chord rhythm rules
            if(melodyNotes==null && rand.nextDouble() >= chordThemeRules)
            {
               if (startNote != -1 && i==0 && noteInChord(startNote, scale, scale.length))
               {
                  int[] info = getChordInfoThatHasNote(startNote, chordSetsA);
                  if(info != null)
                  {
                     chordGroups[i] = info[0];
                     chordIndexes[i] = info[1];
                  }
                  else	//something went wrong, so pick a random chord
                  {
                     int[] chInfo = getRandChordInfo(chordSetsA);
                     chordGroups[i] = chInfo[0];
                     chordIndexes[i] = chInfo[1];
                  }
               }	
               else
                  if (endNote != -1 && i==chordGroups.length-1 && noteInChord(endNote, scale, scale.length))
                  {
                     int[] info = getChordInfoThatHasNote(endNote, chordSetsA);
                     if(info != null)
                     {
                        chordGroups[i] = info[0];
                        chordIndexes[i] = info[1];
                     }
                     else	//something went wrong, so pick a random chord
                     {
                        int[] chInfo = getRandChordInfo(chordSetsA);
                        chordGroups[i] = chInfo[0];
                        chordIndexes[i] = chInfo[1];                        
                     }
                  }
                  else									//pick a random chord because we are not following any rules 
                  {
                     int[] chInfo = getRandChordInfo(chordSetsA);
                     chordGroups[i] = chInfo[0];
                     chordIndexes[i] = chInfo[1];
                  }
            }
            else
               if (melodyNotes != null)			//if we sent melody notes, make each successive chord
               {											//contain each one of the melody notes in order
                  if(index < melodyNotes.length && noteInChord(melodyNotes[index], scale, scale.length))
                  {
                     int[] info = getChordInfoThatHasNote(melodyNotes[index++], chordSetsA);
                     if(info != null)
                     {
                        chordGroups[i] = info[0];
                        chordIndexes[i] = info[1];
                     }
                     else	//something went wrong, so pick a random chord
                     {
                        int[] chInfo = getRandChordInfo(chordSetsA);
                        chordGroups[i] = chInfo[0];
                        chordIndexes[i] = chInfo[1];
                     }
                  }  
                  else
                  {
                     int[] chInfo = getRandChordInfo(chordSetsA);
                     chordGroups[i] = chInfo[0];
                     chordIndexes[i] = chInfo[1];
                  }
               }
               else   									// if (chordThemeRules == true)
               {											//our first chord in the chord rhythm
                  if((i==0) || (i==1 && chordDurations[0] < 0))	//might be the second duration if the first is a rest								
                  {
                     if(rand.nextDouble() < startChordThemeOnI)
                     {
                        chordGroups[i] = 0;		//make the first chord the I chord (root) if the rules mandate it
                        chordIndexes[i] = getRandChordInfo(chordSetsA, 0);
                     }
                     else								//otherwise, pick a random first chord
                     {
                        int[] chInfo = getRandChordInfo(chordSetsA);
                        chordGroups[i] = chInfo[0];
                        chordIndexes[i] = chInfo[1];
                     }
                  }
                  else									//our last chord of the chord rhythm
                     if(i==chordGroups.length-1 && rand.nextDouble() < endChordThemeOnI_IVorV && scaleHasIVandV())
                     {	//a IV chord for the second to last chord can resolve with I or V
                        if(chordGroups[i-1] == fourth)		//if our second to last chord is the IV
                        {
                           if(rand.nextDouble() < .5)	
                              chordGroups[i] = 0;		//either end on the I chord
                           else
                              chordGroups[i] = fifth;		//or end on the V chord
                        }
                        else
                           if(chordGroups[i-1] == fifth)	//if our second to last chord is the V
                              chordGroups[i] = 0;		//then end on the I chord
                           else
                           {									//if our second to last chord is a I,ii,iii,vi or vii
                              double whichOne = rand.nextDouble();	//prioritize ending on I or V over IV
                              if(whichOne < .4)			//then either end on a I
                                 chordGroups[i] = 0;
                              else
                                 if(whichOne < .8)		//or end on a V
                                    chordGroups[i] = fifth;
                                 else						//or end on a IV
                                    chordGroups[i] = fourth;
                           }
                        chordIndexes[i] = getRandChordInfo(chordSetsA, chordGroups[i]);
                     }
                     else
                        if(i > 0 && rand.nextDouble() < majChordProgressionRules && scaleHasIVandV())		
                        {										//for picking any interior chord other than the first or last
                           if(chordGroups[i-1] == 0)	//I can go to any chord
                           {
                              int randGroupIndex = (int)(rand.nextDouble()*chordSetsA.length);	//I, ii, iii...vii
                              chordGroups[i] = randGroupIndex;
                           }
                           else
                              if(second != -1 && prevChordGroup == second)	
                              {								//ii can go to ii or iii or V
                                 int whichOne = (int)(rand.nextDouble()*3);
                                 if(whichOne == 0)
                                 {
                                    if(third != -1)
                                       chordGroups[i] = third;
                                    else
                                       chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                 }	
                                 else
                                    if(whichOne == 1)
                                       chordGroups[i] = fifth;
                                    else
                                       chordGroups[i] = second;
                              }
                              else
                                 if(third != -1 && prevChordGroup == third)	
                                 {								//iii can go to vi or IV or iii
                                    int whichOne = (int)(rand.nextDouble()*3);
                                    if(whichOne == 0)
                                    {
                                       if(sixth != -1)
                                          chordGroups[i] = sixth;
                                       else
                                          chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                    }
                                    else
                                       if(whichOne == 1)
                                          chordGroups[i] = fourth;
                                       else
                                          chordGroups[i] = third;
                                 }
                                 else
                                    if(prevChordGroup == fourth)	
                                    {								//IV can go to ii or V or IV
                                       int whichOne = (int)(rand.nextDouble()*3);
                                       if(whichOne == 0)
                                       {
                                          if(second != -1)
                                             chordGroups[i] = second;
                                          else
                                             chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                       }
                                       else
                                          if(whichOne == 1)
                                             chordGroups[i] = fifth;
                                          else
                                             chordGroups[i] = fourth;
                                    }
                                    else
                                       if(prevChordGroup == fifth)	
                                       {								//V can go to iii or vi or I or V
                                          int whichOne = (int)(rand.nextDouble()*4);
                                          if(whichOne == 0)
                                          {
                                             if(third != -1)
                                                chordGroups[i] = third;
                                             else
                                                chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                          }
                                          else
                                             if(whichOne == 1)
                                             {
                                                if(sixth != -1)
                                                   chordGroups[i] = 5;
                                                else
                                                   chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                             }
                                             else
                                                if(whichOne == 2)
                                                   chordGroups[i] = 0;
                                                else
                                                   chordGroups[i] = fifth;
                                       }
                                       else
                                          if(sixth != -1 && prevChordGroup == sixth)	
                                          {								//vi can go to ii or IV or vi
                                             int whichOne = (int)(rand.nextDouble()*3);
                                             if(whichOne == 0)
                                             {
                                                if(second != -1)
                                                   chordGroups[i] = second;
                                                else
                                                   chordGroups[i] = (int)(rand.nextDouble()*chordSetsA.length);
                                             }
                                             else
                                                if(whichOne == 1)
                                                   chordGroups[i] = fourth;
                                                else
                                                   chordGroups[i] = sixth;
                                          }
                                          else
                                          {
                                             int randGroupIndex = (int)(rand.nextDouble()*chordSetsA.length);	//I, ii, iii...vii
                                             chordGroups[i] = randGroupIndex;
                                          }
                           chordIndexes[i] = getRandChordInfo(chordSetsA, chordGroups[i]);
                        }//end if(i > 0 && rand.nextDouble() < majChordProgressionRules)
                        else
                        {//something went wrong, so lets pick a random chord group and chord
                           int[] chInfo = getRandChordInfo(chordSetsA);
                           chordGroups[i] = chInfo[0];
                           chordIndexes[i] = chInfo[1];
                        }
               }
         }
         else				//richness < 7 and/or we have a scale with more or less than 7 notes
         {
            chordGroups[i] = ourChordIndexes[(int)(rand.nextDouble()*ourChordIndexes.length)];
            chordIndexes[i] = getRandChordInfo(chordSetsA, chordGroups[i]);
         }
      }
      ArrayList<int[]>chordRhy = new ArrayList();
      if(rand.nextDouble() < .5 && chordDurations.length > 1)	//50% of the time, we'll double our chord rhythm
         chordDurations = doubleDurations(chordDurations);  	//to 32 beats (8 whole notes)
      chordRhy.add(chordDurations);
      chordRhy.add(chordGroups);
      chordRhy.add(chordIndexes);
      return chordRhy;
   }

//makes an array of melody durations with some repeating patterns
   public static int[] makeMelodyDurations(int total)
   {
      int [] duration = {wholeNote/8,wholeNote/4,wholeNote/4,wholeNote/4,wholeNote/2,wholeNote/2,wholeNote};
      ArrayList<Integer> dur = new ArrayList();
      int[] times = {wholeNote + wholeNote/2, wholeNote, wholeNote/2 + wholeNote/4, wholeNote/2};
      int time1 = times[(int)(rand.nextDouble()*times.length)];
      int[] segment1 = makeRiffDurations(time1, true, -1, .85, -1);
      int time2 = times[(int)(rand.nextDouble()*times.length)];
      int[] segment2 = makeRiffDurations(time2, true, -1, .85, -1);
      int timeSoFar = 0;
      while(timeSoFar < total)
      {
         double whatWillWeDo = rand.nextDouble();
         if(whatWillWeDo < .4)
         {
            for(int i:segment1)
               dur.add(i);
            timeSoFar += time1;   
         }
         else
            if(whatWillWeDo < .8)
            {
               for(int i:segment2)
                  dur.add(i);
               timeSoFar += time2;  
            }         
            else
            {
               int t = duration[(int)(rand.nextDouble()*duration.length)];
               dur.add(t);
               timeSoFar += t;  
            } 
      }
      int[] retVal = new int[dur.size()];
      int index = 0;
      for(Integer i:dur)
         retVal[index++] = i;
      retVal = equalizeDurations(retVal, total);
      removeDoubleRests(retVal);  
      return retVal;
   }

//returns a collection of durations that can be used as a repeating riff of summed duration 'total'
//negative values will signify a rest
//canWeDouble denotes whether or not we throw a random to possibly double the size of the riff
//forceDurationLength of -1 will choose random durations.  Otherwise, it forces all durations the same (except possibly the last)
//if shorterNotes is false, there is a possibility that the durations will be a collection of 1/8 notes through 1/2 notes
//if shorterNotes is true, it can only be comprised of 1/4 notes through whole notes
//if timeSig ==3 (in 3/4 time), the longest duration should be a dotted half-note
   public static int[] makeRiffDurations(int total, boolean canWeDouble, int forceDurationLength, double shorterNotes, int timeSig)
   {
      int [] duration = {wholeNote/8,wholeNote/4,wholeNote/4,wholeNote/4,wholeNote/2,wholeNote/2,wholeNote};
        
      if(rand.nextDouble() < shorterNotes)		//% of the time, make it a riff of shorter notes
      {
         for(int i=1; i<duration.length; i++)
            duration[i] /= 2;
      }
      if(timeSig == 3)  
      {  //if timeSig ==3 (in 3/4 time), the longest duration should be a dotted half-note
         int longestNote = duration[duration.length-1];
         duration[duration.length-1] = longestNote/2 + longestNote/4;
      }
      int [] riff = new int[50];         			//a collection of durations in a riff
   //int total = wholeNote * 2;
      double doWeDurFormat = rand.nextDouble();	//do we want to make it so that any notes of the shortest duration come in a group of an even number of notes?
      for(int i=0; i<riff.length && total > 0; i++)
      {
         int dur = duration[(int)(rand.nextDouble()*duration.length)];	//pick a random duration from the array of durations
         if(forceDurationLength != -1)
            dur = forceDurationLength;   
         if(total - dur > 0)
         {	
            if(dur == duration[0] && doWeDurFormat < durationFormat) 
            {	//if our duration is the shortest duration and we want all of the smallest durations to come in groups of even numbers
               if(total - dur*2 > 0)
               {
                  for(int j=0; j<2 && i<riff.length && total > 0; j++)
                  {
                     riff[i++] = dur;
                     total -= dur;
                  }
                  i--;
               }
            }
            else
            {
               riff[i] = dur;
               total -= dur;
            }
         }
         else
         {
            if(i != 0)  
               riff[i-1] = riff[i-1]+total;
            else
               riff[i] = total;
            total = 0;
         }
      }
      ArrayList<Integer>riffTemp=new ArrayList();
      for(int i=0; i<riff.length; i++)
         if(riff[i]!=0)
            riffTemp.add(riff[i]);
      int [] theRiff = new int[riffTemp.size()];
      for(int i=0; i<theRiff.length; i++)		//clean out the non-used elements
      {
         theRiff[i] = riffTemp.get(i);
         if(rand.nextDouble()>=busyNess && theRiff[i]<wholeNote)
         {	//allow it to be a rest if it is the first note OR the last note is not a rest
         //this way, we will not have two rests in a row 
            if(i==0 || (i > 0 && theRiff[i-1]>0 )) 
               theRiff[i]*=(-1);  					//rest or not, n% chance that it will be a note	   
         }
      }
      if(rand.nextDouble()<.5 && canWeDouble)
         theRiff = doubleArray(theRiff, false); 
      removeDoubleRests(theRiff);    
      return theRiff;
   }

//return melody durations (index 0) and melody notes (index 1) built upon the chord theme
   public static ArrayList<int[]> makeMelodyNotes(int[]chordThemeDurations,int[]chordThemeGroupIndexes, int[]chordThemeChordIndexes)
   {  //see which durations are the longest and most common.
   //if a riffDuration subset is created for a long duration (on the first time),
   //record it and use it again when that duration comes up again.
      int maxDur = max(chordThemeDurations);	//the biggest duration in chordThemeDurations
      int maxFreq = 0;								//the # times that biggest duration occurs
      for(int i:chordThemeDurations)
         if(Math.abs(i)==maxDur)
            maxFreq++;
      ArrayList<Integer> pattern = null;		//possible repeating duration pattern
      if(maxFreq > 1)
         pattern = new ArrayList();
   
      int [] riffDurations = new int[0];
      int [] riffNotes 		= new int[0];
   //see where the chords hit and what chords they are.  
   //Then create mini-melodies between each chord
      int ourNote = 0;
      for(int i=0; i< chordThemeDurations.length; i++)
      {//if currDur is negative, it is a rest
         int currDur = Math.abs(chordThemeDurations[i]);
         int[] currChord = ((Chord)(chordSetsA[chordThemeGroupIndexes[i]].get(chordThemeChordIndexes[i]))).getNotes();
         int[] nextChord = null;
         if(i < chordThemeDurations.length-1)
            nextChord = ((Chord)(chordSetsA[chordThemeGroupIndexes[i+1]].get(chordThemeChordIndexes[i+1]))).getNotes();
         int startNote = currChord[(int)(rand.nextDouble()*currChord.length)];
         int endNote = 0;
         if(nextChord != null)
            endNote = nextChord[(int)(rand.nextDouble()*nextChord.length)];
         else
         {
            endNote = scale[(int)(rand.nextDouble()*scale.length)];	
            endNote = getInSameOctave(endNote, startNote);
         }
         int [] tempDur = null;
         int prevNote = -1;									//the last melody note before we add a new sub-melody
         if(riffNotes.length > 0)
            prevNote = riffNotes[riffNotes.length-1];
         ArrayList<int[]> miniMelody = makeMelodyNotes(tempDur, currDur, startNote, endNote, prevNote);
         tempDur = miniMelody.get(miniMelody.size()-1);
         if(pattern != null)
         {
            if(currDur == maxDur && pattern.size()==0)	//1st time we have come across the maximum duration  															
               for(int d:tempDur)								//record that duration subset into pattern
                  pattern.add(d);
            else
               if(currDur == maxDur)//&& pattern.size() > 0)//we see our maximum duration again 
               {							//copy the pattern into the duration subset
                  int index = 0;
                  tempDur = new int[pattern.size()];
                  for(Integer d:pattern)
                     tempDur[index++] = d;
                  miniMelody = makeMelodyNotes(tempDur, -1, startNote, endNote, prevNote);  
               }
         }
                 //equalizeDurations(int[] melodyDurations, int[] melodyNotes, int total)
         miniMelody = equalizeDurations(tempDur, miniMelody.get(0), currDur);
         riffDurations = append(riffDurations,  miniMelody.get(0));
         riffNotes = append(riffNotes, miniMelody.get(1));
      } 
      ArrayList<int[]> retVal = new ArrayList();
      retVal.add(riffDurations);
      retVal.add(riffNotes); 
      return retVal;
   }
  
//creates array(s) of melody notes starting with 'startNote' (-1 for not specified) and ending just above or below endNote (could also be -1)	
//if we are using this to build a sub-melody following another, we can send a previous note 'prevNote', -1 if not used
//if durTotal is not -1, it creates a new riffDuration of that total duration 
//might have 3 arrays - a melody and 2 variations
//if 'riffDuration' has a repeating pattern (hasBenDoubled), the first melody (index 0) is standard,
//the second melody (index 1) has the first half repeated and the third melody (index 2) has the second half a repeat of the same intervals, but at a different starting note
//index 3 is the riffDuration (in case it changed)
   public static ArrayList<int[]> makeMelodyNotes(int[]riffDuration, int durTotal, int startNote, int endNote, int prevNote)
   {
      ArrayList<int[]> retVal = new ArrayList();
      if(durTotal != -1)
      {
         if(rand.nextDouble() < .5)
            riffDuration = makeMelodyDurations(Math.abs(durTotal));
         else
            riffDuration = makeRiffDurations(Math.abs(durTotal), true, -1, .25, -1);
      }
      int [] riffNotes = new int[riffDuration.length];
      int j;		//index of which note in the scale we will use
   //these are the note interval values depending on the scale
      int second = chordGroupIndex(2);
      int third = chordGroupIndex(3);
      int fourth = chordGroupIndex(4);
      int fifth = chordGroupIndex(5);
      int sixth = chordGroupIndex(6);
      int seventh = chordGroupIndex(7);
      if(startNote == -1 && rand.nextDouble() < startOnWiseNote)		//% of time we start a melody on (by priority) I, iii or V, ii or IV
      {	//if we haven't selected a starting note and randoms deem that we want to start on a I, iii or V, ii or IV
         double whichNote = rand.nextDouble();
         if(whichNote < .5)		//start on root 50% of time
            j=0;
         else
            if(whichNote < .8)	//start on iii or V 30% of time
            {
               if(rand.nextDouble() < .5)
               {
                  if(third != -1)	//theres a iii in the scale
                     j = third;
                  else
                     j = (int)(rand.nextDouble()*scale.length);
               }   
               else
               {
                  if(fifth != -1)	//theres a V in the scale
                     j = fifth;
                  else
                     j = (int)(rand.nextDouble()*scale.length);
               }   
            }
            else						//start on ii or IV 20%of time
            {
               if(rand.nextDouble() < .5)
               {
                  if(second != -1)	//theres a ii in the scale
                     j = second;
                  else
                     j = (int)(rand.nextDouble()*scale.length);
               }   
               else
               {
                  if(fourth != -1)	//theres a IV in the scale
                     j = fourth;
                  else
                     j = (int)(rand.nextDouble()*scale.length);
               }
            }
      }
      else
         if(startNote == -1)
         {//if we haven't specified a starting note
            if(rand.nextDouble() < .5)
               j = (int)(rand.nextDouble()*scale.length);//50% of the time, pick a random note from the scale
            else
               j = indexOfNote(note, scale);					//50% of the time, pick the last note played
         }   
         else
            j = indexOfNote(startNote, scale);				//pick the startNote      
      int startJ = j;
      for(int i=0; i<riffNotes.length; i++)
      {
         if(j<0 || j >= scale.length)
            j=(int)(rand.nextDouble()*scale.length);
         if(i==1 && riffDuration[0] < 0)	//negative duration values in riff denote rests
            if(startJ>=0 && startJ < scale.length)
               j = startJ;							//if the first note is a rest, make the second note what we originally picked for the first note
         int ourNote = scale[j];
         int previousNote = -1;				//the note before 'note'   
         if(i > 0)
         {  //get to the first previous note that is not a rest
            int pnIndex = i-1;	//previous note index
            while (pnIndex >= 0 && riffNotes[pnIndex] == 0)
               pnIndex--;
            if(pnIndex >= 0)   
               previousNote = riffNotes[pnIndex];    
         }
         else
            if(i==0 && prevNote != -1)		//if we are building a sub-melody following another
               previousNote = prevNote;   
      	//only do this if the triTone is not in the scale
         if(rand.nextDouble() < allowTritone && !isTritoneInScale(scale))
         {  //avoid playing tritone at the start or end of the phrase
         //AND make sure that the tritone is either preceded by a 4th or 5th or in the middle of a 4th or 5th
            if(i>0 && i<riffNotes.length-1 && ( isFifth(previousNote,scale) || isFourth(previousNote,scale)) )
            {//sometimes allow the tritone (augmented 4th or dim 5th) - or half of the octave.  
            //1/2 step up from 4, 1/2 down from 5th
            //in C, 4th is F, tritone is F#, 5th is G
            //if 0 is the root, the tritone is 6 and the octave is 12
               if(isFifth(ourNote, scale))//our previous note was a 4th or 5th, and our picked note is a 5th
                  ourNote--;						//now, its a diminished 5th
               else
                  if(isFourth(ourNote, scale))//previous note was a 4th or 5th, and our picked note is a 4th
                     ourNote++;						//now its an augmented 4th
            }
         }
         else
         //if our previous note is a tritone, resolve with a 4th or 5th 75% of the time
            if(previousNote>0 && isTritone(previousNote, scale) && rand.nextDouble() < .75)
            {
               if(scaleHasIVandV())	
               {						//we have a 4th and 5th, so randomly pick one or the other
                  if(rand.nextDouble() < .5)
                     ourNote++;		//resolve with a 5th
                  else
                     ourNote--;		//resolve with a 4th
               }
               else
                  if(scaleHasIV())
                     ourNote--;		//resolve with a 4th
                  else
                     if(scaleHasV())
                        ourNote++;	//resolve with a 4th
                     else			//scale has neither a 4th or 5th to resolve to, so pick a random note
                        ourNote = scale[(int)(rand.nextDouble()*scale.length)];
            }
      
         riffNotes[i] = ourNote;
         if(riffDuration[i] < 0)		//negative duration values in riff denote rests
         {
            ourNote = 0; 
            riffNotes[i] = ourNote;
         }	
         else
            if(endNote == -1 || i < riffNotes.length - 3)//if we don't have to end near a specific note
            {															//or we do but are more than 3 events away from the last note
               int nextNote = 1;									//80% of the time, use step-wise (conjunct) motion
               if(rand.nextDouble() < disjunctMotion)		//20% of the time, use disjunct motion
               {	//pick a random next step between 2 and 5
                  nextNote = (int)(rand.nextDouble()*4)+2;// makes the next note within five notes of each other, up or down
               /*Avoid leaps greater than a fifth, Except ascending m6th. 
                 The ascending minor sixth is the interval formed by rising from the major third of the key to the tonic 
                 (in the key of C, from E up to C).  So, if we are a sixth below the root, we can jump up a 6th to the root
                 and pick a random disjunct motion from 2 to 6*/
                  if (isMaj3(scale[j], scale))
                  {
                     nextNote = (int)(rand.nextDouble()*5)+2;
                     if(rand.nextDouble() < ascendMin6th)
                     {	//find the number of index increases to get to the next root octave
                        int step=0;	//step should be 5
                        while(j+step < scale.length)
                        {
                           if(isRoot(scale[j+step], scale))
                              break;
                           step++;   
                        }
                        if (j+step < scale.length)
                           nextNote = step;
                        else			//go to the root, one octave lower
                           if (j+step-8 >= 0 && j+step-8 < scale.length)
                              nextNote = step - 8;
                           else		//something went wrong - pick random disjunct motion
                              nextNote = (int)(rand.nextDouble()*5)+2;
                     }    
                  }
                  else					//5% of the time, our disjunct motion is an octave (if in range)  
                     if((j+8 < scale.length || j-8>=0) && rand.nextDouble() < .05)
                        nextNote = 8;
                     else
                     /*
                     Allow leaps of an ascending seventh n% of the time:
                     This interval appears only between the dominant and subdominant in a major key (G-F in key of C) 
                     or between the subtonic and submediant in a minor key (G-F in a minor).   */
                        if (isFifth(scale[j], scale))
                        {
                           nextNote = (int)(rand.nextDouble()*5)+2;
                           if(rand.nextDouble() < ascendMin7th)
                           {	//find the number of index increases to get to the next min 7
                              int step=0;	//step should be 6
                              while(j+step < scale.length)
                              {
                                 if(isFourth(scale[j+step], scale))
                                    break;
                                 step++;   
                              }
                              if (j+step < scale.length)
                                 nextNote = step;
                              else			//go to the root, one octave lower
                                 if (j+step-8 >= 0 && j+step-8 < scale.length)
                                    nextNote = step - 8;
                                 else		//something went wrong - pick random disjunct motion
                                    nextNote = (int)(rand.nextDouble()*5)+2;
                           }    
                        }
               
               }
               double upOrDown = rand.nextDouble();     	//will the next note go up or down?  
               double pickInRange = rand.nextDouble(); 	//should we pick the next note within a five step range?     
               if(previousNote>0 && riffNotes[i]>0)		//if previousNote and note are not rests
                  if(rand.nextDouble() < resolveDisjunct && i>0 && Math.abs(previousNote - riffNotes[i]) >= 7)		
                  {//if the difference between the last note and the current note is an interval of 5 or more (7 semitones)
                  //resolve disjunct motion with conjunct motion in opposite direction
                     int diffFromLast = difference(previousNote, riffNotes[i]);
                     nextNote = 1;
                     if(previousNote < riffNotes[i])		//large interval leap from low to high should resolve in the opposite direction
                        upOrDown = 1;
                     else
                        upOrDown = 0;
                  }
               if(rand.nextDouble() < melodyRules)			//n% of the time, choose next note following above rules						
               {
                  if(upOrDown<.5)
                  {
                     if(j+nextNote < scale.length-1)
                        j+=nextNote;
                     else
                        if(nextNote != 5)						//disjunct motion of a 6th must be ascending to the root
                           j-=nextNote;
                  }
                  else
                  {	   
                     if(j-nextNote >= 0 && nextNote != 5)//disjunct motion of a 6th must be ascending to the root
                        j-=nextNote;
                     else
                        j+=nextNote;
                  } 
               }
               else													 //if we aren't following melody rules, the next note is a random note in the scale or mode
                  j=(int)(rand.nextDouble()*scale.length);//j is the note (the index of the notes array)
            }   
            else										//we nead to approach the endNote
            {
               int nextNote = 1;					//80% of the time, use step-wise (conjunct) motion
               if(rand.nextDouble() < .2)		//20% of the time, use disjunct motion
                  nextNote = (int)(rand.nextDouble()*6);	//makes the next note within five notes of each other, up or down
               if(scale[j] < endNote && j+nextNote < scale.length-1)			
                  j+=nextNote;					//move towards the endNote
               else
                  if(j-nextNote >= 0)
                     j-=nextNote; 
                  else								//5% of the time, the next note is a random note in the scale or mode
                     j=(int)(rand.nextDouble()*scale.length);             
            }                    
      }
      retVal.add(riffNotes);  
   //now do a couple of variations of riffNotes if the durations are doubled
      if(hasBeenDoubled(riffDuration))	//with a riff that has the same second half durations as the first,
      {											//make the notes in the second half the same as well
         int[]riffVar = riffNotes.clone();
         int index = riffNotes.length/2;
         int octShift = (int)(rand.nextDouble()*3) - 1;	//-1, 0 or 1 
         for(int i=0; i<riffNotes.length/2; i++)			//second half shiftend down an octave, not at all or up an octave
            riffVar[index++] = riffNotes[i] + OCTAVE * octShift;
         retVal.add(forceChordInRange(riffVar));    
      //make the notes in the second half the same intervals but different starting note
         riffVar = riffNotes.clone();
         int[] indexes = getIndexIntervalsInRiff(riffNotes,true);
         int delta = 0;
      //make sure no notes will go out of range of the scale
         int min = min(indexes);		//minimum index
         delta = (int)(rand.nextDouble()*8) - min;
         boolean pickAgain = true;
         while(pickAgain)				//make sure shift up doesn't have indexes that go outside of the scale
         {  
            pickAgain = false;
            delta = (int)(rand.nextDouble()*8) - min;
            for(int i=0; i<indexes.length; i++)
               if(indexes[i] > scale.length)
               {
                  pickAgain = true;
                  break;
               }
         }
         indexes = addScalar(indexes, delta);
         index = riffNotes.length/2;
         for(int i=0; i<indexes.length; i++)
            if(indexes[i] >= 0)
               riffVar[index++] = scale[indexes[i]];
            else
               riffVar[index++] = 0;
         retVal.add(riffVar);           
      }
      retVal.add(riffDuration);
      return retVal;
   }

//returns an int array of notes starting at 'startNote' that runs through a scale.
//numNotes is the number of notes, which will be randomly assigned if it comes in as -1
//this is used to assign the number of notes needed in the scale run
//if melodyNotes is not null, there is a possibility that every nth note will follow the sent melody
   public static int[] makeScaleRun(int numNotes, int startNote, int[]melodyNotes)
   {  
      int j;														//index of which note in the scale we will use
      if(startNote == -1 && rand.nextDouble() < .75)	//75% of the time, the first note is the root
         j=0;
      else
         if(startNote == -1)
            j = (int)(rand.nextDouble()*scale.length);//pick a random note from the scale
         else
            j = indexOfNote(startNote, scale);			//pick the startNote
   
      int interval = (int)(rand.nextDouble()*3)+1;		//intervals can be 1-3
      if(interval>1 && rand.nextDouble() < .5)			//but usually 1.  
         interval = 1;
   
      if(numNotes == -1)
         numNotes = (int)(rand.nextDouble()*19)+2;	//between 2 and 20 notes in the run
          
      int [] arpNotes = new int[numNotes];
   
      double haveSameNote = rand.nextDouble();			//should the arp have one note that is the same (20% of the time)
      int totalSameNotes = 0;					//we will count the number of notes that are the same
      int sameNoteNum = (int)(rand.nextDouble()*3);	//should the same note be the first note(0) or the second (1) or the third(2)
      if (sameNoteNum > 0 && rand.nextDouble() < .5)	//but it is usually the first note (0)
         sameNoteNum = 0;
      int  sameNoteValue = startNote;		//75% of the time, the same note in the arp will be the one we start with
      if(rand.nextDouble() < .25)			//25% of the time, it will be a random note in the scale
         sameNoteValue = scale[(int)(rand.nextDouble()*scale.length)];  
      int ourNote = 0;
      for(int u=0; u<arpNotes.length; u++)
      {           
         double dir = rand.nextDouble();		//should the arpeggio go up or down?
               
         if(u < numNotes -4)				//the last 4 notes in the arp can be the same
         {												//so, if we still have a lot of notes to go through,
            if(j<=0)									//then change directions if we get to the outer bounds of the scale
               dir = 1;						//if we are at the low boundry, change direction to go up
            else
               if(j >= scale.length -1)
                  dir = 0;					//if we are at the high boundry, change dir to go down
         }     
         if(j < 0 || j >= scale.length)  
            j = (int)(rand.nextDouble()*scale.length);    
         ourNote = scale[j];   
         if(haveSameNote < .25 && ((u%2 == sameNoteNum) || (u%3==0 && sameNoteNum==3)))
         {
            ourNote = sameNoteValue;
            totalSameNotes++;
         }  
         arpNotes[u] = ourNote; 
         if(dir<.5 &&  j>=0 && j<scale.length)  //up arp 50% of the time when our note index is in range
            j+=interval;								//step to the next note in the arp
         else
            if(j>=0 && j<scale.length)				//down arp 50% of the time when our note index is in range
               j-=interval;							//step to the next note in the arp
         if(j<0 || j>=scale.length)
         {      
            if(j<0)										//if our next note index is out of bounds,
               j=0;											//then make it either the first root
            else if(j>=scale.length)
               j=scale.length-1;						//or the last root
            if(rand.nextDouble() < .33)			//33% change - just pick a new random index
               j=(int)(rand.nextDouble()*scale.length);
         }
      }	
      if(melodyNotes != null && totalSameNotes > 0 && rand.nextDouble() < .25)
      {														//every nth note will follow the sent melody
         int index = 0;	//index for melodyNotes
         for(int i=0; i<arpNotes.length; i++)
            if(arpNotes[i] == sameNoteValue)
            {
               if(index >= melodyNotes.length)
                  index = 0;
               arpNotes[i] = melodyNotes[index++];
            }
      }
      return arpNotes;
   }

//returns an int array of notes starting at 'startNote' that runs through a scale.
//such that every other note in the scale run is a constant, or a successive note in the melody, a chord or another scale run
//numNotes is the number of notes, which will be randomly assigned if it comes in as -1
//this is used to assign the number of notes needed in the scale run
//if melodyNotes is not null, there is a possibility that every other note will follow the sent melody      
//chordGroupIndexes is a collection of chord groups (I..vii) and chordIndexes is the parallel array of which one of each chord is selected
//there is a possibility that every other note will be selected from an expanded chord       
   public static int[] makeSeratedScaleRun(int numNotes, int startNote, int[]melodyNotes, int[] chordGroupIndexes, int[] chordIndexes)
   {
      int j;														//index of which note in the scale we will use
      if(startNote == -1 && rand.nextDouble() < .75)	//75% of the time, the first note is the root
         j=0;
      else
         if(startNote == -1)
            j = (int)(rand.nextDouble()*scale.length);//pick a random note from the scale
         else
            j = indexOfNote(startNote, scale);			//pick the startNote
      if(numNotes == -1)
         numNotes = (int)(rand.nextDouble()*29)+2;	//between 2 and 30 notes in the run
          
      int [] runNotes = new int[numNotes];
      double dir = rand.nextDouble();		//should the run go up or down?
      if (dir >= .5)						//run going down
      {										//make the start note higher in the scale
         j = highestNoteIndex(j);
      }
      int ourNote = 0;
      int everyOtherNote = scale[j];					//every other note will be the startNote
      double everyOtherStrat = rand.nextDouble();	//or succesive notes from the melody, another scale run or a chord
      int[] scaleRun2 = makeScaleRun(numNotes/2, startNote, melodyNotes);
      int whichChord = (int)(rand.nextDouble()*chordGroupIndexes.length);
      int[] randChord = expandChord(((Chord)(chordSetsA[chordGroupIndexes[whichChord]]).get(chordIndexes[whichChord])).getNotes(),-1);
      int otherIndex = 0;									//index of the everyOtherNote as it is picked from another array
      int [] everyOtherSource = null;					//where we get our everyOtherNotes from - either the melody, another scale run, or a chord
      if(everyOtherStrat < .25 && melodyNotes != null)
         everyOtherSource = melodyNotes;  
      else
         if(everyOtherStrat < .5)
            everyOtherSource = scaleRun2;
         else
            if(everyOtherStrat < .75)
               everyOtherSource = randChord;
      if(everyOtherSource != null)
         everyOtherNote = everyOtherSource[otherIndex];           
      for(int i=0; i<runNotes.length; i++)
      {
         if(i % 2 == 0)
         {
            if(j<=0)							//then change directions if we get to the outer bounds of the scale
               dir = 0;						//if we are at the low boundry, change direction to go up
            else
               if(j >= scale.length -1)
                  dir = 1;					//if we are at the high boundry, change dir to go down
            if(j < 0 || j >= scale.length)  
               j = (int)(rand.nextDouble()*scale.length);    
            ourNote = scale[j];   
         
            runNotes[i] = ourNote; 
            if(dir<.5 &&  j>=0 && j<scale.length)  //up arp 50% of the time when our note index is in range
               j++;											//step to the next note in the arp
            else
               if(j>=0 && j<scale.length)				//down arp 50% of the time when our note index is in range
                  j--;										//step to the next note in the arp
            if(j<0 || j>=scale.length)
            {      
               if(j<0)										//if our next note index is out of bounds,
                  j=0;											//then make it either the first root
               else if(j>=scale.length)
                  j=scale.length-1;						//or the last root
               if(rand.nextDouble() < .33)			//33% change - just pick a new random index
                  j=(int)(rand.nextDouble()*scale.length);
            }
         }
         else
         {
            runNotes[i] = everyOtherNote;
            if(everyOtherSource != null)
            {
               if(otherIndex >= everyOtherSource.length)
                  otherIndex = 0;
               everyOtherNote = everyOtherSource[otherIndex++];
            }
         }
      }	
      if(rand.nextDouble() < .5)
         runNotes = reverse(runNotes);
      for(int i=0; i<runNotes.length; i++)
         if(runNotes[i] < 0)
            runNotes[i] = 0;   
      return runNotes;
   }
//returns an int array of notes starting at 'startNote' that runs through an ascending or descending lick.
//numNotes is the number of notes, which will be randomly assigned if it comes in as -1
//this is used to assign the number of notes needed in the scale run
   public static int[] makeLickRun(int numNotes, int startNote)
   {
      int [] ourScale = expandChord(scale, scale.length * 3);
      int j;														//index of which note in the scale we will use
      if(startNote == -1 && rand.nextDouble() < .75)	//75% of the time, the first note is the root
         j=0;
      else
         if(startNote == -1)
            j = (int)(rand.nextDouble()*scale.length);						//pick a random note from the scale
         else
            j = indexOfNote(startNote, scale);									//pick the startNote
         
      int numIntervalChanges = (int)(rand.nextDouble()*5)+2;				//between 2 and 4 interval changes int the lick
      if(numNotes == -1)
         numNotes = ((int)(rand.nextDouble()*9)+2)*numIntervalChanges;	//between 2 and 10 notes in the run      
      int [] lickNotes = new int[numNotes];
      int [] lickIntervals = new int[numIntervalChanges];
      for(int i=0; i<lickIntervals.length; i++)
         lickIntervals[i] = (int)(rand.nextDouble()*8)+1;
      boolean ascend = true;		//66% of the time, make the lick ascend
      if(rand.nextDouble() < .33)//33% of the time, make the lick descend
         ascend = false;  
      int k=0;							//index to traverse through lickIntervals
      int startingPoint = 0;		//where the lick starts with respect to the startNote (gets added to for ascend, subtracted from for descend)
      for(int i=0; i<lickNotes.length; i++)
      {
         if(k>=lickIntervals.length)
            k = 0;					//if we clip to the end of the interval, reset back to the beginning
         for(int m=0; m<lickIntervals.length; m++)	
         {								//reverse the direction of the lick if one of the notes in the lick is out of the scale
            if(((j + (lickIntervals[k]) + (startingPoint)) >= ourScale.length) || ((j + (lickIntervals[k]) + (startingPoint))<0) || !isNoteInRange(ourScale[j + (lickIntervals[k]) + (startingPoint)]))
            {
               ascend = !ascend;
               startingPoint = 0;
               break;
            }
         }
         if((j + (lickIntervals[k]) + startingPoint) < ourScale.length && (j + (lickIntervals[k]) + startingPoint)>=0)      
            lickNotes[i] = ourScale[j + (lickIntervals[k++]) + (startingPoint)];
         if(ascend)
            startingPoint++;
         else
            startingPoint--;	
      }
      return lickNotes;
   }

//create two scale runs and combine them into one that merges them together
   public static int[] makeCombinedScaleRun(int startNote)
   {
      int[]combined = null, scaleRun1 = null, scaleRun2 = null ;
      int numNotes = (int)(rand.nextDouble()*27) + 4;		//between 4 and 30 notes
      while(numNotes % 2 != 0 && numNotes % 3 != 0)		//must be a mult of 2 or 3
         numNotes = (int)(rand.nextDouble()*27) + 4;
      combined = new int[numNotes];
   
      int startNote2index = (int)(rand.nextDouble()*scale.length);
      if(startNote2index < scale.length/2)					//make the second scale run usually start on a higher note
         startNote2index = (int)(rand.nextDouble()*scale.length);
      int startNote2 = scale[startNote2index];   
   
      if(numNotes % 2 == 0)
      {
         scaleRun1 = makeScaleRun(numNotes/2, startNote, null);
         scaleRun2 = makeScaleRun(numNotes/2, startNote2, null);
         int index1=0, index2=0, i=0;
         while(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
         {
            combined[i++] = scaleRun1[index1++];
            if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
            {
               combined[i++] = scaleRun1[index1++];
               if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
               {
                  combined[i++] = scaleRun2[index2++];
                  if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
                  {
                     combined[i++] = scaleRun2[index2++];
                  }
               }
            }
         }
      }
      else	//if(numNotes % 3 == 0)
      {
         scaleRun2 = makeScaleRun(numNotes/3, startNote, null);
         numNotes -= (numNotes/3);
         scaleRun1 = makeScaleRun(numNotes, startNote2, null);
         int index1=0, index2=0, i=0;
         while(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
         {
            combined[i++] = scaleRun1[index1++];
            if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
            {
               combined[i++] = scaleRun1[index1++];
               if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
               {
                  combined[i++] = scaleRun1[index1++];
                  if(i<combined.length && index1<scaleRun1.length && index2<scaleRun2.length)
                  {
                     combined[i++] = scaleRun2[index2++];
                  }
               }
            }
         } 
      }
      return combined;
   }

//makes a melody sent with sent durations at tracking position 'where', with variations in transposition
//melodyNotes and melodyDurations should have the same length
//returns an ArrayList where index 0 is the durations and index 1 is the notes
   public static ArrayList<int[]> makeMelodyVariations(int[]melodyDurations, int[]melodyNotes)
   {
      ArrayList<int[]> returnVals = new ArrayList();  
   
      int[] ourMelody = melodyNotes.clone();
      int[] ourDurations = melodyDurations.clone();  
   
      int i2o = intervalsToOctave(scale);							//the # of intervals to an octave in the chosen scale
      int [] jumps = new int[(int)(rand.nextDouble()*6)+2];	//between 2 and 8 interval jumps 
      for(int i=0; i < jumps.length; i++)
         jumps[i] = (int)(rand.nextDouble()*i2o) - (i2o/2);	//make the transposed melody have an interval jump + or - (1/2)octave
      double whichOrder = rand.nextDouble();  
      if(whichOrder < .4)					//40% chance ascending order
         selSort(jumps);
      else
         if(whichOrder < .8)				//40% chance descending order
         {
            selSort(jumps);
            jumps = reverse(jumps);	  
         }
         else
            if(whichOrder < .9)			//10% chance shuffled order
            {
               selSort(jumps);
               jumps = shuffle(jumps);	  
            }									//10% chance random order
      int[] currentMelody = new int[0];
      int[] currentDurations = new int[0];	
   
      if(jumps.length > 3 || rand.nextDouble() < .25)	
      {//change currentMelody and currentDurations to be a subset of the originals
         int subsetLength = wholeNote;		//either whole, dotted whole or double whole
         double whichLength = rand.nextDouble();
         if(whichLength < .3 || timeSig==3)
            subsetLength = wholeNote + (wholeNote/2);
         else
            if(whichLength < .6)
               subsetLength = wholeNote * 2;
         currentDurations = equalizeDurations(ourDurations, subsetLength);      
      //TO DO: need to make sure currentDurations is at least size 2
      //and make sure the durations are interesting
         if(currentDurations.length < 2)
         {
            currentDurations = equalizeDurations(ourDurations, subsetLength*2);	//grab twice as big a subset of durations
            currentDurations = multScalar(currentDurations, 0.5);						//then make them half as much	
         }
         currentMelody = new int[currentDurations.length];
         for(int i=0; i<currentMelody.length && i<melodyNotes.length; i++)
            currentMelody[i] = melodyNotes[i];		
      }
      else
      {
         currentMelody = ourMelody;
         currentDurations = ourDurations;
      }
      for(int numTimes=0; numTimes < jumps.length; numTimes++)
      {
         ourMelody = append(ourMelody, transposeInScale(currentMelody, jumps[numTimes]));
         ourDurations = append(ourDurations, currentDurations); 
      } 
   
      returnVals.add(ourDurations);
      returnVals.add(ourMelody);
      return returnVals;
   }

//will adjust a note by adding or subtracting octaves until it is in an acceptable range
//a piano range is from 22(A0) to 108(C8)
//if the note is negative, it will return a random note from the scale
   public static int forceNoteInRange(int ourNote, int[] scale)
   {
      if (ourNote == 0)
         return 0;
      if(ourNote < 0)					//if we have a negative ourNote for some reason, make it a random ourNote in the scale
         return scale[(int)(rand.nextDouble()*scale.length)];
      if(ourNote > 108)					//if ourNote is too high in frequency, drop it octaves until it is in range
      {
         while(ourNote > 108)
            ourNote -= OCTAVE;
         return ourNote;
      }
      if(ourNote > 0 && ourNote < 22)	//figure out what ourNote is too low to hear	
      {
         while(ourNote > 0 && ourNote < 22)
            ourNote += OCTAVE;
         return ourNote;
      }
      return ourNote;
   }
  
//plays a sequence of chords (from chord group 'chordGroupIndexes' (from chordSetsA) with indexes 'chordIndexes' with respective durations 'chordDurations' at tracking position 'where'
//if chordDrations[0] is negative, then start on the off beat (hold a half note before the first chord
//if melodyDurations and melodyNotes are not null, it will play a melody along with the chords such that it fits
//otherwise, it may randomly play a melody or just the chords
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playChordTheme(int[] chordDurations, int[] chordGroupIndexes, int[] chordIndexes, int[] melodyDurations, int [] melodyNotes,  int where, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {  
      int [] returnVals = new int[3];
      int ourNote = -1;
      boolean bassLine = false;			//do we want to do a full bass line or notes from chords dropped 2 octaves?
      if(rand.nextDouble() < .5)
         bassLine = true;
      int bassStart = where;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      int transitionPart = (int)(rand.nextDouble()*3)+2;	//this is the partition for when we transitin back:2,3,4 - transition in 2nd half, last 3rd or last 4th
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//50% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//50% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
      if(rand.nextDouble() < .5)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
      boolean flareChord = false;
      if(rand.nextDouble() < forceArp || rand.nextDouble() < .25)	//25% of the time, allow the chords to arpeggiate
         flareChord = true;
      int firstTracking = where;	//save starting tracking values to see if a melody note is on the on-beat or off-beat	
      int melodyTracking = where;//save starting tracking values so we know where to start the melody because chords need to be played first
      int bassTracking = where;	//save starting tracking values so we know where to start the bass because chords are played first
      int total = 0;					//total duration time for the chord rhythm		
      for(int i=0; i < chordDurations.length; i++)
         total += Math.abs(chordDurations[i]);
      if(chordDurations[0] < 0)	//if the first duration is negative, it denotes a half note rest (chord starts on off beat)
      {												//so find the minimum duration so that we know how long to rest
         if(chordDurations[0] < 0)
            total = nextMultOfInterval(total, wholeNote);     //next closest whole note multiple   
         int minDur = total / 16;   
         where += minDur*2;
      }
      boolean maybePlayMelody = false;//should we just play the chords by themselves?
      if(rand.nextDouble() < .75)
         maybePlayMelody = true;
   
      if(melodyDurations != null && melodyNotes != null)  
      {  //make the collections of sent durations and notes have the same running time as the sent collection of chord durations
         ArrayList<int[]> result = equalizeDurations(melodyDurations, melodyNotes, total);
         melodyDurations = result.get(0);
         melodyNotes = result.get(1);
      }
      else
      {	//if we didn't send a melody, we will make some new ones to possibly play with our chord rhythm
         if(melodyDurations==null || melodyDurations.length == 0)
            melodyDurations = makeRiffDurations(total, false, -1, .25, -1);
         if(melodyNotes == null || melodyNotes.length == 0)
         {
            ArrayList<int[]> melodySets = makeMelodyNotes(melodyDurations, -1, -1, -1, -1);
            melodyNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));//the last index is excluded because it is durations (in case they change)
         }
      }
   
      melodyNotes =riffOctave(melodyNotes, 1);	//raise the riff an octave
      int bassNote = 0;
      int lastBassNote = 0;
      double whichChordApproach = rand.nextDouble();
     	
      for(int i=0; i<chordDurations.length; i++)
      {
      //******for tempo changes******
         if(willWeChangeTempo)
         { 				//within the last 1/transitionPart of the events 
            if(i >= chordDurations.length - (chordDurations.length/transitionPart) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first so many events, or not transitioning back   
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(newTempo > tempo*4)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(newTempo < tempo / 4)//too slow...speed things up
               {
                  speedUp = true;
                  slowDown = false;
               }
         }
         //***********************************
      
         int chordGroup = chordGroupIndexes[i];				//I, ii, iii,...,vii chord group
         int chordIndex = chordIndexes[i];					//which chord in that group
         int velocitySoften = (int)(rand.nextDouble()*16)+5;
         ArrayList ourGroup = null;
         ourGroup = chordSetsA[chordGroup];
         int[]chord = null;
      
         int randIndex = (int)(rand.nextDouble() * chordSetsA[chordGroup].size());
         Chord theChord = ((Chord)(ourGroup.get(randIndex)));
         chord = theChord.getNotes();
         bassNote = chord[(int)(rand.nextDouble()*chord.length)] - (OCTAVE*2);
         bassNote = forceNoteInRange(bassNote, scale);
         bassTracking = where;
                                  
         if(whichChordApproach < .5 && !flareChord)
            where = playChord(theChord, Math.abs(chordDurations[i]), velocity-velocitySoften, where, CHORDSCHNL, music);   
         else
         {
            if(flareChord)
            {
               if(rand.nextDouble() >= altChords)
               {
                  if(rand.nextDouble() < limitChord)  
                     chord = makePalindrome(limitChord(chord, (int)(rand.nextDouble()*3)+1));
               }
               int noteSize = 4;
               int numNotesToPlay = Math.abs(chordDurations[i]) * noteSize / wholeNote;
               int noteDur = wholeNote/noteSize;
               int timePassed = where;
            //write chord name into MIDI file              
               String text = theChord.getName();
               addEvent(music, TEXT, text.getBytes(), where);
               for(int y = 0; y<chord.length && y<numNotesToPlay; y++)//for each note of the chord 
               {  
                  int chordNote = forceNoteInRange(chord[y], scale);
                  int[]temp = new int[1];
                  temp[0] = chordNote;
                  chordPlayed.put(where+(y*noteDur), temp);				//we are playing a chord, so add it to the Map of chords that have been played 
                  playNote(chordNote, noteDur, velocity-velocitySoften, where+(y*noteDur), CHORDSCHNL, music);
                  timePassed += (noteDur);
               }
               while(numNotesToPlay > chord.length)
               {
                  numNotesToPlay = numNotesToPlay - chord.length;
                  int index=0;
                  int numNotes = numNotesToPlay;
                  while(numNotes > chord.length)
                     numNotes -= chord.length;
                  for(int y = numNotes-1; y>=0; y--)					//for each note of the chord 
                  {  
                     int chordNote = forceNoteInRange(chord[y], scale);
                     int[]temp = new int[1];
                     temp[0] = chordNote;
                     chordPlayed.put(where+(y*noteDur), temp);		//we are playing a chord, so add it to the Map of chords that have been played 
                     playNote(chordNote, noteDur, velocity-velocitySoften, where+(index*noteDur), CHORDSCHNL, music);
                     timePassed += (noteDur);
                     index++;
                  }
               }
               where = timePassed;
            }
            else
               where = playChord(CHORDSCHNL, null, null, -1, where, chordGroup, chordIndex, Math.abs(chordDurations[i]), velocity-velocitySoften, forceArp, 0, music);
         }
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         if(rand.nextDouble() < .25 && velocity + Math.abs(velocityChange) < 95)
            velocityChange *= -1;
         if(bassLine == false)
            playNote(bassNote, Math.abs(chordDurations[i]), velocity + velocityChange, bassTracking, BASSCHNL, bassNotesPlayed, music);
      }
      int whichBeatToPlayBass = (int)(rand.nextDouble()*2)+1;
   
      if(maybePlayMelody)
      {
         for(int m=0; m<melodyNotes.length; m++)		//MELODY
         {
            ourNote=melodyNotes[m];
            if(ourNote > 0 && isOnBeat(firstTracking, melodyTracking, whichBeatToPlayBass))
            {
               int[] harmNotes =  getHarmonizeNotes(ourNote, scale);
               selSort(harmNotes);
               bassNote = harmNotes[(int)(rand.nextDouble()*harmNotes.length)] - (OCTAVE*2);
               bassNote = forceNoteInRange(bassNote, scale);
               int bassDuration = Math.abs(melodyDurations[m]);  
               if(bassDuration <= wholeNote/4)
                  bassDuration *= 2;
               if(bassLine == false)   
                  playNote(bassNote, bassDuration, velocity, melodyTracking, BASSCHNL, bassNotesPlayed, music);
            }
            melodyTracking = playNote(ourNote, Math.abs(melodyDurations[m]), velocity, melodyTracking, MELODYCHNL, melodyNotesPlayed, music);  
         }
      }
    //******for adding a bass line
      if(bassLine)
      {	
         int oldVelocity = velocity;
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         velocity += velocityChange;
         int bassTime = where - bassStart;
         ArrayList<int[]> bassLineInfo =  makeBassLine(bassStart, bassTime, 0, -1, false);
         int[] bassLineNotes = bassLineInfo.get(0);
         int[] bassLineDurations = bassLineInfo.get(1);
         playMelody(bassLineDurations, bassLineNotes, bassStart, 3, bassNotesPlayed, music);
         velocity = oldVelocity;
      }//****************************	
   
   //******for tempo change******
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //****************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(ourNote, scale);
      returnVals[2] = ourNote;
      return returnVals;
   }

//plays a sequence of chords with a melody on top.  The chords played will contain 'riffNotes' in sequence, or use the chord theme
//we can force the tempo of the sequence with 'chordLength'.  A 'time' of 3 is 3/4 (waltz).  'time' of 2 or 4 is 4/4 (standard).
//A time of 5 is 4/4 waltz omm-pah-pah-oom-omm-pah-pah-omm
//a time of 2 means that each chord will be played twice before moving to the next 
//'counter' is the % of time you want it to play a counter melody as well
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playChordSequence(int[] riffNotes, int[] chordGroupIndexes, int[] chordIndexes, int chordLength, int where, int time, double counter, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      boolean stacatto = false;									//should our chord strikes be stacatto
      int chLengthCut = 1;											//the amount we divide the chord length by if its stacatto
      if(rand.nextDouble() < .25)								//25% of the time, we will use stacatto chords
      {
         stacatto = true;
         chLengthCut = 2;
      }   											
      int chordStrat = (int)(rand.nextDouble()*2);			//0-build chords from riffNotes, 1-use chords from chord theme
      int [] returnVals = new int[3];
      int[] mult = multiples (riffNotes.length);			//to use for breaks in the sequence
      int whenDoWeBreak = 999;
      if(mult!=null && mult.length > 0)
      {
         whenDoWeBreak = mult[(int)(rand.nextDouble()*mult.length)];
         if(rand.nextDouble() < .5 && whenDoWeBreak == 2)	//less likely to be 2
            whenDoWeBreak = mult[(int)(rand.nextDouble()*mult.length)];
         if(rand.nextDouble() < .15)		//15% of the time, we won;t break at all
            whenDoWeBreak = 999;
      }   
      int ourNote = -1;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      int transitionPart = (int)(rand.nextDouble()*3)+2;	//this is the partition for when we transitin back:2,3,4 - transition in 2nd half, last 3rd or last 4th
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//50% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//50% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
      if(rand.nextDouble() < .5)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
   
      if (chordLength == -1)		//we are not doing forceChordLength, so pick a random between eighth and double-wholeNote
      {
         int exp = (int)(rand.nextDouble()*3);	//0,1,2
         double whichTime = rand.nextDouble();
         if(whichTime < .10)							//10% of the time, it is a double whole-note
            chordLength = wholeNote * 2;
         else
            if(whichTime < .95)
               chordLength = wholeNote / (int)(Math.pow(2,exp));	//28.3 % of the time, use a 4th, half or whole note each
            else
               chordLength = wholeNote / 8;		//5% of the time, it is an eighth note
      }
      if (time == -1 || time < 2 || time > 5)	//pick a random time signature - either 2, 3, 4 or 5  
         time = (int)(rand.nextDouble()*4) + 2;
   
      int lastOmm = -1;				//the last low note in the chord that we played   
      boolean playFullPah = true;					//do we want to play both notes in the pah part of oom-pah-pah
      if(rand.nextDouble() < .5)
         playFullPah = false;
      int pahType = (int)(rand.nextDouble()*3);	//0-one note, 1-staggered 2 notes, 3-mixed
      int numTimes = 0;
      if(chordStrat==0)  			//0-build chords from riffNotes
         numTimes = riffNotes.length;
      else								//1-use chords from chord theme
         numTimes = chordGroupIndexes.length;
      int prevNote = -1;			//the last melody note from each sub-melody
      for(int i=0; i<numTimes; i++)
      {
      //******for tempo changes******
         if(willWeChangeTempo)
         {  			//within the last 1/transitionPart of the events 
            if(i >= numTimes - (numTimes/transitionPart) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first so many events, or not transitioning back  
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(newTempo > tempo*4)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(newTempo < tempo / 4)			//too slow...speed things up
               {
                  speedUp = true;
                  slowDown = false;
               }
         }
      //***********************************
      
         int riffTracking1 = where;//save starting tracking values so we know where to start the riff because chords need to be played first
         int riffTracking2 = where;//for the counter melody  
      
         int startNote1 = -1, endNote1 = -1, startNote2 = -1, endNote2 = -1;
         int[]chord = null;
         Chord theChord = null;
         if(chordStrat==0)				//0-build chords from riffNotes
         {
         //make sure the riffNote is in the scale, unlike a tritone or a rest
            if(noteInChord(riffNotes[i], scale, scale.length) && riffNotes[i] > 0)
            {
               if (rand.nextDouble() < altChords)
               {
                  theChord = getChordThatHasNote(riffNotes[i], chordSetsB);
                  if (theChord != null)
                     chord = theChord.getNotes();		//the alternate chord to play
               }
               else
               {
                  theChord = getChordThatHasNote(riffNotes[i], chordSetsA);
                  if (theChord != null)
                     chord = theChord.getNotes();		//the common chord to play
               }
            }
         }
         else//if(chordStrat==1)		1-use chords from chord theme
         {
            theChord = (Chord)(chordSetsA[chordGroupIndexes[i]].get(chordIndexes[i]));
            chord = theChord.getNotes();
         }
         if (chord != null)			//if no such chord has that note, so it must be the tritone or a rest
         {
            startNote1 = chord[(int)(rand.nextDouble()*chord.length)];	//first note of our phrase is a note of the chord being played
            endNote1 = -1;																//last note of the phrase will be close to the next chord that will be played
            startNote2 = chord[(int)(rand.nextDouble()*chord.length)];	//same for counter melody
            endNote2 = -1;
            if(i < numTimes - 1)
               endNote1 = chord[(int)(rand.nextDouble()*chord.length)];
            if(i < numTimes - 1)
               endNote2 = chord[(int)(rand.nextDouble()*chord.length)];
         }
         int riffLength;		
         if(time==2 || time==5)
            riffLength = chordLength * 4;
         else
            riffLength = chordLength*time;
         int[]riffDuration = makeRiffDurations(riffLength, false, -1, .25, time);	
      //int[]melodyNotes = makeMelodyNotes(riffDuration, startNote1, endNote1);
         ArrayList<int[]> melodySets = makeMelodyNotes(riffDuration, -1, startNote1, endNote1, prevNote);
         int[]melodyNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));//the last index is excluded because it is durations (in case they change)
         melodyNotes =riffOctave(melodyNotes, 1);			//raise the riff an octave
         prevNote = melodyNotes[melodyNotes.length-1];  	//last note of the melody set
         ArrayList<int[]>counterMelody = makeCounterMelody(riffDuration, startNote2, endNote2, time);
         int[]riffDuration2=counterMelody.get(0);
         int[]melodyNotes2=counterMelody.get(1);
      	                   
         if(chord!=null)
         {
            int velocitySoften = (int)(rand.nextDouble()*16)+5;
            int lowNote1 = chord[0];
            int chordIndex1 = (int)(rand.nextDouble()*chord.length);	//random chord index (0,1,2,3 or 4) for our second low note
            int lowNote2 = chord[chordIndex1]-OCTAVE;
            if(lastOmm!= -1)		//if the last low note and the current low note are two apart, make the last low note inbetween the two
            {
               int middleNote = noteInMiddle(lowNote1, lastOmm);
               if(middleNote != -1)
                  lastOmm = middleNote;
            }
            if(time == 5 && lastOmm!= -1 && (i+1) % whenDoWeBreak != 0)	//4/4 waltz omm-pah-pah-omm-omm-pah-pah-omm.  This is the 2nd omm
            {
               playNote(lastOmm, chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);	//play low note of the chord (ooohm -)
               where += chordLength;
            }
            if((i+1) % whenDoWeBreak != 0)
            {
               playNote(lowNote1, chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);	//play low note of the chord (ooohm -)
               where += chordLength;
            }
            lastOmm = lowNote1;
         
            int numChords = 2;				//in 3/4 time (time 3 or 5), we want to first play the low note(ooohm-), then strike the chord twice (pah- pah-)
            if (time == 2 || time == 4)	//in 4/4 time, play low note, then rest of chord, then different low note, then rest of chord
               numChords = 1;					//so since we only want to play the first chord once...
            chordIndex1 = (int)(rand.nextDouble()*(chord.length-1))+1;		//(any index but index 0) indexes of the notes in the chord we are going to play
            int chordIndex2= (int)(rand.nextDouble()*(chord.length-1))+1;
            while(chordIndex1==chordIndex2)
               chordIndex2= (int)(rand.nextDouble()*(chord.length-1))+1;
            
            if(theChord != null && (i+1) % whenDoWeBreak != 0)					//write chord name into MIDI file
            { 							              
               String text = theChord.getName();
               addEvent(music, TEXT, text.getBytes(), where);
            }
            if((i+1) % whenDoWeBreak != 0)  	
               for(int j=0; j<numChords; j++)
               {
                  if(numChords == 2 && playFullPah==false)
                  {
                     double whichOne = rand.nextDouble();
                     if(pahType==0 || (pahType==2 && whichOne<.5))
                     {	//here, we want to play one note at a time
                        if(j==0)		
                           playNote(chord[chordIndex1],chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                        else
                           playNote(chord[chordIndex2], chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//(pah - pah -)
                     }
                     else //if(pahType==1)
                     {	//play and hold one note and then the other (at half the duration)
                        playNote(chord[chordIndex1],chordLength, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                        playNote(chord[chordIndex2], chordLength/2, velocity-velocitySoften, where+chordLength/2, CHORDSCHNL, music);
                     }
                  }
                  else
                  {		//play both chord notes at the same time here
                     playNote(chord[chordIndex1],chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                     playNote(chord[chordIndex2], chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//(pah - pah -)
                  }
                  chordPlayed.put(where, chord);//we are playing a chord, so add it to the Map of chords that have been played
                  where +=chordLength;
               }
                     
            if(time == 4)							//we will change to different notes for that chord to be played
            {	//(1,2,3 or 4) indexes of the notes in the chord we are going to play
               chordIndex1 = (int)(rand.nextDouble()*(chord.length-1))+1;		
               chordIndex2= (int)(rand.nextDouble()*(chord.length-1))+1;
               while(chordIndex1==chordIndex2)
                  chordIndex2= (int)(rand.nextDouble()*(chord.length-1))+1;
            }  
            if (time == 2 || time == 4  && (i+1) % whenDoWeBreak != 0)//play the second low note, then rest of the chord
            {
               playNote(lowNote2, chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);	//play low note of the chord (ooohm -)
               where += chordLength;
               if(playFullPah==false)
               {	
                  double whichOne = rand.nextDouble();
                  if(pahType==0 || (pahType==2 && whichOne<.5))
                  {//only play one of the notes
                     if(rand.nextDouble() < .5)
                        playNote(chord[chordIndex1],chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                     else
                        playNote(chord[chordIndex2], chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);
                  }
                  else
                  {//play and hold one note and then the other (at half the duration)
                     playNote(chord[chordIndex1],chordLength, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                     playNote(chord[chordIndex2], chordLength/2, velocity-velocitySoften, where+chordLength/2, CHORDSCHNL, music);
                  }
               }
               else
               {	//play both of the notes
                  playNote(chord[chordIndex1],chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);//play the other two notes in the chord
                  playNote(chord[chordIndex2], chordLength/chLengthCut, velocity-velocitySoften, where, CHORDSCHNL, music);
               }
               chordPlayed.put(where, chord);
               where +=chordLength;
            }
            if ((i+1) % whenDoWeBreak == 0)
                  //playChord(int chnl, Chord chordObject, int[] forceChord, int note, int where, int forceChordGroup, int forceChordIndex, int forceChordLength, int forceVelocity, double forceArp, int octave, Track music)
               where = playChord(CHORDSCHNL, theChord, chord, -1, where, -1, -1, riffLength, velocity-velocitySoften, forceArp, 0, music);
         } 
         for(int m=0; m<melodyNotes.length; m++)			//MELODY
         {
            ourNote=melodyNotes[m];
            riffTracking1 = playNote(ourNote, Math.abs(riffDuration[m]), velocity, riffTracking1, MELODYCHNL, melodyNotesPlayed, music);  
         }
         if(rand.nextDouble() < counter)
            for(int m=0; m<melodyNotes2.length; m++)		//COUNTER MELODY
            {
               ourNote=melodyNotes2[m];//choose our next counter melody note
               int melNote = noteHasBeenPlayed(riffTracking2, melodyNotesPlayed);		
               if(melNote != 0)			//if there is a main melody note already there,
               {								//then change the counter melody note to one that harmonizes
                  int[]harmnotes = getHarmonizeNotes(melNote, scale);
                  ourNote =  closestNoteInChord(ourNote, harmnotes, harmnotes.length);  
               }
               int velocitySoften = (int)(rand.nextDouble()*16)+5;	//soften the chord velocity between 5 and 20   
               riffTracking2 = playNote(ourNote, Math.abs(riffDuration2[m]), velocity-velocitySoften, riffTracking2, HARMONYCHNL, harmonyNotesPlayed, music);  
            }	
      }
   //******for tempo change******
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //****************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(ourNote, scale);
      returnVals[2] = ourNote;
      return returnVals;
   }

//will play notes in 'riffNotes' with durations as specified in 'riffDurations' at time 'where' in Track 'music'
//the riff may be played with variations in pitch, octave, with or without harmony, countermelody and chord accompanyment
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playMelodyTheme(int[]riffDuration, int[]riffNotes, int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int [] returnVals = new int[3];
      int note = -1;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean speedUp = false;			//50% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//50% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
   
      if(rand.nextDouble() < .5)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
      int riffTracking2 = where;		//so counter melody starts where the melody starts
      
      ArrayList<int[]>counterMelody = makeCounterMelody(riffDuration, -1, -1, -1);
      int[]riffDuration2=counterMelody.get(0);
      int[]melodyNotes2=counterMelody.get(1);
   
   
      boolean canWeDeviate = true;
   
      int riffStrat = (int)(rand.nextDouble()*4);	
   //0-just play riff with harmony notes
   //1-play octave lower riff with repeating stacatto chord parts, 
   //2-play riff with consistent durations and chords at intervals
   //3-play riff with countermelody
      if(riffStrat==1)
      {	
         canWeDeviate = false;
         ArrayList ourChords = getRiffChords(riffNotes, chordSetsA);
      
         Set<Integer> chordNotesToPlay = new HashSet();
         int riffTime = 0;				//total length of the riff
         for(int i=0; i < riffDuration.length; i++)
            riffTime += Math.abs(riffDuration[i]);       
      //pick a random chord out of the set of chords that have the most riff notes in common   
         int randIndex = (int)(rand.nextDouble()*ourChords.size());
         Chord theChord = ((Chord)(ourChords.get(randIndex)));
         int[]chord = null;
         int [] commonNotes = null;
         int numNotes = 0;
         if (theChord != null)
         {
            chord = theChord.getNotes();
            numNotes = (int)(rand.nextDouble()*chord.length-1)+1;	//we will pick at least 1 chord note to play
            commonNotes = getRiffNotesInChord(riffNotes, chord);
         }
         if(commonNotes == null)	//if there are no common notes, make our notes to play the root and its octave
         {
            chordNotesToPlay.add(scale[0]);
            chordNotesToPlay.add(scale[0]+OCTAVE);
         }
         else							//pick some random notes from commonNotes
         {
            for(int i=0; i < numNotes; i++)
            {
               if(numNotes > commonNotes.length && chordNotesToPlay.size() == commonNotes.length)
                  break;
               randIndex = (int)(rand.nextDouble()*commonNotes.length);
               while(chordNotesToPlay.contains(commonNotes[randIndex]))
                  randIndex = (int)(rand.nextDouble()*commonNotes.length);
               chordNotesToPlay.add(commonNotes[randIndex]);
            }
            if(chordNotesToPlay.size()==1 && rand.nextDouble()<.5)	//if there is only one note to play with our riff, then
            {
               if(rand.nextDouble() < .5)										//25% of the time we will add a rest after each note
                  chordNotesToPlay.add(0);
               else
               {
                  for(Integer x:chordNotesToPlay)							//25% of the time we will add an octave of our one note
                     chordNotesToPlay.add(x+OCTAVE);
               }
            }   
         }
      //Pick alternating notes from chordNotes to play at a constant duration for the length of the entire riff		 
         int chordIndex = 0;
         int[]myChord = new int[chordNotesToPlay.size()];
         for(Integer x:chordNotesToPlay)
            myChord[chordIndex++] = x;
         chordIndex = 0;
         int chordTracking = where;
         int chordlength;
         double chordSize = (int)(rand.nextDouble());
         if(chordSize < .45)					//45% of the time, our chord is a quarter note
            chordlength = wholeNote/4;
         else
            if(chordSize < .90)
               chordlength = wholeNote/8;	//45% of the time, our chord is an eigth note
            else
               chordlength = wholeNote/2;	//10% of the time it is a half note
         int runTime = where + riffTime;  
         int velocitySoften = (int)(rand.nextDouble()*16)+5;	//soften the chord velocity between 5 and 20
         boolean playFullChords = (commonNotes!=null && commonNotes.length >= 3 && rand.nextDouble() < .75);
         while(chordTracking < runTime)
         {
         //******for tempo changes******
            if(willWeChangeTempo)
            {   
               if(speedUp)
               {
                  newTempo += tempoDelta;
                  setTempo(newTempo, chordTracking, music);
               }
               else
                  if(slowDown)
                  {
                     newTempo -= tempoDelta;
                     setTempo(newTempo, chordTracking, music);
                  }
               if(newTempo > tempo*4)		//too fast..slow things down
               {
                  speedUp = false;
                  slowDown = true;
               }	
               else
                  if(newTempo < tempo / 3)			//too slow...speed things up
                  {
                     speedUp = true;
                     slowDown = false;
                  }
            }
         //***********************************
         
            if(playFullChords)	//if we have enough notes for a full chord
            {
               double arpChord = 0;  
               if(rand.nextDouble() < .25)
                  arpChord = 1.0;
               chordTracking += playChord(CHORDSCHNL, theChord, myChord, -1, chordTracking, -1, -1, chordlength, velocity-velocitySoften, arpChord, 0, music);
            //playChord(int chnl, int[] forceChord, int note, int where, int forceChordGroup, int forceChordIndex, int forceChordLength, int forceVelocity, boolean forceArp, int octave, Track music)
            }
            else
            {	//reset chordIndex to wrap around back to the beginning of the set of notes in the chord
               if(chordIndex >= chordNotesToPlay.size())		
                  chordIndex = 0;
               int chordNote = myChord[chordIndex];
            
               chordTracking = playNote(chordNote, chordlength, velocity-velocitySoften, chordTracking, CHORDSCHNL, music);
               chordIndex++;
            }
         }
      //drop riff down one or two octaves - but mostly just one
         int drop = (int)(rand.nextDouble()*2)+1;	
         if(drop == 2 && rand.nextDouble() < .5)
            drop = 1;
         riffNotes = riffOctave(riffNotes, (-1)*drop);
      }
   
      int playChordOnN = (int)(rand.nextDouble()*2)+1;	//1 or 2	
      int noteLength = wholeNote/2;
   //play chord on every note (1), every even note (2), every third note (3), every fourth note (4)
      if(riffStrat == 2)		//riffStrategy 2, make durations consistent & have chords played behind them
      {  
         double randLength = rand.nextDouble();
         if(randLength < .05)	//5% of the time, make the duration 8th notes and don't play chords with them
         {
            noteLength = wholeNote/8;
            playChordOnN = 0;	
         }
         else
            if(randLength < .55)
            {
               noteLength = wholeNote/4;
               playChordOnN = (int)(rand.nextDouble()*2)+3;	//3 or 4
            }
            else
            {
               noteLength = wholeNote/2;
               playChordOnN = (int)(rand.nextDouble()*5);	//0-4
            }
      }
      int lastHarmonyNote = -1;
      velocity =(int)(rand.nextDouble()*31)+50;
      double addHarmony = rand.nextDouble();	
      boolean invert = false;				//should we invert the riff?
      if(rand.nextDouble() < .25)		//invert ther riff 25% of the time
         invert = true;
      int i2=0;								//index for riff (regular or inverted)
      for(int i=0; i<riffDuration.length; i++)
      {
         if(invert)
            i2 = (riffDuration.length-1) - i;
         else
            i2 = i;
         int noteTime = riffDuration[i];
         note = riffNotes[i2];
         int noteIndex = indexOfNote(note, scale);
         if (noteTime < 0)										//negative durations are rests
         {
            note = 0;											//make the note a rest
            noteTime *= (-1);									//set the duration back to a positive number
         }
         double variation = rand.nextDouble();
         if (note > 0)
         {
            if (variation < .05 && canWeDeviate)			//5% of the time, allow for variation in the riff
            {
               note = scale[(int)(rand.nextDouble()*scale.length)];  
            }
            else
               if (variation < .30 && canWeDeviate)		//25% of the time, make the note between 4 notes (up or down) in the scale
               {														//instead of the root, play the 4th or 5th
                  if(isRoot(note, scale))							//bump the root up to the 4th or the 5th
                  {
                     int altIndex = (int)(rand.nextDouble()*2)+4;	//index of alternate note (4 or 5)
                     if(noteIndex+altIndex < scale.length && noteIndex+altIndex>=0)
                        note = scale[noteIndex+altIndex];
                  }
                  else												//instead of the 4th, play the root or 5th
                     if(isFourth(note, scale))							//bump the root up to the 4th or the 5th
                     {
                        int altIndex = (int)(rand.nextDouble()*2)-4;	//index of alternate note (-4 or 1)
                        if(altIndex==-3)
                           altIndex = 1;
                        if(noteIndex+altIndex < scale.length && noteIndex+altIndex>=0)
                           note = scale[noteIndex+altIndex];
                     }
                     else											//instead of the 5th, play the root or 4th
                        if(isFifth(note, scale))							//bump the root up to the 4th or the 5th
                        {
                           int altIndex = (int)(rand.nextDouble()*2)-1;	//index of alternate note (-1 or -5)
                           if(altIndex==0)
                              altIndex = -5;
                           if(noteIndex+altIndex < scale.length && noteIndex+altIndex>=0)
                              note = scale[noteIndex+altIndex];
                        }
                        else
                        {
                           int diff = (int)(rand.nextDouble()*9)-4;//pick a value between -4 and 4 to shift the modified note up or down in the scale
                           while(noteIndex+diff < 0 || noteIndex+diff>=scale.length)	//keep picking until it is a valid index
                              diff = (int)(rand.nextDouble()*9)-4;
                           note = scale[noteIndex+diff];				//change the note to one that is within 4 (up or down) in the scale
                        }
               }
         }    
         int harmonyNote = 0;
         int[] harmNotes = null;
         if(note > 0)
         {
            harmonyNote = note+OCTAVE;
            harmNotes = Scale.addOctave(getHarmonizeNotes(note, scale));    
         }
         if (harmNotes != null)	
         {
            harmonyNote = harmNotes[(int)(rand.nextDouble()*harmNotes.length)];	//pick a random note from the set of harmony notes
            if(i > 0)		//find a note in the set that is the closest to the last harmony note
            {
               int closestIndex=0;
               int minDiff = 999;
               for(int c=0; c < harmNotes.length; c++)
                  if(Math.abs(harmNotes[c] - lastHarmonyNote) < minDiff && harmNotes[c] != lastHarmonyNote)
                  {
                     minDiff = Math.abs(harmNotes[c] - lastHarmonyNote);
                     closestIndex = c;
                  }
               int harmDir = (int)(rand.nextDouble()*3)-1;	//-1, notes fall.  0, notes vary.  1, notes rise.	  
               if(harmDir==-1)				//harmony notes will fall
               {
                  if(harmNotes[closestIndex] < lastHarmonyNote)
                     harmonyNote = harmNotes[closestIndex];
                  else
                     if(closestIndex-1 >=0)
                        harmonyNote = harmNotes[closestIndex-1];
                     else
                        harmonyNote = harmNotes[closestIndex];
               }
               else								//harmony notes will float around the middle
                  if (harmDir==0 && note != harmNotes[closestIndex])
                     harmonyNote = harmNotes[closestIndex];
                  else
                     if(harmDir==1)			//harmony notes will rise
                     {
                        if(harmNotes[closestIndex] > lastHarmonyNote)
                           harmonyNote = harmNotes[closestIndex];
                        else
                           if(closestIndex+1 < harmNotes.length)
                              harmonyNote = harmNotes[closestIndex+1];
                           else
                              harmonyNote = harmNotes[closestIndex];
                     }
            }
         }	
         if (note==0 || riffStrat == 2)
            harmonyNote = 0;	//no need for a harmony note if there is no note or we are going to play a chord behind it
         boolean playOctave = false;
         int octave = 1;
         if(rand.nextDouble() < .5 && addHarmony >= harmonize)	//50% time we are not playing a harmony note as well, play an octave of the riff
         {
            playOctave = true;
            if(note+OCTAVE*octave >= 120)							//if our note + an octave is higher than the max note, make the octave one lower
               octave = -1;
         }
         int octNote = forceNoteInRange(note+OCTAVE*octave,scale);
         if (note <= 0)
            octNote = 0;
         int velocityChange = (int)(rand.nextDouble()*11) + 5;
         int velocityChange2 = (int)(rand.nextDouble()*16) + 10;
         if(riffStrat==2)
            noteTime = noteLength;
         if (riffStrat == 0 && addHarmony < harmonize)  
            playNote(harmonyNote, noteTime, velocity-velocityChange2, where, HARMONYCHNL, harmonyNotesPlayed, music);  
         if (playOctave)  
            playNote(octNote, noteTime, velocity-velocityChange, where, BASSCHNL, bassNotesPlayed, music); 
         if (riffStrat == 2 && playChordOnN != 0 && i % playChordOnN == 0)
         {//riffStrategy 2, make durations consistent & have chords played behind them
            playChord(CHORDSCHNL, null, null, note, where, -1, -1, noteLength, velocity - velocityChange, 0, 0, music);    
         //playChord(int chnl, int[]forceChord,  int note, int where, int forceChordGroup, int forceChordIndex, int forceChordLength, int forceVelocity, boolean forceArp, int octave, Track music)   
         }
         where = playNote(note, noteTime, velocity, where, MELODYCHNL, melodyNotesPlayed, music);  
         lastHarmonyNote = harmonyNote;
      }	//end picking of a melody note
      if(riffStrat==3)
      {  
         for(int m=0; m<melodyNotes2.length; m++)		//COUNTER MELODY
         {
            int ourNote=melodyNotes2[m];
            int melNote = noteHasBeenPlayed(riffTracking2, melodyNotesPlayed);		//the main melody note that we might have to harmonize with
            int[]harmnotes = null;
         
            if(melNote != 0)
            {
               harmnotes = getHarmonizeNotes(melNote, scale);
               ourNote =  closestNoteInChord(ourNote, harmnotes, harmnotes.length);  
            }
            int velocitySoften = (int)(rand.nextDouble()*16)+5;	//soften the chord velocity between 5 and 20   
            riffTracking2 = playNote(ourNote, Math.abs(riffDuration2[m]), velocity-velocitySoften, riffTracking2, HARMONYCHNL, harmonyNotesPlayed, music);  
         }
      }
   //******for tempo change******
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //****************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(note, scale);
      returnVals[2] = note;
      return returnVals;
   }

//creates and plays a musical phrase at time 'where' with some note or chord accompanyment
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playPhrase(int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int [] returnVals = new int[3];
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean speedUp = false;			//50% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//50% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;	//how much the tempo changes by each step (1-8)  
   
      if(rand.nextDouble() < .5)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
   
      int[]riffDuration = makeRiffDurations(wholeNote * 2, true, -1, .25, -1);
   //int[]riffNotes = makeMelodyNotes(riffDuration, -1, -1);
      ArrayList<int[]> melodySets = makeMelodyNotes(riffDuration, -1, -1, -1, -1);
      int[]riffNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));//the last index is excluded because it is durations (in case they change)
      
      int riffStrat = (int)(rand.nextDouble()*2);	//0-full chord, 1-chord with mostly riff notes
      int firstNote = riffNotes[0];
      int[]myChord = null;
      Chord theChord = null;
      int chordIndex = 0;	//to traverse through each note in myChord to play them one at a time and alternate
      int riffTime = 0;		//total length of the phrase
      for(int i=0; i < riffDuration.length; i++)
         riffTime += Math.abs(riffDuration[i]);  
      
      boolean sameRiff = true;		
      boolean sameChords = true;
      ArrayList ourChords = getRiffChords(riffNotes, chordSetsA);		//set of chords that have the most notes in common with riffNotes
      int randIndex = (int)(rand.nextDouble()*ourChords.size());
      int[]chord = ((Chord)(ourChords.get(randIndex))).getNotes();	//get a random chord out of the set of chords that have the most notes in common with the riff
   	
      int [] commonNotes = getRiffNotesInChord(riffNotes, chord);
   
      if(rand.nextDouble() < .5)
         sameRiff = !sameRiff;
      if(rand.nextDouble() < .5)
         sameChords = !sameChords;
      int numTimes = (int)(rand.nextDouble()*3)+1;
      int prevNote = -1;								//last note of the previous melody subset  
      for(int times=0; times<numTimes; times++)
      {  
         if(times==1 && !sameRiff)
         {
         //riffNotes = makeMelodyNotes(riffDuration, -1, -1);
            ArrayList<int[]> melodySets2 =makeMelodyNotes(riffDuration, -1, -1, -1, prevNote);
            riffNotes = melodySets2.get((int)(rand.nextDouble()*melodySets2.size()-1));//the last index is excluded because it is durations (in case they change)
            firstNote = riffNotes[0];
            prevNote = riffNotes[riffNotes.length-1];
         }
         if(times==1 && !sameChords)
         {	//get a different chord that has firstNote in it
            if(noteInChord(firstNote, scale, scale.length))
            {//and make sure firstNote is in the scale, unlike a tritone
               if (rand.nextDouble() < altChords)
               {
                  theChord = getChordThatHasNote(firstNote, chordSetsB);
                  if(theChord != null)
                     myChord = theChord.getNotes();		//the alternate chord to play
               }
               else
               {
                  theChord = getChordThatHasNote(firstNote, chordSetsA);
                  if(theChord != null)
                     myChord = theChord.getNotes();		//the common chord to play
               }
            }
         }                 
         if(riffStrat==0 && chordIndex != -1)	//pick a chord that has the first riff note in it
         {	//get a chord that has firstNote in it
            if(noteInChord(firstNote, scale, scale.length))
            {//and make sure firstNote is in the scale, unlike a tritone
               if (rand.nextDouble() < altChords)
               {
                  theChord = getChordThatHasNote(firstNote, chordSetsB);
                  if(theChord != null)
                     myChord = theChord.getNotes();		//the alternate chord to play
               }
               else
               {
                  theChord = getChordThatHasNote(firstNote, chordSetsA);
                  if(theChord != null)
                     myChord = theChord.getNotes();		//the common chord to play
               }
            }
         //the common chord to play
         }                             
         else
            if(riffStrat==1)
            {	
               ArrayList chordList = getRiffChords(riffNotes, chordSetsA);
                        	
               Set<Integer> chordNotesToPlay = new HashSet();             
            //pick a random chord out of the set of chords that have the most riff notes in common   
            
               randIndex = (int)(rand.nextDouble()*chordList.size());
               theChord = ((Chord)(chordList.get(randIndex)));
               chord = theChord.getNotes();    
            
               int numNotes = (int)(rand.nextDouble()*chord.length-1)+1;	//we will pick at least 1 chord note to play
               commonNotes = getRiffNotesInChord(riffNotes, chord);
               if(commonNotes == null)	//if there are no common notes, make our notes to play the root and its octave
               {
                  chordNotesToPlay.add(scale[0]);
                  chordNotesToPlay.add(scale[0]+OCTAVE);
               }
               else							//pick some random notes from commonNotes
               {
                  for(int i=0; i < numNotes; i++)
                  {
                     if(numNotes > commonNotes.length && chordNotesToPlay.size() == commonNotes.length)
                        break;
                     randIndex = (int)(rand.nextDouble()*commonNotes.length);
                     while(chordNotesToPlay.contains(commonNotes[randIndex]))
                        randIndex = (int)(rand.nextDouble()*commonNotes.length);
                     chordNotesToPlay.add(commonNotes[randIndex]);
                  }
                  if(chordNotesToPlay.size()==1 && rand.nextDouble()<.5)	//if there is only one note to play with our riff, then
                  {
                     if(rand.nextDouble() < .5)				//25% of the time we will add a rest after each note
                        chordNotesToPlay.add(0);
                     else
                     {
                        for(Integer x:chordNotesToPlay)	//25% of the time we will add an octave of our one note
                           chordNotesToPlay.add(x+OCTAVE);
                     }
                  }   
               }
            //Pick alternating notes from chordNotes to play at a constant duration for the length of the entire riff
               int chIndex = 0;		 
               myChord = new int[chordNotesToPlay.size()];
               for(Integer x:chordNotesToPlay)
                  myChord[chIndex++] = x;
            }
      
         if(myChord != null)
         {
            int chIndex = 0;
            int chordTracking = where;
            int chordlength;
            double chordSize = (int)(rand.nextDouble());
            if(chordSize < .45)												//45% of the time, our chord is a quarter note
               chordlength = wholeNote/4;
            else
               if(chordSize < .90)
                  chordlength = wholeNote/8;								//45% of the time, our chord is an eigth note
               else
                  chordlength = wholeNote/2;								//10% of the time it is a half note
            int runTime = where + riffTime;  
            int velocitySoften = (int)(rand.nextDouble()*16)+5;	//soften the chord velocity between 5 and 20
         
            int backIndex = myChord.length -1;							//index to traverse the chord backwards
            double chordDir = rand.nextDouble();						//direction that we run through the chord notes
            int cdex = 0;
            boolean playFullChords = (commonNotes!=null && commonNotes.length >= 3 && rand.nextDouble() < .75);
            while(chordTracking < runTime)
            {
            //******for tempo changes******
               if(willWeChangeTempo)
               {   
                  if(speedUp)
                  {
                     newTempo += tempoDelta;
                     setTempo(newTempo, chordTracking, music);
                  }
                  else
                     if(slowDown)
                     {
                        newTempo -= tempoDelta;
                        setTempo(newTempo, chordTracking, music);
                     }
                  if(newTempo > tempo*3)		//too fast..slow things down
                  {
                     speedUp = false;
                     slowDown = true;
                  }	
                  else
                     if(newTempo < tempo / 3)			//too slow...speed things up
                     {
                        speedUp = true;
                        slowDown = false;
                     }
               }
            //***********************************
            
               if(playFullChords)											//if we have enough notes for a full chord
               {
                  double arpChord = 0;  
                  if(rand.nextDouble() < .25)
                     arpChord = 1.0;
                  chordTracking += playChord(CHORDSCHNL, theChord, myChord, -1, chordTracking, -1, -1, chordlength, velocity-velocitySoften, arpChord, 0, music);
               }
               else
               {
                  if(backIndex < 0)
                     backIndex = myChord.length - 1;
                  if(chIndex >= myChord.length)		//reset chordIndex to wrap around back to the beginning of the set of notes in the chord
                     chIndex = 0;
                  if(chordDir < .5)						//50% of the time, we roll our chord backwards
                     cdex = backIndex;
                  else
                     cdex = chIndex;
                  int chordNote = myChord[cdex];
               
                  chordTracking = playNote(chordNote, chordlength, velocity-velocitySoften, chordTracking, CHORDSCHNL, music);
                  chIndex++;
                  backIndex--;   
               }
            }
         }
      //drop or raise phrase one or two octaves - but mostly just one
         int octaveChange = (int)(rand.nextDouble()*4)-2;  	//change to either raise or drop -2,-1,1
         while(octaveChange == 0)
            octaveChange = (int)(rand.nextDouble()*4)-2;
         riffNotes = riffOctave(riffNotes, octaveChange);
         int octave = (int)(rand.nextDouble()*3)-1;			//-1,0,1   
      
         velocity =(int)(rand.nextDouble()*31)+50;
         boolean invert = false;				//should we invert the riff?
         if(rand.nextDouble() < .25)		//invert ther riff 25% of the time
            invert = true;
         
         boolean playOctave = false;    
         if(rand.nextDouble() < .5)			
            playOctave = true;
         
         int i2=0;								//index for riff (regular or inverted)
         for(int i=0; i<riffDuration.length; i++)
         {
            if(invert)
               i2 = (riffDuration.length-1) - i;
            else
               i2 = i;
            int noteTime = riffDuration[i];
            int note = riffNotes[i2];
            if (noteTime < 0)										//negative durations are rests
            {
               note = 0;											//make the note a rest
               noteTime *= (-1);									//set the duration back to a positive number
            }
                    		
            if(i==riffDuration.length-1 && myChord!=null) //make the last note in the riff the same as one in the chord
               note = closestNoteInChord(note,myChord,myChord.length);
            note = forceNoteInRange(note, scale);
            int octNote = forceNoteInRange(note+OCTAVE*octave, scale);
            if (note <= 0)
               octNote = 0;
            int velocityChange = ((int)(rand.nextDouble()*11) + 5)*(-1);
            if(rand.nextDouble() < .25 && velocity + Math.abs(velocityChange) < 95)
               velocityChange *= -1;
            if(playOctave)
               playNote(octNote, noteTime, velocity+velocityChange, where, BASSCHNL, bassNotesPlayed, music);  
            where = playNote(note, noteTime, velocity, where, MELODYCHNL, melodyNotesPlayed, music);  
         }	//end picking of a melody note
      }
   //******for tempo change******
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //****************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(note, scale);
      returnVals[2] = note;
      return returnVals;
   }

//note is the current note before call
//plays a series of n rolling arpeggio chords with a bass melody behind it
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playRollingChords(int note, int[]riffNotes, int[] chordGroupIndexes, int[] chordIndexes, int where, int octave, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      boolean didWeCutNoteLength = false;	//flag to see if we chopped the bass note length for adding a bass note half way inbetween a rolling chord
   
      int[] returnVals = new int[3];
      boolean mixOrderOnInterval = false;
      if(rand.nextDouble() < .5)
         mixOrderOnInterval = true;
      int[]orders = {1, 2, 3, 4, 6};							//1-always random, 2to6-order every nth chord
      int orderInterval = orders[(int)(rand.nextDouble()*orders.length)];
      int whichOrder = (int)(rand.nextDouble()*5) + 1;	//1-sorted		2-reversed		3-random		4-shuffled
   
    //******for adding a bass line or struck chord
      double whichAugment = rand.nextDouble();
      boolean bassLine = false;		//do we want to do a full bass line or octaves every so many notes?
      boolean strikeChord = false;	//do we want to strike and hold a chord while we roll individual notes?
      if(whichAugment < .33)
         bassLine = true;
      else    
         if(whichAugment < .66)
            strikeChord = true;
   
      int bassStart = where;
   //******************************
   
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      int transitionPart = (int)(rand.nextDouble()*3)+2;	//this is the partition for when we transitin back:2,3,4 - transition in 2nd half, last 3rd or last 4th
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//60% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//40% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
   
      if(rand.nextDouble() < .4)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
      if(chordHasBeenPlayed(where, chordPlayed)!=null)//if a chord has already been played there, stop
      {
         returnVals[0] = where;
         returnVals[1] = indexOfNote(note, scale);
         returnVals[2] = note;
         return returnVals;
      }
      int velocitySoften = (int)(rand.nextDouble()*21);//soften the chord velocity between 0 and 20 
      double whichLength = rand.nextDouble();
      int rollDelayTemp = wholeNote/8;
      if (whichLength < .45) 
         rollDelayTemp = wholeNote/8;						//80% of the time, eighth notes
      else 
         if(whichLength < .9)										//20% of the time, sixteenth notes
            rollDelayTemp = wholeNote/16;
         else //if(whichLength < .9)
         {
            rollDelayTemp = wholeNote/32;
            if(willWeChangeTempo && rand.nextDouble() < .75)
            {																//we'll slow down here
               speedUp = false;
               slowDown = true;
            }
         }
      boolean dottedDurations = false;
      if(timeSig==3 || rand.nextDouble() < .25)		//dotted note duration
      {
         rollDelayTemp += (rollDelayTemp / 2);
         dottedDurations = true;
      }
      int rollType = (int)(rand.nextDouble()*6);	//0-just roll up or down, 1-just roll down, 2-roll up and down, 3-roll up whole chord, then roll up last 2 or 3 notes, 4-same as 3, but last 2 or 3 notes are a constant sequence
   															//5-roll chords down the piano, from highest octave of the first chord, and the next chord being the closest note in the chord to the last chord note played
      if(timeSig==3 || rand.nextDouble() < .25)		//dotted note duration
         rollDelayTemp += (rollDelayTemp / 2);
      int type3back = 0;										//for roll type 3, this is the number of notes we roll back to after we roll up the chord
      int rollStrat = (int)(rand.nextDouble()*3);		//do we want to roll chords off the riff (0), pick notes(1) or use chord theme chords(2)
      int numChords = (int)(rand.nextDouble()*6) + 1;	//1 to 6 rolling chords         
      int j =  indexOfNote(note, scale);					//find the index of the note
      if(j == -1)													//if we can't find the note in the scale, pick a random index
         j = (int)(rand.nextDouble()*scale.length);
   
      int numTimes = numChords;
      if(rollStrat ==0 )										//we want to roll chords off the riff (0)
      {
         numTimes = riffNotes.length; 
         if (hasBeenDoubled(riffNotes) || numTimes > 12)
            numTimes /= 2;
      }
      else
         if(rollStrat == 2)									//we want to use chord theme chords(2)
            numTimes = chordGroupIndexes.length;
      int numNotesToPlay = (int)(rand.nextDouble()*13) + 3; //the number of notes in the rolling chord to play 3-15		
      int[]numNotes={3,3,4,4,6,8,9,12,15,15};
      if(rand.nextDouble() < .75)									//which will usually be a mult of 3 or 4
      {
         numNotesToPlay = numNotes[(int)(rand.nextDouble()*numNotes.length)];
         if(timeSig==3)
            numNotesToPlay = 3 * ((int)(rand.nextDouble()*4)+1);		//for 3/4 time, make it a 3,6,9 or 12
         else
            if(timeSig%2==0)
               numNotesToPlay = 4 * ((int)(rand.nextDouble()*3)+1);	//for 3/4 time, make it a 3,6,9 or 12
      }
      int[] type4notes = null;		//constant note sequence we end on for each chord in rollType 4
      if(rollType == 3  || rollType == 4)
      {
         int which = (int)(rand.nextDouble()*5);
         switch(which)
         {
            case 0:	numNotesToPlay = 6;
               type3back = 2;		
               break;
            case 1:	numNotesToPlay = 5;
               type3back = 3;		
               break;
            case 2:	numNotesToPlay = 4;
               type3back = 4;		
               break;
            case 3:	numNotesToPlay = 4;
               type3back = 2;		
               break;
            default:	numNotesToPlay = 3;
               type3back = 3;		
               break;
         }
      //most of the time, a 6 note rolled chord and a made bass line are ill timed
         if(bassLine && numNotesToPlay + type3back == 6 && rand.nextDouble() < .98)
            bassLine = false;
         type4notes = new int[type3back];
         int whichOne = (int)(rand.nextDouble()*8);
      //0-last notes of root chord		1-last notes of random chord
      //2-random notes of root chord	3-random notes of any chord
      //4-random notes from scale		5-random notes from melody
      //6-first notes from melody		7-last notes from melody
         if(whichOne >= 4)
            for(int i=0; i<type3back; i++)
            {
               if(whichOne==5)
                  type4notes[i] = riffNotes[(int)(rand.nextDouble()*riffNotes.length)];
               else if(whichOne==6 && i<riffNotes.length)
                  type4notes[i] = riffNotes[i];
               else if(whichOne==7 && (riffNotes.length-1-i)>=0)
                  type4notes[i] = riffNotes[(riffNotes.length-1-i)];
               else//if(whichOne==4)
                  type4notes[i] = scale[(int)(rand.nextDouble()*scale.length)];
            }
         else	//whichNote is 0,1,2,3
         {
            int whichGroup = 0;
            int whichChord = 0;
            if(whichOne ==1 || whichOne == 3)
               whichGroup = (int)(rand.nextDouble()*chordSetsA.length); 
            whichChord = getRandChordInfo(chordSetsA,whichGroup);
            int[] ch = ((Chord)(chordSetsA[whichGroup].get(whichChord))).getNotes();
            for(int i=0; i<type3back; i++)
            {
               if((whichOne ==0 || whichOne == 1) && (ch.length-1-i)>=0)
                  type4notes[i] = ch[(ch.length-1-i)];
               else
                  type4notes[i] = ch[(int)(rand.nextDouble()*ch.length)];
            }         
         }
      //now pick orders - sorted, reversed, random, shuffled  
         whichOne = (int)(rand.nextDouble()*5);
      //0-unchanged	1-sorted		2-reversed		3-random		4-shuffled
         if(whichOne == 1)
            selSort(type4notes);
         else  
            if(whichOne == 3)
               scramble(type4notes);
            else
               if(whichOne == 2)
                  type4notes = reverse(type4notes);
               else
                  if(whichOne == 4)
                     type4notes = shuffle(type4notes);
      }
      int noteLength = numNotesToPlay*rollDelayTemp;
      int origNoteLength = noteLength;		//record original note length for situations where we cut the bass note length in half for bass notes added half way through a rolling chord
      int[]chord = null;
      int[]lastChord = null;						//the previous chord played
   //order type for rollType 5
      int orderType = (int)(rand.nextDouble() * 6);	//0-unchanged, 1-reversed, 2-sorted, 3-sorted/reversed, 4-shuffled, 5-scrambled
      for(int i=0; i < numTimes; i++)
      {
         lastChord = chord;  
      //******for tempo changes******
         if(willWeChangeTempo)
         {   			//within the last 1/transitionPart of the events 
            if(i >= numTimes - (numTimes/transitionPart) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first so many events, or not transitioning back 
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(newTempo > tempo*4)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(newTempo < tempo / 4)			//too slow...speed things up
               {
                  speedUp = true;
                  slowDown = false;
               }
         }
      //***********************************
      //we only want to build off a riff note if it is in the scale (might be a tritone) and its not a rest
         if(rollStrat ==0 && noteInChord(riffNotes[i], scale, scale.length) && riffNotes[i] > 0)
            note = riffNotes[i];						//do we want to roll chords off the riff (0)
         else 
            if(rollStrat == 2)						//note = root of current chord
               note = (((Chord)chordSetsA[chordGroupIndexes[i]].get(chordIndexes[i])).getNotes())[0];
            else
               note = scale[j];						//we want to roll chords off the picked notes(1)
         Chord theChord = null; 
         if(rollStrat < 2)								//we want to roll chords off the riff (0) or pick notes(1)
         {  
            if (rand.nextDouble() < altChords)
            {
               theChord = getChordThatHasNote(note, chordSetsB);
               if(theChord != null)
                  chord = theChord.getNotes();	//the alternate chord to play
            }
            else											
            {
               theChord = getChordThatHasNote(note, chordSetsA); 
               if(theChord != null)
                  chord = theChord.getNotes();	//the common chord to play 
            }
         }
         else	//if rollStrat == 2					//we want to use chord theme chords(2)
         {
            theChord = (Chord)chordSetsA[chordGroupIndexes[i]].get(chordIndexes[i]);
            if(theChord != null)
               chord = theChord.getNotes();		
         }
         if(chord!=null)
         {
            if(theChord != null)
            { 												//write chord name into MIDI file              
               String text = theChord.getName();
               addEvent(music, TEXT, text.getBytes(), where);
            }   
            chordPlayed.put(where, chord);		//we will play a chord, so add it to the Map of chords played
            chord = forceChordInRange(expandChord(chord, numNotesToPlay));  
            if(mixOrderOnInterval && i%orderInterval == 0)
            {	//1-sorted		2-reversed		3-random		4-shuffled
               if(orderInterval == 1)				//order could be any of the following
                  whichOrder = (int)(rand.nextDouble()*5);	//whichOrder of 0 is unchanged
               if(whichOrder==1)						//make some of the chords roll notes in order
                  selSort(chord);
               else 
                  if(whichOrder==2)				//make some of the chords roll notes in reverse order
                     chord = reverse(chord);
                  else 
                     if(whichOrder==3)				//make some of the chords roll notes in shuffled order
                        chord = shuffle(chord);   
                     else 
                        if(whichOrder==4)				//make some of the chords roll notes in random order
                           scramble(chord);
            }
            int chordlength = wholeNote/4;
         
            int slop = wholeNote/16;				//amount added to the duration of each individual rolled chord note				
            if(rand.nextDouble() < .5)
               slop = chordlength/((int)(rand.nextDouble()*4)+1);
               		
            if(chord != null)   
            {     	
               //if we will roll a chord, pick a random note from that chord (dropped an octave) to hold as the chord is rolled
               //System.out.println("Picked chord "+chordIndex+" of "+chords.length);	
               double willWeChangeOnDown = rand.nextDouble();	//change to a different chord on downSweep?
               int octNote = forceNoteInRange(note-OCTAVE*2,scale);
               if(rollType == 2 && rand.nextDouble() < willWeChangeOnDown)		//we will play the next riff note on the down arp
               {
                  noteLength /= 2;
                  didWeCutNoteLength = true;
               } 
               else if(didWeCutNoteLength)
               {
                  didWeCutNoteLength = false;
                  noteLength = origNoteLength;
               } 															//so cut the duration in half
               int velocityChange = (int)(rand.nextDouble()*6) + 5;
               if(rand.nextDouble() < .25 || velocityChange + velocity > 95)
                  velocityChange *= -1;
               if(bassLine == false)
               {
                  playNote(octNote, noteLength, velocity+velocityChange, where, BASSCHNL, bassNotesPlayed, music);
               //lets get the previous bass note.  If it is two steps away from the current note, 
               //put an intermediate note between the two
                  Integer lastBassNote = bassNotesPlayed.get(where - noteLength);
                  if(lastBassNote != null)
                  {
                     int octNoteIndex = indexOfNote(octNote, scale);
                     int lastBassNoteIndex = indexOfNote(lastBassNote, scale);
                     if(Math.abs(octNoteIndex-lastBassNoteIndex) == 2)
                     {
                        int middleIndex = 0;
                        if(octNoteIndex < lastBassNoteIndex)
                           middleIndex = octNoteIndex + 1;
                        else
                           middleIndex = lastBassNoteIndex + 1;
                        int middleNote = forceNoteInRange(scale[middleIndex] - (OCTAVE*2),scale);
                        int middleTracking = (where + (where - noteLength))/2;
                        playNote(middleNote, noteLength/2, velocity+velocityChange, middleTracking, BASSCHNL, bassNotesPlayed, music);
                     }
                  }
               }  
               int flareChord = 1;
            
               if(strikeChord)
               {
                  int [] holdChord = chord.clone();
                  holdChord = limitChord(holdChord,1);
                  for(int y = 0; y < holdChord.length; y++)
                     playNote(holdChord[y], chordlength,  velocity-Math.abs(velocityChange), where, CHORDSCHNL, music);
               }                           
            //*******TESTING********************
            //rollType = 5;
            //orderType = 3;
            //*********************************/
               if(rollType == 5)	//roll chords down the piano, from highest octave of the first chord, and the next chord being the closest note in the chord to the last chord note played
               {
                  int [] nextChord = chord;
                  if( i == 0 )	//1st chord - make as high an octave as possible
                     nextChord = highestOctave(chord);
                  else
                     nextChord = nextOctave(chord, lastChord);
                                   //0-unchanged, 1-reversed, 2-sorted, 3-sorted/reversed, 4-shuffled
                  if(orderType == 1)
                     nextChord = reverse(nextChord);
                  else if(orderType == 2)
                     selSort(nextChord);	
                  else if(orderType == 3)
                  {
                     selSort(nextChord);	
                     nextChord = reverse(nextChord);
                  }
                  else if(orderType == 4)
                     nextChord = shuffle(nextChord); 
                  else if(orderType == 5)
                     scramble(nextChord); 
               
                  int slopTemp = slop;	
                  for(int y = numNotesToPlay-1; y>=0; y--)					 
                  {  //on the last two notes, decrease slop so that it doesn't spill over the next event
                     if(y < 2)	
                        slopTemp /= 2; 
                     int chordNote = forceNoteInRange(nextChord[y]+OCTAVE*octave,scale);
                     //start each successive note in the chord a little later for a flared chord
                     playNote(chordNote, chordlength + slopTemp, velocity-velocitySoften, where+(y*rollDelayTemp), CHORDSCHNL, music);
                  }
                  where += ((numNotesToPlay)*rollDelayTemp);
               }
               else
                  if (rollType == 0 || rollType == 2  || rollType == 3  || rollType == 4)	   //0-just roll up or down, 2-roll up and down
                  {																									//3-roll up whole chord, then roll up last 2 or 3 notes
                     int slopTemp = slop;	
                     boolean justUp = true;		//for roll type 0, do we just want to roll notes up or down?
                     if(rollType == 0)
                     {
                        if(rand.nextDouble() < .5)
                           justUp = false;
                     }  
                     if (justUp == false)			//just roll notes down
                     {
                        for(int y = numNotesToPlay-1; y>=0; y--)					 
                        {  //on the last two notes, decrease slop so that it doesn't spill over the next event
                           if(y < 2)	
                              slopTemp /= 2; 
                           int chordNote = forceNoteInRange(chord[y]+OCTAVE*octave,scale);
                        //start each successive note in the chord a little later for a flared chord
                           playNote(chordNote, chordlength + slopTemp, velocity-velocitySoften, where+(y*rollDelayTemp), CHORDSCHNL, music);
                        }
                     }
                     else								//roll notes up														
                        for(int y = 0; y<numNotesToPlay; y++)					 
                        {  //on the last two notes, decrease slop so that it doesn't spill over the next event
                           if(y >= numNotesToPlay-2)	
                              slopTemp /= 2; 
                           int chordNote = forceNoteInRange(chord[y]+OCTAVE*octave,scale);
                        //start each successive note in the chord a little later for a flared chord
                           playNote(chordNote, chordlength + slopTemp, velocity-velocitySoften, where+(y*rollDelayTemp), CHORDSCHNL, music);
                        }
                     where += ((numNotesToPlay)*rollDelayTemp);
                     if(rollType == 3 || rollType == 4)				//3-roll up whole chord, then roll up last 2 or 3 notes
                     {
                        int index = numNotesToPlay - type3back;	//index of which chord note we want to play
                        slopTemp = slop;
                        int type4index=0;									//index for constant sequence used for rollType 4
                        for(int y = 0; y<type3back; y++)					 
                        {  
                        //on the last two notes, decrease slop so that it doesn't spill over the next event
                           if(y >= type3back-2)	
                              slopTemp /= 2;
                           int ourNote = forceNoteInRange(chord[index++]+OCTAVE*octave,scale);
                           if(rollType == 4)								//make ourNote be the next in the constant sequence
                              ourNote = type4notes[type4index++];
                        //start each successive note in the chord a little later for a flared chord
                           playNote(ourNote, chordlength + slopTemp, velocity-velocitySoften, where+(y*rollDelayTemp), CHORDSCHNL, music);
                        }
                        where += ((type3back)*rollDelayTemp);
                     }
                  }
               if(rollType == 2 && rand.nextDouble() < willWeChangeOnDown && i < riffNotes.length - 1)  
               {																//2-roll up and down
                  octNote = forceNoteInRange(riffNotes[++i]-OCTAVE*2,scale);
                  if(rand.nextDouble() < .25 || velocityChange + velocity > 95)
                     velocityChange *= -1;
                  playNote(octNote, noteLength, velocity+velocityChange, where, BASSCHNL, bassNotesPlayed, music);
               }
               if(rollType == 1 || rollType == 2)					//1-just roll down, 2-roll up and down
               {
                  if (rollType == 2 && rand.nextDouble() < willWeChangeOnDown)	
                  {		//50% of the time, change the chord on the downsweep to another that has the same note
                     if(rollStrat < .5)
                     { 
                        if(noteInChord(riffNotes[i], scale, scale.length))
                           note = riffNotes[i];
                        else	//we have a tritone, so pick a random note to build on
                           note = scale[(int)(rand.nextDouble()*scale.length)];   
                     }
                     if (rand.nextDouble() < altChords)
                     {
                        theChord = getChordThatHasNote(note, chordSetsB);
                        if(theChord != null)
                           chord = theChord.getNotes();		//the alternate chord to play
                     }
                     else
                     {
                        theChord = getChordThatHasNote(note, chordSetsA);
                        if(theChord != null)
                           chord = theChord.getNotes();		//the common chord to play 
                     }
                     if(chord != null)
                        chord = forceChordInRange(expandChord(chord, numNotesToPlay));//with the same size as the previous chord   
                  }
                  if(chord != null)
                  {
                     if(strikeChord)
                     {
                        int [] holdChord = chord.clone();
                        holdChord = limitChord(holdChord,1);
                        for(int y = 0; y < holdChord.length; y++)
                           playNote(holdChord[y], chordlength, velocity-Math.abs(velocityChange), where, CHORDSCHNL, music);
                     }   
                     int slopTemp = slop;
                     for(int y = numNotesToPlay-1; y>=0; y--)					 
                     {  
                     //on the last two notes, decrease slop so that it doesn't spill over the next event
                        if(y <= 2)	
                           slopTemp /= 2;
                        int index = (chord.length-1)-y;
                        int chordNote = forceNoteInRange(chord[y]+OCTAVE*octave,scale);
                        playNote(chordNote, (chordlength + slopTemp), (velocity-velocitySoften), (where+(index*rollDelayTemp)), CHORDSCHNL, music);
                     }
                     where += (numNotesToPlay*rollDelayTemp);
                  }
               }
            }
         }//end if(chordIndex != -1)        
                 
      //pick a new note so we can get a new chord
         int nextNote = (int)(rand.nextDouble()*6);// makes the next note within five notes of each other, up or down
         double upOrDown = rand.nextDouble();     	// will the next note go up or down?  
         double pickInRange = rand.nextDouble(); 	//should we pick the next note within a five step range?     
         if(pickInRange<noteRange)						//n% of the time, the next note will be within 5 steps of the previous
            if(upOrDown<.5)
            {
               if(j+nextNote < scale.length-1)
                  j+=nextNote;
               else
                  j-=nextNote;
            }
            else
            {
               if(j-nextNote > 0)
                  j-=nextNote;
               else
                  j+=nextNote;
            } 
         else														//30% of the time, the next note is a random note in the scale or mode
            j=(int)(rand.nextDouble()*scale.length);	//j is the note (the index of the notes array)
      }
     //******for adding a bass line
      if(bassLine)
      {	
         int oldVelocity = velocity;
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         velocity += velocityChange;
         int bassTime = where - bassStart;
         ArrayList<int[]> bassLineInfo =  makeBassLine(bassStart, bassTime, 0, 3, dottedDurations);
         int[] bassLineNotes = bassLineInfo.get(0);
         int[] bassLineDurations = bassLineInfo.get(1);
         playMelody(bassLineDurations, bassLineNotes, bassStart, 3, bassNotesPlayed, music);
         velocity = oldVelocity;
      }//****************************	
   
      if(rand.nextDouble() < .5)	//end on a run through the scale with the same noteLength
      {
         int numRiffNotes = (int)(rand.nextDouble()*(riffNotes.length-1))+1;
         if(numRiffNotes > 10 && rand.nextDouble() < .5)
            numRiffNotes /= 2;
         int[]riffSubset = new int[numRiffNotes];
         for(int i=0; i< riffSubset.length; i++)
            riffSubset[i] = riffNotes[i];
         int[]values = playRunThruScale(riffSubset, rollDelayTemp, true, where, music);
         where = values[0];	//returns these values so that following improvisations can follow off the last notes
         j = values[1];
         note = values[2];
      }
          //******for tempo change******
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //****************************   
      returnVals[0] = where;
      returnVals[1] = j;
      returnVals[2] = note;
      return returnVals;
   }

 //returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playRunThruScale(int[]melodyThemeNotes, int noteLength, boolean playHarm, int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {  
      int[] returnVals = new int[3]; 
   //run through the scale and accent the melody notes with bass octaves 
      int numJumpbackNotes = (int)(rand.nextDouble()*5);		//(0-4) number of notes to fall back down the scale when we hit the accent melody note
      boolean strikeChord = false;		//do we want to strike and hold a chord while we run through the scale?
      int whenToStrike = 0;				//when we strike a chord.
      int [] strikeTimes = {0,1,2,3,4,6,8};
      ArrayList<Integer> melodyTemp = new ArrayList();
      for(int i=0; i<melodyThemeNotes.length; i++)
         if(melodyThemeNotes[i] > 0)
            melodyTemp.add(melodyThemeNotes[i]);
      int[]melodyNotes = new int[melodyTemp.size()];	//all melody notes without rests (note 0)
      for(int i=0; i<melodyNotes.length; i++)
         melodyNotes[i] = melodyTemp.get(i); 
   
   //0-strike on melody notes	1,2,3,4,6,8-strike on nth note
      whenToStrike = strikeTimes[(int)(rand.nextDouble()*strikeTimes.length)];
      if (rand.nextDouble() < .5 && whenToStrike == 1)	//but less likely to be 1
         whenToStrike = strikeTimes[(int)(rand.nextDouble()*strikeTimes.length)];
   
      if(rand.nextDouble() < .5)
         strikeChord = true;
      int ourDuration = wholeNote/8;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean speedUp = false;			//33% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//33% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      double whichDuration = rand.nextDouble();
      if(whichDuration < .5)	 		
      {																//we'll slow down here
         double startDuration = rand.nextDouble();		//choose random starting duration
         if(startDuration < .5)
            ourDuration = wholeNote/16;
         else
            if(startDuration < .75)
               ourDuration = wholeNote/16 + wholeNote/32;
            else
               ourDuration = wholeNote/8;
         speedUp = false;
         slowDown = true;
      }
      else	//if(whichDuration < .66)			//we'll speed up here			
      {
         double startDuration = rand.nextDouble();
         if(startDuration < .5)
            ourDuration = wholeNote/8;
         else
            if(startDuration < .75)
               ourDuration = wholeNote/8 + wholeNote/16;
            else
               ourDuration = wholeNote/4;
      
         speedUp = true;
         slowDown = false;
      }
   //*************************
      if(noteLength > 0)
         ourDuration = noteLength;
      int slop = wholeNote/32;				//amount added to the duration of each individual rolled chord note				
      if(rand.nextDouble() < .5)
         slop = noteLength/((int)(rand.nextDouble()*4)+1);    
      int size = scale.length;
      if(rand.nextDouble() < .75)	//75% of the time, run through 2 octaves of the scale
         size *= 2;   
      int[]ourScale = expandChord(scale, size);
      if(rand.nextDouble() < .5)  	//50% of the time, start an octave lower
         ourScale = riffOctave(ourScale, -1);
      int start = -1;
      int end = 0;
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;	//how much the tempo changes by each step (1-8)  
      int numNotes = 0;
      int numMelNotes = melodyNotes.length; 					//with a doubled melody, make the scale run only go through the first full melody
      if (hasBeenDoubled(melodyNotes) || (numMelNotes > 20 && rand.nextDouble() < .5))
         numMelNotes /= 2;
      for(int r=0; r < numMelNotes; r++)
      {
         boolean melodyNoteHit = false;
         int slopTemp = slop;
         if((ourDuration >= wholeNote/4 && numNotes > 20 && rand.nextDouble() < .75) || (numNotes > 50 && rand.nextDouble() < .75))
            break;								//don't let these runs go to long of the notes are longer durations
         for(int t=start+1; t<ourScale.length; t++)
         {
            end = -99;
            if(r >= numMelNotes && rand.nextDouble() < .5)
               break;		//50% of the time when we go through all the riff notes, end the scale run
            int theNote = 0;
            if(r < numMelNotes && normalize(ourScale[t]) == normalize(melodyNotes[r]))
            {					//play harmony note
               theNote = forceNoteInRange(ourScale[t]-OCTAVE, scale);
               if(playHarm)
                  playNote(theNote, ourDuration*2, velocity, where, HARMONYCHNL,harmonyNotesPlayed,  music);
                        //playNote(ourScale[t]-OCTAVE*2, ourDuration*2, velocity, tracking, 3, bassNotesPlayed, music);
            //numNotes++;
               melodyNoteHit = true;
               r++;
               end = t;
            }
            note = ourScale[t];
            if(strikeChord)
            {  
               boolean timeToStrike = false;
               if (whenToStrike==0 && (r > 0 && r < melodyNotes.length && normalize(ourScale[t]) == normalize(melodyNotes[r])))
                  timeToStrike = true;
               else
                  if(numNotes!= 0 && whenToStrike!=0 && numNotes % whenToStrike == 0)
                     timeToStrike = true;   
               if(timeToStrike)
               {
                  ArrayList chords = null;
                  if(theNote > 0 && note > 0)
                     chords = getAllChordsThatHasNotes(theNote, note, chordSetsA);
                  else
                     if(note > 0)
                        chords = getAllChordsThatHasNote(note, chordSetsA);
                  Chord ourChord = (Chord)chords.get((int)(rand.nextDouble()*chords.size()));
                  int velocitySoften = (int)(rand.nextDouble()*16)+5;
                  playChord(ourChord, ourDuration, velocity-velocitySoften, where, CHORDSCHNL, music) ;     
               }
            }
         //on the last two notes, decrease slop so that it doesn't spill over the next event
            if(t >= ourScale.length - 2)	
               slopTemp /= 2;
            where = playNote(note, ourDuration+slopTemp, velocity, where, MELODYCHNL, melodyNotesPlayed, music) - slopTemp;	//subtract slopTemp from where because we want notes to bleed over a bit           
            numNotes++;
            if(willWeChangeTempo)
            {
               if(speedUp)
               {
                  newTempo += tempoDelta;
                  setTempo(newTempo, where, music);
               }
               else
                  if(slowDown)
                  {
                     newTempo -= tempoDelta;
                     setTempo(newTempo, where, music);
                  }
               if(newTempo > tempo*3)		//too fast..slow things down
               {
                  speedUp = false;
                  slowDown = true;
               }	
               else
                  if(newTempo < tempo / 3)//too slow...speed things up
                  {
                     speedUp = true;
                     slowDown = false;
                  }
            }
            if(end==t && t!=start)
               break;
         }
         slopTemp = slop;
         if (r < numMelNotes && end > 0)
         {
            if(ourDuration >= wholeNote/4 && numNotes > 20 && rand.nextDouble() < .75)
               break;								//don't let these runs go to long of the notes are longer durations
            int numTimes = 0, t=0;
            for(t=end-1; t>=0 && numTimes<numJumpbackNotes; t--)
            {
               if(r >= numMelNotes && rand.nextDouble() < .5)
                  break;							//50% of the time when we go through all the riff notes, end the scale run
               int theNote = 0;
               if(r < numMelNotes && normalize(ourScale[t]) == normalize(melodyNotes[r]))
               {										//play a harmony note
                  theNote = forceNoteInRange(ourScale[t]-OCTAVE, scale);
                  if(playHarm)
                     playNote(theNote, ourDuration*2, velocity, where, HARMONYCHNL,harmonyNotesPlayed,  music);
                  r++;
                  melodyNoteHit = true;
               //numNotes++;
               }
               note = ourScale[t];
               if(strikeChord)
               {  
                  boolean timeToStrike = false;
                  if (whenToStrike==0 && (r > 0 && r < melodyNotes.length && normalize(ourScale[t]) == normalize(melodyNotes[r])))
                     timeToStrike = true;
                  else
                     if(numNotes!= 0 && whenToStrike!=0 && numNotes % whenToStrike == 0)
                        timeToStrike = true;   
                  if(timeToStrike)
                  {
                     ArrayList chords = null;
                     if(theNote > 0 && note > 0)
                        chords = getAllChordsThatHasNotes(theNote, note, chordSetsA);
                     else
                        if(note > 0)
                           chords = getAllChordsThatHasNote(note, chordSetsA);
                     Chord ourChord = (Chord)chords.get((int)(rand.nextDouble()*chords.size()));
                     int velocitySoften = (int)(rand.nextDouble()*16)+5;
                     playChord(ourChord, ourDuration, velocity-velocitySoften, where, CHORDSCHNL, music) ;     
                  }
               }
             //on the last two notes, decrease slop so that it doesn't spill over the next event
               if(r >= melodyNotes.length - 2)	
                  slopTemp /= 2;
               where = playNote(note, ourDuration+slopTemp, velocity, where, MELODYCHNL, melodyNotesPlayed, music);
               numNotes++;
               if(willWeChangeTempo)
               {   
                  if(speedUp)
                  {
                     newTempo += tempoDelta;
                     setTempo(newTempo, where, music);
                  }
                  else
                     if(slowDown)
                     {
                        newTempo -= tempoDelta;
                        setTempo(newTempo, where, music);
                     }
                  if(newTempo > tempo*4)		//too fast..slow things down
                  {
                     speedUp = false;
                     slowDown = true;
                  }	
                  else
                     if(newTempo < tempo / 4)			//too slow...speed things up
                     {
                        speedUp = true;
                        slowDown = false;
                     }
               }
               numTimes++;
            }
            if(t >=0)
               start = t;
         }
         if(melodyNoteHit)
            r--;
      }
    //******for tempo change*****
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
    //**************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(note, scale);
      returnVals[2] = note;
      return returnVals;
   }

 //returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playRegularScaleRun(int[]melodyThemeNotes,int startNote, int bassDrop, int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int[] returnVals = new int[3];
      boolean bassLine = false;			//do we want to do a full bass line or octaves every so many notes?
      if(rand.nextDouble() < .5)
         bassLine = true;
      int bassStart = where;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      int transitionPart = (int)(rand.nextDouble()*3)+2;	//this is the partition for when we transitin back:2,3,4 - transition in 2nd half, last 3rd or last 4th
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//60% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//40% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)   
      if(rand.nextDouble() < .4)	 		
      {											//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else										//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
      int[]ourScaleRun = null;
      double scaleRunType = rand.nextDouble();
      if(rand.nextDouble() < .4)  		//40% of the time, make a standard scale run 
         ourScaleRun = 	makeScaleRun(-1, startNote, melodyThemeNotes);
      else	
         if(rand.nextDouble() < .7)		//30% of the time, make it two merged scale runs
            ourScaleRun =  makeCombinedScaleRun(-1);
         else									//30% of the time, make it two shuffled scale runs
         {
            int numNotes = (int)(rand.nextDouble()*10)+1;	//between 1 and 10 notes in the run
            int startN = startNote;		//start note for the scale run
            if(rand.nextDouble() < .5)	//50% of the time, let the method pick it
               startN = -1;
            int [] runA = makeScaleRun(numNotes, startN, melodyThemeNotes);
            startN = startNote;			//start note for the scale run
            if(rand.nextDouble() < .5)	//50% of the time, let the method pick it
               startN = -1;
            int [] runB = makeScaleRun(numNotes, startNote, melodyThemeNotes);
            if(rand.nextDouble() < .5)	//50% of the time, reverse runA
               runA = reverse(runA);
            if(rand.nextDouble() < .5)	//50% of the time, reverse runB
               runB = reverse(runB);
            ourScaleRun = shuffle(runA, runB);
         } 		 	
      int noteTime;
      if (rand.nextDouble() < .3)
         noteTime = wholeNote/4;			//30% of the time, 4th note for scale-run
      else
         noteTime = wholeNote/8;			//70% of the time, 8th note scale-run
                            
      double playChordsOnN = rand.nextDouble();		//should we play a chord on every Nth note?
      if(noteTime == wholeNote/4 && ourScaleRun.length>=8)
         playChordsOnN += .2;								//20% more likely for a long arp of quarter notes
      int playOnN = (int)(rand.nextDouble()*3)+2;	//the Nth note we play on (2-4)
      if (ourScaleRun.length % 3 == 0)
         playOnN = 3;
      else
         if (ourScaleRun.length % 2 == 0)
         {
            playOnN = 2;
            if(rand.nextDouble() < .5)
               playOnN += 2;
         }
      boolean rest = false;				//will one of the scale notes be a rest
      int restNote = 99;					//which note is the rest
      if(rand.nextDouble() < .25)
         rest = true;   
      if(rest)
      {
         int[]skip = {2,3,4};		//do we skip every 2,3 or 4th note?	
         restNote = skip[(int)(rand.nextDouble()*skip.length)];
      }	
      for(int u = 0; u<ourScaleRun.length; u++)
      {   
      //******for tempo changes******
         if(willWeChangeTempo)
         { 				//within the last 1/transitionPart of the events 
            if(u >= ourScaleRun.length - (ourScaleRun.length/transitionPart) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first so many events, or not transitioning back   
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(noteTime == wholeNote/4 && newTempo > tempo*4)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(noteTime == wholeNote/8 && newTempo > tempo*2)		//too fast..slow things down
               {
                  speedUp = false;
                  slowDown = true;
               }	
               else
               
                  if(newTempo < tempo / 4)			//too slow...speed things up
                  {
                     speedUp = true;
                     slowDown = false;
                  }
         }
      //***********************************
      
         note = ourScaleRun[u];
                  /*********************************************************************************
                  if we are supposed to play a chord at every chordInterval AND the next note will take us over the next chordInterval
                  THEN play a chord at that next chordInterval */
         if(noteTime >= wholeNote/4)
            if(chordTime==0 && ((where + noteTime)/chordInterval) >  (where/chordInterval))
               playChord(CHORDSCHNL,null,null,  note, nextMultOfInterval(where, chordInterval), -1, -1, noteTime*2, velocity,forceArp, 0, music);
            else
               if(rand.nextDouble() < playChordsOnN && u % playOnN == 0)
                  playChord(CHORDSCHNL,null,null, note, where, -1, -1, noteTime*2, velocity, forceArp, 0, music); 
                  //play a bassNote at the start and in the middle of the scale-run
         if((u==0 || u==ourScaleRun.length/2) && bassLine==false)		
         {
            int bassNote=note;
            if(bassNote-bassDrop > 0)
               bassNote -= bassDrop;
            bassNote = forceNoteInRange(bassNote, scale);
            int bassLength = noteTime*ourScaleRun.length/2;   
            playNote(bassNote, bassLength, velocity, where, BASSCHNL, bassNotesPlayed, music);  
         }
         if(u % restNote == 0 || note < 0)
            note = 0;            
         where = playNote(note, noteTime, velocity, where, MELODYCHNL, melodyNotesPlayed, music);  
      }
      if(bassLine)
      {	
         int oldVelocity = velocity;
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         velocity += velocityChange;
         int bassTime = where - bassStart;
         ArrayList<int[]> bassLineInfo =  makeBassLine(bassStart, bassTime,-1,-1, false);
         int[] bassLineNotes = bassLineInfo.get(0);
         int[] bassLineDurations = bassLineInfo.get(1);
         playMelody(bassLineDurations, bassLineNotes, bassStart, 3, bassNotesPlayed, music);
         velocity = oldVelocity;
      }
   //******for tempo change*****
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //***************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(note, scale);
      returnVals[2] = note;
      return returnVals;
   }

//if forceNotes is null, it creates its own lick notes to use - otherwise, uses forceNotes
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playLickRun(int[] forceNotes, int bassDrop, int where, Track music) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int[] returnVals = new int[3];
      int note = 0;
      boolean bassLine = false;			//do we want to do a full bass line or octaves every so many notes?
      if(rand.nextDouble() < .5)
         bassLine = true;
      int bassStart = where;
   
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      int transitionPart = (int)(rand.nextDouble()*3)+2;	//this is the partition for when we transitin back:2,3,4 - transition in 2nd half, last 3rd or last 4th
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//60% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//40% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
   
      if(rand.nextDouble() < .4)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
   
      int [] lick = null;
      if (forceNotes==null)
         lick = makeLickRun(-1, -1);
      else
         lick = forceNotes;
      double randValue = rand.nextDouble();
      boolean bassOctaves = false;			//do we want our bass notes to be an octave of the current note?
      boolean ascendBassNotes = true;		//do we want our bass notes to try to ascend or descend?
      if(randValue < .33)
         ascendBassNotes = !ascendBassNotes;
      else
         if(randValue < .66)
            bassOctaves = true;
      int bassNote=-1;
      randValue = rand.nextDouble();
      int lastBassNote = -1;
      int bassDuration = wholeNote;
      int bassFreq = 8;							//hits bass note every 8 lick notes
      if(randValue < .25)
      {
         bassFreq = 4;								//hits bass note every 4 lick notes
         bassDuration = wholeNote/2;
      }
      else
         if(randValue < .5)					//hits bass note every 2 lick notes
         {
            bassFreq	= 2;							
            bassDuration = wholeNote/4;
         }
         else
            if(randValue < .75)					//hits bass note every 16 lick notes
            {
               bassFreq	= 16;							
               bassDuration = wholeNote*2;
            }
      boolean rest = false;					//will one of the lick notes be a rest
      int restNote = 99;						//which note is the rest
      if(rand.nextDouble() < .25)
         rest = true;   
      if(rest)
      {
         int[]skip = {2,3,4};						//do we skip every 2,3 or 4th note?	
         restNote = skip[(int)(rand.nextDouble()*skip.length)];
      }         			
      for(int g=0; g<lick.length; g++)
      {
      //******for tempo changes******
         if(willWeChangeTempo)
         {   			//within the last 1/transitionPart of the events 
            if(g >= lick.length - (lick.length/transitionPart) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first so many events, or not transitioning back 
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(newTempo > tempo*2)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(newTempo < tempo / 4)			//too slow...speed things up
               {
                  speedUp = true;
                  slowDown = false;
               }
         }
      //***********************************
      
         if(g==0 || g % bassFreq == 0)
         {
            if(bassOctaves)
               bassNote = lick[g];
            else
            {
               int []harmNotes = getHarmonizeNotes(lick[g], scale);
               if (lastBassNote == -1)				//for the first bass note,
               {
                  if(ascendBassNotes)				//make it the lowest note if the base line is ascending
                     bassNote = harmNotes[0];
                  else									//or the highest note if it is descending
                     bassNote = harmNotes[harmNotes.length-1];
               }
               else
               {
                  if(ascendBassNotes)	//find the note in harmNotes that is the next note higher than lastBassNote
                     bassNote=getNextClosestNote(lastBassNote, harmNotes, true);
                  else						//find the note in harmNotes that is the next note lower than lastBassNote
                     bassNote=getNextClosestNote(lastBassNote, harmNotes, false);          
               }
               lastBassNote = bassNote;
            }
            int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
            if(rand.nextDouble() < .25 && velocity + Math.abs(velocityChange) < 95)
               velocityChange *= -1;
            if(bassLine == false)
            {
               bassNote = forceNoteInRange(bassNote-bassDrop, scale);
               playNote(bassNote, bassDuration, velocity+velocityChange, where, BASSCHNL, bassNotesPlayed,music);
            }
         }
         note = lick[g];
      // if(g % restNote ==0)
         // note = 0;				//rest
         where = playNote(note, wholeNote/8, velocity, where, MELODYCHNL, melodyNotesPlayed, music);                    
      }
      if(bassLine)
      {	
         int oldVelocity = velocity;
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         velocity += velocityChange;
         int bassTime = where - bassStart;
         ArrayList<int[]> bassLineInfo =  makeBassLine(bassStart, bassTime,-1,-1, false);
         int[] bassLineNotes = bassLineInfo.get(0);
         int[] bassLineDurations = bassLineInfo.get(1);
         playMelody(bassLineDurations, bassLineNotes, bassStart, 3, bassNotesPlayed, music);
         velocity = oldVelocity;
      }
    //******for tempo change*****
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //***************************
      returnVals[0] = where;
      returnVals[1] = indexOfNote(note, scale);
      returnVals[2] = note;
      return returnVals;
   }

//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playTriads(int j, int bassDrop, int where, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {   
      int[] returnVals = new int[3];
      boolean bassLine = false;			//do we want to do a full bass line or octaves every so many notes?
      if(rand.nextDouble() < .5)
         bassLine = true;
      int bassStart = where;
   //******for tempo changes******
      boolean willWeChangeTempo = (rand.nextDouble() < freeTime);
      boolean transitionBack = false;	//will we transition back to the original tempo in the last 1/3 of events?
      if(rand.nextDouble() < .5)
         transitionBack = true;
      boolean speedUp = false;			//60% of the time we will speed up the scale run as we go through it
      boolean slowDown = false;			//40% of the time we will slow down the scale run as we go through it
      int newTempo = tempo;				//to store the original tempo in case we want to speed up or slow down the scale
      int tempoDelta = (int)(rand.nextDouble()*8) + 1;//how much the tempo changes by each step (1-8)  
   
      if(rand.nextDouble() < .4)	 		
      {																//we'll slow down here
         speedUp = false;
         slowDown = true;
      }
      else															//we'll speed up here			
      {
         speedUp = true;
         slowDown = false;
      }
   //*******************************
      boolean rest = false;									//will one of the triad notes be a rest
      int restNote = 99;										//which note (1,2 or 3) is the rest
      if(rand.nextDouble() < .25)
         rest = true;
      if(rest)
         restNote = (int)(rand.nextDouble()*3)+1;
      int numSameNotes = (int)(rand.nextDouble()*3);	//0,1 or 2 same notes throughout the triads
      if(numSameNotes==0 && rand.nextDouble()<.5)		//but mostly 1 or 2
         if(rand.nextDouble()<.75)
            numSameNotes=1;
         else
            numSameNotes=2;
      int sameNote1=-1, sameNote2=-1; 					//which notes in the triad are the same (1st,2nd or 3rd)  -1 if not used 
      int note1=-1, note2=-1;     						//note values for the ones that will be the same in the triad
      if (numSameNotes==1)
      {
         sameNote1=(int)(rand.nextDouble()*3)+1;
         note1  = note;							//75% of the time, the same note in the arp will be the one we start with
         if(rand.nextDouble() < .25)		//25% of the time, it will be a random note in the scale
            note1 = scale[(int)(rand.nextDouble()*scale.length)];  
      }
      else
         if (numSameNotes==2)
         {
            sameNote1=(int)(rand.nextDouble()*3)+1;
            sameNote2=(int)(rand.nextDouble()*3)+1;
            while(sameNote1==sameNote2)		//make sure they are not the same sameNotes
               sameNote2=(int)(rand.nextDouble()*3)+1;
            note1  = note;							//75% of the time, the same note in the arp will be the one we start with
            if(rand.nextDouble() < .25)		//25% of the time, it will be a random note in the scale
               note1 = scale[(int)(rand.nextDouble()*scale.length)];  
            note2 = scale[(int)(rand.nextDouble()*scale.length)]; 
            while (note2 == note1)				//make sure note2 is not the same as note1
               note2 = scale[(int)(rand.nextDouble()*scale.length)]; 	
         }
      int triadLength = (int)(rand.nextDouble()*8)+1;	//1 to 8 triads will be placed in the song
      int noteTime;
      if(rand.nextDouble() < .6)
         noteTime = wholeNote/8;					//60% of the time, 8th note triads
      else
         noteTime = wholeNote/4;					//40% of the time, 4th note triads
      boolean dottedDurations = false;
      if(triadLength % 2 == 0 && rand.nextDouble() < .5)	//half the time with 2,4 or 8 triads, make them dot quarter or dot eigth notes
      {
         noteTime += noteTime/2;
         dottedDurations = true;
      }
      int interval = (int)(rand.nextDouble()*5)+1;			//intervals can be 1-5
      if(rand.nextDouble()<.25 && interval>2)				//but mostly 1
         interval=1;
      if (triadLength == 1 && rand.nextDouble()<.25)		//25% of time with one triad, make them the same note
         interval = 0;
      double dir;
      if(j-interval < 0)	//if the index with the interval will take the index out of range
         dir = 1;				//assign the triad a direction so that will stay in range 
      else
         if (j+interval >=	scale.length)
            dir = 0;
         else
            dir = rand.nextDouble();					//up triads or down
      double reStep = rand.nextDouble();				//step up or back an interval for the next triad?
      int numSteps = (int)(rand.nextDouble()*2)+1;	//step back one or two intervals?
            
      int bassNote = note-bassDrop;    
      int bassLength = noteTime*triadLength/2;
            
      double playChordsOnN = rand.nextDouble();		//should we play a chord on every Nth note?
      int playOnN = (int)(rand.nextDouble()*3)+1;	//the Nth note we play on (1-3)
      boolean playChordsOnAll = false;
      if (playChordsOnN < .05)
         playChordsOnAll = true;	
      for(int u = 0; u<triadLength; u++)
      {
      //******for tempo changes******
         if(willWeChangeTempo)
         {  			//within the last 1/3 of the events 
            if(u >= triadLength - (triadLength/3) && transitionBack)	
            {			//approach the original tempo
               if(newTempo > tempo)
               {
                  speedUp = false;
                  slowDown = true;
               }
               else
               {
                  speedUp = true;
                  slowDown = false;
               }
            }
         //either within the first 2/3 of events, or not transitioning back
            if(speedUp)
            {
               newTempo += tempoDelta;
               setTempo(newTempo, where, music);
            }
            else
               if(slowDown)
               {
                  newTempo -= tempoDelta;
                  setTempo(newTempo, where, music);
               }
            if(newTempo > tempo*2)		//too fast..slow things down
            {
               speedUp = false;
               slowDown = true;
            }	
            else
               if(newTempo < tempo / 4)			//too slow...speed things up
               {
                  speedUp = true;
                  slowDown = false;
               }
         }
      //***********************************
         if(u < triadLength - 1)			//we will allow the last triad to be all the same note, but if we have more to go
         {										//then change directions if we get too close to an outer boundry
            if(j<=0)							//if we are at the lower boundry, make the direction go up
               dir = 1;				 
            else
               if (j >=	scale.length-1)//if we are at the upper boundry, make the direction go down
                  dir = 0;
         }
         if(rand.nextDouble() < .3 &&  sameNote1==-1)	//30% of the time, switch the first triad note if there are no sameNotes
            j=(int)(rand.nextDouble()*scale.length);
         for(int t=1; t<=3; t++)								//there are three notes in a triad
         {
            if(j<0 || j>=scale.length)
            {      
               if(j<0)										//if our next note index is out of bounds,
                  j=0;										//then make it either the first root
               else if(j>=scale.length)
                  j=scale.length-1;						//or the last root
               double outOfBoundsStrat = rand.nextDouble();	//strategy for how to handle an index out of bounds
               if(outOfBoundsStrat < .33)				//33% change - possibly change directions if we hit the bounds
                  dir = rand.nextDouble();
               else
                  if(outOfBoundsStrat < .66)			//33% change - just pick a new random index
                     j=(int)(rand.nextDouble()*scale.length);
            }
            note = scale[j];
            if(numSameNotes>0 &&  j>=0 && j<scale.length)	//assign the note in the triad to the sameNote if there are some
            {
               if(sameNote1==t && note1>0)
                  note = note1;
               if(sameNote2==t && note2>0)
                  note = note2;
            }
            else	//if we are out of range somehow, pick random notes
               if (numSameNotes>0 && (j<0 || j>=scale.length))	
               {
                  note1=scale[(int)(rand.nextDouble()*scale.length)];
                  note2=scale[(int)(rand.nextDouble()*scale.length)];
               }
            if(noteTime >= wholeNote/4)
               if((rand.nextDouble() < playChordsOnN && t == playOnN) || playChordsOnAll)
                  playChord(CHORDSCHNL,null,null,note, where, -1, -1, noteTime*2, velocity, forceArp, 0, music);
               //play a bassNote at the start and in the middle of the tri
            if((u==0 || u==triadLength/2) && bassLine==false)
            {
               bassNote=note;
               if(bassNote-bassDrop > 0)
                  bassNote -= bassDrop; 
               bassNote = forceNoteInRange(bassNote, scale);   
               playNote(bassNote, bassLength, velocity, where, BASSCHNL, bassNotesPlayed, music);  
            }
            if(rest && t==restNote)
               note = 0;            
            where = playNote(note, noteTime, velocity, where, MELODYCHNL, melodyNotesPlayed, music);  
                                          
            if(dir<.5 &&  j>=0 && j<scale.length)	//up triad with valid index for note
               j+=interval;								//step to the next note in the triad
            else if(j>=0 && j<scale.length)			//down triad with valid index for note
               j-=interval;								//go to the next note in the triad
            if(j<0 || j>=scale.length)
            {      
               if(j<0)										//if our next note index is out of bounds,
                  j=0;										//then make it either the first root
               else if(j>=scale.length)
                  j=scale.length-1;						//or the last root
               double outOfBoundsStrat = rand.nextDouble();	//strategy for how to handle an index out of bounds
               if(outOfBoundsStrat < .33)				//33% change - possibly change directions if we hit the bounds
                  dir = rand.nextDouble();
               else
                  if(outOfBoundsStrat < .66)			//33% change - just pick a new random index
                     j=(int)(rand.nextDouble()*scale.length);
            }
         }										//end loop for this particular triad
         if (reStep<.5)						//step the first note in the next triad back one or two intervals
         {
            if(dir<.5)
               j-=interval*numSteps;
            else
               j+=interval*numSteps;
         }
         if(j<0 && rand.nextDouble()<.5)					//if our note index got out of range, 
            j=0;													//sometimes make it the low root
         else if(j>=scale.length && rand.nextDouble()<.5)
            j=scale.length-1;									//sometimes make it the high root
         else if (j<0 || j>=scale.length)
            j=(int)(rand.nextDouble()*scale.length);	//sometimes make it a random note
      }
      if(bassLine)
      {	
         int oldVelocity = velocity;
         int velocityChange = ((int)(rand.nextDouble()*11) + 5) * (-1);
         velocity += velocityChange;
         int bassTime = where - bassStart;
         ArrayList<int[]> bassLineInfo =  makeBassLine(bassStart, bassTime,-1,-1, dottedDurations);
         int[] bassLineNotes = bassLineInfo.get(0);
         int[] bassLineDurations = bassLineInfo.get(1);
         playMelody(bassLineDurations, bassLineNotes, bassStart, 3, bassNotesPlayed, music);
         velocity = oldVelocity;
      }
    //******for tempo change*****
      if(willWeChangeTempo)
         setTempo(tempo, where, music);
   //***************************
      returnVals[0] = where;
      returnVals[1] = j;
      returnVals[2] = note;
      return returnVals;
   }

//plays the melody sent with sent durations at tracking position 'where'
//melodyNotes and melodyDurations should have the same length
//returns current tracking position (index 0), current note index j (index 1) and current note (index 2)
   public static int[] playMelody(int[]melodyDurations, int[]melodyNotes, int where, int chnl, Map<Integer, Integer> alreadyPlayed, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int[] returnVals = new int[3];
      for(int i=0; i<melodyDurations.length && i<melodyNotes.length; i++)
      {
         where = playNote(melodyNotes[i], melodyDurations[i], velocity, where, chnl, alreadyPlayed, music);
      }
      int lastNote = note;
      if(melodyNotes.length > 0)
         lastNote = melodyNotes[melodyNotes.length - 1];
      returnVals[0] = where;
      returnVals[1] = indexOfNote(lastNote, scale);
      returnVals[2] = lastNote;
      return returnVals;
   }
	
//i is the ith event played in the track.  j is the index of the last note played.
//melodyTheme notes is an array of notes for the repeating melody theme.
//lastHarmonyNote is the value of the last harmony note played, used to keep the next one close.
//lastNoteWasARest is used so we don't choose 2 rests in a row.
//bassDrop is the amount (# of octaves) to drop the bass note played while improvising.
//where is the tracking ticks that we start to improvise on.
//whereEnd is an optional tracking tick value for where we should end at.  -1 if not needed
//returns current note index j (index 0), lastNoteWasARest (index 1), lastHarmonyNote (index 2) and current tracking position (index 3), and last note played (index 4)
   public static int[] improvise(int i, int j, int [] melodyThemeNotes, boolean lastNoteWasARest, int lastHarmonyNote, int bassDrop, int where, int whereEnd, Track music)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int[] retVals = new int[5];
      int [] duration = {wholeNote/4,wholeNote/4,wholeNote/4,wholeNote/2,wholeNote/2,wholeNote};			//the possible length of the notes
   //3 out of 6 times we will pick a quarter note, 2 out of 6 times we will pick a half-note, 1 out of 6 times we will pick a whole note
      int [] restDuration = {wholeNote/16,wholeNote/8,wholeNote/8,wholeNote/8,wholeNote/4,wholeNote/4};	//the possible length of the rest
   //1 out of 6 times we will pick a 16th rest, 3 out of six times we pick an eigth rest, 2 out of six times we pick a quarter rest
      int noteLengthIndex = (int)(rand.nextDouble()*duration.length);		//pick our note duration     
      int restLengthIndex = (int)(rand.nextDouble()*restDuration.length);//pick a possible rest durration
   //while we have a specified end time (whereEnd), pick another duration if it will go over our end time
      while(whereEnd != -1 && where + duration[noteLengthIndex] > whereEnd)
         noteLengthIndex = (int)(rand.nextDouble()*duration.length);
   	
      if (j<0 || j>=scale.length)
         j = (int)(rand.nextDouble()*scale.length);
                  
      if(rand.nextDouble()<busyNess || lastNoteWasARest == true)	
      {	//rest or not, n% chance that it will be a note
         note = scale[j];  				//this code actually makes the note
         lastNoteWasARest = false;
      }
      else										//rest if the last event was not a rest
      {
         note = 0;							
         noteLengthIndex = restLengthIndex;	
         lastNoteWasARest = true;
      }
      int noteTime = duration[noteLengthIndex];
                  //picks the next note in the song
      int nextNote = (int)(rand.nextDouble()*4);//makes the next note within five notes of each other, up or down
                  //nextNote will be a 0-3.  If it is 0, the next note is the same as the previous one
                  //If it is 3, then the next note is 3 notes higher/lower in the scale, which is 5 semi-tones.    
      double upOrDown = rand.nextDouble();     	//will the next note go up or down?  
      double pickInRange = rand.nextDouble(); 	//should we pick the next note within a five step range?     
      if(pickInRange<noteRange)						//pickInRange % of the time the next note will be within 5 steps of the previous
         if(upOrDown<.5)
         {
            if(j+nextNote < scale.length-1)
               j+=nextNote;
            else
               j-=nextNote;
         }
         else
         {
            if(j-nextNote > 0)
               j-=nextNote;
            else
               j+=nextNote;
         } 
      else														//n% of the time, the next note is a random note in the scale or mode
         j=(int)(rand.nextDouble()*scale.length);	//j is the note (the index of the notes array)
                  
      int noteIndex=0;									//the index where the current note is
      int harmonyNote=-1;								//can we have a harmony note?
      double addHarmony = rand.nextDouble();		//should we harmonize the note	
                  /*****************************************************************************
                  ************************PICK HARMONY NOTE*************************************
                  *****************************************************************************/
      int[] harmNotes = Scale.addOctave(getHarmonizeNotes(note, scale));   
      if (harmNotes != null)	
      {
         harmonyNote = harmNotes[(int)(rand.nextDouble()*harmNotes.length)];	//pick a random note from the set of harmony notes
         if(i > 0)	//find a note in the set that is the closest to the last harmony note
         {
            int closestIndex=0;
            int minDiff = 999;
            for(int c=0; c < harmNotes.length; c++)
               if(Math.abs(harmNotes[c] - lastHarmonyNote) < minDiff && harmNotes[c] != lastHarmonyNote)
               {
                  minDiff = Math.abs(harmNotes[c] - lastHarmonyNote);
                  closestIndex = c;
               }
            harmonyNote = harmNotes[closestIndex];
         }
      }	                  
      if (note==0)
         harmonyNote = 0;
                                              
      double addBass = rand.nextDouble();		//should we add a bass note at this part of the meledy?
      int bassNote=scale[0];						//this will be the bass note we play  
      if(addHarmony < harmonize && harmonyNote >= 0 && rand.nextDouble()<.5)
         bassNote=harmonyNote;					//50% of the time, the bassNote is the harmonyNote
      else
         bassNote=note;
      if(bassNote-bassDrop > 0)
         bassNote -= bassDrop;
      bassNote = forceNoteInRange(bassNote, scale);   
      if (note==0)
         harmonyNote = 0;
                  
      boolean timeToPlayChord1 = (chordTime==0 && ((where + noteTime)/chordInterval) >  (where/chordInterval));
      boolean timeToPlayChord2 = ((chordTime==1 && (where%chordTimeSeed==0 || where%chordTimeSeed2==0))
                        || (chordTime==2 && (i%chordTimeSeed==0 || i%chordTimeSeed2==0)));
                  //*********************************************************************************
                  //****INSERT CHORD, PHRASE OR MELODYCHNL THEME ****************************************
                  //*********************************************************************************
      if(timeToPlayChord1)
      {
         int startTime = nextMultOfInterval(where, chordInterval);
         noteTime = Math.abs(startTime - where);			//cut noteTime so melody notes won't bleed into the chord
         playChord(CHORDSCHNL,null,null, melodyThemeNotes[0], startTime, -1, -1, forceChordLength, velocity, forceArp, 0, music);
      }
      else
         if(timeToPlayChord2)
            where=playChord(CHORDSCHNL,null,null,note, where, -1, -1,  forceChordLength, velocity, forceArp, 0, music); 
                  //*********************************************************************************   
                  //****END CHORD, PHRASE OR MELODYCHNL THEME *******************************************
                  //*********************************************************************************
      if (addHarmony < harmonize)         
         playNote(harmonyNote, noteTime, velocity, where, HARMONYCHNL, harmonyNotesPlayed, music);  
      if(addBass<.6 && noteTime >= wholeNote/2)		//add a bass note 60% of the time we are on at leas a half note
         playNote(bassNote, noteTime, velocity, where, BASSCHNL, bassNotesPlayed, music);  
      where = playNote(note, noteTime, velocity, where, MELODYCHNL, melodyNotesPlayed, music);  
      lastHarmonyNote = harmonyNote;    
   //returns current note index j (index 0), lastNoteWasARest (index 1), lastHarmonyNote (index 2), current tracking position (index 3) and last note played (index 4)
      retVals[0] = j;
      retVals[1] = booleanToInt(lastNoteWasARest);
      retVals[2] = lastHarmonyNote;
      retVals[3] = where;
      retVals[4] = note;
      return retVals;
   }	//end picking of a improvise note
	
//Pre: A specificScale needs to be set
//Post: Sets the music		O(n)
   public static Track generateMusic2(Sequence s) throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      int tracking = 0;						//where the current event will be placed in the song, constantly updated
      scale = specificScale.getNotes();//get the scale of notes from the scale chosen
      chordSetsA = specificScale.getChordSetsA();
      chordSetsB = specificScale.getChordSetsB();
      note=scale[0];							//a single note from the scale 
      velocity=100;							//the velocity of the note (changed randomly in the loop)
      int j=0;									//location in scale array, refrenced as scale[ j ];
      note=scale[0];							//a single note from the scale
      Track music = s.createTrack();	//the MIDI track we write our notes into
      setTempo(tempo, 0, music);
                    	
      music.add(new MidiEvent(ChangeInstrument(melodyInst, MELODYCHNL), 0));	//melody track
   
      music.add(new MidiEvent(ChangeInstrument(chordInst, CHORDSCHNL), 0));	//chord track
   
      music.add(new MidiEvent(ChangeInstrument(harmonyInst, HARMONYCHNL), 0));//harmony track
   
      music.add(new MidiEvent(ChangeInstrument(bassInst, BASSCHNL), 0));		//bass track
   
      j=(int)(rand.nextDouble()*scale.length);
      boolean lastNoteWasARest = false;//to make sure we dont pick two rest events in a row  
      int lastHarmonyNote = -1;			//the last harmony note played, so we can pick one close to it on the next note for the melody
            
      int bassDrop=OCTAVE;					//do we lower the bass one octave or two?
      if(rand.nextDouble()<.25)			//50% of the time it will be two octaves lower
         bassDrop+=OCTAVE;
   
      ArrayList<int[]> rhy = makeChordTheme(null, -1, -1);
      int[]chordThemeDurations = rhy.get(0);
      int[]chordThemeGroupIndexes = rhy.get(1);
      int[]chordThemeChordIndexes = rhy.get(2);
   
      int melodyLength = wholeNote * 2;//the duration of the melody is either 2 whole notes or 4 whole notes
      int [] melodyThemeDurations = null;
      boolean chordMadeMelody = false;
   //melodyBuildStrat->strategy for how melodies are built.  0-choose random, 1-build from chord theme, 2-build using repeating substructures, 3-build loosly
      if(melodyBuildStrat == 0)									//choose random
         melodyBuildStrat = (int)(rand.nextDouble()*3)+1;//1,2 or 3
      int [] melodyThemeNotes = null;
      ArrayList<int[]> melodySets = null;
      if(melodyBuildStrat==2)
      {
         melodyLength *= 2;				//make a more structured collection of durations (more releating patterns)
         melodyThemeDurations = makeMelodyDurations(melodyLength);
         melodySets =makeMelodyNotes(melodyThemeDurations, -1, -1, -1, -1);
         melodyThemeNotes = melodySets.get(0);
      }
      else
         if(melodyBuildStrat==3)
         {
            if(rand.nextDouble() < .5)
               melodyLength *= 2;			//more freeform creation of durations
            melodyThemeDurations = makeRiffDurations(melodyLength, true, -1, .25, -1);
            melodySets =makeMelodyNotes(melodyThemeDurations, -1, -1, -1, -1);
            melodyThemeNotes = melodySets.get(0);
         }
         else
            if(melodyBuildStrat==1)
            {											//melody built from the chord theme
               chordMadeMelody = true;
               ArrayList<int[]> melodyInfo = makeMelodyNotes(chordThemeDurations, chordThemeGroupIndexes, chordThemeChordIndexes);
               melodyThemeDurations = melodyInfo.get(0);
               melodyThemeNotes = melodyInfo.get(1);
            }  
   //melody variation by transposing    
      ArrayList<int[]> melodyVar = makeMelodyVariations(melodyThemeDurations, melodyThemeNotes);
      int[] melodyVarDurations = melodyVar.get(0);
      int[] melodyVarNotes = melodyVar.get(1);
   		     	
      int [] melodyThemeNotesOrig = melodyThemeNotes.clone();	//so we can switch back when we change keys
      int [] melodyThemeDurOrig = melodyThemeDurations.clone();
   
      if(!chordMadeMelody)
         for(int mt=0; mt<melodySets.size()-1; mt++)				//the last index is excluded because it is durations (in case they change)
         {
            System.out.println("Melody Theme Notes  v"+mt+":"+printArray(melodySets.get(mt), false, false));
            System.out.println("note names          v"+mt+":"+printArray(melodySets.get(mt), true, false));
         }
      else
      {
         System.out.println("Melody Theme Notes    :"+printArray(melodyThemeNotes, false, false));
         System.out.println("note names            :"+printArray(melodyThemeNotes, true, false));
      }
   
      System.out.println("Melody Theme Durations:"+printArray(melodyThemeDurations, false, false));
      System.out.println("duration names        :"+printArray(melodyThemeDurations, false, true));
   
      System.out.println("chord Theme Group Indexes :"+printArray(chordThemeGroupIndexes, false, false));
      System.out.println("chord Theme Chord Indexes :"+printArray(chordThemeChordIndexes, false, false));
      System.out.println("chord Theme Durations     :"+printArray(chordThemeDurations, false, false));
      System.out.println("chord Theme duration names:"+printArray(chordThemeDurations, false, true));
      System.out.println("chord names               :"+printChords(chordThemeGroupIndexes,chordThemeChordIndexes));      		 
      double melodyThemeMin = 0;
      double melodyThemeMax = tryMelodyTheme;
      double phraseMin = melodyThemeMax;
      double phraseMax = phraseMin+tryPhrase;
      double scaleRunMin = phraseMax;
      double scaleRunMax = scaleRunMin+tryScaleRun;
      double triadsMin = scaleRunMax;
      double triadsMax = triadsMin+tryTriads;
      double rollingChordsMin = triadsMax;
      double rollingChordsMax = rollingChordsMin+tryRollingChords;
      double sequenceMin = rollingChordsMax;
      double sequenceMax = sequenceMin + trySequence;
      double chordThemeMin = sequenceMax;
      double chordThemeMax = 100;
   
      velocity =(int)(rand.nextDouble()*31)+50;	//the  velocity of the note or chord (50-80)
      int whichMelody = 0;
      if(rand.nextDouble() < .5)						//start out with the melody theme
      {  
         System.out.println("-MT"+whichMelody);
         int[]values =  playMelody(melodyThemeDurations, melodyThemeNotes, tracking, MELODYCHNL, melodyNotesPlayed, music);
         tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
         j = values[1];
         note = values[2];
      }
   //write new scale name into MIDI file              
      String text = intToKey(specificScale.getKey())+" "+specificScale.getName();
      addEvent(music, TEXT, text.getBytes(), tracking);
      int improvPercent = (int)(tryImprov * 100);		//used to figure out the number of events in the song
      int numEvents = (int)(Math.pow(1.06, improvPercent));	//since each improv event takes much less time than structures, the more improv we do, the more events we want
   //************TESTING******************************
      if(numEvents < 10)										//at least 10 events
         numEvents = 10;
      int firstQuarter = (int)(numEvents/4);				//allow at least the first 1/4 of events be in the original key
      int timeToChangeScale = (int)(rand.nextDouble()*(numEvents-firstQuarter))+firstQuarter;	//change to a relative, parallel, similar or expanded scale
      int eventsLeft = numEvents - timeToChangeScale; 
      int timeToChangeBack = (int)(rand.nextDouble()*eventsLeft) + timeToChangeScale;				//change back to specificScale
      for(int event = 0; event < numEvents; event++)
      {        //maybe switch to a variation of the melody
         if(rand.nextDouble() < .5)
         {
            if(!chordMadeMelody)
            {
               whichMelody = (int)(rand.nextDouble()*melodySets.size()-1);	//the last index is excluded because it is durations (in case they change)
               melodyThemeNotes = melodySets.get(whichMelody);		
               melodyThemeDurations =  melodySets.get(melodySets.size()-1);//the last index is excluded because it is durations (in case they change)
            }
            else
            {
               whichMelody = 0;
               melodyThemeDurations = melodyThemeDurOrig;
               melodyThemeNotes = melodyThemeNotesOrig;
            }
         }
         else	//melody variation by transposing
         {
            whichMelody = 3;
            melodyThemeDurations = melodyVarDurations;
            melodyThemeNotes = melodyVarNotes;
         }
       
         if(allowRelativeScale || allowParallelScale || allowSimilarScale || allowExpandedScale)
         {
            if(event==timeToChangeScale && altScale!=null)
            {
            //write new scale name into MIDI file              
               text = intToKey(altScale.getKey())+" "+altScale.getName();
               addEvent(music, TEXT, text.getBytes(), tracking);
            
               scale = altScale.getNotes();	//get the scale of notes from the scale chosen
               chordSetsA = altScale.getChordSetsA();
               chordSetsB = altScale.getChordSetsB();
            //melodyThemeNotes = makeMelodyNotes(melodyThemeDurations, -1, -1);	//we have a new scale now
               ArrayList<int[]> melodySets2 = makeMelodyNotes(melodyThemeDurations, -1,-1, -1, -1);
               melodyThemeNotes = melodySets2.get((int)(rand.nextDouble()*melodySets2.size()-1));//the last index is excluded because it is durations (in case they change)
               for(int chi=0; chi<chordThemeChordIndexes.length; chi++)				//we have new chords now
               {
                  int groupIndex = chordThemeGroupIndexes[chi];
                  ArrayList ourGroup = chordSetsA[groupIndex];
                  int randChordIndex = (int)(rand.nextDouble()*ourGroup.size());
                  chordThemeChordIndexes[chi] = randChordIndex; 
               }
            }
            else
               if(event==timeToChangeBack && altScale!=null)
               {
               //write new scale name into MIDI file              
                  text = intToKey(specificScale.getKey())+" "+specificScale.getName();
                  addEvent(music, TEXT, text.getBytes(), tracking);
               
                  scale = specificScale.getNotes();				//get the scale of notes from the scale chosen
                  chordSetsA = specificScale.getChordSetsA();	//switch chords and melody back to original scale
                  chordSetsB = specificScale.getChordSetsB();
                  melodyThemeNotes = melodyThemeNotesOrig;
                  chordThemeChordIndexes = rhy.get(2);
               }
         }
         velocity =(int)(rand.nextDouble()*31)+50;	//the  velocity of the note or chord (50-80)
         double whatWillWeDo = rand.nextDouble();	//structure, chordSequence or melody?
      //whther or not to play an arpeggio, triads or meledy
         boolean melodyTheme = false;		//scaleRun or not
         boolean phrase = false;				//triads or not
         boolean scaleRun = false;			//scaleRun or not
         boolean triads = false;				//triads or not
         boolean rollingChords = false;	//rolling chords or not
         boolean sequence = false;			//sequence chord progression or not
         boolean chordTheme = false;		//chord theme or not
         if(whatWillWeDo < tryStructure)	
         {											//n% of the time it will play a scale run, triads or rolling chords
            double pickEvent = rand.nextDouble();
            if(pickEvent >=melodyThemeMin && pickEvent < melodyThemeMax)	
               melodyTheme = true;  			//if so, n% of the time it will be a melodyTheme             
            else if(pickEvent >= phraseMin && pickEvent < phraseMax)	
               phrase = true;					//otherwise, n% it will be some phrasing
            if(pickEvent >= scaleRunMin && pickEvent < scaleRunMax)	
               scaleRun = true;  			//if so, n% of the time it will be a scaleRun             
            else if(pickEvent >= triadsMin && pickEvent < triadsMax)	
               triads = true;				//otherwise, n% it will be some triads
            else if(pickEvent >= rollingChordsMin && pickEvent < rollingChordsMax)
               rollingChords = true;//n% of the time it will be rolling chords
            else if(pickEvent >= sequenceMin && pickEvent < sequenceMax)
               sequence = true;	//n% of the time it will be chord progrerssion sequence with melody
            else if(pickEvent >= chordThemeMin && pickEvent < chordThemeMax)
               chordTheme = true;	//n% of the time it will be chord theme sequence with or w/o melody or main riff
         }  
      /*****************TEST************************
      melodyTheme = false;
      phrase = false;
      scaleRun = false;
      triads = false;
      rollingChords = true;
      sequence = false;
      chordTheme = false;
      //*********************************************/
         if(melodyTheme)	//*************MELODY THEME****************
         {						//**********revisit a melody***************
            int startTime = nextMultOfInterval(tracking, chordInterval);
            while(startTime - tracking > wholeNote/2)
            {	//improvise inbetween current time and where we start the melody theme  
               System.out.print("~Im~");
            //returns current note index j (index 0), lastNoteWasARest (index 1), lastHarmonyNote (index 2), current tracking position (index 3) and last note played (index 4)
               int[] values = improvise(event, j, melodyThemeNotes, lastNoteWasARest, lastHarmonyNote, bassDrop, tracking, -1, music);
               j = values[0];
               lastNoteWasARest = intToBoolean(values[1]);
               lastHarmonyNote = values[2];
               tracking = values[3];
               note = values[4];
            }
            System.out.println("-MT"+whichMelody);
            int[]values = playMelodyTheme(melodyThemeDurations, melodyThemeNotes, startTime, music);
            tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
            j = values[1];
            note = values[2];
            playChord(CHORDSCHNL,null,null, melodyThemeNotes[0], startTime, -1, -1, forceChordLength, velocity, forceArp, 0, music);
         }
         else if(phrase)	//**************PHRASE**************
         {						//******play a repeating phrase*****
            int startTime = nextMultOfInterval(tracking, chordInterval);
            while(startTime - tracking > wholeNote/2)
            {	//improvise inbetween current time and where we start the phrasing  
               System.out.print("~Im~");
            //returns current note index j (index 0), lastNoteWasARest (index 1), lastHarmonyNote (index 2), current tracking position (index 3) and last note played (index 4)
               int[] values = improvise(event, j, melodyThemeNotes, lastNoteWasARest, lastHarmonyNote, bassDrop, tracking, -1, music);
               j = values[0];
               lastNoteWasARest = intToBoolean(values[1]);
               lastHarmonyNote = values[2];
               tracking = values[3];
               note = values[4];
            }
            System.out.println("-Ph-");
            int[]values = playPhrase(tracking, music);
            tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
            j = values[1];
            note = values[2];
            playChord(CHORDSCHNL,null,null, melodyThemeNotes[0], startTime, -1, -1, forceChordLength, velocity, forceArp, 0, music);
         }
         else if(rollingChords)
         {
            System.out.println("-RC-");
            int[]values = playRollingChords(note, melodyThemeNotes, chordThemeGroupIndexes, chordThemeChordIndexes,tracking, 0, music);
            tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
            j = values[1];
            note = values[2];
            double endStrat = rand.nextDouble();	//end of rolling chord strategy
            if(endStrat < .25)					//25% of time end with a strike of a half-note chord
               tracking = playChord(CHORDSCHNL,null,null, note, tracking, -1, -1, wholeNote/2, velocity,0, 0, music);
            else
               if(endStrat < .5)					//25% of the time a quarter note chord
                  tracking = playChord(CHORDSCHNL,null,null, note, tracking, -1, -1, wholeNote/4, velocity,0, 0, music);
               else
                  if(endStrat < .75)			//25% of time end with a strike of a whole-note chord
                     tracking = playChord(CHORDSCHNL,null,null, note, tracking, -1, -1, wholeNote, velocity,1.0, 0, music);
         }												//25% of the time, don't end on a struck chord
         else if(scaleRun)	//*****SCALE RUN*********************************
         {						/*****3 types of scale runs:	1)accent the melody notes with bass octaves,		
                   		 ************************** 	2) run through scale with dynamic pattern,
         							 								3) regular scale run or 4) serated scale run*/
            double runType = rand.nextDouble();
            if(runType < .25)	//run through the scale and accent the melody notes with bass octaves
            {	System.out.println("-SR1");
               int[]values = playRunThruScale(melodyThemeNotes, -1, true, tracking, music);
               tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
               j = values[1];
               note = values[2];
            }
            else
               if(runType < .50)//plays a bass note on every 16, 8, 4, or 2nd note in the lick
               {	
                  System.out.println("-SR2");
                  int[]values = playLickRun(null, bassDrop, tracking, music);
                  tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
                  j = values[1];
                  note = values[2];
               }
               else
                  if(runType < .75)
                  {
                     System.out.println("-SR3");
                     if(j < 0)
                        j = 0;
                     int[]values = playRegularScaleRun(melodyThemeNotes, scale[j], bassDrop, tracking, music);
                     tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
                     j = values[1];
                     note = values[2];
                  }
                  else
                  {									//serated scale run
                     System.out.println("-SR4");
                     int[]sr = makeSeratedScaleRun(-1, -1, melodyThemeNotes,chordThemeGroupIndexes,chordThemeChordIndexes);
                     int[]durs = new int[sr.length];
                     int noteLength = wholeNote/4;
                     double whichLength = rand.nextDouble();
                     if(whichLength < .05)
                        noteLength = wholeNote/16;
                     else
                        if(whichLength < .3)
                           noteLength = wholeNote/8;
                     if((sr.length % 3 == 0 && rand.nextDouble() < .75) || rand.nextDouble() < .25)
                        noteLength += (noteLength/2);	//if the number of notes is divisible by 3 or 25% of the time, use dotted note durations
                     for(int sri=0; sri<durs.length; sri++)
                        durs[sri] = noteLength;
                     int[]values = null;
                     if(rand.nextDouble() < .65)	//play serated scale run with bass notes every so many notes
                        values = playLickRun(sr, bassDrop, tracking, music);
                     else   							//play serated scale run straight
                        values =  playMelody(durs, sr, tracking, MELODYCHNL, melodyNotesPlayed, music);
                     tracking = values[0];		//returns these values so that following improvisations can follow off the last notes
                     j = values[1];
                     note = values[2];
                  }
         }
         else if(triads)//*****TRIADS************************************							
         {					//*****pick a sequence of triads*****************
            System.out.println("-Tr-");
            int[]values = playTriads(j, bassDrop, tracking, music);
            tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
            j = values[1];
            note = values[2];
         }
         else if(sequence) //*****SEQUENCE CHORDPROGRESSION*************************
         {						//sequence of chords and melody (and maybe countermelody) in either 2/4, 3/4 or 4/4
            System.out.println("-Se-");
            int [] chordProgSize = new int[4];	//temporary array to set number of notes in chordProg
         //int [] chordProg = makeMelodyNotes(chordProgSize, -1, -1);
            ArrayList<int[]> melodySets3 = makeMelodyNotes(chordProgSize, -1, -1, -1, -1);
            int [] chordProg = melodySets3.get((int)(rand.nextDouble()*melodySets3.size()-1));
            int[]values = playChordSequence(melodyThemeNotes, chordThemeGroupIndexes, chordThemeChordIndexes, forceChordLength, tracking, timeSig, counterMelody, music);
            tracking = values[0];					//returns these values so that following improvisations can follow off the last notes
            j = values[1];
            note = values[2];
            double endStrat = rand.nextDouble();//end of play sequence strategy
               
            if(endStrat < .30)					//30% of time end with a strike of a half-note chord
               tracking = playChord(CHORDSCHNL,null,null,note, tracking, -1, -1, wholeNote/2, velocity,0, 0, music);
            else
               if(endStrat < .60)				//30% of the time a struck whole note but it is sustained over the next event
                  tracking = playChord(CHORDSCHNL,null,null,note, tracking, -1, -1, wholeNote, velocity,0, 0, music) - wholeNote/2;
               else
                  if(endStrat < .90)			//30% of time end with a rolled whole-note chord
                     tracking = playChord(CHORDSCHNL,null,null,note, tracking, -1, -1, wholeNote, velocity,1, 0, music);
         }												//10% of the time, don't end on a struck chord  
         else if(chordTheme && chordThemeDurations.length > 0 && chordThemeGroupIndexes.length > 0 && chordThemeChordIndexes.length > 0)
         {//****CHORD PROGRESSION THEME************
         //a chord progression that can resurface in the song (w or w/o a new melody or our resurfacing melody)
            System.out.println("-CT-");
            int numTimes = (int)(rand.nextDouble()*6)+2;
            for(int n=0; n <numTimes; n++)
            {
               if(rand.nextDouble() < .5)
               {	//play chord theme with the built melody
                  int[]values = playChordTheme(chordThemeDurations, chordThemeGroupIndexes, chordThemeChordIndexes, melodyThemeDurations, melodyThemeNotes, tracking, music);
                  tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
                  j = values[1];
                  note = values[2];
               }
               else
               {	//play chord theme and make up a new melody
                  int[]values = playChordTheme(chordThemeDurations, chordThemeGroupIndexes, chordThemeChordIndexes, null, null, tracking, music);
                  tracking = values[0];	//returns these values so that following improvisations can follow off the last notes
                  j = values[1];
                  note = values[2];
               }
            }
         }
         else	//***********************************************	
         { 		//*****IMPROVISE*********************************
            System.out.print("-Im-");
         //returns current note index j (index 0), lastNoteWasARest (index 1), lastHarmonyNote (index 2), current tracking position (index 3) and last note played (index 4)
            int[] values = improvise(event, j, melodyThemeNotes, lastNoteWasARest, lastHarmonyNote, bassDrop, tracking, -1, music);
            j = values[0];
            lastNoteWasARest = intToBoolean(values[1]);
            lastHarmonyNote = values[2];
            tracking = values[3];
            note = values[4];
         }      
      }//end for loop for numEvents              
   
   //***********************************************
   //*****ENDINGS***********************************
   //***********************************************
   //make the last note and chord the root of the piece (are there other ones that will sound good at the end?       
      note = scale[0];
      if(rand.nextDouble()<.5)	//jump last note up or down an octave? 
      {  
         if (rand.nextDouble() < .5 && note<100 && note>0)
            note += OCTAVE;	
         else
            if(note >= 12)
               note -= OCTAVE;
      }
      note = forceNoteInRange(note, scale);
   //last note
      if(rand.nextDouble() < .5)	//50% chance we play the root note before the last chord
         tracking = playNote(note, wholeNote/2, 70, tracking, MELODYCHNL, melodyNotesPlayed, music);        
   //find the last bass note, and make one that fits the chord that is as close as possible to the last one 	
      int lastBassNote=0;
      for(int i=tracking-wholeNote*4; i<tracking; i++)	//check all ticks from a quadruple whole note back to current position
      {
         Integer bassTemp = bassNotesPlayed.get(i);
         if(bassTemp != null)
            lastBassNote = bassTemp;
      }
   //last chord
   //*********************************************************************************************************	
      ArrayList[] chordSet;
      if (rand.nextDouble() < altChords)
         chordSet = chordSetsB;
      else
         chordSet = chordSetsA;
      Chord theChord = getChordThatHasNote(note, chordSet);	
      int bassNote = 0;
      if(lastBassNote > 0)
      {  
         bassNote = closestNoteInChord(lastBassNote, theChord, -1);
         bassNote = getInSameOctave(bassNote, lastBassNote);
         bassNote = forceNoteInRange(bassNote, scale);
      } 
      if(rand.nextDouble() < .5)	//a chord that (if it is not chordIndex 0) will need some resolution 
      {
         if(bassNote > 0)
            playNote(bassNote, wholeNote, velocity, tracking, BASSCHNL, bassNotesPlayed, music);  
         tracking = playChord(CHORDSCHNL,theChord,null, 0, tracking, -1,-1, wholeNote, velocity, forceArp, 0, music);	//play the root chord  
      }
   // playChord(int chnl, Chord chordObject, int[] forceChord, int note, int where, int forceChordGroup, int forceChordIndex, int forceChordLength, int forceVelocity, double forceArp, int octave, Track music)
      double endChordArp=0;
      if(rand.nextDouble() < .5)	//maybe roll the last chord
         endChordArp = 1;
      theChord = (Chord)(chordSetsA[0].get(0));
      bassNote = 0;
      if(lastBassNote > 0 && rand.nextDouble() < .5)
      {  								//last bass note is a note of the last chord that is closest to the previous bass note
         bassNote = closestNoteInChord(lastBassNote, theChord, -1);
         bassNote = getInSameOctave(bassNote, lastBassNote);
         bassNote = forceNoteInRange(bassNote, scale);
      } 
      else								//last bass note is root of the last chord
         bassNote = forceNoteInRange(getInSameOctave(theChord.getNotes()[0], lastBassNote), scale);
      if(bassNote > 0)
         playNote(bassNote, wholeNote*2, velocity, tracking, BASSCHNL, bassNotesPlayed, music);   
      tracking = playChord(CHORDSCHNL, theChord,null, note, tracking, -1, -1, wholeNote*2, velocity, endChordArp, 0, music);
   //*********************************************************************************************************
      return music;
   }


   public static void main(String [] args)throws InvalidMidiDataException, MidiUnavailableException, IOException
   {
      seed = (long)(Math.random()*Integer.MAX_VALUE);
   //seed = new Long(961090029); 
   //seed = new Long(210730367);//check gaps in song here
   //seed = new Long(1951687038);//test melody notes here
   //seed = new Long(1137694012);//test short bass notes in rolling chords here
      if(seed>=0)
         rand = new Random(seed);
      else rand = new Random();
   
      String filename = "BLANK";
      Sequencer sequencer = MidiSystem.getSequencer(); 
      sequencer.open();
      userInput(); 
   
      Sequence song = new Sequence(javax.sound.midi.Sequence.PPQ,24);
   
      Track t1 = generateMusic2(song);
   
   //****  set track name (meta event)  ****
      MetaMessage mmessage = new MetaMessage();
      String TrackName = new String("" + intToKey(specificScale.getKey()) + "" + specificScale.getName() + "" + seed);
      mmessage.setMessage(0x03 ,TrackName.getBytes(), TrackName.length());
      t1.add(new MidiEvent(mmessage,(long)0));
   
      filename = specificScale.getName()+".mid";
   
      int[] allowedTypes = MidiSystem.getMidiFileTypes(song); 
   
      if (allowedTypes.length == 0) 
      { 
         System.err.println("No supported MIDI file types.");
      } 
      else 
      { 
         try
         {
            MidiSystem.write(song, allowedTypes[0], new File(filename));//write to the file
         }
         catch(FileNotFoundException ex) {System.out.println("\n\n\nERROR:\nPlease close the media player and run again");
         }
      }
      System.exit(0);
   }
} 	
