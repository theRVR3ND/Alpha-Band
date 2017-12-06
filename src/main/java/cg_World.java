/**
 * Alpha Band - Multiplayer Rythym Game | cg_World
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Client-side version of world. Handle client-only actions.
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

public class cg_World extends bg_World{
   
   /**
    * Most recent gamestate received from server.
    */
   private HashMap<Short, byte[]> gamestate;
   
   /**
    * Constructor.
    */
   public cg_World(byte gamemode){
      super(gamemode);
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
   }
   
   /**
    * Render world contents.
    * 
    * @param g2               Graphics object to render into.
    */
   public void render(Graphics2D g2){
      //Figure out who we control
      bg_Player player = super.getPlayer(cg_Panel.getConnection().getClientID());
      
      if(player == null){
         return;
      }
      
      //Draw incomming notes
      //g2.setColor(Color.BLACK);
      final byte instrument = player.getInstrument();
      
      //Graphics tings
      g2.setColor(Color.BLACK);
      FontMetrics fm;
      
      //Scoreboard header
      g2.setFont(new Font(
         "Century Gothic",
         Font.BOLD,
         util_Utilities.getFontSize()
      ));
      fm = g2.getFontMetrics();
      
      String toDraw = "SCOREBOARD:";
      g2.drawString(
         toDraw,
         cg_Client.SCREEN_WIDTH - fm.stringWidth(toDraw) - 40,
         50
      );
      
      //Default writing font
      g2.setFont(new Font(
         "Century Gothic",
         Font.PLAIN,
         util_Utilities.getFontSize()
      ));
      fm = g2.getFontMetrics();
      
      //Draw all entities
      int shiftInd = 1;
      for(Short key : entities.keySet()){
         //Draw player scores and stuff
         if(entities.get(key) instanceof bg_Player){
            bg_Player otherPlayer = (bg_Player)(entities.get(key));
            
            //Ignore data player entity
            if(otherPlayer.getController() == -1)
               continue;
            
            //Draw player score on right side of screen
            toDraw = otherPlayer.getName() + ": " + otherPlayer.getScore();
            g2.drawString(
               toDraw,
               cg_Client.SCREEN_WIDTH - fm.stringWidth(toDraw) - 40,
               50 + shiftInd * 30
            );
            shiftInd++;
         
         //Draw cascading note
         }else if(entities.get(key) instanceof bg_Note){
            g2.fillRect(100, 100, 100, 100);
            /*
            bg_Note note = (bg_Note)entities.get(key);
            g2.fillRect(
                note.getNote(),
            );
            */
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
      
      //Check if we need to add a new player or note
      if(entities.get(ID) == null){
         bg_Entity spawn = null;
         byte entType = delta[2];
         
         if(entType == PLAYER)
            spawn = new bg_Player();
         else if(entType == NOTE)
            spawn = new bg_Note();
         
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