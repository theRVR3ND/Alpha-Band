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
   
   private static final short NOTE_WIDTH = (short)(0.6 * cg_Client.SCREEN_WIDTH / 10);
   
   public cg_Note(final byte note, final short beat, final byte duration){
      super(note, beat, duration);
   }
   
   public void render(Graphics2D g2, final float currMilliBeats, final byte keyShift){
      //Figure out dimensions and location of note
      final short drawX = (short)((note - keyShift + 0.5) * cg_Client.SCREEN_WIDTH / 10),
                  drawY = (short)((1 - (beat - currMilliBeats)) * cg_Client.SCREEN_HEIGHT * 3 / 4.0),
             drawHeight = (short)(duration * 25000 / cg_Client.SCREEN_HEIGHT);
      
      //Draw note I guess
      g2.setColor(ui_Theme.getColor(ui_Theme.NOTE_COLOR));
      g2.fillRect(drawX - NOTE_WIDTH / 2, drawY, NOTE_WIDTH, drawHeight);
   }
}