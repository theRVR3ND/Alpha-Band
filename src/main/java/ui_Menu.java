/**
 * Kilo - Java Multiplayer Engine | ui_Menu
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu object for Client interface.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class ui_Menu extends JPanel implements MouseListener, MouseMotionListener{
   
   /**
    * Array of all buttons in screen. Initialized in child class.
    */
   protected ui_Button[] buttons;
   
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
   public static ui_Guide guide;
   
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
    * Theme color for client.
    */
   public static Color BLUE, HIGHLIGHT, TEXT, OUTLINE;
   
   /**
    * Default set font for graphics.
    */
   public static final Font defaultFont = new Font(
      "Century Gothic",
      Font.PLAIN,
      util_Utilities.getFontSize()
   );
   
   /**
    * Pre-loading of stuff for splash screen use.
    */
   public static void preload(){
           BLUE = new Color(0, 148, 255);
      HIGHLIGHT = new Color(128, 128, 128, 112);
           TEXT = Color.WHITE;
        OUTLINE = Color.DARK_GRAY;
   }
   
   /**
    * Initializing to be completed while splash screen is showing.
    */
   public static void load(){
      Thread load = new Thread() {
         public void run(){
                    main = new ui_Main();
                 servers = new ui_Servers();
            createServer = new ui_CreateServer();
                   guide = new ui_Guide();
                   setup = new ui_Setup();
                settings = new ui_Settings();
                  player = new ui_Player();
                controls = new ui_Controls();
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
      
      //Draw background
      g2.setColor(BLUE);
      g2.fillRect(0, 0, getWidth(), getHeight());
      
      //Draw buttons
      for(ui_Button b : buttons)
         b.draw(g2);
      
      //Set default(s)
      g2.setColor(TEXT);
      g2.setFont(defaultFont);
      
      //Write program info (so I don't waste another hour changing the wrong project)
      g2.drawString("Kilo Engine | By: Kelvin Peng, '18", 10, getHeight() - 10);
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