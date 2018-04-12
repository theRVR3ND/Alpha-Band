/**
 * Alpha Band - Multiplayer Rythym Game | bg_World
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Game world. Where the magic happens.
 */

import java.util.*;
import java.awt.*;

public abstract class bg_World implements bg_Constants{
   
   /**
    * All entities in world.
    */
   protected HashMap<Short, bg_Entity> entities;
   
   /**
    * Time (in milliseconds) of last think call.
    */
   private long lastThinkTime;
   
   protected long songStartTime;
   
   private short currBeat;
   
   protected short bpm;
   
   protected final byte gamemode;
   
   protected final float ALLOWED_ERROR = 0.02f; //For hit scoring. Lower = more stringent.
   
   /**
    * Maximum update rate of world.
    */
   private final byte THINK_RATE = 30;
   
   /**
    * Constructor.
    */
   public bg_World(byte gamemode){
      //Initialize stuff
      entities = new HashMap<Short, bg_Entity>();
      lastThinkTime = System.currentTimeMillis();
      songStartTime = Long.MAX_VALUE;
      
      this.gamemode = gamemode;
      
      //Start think
      Thread thinker = new Thread(){
         public void run(){
            while(true){
               //Time (in milliseconds) since last time think() was called
               byte deltaTime = (byte)(System.currentTimeMillis() - lastThinkTime);
               
               think(deltaTime);
               
               //lastThinkTime now equals current time
               lastThinkTime += deltaTime;
            }
         }
      };
      thinker.start();
   }
   
   /**
    * World's update method. Called once per frame.
    */
   public void think(final byte deltaTime){
      //Limit rate of update
      if(deltaTime < 1000.0 / THINK_RATE){
         try{
            Thread.sleep((int)(1000.0 / THINK_RATE - deltaTime));
         }catch(InterruptedException e){}
      }
      
      //Run think for all entities
      try{
         for(Short key : entities.keySet()){
            entities.get(key).think(deltaTime);
         }
      }catch(ConcurrentModificationException e){}
      
      //Update currBeat count
      if(songStartTime < System.currentTimeMillis()){
         currBeat = (short)((System.currentTimeMillis() - songStartTime) / (60000.0 / bpm));
      }
      System.out.println(this.toString() + " " + currBeat + " " + bpm + " " + songStartTime);
   }
   
   /**
    * Return player that is controlled by client with ID of controller.
    * Does that make sense?
    * 
    * @param controller       Player's controller's ID.
    */
   public bg_Player getPlayer(byte controller){
      //Search through all entities
      try{
         for(Short key : entities.keySet()){
            if(entities.get(key) instanceof bg_Player){
               bg_Player player = (bg_Player)(entities.get(key));
               if(player.getController() == controller)
                  return player;
            }
         }
      }catch(ConcurrentModificationException e){}
      return null;
   }
   
   /**
    * Return number of players in world.
    */
   public byte getNumPlayers(){
      byte ret = -1;
      for(Short key : entities.keySet())
         if(entities.get(key) instanceof bg_Player)
            ret++;
      return ret;
   }
   
   /**
    * Return list of all entities in world.
    */
   public HashMap<Short, bg_Entity> getEntities(){
      return entities;
   }
   
   public long getSongStartTime(){
      return songStartTime;
   }
   
   public short getCurrBeat(){
      return currBeat;
   }
   
   public byte getGamemode(){
      return gamemode;
   }
   
   public void setSongStartTime(long songStartTime){
      this.songStartTime = songStartTime;
   }
   
   //Calculate score for a key press *gap* beats away from actual note start/end
   protected short calculateScore(float gap, byte bonusCombo){
      return (short)(Math.pow(1 - gap, 2) * 50 * (bonusCombo + 1));
   }
   
