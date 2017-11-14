/**
 * Alpha Band - Multiplayer Rythym Game | g_World
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Server-side version of world. Has world data saving feature.
 */

import java.util.*;
import java.awt.*;
import java.io.*;

public class g_World extends bg_World{
   
   /**
    * Master gamestate. Holds all current entity states.
    * Data is NOT compressed.
    */
   private HashMap<Short, byte[]> gamestate;
   
   /**
    * Most recent game state sent to paticular clients.
    * Key is client ID. Inner map key is entity ID. Data
    * is NOT compressed.
    */
   private HashMap<Byte, HashMap<Short, byte[]>> snapshots;
   
   /**
    * Current song vote. Row 1 = index of song, row 2 = # vote.
    */
   private byte[][] currVote;
   
   /**
    * Countdown for vote.
    */
   private short voteTimeout;
   
   /**
    * Info of all songs that can be played.
    */
   private static ArrayList<byte[]> songList;
   
   /**
    * Constructor.
    */
   public g_World(byte gamemode){
      super(gamemode);
      
      //Initialize gamestate tracking structures
      gamestate = new HashMap<Short, byte[]>();
      snapshots = new HashMap<Byte, HashMap<Short, byte[]>>();
      
      //Spawn fake player to store world data
      spawnPlayer("", Color.WHITE, (byte)-1);
         
      //Load all songs on desktop
      try{
         File[] folder = (new File(util_Utilities.getDirectory() + "/resources/songs")).listFiles();
         songList = new ArrayList<>(folder.length);
         
         //Read in each song's info
         for(File f : folder){
            //Input lines
            Scanner input = new Scanner(f);
            
            //Check if file is in correct format
            String songName = f.getName();
            songName = songName.substring(0, songName.indexOf("."));
            
            if(songName.equals("example"))
               continue;
            
            byte[] info = new byte[songName.length() + 4];
            
            //Put in other song info
            for(byte i = 0; i < 3; i++)
               info[i] = input.nextByte();
            
            //Put in song name
            info[3] = (byte)(songName.length());
            for(byte i = 0; i < songName.length(); i++){
               info[i + 4] = (byte)(songName.charAt(i));
            }
            
            songList.add(info);
         }
      }catch(IOException e){
         System.out.println("Error while loading songs.");
         e.printStackTrace();
         System.exit(1);
      }
      
      //Start voting
      startVote();
   }
   
   /**
    * Update world. Record world state.
    */
   public void think(){
      super.think();
      
      //Record world state
      gamestate.clear();
      for(Short key : entities.keySet()){
         gamestate.put(
            key,
            dataToBytes(entities.get(key).getData(new LinkedList<Object>()))
         );
      }
      
      //Update current vote
      if(voteTimeout > 0){
         voteTimeout--;
      }else if(voteTimeout == 0 && currVote != null){
         //Figure out which song won. Start winning song.
         byte max = Byte.MIN_VALUE;
         byte ind = 0;
         for(byte i = 0; i < currVote.length; i++){
            if(max < currVote[i][1]){
               max = currVote[i][1];
               ind = i;
            }
         }
         
         //If randomly generating/picking song
         if(ind == 3)
            ind = (byte)(Math.random() * 3);
         else if(ind == 4)
            ind = -1;
         
         startSong(ind);
      }
   }
   
   public byte[][] getCurrVote(){
      return currVote;
   }
   
   public ArrayList<byte[]> getSongList(){
      return songList;
   }
   
   public void startVote(){
      currVote = new byte[5][2];
      voteTimeout = (short)(300);
      
      //Choose three songs to vote on
      for(byte r = 0; r < 3; r++){
         if(songList.size() < r + 1){
            for(byte i = r; i < 3; i++)
               currVote[i][0] = -1;
            break;
         }
         
         //Random difficulty
         final byte diff = (byte)(Math.pow(Math.random() - 0.5, 2) * 10 * (Math.random() - 0.5) + 2);
         
         //Find random song with correct difficulty
         byte ind = (byte)(Math.random() * songList.size());
         test:
            for(byte tolerance = 0; tolerance < 3; tolerance++){
               for(byte i = 0; i < songList.size(); i++){
                  if(Math.abs(songList.get(ind)[0] - diff) < tolerance){
                     ind = (byte)((ind + 1) % songList.size());
                     break test;
                  }
               }
            }
         
         //Track song we're voting on
         currVote[r][0] = ind;
      }
   }
   
