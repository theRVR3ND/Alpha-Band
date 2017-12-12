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
   
   public static final byte PIANO = 0,
                     CLEAN_GUITAR = 1,
                            DRUMS = 2,
                             BASS = 3,
                      DIST_GUITAR = 4,
                            AGOGO = 5;
   
   public static final String[] instruments = new String[] {
      "Piano",
      "Guitar",
      "Drums",
      "Bass",
      "Distorted Guitar",
      "Agogo",
   };
   
   private static final byte[][] INTERVALS = new byte[][]{
      {0, 2, 4, 5, 7, 9,  11, 12},
      {0, 2, 3, 5, 7, 9,  11, 12},
      {0, 3, 5, 6, 7, 10, 12},
      {0, 2, 3, 5, 7, 8,  11, 12}
   };
   
   //private static final byte[] KEYS = new byte[] {0, 1, 2, 3, 4, 5, 6};
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 33, 38, 30, 113};
   
   public static void main(String[] args){
      //playSong(generateSong((byte)2, (short)(Math.random() * Short.MAX_VALUE)));
      playSong(DRUMS, generatePart((byte)2, (short)(Math.random() * Short.MAX_VALUE), DRUMS));
   }
   
   //Generate specified instrument's part
   public static ArrayList<ArrayList<Byte>> generatePart(byte difficulty, short seed, byte instrument){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)(difficulty * 15 + 30 + (rand.nextDouble() * 15));   //multiply by 2 for actual
      final byte measureLength = (byte)(Math.pow(2, difficulty) * 8);//how many columns in gen make up one measure
      final short songLength = (short)((rand.nextDouble() * 3 + 3) * measureLength * 40);//in seconds
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = (byte)(rand.nextDouble() * 9);//KEYS[(int)(rand.nextDouble() * KEYS.length)];
      final int totalBeats = (int)(bpm * songLength / 60.0);
      
      ArrayList<ArrayList<Byte>> song = new ArrayList<>(totalBeats);//For only given instrument
      
      song.add(new ArrayList<Byte>(3));
      song.get(0).add(bpm);
      song.get(0).add(scale);
      song.get(0).add(key);
      
      /*
         One beat looks like this:
         {note1, note2, note3, ... noteX, Byte.MIN_VALUE}
      */
      
      //Generate notes based on instrument
      if(instrument == PIANO){
      
      }else if(instrument == CLEAN_GUITAR){
      
      //Drums
      }else if(instrument == DRUMS){
         //Beat stats
        // byte bassInterval = (byte)(Math.pow(2, 2 * (5 - difficulty))),
         //    snareInterval = (byte)(bassInterval / 2.0),
        //    cymbalInterval = 
             
         //Generate single measure of beat
         ArrayList<ArrayList<Byte>> measure = new ArrayList<>();
         for(byte i = 0; i < measureLength; i++){
            ArrayList<Byte> chord = new ArrayList<>();
            if(i % 2 == 0){
               chord.add((byte)35);
               if(i == 2)
                  chord.add((byte)49);
            }else{
               chord.add((byte)38);
            }
            measure.add(chord);
         }
         
         //Repeat measure into song
         for(int i = 0; i < totalBeats; i++){
            song.add(measure.get(i % measureLength));
         }
      
      //Bass guitar
      }else if(instrument == BASS){
      
      }else if(instrument == DIST_GUITAR){
      
      }else if(instrument == AGOGO){
      
      }else{
         System.out.println("Oh shucks!");
         System.exit(1);
      }
      
      return song;
   }
   
   public static void playSong(final byte instrument, ArrayList<ArrayList<Byte>> song){
      //Set up midi
      try{
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         
         MidiChannel[] channels = synth.getChannels();
         Instrument[] instruments = synth.getDefaultSoundbank().getInstruments();
         
         MusicPlayer mp;
         
         if(instrument != DRUMS)
            mp = new MusicPlayer(channels[instrument], instruments[INSTRUMENTS[instrument]], song);
         else
            mp = new MusicPlayer(channels[9], instruments[INSTRUMENTS[instrument]], song);
         
         mp.run();
         
      }catch(MidiUnavailableException e){
         e.printStackTrace();
      }
   }
   
   /**
    * Thread class to play one player's music without interrupting other processes.
    */
   private static class MusicPlayer extends Thread{
      
      private MidiChannel channel;
      
      private Instrument instrument;
      
      private final ArrayList<ArrayList<Byte>> song;
      
      public MusicPlayer(MidiChannel channel, Instrument instrument, ArrayList<ArrayList<Byte>> song){
         //Initialize stuff
         this.channel = channel;
         this.instrument = instrument;
         this.song = song;
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (short)(song.get(0).get(0) * 4);
         final byte scale = song.get(0).get(1),
                      key = song.get(0).get(2);
         
         HashSet<Byte> currNotes = new HashSet<>();
         ArrayList<Byte> toRemove = new ArrayList<>();
         
         //Progress through each beat
         for(int beat = 1; beat < song.size() - 1; beat++){
            //Play all notes for current beat
            ArrayList<Byte> chord = song.get(beat);
            
            //End previous notes
            for(Byte b : toRemove){
               channel.noteOff(b);
               currNotes.remove(b);
            }
            toRemove.clear();
            
            //Process notes in chord
            for(Byte b : chord){
               //Start note
               if(!currNotes.contains(b)){
                  channel.noteOn(b, 100);
                  currNotes.add(b);
               }
               
               //Note is to be removed
               if(!song.get(beat + 1).contains(b)){
                  toRemove.add(b);
               }
            }
            
            //Wait until beat time passes
            try{
               sleep((int)(60000.0 / bpm));
            }catch(InterruptedException e){
               e.printStackTrace();
            }
         }
      }
   }
}