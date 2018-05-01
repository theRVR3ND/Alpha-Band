/**
 * Alpha Band - Multiplayer Rythym Game | bg_Player
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
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
    * Instrument that player plays.
    */
   private byte instrument;
   
   /**
    * Number of points player has.
    */
   private short score;
   
   /**
    * Bonus combo.
    */
   private byte bonus;
   
   /**
    * Constructor.
    * 
    * @param name          Player name.
    * @param color         Player theme color.
    * @param controller    Controller's ID.
    */
   public bg_Player(String name, Color color, byte controller){
      this.name = name;
      this.color = color;
      this.controller = controller;
      this.instrument = 0;
      
      score = 0;
      bonus = 0;
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
   
   public byte getInstrument(){
      return instrument;
   }
   
   public short getScore(){
      return score;
   }
   
   public byte getBonus(){
      return bonus;
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
   
   public void setInstrument(byte instrument){
      this.instrument = instrument;
   }
   
   public void setScore(short score){
      this.score = score;
   }
   
   public void setBonus(byte bonus){
      this.bonus = bonus;
   }
   
   /**
    * Return list of essential data of this player.
    * 
    * @param list          List to fill with data.
    */
   public LinkedList<Object> getData(LinkedList<Object> list){
      list.add(name);
      list.add(color);
      list.add(controller);
      list.add(instrument);
      list.add(score);
      list.add(bonus);
      
      return list;
   }
   
   /**
    * Set player data from incomming wrapper objects.
    * 
    * @param data          New data to set to.
    */
   public void setData(LinkedList<Object> data){
      name = (String)(data.remove(0));
      color = (Color)(data.remove(0));
      controller = (Byte)(data.remove(0));
      instrument = (Byte)(data.remove(0));
      score = (Short)(data.remove(0));
      bonus = (Byte)(data.remove(0));
   }
   
   public String toString(){
      return name + " " + controller;
   }
}