/**
 * Alpha Band - Multiplayer Rythym Game | ui_Slider
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Slider object for changing settings.
 */

import java.awt.*;

public class ui_Slider{
   
   /**
    * Scalable position of slider on screen.
    */
   private final float x, y;
   
   /**
    * Scalable dimension of slider on screen.
    */
   private final float w, h;
   
   /**
    * Text name/label of slider.
    */
   private final String name;
   
   /**
    * Slider's current value, between min and max.
    */
   private short val;
   
   /**
    * Range limit on slider value.
    */
   private final short min, max;
   
   /**
    * Current "is being dragged by mouse" state.
    */
   private boolean dragging;
   
   /**
    * Constructor. Initialize variables based on arguments.
    * 
    * @param x                   Slider's scalable x-coordinate.
    * @param y                   Slider's scalable y-coordinate.
    * @param w                   Slider's scalable width.
    * @param h                   Slider's scalable height.
    * @param name                Slider's name label.
    * @param min                 Slider's minimum value.
    * @param max                 Slider's maximum value.
    */
   public ui_Slider(float x, float y, float w, float h, String name, short min, short max){
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.name = name;
      this.min = min;
      this.max = max;
      this.val = (short)((max - min) / 2);
      dragging = false;
   }
   
   /**
    * Draw slider.
    * 
    * @param g2                  Graphics object to draw in to.
    */
   public void draw(Graphics2D g2){
      //Draw slider bar outline
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.drawRect(getX(), getY(), getWidth(), getHeight());
      
      //Draw slider value bar
      g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
      g2.fillRect(getX() + 1, getY() + 1, (short)((getWidth() - 1) * (1.0 * val / (max - min))), getHeight() - 1);
      
      //Draw value
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.drawString(getValue() + "",
                    getX() + getWidth() + (short)(0.01 * cg_Client.SCREEN_WIDTH),
                    getY() + getHeight());
      
      //Draw name
      FontMetrics fontMetrics = g2.getFontMetrics();
      short nameWidth = (short)fontMetrics.stringWidth(name);
      g2.drawString(name, getX() - (short)(nameWidth * 1.1), getY() + getHeight());
   }
   
   /**
    * Return pixel x-coordinate.
    */
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel y-coordinate.
    */
   public short getY(){
      return (short)(y * cg_Client.SCREEN_HEIGHT);
   }
   
   /**
    * Return pixel width.
    */
   public short getWidth(){
      return (short)(w * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel height.
    */
   public short getHeight(){
      return (short)(h * cg_Client.SCREEN_HEIGHT);
   }
   
   /**
    * Return current slider value.
    */
   public short getValue(){
      return val;
   }
   
   /**
    * Set slider's value.
    * 
    * @param val                 New value to set to.
    */
   public void setValue(short val){
      this.val = val;
   }
   
   /**
    * Check if mouse press is on slider (starting to drag slider).
    * Return true if pressed, false if not.
    * 
    * @param pX                  Mouse press x-coordinate.
    * @param pY                  Mouse press y-coordinate.
    */
   public boolean checkPress(short pX, short pY){
      if(Math.abs(pX - (getX() + (1.0 * val / (max - min)) * getWidth())) < 0.05 * cg_Client.SCREEN_WIDTH &&
         Math.abs(pY - (getY() + 0.5 * getHeight())) < getHeight() / 2){
         dragging = true;
         return true;
      }else
         return false;
   }
   
   /**
    * Drag slider along with mouse drag.
    * 
    * @param pX                  Mouse drag's x-coordinate.
    */
   public void checkDrag(short pX){
      if(dragging){
         val = (short)((max - min) * (1.0 * (pX - getX()) / (getWidth())));
         if(val < min)
            val = min;
         else if(val > max)
            val = max;
      }
   }
   
   /**
    * Release any drag flags (mouse released).
    */
   public void release(){
      dragging = false;
   }
}