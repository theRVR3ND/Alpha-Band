/**
 * Alpha Band - Multiplayer Rythym Game | cg_Note
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Graphical musical note.
 */

import java.awt.*;

public class cg_Note extends bg_Note{
   
   private static final short NOTE_WIDTH = (short)(0.6 * cg_Client.SCREEN_WIDTH / 48);
   
   public cg_Note(final byte note, final short beat, final byte duration){
      super(note, beat, duration);
   }
   
   public void render(Graphics2D g2, final float currMilliBeats){
      //Figure out dimensions and location of note
      final short drawX = (short)(0.2 * cg_Client.SCREEN_WIDTH + note * NOTE_WIDTH),
                  drawY = (short)((hitBeat - currBeat) / 2.0),
             drawHeight = (short)(100);
      
      //Draw note I guess
      g2.setColor(ui_Theme.getColor(ui_Theme.NOTE_COLOR));
      g2.fillRect(drawX, drawY, NOTE_WIDTH, drawHeight);
   }
}