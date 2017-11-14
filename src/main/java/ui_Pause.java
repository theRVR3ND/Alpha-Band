/**
 * Alpha Band - Multiplayer Rythym Game | ui_Pause
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Pause menu.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Pause extends ui_Menu{
   
   /**
    * Constructor. Read previouse settings from text file.
    */
   public ui_Pause(){
      buttons = new ui_Button[] {
         new ui_Button("RESUME",     0.5f, 0.45f),
         new ui_Button("DISCONNECT", 0.5f, 0.60f)
      };
   }
   
   /**
    * Draws panel contents.
    *
    * @param g                   Graphics object to draw into.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      //Graphics2D g2 = util_Utilities.improveQuality(g);
      
      repaint();
   }
   
   /**
    * Process mouse click event. Write settings to file if
    * exiting menu.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other wonderful places
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(cg_Panel.gamePanel);
         cg_Panel.gamePanel.requestFocus();
         cg_Client.frame.revalidate();
      
      }else if(buttons[1].isDown()){
         //cg_Client.frame.setContentPane(ui_Menu.pause);
      
      }else{
         return;
      }
      
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   /**
    * Process mouse press event. Check if slider is pressed.
    *
    * @param e                   MouseEvent to process.
    */
   public void mousePressed(MouseEvent e){}
   
   /**
    * Process mouse release event. Un-drag all sliders.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseReleased(MouseEvent e){}
   
   public void mouseMoved(MouseEvent e){
      for(ui_Button b : buttons)
         b.checkHover((short)e.getX(), (short)e.getY());
   }
   
   /**
    * Process mouse drag event. Pass on coordinates to
    * sliders for slider movement.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseDragged(MouseEvent e){}
}