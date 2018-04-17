/**
 * Alpha Band - Multiplayer Rythym Game | ui_Studio
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Song studio for transcribing/creating songs into game format.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Studio extends ui_Menu implements KeyListener{
   
   private ui_Textbox nameTextbox; //Name of song
   
   private ui_Slider bpmSlider;
   
   private byte key, scale, instrument;
   
   private static final String[] keys = new String[] {
      "C",
      "C#",
      "D",
      "D#",
      "E",
      "F",
      "F#",
      "G",
      "G#",
      "A",
      "A#",
      "B"
   };
   
   private static final String[] scales = new String[] {
      "Major",
      "Blues",
      "Minor",
      "Harmonic"
   };
   
   /**
    * Constructor.
    */
   public ui_Studio(){
      buttons = new ui_Button[] {
         new ui_Button("SAVE", 0.5f, 0.7f),
         new ui_Button("BACK", 0.5f, 0.85f)
      };
      
      nameTextbox = new ui_Textbox(
         0.02f, 0.03f,
         0.3f, 0.05f,
         (byte)30
      );
      nameTextbox.setContents("Song Name - Artist");
      
      bpmSlider = new ui_Slider(
         "BPM:",
         0.06f, 0.15f,
         0.2f, 0.02f,
         (short)60, (short)210
      );
      
      //Initialize stuff
      key = 0;
      scale = 0;
      instrument = 0;
      
      //Add key listener for entering player name
      this.setFocusable(true);
      this.addKeyListener(this);
   }
   
   /**
    * Paint method for panel.
    *
    * @param g                   Graphics instance to paint into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      nameTextbox.draw(g2);
      bpmSlider.draw(g2);
      
      g2.setFont(new Font(
         "Courier New",
         Font.PLAIN,
         util_Utilities.getFontSize()
      ));
      
      g2.drawString(
         "Key (- or +): " + keys[key],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.25f * cg_Client.SCREEN_HEIGHT)
      );
      
      g2.drawString(
         "Scale (< or >): " + scales[scale],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.3f * cg_Client.SCREEN_HEIGHT)
      );
      
      g2.drawString(
         "Instrument ([ or ]): " + util_Music.instruments[instrument],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.35f * cg_Client.SCREEN_HEIGHT)
      );
      
      g2.drawRect(
         (short)(0.34 * cg_Client.SCREEN_WIDTH),
         (short)(0.03 * cg_Client.SCREEN_HEIGHT),
         (short)(0.62 * cg_Client.SCREEN_WIDTH),
         (short)(0.60 * cg_Client.SCREEN_HEIGHT)
      );
      
      repaint();
   }
   
   /**
    * Process mouse click event.
    *
    * @param e                   Mouse click event to process
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Save current song
      if(buttons[0].isDown()){
         
      
      //Redirect to main page
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.main);
      
      }else{
         nameTextbox.checkClick((short)e.getX(), (short)e.getY());
      }
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   public void mousePressed(MouseEvent e){
      bpmSlider.checkPress((short)e.getX(), (short)e.getY());
   }
   
   public void mouseReleased(MouseEvent e){
      bpmSlider.release();
   }
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   public void mouseDragged(MouseEvent e){
      bpmSlider.checkDrag((short)e.getX());
   }
   
   public void keyPressed(KeyEvent e){
      if(nameTextbox.isSelected()){
         nameTextbox.keyPressed(e);
      }else{
         //Lower key
         if(e.getKeyCode() == KeyEvent.VK_MINUS){
            key--;
            if(key < 0)
               key = (byte)(keys.length - 1);
         
         //Raise key
         }else if(e.getKeyCode() == KeyEvent.VK_EQUALS){
            key = (byte)((key + 1) % keys.length);
         
         //Cycle left through scales
         }else if(e.getKeyCode() == KeyEvent.VK_COMMA){
            scale--;
            if(scale < 0)
               scale = (byte)(scales.length - 1);
         
         //Cycle right through scales
         }else if(e.getKeyCode() == KeyEvent.VK_PERIOD){
            scale = (byte)((scale + 1) % scales.length);
         
         //Cycle left through instruments
         }else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
            instrument--;
            if(instrument < 0)
               instrument = (byte)(util_Music.instruments.length - 1);
         
         //Cycle right through instruments
         }else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
            instrument = (byte)((instrument + 1) % util_Music.instruments.length);
         }
      }
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
}