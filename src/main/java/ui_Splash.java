/**
 * Kilo - Java Multiplayer Engine | ui_Splash
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Splash screen for looking cool.
 */

import javax.swing.*;
import java.awt.image.*;
import java.awt.*;

public class ui_Splash extends JPanel{
   
   /**
    * Images/logos that appear on splash screen.
    */
   private BufferedImage[] splashImages;
   
   /**
    * Index in splashImages screen is currently displaying.
    */
   private byte splashInd;
   
   /**
    * Count variable tracking number of frames elapsed.
    */
   private short timeCount;
   
   /**
    * Last time (in milliseconds) paintComponent() was called.
    */
   private short lastUpdateTime;
   
   /**
    * Number of frame updates for each splash screen.
    */
   private final short SPLASH_LENGTH = 150;
   
   /**
    * Maximum number of times paintComponent() should
    * be called per second.
    */
   private final byte MAX_UPDATE_RATE = 30;
   
   /**
    * Constructor. Load splash image and set variables.
    */
   public ui_Splash(){
      //Add any splash images you want in here (up to 127 images)
      splashImages = new BufferedImage[] {
         util_Utilities.loadImage("menu/Icon.png"),
      };
      
      splashInd = 0;
      timeCount = 0;
      lastUpdateTime = 0;
      
      //While splash is showing, load menus.
      ui_Menu.load();
   }
   
   /**
    * Paint method of panel. Draw splash image(s).
    * 
    * @param g                      Graphics object to draw in to.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Move on to next splash image or redirect to menu.
      if(timeCount >= SPLASH_LENGTH){
         //Move to next splash
         if(splashInd < splashImages.length - 1){
            splashInd++;
            timeCount = 0;
         
         //Redirect to menu
         }else{
            cg_Client.frame.setContentPane(ui_Menu.main);
            cg_Client.frame.revalidate();
            return;
         }
      }
      
      //Draw splash background
      g2.setColor(ui_Menu.BLUE);
      g2.fillRect(0, 0, getWidth(), getHeight());
      
      //Draw splash screen
      short cX = (short)(0.5 * getWidth() - splashImages[splashInd].getWidth() / 2),
            cY = (short)(0.5 * getHeight() - splashImages[splashInd].getHeight() / 2);
      g2.drawImage(splashImages[splashInd], cX, cY, null);
      
      //Add fade-in effect
      byte fadeValue = 0;
      
      if(timeCount < 51){//Fade in
         fadeValue = (byte)(51 - timeCount);
      
      }else if(timeCount > SPLASH_LENGTH - 51){//Fade out
         fadeValue = (byte)(51 - (SPLASH_LENGTH - timeCount));
      }
      
      if(fadeValue != 0){
         g2.setColor(new Color(0, 0, 0, fadeValue * 5));
         g2.fillRect(0, 0, getWidth(), getHeight());
      }
      timeCount++;
      
      //Limit update rate
      try{
         byte waitTime = (byte)((lastUpdateTime + 1000 / MAX_UPDATE_RATE) - System.currentTimeMillis() % Byte.MAX_VALUE);
         if(waitTime > 0)
            Thread.sleep(waitTime);
         lastUpdateTime = (short)(System.currentTimeMillis() % Byte.MAX_VALUE);
      }catch(InterruptedException e){}
      
      repaint();
   }
}