   /**
    * Return byte array version of data.
    * 
    * @param data             Objects to convert to bytes.
    */
   protected static byte[] dataToBytes(LinkedList<Object> data){
      byte[] bytes = new byte[Byte.MAX_VALUE];
      byte ind = 0;
      
      //Convert numbers into bytes and put into ret
      for(Object o : data){
         byte[] temp = null;
         
         if(o instanceof Byte){
            temp = new byte[] {(Byte)(o)};
         
         }else if(o instanceof Short){
            temp = shortToBytes((Short)o);
         
         }else if(o instanceof Integer){
            temp = intToBytes((Integer)o);
         
         }else if(o instanceof Float){
            temp = floatToBytes((Float)o);
            
         }else if (o instanceof Long){
            temp = longToBytes((Long)o);
            
         }else if(o instanceof String){
            String s = (String)o;
            temp = new byte[MAX_PLAYER_NAME_LENGTH + 4];
            
            byte[] stringBytes = s.getBytes();
            for(byte i = 0; i < stringBytes.length; i++)
               temp[i + 1] = stringBytes[i];
            
            //Add length of string at front
            temp[0] = (byte)(stringBytes.length);
         
         }else if(o instanceof Color){
            Color c = (Color)o;
            temp = new byte[] {
               (byte)(c.getRed()   + Byte.MIN_VALUE),
               (byte)(c.getGreen() + Byte.MIN_VALUE),
               (byte)(c.getBlue()  + Byte.MIN_VALUE)
            };
         
         }else{
            System.out.println("INVALID DATA TYPE");
            System.exit(1);
         }
         
         //Combine temp into ret
         for(byte k = 0; k < temp.length; k++){
            bytes[ind++] = temp[k];
         }
      }
      
      //Trim of excess indices
      byte[] ret = new byte[ind];
      for(byte i = 0; i < ind; i++){
         ret[i] = bytes[i];
      }
      
      return ret;
   }
   
   /**
    * Convert byte array to entity data.
    * 
    * @param bytes            Bytes to convert from.
    * @param template         Pattern to convert bytes in.
    */
   protected static LinkedList<Object> bytesToData(byte[] data, LinkedList<Object> template){
      LinkedList<Object> ret = new LinkedList<Object>();
      
      //Track current data traverse
      byte i = 0;
      
      //Convert bytes in same order as template
      for(Object t : template){
         if(t instanceof Byte){
            ret.add(data[i]);
            i += 1;
         
         }else if(t instanceof Short){
            ret.add(bytesToShort(data, i));
            i += 2;
         
         }else if(t instanceof Integer){
            ret.add(bytesToLong(data, i));
            i += 4;
         
         }else if(t instanceof Float){
            ret.add(bytesToFloat(data, i));
            i += 4;
         
         }else if(t instanceof Long){
            ret.add(bytesToLong(data, i));
            i += 8;
         
         }else if(t instanceof String){
            //Get length of string (encoded in data)
            ret.add(new String(data, i + 1, data[i]));
            i += MAX_PLAYER_NAME_LENGTH + 4;
         
         }else if(t instanceof Color){
            ret.add(new Color(
               data[i++] - Byte.MIN_VALUE,
               data[i++] - Byte.MIN_VALUE,
               data[i++] - Byte.MIN_VALUE
            ));
         
         }else{
            System.out.println("INVALID DATA TYPE");
            System.exit(1);
         }
      }
      
      return ret;
   }
   
   /**
    * Return array of value difference between start and end.
    * Both arrays should be same length.
    * 
    * @param start            Comparison's start state.
    * @param end              Comparison's end state.
    */
   protected static byte[] findDelta(byte[] start, byte[] end){
      byte[] res = new byte[end.length];
      
      for(byte i = 0; i < end.length; i++){
         if(Math.abs(end[i] - start[i]) < Byte.MAX_VALUE)
            res[i] = (byte)(end[i] - start[i]);
         //Deal with byte overflow
         else
            res[i] = (byte)((end[i] - start[i]) - (Byte.MAX_VALUE - Byte.MIN_VALUE) - 1);
      }
      
      return res;
   }
   
   /**
    * Compress comp with Run Length Encoding.
    *
    * @param comp             Array to compress.
    */
   protected static byte[] compress(byte[] comp){
      /*
         If you don't know what the heck that is, read this:
         http://www.javacodex.com/Strings/Run-Length-Encoding
      */
      
      //Compress
      byte[] buff = new byte[comp.length * 2];
      byte length = 0;
      
      for(byte i = 0; i < comp.length; i++){
         byte val = comp[i];
         byte numOccur = 0;
         for(byte k = i; k < comp.length; k++){
            if(comp[k] == val){
               numOccur++;
               i = k;
            }else{
               break;
            }
         }
         buff[length++] = val;
         buff[length++] = numOccur;
      }
      
      byte[] ret = new byte[length];
      for(byte i = 0; i < ret.length; i++)
         ret[i] = buff[i];
      
      return ret;
   }
   
