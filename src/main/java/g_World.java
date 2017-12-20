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
   
   private NoteSpawner noteSpawner;
   
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
      //gamestate.clear();
      for(Short key : entities.keySet()){
         gamestate.put(
            key,
            dataToBytes(entities.get(key).getData(new LinkedList<Object>()))
         );
      }
      
      //Start game
      if(System.currentTimeMillis() > voteTimeout){
         startSong();
         voteTimeout = Long.MAX_VALUE;
      }
   }
   
   public byte[][] getCurrVote(){
      return currVote;
   }
   
   public ArrayList<byte[]> getSongList(){
      return songList;
   }
   
   public void startVote(){
      //voteTimeout = (long)(System.currentTimeMillis() + 180000); //3 minute timeout
      voteTimeout = (long)(System.currentTimeMillis() + 15000);//TEMPORARY
      
      HashSet<Byte> toVoteOn = new HashSet<>();
      
      byte numChoices = (byte)(Math.min(3, songList.size()));
      
      currVote = new byte[numChoices + 2][2];
      currVote[numChoices][0] = -1;
      currVote[numChoices + 1][0] = -1;
      
      //Choose three songs to vote on
      byte difficulty = 0;//Target difficulty for choosing song
      for(byte r = 0; r < numChoices; r++){
         byte voteSong = 0;
         
         attempt:
            for(byte tolerance = 0; tolerance < 3; tolerance++){
               for(byte tries = 0; tries < 20; tries++){
                  voteSong = (byte)(songList.size() * Math.random());
                  
                  if(!toVoteOn.contains(voteSong) && Math.abs(songList.get(voteSong)[0] - difficulty) > tolerance)
                     break attempt;
               }
            }
         
         //Track song we're voting on
         currVote[r][0] = voteSong;
         toVoteOn.add(voteSong);
         
         difficulty += 2;
      }
   }
   
   /**
    * Trigger song playing in game.
    */
   public void startSong(){
      bg_Player infoEnt = getPlayer((byte)-1);
      
      //System.out.println("startSong()");
      
      //Assign instruments to players
      byte currInstrument = 0;
      for(Short key : entities.keySet()){
         if(entities.get(key) instanceof bg_Player && entities.get(key) != infoEnt){
            bg_Player player = (bg_Player)(entities.get(key));
            player.setInstrument(currInstrument++);
            //System.out.println(player.getInstrument());
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
      
      //Generate all song parts
      ArrayList<ArrayList<ArrayList<Byte>>> wholeSong = new ArrayList<>();
      final short seed = (short)(Math.random() * Short.MAX_VALUE);
      wholeSong.add(util_Music.generatePart((byte)2, seed, util_Music.DRUMS));
      
      noteSpawner = new NoteSpawner(wholeSong);
      noteSpawner.start();
      
      //System.out.println("finishing startSong()");
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
         Short key = Short.MIN_VALUE;
         
         do{
            try{
               key = (Short)iter.next();
            }catch(ConcurrentModificationException e){}
         }while(key == Short.MIN_VALUE);
         
         //Check if data should be sent
         if(entities.get(key) instanceof bg_Note){
            bg_Note test = (bg_Note)(entities.get(key));
            if(test.isDepreciated() || test.getInstrument() != player.getInstrument()){
               System.out.println("asdf " + test.getInstrument() + " " + player.getInstrument());
               continue;
            }
         }
         
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
         else if(entities.get(key) instanceof bg_Note){
            add[2] = NOTE;
         }
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      return ret;
   }
   
   //Spawn notes in correct timing
   private class NoteSpawner extends Thread{
      
      //            W  H  A  T   T  H  E   F  R  I  C  K  .  .  .
      private final ArrayList<ArrayList<ArrayList<Byte>>> allParts;
      //Outer: each instrument part. Index = instrument number
      //Middle: Each beat of song for specific part
      //Inner: Each chord
         
      //Convenient storage for all notes in world. Not the official data set.
      private HashSet<bg_Note> allNotes;
      
      private final short bpm;
      
      private final byte scale, key;
      
      public NoteSpawner(ArrayList<ArrayList<ArrayList<Byte>>> allParts){
         //Initialize stuff
         this.allParts = allParts;
         allNotes = new HashSet<>();
         
         //Figure out song metrics
         bpm = (short)(allParts.get(0).get(0).get(0) * 4);
         scale = allParts.get(0).get(0).get(1);
         key = allParts.get(0).get(0).get(2);
      }
      
      @Override
      public void run(){
         //Progress through each beat
         for(short beat = 1; beat < allParts.get(0).size(); beat++){
            //Execute for each instrument
            for(byte i = 0; i < allParts.size(); i++){
               ArrayList<Byte> chord = allParts.get(i).get(beat);
               
               //Process each note in chord
               processChord:
                  for(Byte note : chord){
                     //Figure out duration of note
                     byte duration = 1;
                     for(short checkBeat = beat; checkBeat < allParts.get(0).size(); checkBeat++){
                        if(allParts.get(i).get(checkBeat).contains(note))
                           duration++;
                        else
                           break;
                     }
                     
                     //Check if we can reuse depreciated note
                     for(bg_Note n : allNotes){
                        if(n.isDepreciated()){
                           n.setData(duration, note, i, beat);
                           continue processChord;
                        }
                     }
                     
                     //Must spawn new note
                     bg_Note newNote = new bg_Note(duration, note, (byte)0, beat);
                     //entities.put((short)(bg_Entity.getEntityCount() - 1), newNote);
                     allNotes.add(newNote);
                     
                     System.out.println("NoteSpawner spawning note");
                  }
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