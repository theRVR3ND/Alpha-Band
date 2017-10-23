/**
 * Alpha Band - Multiplayer Rythym Game | ui_Player
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for player info viewing and changing.
 */

import java.util.*;
import javax.sound.midi.*;

public class util_Music{
   
   //********STATIC CONSTANTS********//
   
   private static final byte MAJOR = 0,
                             MINOR = 1,
                             BLUES = 2,
                          HARMONIC = 3,            //Harmoic minor
                        NUM_SCALES = 4;
   
   private static final byte PIANO = 0,
                      CLEAN_GUITAR = 1,
                             DRUMS = 2,
                              BASS = 3,
                       DIST_GUITAR = 4,
                             AGOGO = 5;
   
   private static final byte[][] INTERVALS = new byte[][]{
      {0, 2, 4, 5, 7, 9,  11, 12},
      {0, 2, 3, 5, 7, 9,  11, 12},
      {0, 3, 5, 6, 7, 10, 12},
      {0, 2, 3, 5, 7, 8,  11, 12}
   };
   
   //private static final byte[] KEYS = new byte[] {0, 1, 2, 3, 4, 5, 6};
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 34, 38, 30, 113};
   
   public static void main(String[] args){
      playSong(generateSong((byte)2, (short)(Math.random() * Short.MAX_VALUE)));
      /**
      try{
      System.out.println(MidiSystem.getSynthesizer().getDefaultSoundbank().getInstruments().length + "");
      }catch(Exception e){}
      */
   }
   
   
   //Run
   public static byte[][] generateSong(byte difficulty, short seed){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)(difficulty * 15 + 30 + (rand.nextDouble() * 15));   //multiply by 2 for actual
      final byte measureLength = (byte)(Math.pow(2, difficulty));//how many columns in gen make up one measure
      final short songLength = (short)((rand.nextDouble() * 3 + 3) * measureLength * 40);
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = (byte)(rand.nextDouble() * 9);//KEYS[(int)(rand.nextDouble() * KEYS.length)];
      
      //Generate song data array
      byte[][] gen = new byte[INSTRUMENTS.length][songLength + 1];   //extra column for song info
      
      //Store song info in array
      gen[0][0] = difficulty;
      gen[1][0] = bpm;
      gen[2][0] = scale;
      gen[3][0] = key;
      
      //Fill with default value
      for(byte r = 0; r < gen.length; r++)
         for(short c = 1; c < gen[0].length; c++)
            gen[r][c] = Byte.MIN_VALUE;
      
      
      //Create a diversified beat
      final byte avgInterval = (byte)(rand.nextDouble() * measureLength / 2 + 2);
      
      byte[] beat = new byte[measureLength * (byte)(rand.nextDouble() * difficulty + 4)];
      for(byte i = 0; i < beat.length; i++){
         if(i == 0){
            if(rand.nextBoolean())
               beat[i] = 0;
            else
               beat[i] = Byte.MIN_VALUE;
            continue;
         }
         
         if(rand.nextDouble() < (difficulty + 1) / 10.0){
            if(beat[i - 1] == 0)
               beat[i] = Byte.MIN_VALUE;
            else
               beat[i] = 0;
         }else
            beat[i] = beat[i - 1];
      }
      
      /*
      for(byte i = 0; i < beat.length; i++)
         System.out.print(beat[i] + " ");
      System.out.println("\n" + (Math.pow(2, difficulty)) + "\nbpm: " + bpm + "\nsongLength: " + songLength);
      */
      
      //Repeat beat into song forever-ish
      for(short i = 1; i < gen[0].length; i++){
         gen[DRUMS][i] = beat[(i - 1) % beat.length];
         
         //Check if entire beat can still fit in rest of song length
         if((i - 1) % beat.length == beat.length - 1 && i + beat.length >= gen[0].length){
            break;
         }
      }
      
      for(int r = 0; r < gen.length; r++){
         for(int c = 0; c < gen[0].length; c++){
            System.out.print(gen[r][c] + "\t");
         }
         System.out.println();
      }
      
      return gen;
   }
   
   public static void playSong(byte[][] song){
      MusicPlayer mp = new MusicPlayer(song);
      mp.run();
   }
   
   /**
    * Thread class to play music without interrupting other processes.
    */
   private static class MusicPlayer extends Thread{
      
      private Synthesizer synth;
      
      private MidiChannel[] channels;
      
      private Instrument[] instruments;
      
      private final byte[][] song;
      
      public MusicPlayer(byte[][] song){
         //Initialize stuff
         this.song = song;
         
         //Set up midi
         try{
            synth = MidiSystem.getSynthesizer();
            synth.open();
            
            channels = synth.getChannels();
            instruments = synth.getDefaultSoundbank().getInstruments();
            
            for(byte i = 0; i < INSTRUMENTS.length; i++){
               if(i == DRUMS)
                  channels[9].programChange(instruments[INSTRUMENTS[i]].getPatch().getProgram());
               else
                  channels[i].programChange(instruments[INSTRUMENTS[i]].getPatch().getProgram());
            }
            
         }catch(MidiUnavailableException e){
            e.printStackTrace();
         }
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final byte diff =  song[0][0];
         final byte bpm =   song[1][0];
         final byte scale = song[2][0];
         final byte key =   song[3][0];
         
         //Progress through each beat
         for(short beat = 1; beat < song[0].length; beat++){
            //Play each instrument's note
            for(byte i = 0; i < INSTRUMENTS.length; i++){
               //End previously played note
               if((song[i][beat - 1] != Byte.MIN_VALUE && song[i][beat] != song[i][beat - 1]) || song[i][beat] == Byte.MIN_VALUE){
                  if(i == DRUMS)
                     channels[9].noteOff(30 + song[i][beat - 1]);
                  else
                     channels[i].noteOff(60 + song[i][beat - 1]);
               
               //Play new note
               }else if(beat == 1 || song[i][beat - 1] != song[i][beat]){
                  //Find the note length
                  byte noteLength = 0;
                  for(short j = beat; j < song[0].length; j++){
                     if(song[i][beat] == song[i][j])
                        noteLength++;
                     else
                        break;
                  }
                  
                  //Play the note
<<<<<<< HEAD
                  if(i == DRUMS)
                     channels[9].noteOn(30 + song[i][beat], 50);
                  else
                     channels[i].noteOn(60 + song[i][beat], 50);
=======
                  channels[i].noteOn(60 + song[i][beat], 50);
>>>>>>> 1bb0dee9d52a07453c069fd9a15b4e14c45e6638
               }
            }
            
            //Wait until beat time passes
            try{
               sleep((int)((60000.0 / Math.pow(2, diff)) / bpm));
            }catch(InterruptedException e){
               e.printStackTrace();
            }
         }
      }
   }
}