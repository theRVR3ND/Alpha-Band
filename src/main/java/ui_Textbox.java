/**
 * Alpha Band - Multiplayer Rythym Game | ui_Textbox
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Clickable and hoverable button for menu interface.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Textbox{
   
   /**
    * Textbox's contents;
    */
   private char[] contents;
   
   /**
    * Scalable coordinate position of textbox's top-left corner.
    */
   private final float x, y;
   
   /**
    * Scalable dimension of textbox's top-left corner.
    */
   private final float w, h;
   
   /**
    * Index in contents array that is being modified.
    */
   private byte index;
   
   /**
    * Textbox's activity state. If selected, key presses will register effect.
    */
   private boolean selected;
   
   /**
    * Constructor.
    * 
    * @param x                   Textbox's scalable x-position.
    * @param y                   Textbox's scalable y-position.
    * @param w                   Textbox's scalable width.
    * @param h                   Textbox's scalable height.
    * @param maxChar             Maximum number of characters in textbox.
    */
   public ui_Textbox(float x, float y, float w, float h, byte maxChar){
      contents = new char[maxChar];
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      index = 0;
      selected = false;
   }
   
   /**
    * Draw textbox and its contents.
    * 
    * @param g2                  Graphics object to draw into.
    */
   public void draw(Graphics2D g2){
      //Draw actual box
      g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
      g2.fillRect(getX(), getY(), getWidth(), getHeight());
      
      //Draw contents
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      FontMetrics fontMetrics = g2.getFontMetrics();
      final byte spacing = (byte)(getHeight() - fontMetrics.getHeight());
      g2.drawString(
         getContents(),
         getX() + spacing,
         getY() + getHeight() - spacing
      );
      
      //Draw outline
      if(selected){
         g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
         g2.drawRect(
            getX() - spacing / 2,
            getY() - spacing / 2,
            getWidth() + spacing,
            getHeight() + spacing
         );
      }
   }
   
   /**
    * Return pixel x-position of textbox in panel.
    */
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel y-position of textbox in panel.
    */
   public short getY(){
      return (short)(y * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel width of textbox in panel.
    */
   public short getWidth(){
      return (short)(w * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel height of textbox in panel.
    */
   public short getHeight(){
      return (short)(h * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return current contents of textbox in string format.
    */
   public String getContents(){
      return new String(contents, 0, index);
   }
   
   /**
    * Return selection state of textbox.
    */
   public boolean isSelected(){
      return selected;
   }
   
   /**
    * Check if mouse click is on textbox.
    * 
    * @param cX                        Mouse click's x-coordinate
    * @param cY                        Mouse click's y-coordinate
    * @return                          Textbox's new selection state.
    */
   public boolean checkClick(short cX, short cY){
      if(cX > getX() && cX < getX() + getWidth() &&
         cY > getY() && cY < getY() + getHeight()){
         selected = !selected;
      }else{
         selected = false;
      }
      return selected;
   }
   
   /**
    * Processes key press event. Edit contents if needed.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){
      if(selected){
         //Delete letters
         if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
            if(index > 0)
               contents[--index] = '\u0000';
         
         //Add letters
         }else if(Character.isAlphabetic(e.getKeyChar())){
            if(index < contents.length){
               contents[index++] = e.getKeyChar();
            }
         }
      }
   }
   
   /**
    * Set contents of textbox.
    * 
    * @param contents            New contents.
    */
   public void setContents(String contents){
      setContents(contents.toCharArray());
   }
   
   /**
    * Set contents of textbox.
    * 
    * @param contents            New contents.
    */
   public void setContents(char[] contents){
      for(byte i = 0; i < this.contents.length && i < contents.length; i++){
         this.contents[i] = contents[i];
         index = (byte)(i + 1);
      }
   }
}