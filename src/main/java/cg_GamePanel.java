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
import java.awt.image.*;
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
   
   private HashSet<Byte> currNotes;
   
   public static final HashMap<Integer, Byte> noteMap = new HashMap<Integer, Byte>();
   
   private final char[] KEYS = new char[] {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', ';'};
   
   /**
    * Constructor.
    */
   public cg_GamePanel(){
      //Initialize stuff
      world = null;
      currActions = new HashSet<Byte>();
      currNotes = new HashSet<Byte>();
      
      //Map note values to keys
      noteMap.put(KeyEvent.VK_A,         (byte)0);
      noteMap.put(KeyEvent.VK_S,         (byte)1);
      noteMap.put(KeyEvent.VK_D,         (byte)2);
      noteMap.put(KeyEvent.VK_F,         (byte)3);
      noteMap.put(KeyEvent.VK_G,         (byte)4);
      noteMap.put(KeyEvent.VK_H,         (byte)5);
      noteMap.put(KeyEvent.VK_J,         (byte)6);
      noteMap.put(KeyEvent.VK_K,         (byte)7);
      noteMap.put(KeyEvent.VK_L,         (byte)8);
      noteMap.put(KeyEvent.VK_SEMICOLON, (byte)9);
      
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
      //sendActions();
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw background
      /*
      g.drawImage(
         background,
         0, 0,
         cg_Client.SCREEN_WIDTH,
         cg_Client.SCREEN_HEIGHT,
         null
      );
      */
      
      //World rendering handled in cg_Renderer
      world.render(g2);
      
      //Draw current depressed keys
      g2.setFont(ui_Menu.defaultFont);
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      for(byte i = 0; i < noteMap.size(); i++){
         //Key is pressed
         if(currNotes.contains(i)){
            g2.drawRect(
               (int)((i + 0.05) * cg_Client.SCREEN_WIDTH / 10.0),
               (int)(cg_Client.SCREEN_HEIGHT * 14.5 / 20.0),
               (int)(cg_Client.SCREEN_WIDTH * 0.09),
               (int)(cg_Client.SCREEN_HEIGHT * 1.0 / 20)
            );
         //Key is up
         }else{
            g2.drawRect(
               (int)((i + 0.1) * cg_Client.SCREEN_WIDTH / 10.0),
               (int)(cg_Client.SCREEN_HEIGHT * 14.6 / 20.0),
               (int)(cg_Client.SCREEN_WIDTH * 0.08),
               (int)(cg_Client.SCREEN_HEIGHT * 0.8 / 20)
            );
         }
         
         //Label key
         short letterWidth = (short)g2.getFontMetrics().charWidth(KEYS[i]),
              letterHeight = (short)g2.getFontMetrics().getHeight();
         g2.drawString(
            KEYS[i] + "",
            (int)((i + 0.5) * cg_Client.SCREEN_WIDTH / 10.0 - letterWidth / 2),
            (int)(cg_Client.SCREEN_HEIGHT * 15 / 20.0 + letterHeight / 4)
         );
      }
      
      //Draw chat messages
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
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
      
      //Action key
      if(chatMessage == null){
         //Play new note
         if(noteMap.containsKey(e.getKeyCode())){
            if(currNotes.contains(e.getKeyCode())){
               
            }
            currNotes.add(noteMap.get(e.getKeyCode()));
            
            //Send to server and client worlds
            byte[] bytes = bg_World.longToBytes(e.getWhen());
            connection.writeOut(new byte[] {
               ACTION,
               (byte)(noteMap.get(e.getKeyCode()) + 34),
               bytes[0],
               bytes[1],
               bytes[2],
               bytes[3]
            });
            world.processAction((byte)(noteMap.get(e.getKeyCode()) + 34), e.getWhen());
         }
      }
   }
   
   @Override
   public void keyReleased(KeyEvent e){
      //if(bindTable.containsKey((short)(e.getKeyCode())))
         //currActions.remove(bindTable.get((short)(e.getKeyCode())));
      
      if(noteMap.containsKey(e.getKeyCode())){
         currNotes.remove(noteMap.get(e.getKeyCode()));
         
         //Send to server and client worlds
         byte[] bytes = bg_World.longToBytes(e.getWhen());
         connection.writeOut(new byte[] {
            ACTION,
            (byte)(-(noteMap.get(e.getKeyCode()) + 34)),
            bytes[0],
            bytes[1],
            bytes[2],
            bytes[3]
         });
         world.processAction((byte)(-(noteMap.get(e.getKeyCode()) + 34)), e.getWhen());
      }
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
   /*
   private void sendActions(){
      //Don't try to send if disconnected
      if(connection == null)
         return;
      
      //Don't send empty space
      if(currActions.size() == 0)
         return;
      
      //Send to server
      byte[] send = new byte[currActions.size() + 1];
      send[0] = ACTION;
      byte i = 1;
      for(Byte action : currActions){
         send[i++] = action;
      }
      connection.writeOut(send);
         
      //Send to our version of world
      byte[] ourSend = new byte[send.length - 1];
      for(byte k = 1; k < send.length; k++)
         ourSend[k - 1] = send[k];
      world.getPlayer(connection.getClientID()).processActions(ourSend);
   }
   */
}