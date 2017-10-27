/**
 * Alpha Band - Multiplayer Rythym Game | cg_Note
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Musical note in game with render capabilities.
 */

import java.awt.Graphics2D;
import java.awt.Color;

public class cg_Note extends bg_Note{
   
   private byte scrollValue;
   
   private final byte duration;
   
   private final byte note;
   
   public cg_Note(final byte duration, final byte note){
      super(duration, note);
      
      this.duration = duration;
      this.note = note;
      
      scrollValue = 0;
   }
   
   public void render(Graphics2D g2){
      g2.setColor(Color.PINK);
   }
   
   public void think(long deltaTime){
      //Check if reaching end of scroll
      if(scrollInd <= Byte.MAX_VALUE)
         scrollInd++;
   }
   
   public byte getScrollValue(){
      return scrollValue;
   }
}