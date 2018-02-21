/**
 * Alpha Band - Multiplayer Rythym Game | ui_CreateServer
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for launching server.
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

public class ui_CreateServer extends ui_Menu implements KeyListener,
                                                        MouseWheelListener,
                                                        bg_Constants{
   
   /**
    * Used to enter user-given name of server, to differentiate servers.
    */
   private ui_Textbox nameTextbox;
   
   /**
    * List of all available map types or game modes.
    */
   private ui_Table gamemodeList;
   
   /**
    * Constructor. Load map list from text file.
    */
   public ui_CreateServer(){
      buttons = new ui_Button[] {
         new ui_Button("LAUNCH", 0.5f, 0.7f),
         new ui_Button("BACK",   0.5f, 0.85f)
      };
      
      //Initialize stuff
      nameTextbox = new ui_Textbox(
         0.35f, 0.08f,
         0.3f, 0.05f,
         (byte)18
      );
      nameTextbox.setContents("Server");
      
      //List available game modes
      gamemodeList = new ui_Table(
         0.4f, 0.3f, 0.2f, 0.2f,
         new String[] {"Game Mode"},
         new float[] {0.41f}
      );
      ArrayList<String[]> modeList = new ArrayList<String[]>();
      for(String gm : gamemodes){
         modeList.add(new String[] {gm});
      }
      gamemodeList.setContents(modeList);
      gamemodeList.setHoverRow((byte)0);
      
      //Add listeners
      this.setFocusable(true);
      this.addKeyListener(this);
      this.addMouseWheelListener(this);
   }
   
   /**
    * Draws panel contents.
    *
    * @param g                   Graphics object to draw into.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw server name textbox
      g.drawString(
         "Server Name:",
         nameTextbox.getX(),
         nameTextbox.getY() - (byte)(0.015 * cg_Client.SCREEN_HEIGHT)
      );
      nameTextbox.draw(g2);
      
      //Draw table
      gamemodeList.draw(g2);
      
      repaint();
   }
   
   /**
    * Processes key press event. Edits server name based on key.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){
      nameTextbox.keyPressed(e);
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
   
   /**
    * Process mouse click event.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         //Launch server
         ServerSocket socket;
         try{
            socket = new ServerSocket(SERVER_PORT);
         }catch(IOException exception){
            return;
         }
         if(socket.isBound()){
            g_Server server = new g_Server(socket, nameTextbox.getContents(), gamemodeList.getHoverRow());
            server.start();
         }
         
         //Join said server. Use loopback IP address.
         ui_Menu.servers.joinServer(
            "127.0.0.1",
            gamemodeList.getHoverRow()
         );
      
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.servers);
      
      }else{
         //Check if textbox clicked
         if(nameTextbox.checkClick((short)e.getX(), (short)e.getY())){
            return;
         }
         
         //Check if table row clicked
         byte oldHoverRow = gamemodeList.getHoverRow();
         gamemodeList.checkHover((short)e.getX(), (short)e.getY());
         if(gamemodeList.getHoverRow() < 0){
            gamemodeList.setHoverRow(oldHoverRow);
            return;
         }
         
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
    * Process mouse scroll. Scroll along map list if possible.
    * 
    * @param e                      MouseWheelEvent to process.
    */
   public void mouseWheelMoved(MouseWheelEvent e){
      //Tell table to scroll
      gamemodeList.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
}