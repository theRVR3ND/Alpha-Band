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
    * Key note note.
    */
   private byte note;
   
   /**
    * Constructor. Establish note parameters.
    */
   public bg_Note(byte duration, byte note){
      frameCount = Byte.MAX_VALUE;
      this.duration = duration;
      this.note = note;
   }
   
   public bg_Note(){
      this((byte)0, (byte)0);
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
   
   public byte getValue(){
      return note;
   }
   
   public LinkedList<Object> getData(LinkedList<Object> list){
      list.add(frameCount);
      list.add(duration);
      list.add((byte)note);
      
      return list;
   }
   
   public void setData(LinkedList<Object> data){
      frameCount = (Byte)(data.remove(0));
      duration = (Byte)(data.remove(0));
      note = (Byte)(data.remove(0));
   }
}