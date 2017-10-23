/**
 * Alpha Band - Multiplayer Rythym Game | ui_Player
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for player info viewing and changing.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class ui_Vote extends ui_Menu implements KeyListener, MouseWheelListener, bg_Constants{
   
   private ui_Table voteList;
   
   private byte[] currVotes;
   
   private ui_Table voteList;
   
   /**
    * Constructor. Read previous settings from text file.
    */
   public ui_Vote(){
      buttons = new ui_Button[] {
         new ui_Button("SUBMIT", 0.5f, 0.85f)
      };
      
      voteList = new ui_Table(
<<<<<<< HEAD
         0.3f, 0.1f, 0.4f, 0.4f,
         new String[] {"Song", "Difficulty", "Length"},
         new float[] {0.31f, 0.5f, 0.6f}
      );
      
=======
         0.4f, 0.1f, 0.2f, 0.2f,
         new String[] {"Vote"},
         new float[] {0.41f}
      );
      
      //Add all song options to list
      voteList.getContents().add(new String[] {"Generate a Song"});
      voteList.getContents().add(new String[] {"Random Song"});
      
      String[] settings = util_Utilities.readFromFile("menu/songList.cfg");
      
      
>>>>>>> 1bb0dee9d52a07453c069fd9a15b4e14c45e6638
      //Add key listener for entering player name
      this.setFocusable(true);
      this.addKeyListener(this);
      this.addMouseWheelListener(this);
   }
   
   /**
    * Paint method of panel. Displays all current information.
    * 
    * @param g                   Graphics component to draw into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      voteList.draw(g2);
      
      repaint();
   }
   
   public void startVote(byte[] info){
      //Clear current vote stuff
      voteList.getContents().clear();
      
      //Extract vote info
      byte i = 1;
      for(byte j = 0; j < 3; j++){
         //Check if there is info to extract
         if(info[i + 1] == 0 && info[i + 2] == 0)
            break;
         
         //Extract song info
         String difficulty = info[i] + "";
         String songLength = info[i + 1] + ":" + info[i + 2];
         i += 3;
         
         //Extract song name
         String songName = new String(info, i + 1, info[i]);
         i += info[i] + 1;
         
         //Add to vote list
         voteList.getContents().add(new String[] {songName, difficulty, songLength});
      }
      
      //Add all song options to list
      voteList.getContents().add(new String[] {"Generate a Song", "", ""});
      voteList.getContents().add(new String[] {"Random Song", "", ""});
   }
   
   /**
    * Processes key press event.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){}
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
   
   /**
    * Process mouse click event. Save current info if exiting screen.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Submit vote
      if(buttons[0].isDown()){
         //Do shit here
      }else{
         return;
      }
      
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   /**
    * Process mouse press. Check if dragging panel objects.
    *
    * @param e                   MouseEvent to process.
    */
   public void mousePressed(MouseEvent e){}
   
   /**
    * Process mouse release. Releases any objects in panel being dragged.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseReleased(MouseEvent e){}
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   /**
    * Process mouse drag. Relay effect onto currently dragged objects.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseDragged(MouseEvent e){}
<<<<<<< HEAD
   
   public void mouseWheelMoved(MouseWheelEvent e){
      voteList.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
=======
>>>>>>> 1bb0dee9d52a07453c069fd9a15b4e14c45e6638
}