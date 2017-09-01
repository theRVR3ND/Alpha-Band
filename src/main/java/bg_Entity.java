/**
 * Alpha Band - Multiplayer Rythym Game | bg_Entity
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Entity in game world.
 */

import java.util.*;

public abstract class bg_Entity{
   
   /**
    * Position of this in world.
    */
   protected bg_Vector position;
   
   /**
    * Running total of number of entities instantiated.
    */
   private static short entityCount = 0;
   
   /**
    * Constructor.
    */
   public bg_Entity(short x, short y, short rot){
      position = new bg_Vector(x, y, rot);
      entityCount++;
   }
   
   /**
    * Update entity.
    * 
    * @param deltaTime        Time (in milliseconds) since last think.
    */
   public void think(final short deltaTime){
      /* Think, dammit. */
   }
   
   /**
    * Return position of this.
    */
   public bg_Vector getPosition(){
      return position;
   }
   
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
   public LinkedList<Object> getData(LinkedList<Object> list){
      //Add primitives and stuff to list
      list.add(position.getX());
      list.add(position.getY());
      list.add(position.getRot());
      
      return list;
   }
   
   /**
    * Set entity data from incomming wrapper objects.
    * 
    * @param data             Changes in data to process.
    */
   public void setData(LinkedList<Object> data){
      position.setX((Short)data.remove(0));
      position.setY((Short)data.remove(0));
      position.setRot((Short)data.remove(0));
   }
}