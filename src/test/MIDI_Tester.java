import javax.sound.midi.*;

public class MIDI_Tester{
   public static void main(String[] args){
      try{
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         MidiChannel[] channels = synth.getChannels();
         Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
         
         channels[0].programChange(instr[0].getPatch().getProgram());
      
         Thread.sleep(100);
         channels[0].noteOn(60, 100);
         Thread.sleep(1000);
      }catch (Exception ignored){}
   }
}