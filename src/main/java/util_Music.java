/**
 * Alpha Band - Multiplayer Rythym Game | ui_Player
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Menu panel for player info viewing and changing.
 */

import java.util.*;

public class util_Music{
   
   //********STATIC CONSTANTS********//
   
   private static final byte MAJOR = 0,
                             MINOR = 1,
                             BLUES = 2,
                          HARMONIC = 3,            //Harmoic minor
                        NUM_SCALES = 4;
   
   private static final byte[] KEYS = new byte[] {0, 1, 2, 3, 4, 5, 6};
   
   private static final byte[] INSTRUMENTS = new byte[] {1, 28, 36, 35, 31, 56};
   
   //Run
   public static byte[][] generateSong(byte difficulty, short seed){
      //Make a random number genrator
      Random rand = new Random(seed);
      
      //Generate song parameters
      final short bpm = (short)((difficulty / 100.0) * rand.nextDouble() * 170 + 60);
      final short songLength = (short)((rand.nextDouble() * 2.5 + 0.5) * bpm);
      final byte scale = (byte)(rand.nextDouble() * NUM_SCALES);
      final byte key = KEYS[(int)(rand.nextDouble() * KEYS.length)];
      
      //Generate song data array
      byte[][] gen = new byte[INSTRUMENTS.length][songLength];
      
      //Generate piano part
      for(short i = 0; i < gen.length; i++){
         
      }
   }
   
}