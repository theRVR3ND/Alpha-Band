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
   /*
   private static final byte MAJOR = 0,
                             BLUES = 1,
                             MINOR = 2,
                          HARMONIC = 3,            //Harmoic minor
                        NUM_SCALES = 4;
   */
   public static final byte PIANO = 0,
                     CLEAN_GUITAR = 1,
                            DRUMS = 2,
                             BASS = 3,
                      DIST_GUITAR = 4,
                            AGOGO = 5,
                  NUM_INSTRUMENTS = 6;
   
   public static final String[] instruments = new String[] {
      "Piano",
      "Guitar",
      "Drums",
      "Bass",
      "Distorted Guitar",
      "Agogo",
   };
   
   private static final byte[][] INTERVALS = new byte[][] {
      {0, 2, 4, 5, 7, 9,  11, 12},
      {0, 3, 5, 6, 7, 10, 12},
      {0, 2, 3, 5, 7, 9,  11, 12},
      {0, 2, 3, 5, 7, 8,  11, 12}
   };
   
   private static final byte[][] PENTATONICS = new byte[][] {
      {0, 2, 4, 7, 9,  12},   //Major pentatonic interval
      {0, 3, 5, 7, 10, 12}    //Minor pentatonic
   };
   
   //Chord intervals for pedal tones
   private static final byte[][][] CHORDS = new byte[][][] {
      {                 //In MusicSketchpad:
         {0, 7,  4},    //maj
         {0, 12, 7},    //5
         {0, 12, 0},    //oct
         {0, 12, 7},    //5
         {0, 7,  3},    //m
         {0, 7,  4}     //maj
      },
      {
         {0, 7,  3},    //m
         {0, 7,  4},    //maj
         {0, 12, 7},    //5
         {0, 12, 0},    //oct
         {0, 12, 7},    //5
         {0, 7,  3},    //m
      },
   };
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 33, 34, 30, 113};
   
   public static void main(String[] args){
      ArrayList<HashMap<Short, HashSet<Byte>>> song = new ArrayList<>();
      final short seed = (short)(Math.random() * Short.MAX_VALUE);
      for(byte i = 0; i < NUM_INSTRUMENTS; i++){
         song.add(generatePart((byte)4, seed, i));
      }
      
      //Print song
      System.out.println("BEAT      PIANO         GUITAR        DRUMS         BASS          DIST_GUIT     AGOGO");
      for(short b = 1; b < 100; b++){
         System.out.print(b + "\t-      ");
         for(byte i = 0; i < song.size(); i++){
            String toPrint = "";
            if(song.get(i).get(b) != null){
               for(Byte n : song.get(i).get(b))
                  toPrint += n + " ";
            }
            while(toPrint.length() < 14)
               toPrint += " ";
            System.out.print(toPrint);
         }
         System.out.println();
      }
      
      playSong(song);
   }
   
   public static byte generateBPM(byte difficulty, short seed){
      return (byte)(Math.pow(9, difficulty / 2.0) + 30 + 5 * ((new Random(seed)).nextInt(5) - 2));// * 2 for actual
   }
   
   //Generate specified instrument's part
   public static HashMap<Short, HashSet<Byte>> generatePart(final byte difficulty, final short seed, final byte instrument){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = generateBPM(difficulty, seed);
      final byte measureLength = (byte)(2 * difficulty + 4);//how many columns in gen make up one measure
      final short songLength = (short)(measureLength * (rand.nextInt(20) + 40));//in beats
      final byte scale = (byte)(rand.nextInt(INTERVALS.length));
      final byte key = (byte)(rand.nextInt(12) + 60);
      final byte beatInterval = (byte)(measureLength / (difficulty + 2));
      //System.out.println("bpm: " + (bpm * 2) + "\nmeasure length:" + measureLength + "\nsong length:" + songLength + "\nscale:" + scale + "\nkey:" + key + "\nbeat interval:" + beatInterval);
      HashMap<Short, HashSet<Byte>> song = new HashMap<>();
      
      //Add neccessary song info into song data structure
      HashSet<Byte> info = new HashSet<Byte>();
      info.add(bpm);
      song.put((short)0, info);
      
      //---Generate notes based on instrument---//sammy was here
      //PIANO
      if(instrument == PIANO){
         byte root = (byte)(key + PENTATONICS[scale / 2][0] - 24);
         
         for(short beat = 1; beat < songLength; beat++){ 
            HashSet<Byte> chord = new HashSet<>();
            
            final byte pentatonicsIndex = (byte)(rand.nextInt(PENTATONICS[scale / 2].length));
            
            //Pedal tone
            if(beat % measureLength == 0){
               for(byte i = 0; i < difficulty / 2 + 1; i++){
                  chord.add((byte)(root + CHORDS[scale / 2][pentatonicsIndex][i]));
               }
               chord.add((byte)(root + 12));
            
            //Melody
            }else if(beat % beatInterval == 0){
               root = (byte)(key + PENTATONICS[scale / 2][pentatonicsIndex] - 24);
               chord.add((byte)(root + 12));
            }
            
            if(!chord.isEmpty())
               song.put(beat, chord);
         }
      
      //GUITAR
      }else if(instrument == CLEAN_GUITAR){
      
      //DRUMS
      }else if(instrument == DRUMS){
         //Beat statistacs
         //*
         final byte snareInterval = (byte)(beatInterval + (rand.nextInt(1) + 1) * 2);
         byte cymbalBeat = (byte)(rand.nextInt(measureLength));
         if(cymbalBeat % beatInterval == 0)
            cymbalBeat--;
         //System.out.println("snare interval: " + snareInterval + "\ncymbal beat: " + cymbalBeat);
         
         for(short beat = 1; beat < songLength; beat++){
            HashSet<Byte> chord = new HashSet<>();
            //Cymbal
            if(beat % measureLength == cymbalBeat){
               if(beat >= measureLength && beat < songLength - measureLength)
                  chord.add((byte)49);
            }else{
               //Bass drum
               if(beat % beatInterval == 0 || beat % measureLength == 0){
                  if(beat + beatInterval >= measureLength && beat < songLength - measureLength)
                     chord.add((byte)35);
               }
               
               //Snare
               if((beat % measureLength) % snareInterval == 0 && beat % measureLength != 0){
                  if(beat >= measureLength && beat < songLength - measureLength)
                     chord.add((byte)38);
               }
            }
            
            if(!chord.isEmpty())
               song.put(beat, chord);
         }
         //*/
      
      //BASS GUITAR
      }else if(instrument == BASS){
         byte root = (byte)(key + PENTATONICS[scale / 2][0] - 24);
         
         for(short beat = beatInterval; beat < songLength; beat += 4 * beatInterval){ 
            HashSet<Byte> chord = new HashSet<>();
            
            //Melody
            final byte pentatonicsIndex = (byte)(rand.nextInt(PENTATONICS[scale / 2].length));
            root = (byte)(key + PENTATONICS[scale / 2][pentatonicsIndex] - 24);
            chord.add(root);
            
            if(!chord.isEmpty())
               song.put(beat, chord);
         }
      
      //DISTORTED GUITAR
      }else if(instrument == DIST_GUITAR){
      
      //AGOGO
      }else if(instrument == AGOGO){
         short playBeat = (short)(rand.nextInt(songLength));
         HashSet<Byte> note = new HashSet<>();
         note.add((byte)(rand.nextInt(60) + 30));
         song.put(playBeat, note);
      
      }else{
         System.out.println("Oh shucks!");
         System.exit(1);
      }
      return song;
   }
   
   public static void playSong(ArrayList<HashMap<Short, HashSet<Byte>>> song){
      MusicPlayer mp = new MusicPlayer(song);
      mp.start();
   }
   
   //Play multiple player's music simultaneously
   private static class MusicPlayer extends Thread{
      
      private MidiChannel[] channels;
      
      private Instrument[] instruments;
      
      private final ArrayList<HashMap<Short, HashSet<Byte>>> song;
      
      public MusicPlayer(ArrayList<HashMap<Short, HashSet<Byte>>> song){
         //Initialize stuff
         this.song = song;
      
         try{
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            
            channels = synth.getChannels();
            instruments = synth.getDefaultSoundbank().getInstruments();
            System.out.println("Dropping the bass: " + instruments[INSTRUMENTS[BASS]]);
            //Set channels' instruments
            for(byte i = 0; i < channels.length; i++){
               channels[i].programChange(instruments[i].getPatch().getProgram());
            }
         }catch(MidiUnavailableException e){
            e.printStackTrace();
         }
         
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (short)(2 * (Byte)(song.get(0).get((short)0).iterator().next()));
         
         //Track currently playing notes
         ArrayList<HashSet<Byte>> currNotes = new ArrayList<>();
         for(byte i = 0; i < song.size(); i++)
            currNotes.add(new HashSet<Byte>());
         
         //Progress through each beat
         short beat = 1;
         while(true){
            //Track start time of loop
            final long startTime = System.currentTimeMillis();
            
            //Play chord for each instrument in current beat
            for(byte instrument = 0; instrument < song.size(); instrument++){
               //Current beat's notes to play
               HashSet<Byte> chord = song.get(instrument).get(beat);
               
               //End applicable notes
               if(beat > 1){
                  HashSet<Byte> prevChord = song.get(instrument).get((short)(beat - 1));
                  
                  if(prevChord != null){
                     for(Byte note : prevChord){
                        if(chord == null || !chord.contains(note)){
                           currNotes.get(instrument).remove(note);
                        }
                     }
                  }
               }
               
               //Play new notes
               if(chord != null){
                  for(Byte note : chord){
                     if(!currNotes.get(instrument).contains(note)){
                        //Percussion
                        if(instrument == DRUMS)
                           channels[9].noteOn(note, 20);
                        //Other instrument
                        else
                           channels[instrument].noteOn(note, 30);
                        
                        currNotes.get(instrument).add(note);
                     }
                  }
               }
            }
            
            //Wait until next beat
            try{
               int sleepTime = (int)(startTime + 60000.0 / bpm - System.currentTimeMillis());
               if(sleepTime > 0)
                  sleep(sleepTime);
            }catch(InterruptedException e){}
            
            beat++;
         }
      }
   }
}