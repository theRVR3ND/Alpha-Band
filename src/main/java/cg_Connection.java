/**
 * Alpha Band - Multiplayer Rythym Game | cg_Connection
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Client's connection handler.
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class cg_Connection extends Thread implements bg_Constants{
   
   /**
    * Connection with server.
    */
   private Socket socket;
   
   /**
    * Receives byte array stream from client.
    */
   private InputStream in;
   
   /**
    * Writes out byte array stream to client.
    */
   private OutputStream out;
   
   /**
    * ID used by server to track this client. Used also in game world.
    */
   private byte clientID;
   
   /**
    * Constructor.
    */
   public cg_Connection(final String IP) throws IOException{
      //Establish connection
      socket = new Socket(IP, SERVER_PORT);
      in = socket.getInputStream();
      out = socket.getOutputStream();
      
      //Send info to server
      sendPlayerInfo();
      
      //Start updating stream
      start();
   }
   
   /**
    * Run update process. Receive bytes from server and process.
    */
   public void run(){
      try{
         in.read(new byte[Byte.MAX_VALUE]);
         
         while(true){
            //Receive input stream from server
            byte[] info = new byte[Byte.MAX_VALUE];
            byte numByte = (byte)in.read(info);
            if(numByte > 0){
               processInStream(info, numByte);
            }
         }
      }catch(IOException e){
         //e.printStackTrace();
      
      }finally{
         //Exit out to menu
         cg_Panel.disconnect();
         
         cg_Client.frame.setContentPane(ui_Menu.servers);
         cg_Client.frame.revalidate();
      }
   }
   
   /**
    * Return socket of connection with server.
    */
   public Socket getSocket(){
      return socket;
   }
   
   /**
    * Return input stream of connection with server.
    */
   public InputStream getInStream(){
      return in;
   }
   
   /**
    * Return output stream of connection with server.
    */
   public OutputStream getOutStream(){
      return out;
   }
   
   /**
    * Return client ID received from server.
    */
   public byte getClientID(){
      return clientID;
   }
   
   /**
    * Write out byte array through stream to server.
    * 
    * @param line                Byte array to write out.
    */
   public void writeOut(byte[] line){
      //Write out through stream
      while(true){
         try{
            out.write(line);
         }catch(IOException e){
            //e.printStackTrace();
            //System.exit(1);
            continue;
         }
         break;
      }
   }
   
   /**
    * Transmit message to display to other clients (chat).
    * 
    * @param message             Message to show to others.
    */
   public void writeOut(String message){
      byte[] bytes = message.getBytes();
      
      //Add stream tag to bytes
      byte[] outLine = new byte[bytes.length + 1];
      for(byte i = 0; i < bytes.length; i++)
         outLine[i + 1] = bytes[i];
      
      //Stream tag
      outLine[0] = MESSAGE;
      
      writeOut(outLine);
   }
   
   /**
    * Close any connection with server.
    */
   public void close(){
      try{
         socket.close();
         in.close();
         out.close();
      }catch(IOException e){}
   }
   
   /**
    * Send server player's information, including player name and color.
    */
   private void sendPlayerInfo(){
      byte[] info = new byte[cg_Panel.playerName.length() + 4];
      
      //Add tag
      info[0] = INITIALIZE;
      
      //Add player color
      info[1] = (byte)(cg_Panel.playerColor.getRed()   + Byte.MIN_VALUE);
      info[2] = (byte)(cg_Panel.playerColor.getGreen() + Byte.MIN_VALUE);
      info[3] = (byte)(cg_Panel.playerColor.getBlue()  + Byte.MIN_VALUE);
      
      //Add player name
      byte[] playerName = cg_Panel.playerName.getBytes();
      for(byte i = 0; i < playerName.length; i++)
         info[i + 4] = playerName[i];
      
      //Write out
      writeOut(info);
   }
   
   /**
    * Process incomming bytes from server.
    */
   private void processInStream(byte[] info, final byte numByte){
      //Receiving important info
      if(info[0] == INITIALIZE){
         clientID = info[1];
         
      //Incomming message
      }else if(info[0] == MESSAGE){
         cg_Panel.addMessage(new String(info, 1, numByte - 1));
      
      //Update world
      }else if(info[0] == UPDATE){
         //Clip out update tag
         byte[] send = new byte[numByte - 1];
         for(byte i = 0; i < send.length; i++)
            send[i] = info[i + 1];
         
         //Send data to world
         if(cg_Panel.gamePanel.getWorld() != null)
            cg_Panel.gamePanel.getWorld().setData(send);
      
      //Start vote
      }else if(info[0] == VOTE){
         ui_Menu.vote.startVote(info);
      
      //Process incomming notes for user to play
      }else if(info[0] == NOTES){
         //Clip out update tag
         byte[] send = new byte[numByte - 1];
         for(byte i = 0; i < send.length; i++)
            send[i] = info[i + 1];
         
         cg_Panel.gamePanel.getWorld().processNotes(send);
      
      //Play note press by other player(s)
      }else if(info[0] == ACTION){
         cg_MIDI.playNote(info[1], info[2]);
      
      //Uh oh!
      }else{
         System.out.print("Unidentified data in cg_Connection: ");
         for(byte i : info)
            System.out.print(i + " ");
         System.out.println();
      }
   }
}