/**
 * Kilo - Java Multiplayer Engine | ui_Theme
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Theme color handling.
 */

import java.awt.Color;

public class ui_Theme{
   
   /**
    * Current game theme (also index in color and name arrays).
    */
   public static byte currTheme;
   
   /**
    * Themez.
    */
   public static final byte CLASSIC = 0,
                                RED = 1,
                             ORANGE = 2,
                             YELLOW = 3,
                              GREEN = 4,
                               BLUE = 5,
                             PURPLE = 6,
                              LIGHT = 7,
                               DARK = 8,
                         NUM_THEMES = 9;
   
   /**
    * Color components of each theme.
    */
   public static final byte BACKGROUND = 0,
                                  TEXT = 1,
                             HIGHLIGHT = 2,
                            NOTE_COLOR = 3;
   
   /**
    * Theme names. Parallel to THEMES.
    */
   public static final String[] NAMES = new String[] {
<<<<<<< HEAD
      "Classic",
      "Red",
      "Orange",
      "Yellow",
      "Green",
      "Blue",
      "Purple",
      "White",
      "Dark"
=======
      "Classic", "Red", "Orange", "Yellow", "Green", "Blue", "Purple", "White", "Dark"
>>>>>>> 1bb0dee9d52a07453c069fd9a15b4e14c45e6638
   };
   
   /**
    * Theme colors. Row is for theme value, column is for theme component.
    */
   private static final Color[][] THEMES = new Color[][] {
      {new Color(0, 148, 255),   Color.WHITE,      new Color(128, 128, 128, 150), Color.WHITE},
      {Color.RED,                Color.YELLOW,     new Color(100, 100, 100, 200), Color.WHITE},
      {new Color(255, 106, 0),   Color.WHITE,      new Color(100, 100, 100, 200), Color.WHITE},
      {new Color(216, 197, 101), Color.DARK_GRAY,  new Color(100, 100, 100, 130), Color.WHITE},
      {new Color(50, 114, 22),   Color.WHITE,      new Color(100, 100, 100, 130), Color.WHITE},
      {new Color(43, 50, 91),    Color.WHITE,      new Color(200, 200, 200, 130), Color.WHITE},
      {new Color(87, 0, 127),    Color.WHITE,      new Color(200, 200, 200, 130), Color.WHITE},
      {Color.WHITE,              Color.DARK_GRAY,  new Color(128, 128, 128, 112), Color.BLACK},
      {Color.DARK_GRAY,          Color.LIGHT_GRAY, new Color(200, 200, 200, 112), Color.WHITE}
   };
   
   /**
    * Return desired component of current theme color.
    * 
    * @param component                 Component color of theme to return.
    */
   public static Color getColor(byte component){
      return THEMES[currTheme][component];
   }
   
   /**
    * Return current theme's name.
    */
   public static String getThemeName(){
      return NAMES[currTheme];
   }
}