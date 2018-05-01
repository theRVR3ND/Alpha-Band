/**
 * Kilo - Java Multiplayer Engine | bg_Entity
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Entity in game world.
 */

import java.util.*;

public abstract class bg_Entity{
   
   /**
    * Running total of number of entities instantiated.
    */
   private static short entityCount = 0;
   
   /**
    * Constructor.
    */
   public bg_Entity(){
      entityCount++;
   }
   
   /**
    * Update entity.
    * 
    * @param deltaTime        Time (in milliseconds) since last think.
    */
   public abstract void think(final short deltaTime);
   
   /**
    * Return count of number of entities initialized.
    */
   public static short getEntityCount(){
      return entityCount;
   }
   
   /**
    * Return list of essential data of this entity.
    * 
    * @param list             List to put data into.
    */
   public abstract LinkedList<Object> getData(LinkedList<Object> list);
   
   /**
    * Set entity data from incomming wrapper objects.
    * 
    * @param data             Changes in data to process.
    */
   public abstract void setData(LinkedList<Object> data);
}