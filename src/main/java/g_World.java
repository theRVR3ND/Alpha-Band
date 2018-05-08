/**
 * Alpha Band - Multiplayer Rythym Game | g_World
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Server-side version of world.
 */

import java.util.*;
import java.awt.*;
import java.io.*;

public class g_World extends bg_World{
   
   private final byte serverDifficulty;
   
   private byte[][] currVote; //Current song vote. Row 1 = index of song, row 2 = # votes.
   
   private static ArrayList<byte[]> songList; //Info of all songs on file
   
   private ArrayList<HashMap<Short, HashSet<Byte>>> song; //All notes ever
   
   private ArrayList<HashMap<Short, byte[]>> noteData; //Note data to send to client
   
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
         //System.exit(1);
      }
      
      //Start voting
      startVote();
   }
   
   /**
    * Update world. Record world state.
    */
   public void think(final byte deltaTime){
      super.think(deltaTime);
      
      //Record world state
      try{
         for(Short key : entities.keySet()){
            gamestate.put(
               key,
               dataToBytes(entities.get(key).getData(new LinkedList<Object>()))
            );
         }
      }catch(ConcurrentModificationException e){}
      
      //Start game
      if(song == null && currVote != null){
         if(System.currentTimeMillis() > super.songStartTime - 10000)
            startSong();
      
      //End song
      }else{
         //Start next song process
         if(super.getCurrBeat() > songLength + bpm / 3.0){
            if(currVote == null){
               startVote();
            }
            
            song = null;
            noteData = null;
            songLength = 0;
   
            //Reset stuff
            for(Short key : entities.keySet()){
               if(entities.get(key) instanceof bg_Player){
                  bg_Player player = (bg_Player)(entities.get(key));
                  if(player.getController() != -1){
                     player.setScore((short)0);
                  }else{
                     player.setName("");
                  }
               }
            }
         }else if(super.getCurrBeat() > songLength){
            currVote = null;
         }
      }
   }
   
   //********ACCESSORS********//
   
   public byte[][] getCurrVote(){
      return currVote;
   }
   
   public ArrayList<byte[]> getSongList(){
      return songList;
   }
   
   public short getSongLength(){
      return songLength;
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
         
         //Find change in world data since last update
         byte[] delta;
         if(snapshots.get(clientID).containsKey(key)){
            delta = findDelta(snapshots.get(clientID).get(key), comp);
         }else{
            delta = findDelta(new byte[comp.length], comp);
         }
         
         //Update snapshot
         snapshots.get(clientID).put(key, comp);
         comp = delta;
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
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      return ret;
   }
   
   //Should only be requested by single player who is playing instrument
   public byte[] getNotes(byte clientID){
      final byte instrument = getPlayer(clientID).getInstrument();
      if(noteData != null){
         return noteData.get(instrument).get((short)(super.getCurrBeat() + 6));
      }else{
         return null;
      }
   }
   
   //********MUTATORS********//
   
   public void startVote(){
      //super.songStart = (long)(System.currentTimeMillis() + 180000); //3 minute timeout
      super.songStartTime = (long)(System.currentTimeMillis() + 30000);//TEMPORARY
      
      HashSet<Byte> toVoteOn = new HashSet<>();
      
      byte numChoices = (byte)(Math.min(3, songList.size()));
      
      currVote = new byte[numChoices + 2][2];
      currVote[numChoices][0] = -1;
      currVote[numChoices + 1][0] = -1;
      
      //Choose three songs to vote on
      for(byte r = 0; r < numChoices; r++){
         byte voteSong = 0;
         
         attempt:
            for(byte tolerance = 0; tolerance < 5; tolerance++){
               for(byte tries = 0; tries < 20; tries++){
                  voteSong = (byte)(songList.size() * Math.random());
                  
                  if(!toVoteOn.contains(voteSong) && Math.abs(songList.get(voteSong)[0] - serverDifficulty) <= tolerance){
                     //Track song we're voting on
                     currVote[r][0] = voteSong;
                     toVoteOn.add(voteSong);
                     break attempt;
                  }
               }
            }
      }
      
      for(byte r = 0; r < currVote.length; r++){
         for(byte c = 0; c < currVote[0].length; c++){
            System.out.print(currVote[r][c] + " ");
         }
         System.out.println();
      }
      System.out.println();
   }
   
   /**
    * Trigger song playing in game.
    */
   public void startSong(){
      bg_Player infoEnt = null;
      do{
         infoEnt = getPlayer((byte)-1);
      }while(infoEnt == null);
      
      //Assign instruments to players
      if(super.gamemode == COMPETITION){
         //Competition - every player plays piano
         for(Short key : entities.keySet()){
            if(entities.get(key) instanceof bg_Player){
               bg_Player player = (bg_Player)(entities.get(key));
               if(player.getController() != -1)
                  player.setInstrument(util_Music.PIANO);
            }
         }
      }else{
         //Collaborative (band) mode - each player gets unique instrument
         byte currInstrument = 0;
         for(Short key : entities.keySet()){
            if(entities.get(key) instanceof bg_Player){
               bg_Player player = (bg_Player)(entities.get(key));
               if(player.getController() != -1)
                  player.setInstrument(currInstrument++);
            }
         }
      }
      
      //Figure out winner of vote
      byte highestVotes = 0;
      ArrayList<Byte> maxVotes = new ArrayList<>();
      
      while(currVote == null){
         try{
            Thread.sleep(10);
         }catch(InterruptedException e){}
      }
      
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
      byte choice = maxVotes.get((byte)(maxVotes.size() * Math.random()));//Index in currVote
      if(choice == currVote.length - 2)
         choice = (byte)(Math.random() * songList.size());
      
      //Song parameters
      song = new ArrayList<>();
      byte scale = 0, //Song's scale
             key = 0; //Song's key
      songLength = 0;
      
      //Generate/load song part for players
      if(choice == currVote.length - 1){//Randomly generated song
         final short seed = (short)(Math.random() * Short.MAX_VALUE);
         this.bpm = (short)(util_Music.generateBPM((byte)2, seed) * 2);
         scale = util_Music.chooseScale(seed);
         key = util_Music.chooseKey(seed);
         
         //Generate part for each different instrument
         if(super.gamemode == COLLABORATIVE){
            for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
               song.add(util_Music.generatePart(serverDifficulty, seed, i));
            }
         
         //Generate same part (piano) for each player
         }else{
            song.add(util_Music.generatePart(serverDifficulty, seed, util_Music.PIANO));
         }
         
         infoEnt.setName("Randomly generated song");
         songLength = util_Music.generateSongLength(serverDifficulty, seed);
      
      }else{//Load song
         try{
            Scanner input = new Scanner(new File(
               util_Utilities.getDirectory() + "/resources/songs/" +
               new String(songList.get(currVote[choice][0]), 4, songList.get(currVote[choice][0])[3]) + ".cfg"
            ));
            
            input.nextLine(); //Skip song difficulty
            input.nextLine(); //Skip song length
            
            this.bpm = (short)(input.nextInt());
            scale = input.nextByte();
            key = input.nextByte();
            input.nextLine(); //Thing to do thing. Thing.
            
            //Load notes
            ArrayList<HashMap<Short, HashSet<Byte>>> songFile = new ArrayList<>();
            for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
               songFile.add(new HashMap<Short, HashSet<Byte>>());
               
               while(true){
                  String[] line = input.nextLine().split(" ");
                  
                  if(line.length <= 1)
                     break;
                  
                  short beat = Short.parseShort(line[0]);
                  songFile.get(i).put(beat, new HashSet<Byte>());
                  
                  for(byte j = 1; j < line.length; j++){
                     songFile.get(i).get(beat).add(Byte.parseByte(line[j]));
                  }
                  songLength = (short)(Math.max(songLength, beat + 1));
               }
            }
            
            //Assign part for each different instrument
            if(super.gamemode == COLLABORATIVE){
               song = songFile;
            }else{
               //Assign same part (piano) for each player
               song.add(songFile.get(0));
            }
         
         }catch(IOException e){
            e.printStackTrace();
         }
         
         //Share song name with players
         infoEnt.setName(new String(
            songList.get(currVote[choice][0]),
            4,
            songList.get(currVote[choice][0])[3]
         ));
      }
      
      //Translate song into bytes for sending
      noteData = new ArrayList<>();
      for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
         //No need to convert same note data for each player
         if(gamemode == COMPETITION && i > 0)
            break;
         
         //Convert notes to byte array
         noteData.add(new HashMap<Short, byte[]>());
         for(short beat = 0; beat < songLength; beat++){
            final byte numNotes; //Number of notes played on beat
            if(song.get(i).get(beat) != null){
               numNotes = (byte)song.get(i).get(beat).size();
            }else{
               continue;
            }
            byte[] notes = new byte[numNotes + 2];
            
            byte[] bytes = shortToBytes(beat);
            notes[notes.length - 2] = bytes[0];
            notes[notes.length - 1] = bytes[1];
            
            byte n = 0;
            for(Byte note : song.get(i).get(beat)){
               notes[n++] = note;
            }
            
            noteData.get(i).put(beat, notes);
         }
      }
      
      //"Send" song info to clients
      infoEnt.setColor(new Color(bpm, scale, key));
      infoEnt.setScore(songLength);
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
   
   /**
    * Track incomming vote from client.
    */
   public void tallyVote(byte vote){
      currVote[vote][1]++;
   }
   
   public void processAction(final byte clientID, final byte noteValue, final long actionTime){
      final float actionBeat = (float)((actionTime - super.songStartTime) / (60000.0 / bpm));
      final bg_Player player = getPlayer(clientID);
      float closestGap = Float.MAX_VALUE;
      
      //Find closest note to current beat
      for(Short beat : song.get(player.getInstrument()).keySet()){
         for(Byte note : song.get(player.getInstrument()).get(beat)){
            if(note == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - beat));
            }
         }
      }
      
      //Award bonus combo
      if(closestGap < ALLOWED_ERROR)
         player.setBonus((byte)(player.getBonus() + 1));
      else
         player.setBonus((byte)(0));
      
      //Award points
      if(closestGap < 1){
         player.setScore((short)(player.getScore() + super.calculateScore(closestGap, player.getBonus())));
      }
   }
}