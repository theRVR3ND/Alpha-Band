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
   
   private ArrayList<cg_Note> notes;
   
   /**
    * Constructor.
    */
   public cg_World(byte gamemode){
      super(gamemode);
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
      notes = new ArrayList<cg_Note>();
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
      
      //Text tings
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
      g2.drawString(notes.size() + " " + super.getCurrBeat(), 100, 200);
      //Song info
      bpm = (short)(super.getPlayer((byte)-1).getColor().getRed() * 2);
      
      //Draw all players' info
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
         }
      }
      
      //Draw notes
      final float currMilliBeats = (float)((System.currentTimeMillis() - songStartTime) / (60000.0 / bpm));
      Iterator iter = notes.iterator();
      while(iter.hasNext()){
         try{
            ((cg_Note)(iter.next())).render(g2, currMilliBeats);
         }catch(ConcurrentModificationException e){}
      }
      
      //Show countdown
      if(System.currentTimeMillis() < songStartTime && songStartTime - System.currentTimeMillis() < 10000){
         g2.setFont(new Font(
            "Century Gothic",
            Font.PLAIN,
            util_Utilities.getFontSize() * 8
         ));
         
         short alpha = (short)(255.0 * ((songStartTime - System.currentTimeMillis()) % 1000) / 1000);
         g2.setColor(new Color(
            g2.getColor().getRed(),
            g2.getColor().getGreen(),
            g2.getColor().getBlue(),
            alpha
         ));
         
         toDraw = 1 + (songStartTime - System.currentTimeMillis()) / 1000 + "";
         
         g2.drawString(
            toDraw,
            cg_Client.SCREEN_WIDTH / 2 - g2.getFontMetrics().stringWidth(toDraw) / 2,
            cg_Client.SCREEN_HEIGHT / 2
         );
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
      byte i = 0;
      while(i < noteData.length){
         //Spawn new note
         notes.add(new cg_Note(
            noteData[i],
            bytesToShort(noteData, (byte)(i + 1)),
            noteData[i + 3]
         ));
         i += 4;
      }
   }
}