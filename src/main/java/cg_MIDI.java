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
         channels[9].noteOn(note, ui_Menu.settings.getVolume());
      
      }else{ //Other instrument
         channels[instrument].allNotesOff();
         channels[instrument].noteOn(note, ui_Menu.settings.getVolume());
      }
      System.out.println(note + "");
   }
}