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
   
   private static final byte[] KEYS = new byte[] {0, 1, 2, 3, 4, 5, 6};
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 38, 38, 30, 113};
   
   public static void main(String[] args){
      playSong(generateSong((byte)0, (short)0));
   }
   
   //Run
   public static byte[][] generateSong(byte difficulty, short seed){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)((difficulty / 50.0) * rand.nextDouble() * 85 - 97);   //add Byte.MAX_VALUE for actual
      final short songLength = (short)((rand.nextDouble() * 2.5 + 0.5) * (bpm + Byte.MAX_VALUE));
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = KEYS[(int)(rand.nextDouble() * KEYS.length)];
      
      //Generate song data array
      byte[][] gen = new byte[INSTRUMENTS.length][songLength + 1];   //extra column for song info
      
      gen = new byte[][] {
         {0, 0, 0, 0, 1, 3, 1, 5, 1, 6, 0},
         {0,  1, 0, 8, 8, 1, 3, 2, 3, 8, 0},
         {0,  5, 7, 8, 0, 7, 7, 7, 4, 0, 0},
         /*
         {0,  1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
         {0,  1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
         {0,  1, 1, 1, 0, 0, 0, 0, 0, 0, 0}
         */
      };
      
      //Store song info in array
      gen[0][0] = (byte)((bpm + Byte.MIN_VALUE) / 2);
      gen[1][0] = scale;
      gen[2][0] = key;
      
      for(byte r = 0; r < gen.length; r++)
         for(byte c = 0; c < gen[0].length; c++){
            if(gen[r][c] == 0)
               gen[r][c] = Byte.MIN_VALUE;
         }
      
      return gen;
   }
   
   public static void playSong(byte[][] song){
      MusicPlayer mp = new MusicPlayer(song);
      mp.run();
   }
   
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
               channels[i].programChange(instruments[INSTRUMENTS[i]].getPatch().getProgram());
            }
         }catch(MidiUnavailableException e){}
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final byte bpm = (byte)(song[0][0] * 2);
         final byte scale = song[1][0];
         final byte key = song[2][0];
         
         //Song things
         short beat = 1;
         
         //Progress through each beat
         while(beat < song[0].length){
            //Play each instrument's note
            for(byte i = 0; i < song.length; i++){
               if(song[i][beat] == Byte.MIN_VALUE){
                  channels[i].noteOff(60 + song[i][beat]);
               }else if(beat == 0 || song[i][beat - 1] != song[i][beat]){
                  //Find the note length
                  byte noteLength = 0;
                  for(short j = beat; j < song[0].length; j++){
                     if(song[i][j] == song[i][j])
                        noteLength++;
                  }
                  
                  //Play the note
                  channels[i].noteOn(60 + song[i][beat], 100 * noteLength);
               }
            }
            
            //Wait until beat time passes
            try{
               sleep((long)(60000.0 / bpm));
            }catch(InterruptedException e){}
            
            beat++;
         }
      }
   }
}