/**
 * Alpha Band - Multiplayer Rythym Game | ui_Settings
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for changing game settings.
 */

import java.awt.*;
import java.awt.event.*;

public class ui_Settings extends ui_Menu implements MouseWheelListener{
   
   /**
    * Array of slider objects for modifying settings.
    */
   private ui_Slider[] sliders;
   
   private ui_Table themeList;
   
   /**
    * Constructor. Read previouse settings from text file.
    */
   public ui_Settings(){
      buttons = new ui_Button[] {
         new ui_Button("BACK",  0.5f, 0.85f)
      };
      
      sliders = new ui_Slider[] {
         new ui_Slider( //Sound slider
            "Volume:",
            0.4f, 0.45f,
            0.2f, 0.02f,
            (short)0, (short)100
         ),
      };
      
      themeList = new ui_Table(
         0.4f, 0.1f, 0.2f, 0.2f,
         new String[] {"Themes"},
         new float[] {0.41f}
      );
      
      //Add all themes to theme list
      for(String s : ui_Theme.NAMES){
         themeList.getContents().add(new String[] {s});
      }
      themeList.setHoverRow((byte)0);
      
      //Load settings from file
      String[] settings = util_Utilities.readFromFile("menu/settings.cfg");
      ui_Theme.currTheme = Byte.parseByte(settings[0]);
      
      if(ui_Theme.currTheme < 0 || ui_Theme.currTheme >= ui_Theme.NUM_THEMES)
         ui_Theme.currTheme = ui_Theme.CLASSIC;
      
      themeList.setHoverRow(ui_Theme.currTheme);
      for(byte i = 1; i < settings.length; i++){
         sliders[i - 1].setValue(Byte.parseByte(settings[i]));
      }
      
      //Add mouse wheel listener
      addMouseWheelListener(this);
   }
   
   /**
    * Draws panel contents.
    *
    * @param g                   Graphics object to draw into.
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //Draw sliders
      for(byte i = 0; i < sliders.length; i++){
         sliders[i].draw(g2);
      }
      
      //Draw theme list
      themeList.draw(g2);
      
      repaint();
   }
   
   public short getVolume(){
      return sliders[0].getValue();
   }
   
   /**
    * Process mouse click event. Write settings to file if
    * exiting menu.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Redirect to other menus
      if(buttons[0].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.setup);
         //Write settings to file
         String[] settings = new String[sliders.length + 1];
         settings[0] = themeList.getHoverRow() + "";
         for(byte i = 0; i < sliders.length; i++){
            settings[i + 1] = sliders[i].getValue() + "";
         }
         util_Utilities.writeToFile(settings, "menu/settings.cfg");
      
      }else{
         //Check if color theme has been changed
         byte currHover = themeList.getHoverRow();
         themeList.checkHover((short)e.getX(), (short)e.getY());
         if(themeList.getHoverRow() < 0){
            themeList.setHoverRow(currHover);
         }else{
            ui_Theme.currTheme = (byte)(themeList.getHoverRow());
         }
         
         return;
      }
      
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   /**
    * Process mouse press event. Check if slider is pressed.
    *
    * @param e                   MouseEvent to process.
    */
   public void mousePressed(MouseEvent e){
      for(ui_Slider s : sliders)
         s.checkPress((short)e.getX(), (short)e.getY());
   }
   
   /**
    * Process mouse release event. Un-drag all sliders.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseReleased(MouseEvent e){
      for(ui_Slider s : sliders)
         s.release();
   }
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   /**
    * Process mouse drag event. Pass on coordinates to
    * sliders for slider movement.
    *
    * @param e                   MouseEvent to process.
    */
   public void mouseDragged(MouseEvent e){
      for(ui_Slider s : sliders)
         s.checkDrag((short)e.getX());
   }
   
   public void mouseWheelMoved(MouseWheelEvent e){
      themeList.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
}