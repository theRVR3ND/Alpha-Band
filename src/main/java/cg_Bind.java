/**
 * Kilo - Java Multiplayer Engine | cg_Bind
 * by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * Mouse/key bind for client controls.
 */

import java.awt.event.*;

public class cg_Bind{

   /**
    * Description of action of bind.
    */
   private final String action;
   
   /**
    * Key or mouse code bound to action.
    */
   private short code;
   
   /**
    * Constructor. Initialize variables from String format of bind.
    * 
    * @param line                String to initialze from.
    */
   public cg_Bind(String line){
      String[] cont = line.split("%");
      
      //Read in action and previously set code
      if(cont.length == 1){//No previously set code
         this.action = cont[0];
      }else{
         this.code = Short.parseShort(cont[0]);
         this.action = cont[1];
      }
   }
   
   /**
    * Constructor. Initialize variables from arguments.
    * 
    * @param code                New mouse/key code.
    * @param action              This bind's action description.
    */
   public cg_Bind(short code, String action){
      this.code = code;
      this.action = action;
   }
   
   /**
    * Return action portion of bind.
    * 
    * @return                    Plain text format of action code.
    */
   public String getAction(){
      return action;
   }
   
   /**
    * Return key or mouse code associated with bind.
    */
   public short getCode(){
      return code;
   }
   
   /**
    * Set key or mouse code.
    * 
    * @param code                New key or mouse code.
    */
   public void setCode(short code){
      this.code = code;
   }
   
   /**
    * Return word representation of key or mouse code.
    */
   public String getCodeInText(){
      //Key bind
      if(code > 3){
         return KeyEvent.getKeyText(code);
      
      //Mouse bind
      }else{
         switch(code){
            case(MouseEvent.BUTTON1): return "Left Click";
            
            case(MouseEvent.BUTTON2): return "Middle Click";
            
            case(MouseEvent.BUTTON3): return "Right Click";
            
            default: return "wat...";
         }
      }
   }
   
   /**
    * Return String format of bind for text file.
    */
   @Override
   public String toString(){
      return code + "%" + action;
   }
}