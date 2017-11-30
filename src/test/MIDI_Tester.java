import javax.sound.midi.*;
import java.awt.event.*;
import javax.swing.*;
public class MIDI_Tester extends JFrame {
    MidiChannel channel;  // The channel we play on: 10 is for percussion
    int velocity = 64;    // Default volume is 50%

    public static void main(String[  ] args) throws MidiUnavailableException
    {
        // We don't need a Sequencer in this example, since we send MIDI
        // events directly to the Synthesizer instead.
        Synthesizer synthesizer = MidiSystem.getSynthesizer( );
        synthesizer.open( );
        JFrame frame = new MIDI_Tester(synthesizer);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(50, 128);  // We use window width as volume control
        frame.setVisible(true);
    }    

    public MIDI_Tester(Synthesizer synth) {
        super("Drums");

        // Channel 10 is the GeneralMidi percussion channel.  In Java code, we
        // number channels from 0 and use channel 9 instead.
        channel = synth.getChannels( )[9];

        addKeyListener(new KeyAdapter( ) {
                public void keyPressed(KeyEvent e) {jkl
                    int key = e.getKeyCode( );
                    if (key >= 35 && key <= 81) {
                        channel.noteOn(key, velocity);
                    }
                }
                public void keyReleased(KeyEvent e) {
                    int key = e.getKeyCode( );
                    if (key >= 35 && key <= 81) channel.noteOff(key);
                }
            });

        addMouseMotionListener(new MouseMotionAdapter( ) {
                public void mouseMoved(MouseEvent e) {
                    velocity = e.getX( );
                }
            });
    }
}