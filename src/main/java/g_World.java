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
   
   private static ArrayList<byte[]> songList;
   
   private HashMap<Byte, HashSet<bg_Note>> notes; //Key: instrument number, Value: note buffer
   
   private HashMap<Byte, HashSet<byte[]>> noteData; //Notes to send to client
   
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
   
   private NoteSpawner noteSpawner;
   
   /**
    * Constructor.
    */
   public g_World(byte gamemode, byte serverDifficulty){
      super(gamemode);
      
      //Initialize gamestate tracking structures
      gamestate = new HashMap<Short, byte[]>();
      snapshots = new HashMap<Byte, HashMap<Short, byte[]>>();
      notes = new HashMap<Byte, HashSet<bg_Note>>();
      noteData = new HashMap<Byte, HashSet<byte[]>>();
      
      for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
         notes.put(i, new HashSet<bg_Note>());
         noteData.put(i, new HashSet<byte[]>());
      }
      
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
      if(System.currentTimeMillis() > voteTimeout){
         startSong();
         voteTimeout = Long.MAX_VALUE;
      }
   }
   
   //********ACCESSORS********//
   
   public byte[][] getCurrVote(){
      return currVote;
   }
   
   public ArrayList<byte[]> getSongList(){
      return songList;
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
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      return ret;
   }
   
   //Should only be requested by single player who is playing instrument
   public HashSet<byte[]> getNotes(byte clientID){
      final byte instrument = getPlayer(clientID).getInstrument();
      HashSet<byte[]> ret = noteData.get(instrument);
      noteData.put(instrument, new HashSet<byte[]>());
      return ret;
   }
   
   //********MUTATORS********//
   
   public void startVote(){
      //voteTimeout = (long)(System.currentTimeMillis() + 180000); //3 minute timeout
      songStartTime = (long)(System.currentTimeMillis() + 10000);//TEMPORARY
      
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
      bg_Player infoEnt = null;
      do{
         infoEnt = getPlayer((byte)-1);
      }while(infoEnt == null);
      
      //Assign instruments to players
      //if(super.gamemode == COMPETITION){
      if(false){
         //Competition - every player plays piano
         for(Short key : entities.keySet()){
            if(entities.get(key) instanceof bg_Player && entities.get(key) != infoEnt){
               bg_Player player = (bg_Player)(entities.get(key));
               player.setInstrument(util_Music.PIANO);
            }
         }
      }else{
         //Collaborative (band) mode - each player gets unique instrument
         byte currInstrument = 0;
         for(Short key : entities.keySet()){
            if(entities.get(key) instanceof bg_Player && entities.get(key) != infoEnt){
               bg_Player player = (bg_Player)(entities.get(key));
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
      
      //Generate all song parts
      ArrayList<HashMap<Short, HashSet<Byte>>> song = new ArrayList<>();
      byte scale = 0, //Song's scale
             key = 0; //Song's key
      //if(choice == currVote.length - 1){//Randomly generated song
      if(true){
         final short seed = (short)(Math.random() * Short.MAX_VALUE);
         bpm = (short)(util_Music.generateBPM((byte)2, seed) * 2);
         scale = util_Music.chooseScale(seed);
         key = util_Music.chooseKey(seed);
         
         for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
            song.add(util_Music.generatePart(serverDifficulty, seed, i));
         }
      }else{//Load song
      
      }
      
      //"Send" song info to clients
      infoEnt.setColor(new Color(bpm, scale, key));
      
      //Start spawning notes
      noteSpawner = new NoteSpawner(song);
      noteSpawner.start();
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
      
      //System.out.println(player);
   }
   
   /**
    * Track incomming vote from client.
    */
   public void tallyVote(byte vote){
      currVote[vote][1]++;
   }
   
   public void processAction(final byte clientID, final byte noteValue, final long actionTime){
      final float actionBeat = (float)((actionTime - songStartTime) / (60000.0 / bpm));
      final bg_Player player = getPlayer(clientID);
      float closestGap = Float.MAX_VALUE;
      
      //Key pressed
      if(noteValue > 0){
         //Find closest note to current beat
         for(bg_Note note : notes.get(player.getInstrument())){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - note.getBeat()));
            }
         }
         
         //Award bonus combo
         if(closestGap < ALLOWED_ERROR)
            player.setBonus((byte)(player.getBonus() + 1));
         else
            player.setBonus((byte)(0));
      
      //Key released
      }else{
         //Find closest note end to current beat
         for(bg_Note note : notes.get(player.getInstrument())){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - (note.getBeat() + note.getDuration())));
            }
         }
      }
      
      //Award points
      if(closestGap < 1){
         player.setScore((short)(player.getScore() + super.calculateScore(closestGap, player.getBonus())));
      }
   }
   
   /**
    * THIS IS A THING THAT SPAWNS NOTES. YES IT IS UGLY BUT IT IS MINE. SO BACK OFF.
    */
   private class NoteSpawner extends Thread{
      
      private final ArrayList<HashMap<Short, HashSet<Byte>>> song;
      
      public NoteSpawner(ArrayList<HashMap<Short, HashSet<Byte>>> song){
         //Initialize stuff
         this.song = song;
      
         //Print song
         System.out.println("BEAT      PIANO         GUITAR        DRUMS         BASS          DIST_GUIT     AGOGO");
         for(short b = 1; b < 100; b++){
            System.out.print(b + "\t-      ");
            for(byte i = 0; i < song.size(); i++){
               String toPrint = "";
               if(song.get(i).get(b) != null){
                  for(Byte n : song.get(i).get(b))
                     toPrint += n + " ";
               }
               while(toPrint.length() < 14)
                  toPrint += " ";
               System.out.print(toPrint);
            }
            System.out.println();
         }
      }
      
      @Override
      public void run(){
         //Figure out song metrics
         final short bpm = (short)(2 * (Byte)(song.get(0).get((short)0).iterator().next()));
         
         //Track currently "playing" notes
         ArrayList<HashSet<Byte>> currNotes = new ArrayList<>();
         for(byte i = 0; i < song.size(); i++)
            currNotes.add(new HashSet<Byte>());
         
         //Progress through each beat
         short beat = 1;
         while(true){//YEAH CHANGE THIS LATER BUDDY
            //Track start time of loop
            final long startTime = System.currentTimeMillis();
            
            //Play chord for each instrument in current beat
            for(byte instrument = 0; instrument < util_Music.NUM_INSTRUMENTS; instrument++){
               //Current beat's notes to play
               HashSet<Byte> chord = song.get(instrument).get(beat);
               
               if(chord == null){
                  currNotes.get(instrument).clear();
                  continue;
               }
               
               for(Byte note : chord){
                  //Check if note has already been spawned
                  if(currNotes.get(instrument).contains(note))
                     continue;
                  
                  //Find duration of note
                  byte duration = 1;
                  HashSet<Byte> nextChord = song.get(instrument).get((short)(beat + duration));
                  while(nextChord != null && nextChord.contains(note))
                     duration++;
                  
                  //Spawn note
                  notes.get(instrument).add(new bg_Note(note, beat, duration));
                  
                  //Byte data of note (to send to client)
                  byte[] bytes = shortToBytes(beat);
                  noteData.get(instrument).add(new byte[] {note, bytes[0], bytes[1], duration});
               }
            }
            
            //Wait until next beat
            try{
               int sleepTime = (int)(startTime + 60000.0 / bpm - System.currentTimeMillis());
               if(sleepTime > 0)
                  sleep(sleepTime);
            }catch(InterruptedException e){}
            
            beat++;
         }
      }
   }
}