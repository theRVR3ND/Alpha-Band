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
   //Byte.MIN_VALUE = top of screen, 0 = bottom of screen
   private byte frameCount;
   
   /**
    * How many frames this note will last.
    */
   private byte duration;
   
   /**
    * Key note note.
    */
   private byte note;
   
   private byte instrument;
   
   //Beat in song at which note hits bottom
   private short hitBeat;
   
   /**
    * Constructor. Establish note parameters.
    */
   public bg_Note(byte duration, byte note, byte instrument, short hitBeat){
      super();
      
      this.duration = duration;
      this.note = note;
      this.instrument = instrument;
      this.hitBeat = hitBeat;
      
      frameCount = Byte.MIN_VALUE;
   }
   
   public bg_Note(){
      this((byte)0, (byte)0, (byte)0, (short)0);
   }
   
   public void think(short deltaTime){
      if(frameCount < Byte.MAX_VALUE)
         frameCount++;
   }
   
   public byte getFrameCount(){
      return frameCount;
   }
   
   public byte getDuration(){
      return duration;
   }
   
   public byte getNote(){
      return note;
   }
   
   public byte getInstrument(){
      return instrument;
   }
   
   public boolean isDepreciated(){
      return (frameCount - duration) > 0;
   }
   
   public void setData(byte duration, byte note, byte instrument, short hitBeat){
      this.duration = duration;
      this.note = note;
      this.instrument = instrument;
      this.hitBeat = hitBeat;
      
      frameCount = Byte.MIN_VALUE;
   }
   
   public LinkedList<Object> getData(LinkedList<Object> list){
      list.add(frameCount);
      list.add(duration);
      list.add(note);
      list.add(hitBeat);
      
      return list;
   }
   
   public void setData(LinkedList<Object> data){
      frameCount = (Byte)(data.remove(0));
      duration = (Byte)(data.remove(0));
      note = (Byte)(data.remove(0));
      hitBeat = (Short)(data.remove(0));
   }
}