import java.nio.ByteBuffer;

public class Tester{
   
   public static void main(String[] args){
      /**********************************************************
      final byte[] s = new byte[] {-25, 15, 125, -125};
      final byte[] e = new byte[] {107, -5, -125, 125};
      final byte[] d = findDelta(s, e);
      
      byte[] res = new byte[s.length];
      for(byte i = 0; i < s.length; i++){
         res[i] = (byte)(s[i] + d[i]);
      }
      
      for(byte i = 0; i < s.length; i++)
         System.out.println(e[i] + "< " + d[i] + " >" + res[i]);
      //**********************************************************/
      
      /**********************************************************
      for(short i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++){
         byte[] bytes = shortToBytes(i);
         final short con = bytesToShort(bytes, (byte)0);
         if(con != i)
            System.out.println(i + " " + con);
         //else
            //System.out.println("Sky Jesus");
      }
      //**********************************************************/
      
      //System.out.println(Integer.MAX_VALUE / (60000.0 * 60 * 24));
      /**********************************************************
      for(long i = Long.MIN_VALUE; i <= Long.MAX_VALUE; i++){
         byte[] bytes = longToBytes(i);
         final long con = bytesToLong(bytes, (byte)0);
         if(con != i)
            System.out.println(i + " " + con);
         //else
            //System.out.println("Sky Jesus");
      }
      //**********************************************************/
      
      /**********************************************************
      for(float i = Float.MIN_VALUE; i <= Float.MAX_VALUE; i++){
         byte[] bytes = floatToBytes(i);
         final float con = bytesToFloat(bytes, (byte)0);
         if(con != i)
            System.out.println(i + " " + con);
         //else
            //System.out.println("Sky Jesus");
      }
      //**********************************************************/
      
      /**********************************************************
      for(int i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++){
         byte[] bytes = intToBytes(i);
         final int con = bytesToInt(bytes, (byte)0);
         if(con != i)
            System.out.println(i + " " + con);
         //else
            //System.out.println("Sky Jesus");
      }
      //**********************************************************/
   }
   
   public static byte[] longToBytes(long val){
      byte[] ret = new byte[8];
      byte[] p1 = intToBytes((int)(val >> 32 & 0xFF));
      byte[] p2 = intToBytes((int)(val & 0xFF));
      for(byte i = 0; i < ret.length; i++){
         if(i < 4)
            ret[i] = p1[i];
         else
            ret[i] = p2[i - 4];
      }
      return ret;
   }
   
   public static long bytesToLong(byte[] bytes, byte start){
      return (bytesToInt(bytes, start) << 4) | (bytesToInt(bytes, (byte)(start + 4)));
   }
   
   public static byte[] findDelta(byte[] start, byte[] end){
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
   
   public static byte[] shortToBytes(short val){
      return new byte[] {
         (byte)(val >> 8),
         (byte)(val & 0xFF)
      };
   }
   
   public static short bytesToShort(byte[] bytes, byte start){
      return (short)(bytes[start++] << 8 |
                     bytes[start] & 0xFF);
   }
   
   /**
    * Convert float to byte array (4 bytes).
    * 
    * @param val              Float to convert.
    */
   public static byte[] floatToBytes(float val){
      int bits = Float.floatToIntBits(val);
      return new byte[] {
         (byte)(bits >> 24),
         (byte)(bits >> 16),
         (byte)(bits >> 8),
         (byte)(bits & 0xFF)
      };
   }
   
   public static byte[] intToBytes(int val){
      // return ByteBuffer.allocate(4).putInt(val).array();
      return new byte[] {
         (byte)(val >>> 24),
         (byte)(val >>> 16),
         (byte)(val >>> 8),
         (byte)(val & 0xFF)
      };
   }
   
   /**
    * Convert byte array to float. Start using bytes at index start.
    * 
    * @param bytes            Byte array to convert from.
    * @param start            Index in bytes to convert from.
    */
   public static float bytesToFloat(byte[] bytes, byte start){
      int bits = (int)(bytes[start] << 24 |
                       bytes[start + 1] << 16 |
                       bytes[start + 2] << 8 |
                       bytes[start + 3] & 0xFF);
      return Float.intBitsToFloat(bits);
   }
   
   public static int bytesToInt(byte[] bytes, byte start){
      return (int)((0xFF & bytes[start]) << 24 |
                   (0xFF & bytes[start + 1]) << 16 |
                   (0xFF & bytes[start + 2]) << 8 |
                   (0xFF & bytes[start + 3]));
   }
}