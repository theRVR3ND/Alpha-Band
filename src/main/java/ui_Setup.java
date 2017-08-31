/**
 * Kilo One - 2D Java Multiplayer Engine | ui_Setup
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Menu panel for redirecting to other menus. That make sense?
 */

import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.MouseEvent;

public class ui_Setup extends ui_Menu{
   
   /**
    * Constructor.
    */
   public ui_Setup(){
      buttons = new ui_Button[] {
         new ui_Button(util_Utilities.loadImage("menu/ButtonPLAYER.png"),   0.5f, 0.4f),
         new ui_Button(util_Utilities.loadImage("menu/ButtonCONTROLS.png"), 0.5f, 0.55f),
         new ui_Button(util_Utilities.loadImage("menu/ButtonSETTINGS.png"), 0.5f, 0.7f),
         new ui_Button(util_Utilities.loadImage("menu/ButtonBACK.png"),     0.5f, 0.85f)
      };
   }
   
   /**
    * Paint method for panel.
    * 
    * @param g                   Graphics object to draw in to.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      repaint();
   }
   
   /**
    * Process mouse click. Redirect to other menus if appropriate
    * button clicked.
    * 
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.player);
         player.requestFocus();
      
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.controls);
         controls.requestFocus();
      
      }else if(buttons[2].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.settings);
      
      }else if(buttons[3].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.main);
      
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