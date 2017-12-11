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
   
   public static byte[][] generateSong(byte difficulty, short seed){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)(difficulty * 30 + 30 + (rand.nextDouble() * 15));//multiply by 2 for actual
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
      
      
      //Create a simple 4/4 beat - drum set
      final byte avgInterval = (byte)(rand.nextDouble() * measureLength / 2 + 2);
      
      byte[] beat = new byte[16];
      for(byte i = 0; i < beat.length; i += 4){
         beat[i] = 35;
         beat[i+1] = 55;
         beat[i+2] = 35;
         beat[i+3] = 55;
      }
      
      //Bass     
      byte[] bassline = new byte[16];
      for(byte i = 0; i < bassline.length; i+=4){
         bassline[i] = -24;
         bassline[i+1] = Byte.MIN_VALUE;
         bassline[i+2] = -20;
         bassline[i+3] = Byte.MIN_VALUE;
      }
      
      //Piano
      byte[] pianomel = new byte[16];
      for(byte i = 0; i < pianomel.length; i+=4){
         pianomel[i] = Byte.MIN_VALUE;
         pianomel[i+1] = -24;
         pianomel[i+2] = Byte.MIN_VALUE;
         pianomel[i+3] = -20;
      }
   
      
      /*************************************************************************************
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
      **************************************************************************************/
      
      //Repeat beat into song forever-ish
      for(short i = 1; i < gen[0].length; i++){
         gen[DRUMS][i] = beat[(i - 1) % beat.length];
         //gen[BASS][i] = bassline[(i - 1) % bassline.length];
         //gen[PIANO][i] = pianomel[(i - 1) % pianomel.length];
      
      
         
         //Check if entire beat can still fit in rest of song length
         if((i - 1) % beat.length == beat.length - 1 && i + beat.length >= gen[0].length){
            break;
         }
      }
      /*
      for(int r = 0; r < gen.length; r++){
         for(int c = 0; c < gen[0].length; c++){
            System.out.print(gen[r][c] + "\t");
         }
         System.out.println();
      }
      */
      return gen;
   }
   
   //Generate specified instrument's part
   public static ArrayList<byte[]> generatePart(byte difficulty, short seed, byte instrument){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final byte bpm = (byte)(difficulty * 15 + 30 + (rand.nextDouble() * 15));   //multiply by 2 for actual
      final byte measureLength = (byte)(Math.pow(2, difficulty) * 8);//how many columns in gen make up one measure
      final short songLength = (short)((rand.nextDouble() * 3 + 3) * measureLength * 40);//in seconds
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = (byte)(rand.nextDouble() * 9);//KEYS[(int)(rand.nextDouble() * KEYS.length)];
      final int totalBeats = (int)(bpm * songLength / 60.0);
      
      ArrayList<byte[]> song = new ArrayList<>(totalBeats);//For only given instrument
      song.add(new byte[] {bpm, scale, key});
      
      /*
         One beat looks like this:
         {note1, note2, note3, ... noteX, Byte.MIN_VALUE}
      */
      
      //Generate notes based on instrument
      if(instrument == PIANO){
      
      }else if(instrument == CLEAN_GUITAR){
      
      
      //Drums
      }else if(instrument == DRUMS){
         byte[] bassDrum = new byte[measureLength],
               snareDrum = new byte[measureLength],
                  cymbal = new byte[measureLength];
         System.out.println(measureLength + "");
         //Bass drum part
         boolean tag = false;
         for(byte i = 0; i < measureLength; i++){
            byte noteLength = (byte)(rand.nextDouble() * difficulty * 2 + 8.0 / difficulty);
            for(int j = 0; j < noteLength && j + i < measureLength; j++){
               if(tag){
                  bassDrum[j] = 35;
               }else{
                  bassDrum[j] = Byte.MIN_VALUE;
               }
               tag = !tag;
            }
            i += noteLength;
         }
         
         //Concatenate percussion parts
         byte[][] conc = new byte[measureLength][1];
         for(byte r = 0; r < conc.length; r++){
            conc[r][0] = bassDrum[r];
            //conc[r][1] = snareDrum[r];
            //conc[r][2] = cymbal[r];
         }
         
         //Repeat percussion part into song
         addBeat:
            for(int i = 0; i < totalBeats; i++){
               for(byte j = 0; j < conc[i % measureLength].length; j++){
                  if(conc[i % measureLength][j] != Byte.MIN_VALUE){
                     song.add(conc[i % measureLength]);
                     continue addBeat;
                  }
               }
               song.add(new byte[0]);
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
   
   public static void playSong(final byte instrument, ArrayList<byte[]> song){
      //MusicPlayer mp = new MusicPlayer(song);
      //mp.run();
      
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
    * Thread class to play music without interrupting other processes.
    */
   /*
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
               //if(i == DRUMS)
                  //channels[9].programChange(instruments[34].getPatch().getProgram());
               //else
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
               //if((song[i][beat - 1] != Byte.MIN_VALUE && song[i][beat] != song[i][beat - 1]) || song[i][beat] == Byte.MIN_VALUE){
               if(song[i][beat] != song[i][beat - 1] || song[i][beat] == Byte.MIN_VALUE){
                  if(i == DRUMS)
                     channels[9].noteOff(song[i][beat]);
                  else
                     channels[i].noteOff(60 + song[i][beat - 1]);
               
               }
               
               //Play new note
               if(song[i][beat - 1] != song[i][beat]){
                  //Find the note length
                  byte noteLength = 0;
                  for(short j = beat; j < song[0].length; j++){
                     if(song[i][beat] == song[i][j])
                        noteLength++;
                     else
                        break;
                  }
                  
                  //Play the note
                  if(i == DRUMS)
                     channels[9].noteOn(song[i][beat], 100);
                  else
                     channels[i].noteOn(60 + song[i][beat], 50);
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
   */
   //Play one instrument's part of music
   private static class MusicPlayer extends Thread{
      
      private MidiChannel channel;
      
      private Instrument instrument;
      
      private final ArrayList<byte[]> song;
      
      public MusicPlayer(MidiChannel channel, Instrument instrument, ArrayList<byte[]> song){
         //Initialize stuff
         this.channel = channel;
         this.instrument = instrument;
         this.song = song;
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (short)(song.get(0)[0] * 2);
         final byte scale = song.get(0)[1],
                      key = song.get(0)[2];
         System.out.println(bpm);
         HashSet<Byte> currNotes = new HashSet<>();
         
         /**
         for(int i = 0; i < song.size(); i++){
            for(int x = 0; x < song.get(i).length; x++)
               System.out.print(song.get(i)[x] + " ");
            System.out.println();
         }
         /**/
         
         //Progress through each beat
         for(int beat = 1; beat < song.size(); beat++){
            //Play all notes for current beat
            byte[] chord = song.get(beat);
            
            //End notes
            for(Byte b : currNotes){
               boolean playingDuringNextBeat = false;
               
               for(byte a : chord)
                  if(a == b){
                     playingDuringNextBeat = true;
                     break;
                  }
               
               if(!playingDuringNextBeat){
                  channel.noteOff(b);
                  currNotes.remove(b);
               }
            }
            
            //Start notes
            for(Byte b : chord){
               if(!currNotes.contains(b)){
                  channel.noteOn(b, 100);
                  currNotes.add(b);
               }
            }
            /*
            //End previously played note
            if(song[beat] != song[beat - 1] || song[beat] == Byte.MIN_VALUE){
               if(i == DRUMS)
                  channels[9].noteOff(song[beat]);
               else
                  channels[i].noteOff(60 + song[beat - 1]);
            
            }
            
            //Play new note
            if(song[beat - 1] != song[beat]){
               //Play the note
               if(i == DRUMS)
                  channels[9].noteOn(song[i][beat], 100);
               else
                  channels[i].noteOn(60 + song[i][beat], 50);
            }
            */
            
            for(int x = 0; x < chord.length; x++)
               System.out.print(chord[x] + " ");
            System.out.println();
            
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