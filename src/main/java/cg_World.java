/**
 * Kilo - Java Multiplayer Engine | cg_World
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Client-side version of world. Handle client-only actions.
 */

import java.awt.*;
import java.util.*;

public class cg_World extends bg_World{
   
   /**
    * Most recent gamestate received from server.
    */
   private HashMap<Short, byte[]> gamestate;
   
   /**
    * Constructor.
    */
   public cg_World(){
      super();
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
   }
   
   /**
    * Render world contents.
    * 
    * @param g2               Graphics object to render into.
    */
   public void render(Graphics2D g2){
      //Graphics defaults
      g2.setColor(Color.GRAY);
      g2.fillRect(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      g2.setClip(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      
      //Figure out who we are
      bg_Player us = getPlayer(cg_Panel.connection.getClientID());
      
      //Check if world has been initialized yet
      if(us == null)
         return;
      
      //Draw all entities
      for(Short key : entities.keySet()){
         bg_Entity ent = entities.get(key);
         
         final short relX = (short)(ent.getPosition().getX() - us.getPosition().getX()),
                     relY = (short)(ent.getPosition().getY() - us.getPosition().getY());
         
         //Check if in view area
         if(Math.abs(relX) < VIEW_WIDTH && Math.abs(relY) < VIEW_HEIGHT){
            //Draw based on entity type
            if(ent instanceof bg_Player){
               bg_Player player = (bg_Player)ent;
               g2.setColor(player.getColor());
               
               //Draw player
               g2.fillRect(
                  relX * 2 + cg_Client.SCREEN_WIDTH / 2 - 5,
                 -relY * 2 + cg_Client.SCREEN_HEIGHT / 2 - 5,
                  10,
                  10
               );
               
               //Draw player name
               FontMetrics fontMetrics = g2.getFontMetrics();
               final short nameWidth = (short)fontMetrics.stringWidth(player.getName());
               g2.drawString(
                  player.getName(),
                  relX * 2 + cg_Client.SCREEN_WIDTH / 2 - nameWidth / 2,
                 -relY * 2 + cg_Client.SCREEN_HEIGHT / 2 - 10
               );
            }
         }
      }
   }
   
   /**
    * Set data of particular entity.
    * 
    * @param delta            Change in data to execute.
    */
   public void setData(byte[] delta){
      //Retreive entity's ID from bytes
      short ID = bytesToShort(delta, (byte)0);
      
      //Check if we need to spawn a new entity
      if(entities.get(ID) == null){
         bg_Entity spawn = null;
         byte entType = delta[2];
         
         if(entType == PLAYER)
            spawn = new bg_Player();
         
         entities.put(ID, spawn);
      }
      
      //Clip off ID and entity type info
      byte[] clipped = new byte[delta.length - 3];
      for(byte i = 0; i < clipped.length; i++)
         clipped[i] = delta[i + 3];
      
      //Expand compressed data
      delta = expand(clipped);
      
      //Find last official info on entity
      byte[] data;
      LinkedList<Object> entObj = entities.get(ID).getData(new LinkedList<Object>());
      if(gamestate.containsKey(ID))
         data = gamestate.get(ID);
      else
         data = dataToBytes(entObj);
      
      //Add delta to current data
      for(byte i = 0; i < data.length; i++)
         data[i] += delta[i];
      
      //Set entity's data to new byte data
      entities.get(ID).setData(bytesToData(
         data, entObj
      ));
      
      gamestate.put(ID, data);
   }
}