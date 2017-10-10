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

public class ui_Player extends ui_Menu implements KeyListener, bg_Constants{
   
   /**
    * Used to enter name to be used by client in-game.
    */
   private ui_Textbox nameTextbox;
   
   /**
    * Color-wheel based chooser of client's theme color in-game.
    */
   private ui_ColorWheel picker;
   
   /**
    * Constructor. Read previous settings from text file.
    */
   public ui_Player(){
      buttons = new ui_Button[] {
         new ui_Button("BACK", 0.5f, 0.85f)
      };
      
      //Initialize things
      picker = new ui_ColorWheel(0.39f, 0.3f, 0.22f);
      nameTextbox = new ui_Textbox(
         0.35f, 0.08f, 0.3f, 0.03f, (byte)21
      );
      
      //Read previous settings from text file
      String[] player = util_Utilities.readFromFile("menu/Player.cfg");
      if(player.length == 0){
         nameTextbox.setContents("enod");
      }else{
         nameTextbox.setContents(player[0]);
         picker.setRotation(Short.parseShort(player[1]));
      }
         
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
      
      //Draw name textbox
      g.drawString(
         "Name:",
         nameTextbox.getX(),
         nameTextbox.getY() - (byte)(0.015 * cg_Client.SCREEN_HEIGHT)
      );
      nameTextbox.draw(g2);
      
      //Draw color wheel
      g2.setColor(ui_Colors.getColor(ui_Colors.TEXT));
      g.drawString(
         "Color:",
         nameTextbox.getX(),
         picker.getY() - (byte)(0.04 * cg_Client.SCREEN_HEIGHT)
      );
      picker.draw(g2);
      
      //Draw currently selected color
      g2.setColor(picker.getColor());
      g2.fillPolygon(new int[] {(int)(0.49 * getWidth()),       //Triangle pointing down
                                (int)(0.51 * getWidth()),
                                (int)(0.50 * getWidth())},
                     new int[] {(int)(0.25 * getHeight()),
                                (int)(0.25 * getHeight()),
                                (int)(0.28 * getHeight())}, 3);
      
      repaint();
   }
   
   /**
    * Return player's name.
    */
   public String getName(){
      return nameTextbox.getContents();
   }
   
   /**
    * Return player's theme color.
    */
   public Color getColor(){
      return picker.getColor();
   }
   
   /**
    * Processes key press event.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){
      nameTextbox.keyPressed(e);
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
         
         //Write current info to file
         String[] info = new String[2];
         info[0] = nameTextbox.getContents();
         info[1] = picker.getRotation() + "";
         util_Utilities.writeToFile(info, "menu/Player.cfg");
         
      }else{
         nameTextbox.checkClick((short)e.getX(), (short)e.getY());
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
      picker.checkPress((short)e.getX(), (short)e.getY());
   }
   
   /**
    * Process mouse release. Releases any objects in panel being dragged.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseReleased(MouseEvent e){
      picker.release();
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
      picker.checkDrag((short)e.getX(), (short)e.getY());
   }
}