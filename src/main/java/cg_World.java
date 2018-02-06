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
import java.awt.event.*;
import java.util.*;

public class cg_World extends bg_World{
   
   /**
    * Most recent gamestate received from server.
    */
   private HashMap<Short, byte[]> gamestate;
   
   private ArrayList<cg_Note> notes;
   
   private short currPoints; //Number of recently earned points
   
   private byte pointsMessage;
   
   private byte pointsMessageTimeout;
   
   /**
    * Constructor.
    */
   public cg_World(byte gamemode){
      super(gamemode);
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
      notes = new ArrayList<cg_Note>();
      
      currPoints = 0;
      pointsMessage = -1;
      pointsMessageTimeout = -1;
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
      //g2.drawString(entities.size() + "", 100, 100);
      
      //Draw player info
      toDraw = "Instrument: " + util_Music.instruments[clientPlayer.getInstrument()];
      g2.drawString(toDraw, 40, 50);
      g2.drawString(super.getCurrBeat() + "", 100, 200);
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
      
      //Update/draw notes
      final float currMilliBeats = (float)((System.currentTimeMillis() - songStartTime) / (60000.0 / bpm));
      try{
         for(cg_Note note : notes){
            //*Try* drawing
            note.render(g2, currMilliBeats);
            
            //Get rid of note if it ded
            if(currMilliBeats - (note.getBeat() + note.getDuration()) > 1.5){
               notes.remove(note);
            }
         }
      }catch(ConcurrentModificationException e){}
      
      //Show points messages
      if(pointsMessageTimeout > 0){
         String message = null;
         final short alpha = (short)(255.0 * pointsMessageTimeout / Byte.MAX_VALUE);
         
         if(currPoints > 0){
            g2.drawString("+" + currPoints, 100, 160);
            if(clientPlayer.getBonus() > 0){
               g2.drawString("x" + clientPlayer.getBonus() + " Bonus Combo", 100, 220);
            }
         }
         
         if(pointsMessage == 0){
            message = "Perfect!";
            g2.setColor(new Color(25, 255, 0, alpha));  //Green
         
         }else if(pointsMessage == 1){
            message = "Great!";
            g2.setColor(new Color(157, 224, 0, alpha)); //Yellow-green
         
         }else if(pointsMessage == 2){
            message = "Nice!";
            g2.setColor(new Color(196, 196, 0, alpha)); //Yellow
         
         }else if(pointsMessage == 3){
            message = "Okay...";
            g2.setColor(new Color(255, 125, 0, alpha)); //Orange
         
         }else{
            message = "Potato!";
            g2.setColor(Color.RED);                     //Red
         }
         
         g2.drawString(message, 100, 100);
         
         pointsMessageTimeout--;
      }else{
         currPoints = 0;
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
            ui_Theme.getColor(ui_Theme.TEXT).getRed(),
            ui_Theme.getColor(ui_Theme.TEXT).getGreen(),
            ui_Theme.getColor(ui_Theme.TEXT).getBlue(),
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
   
   public void processAction(final byte noteValue, final long actionTime){
      final float actionBeat = (float)((actionTime - songStartTime) / (60000.0 / bpm));
      float closestGap = Float.MAX_VALUE;
      final bg_Player player = super.getPlayer(cg_Panel.getConnection().getClientID());
      
      //Key pressed
      if(noteValue > 0){
      //if(true){
         //Find closest note to current beat
         for(cg_Note note : notes){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - note.getBeat()));
            }
         }
         
         //Show points message
         if(closestGap < 0.1){
            pointsMessage = 0;
         }else if(closestGap < 0.2){
            pointsMessage = 1;
         }else if(closestGap < 0.4){
            pointsMessage = 2;
         }else if(closestGap < 0.8){
            pointsMessage = 3;
         }else{
            pointsMessage = 4;
         }
         
         //Award bonus combo
         if(pointsMessage == 0)
            player.setBonus((byte)(player.getBonus() + 1));
         else
            player.setBonus((byte)(0));
         
         pointsMessageTimeout = Byte.MAX_VALUE;
      
      //Key released
      }else{
         //Find closest note end to current beat
         for(cg_Note note : notes){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - (note.getBeat() + note.getDuration())));
            }
         }
      }
      
      //Award points
      if(closestGap < 1){
         currPoints = super.calculateScore(closestGap, player.getBonus());
         player.setScore((short)(player.getScore() + currPoints));
      }
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