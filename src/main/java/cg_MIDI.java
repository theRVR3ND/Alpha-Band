/**
 * Alpha Band - Multiplayer Rythym Game | cg_MIDI
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Client MIDI player.
 */

import javax.sound.midi.*;

public class cg_MIDI{
   
   private static MidiChannel[] channels;
   
   private static Instrument[] instruments;
   
   private static final byte[] volumes = new byte[] { //Volume balancing array
      75, //Piano
      55, //Guitar
      85, //Drums
      65, //Bass
      30, //Distorted Guitar
      80  //Agogo
   };
   
   public static void loadChannels(){
      try{
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         
         channels = synth.getChannels();
         instruments = synth.getDefaultSoundbank().getInstruments();
         
         //Set channels' instruments
         for(byte i = 0; i < util_Music.INSTRUMENTS.length; i++){
            channels[i].programChange(instruments[util_Music.INSTRUMENTS[i]].getPatch().getProgram());
         }
      }catch(MidiUnavailableException e){
         e.printStackTrace();
      }
   }
   
   public static void playNote(final byte note, final byte instrument){
      //Play note
      if(instrument == util_Music.DRUMS){ //Percussion
         channels[9].allNotesOff();
         channels[9].noteOn(note, (int)(volumes[instrument] * ui_Menu.settings.getVolume() / 100.0));
      
      }else{ //Other instrument
         channels[instrument].allNotesOff();
         channels[instrument].noteOn(note, (int)(volumes[instrument] * ui_Menu.settings.getVolume() / 100.0));
      }
   }
   
   public static void silence(){
      for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
         if(i == util_Music.DRUMS)
            channels[9].allNotesOff();
         else
            channels[i].allNotesOff();
      }
   }
}