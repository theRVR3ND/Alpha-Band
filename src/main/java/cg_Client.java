/**
 * Alpha Band - Multiplayer Rythym Game | cg_Client
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Client's run class.
 *
 * @author Kelvin Peng
 */

import java.net.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;

public class cg_Client{
   
   /**
    * Frame that holds all content panes.
    */
   public static JFrame frame;
   
   /**
    * Dimensions of screen, needed for proportional rendering.
    */
   public static final short SCREEN_WIDTH = (short)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
                            SCREEN_HEIGHT = (short)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
   
   /**
    * Client run method.
    */
   public static void main(String[] args){
      //Initialize and launch frame
      frame = new JFrame("Alpha Band");
      
      frame.setContentPane(new ui_Splash());
      
      frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
      //frame.setUndecorated(true);
      //frame.setResizable(false);
      frame.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
      frame.setIconImage(util_Utilities.loadImage("menu/icon.png"));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      //Enable OpenGL (dunno if it does anything)
      System.setProperty("sun.java2d.opengl", "True");
      
      frame.setVisible(true);
   }
}