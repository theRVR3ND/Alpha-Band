/**
 * Alpha Band - Multiplayer Rythym Game | ui_Menu
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * Menu object for Client interface.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

public abstract class ui_Menu extends JPanel implements MouseListener, MouseMotionListener{
   
   /**
    * Array of all buttons in screen. Initialized in child class.
    */
   protected ui_Button[] buttons;
   
   private final BufferedImage background = util_Utilities.resize(
      util_Utilities.loadImage("menu/background.png"),
      cg_Client.SCREEN_WIDTH,
      cg_Client.SCREEN_HEIGHT
   );
   
   /**
    * Main menu page.
    */
   public static ui_Main main;
   
   /**
    * Server connection page. Displays available servers.
    */
   public static ui_Servers servers;
   
   /**
    * Server creation interface. Provides options when launching server.
    */
   public static ui_CreateServer createServer;
   
   /**
    * Guide page. Provides game instructions.
    */
   //public static ui_Guide guide;
   
   /**
    * Music creation page.
    */
   public static ui_Studio studio;
   
   /**
    * User setup redirection menu.
    */
   public static ui_Setup setup;
   
   /**
    * Menu for modifying game settings (sound, etc.).
    */
   public static ui_Settings settings;
   
   /**
    * Menu for modifying player settings (player name and theme color).
    */
   public static ui_Player player;
   
   /**
    * Menu for changing game controls.
    */
   public static ui_Controls controls;
   
   /**
    * Song voting menu.
    */
   public static ui_Vote vote;
   
   /**
    * Game pause menu.
    */
   public static ui_Pause pause;
   
   /**
    * Default set font for graphics.
    */
   public static final Font defaultFont = new Font(
      "Century Gothic",
      Font.PLAIN,
      util_Utilities.getFontSize()
   );
   
   /**
    * Initializing to be completed while splash screen is showing.
    */
   public static void load(){
      Thread load = new Thread() {
         public void run(){
            settings     = new ui_Settings();
            main         = new ui_Main();
            servers      = new ui_Servers();
            createServer = new ui_CreateServer();
            //guide        = new ui_Guide();
            studio       = new ui_Studio();
            setup        = new ui_Setup();
            player       = new ui_Player();
            controls     = new ui_Controls();
            vote         = new ui_Vote();
            pause        = new ui_Pause();
            
            cg_MIDI.loadChannels();
         }
      };
      load.start();
   }
   
   /**
    * Constructor.
    */
   public ui_Menu(){
      addMouseListener(this);
      addMouseMotionListener(this);
   }
   
   /**
    * Paint method for menus. Fills background and draws buttons.
    *
    * @param g                Graphics object to draw into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw background theme color
      g2.setColor(ui_Theme.getColor(ui_Theme.BACKGROUND));
      g2.fillRect(0, 0, cg_Client.SCREEN_WIDTH, cg_Client.SCREEN_HEIGHT);
      
      //Draw background
      g2.drawImage(background, 0, 0, null);
      
      //Write program info (so I don't waste another hour changing the wrong project)
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.setFont(new Font(
         "Century Gothic",
         Font.PLAIN,
         (int)(util_Utilities.getFontSize() * 0.75)
      ));
      g2.drawString(
         "Alpha Band | By: Shae McMillan, Christina Nguyen, Sammy Collins, and Kelvin Peng, '18",
         10,
         getHeight() - 10
      );
      
      //Set default(s)
      g2.setFont(defaultFont);
      
      //Draw buttons
      for(ui_Button b : buttons)
         b.draw(g2);
   }
   
   /**
    * Process mouse click event, checking effect on buttons.
    *
    * @param e                MouseEvent to process
    */
   public void mouseClicked(MouseEvent e){
      //Check click on all buttons
      for(ui_Button b : buttons){
         if(b.checkClick((short)e.getX(), (short)e.getY())){
            //Release all other buttons if one clicked
            for(ui_Button r : buttons){
               if(b != r)
                  r.setDown(false);
            }
            break;
         }
      }
      
      //Un-expand buttons
      for(ui_Button b : buttons)
         b.setExpanded(false);
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   public void mousePressed(MouseEvent e){}
   
   public void mouseReleased(MouseEvent e){}
   
   /**
    * Process mouse movement, executing effect on buttons.
    *
    * @param e                MouseEvent from movement
    */
   public void mouseMoved(MouseEvent e){
      for(ui_Button b : buttons){
         b.checkHover((short)e.getX(), (short)e.getY());
      }
   }
   
   public void mouseDragged(MouseEvent e){}
}