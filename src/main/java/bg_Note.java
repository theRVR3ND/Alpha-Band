/**
 * Alpha Band - Multiplayer Rythym Game | bg_Note
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Instrument note for player to hit.
 */

public class bg_Note{
   
   /**
    * Number of updates this has experienced.
    */
   private byte count;
   
   /**
    * Number of updates this should go through
    * before disappearing.
    */
   private final byte duration;
   
   public bg_Note(byte duration){
      count = 0;
      this.duration = duration;
   }
   
   public void update(){
   
   }
}