/**
 * Alpha Band - Multiplayer Rythym Game | ui_Table
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * A variable row and column scrollable table.
 */

import java.util.*;
import java.awt.*;

public class ui_Table{
   
   /**
    * Scalable position of table.
    */
   private final float x, y;
   
   /**
    * Scalable dimensions of table.
    */
   private final float w, h;
   
   /**
    * List of labels for columns.
    */
   private String[] labels;
   
   /**
    * List of scalable x-coordinates of columns.
    */
   private float[] colCor;
   
   /**
    * Contents of table.
    */
   private ArrayList<String[]> contents;
   
   /**
    * Index of table's scroll.
    */
   private short scrollInd;
   
   /**
    * Index of mouse's hovering row.
    */
   private byte hoverRow;
   
   /**
    * Height of each row in table.
    */
   private static final float rowGap = (float)(1.0 / 20);
   
   /**
    * Constructor. Set table's position, dimensions, and labels.
    * 
    * @param x                      Table's scalable x-coordinate.
    * @param y                      Table's scalable y-coordinate.
    * @param w                      Table's scalable width.
    * @param h                      Table's scalable height.
    * @param labels                 Column labels.
    * @param colCor                 Column scalable x-coordinates.
    */
   public ui_Table(float x, float y, float w, float h, String[] labels, float[] colCor){
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.labels = labels;
      this.colCor = colCor;
      contents = new ArrayList<String[]>();
      scrollInd = 0;
      hoverRow = -1;
   }
   
   /**
    * Draw table and contents.
    *
    * @param g2                     Graphics object to draw into
    */
   //table[].length should equal colCor.length
   public void draw(Graphics2D g2){
      //Don't draw if no contents
      if(contents == null)
         return;
      
      //Draw table outline
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.drawRect(getX(), getY(), getWidth(), getHeight());
      
      /*
      //Draw table contents
      for(byte c = 0; c < labels.length; c++){
         //Draw column label
         if(labels[c] != null)
            g2.drawString(
               labels[c],
               (short)(colCor[c] * cg_Client.SCREEN_WIDTH),
               (short)((y - 0.01) * cg_Client.SCREEN_HEIGHT)
            );
         
         //Draw row contents
         for(byte r = 0; r < h / rowGap && r + scrollInd < contents.size(); r++){
            //Draw highlighted row if needed
            if(r == hoverRow - scrollInd){
               g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
               g2.fillRect(getX() + 1, (short)((y + (hoverRow - scrollInd) * rowGap) * cg_Client.SCREEN_HEIGHT + 1),
                           getWidth() - 1, (short)(rowGap * cg_Client.SCREEN_HEIGHT));
               g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
            }
            
            //Draw text
            if(c < contents.get(r + scrollInd).length && contents.get(r + scrollInd)[c] != null){
               g2.drawString(contents.get(r + scrollInd)[c],
                             (short)(colCor[c] * cg_Client.SCREEN_WIDTH),
                             (short)((y + r * rowGap + 0.035) * cg_Client.SCREEN_HEIGHT));
            }
         }
      }
      */
      for(byte r = -1; r < h / rowGap && r + scrollInd < contents.size(); r++){
         //Highlight row
         if(r == hoverRow - scrollInd && r != -1){
            g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
            g2.fillRect(
               getX() + 1,
               (short)(getY() + r * rowGap * cg_Client.SCREEN_HEIGHT + 1),
               getWidth() - 1,
               (short)(rowGap * cg_Client.SCREEN_HEIGHT)
            );
            g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
         }
         
         //Draw column labels and text
         for(byte c = 0; c < labels.length; c++){
            if(r == -1){
               //Draw column label
               if(labels[c] != null){
                  g2.drawString(
                     labels[c],
                     (short)(colCor[c] * cg_Client.SCREEN_WIDTH),
                     (short)((y - 0.01) * cg_Client.SCREEN_HEIGHT)
                  );
               }
               continue;
            }
            
            //Draw text
            if(c < contents.get(r + scrollInd).length && contents.get(r + scrollInd)[c] != null){
               g2.drawString(contents.get(r + scrollInd)[c],
                             (short)(colCor[c] * cg_Client.SCREEN_WIDTH),
                             (short)((y + r * rowGap + 0.035) * cg_Client.SCREEN_HEIGHT));
            }
            
            /*
            //Highlighted row if needed
            if(r == hoverRow - scrollInd){
               g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
               g2.fillRect(
                  getX() + 1,
                  (short)((y + (hoverRow - scrollInd) * rowGap) * cg_Client.SCREEN_HEIGHT + 1),
                  getWidth() - 1,
                  (short)(rowGap * cg_Client.SCREEN_HEIGHT)
               );
               g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
            }
            
            //Draw text
            if(c < contents.get(r + scrollInd).length && contents.get(r + scrollInd)[c] != null){
               g2.drawString(contents.get(r + scrollInd)[c],
                             (short)(colCor[c] * cg_Client.SCREEN_WIDTH),
                             (short)((y + r * rowGap + 0.035) * cg_Client.SCREEN_HEIGHT));
            }
            */
         }
      }
      
      //Draw scrollbar (all ye who enter, abandon all hope)
      if(!contents.isEmpty()){
         short barHeight = (short)(((h / rowGap) / contents.size()) * h * cg_Client.SCREEN_HEIGHT),
                    barY = (short)(((scrollInd * 1.0 / contents.size()) * h + y) * cg_Client.SCREEN_HEIGHT);
         
         if(barHeight > h * cg_Client.SCREEN_HEIGHT)
            barHeight = (short)(h * cg_Client.SCREEN_HEIGHT);
         
         if(barY < getY())
            barY = getY();
         
         g2.setColor(ui_Theme.getColor(ui_Theme.HIGHLIGHT));
         g2.fillRect((short)((x + w) * cg_Client.SCREEN_WIDTH) + 1, barY,
                     (short)(0.01 * cg_Client.SCREEN_WIDTH), barHeight);
      }
   }
   
