/**
 * Alpha Band - Multiplayer Rythym Game | ui_ColorWheel
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Rotatable color wheel for player color selection.
 */

import java.awt.*;

public class ui_ColorWheel{
   
   /**
    * Current player-induced rotation of wheel.
    */
   private short selColor;
   
   /**
    * Rotation value for when being rotated.
    */
   private short startRot, currRot;
   
   /**
    * Position value for this. Based on proportional coordinate system.
    */
   private final float x, y;
   
   /**
    * Diameter of wheel. Based on proportional coordinate system.
    */
   private final float d;
   
   /**
    * Constructor. Initialize values and defaults.
    *
    * @param x                   Scalable top-left x-coordinate
    * @param y                   Scalable top-left y-coordinate
    * @param d                   Scalable diameter of wheel
    */
   public ui_ColorWheel(float x, float y, float d){
      selColor = 0;
      startRot = Short.MAX_VALUE;
      currRot = Short.MAX_VALUE;
      
      this.x = x;
      this.y = y;
      this.d = d;
   }
   
   /**
    * Draw color wheel, rotated based on current rotation.
    *
    * @param g2                  Graphics object to draw into
    */
   public void draw(Graphics2D g2){
      //Draw color wheel
      final byte numSlice = 90;
      for(short theta = 0; theta < 360; theta += (360 / numSlice)){
         g2.setColor(generateColor(theta));
         g2.fillArc(
            getX(), getY(),
            getDiameter(),
            getDiameter(),
            theta + selColor + (currRot - startRot) - 2,
            360 / numSlice + 2
         );
      }
      g2.setColor(ui_Menu.MAIN);
      g2.fillOval(
         getX() + getDiameter() / 4,
         getY() + getDiameter() / 4,
         getDiameter() / 2,
         getDiameter() / 2
      );
   }
   
   /**
    * Return pixel x-coordinate of this' top-left corner.
    */
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return pixel y-coordinate of this' top-left corner.
    */
   public short getY(){
      return (short)(y * cg_Client.SCREEN_HEIGHT);
   }
   
   /**
    * Return pixel diameter of this.
    */
   public short getDiameter(){//Because getD() sounds a bit strange
      return (short)(d * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Return current rotation of this, in degrees.
    */
   public short getRotation(){
      return (short)((selColor + (currRot - startRot)) % 360);
   }
   
   /**
    * Return color wheel's current color (color currently at top of wheel).
    */
   public Color getColor(){
      return generateColor((short)(90 - getRotation()));
   }
   
   /**
    * Set rotation of wheel, in degrees.
    *
    * @param rot                 Rotation, in degrees, to set to.
    */
   public void setRotation(short rot){
      selColor = rot;
   }
   
   /**
    * Modify current rotation state based on mouse click at (pX, pY).
    *
    * @param pX                  Mouse click x-coordinate
    * @param pY                  Mouse click y-coordinate
    * @return                    True if pressed, false if not.
    */
   public boolean checkPress(short pX, short pY){
      //Click on color wheel
      short cX = (short)(getX() + getDiameter() / 2),
            cY = (short)(getY() + getDiameter() / 2);
      short dist = (short)(Math.sqrt(Math.pow(pX - cX, 2) + Math.pow(pY - cY, 2)));
      
      //Avoid / by zero error
      if(pX == cX)
         cX++;
      
      //Check if press is on color wheel (not in donut hole or outside big circle)
      if(dist > getDiameter() / 4 && dist < getDiameter() / 2){
         startRot = (short)(Math.toDegrees(-Math.atan((1.0 * pY - cY) / (pX - cX))));
         if(cX > pX)
            startRot += 180;
         currRot = startRot;
         return true;
      }else
         return false;
   }
   
   /**
    * Stop rotating wheel.
    */
   public void release(){
      selColor += currRot - startRot;
      startRot = Short.MAX_VALUE;
      currRot = Short.MAX_VALUE;
   }
   
   /**
    * Rotates wheel based on mouse position if currently being rotated.
    * 
    * @param dX                  Mouse x-coordinate
    * @param dY                  Mouse y-coordinate
    */
   public void checkDrag(short dX, short dY){
      if(currRot != Short.MAX_VALUE){//Currently being rotated
         short cX = (short)(getX() + getDiameter() / 2),
               cY = (short)(getY() + getDiameter() / 2);
         if(dX == cX)//Avoid / by zero error
            cX++;
         currRot = (short)(Math.toDegrees(-Math.atan((1.0 * dY - cY) / (dX - cX))));
         if(cX > dX)
            currRot += 180;
      }
   }
   
   /**
    * Creates color for wheel based on angle along wheel. Colors
    * generated are gradients from angle value.
    *
    * @param theta               Angle, in degrees, along circle for color.
    */
   public Color generateColor(short theta){
      /*
         Angle:         Color:
            0              red
            120            green
            240            blue
      */
      theta = (short)((theta + 720 * Math.abs(theta / 360)) % 360);
      short[] rgb = new short[] {
         (short)(Math.abs(theta)),
         (short)(Math.abs(theta - 120)),
         (short)(Math.abs(theta - 240))
      };
      for(short i = 0; i < rgb.length; i++){
         if(rgb[i] > 180)
            rgb[i] = (short)(360 - rgb[i]);
         rgb[i] = (short)(255 - rgb[i] / 180.0 * 255);
      }
      return new Color(rgb[0], rgb[1], rgb[2]);
   }
}