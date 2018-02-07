/**
 * Alpha Band - Multiplayer Rythym Game | ui_Studio
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Song studio! For transcribing/creating songs in game format.
 */

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class ui_Studio extends ui_Menu{
   
   /**
    * Constructor.
    */
   public ui_Studio(){
      buttons = new ui_Button[] {
         new ui_Button("SAVE", 0.5f, 0.7f),
         new ui_Button("BACK", 0.5f, 0.85f)
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
      
      //Save current song
      if(buttons[0].isDown()){
         
      
      //Redirect to main page
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.main);
      
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