/**
 * Alpha Band - Multiplayer Rythym Game | g_Connection
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Update thread for a single server-client connection
 */
 
import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;

public class g_Connection extends Thread implements bg_Constants{
   
   /**
    * Connection with client.
    */
   private Socket socket;
   
   /**
    * Receives byte array stream from client.
    */
   private InputStream in;
   
   /**
    * Outputs byte array stream to client.
    */
   private OutputStream out;
   
   /**
    * Whether the current vote has been sent to client.
    */
   private boolean sentBallot;
   
   /**
    * ID for making this unique.
    */
   private final byte clientID;
   
   /**
    * Running count for clientID.
    */
   private static byte clientCount;
   
   /**
    * Constructor. Initialize streams.
    */
   public g_Connection(Socket socket){
      this.socket = socket;
      
      //Get input/output streams from socket
      try{
         in = socket.getInputStream();
         out = socket.getOutputStream();
      }catch(IOException e){
         e.printStackTrace();
      }
      
      //Initialize stuff
      sentBallot = false;
      
      //Keep track of this client's ID
      clientID = clientCount++;
      
      //Start this thread
      this.start();
   }
   
   /**
    * Update connection between server and client.
    */
   public void run(){
      try{
         //Give client some time to catch up
         try{
            Thread.sleep(10);
         }catch(InterruptedException e){}
         
         while(true){
            //long lastUpdateTime = System.currentTimeMillis();
            //Receive input stream from client
            if(in.available() > 0){
               byte[] info = new byte[Byte.MAX_VALUE];
               byte numByte = (byte)in.read(info);
               if(numByte > 0){
                  processInStream(info, numByte);
               }
            }
            
            //Send client vote info
            if(g_Server.server.getWorld().getCurrVote() != null && !sentBallot){
               byte[] toSend = new byte[Byte.MAX_VALUE];
               byte[][] currVote = g_Server.server.getWorld().getCurrVote();
               byte ind = 9;
               
               //Add tags and stuff
               toSend[0] = VOTE;
               
               //Include time to start game
               byte[] bytes = bg_World.longToBytes(g_Server.server.getWorld().getSongStartTime());
               for(byte i = 0; i < bytes.length; i++)
                  toSend[i + 1] = bytes[i];
               
               //Add all songs on ballot into toSend
               for(byte r = 0; r < currVote.length; r++){
                  if(currVote[r][0] == -1)
                     continue;
                  byte[] songInfo = g_Server.server.getWorld().getSongList().get(currVote[r][0]);
                  for(byte i = 0; i < songInfo.length; i++){
                     toSend[ind++] = songInfo[i];
                  }
               }
               
               //Send it!
               writeOut(toSend);
               sentBallot = true;
            
            //Send game world updates
            }else{// if(g_Server.server.getWorld().getPlayer(clientID) != null){
               //Send entity data
               LinkedList<byte[]> data = g_Server.server.getWorld().getRelevantData(clientID);
               for(byte i = 0; i < data.size(); i++){
                  //Add UPDATE stream tag
                  byte[] outLine = new byte[data.get(i).length + 1];
                  
                  outLine[0] = UPDATE;
                  
                  for(byte k = 0; k < data.get(i).length; k++){
                     outLine[k + 1] = data.get(i)[k];
                  }
                  
                  writeOut(outLine);
                  
                  try{
                     Thread.sleep(20);
                  }catch(InterruptedException e){}
               }
               
               //Send note data
               HashSet<byte[]> noteData = g_Server.server.getWorld().getNotes(clientID);
               if(noteData != null && !noteData.isEmpty()){
                  byte[] toSend = new byte[noteData.size() * 4 + 1];
                  
                  toSend[0] = NOTES;
                  
                  byte i = 1;
                  for(byte[] d : noteData){
                     for(byte b : d)
                        toSend[i++] = b;
                  }
                  
                  writeOut(toSend);
               }
               //**/
            }
         }
      }catch(Exception e){
         //e.printStackTrace();
      
      }finally{
         //Disconnected
         try{
            socket.close();
            
            //Send disconnect message to others
            for(g_Connection other : g_Server.server.getClients()){
               if(this == other){
                  other.relayMessage(
                     g_Server.server.getWorld().getPlayer(clientID).getName() + " has disconnected."
                  );
               }
            }
            
            g_Server.server.getClients().remove(this);
         }catch(Exception e){
            //Holy fook
         }
      }
   }
   
   /**
    * Return unique ID of this' client.
    */
   public byte getID(){
      return clientID;
   }
   
   /**
    * Send message to client.
    * 
    * @param message             Message to send.
    */
   public void relayMessage(String message){
      byte[] messageBytes = message.getBytes();
      byte[] outLine = new byte[message.length() + 1];
      
      //Transfer messageBytes to outLine with space at index 0 for stream tag
      for(short i = 1; i < outLine.length; i++){
         outLine[i] = messageBytes[i - 1];
      }
      
      //Stream tag
      outLine[0] = MESSAGE;
      
      writeOut(outLine);
   }
   
   /**
    * Do stuff based on byte stream from client.
    * 
    * @param info                Byte array to process.
    * @param numByte             Number of bytes stored in info.
    */
   private void processInStream(byte[] info, byte numByte){
      //Communicate player info
      if(info[0] == INITIALIZE){
         Color playerColor = new Color(
            info[1] - Byte.MIN_VALUE,
            info[2] - Byte.MIN_VALUE,
            info[3] - Byte.MIN_VALUE
         );
         String playerName = new String(info, 4, numByte - 4);
         
         //Create new player in world
         g_Server.server.getWorld().spawnPlayer(playerName, playerColor, clientID);
         
         //Tell other clients of connection
         for(byte i = 0; i < g_Server.server.getClients().size(); i++){
            if(this == g_Server.server.getClients().get(i))
               continue;
            g_Server.server.getClients().get(i).relayMessage(
               g_Server.server.getWorld().getPlayer(clientID).getName() + " has joined the game."
            );
         }
   
         //Send back client ID
         writeOut(new byte[] {INITIALIZE, clientID});
      
      //Record action press/release state
      }else if(info[0] == ACTION){
         //Send action to our player for processing
         for(byte i = 1; i < numByte; i++){
            g_Server.server.getWorld().getPlayer(clientID).processAction(info[i]);
         }
      
      //Relay message to all other clients
      }else if(info[0] == MESSAGE){
         String message =
            "[" + g_Server.server.getWorld().getPlayer(clientID).getName() + "]: " +
            (new String(info, 1, info.length - 1)).trim();
         
         //Send message to all clients
         for(byte i = 0; i < g_Server.server.getClients().size(); i++)
            g_Server.server.getClients().get(i).relayMessage(message);
         
      //Player has cast vote on song choice
      }else if(info[0] == VOTE){
         g_Server.server.getWorld().tallyVote(info[1]);
      }
   }
   
   /**
    * Write out byte array through output stream.
    * 
    * @param line                Byte array to write out.
    */
   private void writeOut(byte[] line){
      //Write out through stream
      try{
         out.write(line);
      }catch(IOException e){}
   }
}