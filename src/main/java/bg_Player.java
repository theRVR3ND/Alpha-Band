/**
 * Alpha Band - Multiplayer Rythym Game | bg_Player
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Player entity in game world.
 */

import java.awt.*;
import java.util.*;

public class bg_Player extends bg_Entity implements bg_Constants{
   
   /**
    * Player's name.
    */
   private String name;
   
   /**
    * Player's theme color.
    */
   private Color color;
   
   /**
    * ID of client that controlls this.
    */
   private byte controller;
   
   /**
    * Constructor.
    * 
    * @param name          Player name.
    * @param color         Player theme color.
    * @param controller    Controller's ID.
    */
   public bg_Player(String name, Color color, byte controller){
      super((short)0, (short)0, (short)0);
      this.name = name;
      this.color = color;
      this.controller = controller;
   }
   
   /**
    * Constructor. Initialize to default values.
    */
   public bg_Player(){
      this(
         "",
         new Color(
            -Byte.MIN_VALUE,
            -Byte.MIN_VALUE,
            -Byte.MIN_VALUE
         ),
         (byte)0
      );
   }
   
   /**
    * Update player.
    * 
    * @param deltaTime        Time (in milliseconds) since last think.
    */
   public void think(final short deltaTime){
      /* Think, dammit. */
   }
   
   /**
    * Return player's name.
    */
   public String getName(){
      return name;
   }
   
   /**
    * Return player's theme color.
    */
   public Color getColor(){
      return color;
   }
   
   /**
    * Return ID of client that controls this.
    */
   public byte getController(){
      return controller;
   }
   
   /**
    * Set player's in-game name.
    * 
    * @param name          New player name.
    */
   public void setName(String name){
      this.name = name;
   }
   
   /**
    * Set player's in-game theme color.
    * 
    * @param color         New player color.
    */
   public void setColor(Color color){
      this.color = color;
   }
   
   /**
    * Execute action based on action code.
    * 
    * @param action        Code of action to execute.
    */
   public void processAction(final byte action){
      switch(action){
         case(MOVE_UP):
            position.setY((short)(position.getY() + 2));
            break;
         
         case(MOVE_DOWN):
            position.setY((short)(position.getY() - 2));
            break;
         
         case(MOVE_LEFT):
            position.setX((short)(position.getX() - 2));
            break;
         
         case(MOVE_RIGHT):
            position.setX((short)(position.getX() + 2));
            break;
      }
   }
   
   /**
    * Return list of essential data of this player.
    * 
    * @param list          List to fill with data.
    */
   public LinkedList<Object> getData(LinkedList<Object> list){
      super.getData(list);
      
      //Add player-specific data
      list.add(name);
      list.add(color);
      list.add(controller);
      
      return list;
   }
   
   /**
    * Set player data from incomming wrapper objects.
    * 
    * @param data          New data to set to.
    */
   public void setData(LinkedList<Object> data){
      super.setData(data);
      
      //Retrieve player-specific data
      name = (String)(data.remove(0));
      color = (Color)(data.remove(0));
      controller = (Byte)(data.remove(0));
   }
}