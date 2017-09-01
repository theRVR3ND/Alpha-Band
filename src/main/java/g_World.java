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
    * Constructor.
    */
   public g_World(){
      super();
      
      //Initialize gamestate tracking structures
      gamestate = new HashMap<Short, byte[]>();
      snapshots = new HashMap<Byte, HashMap<Short, byte[]>>();
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
   
   /**
    * Create new player in world.
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
      
      //Create player
      Short key = bg_Entity.getEntityCount();
      bg_Player player = new bg_Player(actualName, color, controller);
      
      entities.put(key, player);
      
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
      HashMap<Short, byte[]> visible = new HashMap<Short, byte[]>();
      
      //Find entities that are within view area
      for(Short key : entities.keySet()){
         bg_Entity ent = entities.get(key);
         short distX = (short)(player.getPosition().getX() - ent.getPosition().getX()),
               distY = (short)(player.getPosition().getY() - ent.getPosition().getY());
         if(Math.abs(distX) < VIEW_WIDTH && Math.abs(distY) < VIEW_HEIGHT){
            visible.put(
               key, dataToBytes(ent.getData(new LinkedList<Object>()))
            );
         }
      }
      
      LinkedList<byte[]> ret = new LinkedList<byte[]>();
      
      //Find difference between visible and client's snapshot
      for(Short key : visible.keySet()){
         //Bytes to compress data into
         byte[] comp = new byte[visible.get(key).length];
         for(byte i = 0; i < comp.length; i++)
            comp[i] = visible.get(key)[i];
         
         //Check if we can save byte space
         if(snapshots.get(clientID).containsKey(key)){
            comp = findDelta(snapshots.get(clientID).get(key), comp);
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
         
         add[2] = PLAYER;
         
         for(byte i = 0; i < comp.length; i++)
            add[i + 3] = comp[i];
         
         ret.add(add);
      }
      
      //Update client's snapshot
      for(Short key : visible.keySet()){
         snapshots.get(clientID).put(
            key, visible.get(key)
         );
      }
      
      return ret;
   }
}