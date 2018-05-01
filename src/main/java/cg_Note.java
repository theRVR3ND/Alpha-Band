/**
 * Alpha Band - Multiplayer Rythym Game | cg_Note
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Client's version of musical note.
 */

public class cg_Note extends bg_Note{
   
   private boolean isHit; //not iShit, but isHit
   
   public cg_Note(final byte note, final short beat, final byte duration){
      super(note, beat, duration);
      isHit = false;
   }
   
   public boolean getIsHit(){
      return isHit;
   }
   
   public void setIsHit(boolean isHit){
      this.isHit = isHit;
   }
}