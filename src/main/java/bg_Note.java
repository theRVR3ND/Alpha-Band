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
   }
   
   public bg_Note(){
      this((byte)0, (byte)0, (byte)0, (short)0);
   }
   
   public void think(short deltaTime){}
   
   public byte getDuration(){
      return duration;
   }
   
   public byte getNote(){
      return note;
   }
   
   public byte getInstrument(){
      return instrument;
   }
   
   public void setData(byte duration, byte note, byte instrument, short hitBeat){
      this.duration = duration;
      this.note = note;
      this.instrument = instrument;
      this.hitBeat = hitBeat;
   }
   
   public LinkedList<Object> getData(LinkedList<Object> list){
      list.add(duration);
      list.add(note);
      list.add(hitBeat);
      
      return list;
   }
   
   public void setData(LinkedList<Object> data){
      duration = (Byte)(data.remove(0));
      note = (Byte)(data.remove(0));
      hitBeat = (Short)(data.remove(0));
   }
}