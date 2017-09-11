import javax.sound.midi.*;

public class MIDI_Tester{
   public static void main(String[] args){
      try{
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         
         final MidiChannel[] mc = synth.getChannels();
         
         Instrument[] inst = synth.getDefaultSoundbank().getInstruments();
         
         mc[5].noteOn(60, 600);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}