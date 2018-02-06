/**
 * Alpha Band - Multiplayer Rythym Game | cg_Panel
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Game display for Client. Handles chat messaging system.
 */

import javax.swing.JPanel;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;

public abstract class cg_Panel extends JPanel implements KeyListener,
                                                         bg_Constants{
   
   /**
    * Connection handler.
    */
   protected static cg_Connection connection;
   
   /**
    * For easy key/button code to action conversion.
    */
   protected static HashMap<Short, Byte> bindTable;
   
   /**
    * Client's in-game name.
    */
   public static String playerName;
   
   /**
    * Client's in-game theme color.
    */
   public static Color playerColor;
   
   /**
    * All chat messages received.
    */
   protected static ArrayList<String> messages;
   
   /**
    * Index in messages that scroll is at.
    */
   protected static short messageScroll;
   
   /**
    * Counter for message fade.
    */
   protected static byte messageTimeout;
   
   /**
    * Chat message to send out.
    */
   protected static String chatMessage;
   
   /**
    * Time (in milliseconds) of last repaint call.
    */
   private static long lastUpdateTime;
   
   /**
    * Highest allowed repaint rate. Used to ensure consistent rendering.
    */
   private static final byte MAX_FPS = 45;
   
   /**
    * Panel for game world display.
    */
   public static cg_GamePanel gamePanel = new cg_GamePanel();
   
   /**
    * Constructor.
    */
   public cg_Panel(){
      //Fill bindTable
      ArrayList<cg_Bind> binds = ui_Menu.controls.getBinds();
      bindTable = new HashMap<Short, Byte>();
      for(byte i = 0; i < binds.size(); i++){
         bindTable.put(binds.get(i).getCode(), i);
      }
      
      //Initialize stuff
      messages = new ArrayList<String>();
      messageScroll = 0;
      messageTimeout = Byte.MAX_VALUE;
      chatMessage = null;
      lastUpdateTime = 0;
      
      //Add listeners
      this.setFocusable(true);
      this.addKeyListener(this);
   }
   
   /**
    * Render game world.
    * 
    * @param g                   Graphics object to draw into.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Control repaint rate
      int deltaTime = (int)((System.currentTimeMillis() - lastUpdateTime) % Integer.MAX_VALUE);
      lastUpdateTime += deltaTime;
      if(deltaTime < 1000.0 / MAX_FPS){
         try{
            Thread.sleep((int)(1000.0 / MAX_FPS - deltaTime));
         }catch(InterruptedException e){}
      }
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      g2.setClip(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      
      //Set graphics default
      g2.setFont(
         new Font("Courier New", Font.BOLD, util_Utilities.getFontSize(4.0 / 5))
      );
   }
   
   /**
    * Return connection state.
    */
   public static boolean isConnected(){
      return connection == null;
   }
   
   /**
    * Connect client to server.
    * 
    * @param IP                  IP address of server.
    */
   public static void connect(String IP) throws IOException{
      playerName = ((ui_Player)(ui_Menu.player)).getName();
      playerColor = ((ui_Player)(ui_Menu.player)).getColor();
      
      connection = new cg_Connection(IP);
   }
   
   /**
    * Disconnect this client from server.
    */
   public static void disconnect(){
      connection.close();
      connection = null;
   }
   
   public static cg_Connection getConnection(){
      return connection;
   }
   
   /**
    * Add message to running list.
    * 
    * @param message             Message to add to list.
    */
   public static void addMessage(String message){
      messages.add(message);
      messageTimeout = Byte.MAX_VALUE;
   }
   
   /**
    * Process key press.
    * 
    * @param e                   Event to process.
    */
   @Override
   public void keyPressed(KeyEvent e){
      //Escape to menu
      if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
         cg_Client.frame.setContentPane(ui_Menu.pause);
         ui_Menu.pause.requestFocus();
         cg_Client.frame.revalidate();
         return;
      }
      
      //Modifying chat message
      if(chatMessage != null){
         //Delete message
         if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            if(chatMessage.length() > 0)
               chatMessage = chatMessage.substring(0, chatMessage.length() - 1);
         
         //Send message
         }else if(e.getKeyCode() == KeyEvent.VK_ENTER){
            if(!chatMessage.equals(""))
               connection.writeOut(chatMessage);
            chatMessage = null;
            messageScroll = 0;
         
         //Add to message
         }else if(Character.isDefined(e.getKeyChar())){
            if(chatMessage.length() < Byte.MAX_VALUE){//Limit message length
               chatMessage += e.getKeyChar();
            }
         
         //Scroll through messages
         }else if(e.getKeyCode() == KeyEvent.VK_UP){
            if(messageScroll < messages.size() - 5)
               messageScroll++;
         
         }else if(e.getKeyCode() == KeyEvent.VK_DOWN){
            if(messageScroll > 0)
               messageScroll--;
         
         //Escape to pause menu
         }else if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            cg_Client.frame.setContentPane(ui_Menu.pause);
            cg_Client.frame.revalidate();
         }
      
      //Starting chat message
      }else{
         if(bindTable.containsKey((short)(e.getKeyCode())) &&
            bindTable.get((short)(e.getKeyCode())) == CHAT){
            chatMessage = "";
         }
      }
   }
   
   /**
    * Process key release.
    * 
    * @param e                   Event to process.
    */
   @Override
   public void keyReleased(KeyEvent e){
      if(bindTable.containsKey((short)(e.getKeyCode()))){
         byte[] writeOut = new byte[] {
            ACTION,
            bindTable.get((short)(e.getKeyCode()))
         };
      }
   }
   
   @Override
   public void keyTyped(KeyEvent e){}
   
   /**
    * Draw out most recent messages with fade out effect.
    * 
    * @param g2                  Graphics object to draw into.
    */
   protected void drawMessages(Graphics2D g2){
      //Check timeout
      if(chatMessage != null)
         messageTimeout = Byte.MAX_VALUE;
      
      if(messageTimeout <= 0)
         return;
      
      //Add fade-out effect on color
      byte alpha = (byte)(Math.min((messageTimeout - 16) * 8, Byte.MAX_VALUE));
      g2.setColor(new Color(
         g2.getColor().getRed(),
         g2.getColor().getGreen(),
         g2.getColor().getBlue(),
         alpha - Byte.MIN_VALUE
      ));
      
      //Show received messages
      FontMetrics fontMetrics = getFontMetrics(g2.getFont());
      for(byte i = 0; i < 5; i++){
         try{
            g2.drawString(
               messages.get(messages.size() - i - 1 - messageScroll),
               (short)(0.01 * getWidth()),
               (short)(getHeight() - (fontMetrics.getHeight() * (1.5 + i)))
            );
         }catch(IndexOutOfBoundsException e){
            break;
         }
      }
      
      //Show current chat message
      if(chatMessage != null){
         g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
         g2.drawString(
            "<CHAT>: " + chatMessage,
            (short)(0.01 * getWidth()),
            (short)(getHeight() - fontMetrics.getHeight() * 0.5)
         );
      }
      
      messageTimeout--;
   }
}