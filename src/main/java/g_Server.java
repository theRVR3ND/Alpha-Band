/**
 * Kilo - 2D Java Multiplayer Engine | g_Server
 * W.T.Woodson H.S.
 * 2017
 *
 * Server run program.
 *
 * @author Kelvin Peng
 */

import java.util.*;
import java.net.*;
import java.io.*;

public class g_Server implements Runnable, bg_Constants{
   
   /**
    * Connection with clients.
    */
   private static ServerSocket socket;
   
   /**
    * List of all client's connections.
    */
   private static ArrayList<g_Connection> clients;
   
   /**
    * Name of this server.
    */
   private static String name;
   
   /**
    * Server-side game world. This is the official game state.
    */
   private static g_World world;
   
   /**
    * Stores single server in program.
    */
   public static g_Server server;
   
   /**
    * Creates new server if one does not already exist.
    */
   public g_Server(String name){
      if(server == null){
         //Launch server
         try{
            socket = new ServerSocket(PORT * 2);
         
            //Initialize others
            this.name = name;
            
            clients = new ArrayList<g_Connection>();
            world = new g_World();
            
            server = this;
         }catch(IOException e){
            System.out.println(
               "Fail! Server could not be launched. Horrible. " +
               "The Chinese are making this all up. " +
               "Sad!"
            );
            e.printStackTrace();
            System.exit(1);
         }
         
         //Launch client request handler
         (new Thread(new g_Echo())).start();
      }
   }
   
   /**
    * Server initialize and start.
    */
   @Override
   public void run(){
      //Start accepting clients
      while(true){// <-- This cost me hours of my life.
         //Don't accept any clients if capacity reached
         while(world.getNumPlayers() >= MAX_PLAYERS){
            try{
               Thread.sleep(50);
            }catch(InterruptedException e){}
         }
         
         //Accept client
         try{
            Socket client = socket.accept();
            clients.add(new g_Connection(client));
         }catch(IOException | NullPointerException e){
            e.printStackTrace();
         }
      }
   }
   
   /**
    * Return list of all current client connections.
    */
   public ArrayList<g_Connection> getClients(){
      return clients;
   }
   
   /**
    * Return server's given name.
    */
   public String getName(){
      return name;
   }
   
   /**
    * Return server's official game world.
    */
   public g_World getWorld(){
      return world;
   }
}