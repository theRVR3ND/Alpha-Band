/**
 * Alpha Band - Multiplayer Rythym Game | bg_Note
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Musical note.
 */

import java.util.*;

public class bg_Note{
   
   protected final byte note;
   
   protected final short beat;
   
   protected final byte duration;
   
   public bg_Note(final byte note, final short beat, final byte duration){
      this.note = note;
      this.beat = beat;
      this.duration = duration;
   }
   
   public byte getNote(){
      return note;
   }
   
   public short getBeat(){
      return beat;
   }
   
   public byte getDuration(){
      return duration;
   }
}