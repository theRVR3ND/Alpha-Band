/**
 * Alpha Band - Multiplayer Rythym Game | g_Echo
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Echo server. Responds to client requests for connection.
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class g_Echo implements Runnable, bg_Constants{
   
   /**
    * Socket for connecting with clients.
    */
   private static ServerSocket socket;
   
   public static g_Echo echo;
   
   /**
    * 
    */
   public g_Echo(){
      if(echo == null){
         try{
            socket = new ServerSocket(ECHO_PORT);
            echo = this;
         }catch(IOException e){
            e.printStackTrace();
         }
      }
   }
   
   /**
    * Start up echo server.
    */
   @Override
   public void run(){
      //Figure out what info to return
      byte[] ret = new byte[g_Server.server.getServerName().length() + 3];
      
      ret[0] = (byte)(g_Server.server.getServerName().length());
      for(byte i = 0; i < ret[0]; i++){
         ret[i + 1] = (byte)(g_Server.server.getServerName().charAt(i));
      }
      
      ret[ret.length - 2] = g_Server.server.getWorld().getGamemode();
      
      //Start accepting requesters
      while(true){
         try{
            Socket req = socket.accept();
            
            //Establish streams
            InputStream in = req.getInputStream();
            OutputStream out = req.getOutputStream();
            
            //Get request
            byte[] buff = new byte[REQUEST_MESSAGE.length()];
            in.read(buff);
            
            //Correct request received. Send back server info.
            if((new String(buff)).equals(REQUEST_MESSAGE)){
               byte numPlayer = g_Server.server.getWorld().getNumPlayers();
               ret[ret.length - 1] = numPlayer;
               out.write(ret);
            }
            
            //Close connection
            req.close();
            in.close();
            out.close();
            
         }catch(IOException e){}
      }
   }
   
   public void shutdown(){
      try{
         socket.close();
         echo = null;
      }catch(IOException e){}
   }
}