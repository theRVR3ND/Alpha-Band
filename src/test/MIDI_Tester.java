import javax.sound.midi.*;

public class MIDI_Tester{
   public static void main(String[] args){
      try{
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         MidiChannel[] channels = synth.getChannels();
         Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
         
         int instrument = 81;
         
         channels[0].programChange(instr[instrument].getPatch().getProgram());
         
         /*
         System.out.println(instr[instrument].toString());
         for(int i = 0; i < 20; i++){
            Thread.sleep(100);
            channels[0].noteOn(60 + i, 100);
            Thread.sleep(1000);
            channels[0].noteOff(60 + i, 100);
         }*/
         //for(int i = 0; i < instr.length; i++)
            System.out.println(channels.length + "");
            System.out.println(instr.length + "");
      }catch (Exception ignored){}
   }
}