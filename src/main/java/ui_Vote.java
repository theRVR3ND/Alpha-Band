/**
 * Alpha Band - Multiplayer Rythym Game | ui_Player
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for player info viewing and changing.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class ui_Vote extends ui_Menu implements KeyListener, bg_Constants{
   
   /**
    * Constructor. Read previous settings from text file.
    */
   public ui_Vote(){
      buttons = new ui_Button[] {
         new ui_Button("BACK", 0.5f, 0.85f)
      };
      
      //Add key listener for entering player name
      this.setFocusable(true);
      this.addKeyListener(this);
   }
   
   /**
    * Paint method of panel. Displays all current information.
    *
    * @param g                   Graphics component to draw into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      repaint();
   }
   
   /**
    * Processes key press event.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
   
   /**
    * Process mouse click event. Save current info if exiting screen.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.setup);
      }else{
         return;
      }
      
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   /**
    * Process mouse press. Check if dragging panel objects.
    *
    * @param e                   MouseEvent to process.
    */
   public void mousePressed(MouseEvent e){
   }
   
   /**
    * Process mouse release. Releases any objects in panel being dragged.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseReleased(MouseEvent e){
   }
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   /**
    * Process mouse drag. Relay effect onto currently dragged objects.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseDragged(MouseEvent e){
   }
}