/**
 * Alpha Band - Multiplayer Rythym Game | util_Utilities
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Static methods for commonly used methods client-side.
 */

import java.net.URL;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;

public class util_Utilities{
   
   /**
    * Return scaled font size for on-screen text.
    */
   public static byte getFontSize(double ratio){
      //Returns size 30 for a 1920 x 1080 screen
      return (byte)(getFontSize() * ratio);
   }
   
   /**
    * Return scaled font size for on-screen text.
    */
   public static byte getFontSize(){
      //Returns size 30 for a 1920 x 1080 screen
      return (byte)(30 * cg_Client.SCREEN_WIDTH / 1920.0);
   }
   
   /**
    * Improve rendering quality of graphics.
    * 
    * @param g                Graphics object to git gud.
    */
   public static Graphics2D improveQuality(Graphics g){
      Graphics2D g2 = (Graphics2D)g;
      RenderingHints rh = new RenderingHints(null);
      
      //Add the highest quality values. Gud gud.
      rh.put(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
      rh.put(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      rh.put(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
      rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      
      g2.setRenderingHints(rh);
      return g2;
   }
   
   /**
    * Return file directory leading to "main" folder that contains this.
    */
   public static String getDirectory(){
      URL classLoc = util_Utilities.class.getResource("util_Utilities.class");
      String directory = (String)(classLoc.getPath());
      
      //Go up file tree
      directory = directory.substring(0, directory.lastIndexOf("main") + 4);
      
      return directory.replaceAll("%20", " ");
   }
   
   /**
    * Returns image file loaded from "Client->Resorces" folder.
    *
    * @param fileName         Name of file to load.
    */
   public static BufferedImage loadImage(String fileName){
      String directory = getDirectory() + "/resources/" + fileName;
      try{
         return ImageIO.read(new File(directory));
      }catch(IOException e){
         System.out.println(directory);
         e.printStackTrace();
         System.exit(1);
      }
      return null;
   }
   
   /**
    * Returns contents of text file loaded from "Client->Resources" folder.
    *
    * @param fileName         Name of file to load.
    */
   public static String[] readFromFile(String fileName){
      try{
         LinkedList<String> cont = new LinkedList<String>();
         Scanner input = new Scanner(
            new File(util_Utilities.getDirectory() + "/resources/" + fileName)
         );
         
         //Read into list
         while(input.hasNextLine()){
            String nextLine = input.nextLine();
            if(nextLine.equals("//BREAK//"))
               break;
            cont.add(nextLine);
         }
         
         //Turn into array and send
         return cont.toArray(new String[cont.size()]);
      
      }catch(FileNotFoundException e){
         System.out.println("Fatal error: " + fileName + " not found.");
         e.printStackTrace();
         System.exit(1);
      }
      return null;
   }
   
   /**
    * Writes string array into text file in "Client->Resources" folder.
    *
    * @param lines            Contents to write to file.
    * @param fileName         Name of file to write into.
    */
   public static void writeToFile(String[] lines, String fileName){
      try{
         PrintWriter out = new PrintWriter(
            new File(util_Utilities.getDirectory() + "/Resources/" + fileName)
         );
         for(String line : lines){
            out.println(line);
         }
         out.flush();
         out.close();
      }catch(IOException e){}
   }
}