   /**
    * Returns pixel x-coordinate of top-left corner.
    */
   public short getX(){
      return (short)(x * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Returns pixel y-coordinate of top-left corner.
    */
   public short getY(){
      return (short)(y * cg_Client.SCREEN_HEIGHT);
   }
   
   /**
    * Returns pixel width of this.
    */
   public short getWidth(){
      return (short)(w * cg_Client.SCREEN_WIDTH);
   }
   
   /**
    * Returns pixel height of this.
    */
   public short getHeight(){
      return (short)(h * cg_Client.SCREEN_HEIGHT);
   }
   
   /**
    * Return table's contents.
    */
   public ArrayList<String[]> getContents(){
      return contents;
   }
   
   /**
    * Returns scroll index of this.
    */
   public short getScrollInd(){
      return scrollInd;
   }
   
   /**
    * Returns hovering index of mouse of this.
    * Returns -1 if no valid hovered row.
    */
   public byte getHoverRow(){
      return hoverRow;
   }
   
   /**
    * Sets table's contents.
    *
    * @param contents               Table's new contents.
    */
   public void setContents(ArrayList<String[]> contents){
      this.contents = contents;
   }
   
   /**
    * Sets table's contents.
    *
    * @param contents               Table's new contents.
    * @param fontMetrics            Used to determine font width on screen.
    */
   public void setContents(String[] contents, FontMetrics fontMetrics){
      /**
       * Implement word wrapping (text will not extend beyond table's area).
       * If you do not understand this code, do not worry. I don't either.
       */
      
      //Clear current contents
      this.contents.clear();
      
      //Maximum width in pixels of each line
      final short lineWidth = (short)(getWidth() - 2 * cg_Client.SCREEN_WIDTH * (colCor[0] - x));
      
      //Traverse each line
      for(short i = 0; i < contents.length; i++){
         //Turn line into words for word wrapping
         String[] words = contents[i].split(" ");
         short ind = 0;
         
         //Add to line until end of words or line cannot fit into textbox
         while(ind < words.length){
            String line = "";
            do{
               line += words[ind++] + " ";
            }while(
               ind < words.length &&
               fontMetrics.stringWidth(line + words[ind]) < lineWidth
            );
            this.contents.add(new String[] {line});
            line = "";
         }
      }
   }
   
   /**
    * Sets table hover index.
    * 
    * @param hoverRow               New hover row.
    */
   public void setHoverRow(byte hoverRow){
      this.hoverRow = hoverRow;
   }
   
   /**
    * Check if mouse is hovering over a row in table.
    * 
    * @param cX                     Mouse x-coordinate.
    * @param cY                     Mouse y-coordinate.
    */
   public void checkHover(short cX, short cY){
      hoverRow = -1;
      if(cX > x * cg_Client.SCREEN_HEIGHT && cX < (x + w) * cg_Client.SCREEN_WIDTH){
         byte currHover = (byte)Math.floor((cY - y * cg_Client.SCREEN_HEIGHT) / (rowGap * cg_Client.SCREEN_HEIGHT));
         if(currHover >= 0 && currHover < (h / rowGap) && Math.min(h / rowGap, contents.size()) > currHover)
            hoverRow = (byte)(currHover + scrollInd);
      }
   }
   
   /**
    * Scroll table based on mouse scroll amount if mouse is in table area.
    * 
    * @param sX                     x-coordinate of mouse.
    * @param sY                     y-coordinate of mouse.
    * @param amount                 Amount mouse has scrolled by.
    */
   public void checkScroll(short sX, short sY, byte amount){
      //Check if mouse within table bounds
      if(sX > getX() && sX < getX() + getWidth() && sY > getY() && sY < getY() + getHeight()){
         //Check if can scroll
         scrollInd += amount;
         
         if(scrollInd < 0)
            scrollInd = 0;
         else if(scrollInd > contents.size() - (h / rowGap))
            scrollInd = (short)(Math.max(contents.size() - (h / rowGap), 0));
      }
   }
}