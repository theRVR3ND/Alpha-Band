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
   
   private HashSet<byte[]> notes;
   
   /**
    * Constructor.
    */
   public cg_World(byte gamemode){
      super(gamemode);
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
      notes = new HashSet<byte[]>();
   }
   
   /**
    * Render world contents.
    * 
    * @param g2               Graphics object to render into.
    */
   public void render(Graphics2D g2){
      //Figure out who we control
      bg_Player clientPlayer = super.getPlayer(cg_Panel.getConnection().getClientID());
      
      //Background
      g2.setColor(ui_Theme.getColor(ui_Theme.BACKGROUND));
      g2.fillRect(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      
      //Graphics tings
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      FontMetrics fm;
      
      //Scoreboard header
      g2.setFont(new Font(
         "Century Gothic",
         Font.BOLD,
         util_Utilities.getFontSize()
      ));
      fm = g2.getFontMetrics();
      
      String toDraw = "BANDMATES/SCORE";
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
      g2.drawString(entities.size() + "", 100, 100);
      
      //Draw player info
      toDraw = "Instrument: " + util_Music.instruments[clientPlayer.getInstrument()];
      g2.drawString(toDraw, 40, 50);
      
      //g2.drawString("currBeat: " + super.getCurrBeat(), 200, 200);
      //g2.drawString("countdown: " + (songStartTime - System.currentTimeMillis()), 200, 250);
      
      //Song info
      bpm = (byte)(super.getPlayer((byte)-1).getColor().getRed());
      
      //Draw all entities' info
      int shiftInd = 1;
      byte spacing = (byte)(fm.getHeight() * 1.2);
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
               50 + shiftInd * spacing
            );
            
            //Underline client's player score
            if(clientPlayer.getController() == otherPlayer.getController()){
               g2.drawLine(
                  cg_Client.SCREEN_WIDTH - fm.stringWidth(toDraw) - 40,
                  54 + shiftInd * spacing,
                  cg_Client.SCREEN_WIDTH - 40,
                  54 + shiftInd * spacing
               );
            }
            
            shiftInd++;
         
         //Draw note
         }/*else if(entities.get(key) instanceof bg_Note){
            bg_Note note = (bg_Note)(entities.get(key));
            
            final short noteWidth = (short)(cg_Client.SCREEN_WIDTH / (Byte.MAX_VALUE + 1.0));
            //final short noteY = (short)();
            
            g2.setColor(ui_Theme.getColor(ui_Theme.NOTE_COLOR));
            g2.fillRect(
               noteWidth * note.getNote(),
               100,
               noteWidth,
               100
            );
         }*/
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
   
   //Spawn new notes from noteData
   public void processNotes(byte[] noteData){
      for(byte i = 0; i < noteData.length; i += 5){
         //Spawn new note
         /*
         short key = bg_Entity.getEntityCount();
         entities.put(
            key,
            new bg_Note(
               noteData[i],
               noteData[i + 1],
               noteData[i + 2],
               bytesToShort(noteData, (byte)(i + 3))
            )
         );
         */
         notes.add(new byte[] {
               noteData[i],
               noteData[i + 1],
               noteData[i + 2],
               noteData[i + 3],
               noteData[i + 4],
         });
      }
   }
}