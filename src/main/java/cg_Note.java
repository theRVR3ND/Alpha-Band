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

public class cg_Note extends bg_Note{
   
   public cg_Note(final byte duration, final char value){
      super(duration, value);
   }
   
   public void render(Graphics2D g2){
      g2.setColor(cg_World.NOTE_COLOR);
   }
}