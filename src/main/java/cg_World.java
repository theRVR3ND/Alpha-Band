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
   
   private static final BufferedImage screwImage = util_Utilities.loadImage("game/screwImage.png");
   
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
      g2.setColor(Color.BLACK);
      g2.fillRect(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      
      //Save current rotation
      AffineTransform orig = g2.getTransform();
      
      //Draw note box background
      g2.setColor(Color.DARK_GRAY);
      g2.fillRoundRect(
         (int)(0.2 * cg_Client.SCREEN_WIDTH),
         (int)(0.9 * cg_Client.SCREEN_HEIGHT),
         (int)(0.6 * cg_Client.SCREEN_WIDTH),
         (int)(0.1 * cg_Client.SCREEN_HEIGHT),
         (int)(0.1 * cg_Client.SCREEN_HEIGHT),
         (int)(0.1 * cg_Client.SCREEN_HEIGHT)
      );
      
      //Draw note boxes
      float drawX = 0.225f;
      Color[] colors = new Color[] {
         Color.RED,
         Color.GREEN,
         Color.BLUE,
         Color.YELLOW
      };
      for(byte i = 0; i < colors.length; i++){
         //Draw box
         g2.setColor(colors[i]);
         g2.fillRoundRect(
            (int)(drawX * cg_Client.SCREEN_WIDTH),
            (int)(0.92 * cg_Client.SCREEN_HEIGHT),
            (int)(0.1 * cg_Client.SCREEN_WIDTH),
            (int)(0.06 * cg_Client.SCREEN_HEIGHT),
            (int)(0.03 * cg_Client.SCREEN_HEIGHT),
            (int)(0.03 * cg_Client.SCREEN_HEIGHT)
         );
         
         drawX += 0.15;
         
         //Draw screws
         if(i != colors.length - 1){
            //Rotate screw to look random
            AffineTransform rot = new AffineTransform();
            rot.setToRotation(
               Math.toRadians((i + 1) * 57),
               (int)((drawX - 0.025) * cg_Client.SCREEN_WIDTH),
               (int)(0.95 * cg_Client.SCREEN_HEIGHT)
            );
            g2.setTransform(rot);
            
            //Draw screw
            final int drawWidth = (int)(0.06 * cg_Client.SCREEN_HEIGHT);
            g2.drawImage(
               screwImage,
               (int)((drawX - 0.025) * cg_Client.SCREEN_WIDTH - drawWidth / 2),
               (int)(0.92 * cg_Client.SCREEN_HEIGHT),
               drawWidth,
               drawWidth,
               null
            );
         
            //Reset transform
            g2.setTransform(orig);
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