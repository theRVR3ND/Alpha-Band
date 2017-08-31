/**
 * Kilo - Java Multiplayer Engine | bg_Vector
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Vector math class.
 */

public class bg_Vector{
   
   /**
    * Position in world plane.
    */
   private short x, y;
   
   /**
    * Rotation relative to world.
    */
   private short rot;
   
   /**
    * Constructor.
    */
   public bg_Vector(short x, short y, short rot){
      this.x = x;
      this.y = y;
      this.rot = rot;
   }
   
   /**
    * Get x-component of this.
    */
   public short getX(){
      return x;
   }
   
   /**
    * Get y-component of this.
    */
   public short getY(){
      return y;
   }
   
   /**
    * Get rotation component of this.
    */
   public short getRot(){
      return rot;
   }
   
   /**
    * Set x-component of this.
    */
   public void setX(short x){
      this.x = x;
   }
   
   /**
    * Set y-component of this.
    */
   public void setY(short y){
      this.y = y;
   }
   
   /**
    * Set rotation component of this.
    */
   public void setRot(short rot){
      this.rot = rot;
   }
}