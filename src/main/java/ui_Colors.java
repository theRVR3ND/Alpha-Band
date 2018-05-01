/**
 * Alpha Band - Multiplayer Rythym Game | ui_Colors
 * 
 * By: Shae McMillan, Christina Nguyen, and Kelvin Peng
 * W.T.Woodson H.S.
 * 2017 - 18
 * 
 * For regulating program's theme colors.
 */

import java.awt.Color;

public class ui_Colors{
   
   public static final byte BACKGROUND = 0,
                                  TEXT = 1,
                             HIGHLIGHT = 2,
                            NOTE_COLOR = 3;
   
   public static final byte LIGHT = 0,
                             DARK = 1;
   
   public static final String[] themeNames = new String[] {
      "Light", "Dark"
   };
   
   private static final Color[][] themes = new Color[][] {
      {Color.WHITE, Color.DARK_GRAY, new Color(128, 128, 128, 112), Color.BLACK},
      {Color.DARK_GRAY, Color.LIGHT_GRAY, new Color(200, 200, 200, 112), Color.LIGHT_GRAY}
   };
   
   public static byte currTheme = LIGHT;
   
   public static Color getColor(byte color){
      return themes[currTheme][color];
   }
   
   public static String getThemeName(){
      return themeNames[currTheme];
   }
}