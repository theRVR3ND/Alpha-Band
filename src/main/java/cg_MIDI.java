/**
 * Alpha Band - Multiplayer Rythym Game | cg_World
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Client-side version of world. Handle client-only actions.
 */

import javax.sound.midi.*;

public class cg_MIDI{
   
   private static MidiChannel[] channels;
   
   private static Instrument[] instruments;
   
   public static void playNote(final byte note, final byte instrument){
      //Set up MIDI if not done already
      if(channels == null){
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
      
      //Play note
      if(instrument == util_Music.DRUMS){ //Percussion
         channels[9].allNotesOff();
         channels[9].noteOn(note, 90);
      
      }else{ //Other instrument
         channels[instrument].allNotesOff();
         channels[instrument].noteOn(note, 100);
      }
   }
}