/**
 * Alpha Band - Multiplayer Rythym Game | ui_Controls
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for changing game control binds.
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ui_Controls extends ui_Menu implements KeyListener, MouseWheelListener{
   
   /**
    * List of all current binds.
    */
   private ArrayList<cg_Bind> binds;
   
   /**
    * Table for displaying all control binds.
    */
   private ui_Table table;
   
   /**
    * Index in binds list currently being modified.
    * -1 if none changing right now.
    */
   private int modInd;
   
   /**
    * Constructor. Load in saved binds from text file.
    */
   public ui_Controls(){
      buttons = new ui_Button[] {
         new ui_Button("BACK", 0.5f, 0.85f)
      };
      
      //Initialize variables
      binds = new ArrayList<cg_Bind>();
      table = new ui_Table(
         0.1f, 0.1f, 0.8f, 0.6f,
         new String[] {"Action", "Key"},
         new float[] {0.11f, 0.77f}
      );
      modInd = -1;
      
      //Read in existing key settings and generate table format
      String[] lines = util_Utilities.readFromFile("menu/binds.cfg");
      ArrayList<String[]> tableContents = new ArrayList<String[]>();
      for(String s : lines){
         //Actual bind list
         binds.add(new cg_Bind(s));
         
         //Table formatting
         tableContents.add(new String[] {
            binds.get(binds.size() - 1).getAction(),
            binds.get(binds.size() - 1).getCodeInText()
         });
      }
      table.setContents(tableContents);
      
      //Add key listener
      this.setFocusable(true);
      addKeyListener(this);
      addMouseWheelListener(this);
   }
   
   /**
    * Paint method for panel. Display current control binds.
    * 
    * @param g                      Graphics object to draw on to.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw out current binds
      table.draw(g2);
      
      repaint();
   }
   
   /**
    * Return list of control binds.
    */
   public ArrayList<cg_Bind> getBinds(){
      return binds;
   }
   
   /**
    * Process key press event. Set control bind if applicable.
    * 
    * @param e                      KeyEvent to process.
    */
   public void keyPressed(KeyEvent e){
      //Check if currently changing a control bind
      if(modInd != -1){
         //New key pressed, change bind
         if(!alreadyBound(e.getKeyCode())){//Do not allow conflicting binds
            binds.get(modInd).setCode((short)e.getKeyCode());
         }
         
         //Reflect changes in table
         table.getContents().set(modInd, new String[] {
            binds.get(modInd).getAction(),
            binds.get(modInd).getCodeInText()
         });
         
         modInd = -1;
      }
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
   
   /**
    * Process mouse click. Save binds if exiting menu.
    * 
    * @param e                      MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.setup);
         //Write binds to file
         String[] lines = new String[binds.size()];
         for(int i = 0; i < lines.length; i++)
            lines[i] = binds.get(i).toString();
         util_Utilities.writeToFile(lines, "menu/binds.cfg");
         modInd = -1;
      
      }else{
         //Check if changing a control bind
         if(modInd != -1){
            //Do not allow conflicting binds
            if(! alreadyBound(e.getButton())){
               binds.get(modInd).setCode((short)e.getButton());
            }
            
            //Update table
            table.getContents().get(modInd)[1] = binds.get(modInd).getCodeInText();
            modInd = -1;
         
         //Check if table row clicked
         }else if(table.getHoverRow() != -1){
            //Update table
            table.getContents().get(table.getHoverRow())[1] = "???";
            table.checkHover((short)e.getX(), (short)e.getY());
            modInd = table.getHoverRow();
         }
         return;
      }
      
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   public void mousePressed(MouseEvent e){}
   
   public void mouseReleased(MouseEvent e){}
   
   /**
    * Process mouse movement. Check if mouse is hovering on table.
    * 
    * @param e                      MouseEvent to process.
    */
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
      
      //Check if mouse is hovering on table.
      if(modInd == -1)
         table.checkHover((short)e.getX(), (short)e.getY());
   }
   
   public void mouseDragged(MouseEvent e){}
   
   /**
    * Process mouse scroll. Scroll along table if possible.
    * 
    * @param e                      MouseWheelEvent to process.
    */
   public void mouseWheelMoved(MouseWheelEvent e){
      //Tell table to scroll
      if(modInd == -1)
         table.checkScroll(
            (short)e.getX(),
            (short)e.getY(),
            (byte)e.getWheelRotation()
         );
   }
   
   /**
    * Check if mouse/key code is already bound.
    * 
    * @param code                   Control code to search for.
    * @return                       True if code is already used, false if not.
    */
   private boolean alreadyBound(int code){
      for(cg_Bind b : binds)
         if(b.getCode() == code)
            return true;
      return false;
   }
}