   /**
    * Trigger song playing in game.
    */
   public void startSong(byte ind){
      bg_Player infoEnt = getPlayer((byte)-1);
      
      //Song progress data is transferred through infoEnt's name
      String name = (char)(currVote[ind][0]) + "";
      
      //Randomly generated song to be played
      if(ind == -1){
         //Send seed info through infoEnt's data
         byte[] bytes = shortToBytes((short)(Math.random() * Short.MAX_VALUE));
         name += (char)(bytes[0]) + "" + (char)(bytes[1]);
      
      //Preset song selected
      }else{
         name += (char)(ind);
      }
      
      infoEnt.setName(name);
   }
   
   /**
    * Track incomming vote from client.
    */
   public void tallyVote(byte vote){
      currVote[vote][1]++;
   }
   
   /**
    * Create new player in world.
    * 
    * @param name             In-game name of new player.
    * @param color            In-game theme color of new player.
    * @param controller       ID of client that controlls new player.
    */
   public void spawnPlayer(String name, Color color, byte controller){
      //Get all player names
      //if(controller >= 0){
      HashSet<String> allNames = new HashSet<String>();
      for(Short key : entities.keySet())
         if(entities.get(key) instanceof bg_Player)
            allNames.add(((bg_Player)(entities.get(key))).getName());
      
      //Make sure name is unique (no other players can have same exact name)
      String actualName = name;
      byte i = 1;
      while(allNames.contains(actualName)){
         actualName = name + "(" + i + ")";
         i++;
      }
      name = actualName;
      //}
      
      //Create player
      Short key = bg_Entity.getEntityCount();
      bg_Player player = new bg_Player(name, color, controller);
      
      entities.put(key, player);
      
      System.out.println("spawning: " + player);
      
      //Start taking snapshots of world for client
      snapshots.put(controller, new HashMap<Short, byte[]>());
   }
   
   /**
    * Return data of entities that are visible/owned by client. Data
    * returned is in compressed form.
    * 
    * @param clientID         ID of client to send to.
    */
   public LinkedList<byte[]> getRelevantData(final byte clientID){
      bg_Player player = getPlayer(clientID);
      
      LinkedList<byte[]> ret = new LinkedList<byte[]>();
      
      //Find difference between current world and client's snapshot
      for(Short key : entities.keySet()){
         //Entity's data
         byte[] comp = dataToBytes(entities.get(key).getData(new LinkedList<Object>()));
         
         //Has entity already been tracked in client's snapshot
         //boolean inSnapshot = snapshots.get(clientID).containsKey(key);
         
         //Check if we can save byte space
         /*
         if(inSnapshot){
            byte[] temp = comp;
            comp = findDelta(snapshots.get(clientID).get(key), comp);
            
            //Update snapshot
            snapshots.get(clientID).put(key, temp);
         }else{
            //Start new snapshot
            snapshots.get(clientID).put(key, comp);
         }
         */
         
         //Check if we just need delta
         if(snapshots.get(clientID).containsKey(key)){
            byte[] delta = findDelta(snapshots.get(clientID).get(key), comp);
            
            //Update snapshot
            snapshots.get(clientID).put(key, comp);
            comp = delta;
         }else
            snapshots.get(clientID).put(key, comp);
            
         
         comp = compress(comp);
         
         //Check if sending data is neccessary
         if(comp.length == 2 && comp[0] == 0){
            continue;
         }
         
         //Add other entity info
         byte[] keyBytes = shortToBytes(key);
         
         byte[] add = new byte[comp.length + 3];
         
         add[0] = keyBytes[0];
         add[1] = keyBytes[1];
         
         add[2] = PLAYER;
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      return ret;
   }
}