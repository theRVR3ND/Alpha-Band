/**
 * Alpha Band - Multiplayer Rythym Game | bg_Note
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Game musical note. Scrolls down for sweet rhythym magic.
 */

import java.util.*;

public class bg_Note extends bg_Entity{
   
   /**
    * Current frame count of this note.
    */
   private byte frameCount;
   
   /**
    * How many frames this note will last.
    */
   private byte duration;
   
   /**
    * Key note value.
    */
   private char value;
   
   /**
    * Constructor. Establish note parameters.
    */
   public bg_Note(byte duration, char value){
      frameCount = Byte.MAX_VALUE;
      this.duration = duration;
      this.value = value;
   }
   
   public bg_Note(){
      this((byte)0, (char)0);
   }
   
   public void think(short deltaTime){
      frameCount--;
   }
   
   public byte getFrameCount(){
      return frameCount;
   }
   
   public byte getDuration(){
      return duration;
   }
   
   public char getValue(){
      return value;
   }
   
   public LinkedList<Object> getData(LinkedList<Object> list){
      list.add(frameCount);
      list.add(duration);
      list.add((byte)value);
      
      return list;
   }
   
   public void setData(LinkedList<Object> data){
      frameCount = (Byte)(data.remove(0));
      duration = (Byte)(data.remove(0));
      value = (Character)(data.remove(0));
   }
}