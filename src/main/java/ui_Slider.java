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
    * Text name/label of slider.
    */
   private final String name;
   
   /**
    * Scalable position of slider on screen.
    */
   private final float x, y;
   
   /**
    * Scalable dimension of slider on screen.
    */
   private final float w, h;
   
   /**
    * Range limit on slider value.
    */
   private short min, max;
   
   /**
    * Slider's current value, from 0 to 1.
    */
   private float val;
   
   /**
    * Current "is being dragged by mouse" state.
    */
   private boolean dragging;
   
   /**
    * Constructor. Initialize variables based on arguments.
    * 
    * @param name                Slider's name label.
    * @param x                   Slider's scalable x-coordinate.
    * @param y                   Slider's scalable y-coordinate.
    * @param w                   Slider's scalable width.
    * @param h                   Slider's scalable height.
    * @param min                 Slider's minimum value.
    * @param max                 Slider's maximum value.
    */
   public ui_Slider(String name, float x, float y, float w, float h, short min, short max){
      this.name = name;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.min = min;
      this.max = max;
      this.val = 0.5f;
      dragging = false;
   }
   
   /**
    * Draw slider.
    * 
    * @param g2                  Graphics object to draw in to.
    */
   public void draw(Graphics2D g2){
      //Draw slider value bar
      g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
      g2.fillRect(
         getX() + 1,
         getY() + 1,
         (int)(getWidth() * val),
         getHeight() - 1
      );
      
      //Draw slider bar outline
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.drawRect(
         getX(),
         getY(),
         getWidth(),
         getHeight()
      );
      
      //Draw value
      g2.drawString(
         getValue() + "",
         getX() + getWidth() + (int)(0.01 * cg_Client.SCREEN_WIDTH),
         getY() + getHeight()
      );
      
      //Draw name
      FontMetrics fontMetrics = g2.getFontMetrics();
      short nameWidth = (short)fontMetrics.stringWidth(name);
      g2.drawString(
         name,
         getX() - (short)(nameWidth * 1.1),
         getY() + getHeight() / 2 + fontMetrics.getHeight() / 4
      );
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
      return (short)(min + val * (max - min));
   }
   
   /**
    * Set slider's value.
    * 
    * @param val                 New value to set to.
    */
   public void setValue(short val){
      this.val = (float)(val / (max - min * 1.0));
   }
   
   public void setMinimum(short min){
      this.min = min;
   }
   
   public void setMaximum(short max){
      this.max = max;
   }
   
   /**
    * Check if mouse press is on slider (starting to drag slider).
    * Return true if pressed, false if not.
    * 
    * @param pX                  Mouse press x-coordinate.
    * @param pY                  Mouse press y-coordinate.
    */
   public boolean checkPress(short pX, short pY){
      if(Math.abs(pX - (getX() + val * getWidth())) < 0.1 * getWidth() &&
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
         val = (float)((1.0 * pX - getX()) / getWidth());
         if(val < 0)
            val = 0;
         else if(val > 1)
            val = 1;
      }
   }
   
   /**
    * Release any drag flags (mouse released).
    */
   public void release(){
      dragging = false;
   }
}