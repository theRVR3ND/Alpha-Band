/**
 * Kilo - Java Multiplayer Engine | ui_Guide
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for displaying game instructions.
 */

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class ui_Guide extends ui_Menu implements MouseWheelListener{
   
   /**
    * Used as textbox to display guide.
    */
   private ui_Table textbox;
   
   private final Font guideFont = new Font("Courier New", Font.BOLD, util_Utilities.getFontSize(4.0 / 5));
   
   /**
    * Constructor. Initialze variables and load text file contents.
    */
   public ui_Guide(){
      buttons = new ui_Button[] {
         new ui_Button(util_Utilities.loadImage("menu/ButtonBACK.png"), 0.5f, 0.85f)
      };
      
      //Initialize textbox
      textbox = new ui_Table(
         0.1f, 0.1f, 0.8f, 0.6f,
         new String[] {"Guide"},
         new float[] {0.11f}
      );
      
      //Put guide file into textbox
      String[] file = util_Utilities.readFromFile("menu/Guide.cfg");
      textbox.setContents(file, this.getFontMetrics(guideFont));
      
      //Add mouse scroll listener
      addMouseWheelListener(this);
   }
   
   /**
    * Paint method of panel. Draw guide in textbox.
    * 
    * @param g                      Graphics object to draw in to.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw guide text
      g2.setFont(guideFont);
      textbox.draw(g2);
      
      repaint();
   }
   
   /**
    * Process mouse click. Check if buttons are clicked.
    * 
    * @param e                      MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      if(buttons[0].isDown()){
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
   
   /**
    * Process mouse scroll. Relay rotation to table for scrolling.
    * 
    * @param e                      MouseWheelEvent to process.
    */
   public void mouseWheelMoved(MouseWheelEvent e){
      textbox.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
}