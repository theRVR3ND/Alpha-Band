public class ChannelTime
{
   private int channel;       //MIDI channel to play on
   private int startTime;     //frame # note starts at
   private int endTime;       //frame # note ends at
    
   public ChannelTime(int c, int s, int e)
   {
      channel = c;
      startTime = s;
      endTime = e;
   }
   
   public ChannelTime(int c)
   {
      channel = c;
      startTime = -1;
      endTime = -1;
   }

   
   public void free()         //mark channel available to use
   {
      startTime = -1;
      endTime = -1;
   }
   
   public boolean isFree()
   {
      return (startTime == -1 && endTime == -1);
   }
   
   public int getChannel()
   {
      return channel;
   }
   
   public int getStartTime()
   {
      return startTime;
   }

   public int getEndTime()
   {
      return endTime;
   }

   public void setChannel(int c)
   {
      channel = c;
   }

   public void setStartTime(int s)
   {
      startTime = s;
   }

   public void setEndTime(int e)
   {
      endTime = e;
   }
}