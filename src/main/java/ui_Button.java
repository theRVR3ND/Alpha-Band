/**
 * Alpha Band - Multiplayer Rythym Game | ui_Button
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Clickable and hoverable button for menu interface.
 */

import java.awt.image.*;
import java.awt.*;

public class ui_Button{
   
   /**
    * Button's text contents.
    */
   private final String text;
   
   /**
    * Scalable coordinate of button's top-left corner.
    */
   private final float x, y;
   
   /**
    * Button's clicked up/down state.
    */
   private boolean down;
   
   /**
    * Button's mouse size state (mouse hovering or not).
    */
   private boolean expanded;
   
   private static final short WIDTH = 244,
                             HEIGHT = 94;
   
   /**
    * Constructor. Sets image and coordinates of button.
    * 
    * @param img                       Button's image.
    * @param x                         Button's scalable x-coordiate.
    * @param y                         Button's scalable y-coordiate.
    */
   public ui_Button(String text, float x, float y){
      this.text = text;
      this.x = x;
      this.y = y;
      down = false;
      expanded = false;
   }
   
   /**
    * Draw button.
    * 
    * @param g2                        Graphics2D object to draw on to.
    */
   public void draw(Graphics2D g2){
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      
      //Draw text in center of box
      short textWidth = (short)(g2.getFontMetrics().stringWidth(text)),
            textHeight = (short)(g2.getFontMetrics().getHeight());
      g2.drawString(
         text,
         getX() + getWidth() / 2 - textWidth / 2,
         getY() + getHeight() / 2 + textHeight / 4
      );
      
      //Draw as different size based on mouse hover state
      if(!expanded){
         //Smaller (shrink by 5%) if up
         g2.drawRect(
            getX() + (short)(getWidth() * 0.025),
            getY() + (short)(getHeight() * 0.025),
            (short)(getWidth() * 0.95),
            (short)(getHeight() * 0.95)
         );
         
      //Normal sized if down
      }else{
         g2.drawRect(getX(), getY(), getWidth(), getHeight());
      }
      
   }
   
   /**
    * Return button's x-coordinate.
    * 
    * @return                          Pixel-wise x-coordinate of button's top left corner.
    */
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH - WIDTH * 0.5);
   }
   
   /**
    * Return button's y-coordinate.
    * 
    * @return                          Pixel-wise y-coordinate of button's top left corner.
    */
   public short getY(){
      return (short)(y * cg_Client.SCREEN_HEIGHT - HEIGHT * 0.5);
   }
   
   /**
    * Return button's width.
    * 
    * @return                          Pixel-wise width of button.
    */
   public static short getWidth(){
      return (short)(getHeight() * (1.0 * WIDTH / HEIGHT));
   }
   
   /**
    * Return button's height.
    * 
    * @return                          Pixel-wise height of button.
    */
   public static short getHeight(){
      return (short)(HEIGHT * cg_Client.SCREEN_HEIGHT / 1080.0);
   }
   
   /**
    * Return button's current up/down state.
    */
   public boolean isDown(){
      return down;
   }
   
   /**
    * Return button's current size state.
    */
   public boolean isExpanded(){
      return expanded;
   }
   
   /**
    * Checks if mouse click is on button.
    * 
    * @param cX                        Mouse click's x-coordinate
    * @param cY                        Mouse click's y-coordinate
    * @return                          Button's new up/down state.
    */
   public boolean checkClick(short cX, short cY){
      if(cX > getX() && cX < getX() + getWidth() &&
         cY > getY() && cY < getY() + getHeight()){
         down = true;
      }else{
         down = false;
      }
      return down;
   }
   
   /**
    * Checks if mouse has moved over button (hovering).
    * 
    * @param hX                        Mouse's current x-coordiante
    * @param hY                        Mouse's current y-coordiante
    * @return                          Button's new size state.
    */
   public boolean checkHover(short hX, short hY){
      if(hX > getX() && hX < getX() + getWidth() &&
         hY > getY() && hY < getY() + getHeight()){
         expanded = true;
      }else{
         expanded = false;
      }
      return expanded;
   }
   
   /**
    * Set button's current up/down state.
    * 
    * @param down                      Button's new up/down state.
    */
   public void setDown(boolean down){
      this.down = down;
   }
   
   /**
    * Set button's current size state.
    * 
    * @param expanded                  New size state.
    */
   public void setExpanded(boolean expanded){
      this.expanded = expanded;
   }
}