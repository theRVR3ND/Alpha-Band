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
   
   private static final byte[] INSTRUMENTS = new byte[] {-127, -100, 99, -89, -97, -14};  //add Byte.MAX_VALUE to get actual
   
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
   
   public static void playSong(byte[][] song){
      //Extract some song information
      final byte bpm = song[0][0];
      final byte scale = song[1][0];
      final byte key = song[2][0];
      
      //Play!
      try{
         //Set up midi
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         MidiChannel[] channels = synth.getChannels();
         Instrument[] instruments = synth.getDefaultSoundbank().getInstruments();
         
         //Time to start playing music
         final long startTime = System.currentTimeMillis() + 2000;
         
         //Go through and play it
         MusicPlayer[] players = new MusicPlayer[song.length];
         
         for(byte i = 0; i < players.length; i++){
            players[i] = new MusicPlayer(channels[i], instruments[INSTRUMENTS[i]], song[i], bpm, scale, key, startTime);
            players[i].start();
         }
         
      }catch(MidiUnavailableException e){
         System.out.println("Could not play music.");
         e.printStackTrace();
      }
   }
   
   //Thread that plays one part (one instrument) of song
   private static class MusicPlayer extends Thread{
      
      private MidiChannel channel;
      
      private Instrument instrument;
      
      private byte[] play;
      
      private final byte bpm;
      
      private final byte scale;
      
      private final byte key;
      
      private long lastUpdateTime;
      
      public MusicPlayer(MidiChannel channel, Instrument instrument, byte[] play, byte bpm, byte scale, byte key, long startTime){
         this.channel = channel;
         this.instrument = instrument;
         this.play = play;
         this.bpm = bpm;
         this.scale = scale;
         this.key = key;
         lastUpdateTime = startTime;
      }
      
      public void run(){
         //Set channel instrument
         channel.programChange(instrument.getPatch().getProgram());
         
         //Process music
         for(short i = 1; i < play.length; i++){
            //Make sure loop rate is synced with bpm
            long waitTime = (long)((lastUpdateTime + 60000.0 / bpm) - System.currentTimeMillis());
            if(waitTime > 0){
               try{
                  sleep(waitTime);
               }catch(InterruptedException e){}
            }
            
            //What to play
            byte note = play[i];
            
            if(note != 0){
               //Find note duration
               byte duration = 0;
               for(int j = i; j < play.length; j++){
                  if(play[j] == note)
                     duration++;
                  else
                     break;
               }
               
               //Play
               try{
                  channel.noteOn(60 + note, 100);
                  sleep((duration / bpm) * 60000);
                  channel.noteOff(60 + note, 100);
               }catch(InterruptedException e){}
               
               i += duration;
            }
            
            lastUpdateTime = System.currentTimeMillis();
         }
      }
   }
}