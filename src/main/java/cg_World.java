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
   
   private ArrayList<bg_Note> notes;
   
   private short currPoints; //Number of recently earned points
   
   private byte pointsMessage;
   
   private byte pointsMessageTimeout;
   
   private byte keyShift; //How far along the scale the keys are shifted. Did that make sense?
   
   private byte scale;
   
   /**
    * Constructor.
    */
   public cg_World(byte gamemode){
      super(gamemode);
      
      //Initialize stuff
      gamestate = new HashMap<Short, byte[]>();
      notes = new ArrayList<bg_Note>();
      
      currPoints = 0;
      pointsMessage = -1;
      pointsMessageTimeout = -1;
      keyShift = 0;
      scale = -1;
   }
   
   /**
    * Render world contents.
    * 
    * @param g2               Graphics object to render into.
    */
   public void render(Graphics2D g2){
      //Figure out who we control
      bg_Player clientPlayer = null;
      do{
         clientPlayer = super.getPlayer(cg_Panel.getConnection().getClientID());
      }while(clientPlayer == null);
      
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
         Font.BOLD,
         util_Utilities.getFontSize()
      ));
      fm = g2.getFontMetrics();
      
      byte spacing = (byte)(fm.getHeight() * 1.2);
      
      //Draw all players' info
      int shiftInd = 1;
      for(Short key : entities.keySet()){
         //Draw player scores and stuff
         if(entities.get(key) instanceof bg_Player){
            bg_Player otherPlayer = (bg_Player)(entities.get(key));
            
            //Ignore data player entity
            if(otherPlayer.getController() == -1){
               //Extract song info
               bpm = (short)(otherPlayer.getColor().getRed());
               scale = (byte)otherPlayer.getColor().getGreen();
               keyShift = (byte)otherPlayer.getColor().getBlue();
               
               if(scale >= 0)
                  g2.drawString(otherPlayer.getName(), (int)(0.02 * cg_Client.SCREEN_WIDTH), spacing);
               
               continue;
            }
            
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
      if(scale >= 0){
         final short NOTE_WIDTH = (short)(0.6 * cg_Client.SCREEN_WIDTH / 10);
         g2.drawString(notes.size() + " " + keyShift + " " + scale + " " + bpm, 100, 100);
         for(byte i = 0; i < notes.size(); i++){
            try{
               bg_Note note = notes.get(i);
               
               //Get rid of note if it ded
               if(currMilliBeats > note.getBeat() + 2){
                  notes.remove(i);
                  i--;
               }
               
               //Find keyboard button that correlates with note
               byte scaleInd = -1;
               for(byte j = 0; j < 10; j++){
                  byte octaves = (byte)(j / util_Music.INTERVALS[scale].length);
                  if(note.getNote() == keyShift + util_Music.INTERVALS[scale][j % util_Music.INTERVALS[scale].length] + 12 * octaves){
                     scaleInd = j;
                     break;
                  }
               }
               if(scaleInd == -1){
                  g2.setColor(Color.RED);
                  g2.drawString(note.getNote() + " " + note.getBeat(), 800, i * 100 + 100);
                  g2.setColor(ui_Theme.getColor(ui_Theme.NOTE_COLOR));
                  continue;
               }
               //g2.drawString(note.getNote() + " " + note.getBeat(), 800, i * 100 + 100);
               //Figure out dimensions and location of note
               final short drawX = (short)((scaleInd + 0.5) * cg_Client.SCREEN_WIDTH / 10),
                           drawY = (short)((1 - 0.5 * (note.getBeat() - currMilliBeats)) * cg_Client.SCREEN_HEIGHT * 3 / 4.0),
                      drawHeight = (short)(note.getDuration() * 100000 * (bpm / 200.0) / cg_Client.SCREEN_HEIGHT);
               
               //Render
               g2.setColor(ui_Theme.getColor(ui_Theme.NOTE_COLOR));
               g2.fillRect(drawX - NOTE_WIDTH / 2, drawY, NOTE_WIDTH, drawHeight);
               //g2.drawString(drawX - NOTE_WIDTH / 2 + " " + drawY, 500, i * 100 + 100);
            }catch(ConcurrentModificationException e){}
         }
      }
      
      //Draw player info
      if(currMilliBeats > 0){
         g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
         toDraw = "Instrument: " + util_Music.instruments[clientPlayer.getInstrument()];
         g2.drawString(toDraw, (int)(0.02 * cg_Client.SCREEN_WIDTH), spacing * 2);
      }
      
      //Show points messages
      if(pointsMessageTimeout > 0){
         String message = null;
         final short alpha = (short)(255.0 * pointsMessageTimeout / Byte.MAX_VALUE);
         
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
         
         g2.drawString(message, 40, spacing * 2);
         
         if(currPoints > 0){
            g2.setColor(new Color(
               ui_Theme.getColor(ui_Theme.TEXT).getRed(),
               ui_Theme.getColor(ui_Theme.TEXT).getGreen(),
               ui_Theme.getColor(ui_Theme.TEXT).getBlue(),
               alpha
            ));
            g2.drawString("+" + currPoints, 40, spacing * 3);
            if(clientPlayer.getBonus() > 0){
               g2.drawString("x" + clientPlayer.getBonus() + " Bonus Combo", 40, spacing * 4);
            }
         }
         
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
   
   public byte getKeyShift(){
      return keyShift;
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
      for(byte i = 0; i < data.length && i < delta.length; i++)
         data[i] += delta[i];
      
      //Set entity's data to new byte data
      entities.get(ID).setData(bytesToData(
         data, entObj
      ));
      
      gamestate.put(ID, data);
   }
   
   public void processAction(final byte intervalIndex, final long actionTime){
      if(keyShift < 0)
         return;
      
      final float actionBeat = (float)((actionTime - songStartTime) / (60000.0 / bpm));
      float closestGap = Float.MAX_VALUE;
      final bg_Player player = super.getPlayer(cg_Panel.getConnection().getClientID());
      final byte noteValue = (byte)(
         keyShift + util_Music.INTERVALS[scale][Math.abs(intervalIndex - 1) % util_Music.INTERVALS[scale].length] +
         12 * (Math.abs(intervalIndex - 1) / util_Music.INTERVALS[scale].length)
      );
      
      //Key pressed
      //if(intervalIndex > 0){
         //Find closest note to current beat
         for(bg_Note note : notes){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - note.getBeat()));
            }
         }
         
         //Play note through midi
         byte instrument = getPlayer(cg_Panel.getConnection().getClientID()).getInstrument();
         cg_MIDI.playNote(noteValue, instrument);
         
         //Show points message
         if(closestGap < ALLOWED_ERROR){
            pointsMessage = 0;
         }else if(closestGap < ALLOWED_ERROR * 2){
            pointsMessage = 1;
         }else if(closestGap < ALLOWED_ERROR * 4){
            pointsMessage = 2;
         }else if(closestGap < ALLOWED_ERROR * 8){
            pointsMessage = 3;
         }else if(closestGap != Float.MAX_VALUE){
            pointsMessage = 4;
         }else{
            pointsMessage = -1;
            pointsMessageTimeout = -1;
            /*
            System.out.print(closestGap + " " + noteValue + " /");
            for(bg_Note note : notes)
               System.out.print(note + " ");
            System.out.println();
            */
            return;
         }
         
         //Award bonus combo
         if(pointsMessage == 0)
            player.setBonus((byte)(player.getBonus() + 1));
         else
            player.setBonus((byte)(0));
         
         pointsMessageTimeout = Byte.MAX_VALUE;
      /*
      //Key released
      }else{
         //Find closest note end to current beat
         for(bg_Note note : notes){
            if(note.getNote() == noteValue){
               closestGap = Math.min(closestGap, Math.abs(actionBeat - (note.getBeat() + note.getDuration())));
            }
         }
      }
      */
      //Award points
      if(closestGap < 1){
         currPoints = super.calculateScore(closestGap, player.getBonus());
         player.setScore((short)(player.getScore() + currPoints));
      }
   }
   
   //Spawn new notes from noteData
   public void processNotes(byte[] noteData){
      byte i = 0;
      /*
      while(i < noteData.length){
         //Spawn new note
         notes.add(new bg_Note(
            noteData[i],
            bytesToShort(noteData, (byte)(i + 1)),
            noteData[i + 3]
         ));
         i += 4;
      }
      */
      final short beat = bytesToShort(noteData, (byte)(noteData.length - 2));
      while(i < noteData.length - 2){
         notes.add(new bg_Note(noteData[i++], beat, (byte)1));
      }
   }
}