   /**
    * Reverse process of compress. Expand compressed array.
    * 
    * @param exp              Data to expand.
    */
   protected static byte[] expand(byte[] exp){
      LinkedList<Byte> expanded = new LinkedList<Byte>();
      
      /*
         Expand. Example:
                 exp: {3, 2, 4, 5, 1, 2}  <-- MUST be EVEN length
            expanded: {3, 3, 4, 4, 4, 4, 4, 1, 1}
      */
      for(byte i = 0; i < exp.length - 2; i += 2){
         byte val = exp[i];
         for(byte k = 0; k < exp[i + 1]; k++){
            expanded.add(val);
         }
      }
      
      //Put expanded data into array
      byte[] ret = new byte[expanded.size()];
      for(short i = 0; i < ret.length; i++)
         ret[i] = expanded.remove(0);
      
      return ret;
   }
   
   /**
    * Convert short to byte array (2 bytes).
    * 
    * @param val              Short to convert.
    */
   public static byte[] shortToBytes(short val){
      return new byte[] {
         (byte)(val >> 8),
         (byte)(val & 0xFF)
      };
   }
   
   /**
    * Convert float to byte array (4 bytes).
    * 
    * @param val              Float to convert.
    */
   public static byte[] floatToBytes(float val){
      return intToBytes(Float.floatToIntBits(val));
   }
   
   /**
    * Convert int to byte array (4 bytes).
    * 
    * @param val              Float to convert.
    */
   public static byte[] intToBytes(int val){
      return new byte[] {
         (byte)(val >>> 24),
         (byte)(val >>> 16),
         (byte)(val >>> 8),
         (byte)(val & 0xFF)
      };
   }
   
   /**
    * Convert long to byte array (8 bytes).
    * 
    * @param val              Long to convert.
    */
   public static byte[] longToBytes(long val){
      byte[] ret = new byte[8];
      for(byte i = (byte)(ret.length - 1); i >= 0; i--){
         ret[i] = (byte)val;
         val >>>= 8;
      }
      return ret;
   }
   
   /**
    * Convert byte array to short. Start using bytes at index start.
    * 
    * @param bytes            Byte array to convert from.
    * @param start            Index in bytes to convert from.
    */
   public static short bytesToShort(byte[] bytes, byte start){
      return (short)(bytes[start] << 8 |
                     bytes[start + 1] & 0xFF);
      //                              ^ The Anti-Socrates
   }
   
   /**
    * Convert byte array to float. Start using bytes at index start.
    * 
    * @param bytes            Byte array to convert from.
    * @param start            Index in bytes to convert from.
    */
   public static float bytesToFloat(byte[] bytes, byte start){
      return Float.intBitsToFloat(bytesToInt(bytes, start));
   }
   
   /**
    * Convert byte array to int. Start using bytes at index start.
    * 
    * @param bytes            Byte array to convert from.
    * @param start            Index in bytes to convert from.
    */
   public static int bytesToInt(byte[] bytes, byte start){
      return (int)((0xFF & bytes[start]) << 24 |
                   (0xFF & bytes[start + 1]) << 16 |
                   (0xFF & bytes[start + 2]) << 8 |
                   (0xFF & bytes[start + 3]));
   }
   
   /**
    * Convert byte array to long. Start using bytes at index start.
    * 
    * @param bytes            Byte array to convert from.
    * @param start            Index in bytes to convert from.
    */
   public static long bytesToLong(byte[] bytes, byte i){
        return (((long) bytes[i + 7]) & 0xFF) +
              ((((long) bytes[i + 6]) & 0xFF) << 8) +
              ((((long) bytes[i + 5]) & 0xFF) << 16) +
              ((((long) bytes[i + 4]) & 0xFF) << 24) +
              ((((long) bytes[i + 3]) & 0xFF) << 32) +
              ((((long) bytes[i + 2]) & 0xFF) << 40) +
              ((((long) bytes[i + 1]) & 0xFF) << 48) +
              ((((long) bytes[i]) & 0xFF) << 56);
    }
}