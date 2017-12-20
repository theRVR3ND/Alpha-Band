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
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 33, 38, 30, 113};
   
   public static void main(String[] args){
      //playSong(generateSong((byte)2, (short)(Math.random() * Short.MAX_VALUE)));
      final byte toPlay = PIANO;
      playSong(toPlay, generatePart((byte)2, (short)(Math.random() * Short.MAX_VALUE), toPlay));
   }
   
   //Generate specified instrument's part
   public static ArrayList<ArrayList<Byte>> generatePart(byte difficulty, short seed, byte instrument){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)(difficulty * 10 + 20 + (rand.nextDouble() * 15));//multiply by 2 for actual
      final byte measureLength = (byte)(Math.pow(2, difficulty) + 4);//how many columns in gen make up one measure
      final short songLength = (short)((rand.nextDouble() * 3 + 3) * measureLength * 40);//in beats
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = (byte)(rand.nextInt(12));
      
      ArrayList<ArrayList<Byte>> song = new ArrayList<>(songLength);//For only given instrument
      
      song.add(new ArrayList<Byte>(3));
      song.get(0).add(bpm);
      song.get(0).add(scale);
      song.get(0).add(key);
      
      //---Generate notes based on instrument---//
      //PIANO
      if(instrument == PIANO){
         byte prevNote = key;
         for(short i = 0; i < songLength; i++){
            
         }
         
      //GUITAR
      }else if(instrument == CLEAN_GUITAR){
      
      //DRUMS
      }else if(instrument == DRUMS){
         //Beat statistacs
         final byte bassDrumInterval = (byte)((rand.nextInt(4) * (4 - difficulty)) + (4 - difficulty)),
                   snareDrumInterval = (byte)((rand.nextInt(2) * (4 - difficulty)) + (4 - difficulty));
         
         byte cymbalBeat;
         do{
            cymbalBeat = (byte)(rand.nextInt(measureLength));
         }while(cymbalBeat % bassDrumInterval == 0 || cymbalBeat % snareDrumInterval == 0);
         
         ArrayList<ArrayList<Byte>> fadeMeasure = new ArrayList<>(measureLength),
                                    mainMeasure = new ArrayList<>(measureLength);
         
         for(byte i = 0; i < measureLength; i++){
            ArrayList<Byte> fadeChord = new ArrayList<>(),
                            mainChord = new ArrayList<>();
            
            if(i % bassDrumInterval == 0){
               fadeChord.add((byte)35);
               mainChord.add((byte)35);
            
            }else if(i % snareDrumInterval == 0){
               mainChord.add((byte)38);
            }
            
            if(i == cymbalBeat){
               mainChord.add((byte)49);
            }
            
            fadeMeasure.add(fadeChord);
            mainMeasure.add(mainChord);
         }
         
         //Repeat measure into song
         for(short i = 0; i < songLength; i++){
            //Intro/outro portion of song
            if(i < songLength / 50.0 || i > songLength - songLength / 50.0){
               song.add(fadeMeasure.get(i % measureLength));
            
            //Main portion of song
            }else{
               song.add(mainMeasure.get(i % measureLength));
            }
         }
      
      //BASS GUITAR
      }else if(instrument == BASS){
         ArrayList<ArrayList<Byte>> measure = new ArrayList<>();
         for(byte i = 0; i < measureLength; i++){
            ArrayList<Byte> chord = new ArrayList<>();
            if(i % 2 == 0){
               chord.add((byte)40);
               if(i == 2)
                 chord.add((byte)41);
            }else{
               chord.add((byte)42);
            }
            measure.add(chord);
         }
         
         for(short i = 0; i < songLength; i++){
            song.add(measure.get(i % measureLength));
         }
      
      //DISTORTED GUITAR
      }else if(instrument == DIST_GUITAR){
      
      //AGOGO
      }else if(instrument == AGOGO){
         short playBeat = (short)(rand.nextInt(songLength));
         for(short i = 0; i < songLength; i++){
            ArrayList<Byte> chord = new ArrayList<Byte>();
            if(i == playBeat){
               chord.add((byte)60);
            }
            song.add(chord);
         }
      
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
         
         channel.programChange(instrument.getPatch().getProgram());
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
         for(short beat = 1; beat < song.size() - 1; beat++){
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