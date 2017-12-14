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
   
   private final byte serverDifficulty;
   
   private byte[][] currVote; //Current song vote. Row 1 = index of song, row 2 = # votes.
   
   private long voteTimeout; //Time, in milliseconds, at which vote will time out.
   
   private HashSet<NoteSpawner> noteSpawners;
   
   private static ArrayList<byte[]> songList;
   
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
    * Constructor.
    */
   public g_World(byte gamemode, byte serverDifficulty){
      super(gamemode);
      
      //Initialize gamestate tracking structures
      gamestate = new HashMap<Short, byte[]>();
      snapshots = new HashMap<Byte, HashMap<Short, byte[]>>();
      
      this.serverDifficulty = serverDifficulty;
      noteSpawners = new HashSet<NoteSpawner>();
      
      //Spawn fake player to store world data
      spawnPlayer("", Color.WHITE, (byte)(-1));
      
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
   }
   
   public byte[][] getCurrVote(){
      return currVote;
   }
   
   public ArrayList<byte[]> getSongList(){
      return songList;
   }
   
   public void startVote(){
      currVote = new byte[5][2];
      voteTimeout = (long)(System.currentTimeMillis() + 180000); //3 minute timeout
      
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
      
      //Assign instruments to players
      byte currInstrument = 0;
      for(Short key : entities.keySet()){
         if(entities.get(key) instanceof bg_Player){
            bg_Player player = (bg_Player)(entities.get(key));
            player.setInstrument(currInstrument++);
         }
      }
      
      //Figure out winner of vote
      byte highestVotes = 0;
      ArrayList<Byte> maxVotes = new ArrayList<>();
      for(byte i = 0; i < currVote.length; i++){
         if(currVote[i][1] == highestVotes){
            maxVotes.add(i);
         }else if(currVote[i][1] > highestVotes){
            maxVotes.clear();
            maxVotes.add(i);
            highestVotes = currVote[i][1];
         }
      }
      
      //Choose song out of all tied maximums
      byte choice = maxVotes.get((byte)(maxVotes.size() * Math.random()));
      
      //Song statistacs
      
      for(byte i = 0; i < currInstrument; i++){
         //noteSpawners.add(new NoteSpawner(util_Music.generatePart()));
      }
   }
   
   /**
    * Track incomming vote from client.
    */
   public void tallyVote(byte vote){
      currVote[vote][1]++;
   }
   
   /**
    * Create new player in world. Called once for each client join.
    * 
    * @param name             In-game name of new player.
    * @param color            In-game theme color of new player.
    * @param controller       ID of client that controlls new player.
    */
   public void spawnPlayer(String name, Color color, byte controller){
      //Get all player names
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
      
      //Create player
      Short key = bg_Entity.getEntityCount();
      bg_Player player = new bg_Player(name, color, controller);
      
      entities.put(key, player);
      
      //Start taking snapshots of world for client
      snapshots.put(controller, new HashMap<Short, byte[]>());
      
      //TEST ONLY
      if(controller != -1){
         entities.put(bg_Entity.getEntityCount(), new bg_Note((byte)10, (byte)10));
      }
   }
   
   public long getVoteTimeout(){
      return voteTimeout;
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
      Iterator iter = entities.keySet().iterator();
      while(iter.hasNext()){
         Short key = (Short)iter.next();
         
         //Entity's data
         byte[] comp = dataToBytes(entities.get(key).getData(new LinkedList<Object>()));
         
         //Check if we just need delta
         if(snapshots.get(clientID).containsKey(key)){
            byte[] delta = findDelta(snapshots.get(clientID).get(key), comp);
            
            //Update snapshot
            snapshots.get(clientID).put(key, comp);
            comp = delta;
         }else{
            snapshots.get(clientID).put(key, comp);
         }
         
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
         
         //Send entity's type
         if(entities.get(key) instanceof bg_Player)
            add[2] = PLAYER;
         else if(entities.get(key) instanceof bg_Note)
            add[2] = NOTE;
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      return ret;
   }
   
   //Spawn notes in correct timing
   private class NoteSpawner extends Thread{
      
      private final ArrayList<ArrayList<Byte>> song;
      
      public NoteSpawner(ArrayList<ArrayList<Byte>> song){
         //Initialize stuff
         this.song = song;
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (short)(song.get(0).get(0) * 4);
         final byte scale = song.get(0).get(1),
                      key = song.get(0).get(2);
         
         //Progress through each beat
         for(int beat = 1; beat < song.size(); beat++){
            //Spawn all notes for current beat
            ArrayList<Byte> chord = song.get(beat);
            
            for(Byte note : chord){
               byte duration = 0;
               for(int i = beat; i < song.size(); i++){
                  if(song.get(i).contains(chord))
                     duration++;
                  else
                     break;
               }
               
               Short entityKey = bg_Entity.getEntityCount();
               bg_Note toSpawn = new bg_Note(note, duration);
               
               entities.put(entityKey, toSpawn);
            }
            
            //Wait until beat time passes
            try{
               sleep((int)(60000.0 / bpm));
            }catch(InterruptedException e){
               e.printStackTrace();
            }
         }
      }
   }
}