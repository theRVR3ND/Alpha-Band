/**
 * Alpha Band - Multiplayer Rythym Game | ui_Studio
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Song studio! For transcribing/creating songs in game format.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Studio extends ui_Menu implements KeyListener{
   
   private ui_Textbox nameTextbox; //Name of song
   
   private ui_Slider bpmSlider;
   
   /**
    * Constructor.
    */
   public ui_Studio(){
      buttons = new ui_Button[] {
         new ui_Button("SAVE", 0.5f, 0.7f),
         new ui_Button("BACK", 0.5f, 0.85f),
         new ui_Button("PREV", 0.08f, 0.5f),
         new ui_Button("NEXT", 0.21f, 0.5f),
         new ui_Button("<", 0.77f, 0.7f),
         new ui_Button(">", 0.90f, 0.7f)
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
      nameTextbox.keyPressed(e);
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
}