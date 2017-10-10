/**
 * Alpha Band - Multiplayer Rythym Game | cg_GamePanel
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Game world display.
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;

//                                                    Dost thou even implement, bro?
public class cg_GamePanel extends cg_Panel implements MouseListener,
                                                      MouseMotionListener,
                                                      MouseWheelListener,
                                                      bg_Constants{
   
   /**
    * Unofficial game world. Changes to reflect server world state.
    */
   private cg_World world;
   
   /**
    * Set of all currently-triggered actions.
    */
   private HashSet<Byte> currActions;
   
   /**
    * Constructor.
    */
   public cg_GamePanel(){
      //Initialize stuff
      world = null;
      currActions = new HashSet<Byte>();
      
      //Add listeners
      this.addMouseListener(this);
      this.addMouseWheelListener(this);
   }
   
   /**
    * Render game world and draw chat messages.
    * 
    * @param g             Graphics object to draw into.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Check if we are still connected to server
      if(connection == null)
         return;
      
      //Process current actions
      sendActions();
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //World rendering handled in cg_Renderer
      world.render(g2);
      
      //Draw chat messages
      g2.setColor(ui_Colors.getColor(ui_Colors.TEXT));
      super.drawMessages(g2);
      
      repaint();
   }
   
   /**
    * Return client's game world.
    */
   public cg_World getWorld(){
      return world;
   }
   
   public void startWorld(byte gamemode){
      world = new cg_World(gamemode);
   }
   
   /**
    * Process key press.
    * 
    * @param e             Event to process.
    */
   @Override
   public void keyPressed(KeyEvent e){
      super.keyPressed(e);
      
      //Action key, send to process
      if(chatMessage == null){
         if(bindTable.containsKey((short)(e.getKeyCode()))){
            currActions.add(bindTable.get((short)(e.getKeyCode())));
         }
      }
   }
   
   @Override
   public void keyReleased(KeyEvent e){
      currActions.remove(bindTable.get((short)(e.getKeyCode())));
   }
   
   @Override
   public void keyTyped(KeyEvent e){}
   
   /**
    * Process mouse press.
    * 
    * @param e             Event to process.
    */
   @Override
   public void mousePressed(MouseEvent e){
      if(bindTable.containsKey((short)(e.getButton()))){
         currActions.add(bindTable.get((short)(e.getButton())));
      }
   }
   
   @Override
   public void mouseReleased(MouseEvent e){
      currActions.remove(bindTable.get((short)(e.getButton())));
   }
   
   @Override
   public void mouseClicked(MouseEvent e){}
   
   @Override
   public void mouseDragged(MouseEvent e){}
   
   @Override
   public void mouseEntered(MouseEvent e){}
   
   @Override
   public void mouseExited(MouseEvent e){}
   
   @Override
   public void mouseMoved(MouseEvent e){}
   
   @Override
   public void mouseWheelMoved(MouseWheelEvent e){}
   
   /**
    * Send all current actions to server and client's world.
    */
   private void sendActions(){
      //Don't try to send if disconnected
      //if(connection == null)
      //   return;
      
      //Don't send empty space
      if(currActions.size() == 0)
         return;
      
      //Send to server
      byte[] send = new byte[currActions.size() + 1];
      send[0] = ACTION;
      byte i = 1;
      for(Byte action : currActions){
         send[i++] = action;
         
         //Send to our version of world
         world.getPlayer(connection.getClientID()).processAction(action);
      }
      connection.writeOut(send);
   }
}