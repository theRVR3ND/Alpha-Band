/**
 * Alpha Band - Multiplayer Rythym Game | bg_Note
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
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