/**
 * Alpha Band - Multiplayer Rythym Game | ui_Servers
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
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
   
   private boolean searching;
   
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
         new float[] {0.11f, 0.3f, 0.45f, 0.70f, 0.85f}
      );
      
      searching = false;
      
      //Start server searcher
      Thread refresher = new Thread(){
         public void run(){
            while(true){
               //Wait a bit
               try{
                  sleep(5000);
                  
                  //Only refresh if this panel is being shown
                  while(cg_Client.frame.getContentPane() != ui_Menu.servers){
                     sleep(1000);
                  }
               }catch(InterruptedException e){}
               
               //Update server list
               findServers();
            }
         }
      };
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
      
      //Draw searching icon
      byte ind = (byte)(((System.currentTimeMillis() % 10000) / 100) % 4);
      String searchingIcon = "oooo";
      searchingIcon = searchingIcon.substring(0, ind) + "0" + searchingIcon.substring(ind + 1);
      
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.setFont(new Font("Courier New", Font.BOLD, util_Utilities.getFontSize(4.0 / 5)));
      g2.drawString(searchingIcon, list.getX(), list.getY() + (short)(list.getHeight() * 1.06));
      
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
      byte gamemode = 0;
      for(String g : gamemodes){
         if(list.getContents().get(list.getHoverRow())[2].equals(g))
            break;
         else
            gamemode++;
      }
      joinServer(IP, gamemode);
   }
   
   public void joinServer(String IP, byte gamemode){
      try{
         cg_Panel.connect(IP);
         cg_GamePanel gamePanel = cg_Panel.gamePanel;
         
         gamePanel.startWorld(gamemode);
         
         cg_Client.frame.setContentPane(ui_Menu.vote);
         ui_Menu.vote.requestFocus();
         cg_Client.frame.revalidate();
      
      //Could not connect
      }catch(IOException e){}
   }
   
   private void findServers(){
      searching = true;
      
      //Find subnet (LAN) IP
      String networkIP = "";
      try{
         networkIP = InetAddress.getLocalHost().toString();
         networkIP = networkIP.substring(
            networkIP.indexOf("/") + 1,
            networkIP.lastIndexOf(".") + 1
         );
      }catch(UnknownHostException e){}
      
      final byte timeout = 50;
      for(byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++){
         Socket echo = new Socket();
         String pingIP = networkIP + (i - Byte.MIN_VALUE + 1);
         
         try{
            echo.connect(new InetSocketAddress(pingIP, ECHO_PORT), timeout);
            
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
            String gamemode = gamemodes[buff[numByte - 1]];
            
            //Format server info
            String[] serverInfo = new String[5];
            
            serverInfo[0] = new String(buff, 1, buff[0]);            //Server name
            serverInfo[1] = pingIP;                                  //Server IP
            serverInfo[2] = gamemode;                                //Server gamemode
            serverInfo[3] = buff[numByte - 1] + "/" + MAX_PLAYERS;   //Server capacity
            serverInfo[4] = receiveTime - sendTime + "";             //Server ping
            
            //Add/replace server info in list
            byte ind = -1;
            for(byte k = 0; k < list.getContents().size(); k++){
               if(list.getContents().get(k)[1].equals(pingIP)){
                  ind = k;
                  break;
               }
            }
            if(ind != -1)
               list.getContents().set(ind, serverInfo);
            else
               list.getContents().add(serverInfo);
            
            //Close connection
            echo.close();
            in.close();
            out.close();
         
         //Could not connect in time
         }catch(IOException e){
            //Remove server from list
            for(String[] s : list.getContents()){
               if(s[0].equals(pingIP)){
                  list.getContents().remove(s);
                  break;
               }
            }
         }
      }
      searching = false;
      /*
      ArrayList<String> IPs = new ArrayList<>();
      
      //Get IP of all machines on local network
      try{
         //Run cmd "arp -a" command
         Runtime runtime = Runtime.getRuntime();
         Process process = runtime.exec("arp.bat");
         
         //Read in ARP output
         BufferedReader input = new BufferedReader(
            new InputStreamReader(process.getInputStream())
         );
         
         //Skip some lines in output
         for(byte i = 0; i < 7; i++)
            input.readLine();
         
         //Read in the good stuff
         while(true){
            String pingIP = input.readLine();
            
            if(pingIP.equals("") || pingIP.contains("static"))
               break;
            
            //Extract local IP address
            pingIP = pingIP.substring(2, pingIP.indexOf(" ", 3));
            
            IPs.add(pingIP);
         }
         
         //Include ourselves
         IPs.add("127.0.0.1"); //Loopback address
      
      }catch(IOException e){
         e.printStackTrace();
      }
      
      //Try to connect to machines
      final short timeout = 500; //Time given for server to respond, in milliseconds
      for(String pingIP : IPs){
         try{
            Socket echo = new Socket();
            echo.connect(new InetSocketAddress(pingIP, ECHO_PORT), timeout);
            
            InputStream in = echo.getInputStream();
            OutputStream out = echo.getOutputStream();
            
            //CONNECTED! Send request
            out.write(REQUEST_MESSAGE.getBytes());
            final long sendTime = System.currentTimeMillis();
            
            //Get server info response
            byte[] buff = new byte[Byte.MAX_VALUE];
            byte numByte = (byte)in.read(buff);
            final long receiveTime = System.currentTimeMillis();
            
            //Format server info
            String[] serverInfo = new String[] {
               new String(buff, 1, buff[0]),          //Server name
               pingIP,                                //Server IP
               gamemodes[buff[numByte - 1]],          //Server gamemode
               buff[numByte - 1] + "/" + MAX_PLAYERS, //Server capacity
               receiveTime - sendTime + ""            //Server ping
            };
            
            //Add/replace server info in list
            byte ind = -1;
            for(byte i = 0; i < list.getContents().size(); i++){
               if(list.getContents().get(i)[1].equals(pingIP)){
                  ind = i;
                  break;
               }
            }
            System.out.println(ind + "");
            if(ind != -1)
               list.getContents().set(ind, serverInfo);
            else
               list.getContents().add(serverInfo);
            
            //Close connection
            echo.close();
            in.close();
            out.close();
         
         }catch(IOException e){
            //Remove server from list
            for(String[] s : list.getContents()){
               if(s[0].equals(pingIP)){
                  list.getContents().remove(s);
                  break;
               }
            }
         }
      }
      
      //Reset list scroll
      if(list.getContents().size() - list.getScrollInd() < list.getHoverRow())
         list.setHoverRow((byte)(list.getContents().size() - list.getScrollInd()));
      */
   }
}