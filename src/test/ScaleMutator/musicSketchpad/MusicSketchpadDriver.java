//Rev Dr. Douglas R Oberle, Washington DC, 2015
import javax.swing.JFrame;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class MusicSketchpadDriver								   //Driver Program
{
   public static MusicSketchpadPanel screen;					   //Panel window


   public static void main(String[]args)
   {
      screen = new MusicSketchpadPanel();                   /*TODO:, (F6 SHIFT-F6)harmony matched/not*/
      JFrame frame = new JFrame("Music Sketchpad:(ESC)quit, (F1 SHFT-F1)flip horiz/vert, (F2 SHFT-F2)fit note/chord, (H)ard fit, (F3 SHFT-F3)scale oct up/dn, (F4 SHFT-F4)chart oct up/dn, (F5 SHFT-F5)chart step up/dn");	      //window title
      frame.setSize(1400, 800);					               //Size of window
      frame.setLocation(0, 0);				                  //location of window on the screen
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setContentPane(screen);		
      frame.setVisible(true);
      frame.addKeyListener(new listen());		               //Get input from the keyboard
   
   }

   private static boolean shiftIsPressed=false; 

   public static class listen implements KeyListener 
   {
      public void keyTyped(KeyEvent e)
      {}
   
      public void keyPressed(KeyEvent e)
      {
         if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftIsPressed=true;
         screen.processUserInput(e.getKeyCode(), shiftIsPressed);
      }
   
      public void keyReleased(KeyEvent e)
      {
         if(e.getKeyCode()==KeyEvent.VK_SHIFT)
            shiftIsPressed=false;
      }
   }

}
