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
                             MINOR = 1,
                             BLUES = 2,
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
   
   private static final byte[][] INTERVALS = new byte[][]{
      {0, 2, 4, 5, 7, 9,  11, 12},
      {0, 2, 3, 5, 7, 9,  11, 12},
      {0, 3, 5, 6, 7, 10, 12},
      {0, 2, 3, 5, 7, 8,  11, 12}
   };
   
   public static final short[] INSTRUMENTS = new short[] {0, 27, 33, 34, 30, 113};
   
   public static void main(String[] args){
      //playSong(generateSong((byte)2, (short)(Math.random() * Short.MAX_VALUE)));
      final byte toPlay = PIANO;
      
      /****
      playSong(toPlay, generatePart((byte)4, (short)(Math.random() * Short.MAX_VALUE), toPlay));
      //                            0 to 4
      /******/
      
      /****/
      ArrayList<HashMap<Short, HashSet<Byte>>> allParts = new ArrayList<>();
      final short seed = (short)(Math.random() * Short.MAX_VALUE);
      for(byte i = 0; i < NUM_INSTRUMENTS; i++){
         allParts.add(generatePart((byte)2, seed, i));
      }
      playSongs(allParts);
      /*****/
   }
   
   public static byte generateBPM(byte difficulty, short seed){
      return (byte)(Math.pow(8, difficulty / 2.0) + 30 + ((new Random(seed)).nextDouble() * 15));// * 2 for actual
   }
   
   //Generate specified instrument's part
   public static HashMap<Short, HashSet<Byte>> generatePart(final byte difficulty, final short seed, final byte instrument){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = generateBPM(difficulty, seed);
      final byte measureLength = (byte)(2 * difficulty + 4);//how many columns in gen make up one measure
      final short songLength = (short)(measureLength * (rand.nextInt(20) + 40));//in beats
      final byte scale = 0;//(byte)(rand.nextInt(INTERVALS.length));
      final byte key = 48;//(byte)(rand.nextInt(12) + 48);
      final byte beatInterval = (byte)(measureLength / (difficulty + 2));
      System.out.println("bpm: " + bpm + "\nmeasure length:" + measureLength + "\nsong length:" + songLength + "\nscale:" + scale + "\nkey:" + key + "\nbeat interval:" + beatInterval);
      HashMap<Short, HashSet<Byte>> song = new HashMap<>();
      
      //Add neccessary song info into song data structure
      HashSet<Byte> info = new HashSet<Byte>();
      info.add(bpm);
      song.put((short)0, info);
      
      //Idk what this is called in music. Generating each melody?
      ArrayList<Byte> rootInd = new ArrayList<>(songLength / measureLength);
      rootInd.add((byte)(0));
      for(byte i = 1; i < songLength / measureLength; i++){
         rootInd.add((byte)((rand.nextInt(6) * 2 - 5) % INTERVALS[scale].length));
         //rootInd.add((byte)(i));
      }
      
      //---Generate notes based on instrument---//sammy was here
      //PIANO
      if(instrument == PIANO){
         short ind = 0;
         /*
         for(short beat = 0; beat < songLength - 1; beat += measureLength){
            HashSet<Byte> chord = new HashSet<>();
            
            //Chord
            final byte rootNote;
            if(rootInd.get(ind) < 0){
               rootNote = (byte)(key + INTERVALS[scale][rootInd.get(ind) + INTERVALS[scale].length] - 12);
            }else if(rootInd.get(ind) >= INTERVALS[scale].length){
               rootNote = (byte)(key + INTERVALS[scale][rootInd.get(ind) % INTERVALS[scale].length] + 12);
            }else{
               rootNote = (byte)(key + INTERVALS[scale][rootInd.get(ind)]);
            }
            
            //for(byte i = 0; i < difficulty / 2 + 2; i++){
            for(byte i = 0; i < 2; i++){
               byte addNote = (byte)(rootNote + i * 4 - 12);
               chord.add(addNote);
            }
            
            //Melody
            for(short t = beat; t < beat + measureLength; t += randInt(2) + 1){
               
            }
            
            ind++;
            
            if(!chord.isEmpty())
               song.put((short)(beat + 1), chord);
         }*/
      
      //GUITAR
      }else if(instrument == CLEAN_GUITAR){
      
      //DRUMS
      }else if(instrument == DRUMS){
         //Beat statistacs
         final byte snareInterval = (byte)(beatInterval + (rand.nextInt(1) + 1) * 2);
         byte cymbalBeat = (byte)(rand.nextInt(measureLength));
         if(cymbalBeat % beatInterval == 0)
            cymbalBeat--;
         System.out.println("snare interval: " + snareInterval + "\ncymbal beat: " + cymbalBeat);
         
         for(short beat = 1; beat < songLength - 1; beat++){
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
               if((beat % measureLength) % snareInterval == 0){
                  if(beat >= measureLength && beat < songLength - measureLength)
                     chord.add((byte)38);
               }
            }
            
            if(!chord.isEmpty())
               song.put((short)(beat + 1), chord);
         }
      
      //BASS GUITAR
      }else if(instrument == BASS){
         short ind = 0;
         for(short beat = measureLength; beat < songLength - 1; beat += measureLength){
            HashSet<Byte> chord = new HashSet<>();
            
            final byte note;
            if(rootInd.get(ind) < 0){
               note = (byte)(key + INTERVALS[scale][rootInd.get(ind) + INTERVALS[scale].length] - 12);
            }else if(rootInd.get(ind) >= INTERVALS[scale].length){
               note = (byte)(key + INTERVALS[scale][rootInd.get(ind) % INTERVALS[scale].length] + 12);
            }else{
               note = (byte)(key + INTERVALS[scale][rootInd.get(ind)]);
            }
            chord.add(note);
            
            ind++;
            if(ind >= rootInd.size())
               ind = 0;
            
            if(!chord.isEmpty())
               song.put((short)(beat + 1), chord);
         }
      
      //DISTORTED GUITAR
      }else if(instrument == DIST_GUITAR){
      
      //AGOGO
      }else if(instrument == AGOGO){
         /*
         short playBeat = (short)(rand.nextInt(songLength));
         for(short i = 0; i < songLength; i++){
            ArrayList<Byte> chord = new ArrayList<Byte>();
            if(i == playBeat){
               chord.add((byte)60);
            }
            song.add(chord);
         }
         */
      
      }else{
         System.out.println("Oh shucks!");
         System.exit(1);
      }
      return song;
   }
   
   public static void playSong(final byte instrument, HashMap<Short, HashSet<Byte>> song){
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
   
   public static void playSongs(ArrayList<HashMap<Short, HashSet<Byte>>> songs){
      Synthesizer synth = null;
      MidiChannel[] channels = null;
      Instrument[] instruments = null;
      
      try{
         synth = MidiSystem.getSynthesizer();
         synth.open();
         
         channels = synth.getChannels();
         instruments = synth.getDefaultSoundbank().getInstruments();
      }catch(MidiUnavailableException e){
         e.printStackTrace();
      }
      
      ArrayList<MusicPlayer> players = new ArrayList<>();
      
      for(byte i = 0; i < NUM_INSTRUMENTS; i++){
         MusicPlayer mp;
         
         if(i != DRUMS)
            mp = new MusicPlayer(channels[i], instruments[INSTRUMENTS[i]], songs.get(i));
         else
            mp = new MusicPlayer(channels[9], instruments[INSTRUMENTS[i]], songs.get(i));
         
         players.add(mp);
      }
      
      for(MusicPlayer mp : players)
         mp.start();
   }
   
   /**
    * Thread class to play one player's music without interrupting other processes.
    */
   private static class MusicPlayer extends Thread{
      
      private MidiChannel channel;
      
      private Instrument instrument;
      
      private final HashMap<Short, HashSet<Byte>> song;
      
      public MusicPlayer(MidiChannel channel, Instrument instrument, HashMap<Short, HashSet<Byte>> song){
         //Initialize stuff
         this.channel = channel;
         this.instrument = instrument;
         this.song = song;
         
         channel.programChange(instrument.getPatch().getProgram());
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (Byte)(song.get((short)0).iterator().next());
         
         //Find length of song
         Iterator<Short> keys = song.keySet().iterator();
         short songLength = 0;
         while(keys.hasNext())
            songLength = (short)(Math.max((Short)(keys.next()), songLength));
         
         HashSet<Byte> currNotes = new HashSet<>();
         
         //Progress through each beat
         for(short beat = 1; beat < songLength + 1; beat++){
            //Play all notes for current beat
            HashSet<Byte> chord = song.get(beat);
            
            System.out.print(beat + " - ");
            if(chord != null){
               //Play new notes
               for(Byte note : chord){
                  if(!currNotes.contains(note)){
                     channel.noteOn(note, 100);
                     currNotes.add(note);
                  }
                  System.out.print(note + " ");
               }
               
               //Wait until beat time passes
               try{
                  sleep((int)(30000.0 / bpm));
               }catch(InterruptedException e){}
            
            //No notes to play
            }else{
               System.out.println();
               
               //Wait until beat time passes
               try{
                  sleep((int)(30000.0 / bpm));
               }catch(InterruptedException e){}
               
               continue;
            }
               System.out.println();
            
            //End applicable notes
            HashSet<Byte> nextChord = song.get((short)(beat + 1));
            
            for(Byte note : chord){
               if(nextChord == null || !nextChord.contains(note)){
                  channel.noteOff(note);
                  currNotes.remove(note);
               }
            }
         }
      }
   }
}