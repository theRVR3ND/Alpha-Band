/**
 * Alpha Band - Multiplayer Rythym Game | ui_Servers
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for server listing and selection.
 */
 
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ui_Servers extends ui_Menu implements MouseWheelListener, bg_Constants{
   
   /**
    * Scrolling table/list to display servers.
    */
   private ui_Table list;
   
   /**
    * Thread to concurrently search for servers while panel is running.
    */
   private Refresher refresher;
   
   /**
    * Small icon thing to display refresher status.
    */
   private String searchingIcon;
   
   /**
    * Constructor. Initializes buttons and lists.
    */
   public ui_Servers(){
      buttons = new ui_Button[] {
         new ui_Button("JOIN",   0.5f, 0.55f),
         new ui_Button("CREATE", 0.5f, 0.7f),
         new ui_Button("BACK",   0.5f, 0.85f)
      };
      
      //Initialize stuff
      list = new ui_Table(
         0.1f, 0.1f, 0.8f, 0.35f,
         new String[] {"Server", "IP", "Gamemode", "Players", "Ping"},
         new float[] {0.11f, 0.25f, 0.45f, 0.75f, 0.85f}
      );
      
      refresher = new Refresher();
      refresher.start();
   }
   
   /**
    * Paint method for panel. Draws components and updates refresher.
    *
    * @param                        Graphics component to draw into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw server list
      list.draw(g2);
      
      //Update/draw searchingIcon
      if(searchingIcon != null){
         g2.setFont(new Font("Courier New", Font.BOLD, util_Utilities.getFontSize(4.0 / 5)));
         g2.drawString(searchingIcon, list.getX(), list.getY() + (short)(list.getHeight() * 1.06));
         
         //Animate searching icon
         if(Math.random() < 0.1)
            searchingIcon = searchingIcon.substring(2) + searchingIcon.substring(0, 2);
      }
      
      repaint();
   }
   
   /**
    * Process mouse click event.
    * 
    * @param e                      MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Join selected server
      if(buttons[0].isDown()){
         if(list.getHoverRow() >= 0){
            //Find server's IP
            String IP = list.getContents().get(list.getHoverRow())[1];
            joinServer(IP);
         }
      
      //Redirect to other menus
      }else if(buttons[1].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.createServer);
         createServer.requestFocus();
      
      }else if(buttons[2].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.main);
      
      }else{
         //Check if table row clicked (server selected)
         if(e.getX() > list.getX() && e.getX() < list.getX() + list.getWidth() &&
            e.getY() > list.getY() && e.getY() < list.getY() + list.getHeight()){
            list.checkHover((short)e.getX(), (short)e.getY());
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
    * Process mouse wheel scroll.
    * 
    * @param e                      Event to process.
    */
   public void mouseWheelMoved(MouseWheelEvent e){
      //Scroll through table
      list.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
   
   /**
    * Start up game panel.
    * 
    * @param IP                     IP of server to connect to.
    */
   public void joinServer(String IP){
      joinServer(
         IP,
         Byte.parseByte(list.getContents().get(list.getHoverRow())[2])
      );
   }
   
   public void joinServer(String IP, byte gamemode){
      try{
         cg_Panel.connect(IP);
         cg_GamePanel gamePanel = cg_Panel.gamePanel;
         
         gamePanel.startWorld(gamemode);
         
         cg_Client.frame.setContentPane(gamePanel);
         gamePanel.requestFocus();
         cg_Client.frame.revalidate();
      
      //Could not connect
      }catch(IOException e){}
   }
   
   /**
    * Thread-based server list refresh class.
    */
   private class Refresher extends Thread{
      
      /**
       * Search for servers and update servers list. The code is a trainwreck.
       * Approach with caution.
       */
      public void run(){
         //Maximum time to wait for connection (in milliseconds)
         byte timeout = 100;
         
         //Find subnet (LAN) IP
         String networkIP = "";
         try{
            networkIP = InetAddress.getLocalHost().toString();
            networkIP = networkIP.substring(
               networkIP.indexOf("/") + 1,
               networkIP.lastIndexOf(".") + 1
            );
         }catch(UnknownHostException e){}
         
         //To replace table's contents once done updating
         HashMap<Byte, String[]> cont = new HashMap<Byte, String[]>();
         
         //Constantly echo out
         while(true){
            
            searchingIcon = "0oo";
            
            for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++){
               //Check if this panel is still being displayed
               if(cg_Client.frame.getContentPane() != ui_Menu.servers)
                  continue;
               
               String pingIP = networkIP + (i - Byte.MIN_VALUE + 1);
               
               //Try to ping echo server
               Socket echo = new Socket();
               try{
                  echo.connect(new InetSocketAddress(pingIP, ECHO * 2), timeout);
                  
                  InputStream in = echo.getInputStream();
                  OutputStream out = echo.getOutputStream();
                  
                  //CONNECTED! Send request
                  out.write(REQUEST_MESSAGE.getBytes());
                  final long sendTime = System.currentTimeMillis();
                  
                  //Get server info response
                  byte[] buff = new byte[Byte.MAX_VALUE];
                  byte numByte = (byte)in.read(buff);
                  final long receiveTime = System.currentTimeMillis();
                  
                  //Do stuff
                  String gamemode;
                  if(buff[numByte - 1] == COMPETITION)
                     gamemode = "Competition";
                  else
                     gamemode = "Collab";
                  
                  //Format server info
                  String[] serverInfo = new String[4];
                  
                  serverInfo[0] = new String(buff, 1, buff[0]);            //Server name
                  serverInfo[1] = pingIP;                                  //Server IP
                  serverInfo[2] = gamemode;                                //Server gamemode
                  serverInfo[3] = buff[numByte - 1] + "/" + MAX_PLAYERS;   //Server capacity
                  serverInfo[4] = receiveTime - sendTime + "";             //Server ping
                  
                  //Add server info to list
                  cont.put((byte)(i - Byte.MIN_VALUE), serverInfo);
                  
                  //Close connection
                  echo.close();
                  in.close();
                  out.close();
               
               //Could not connect in time
               }catch(IOException e){
                  //Remove server from list
                  if(cont.containsKey((byte)(i - Byte.MIN_VALUE)))
                     cont.remove((byte)(i - Byte.MIN_VALUE));
               }
               
               //Update server list
               list.setContents(new ArrayList<String[]> (cont.values()));
               
               if(list.getContents().size() < list.getHoverRow())
                  list.setHoverRow((byte)(list.getContents().size() - list.getScrollInd()));
               
               //Searched all network
               if(i == Byte.MAX_VALUE - 1){
                  searchingIcon = null;
                  
                  //If no servers found, lower our timeout standards
                  if(list.getContents().size() == 0 && timeout < 100){
                     timeout += 10;
                  }
                  
                  //Take a break
                  try{
                     Thread.sleep(2000);
                  }catch(InterruptedException e){}
               }
            }
         }
      }
   }
}