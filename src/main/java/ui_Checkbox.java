/**
 * Alpha Band - Multiplayer Rythym Game | ui_Checkbox
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * A box that can be checked.
 */

import java.awt.Graphics2D;

public class ui_Checkbox{
   
   private final float x, y;
   
  // private final String text;
   
   //Checked/unchecked state
   private boolean checked;
   
   public ui_Checkbox(float x, float y){
      this.x = x;
      this.y = y;
      checked = true;
   }
   
   public void draw(Graphics2D g2){
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      
      //Draw box
      g2.drawRect(getX(), getY(), getSize(), getSize());
      
      //Draw mark if checked
      if(checked)
         g2.fillRect(getX() + 3, getY() + 3, getSize() - 5, getSize() - 5);
   }
   
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH);
   }
   
   public short getY(){
      return (short)(y * cg_Client.SCREEN_HEIGHT);
   }
   
   public short getSize(){
      return (short)(20 * cg_Client.SCREEN_WIDTH / 1080.0);
   }
   
   public boolean getChecked(){
      return checked;
   }
   
   public boolean checkClick(short cX, short cY){
      if(getX() < cX && getX() + getSize() > cX && getY() < cY && getY() + getSize() > cY){
         checked = !checked;
         return true;
      }else
         return false;
   }
   
   public void setChecked(boolean checked){
      this.checked = checked;
   }
}