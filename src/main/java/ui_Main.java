/**
 * Alpha Band - Multiplayer Rythym Game | ui_Main
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Main menu panel. Redirects to other menus.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Main extends ui_Menu{
   
   /**
    * Constructor. Initialize all buttons in menu.
    */
   public ui_Main(){
      buttons = new ui_Button[] {
         new ui_Button("SERVERS", 0.5f, 0.4f),
         new ui_Button("SETUP",   0.5f, 0.55f),
         new ui_Button("GUIDE",   0.5f, 0.7f),
         new ui_Button("EXIT",   0.5f, 0.85f)
      };
   }
   
   /**
    * Paint method for panel.
    *
    * @param g                   Graphics instance to paint into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      repaint();
   }
   
   /**
    * Process mouse click event.
    *
    * @param e                   Mouse click event to process
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.servers);
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.setup);
      }else if(buttons[2].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.guide);
      
      //Exit program
      }else if(buttons[3].isDown()){
         System.exit(0);
      
      //Do nothing
      }else{
         return;
      }
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   public void mousePressed(MouseEvent e){}
   
   public void mouseReleased(MouseEvent e){}
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   public void mouseDragged(MouseEvent e){}
}