/**
 * Alpha Band - Multiplayer Rythym Game | ui_Vote
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Menu panel for player info viewing and changing.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class ui_Vote extends ui_Menu implements KeyListener, MouseWheelListener, bg_Constants{
   
   private ui_Table voteList;
   
   private byte[] currVotes;
   
   /**
    * Constructor. Read previous settings from text file.
    */
   public ui_Vote(){
      buttons = new ui_Button[] {
         new ui_Button("SUBMIT", 0.5f, 0.75f)
      };
      
      voteList = new ui_Table(
         0.2f, 0.1f, 0.6f, 0.3f,
         new String[] {"Song", "Difficulty", "Length"},
         new float[] {0.21f, 0.6f, 0.7f}
      );
      
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
      
      //Show list of vote options
      if(voteList.getContents().size() > 0)
         voteList.draw(g2);
      
      //Show vote timeout
      short seconds = (short)((cg_Panel.gamePanel.getWorld().getSongStartTime() - System.currentTimeMillis()) / 1000.0);
      String toDraw;
      if(seconds > 0){
         if(seconds % 60 < 10)
            toDraw = (seconds / 60) + ":0" + (seconds % 60);
         else
            toDraw = (seconds / 60) + ":" + (seconds % 60);
      }else
         toDraw = "";
      
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.drawString(
         "Game starts in: " + toDraw,
         voteList.getX(),
         voteList.getY() + voteList.getHeight() + util_Utilities.getFontSize() + 10
      );
      
      repaint();
   }
   
   public void startVote(byte[] info){
      //Clear current vote stuff
      voteList.getContents().clear();
      
      //Extract vote info
      byte i = 9;
      for(byte j = 0; j < 3; j++){
         //Check if there is info to extract
         if(info[i + 1] == 0 && info[i + 2] == 0){
            break;
         }
         
         //Extract song info
         String difficulty = info[i] + "";
         String songLength;
         if(info[i + 2] < 10)
            songLength = info[i + 1] + ":0" + info[i + 2];
         else
            songLength = info[i + 1] + ":" + info[i + 2];
         i += 3;
         
         //Extract song name
         String songName = new String(info, i + 1, info[i]);
         i += info[i] + 1;
         
         //Add to vote list
         voteList.getContents().add(new String[] {songName, difficulty, songLength});
      }
      
      //Get vote timout
      cg_Panel.gamePanel.getWorld().setSongStartTime(bg_World.bytesToLong(info, (byte)1));
      
      //Add all song options to list
      voteList.getContents().add(new String[] {"Generate a Song"});
      voteList.getContents().add(new String[] {"Choose Random Song"});
      
      //Redirect to this screen if needed
      if(cg_Client.frame.getContentPane() != ui_Menu.vote){
         cg_Client.frame.setContentPane(ui_Menu.vote);
         requestFocus();
         cg_Client.frame.revalidate();
      }
   }
   
   /**
    * Processes key press event.
    *
    * @param e                   KeyEvent to process
    */
   public void keyPressed(KeyEvent e){
      //Escape to menu
      if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
         cg_Client.frame.setContentPane(ui_Menu.pause);
         ui_Menu.pause.requestFocus();
         cg_Client.frame.revalidate();
      }
   }
   
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
         //Send vote back to server
         if(voteList.getHoverRow() >= 0){
            cg_Panel.getConnection().writeOut(
               new byte[] {
                  VOTE,
                  voteList.getHoverRow()
               }
            );
            
            //Direct to game panel
            cg_Client.frame.setContentPane(cg_Panel.gamePanel);
            cg_Panel.gamePanel.requestFocus();
            cg_Client.frame.revalidate();
         }
      }else{
         voteList.checkHover((short)e.getX(), (short)e.getY());
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
   
   public void mouseWheelMoved(MouseWheelEvent e){
      voteList.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
}