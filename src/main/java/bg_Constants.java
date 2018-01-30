/**
 * Alpha Band - Multiplayer Rythym Game | bg_Constants
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 * 
 * List of constant values for client and server. Only
 * accessible through implementation.
 */

public interface bg_Constants{
   
   /**
    * Port number to use for connections.
    */
   public static final int SERVER_PORT = 54024;
   
   /**
    * Port number to use for server requests.
    */
   public static final int ECHO_PORT = 54025;
   
   /**
    * Standard format request from client for connection.
    */
   public static final String REQUEST_MESSAGE = "RESPECT_WOMEN";
   
   /**
    * Maximum number of clients allowed in a server.
    * Limiting is needed to not crash or something.
    */
   public static final byte MAX_PLAYERS = 6;
   
   /**
    * Stream tag. Identifies type of communication
    * being sent.
    */
   public static final byte INITIALIZE = 0,
                                ACTION = 1,
                               MESSAGE = 2,
                                UPDATE = 3,
                                  VOTE = 4,
                                 NOTES = 5;
   
   /**
    * Longest player name allowed.
    */
   public static final byte MAX_PLAYER_NAME_LENGTH = 24;
   
   /**
    * Action trigger value.
    */
   /*
      This should match Binds.cfg exactly.
      First value must equal 0 and following
      values must be sequential.
   */
   public static final byte CHAT = 0,
                            TEST = 1;
   
   /**
    * Visible (to client) dimensions. Any entity within
    * range should be visible.
    */
   public static final short VIEW_WIDTH = 480,
                            VIEW_HEIGHT = 270;
   
   /**
    * Entity type.
    */
   public static final byte PLAYER = 0;
   
   /**
    * Game modes.
    */
   public static final String[] gamemodes = new String[]{
      "Competition",
      "Collaborative"
   };
}