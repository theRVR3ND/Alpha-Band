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
   
   public static final byte[] INSTRUMENTS = new byte[] {-127, -100, 99, -89, -97, -14};  //add Byte.MAX_VALUE to get actual
   
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
      
      //Store song info in array
      gen[0][0] = bpm;
      gen[1][0] = scale;
      gen[2][0] = key;
      
      //Make a beat
      
      
      //Generate piano part
      //for(short i = 0; i < gen.length; i++){
         
      //}
      
      return gen;
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
         final byte bpm = song[0][0];
         final byte scale = song[1][0];
         final byte key = song[2][0];
         
         //Play song
         short curr = 1;
         while(curr < song.length){
            for(byte i = 0; i < song[0].length; i++){
               if(song[curr][i] != Byte.MIN_VALUE){
                  channels[curr].noteOff(60 + song[curr][i]);
               }else{
                  channels[curr].noteOn(60 + song[curr][i], 100);
               }
            }
            try{
               sleep((long)(60000.0 / bpm));
            }catch(InterruptedException e){}
         }
         
      }
   }
}