//Rev Dr. Douglas R Oberle, Washington DC, 2015
//TO DO: a long chord and a short note in the same column clips the chord in playback (but not MIDI)
//       calculate processor speed to adjust timer DELAY so real-time playback matches speed when written to MIDI file
//       harmonize, or countermelody option that takes last composed melody line (V) and creates a harmony or countermelody for it  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.sound.midi.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class MusicSketchpadPanel extends JPanel implements MouseListener, MouseMotionListener
{   
   private static int INSTRUMENT = 0;			      //the melody instrument the user picks (piano is 0, 0<=INSTRUMENT<128)
   public static int WHOLENOTE_TIME=48;				//number of frames for a whole note
   public static final int TEMPO = 96;				   //resolution of a whole note for MIDI syncing -> 24*4

   public static final double EIGHTHNOTE = 1/8.0;
   public static final double QUARTERNOTE = 1/4.0;
   public static final double HALFNOTE = 1/2.0;
   public static final double WHOLENOTE = 1.0;


   private static final int OCTAVE = 12;				//add this to a note to go up an octave, subtract to go down
   private static final int VOLUME=100;				//the VELOCITY of the note (how "hard" the note is struck) 0<=VELOCITY<128

   private static int DELAY=20;	                  //#miliseconds delay between each time the screen refreshes for the timer
                                                   //used to adjust the in-game playback time to the MIDI writing time
                                                   
   private static MidiChannel[] channels=null;		//MIDI channels
   private static ChannelTime [] channelTimes = null;    //keeps track of start and end times for notes on each channel
   private static Instrument[] instr;					//MIDI instrument bank

   private static Map <Integer, Scale> allScales;
   private static ArrayList<Integer> commonScaleIndex;         
   //index of common scales from allScales Map

   private static int scaleChoice;                 //the index of the chosen scale from allScales        
   private static Scale scale;	                  //the scale or mode the user picks
   private static int chordIndex;                  //index of chord intervals to pick from
   private static ArrayList<Chord>[] commonChords; //common chords for the user to paint with
   private static ArrayList<Chord>[] allChords;    //all chords for the user to paint with
   private static boolean useAllChords;            //show just common chords and scales or all chords and scales?
   private static boolean hardFitNotes;            //if we are shifting notes to fit with any chords there, true means that it will
                                                   //shift the note to an element of a chord in the same column.
                                                   //false means that it will shift the note to any common chord that could be randomly
                                                   //chosen from all notes in the same column.

   private static Note [][] chart;                 //chart of note selections for melody the user creates (0-no note)
   private static Note [][] undo;                  //used for an undo feature when the chart is altered
   private static int numRows, numCols;            //user picked dimensions of the chart
   
   private static boolean [] highlight;            //show which notes are being played by highlighting a cell in the bottom bar
   private static boolean playChart;               //toggle music chart play on or off
   private static int chartIndex;                  //index of which note is currently being played in the Listener
   private static int frame;                       //keep track of # frames for timing of notes in Listener
   
   private static int highLightRow;                //which row should be highlighted if using number keys to freeplay
   private static int highLightTime;               //used to turn on and off row highlight when in freeplay
   
   private static int realTimeNote;                //pitch of note played in freeplay or while painting a note in the chart
   private static Chord realTimeChord;             //pitch of chord played while painting a chord in the chart

   private static int key;                          //the key the user picks
    
   protected static int mouseX;			            //locations for the mouse pointer
   protected static int mouseY;
   private static int mouseRow;			            //row in chart where mouse is
   private static int mouseCol;			            //col in chart where mouse is
   private static boolean shiftIsPressed;

   private static boolean textInput;               //are we getting text input for a file name to read in or save to?
   private static String message;                  //message to show user or echo out text input for file names
   private static int messageTime;                 //time to show message to user
   private static final int MESSAGE_DELAY = 100;   //number of frames messages stay on the screen
   private static String fileName;                 //name of file to read in to or write out to
   private static boolean writeToFile;             //toggle mode to write to txt file
   private static boolean readFromFile;            //toggle mode to read from txt file
   
   private Timer t;							            //used to set the speed of the enemy that moves around the screen

   public MusicSketchpadPanel()                     //constructor
   {
      addMouseListener( this );
      addMouseMotionListener( this );
      mouseX = 0;
      mouseY = 0;
      shiftIsPressed = false;
      chart = new Note[32][64];
      undo = new Note[32][64];
      numRows = 16;
      numCols = 16;
      hardFitNotes = true;
      
      highlight = new boolean[numCols];
      playChart = false;
      chartIndex = 0;
      frame = 1;
      
      highLightRow = -1;
      highLightTime = -1;
      realTimeNote = -1;
      realTimeChord = null;
   
      textInput = false;
      message = "";
      messageTime = -1;
      fileName = "";
      writeToFile = false;
      readFromFile = false;
    
      mouseRow = numRows/2;							//start mouse position in the middle
      mouseCol = numCols/2;	
   
      key = 60;
      try 
      {
         Synthesizer synth = MidiSystem.getSynthesizer();
         synth.open();
         channels = synth.getChannels();
         instr = synth.getDefaultSoundbank().getInstruments();
      }
      catch (Exception ignored) 
      {}
      channelTimes = new ChannelTime[channels.length];
      for(int i=0; i<channels.length; i++)
      {
         channels[i].programChange(instr[INSTRUMENT].getPatch().getProgram());
         channelTimes[i] = new ChannelTime(i);
      }
      allScales = buildScaleMap(key);
      
      scaleChoice = 1;
      scale = allScales.get(scaleChoice);
      useAllChords = false;
      chordIndex = 0;
      commonChords = scale.getChordSetsA();
      allChords = scale.getChordSets();
      
      t = new Timer(DELAY, new Listener());				
      t.start();
   }

	//THIS METHOD IS ONLY CALLED THE MOMENT A KEY IS HIT - NOT AT ANY OTHER TIME
	//pre:   k is a valid keyCode
	//post:  processes user input - (sent from the driver)
   public void processUserInput(int k, boolean shift)
   {
      if(k==KeyEvent.VK_ESCAPE)					//End the program	
         System.exit(1);
      if(shift)
         shiftIsPressed = true;
      else
         shiftIsPressed = false; 
      
      if(textInput)                          //entering file name to read in or write out to
      {
         if((k>=KeyEvent.VK_A && k<=KeyEvent.VK_Z) || (k>=KeyEvent.VK_0 && k<=KeyEvent.VK_9) || k==KeyEvent.VK_BACK_SPACE)
         {
            if(k==KeyEvent.VK_BACK_SPACE && message.length() >= 1)
            {
               message = message.substring(0, message.length()-1);
               if(message.length() == 0)
               {
                  readFromFile = false;
                  writeToFile = false;
                  textInput = false;                 
                  message = "";
               }
            }
            else
            {
               String current = "";
               if(k>=KeyEvent.VK_A && k<=KeyEvent.VK_Z)
                  current = "" + (char)('A' + (k - KeyEvent.VK_A));
               else
               {
                  if(k == KeyEvent.VK_0)
                     current = "0";
                  else
                     current = "" + (k - KeyEvent.VK_0);
               }
               if(current.length() > 0 && message.length() < 16)
                  message += current;
            }
         }
         else
            if(k==KeyEvent.VK_ENTER)                   
            {
               if(message.length() > 0)
               {
                  fileName = message;
                  if(readFromFile == true)
                  {
                     updateUndo();
                     loadSongFromFile();
                  }
                  else
                     if(writeToFile == true)
                     {
                        writeSongToFile();
                     }
               }
               readFromFile = false;
               writeToFile = false;
               textInput = false;                 //we finished entering file name
               repaint();			
               return;
            }
      
         repaint();			     
         return;  
      }    
      if(k==KeyEvent.VK_L)                      //load in a txt file
      {
         textInput = true;
         readFromFile = true;
         writeToFile = false;
         message = "";
         messageTime = -1;
         repaint();			     
         return;  
      }    
      if(k==KeyEvent.VK_W)                      //write out to a txt file
      {
         textInput = true;
         readFromFile = false;
         writeToFile = true;
         messageTime = -1;
         message = "";
         repaint();			     
         return;  
      }    
   
      if((k>=KeyEvent.VK_1 && k<=KeyEvent.VK_9) || k==KeyEvent.VK_0)//play a note
      {
         int index = k - KeyEvent.VK_0 - 1;
         if(k==KeyEvent.VK_0)
            index = 9;
         index += (scale.getNumNotes() - 1);   
         if(index >= 0 && index < scale.getNotes().length)
         {
            
            highLightRow = chart.length - index - 1;
            highLightTime = frame;
            int note = scale.getNotes()[index];
            if(shiftIsPressed)                 //make the key sharp
               note++; 
            realTimeNote = note;
         }
         repaint();			     
         return;  
      }   
      highLightRow = -1;
      highLightTime = -1;
      if(k==KeyEvent.VK_T)                      //toggle resolution               
      {
         updateUndo();
         if(numRows==16)                        //low res to high res
         {
            numRows = 32;
            numCols *= 2;
            if(numCols > 64)
               numCols = 64;
            highlight = new boolean[numCols];
         }
         else                                   //high res to low res
         {
            numRows = 16;
            numCols /= 2;
            if(numCols < 1)
               numCols = 1;
            highlight = new boolean[numCols];
         }
         repaint();			     
         return;  
      }
      if(k>=KeyEvent.VK_A && k<=KeyEvent.VK_G)  //change the key                       
      {          
         int oldKey = key;                               
         if(k==KeyEvent.VK_C)
            key = 60;
         else if(k==KeyEvent.VK_D)
            key = 62;
         else if(k==KeyEvent.VK_E)
            key = 64;
         else if(k==KeyEvent.VK_F)
            key = 65;
         else if(k==KeyEvent.VK_G)
            key = 67;
         else if(k==KeyEvent.VK_A)
            key = 69;
         else if(k==KeyEvent.VK_B)
            key = 71;
         if(shiftIsPressed)                     //make the key sharp
            key++;   
         if(oldKey != key)
         {
            allScales = buildScaleMap(key);
            scale = allScales.get(scaleChoice);
            commonChords = scale.getChordSetsA();
            allChords = scale.getChordSets();
         } 
         rebuildChart(scale.getNumNotes(), scale.getNumNotes());
         repaint();			     
         return;  
      }
      if(k==KeyEvent.VK_COMMA)                  //change the scale
      {
         int oldNumNotes = scale.getNumNotes();
         scaleChoice--;
         if(scaleChoice == 0 || !allScales.containsKey(scaleChoice)) //0 is a key of a null scale, so wrap-around
            scaleChoice = allScales.keySet().size() - 1;
         if(useAllChords==false && !commonScaleIndex.contains(scaleChoice))   //cycle left until we find a common scale
         {   
            boolean found = false;
            for(int i=scaleChoice; i >= 1; i--)
            {
               if(commonScaleIndex.contains(i))
               {
                  scaleChoice = i;
                  found = true;
                  break;
               }
            }
            if(found == false)
               scaleChoice = commonScaleIndex.get(commonScaleIndex.size()-1);
         }  
         scale = allScales.get(scaleChoice);
         chordIndex = 0;
         commonChords = scale.getChordSetsA();
         allChords = scale.getChordSets();
         int newNumNotes = scale.getNumNotes();
         rebuildChart(oldNumNotes, newNumNotes);
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_PERIOD)                  //change the scale
      {
         int oldNumNotes = scale.getNumNotes();
         scaleChoice++;
         if(!allScales.containsKey(scaleChoice))
            scaleChoice = 1;
         if(useAllChords==false && !commonScaleIndex.contains(scaleChoice))   //cycle right until we find a common scale
         {   
            boolean found = false;
            for(int i=scaleChoice; i < allScales.keySet().size(); i++)
            {
               if(commonScaleIndex.contains(i))
               {
                  scaleChoice = i;
                  found = true;
                  break;
               }
            }
            if(found == false)
               scaleChoice = 1;
         }  
      
         scale = allScales.get(scaleChoice);
         chordIndex = 0;
         commonChords = scale.getChordSetsA();
         allChords = scale.getChordSets();
         int newNumNotes = scale.getNumNotes();
         rebuildChart(oldNumNotes, newNumNotes);
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_UP)               //increase note length or DELAY
      {  
         if(shiftIsPressed)
         {
            if(DELAY <= 195)
            {
               DELAY+=5;
               t.setDelay(DELAY);
            }
         }
         else
         {
            if(WHOLENOTE_TIME < 1000)
               WHOLENOTE_TIME+=4;
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_DOWN)             //decrease note length or DELAY
      {
         if(shiftIsPressed)
         {
            if(DELAY >= 5)
            {
               DELAY-=5;
               t.setDelay(DELAY);
            }
         }
         else
         {
            if(WHOLENOTE_TIME > 40)
               WHOLENOTE_TIME-=4;
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_RIGHT)            //increase instrument bank
      {
         if(INSTRUMENT < 127)
         {
            INSTRUMENT++;
            channels[0].programChange(instr[INSTRUMENT].getPatch().getProgram());
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_LEFT)             //decrease instrument bank
      {
         if(INSTRUMENT > 0)
         {
            INSTRUMENT--;
            channels[0].programChange(instr[INSTRUMENT].getPatch().getProgram());
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_V)                //make random song
      {
         updateUndo();
         randomSong(shiftIsPressed);
         repaint();			
         return;
      }  
      if(k==KeyEvent.VK_P)                //play selected scale
      {
         if(playChart)                    //turn off chart notes being played
         {
            playChart = false;
            for(int i=1; i<highlight.length; i++)
               highlight[i] = false; 
            chartIndex = 0;   
         }
         playNow(trimScale(scale.getNotes()));
         repaint();			
         return;
      }  
      if(k==KeyEvent.VK_R)                //reset the chart
      {
         updateUndo();
         if(playChart)       
         {
            playChart = false;
            for(int i=1; i<highlight.length; i++)
               highlight[i] = false; 
            chartIndex = 0;   
         }
         channels[0].allNotesOff(); 		
         
         if(shiftIsPressed)               //just remove chords
         {
            for(int r=0; r<chart.length; r++)
               for(int c=0; c<chart[0].length; c++)
                  if(chart[r][c]!=null && chart[r][c].getChord()!=null)
                     chart[r][c] = null;
         }
         else
            chart = new Note[chart.length][chart[0].length];
         repaint();			
         return;
      }  
      if(k==KeyEvent.VK_S)                //silence notes being played
      {
         if(playChart)       
         {
            playChart = false;
            for(int i=1; i<highlight.length; i++)
               highlight[i] = false; 
            chartIndex = 0;   
         }
         channels[0].allNotesOff(); 		
      }
      if(k==KeyEvent.VK_SPACE)            //play notes in chart
      {
         playChart = !playChart;
         if(playChart == false)
         {
            channels[0].allNotesOff(); 	
            chartIndex = 0; 
            for(int i=1; i<highlight.length; i++)
               highlight[i] = false;
         }
         else
            chartIndex = chartToNotes().length - 1;
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_PAGE_UP)
      {                                   //increase durration of current note
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null)
         {
            chart[mouseRow][mouseCol].setDurration(chart[mouseRow][mouseCol].getDurration() + 0.125);
         } 
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_PAGE_DOWN)
      {                                   //decrease durration of current note
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null)
         {
            chart[mouseRow][mouseCol].setDurration(chart[mouseRow][mouseCol].getDurration() - 0.125);
            if(chart[mouseRow][mouseCol].getDurration() < 0.125)
               chart[mouseRow][mouseCol] = null;
         } 
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_I)
      {                                   //inversion on current chord
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null  && chart[mouseRow][mouseCol].getChord()!=null)
         {
            chart[mouseRow][mouseCol].inversion();
            if(chart[mouseRow][mouseCol].getChord()!=null)
               realTimeChord = chart[mouseRow][mouseCol].getChord();   
         } 
         repaint();			
         return;
      }
   
      if(k==KeyEvent.VK_ADD || k==KeyEvent.VK_PLUS || k==KeyEvent.VK_EQUALS)
      {                                   //make current note sharp
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null  && chart[mouseRow][mouseCol].getChord()==null)
         {
            chart[mouseRow][mouseCol].sharp();
            realTimeNote = chart[mouseRow][mouseCol].getNote();
         } 
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_SUBTRACT || k==KeyEvent.VK_MINUS || k==KeyEvent.VK_UNDERSCORE)
      {                                   //make current note flat
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null  && chart[mouseRow][mouseCol].getChord()==null)
         {
            chart[mouseRow][mouseCol].flat();
            realTimeNote = chart[mouseRow][mouseCol].getNote();
         } 
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_O)                //raise or lower note/chord an octave
      {
         updateUndo();
         if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < numCols && chart[mouseRow][mouseCol]!=null && chart[mouseRow][mouseCol].getNote()> 0)
         {
            if(shiftIsPressed)
               chart[mouseRow][mouseCol].octaveDown();
            else
               chart[mouseRow][mouseCol].octaveUp();
            if(chart[mouseRow][mouseCol].getChord()!=null)
               realTimeChord = chart[mouseRow][mouseCol].getChord();   
            else
               realTimeNote = chart[mouseRow][mouseCol].getNote();
         } 
         repaint();			
         return;
      }
   
      if(k==KeyEvent.VK_BRACELEFT || k==KeyEvent.VK_OPEN_BRACKET) //decrease number of chart columns
      {
         updateUndo();
         if(numCols > 1)
         {
            numCols--;
            highlight = new boolean[numCols];
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_BRACERIGHT || k==KeyEvent.VK_CLOSE_BRACKET) //increase number of chart columns
      {
         updateUndo();
         if((numRows==16 && numCols < 32) || (numRows==32 && numCols < 64))
         {
            numCols++;
            highlight = new boolean[numCols];
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_X)                         //toggle complex chords
      {
         useAllChords = !useAllChords;  
         chordIndex = 0;       
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_N)                         //chord scroll
      {
         int index = chart.length - 1 - mouseRow;
         chordIndex--;
         if(useAllChords==false)
         {
            if(chordIndex < 0)
               chordIndex = commonChords[index%commonChords.length].size()-1;
         }
         else
         {
            if(chordIndex < 0)
               chordIndex = allChords[index%allChords.length].size()-1;
         }
      
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_M)                         //chord scroll
      {
         int index = chart.length - 1 - mouseRow;
         chordIndex++;
         if(useAllChords==false)
         {
            if(chordIndex >= commonChords[index%commonChords.length].size())
               chordIndex = 0;
         }
         else
         {
            if(chordIndex >= allChords[index%allChords.length].size())
               chordIndex = 0;
         }
      
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_F1)                       //reverse chart columns/rows
      {
         updateUndo();
         if(shiftIsPressed)
            flip();
         else
            reverse();
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_F2)                       //fit melody to chords/ chords to melody
      {
         updateUndo();
         if(shiftIsPressed)
         {
            if(fitChordToMelody()==false)
            {
               message = "no chord fits there";
               messageTime = frame + MESSAGE_DELAY;
            }
         }
         else	
            fitMelodyToChords();
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_H)                       //hard fit melody to chords toggle
      {
         hardFitNotes = !hardFitNotes;
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_F3)                       //scale octave up/down
      {
         updateUndo();
         if(shiftIsPressed)
         {
            if(scale.getNotes()[0] - OCTAVE >= 22)
            {
               scale.octaveDown();
               rebuildChart(scale.getNumNotes(), scale.getNumNotes());
            }
            else
            {
               message = "octave too low";
               messageTime = frame + MESSAGE_DELAY;
            }
         }
         else		
         {
            if(scale.getNotes()[scale.getNotes().length-1] + OCTAVE <= 108)
            {
               scale.octaveUp();
               rebuildChart(scale.getNumNotes(), scale.getNumNotes());
            }
            else
            {
               message = "octave too high";
               messageTime = frame + MESSAGE_DELAY;
            }
         }
         repaint();			
         return;
      }
      if(k==KeyEvent.VK_F4)                       //chart notes octave up/down
      {
         updateUndo();
         if(shiftIsPressed)
         {
            chartOctaveDown();
         }
         else		
         {
            chartOctaveUp();
         }
         repaint();			
         return;
      }    
      if(k==KeyEvent.VK_F5)                       //chart notes step up/down
      {
         updateUndo();
         if(shiftIsPressed)
         {
            chartStepDown();
         }
         else		
         {
            chartStepUp();
         }
         repaint();			
         return;
      }         
      /* TO DO:  
      if(k==KeyEvent.VK_F6)                       //add harmony notes to melody
      {
         updateUndo();
         if(shiftIsPressed)
         {
            addHarmony(true);                      //add harmony with structure that tries to follow the main melody
         }
         else		
         {
            addHarmony(false);                     //pick random directions to move, (ascend, descend, flatline, mixed)
         }
         repaint();			
         return;
      }       
       */
      if(k==KeyEvent.VK_BACK_SPACE)                   //UNDO last change to chart
      {
         restoreToUndo();
         repaint();			
         return;
      }
   
      repaint();			
   }

   //post: draws contents to the panel
   public void paintComponent(Graphics g)
   {
      super.paintComponent(g); 
      final int TEXT_SIZE=20;	         //size of text being drawn
      int SIZE=40;	                  //size of units being drawn
      int rowShift = 0;
      if(numRows==32)
         SIZE=20;
         
      g.setColor(Color.blue.darker().darker());		//draw a blue background
      if(SIZE == 40)                               //low res
         g.fillRect(0, 0, ((numCols+1)*SIZE), ((numRows+1)*SIZE));
      else
         g.fillRect(0, 0, ((numCols+2)*SIZE), ((numRows+2)*SIZE));
   
      int x = 0;
      int y = 0;
      if(numRows==16)
         g.setFont(new Font("Monospaced", Font.PLAIN, (int)(SIZE/3.5)));
      else
         g.setFont(new Font("Monospaced", Font.PLAIN, SIZE/2));
      
      ArrayList<Integer> dissonantCols = dissonantColumns();   //collection of columns with dissonant notes in them (to mark a different color)
   
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      for(int r = startRow; r <= endRow; r++)        //note chart - block highlighted to show which note is selected
      {
         x = 0;
         for(int c = 0; c < numCols; c++)
         {
            int noteThere = scale.getNotes()[chart.length - 1 - r]; //the note that could be at that position
            if(chart[r][c] != null)
            {
               int difference = chart[r][c].getMod();               //show if note is flat or sharp
            
               Color temp = Color.yellow.darker();
               if( chart[r][c].getChord() != null)
                  temp = Color.magenta.darker();
               if(difference < 0)
                  temp = temp.darker();
               else if(difference > 0)
                  temp = temp.brighter();
               g.setColor(temp);	
               
               int numBlocks = (int)(chart[r][c].getDurration() * 8);
               g.fillRect(x, y, (SIZE/2)*numBlocks, SIZE/10);
               
               if( chart[r][c].getChord() != null)	
                  g.fillRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
               else
                  g.fillOval(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
            
               if(chart[r][c].getChord() != null)
               {
                  g.setColor(Color.black);	
                  String name = chart[r][c].getChord().getName().substring(1);   //trim the chord name to not include the key
                  if(name.charAt(0)=='#' && name.length() > 1)                   //remove the sharp if chord key is sharp
                     name = name.substring(1);
                  if(name.length() > 5 && SIZE==40)                              //only 5 characters will fit in the block in row res
                     name = name.substring(0,5);
                  else  if(name.length() > 3 && SIZE==20)                        //only 3 characters will fit in the block in high res
                     name = name.substring(0,3);
               
                  int inversion = chart[r][c].getInversionType();   
                  g.drawString(name, x+(SIZE/8), y+(SIZE/2)+(SIZE/8));
                  if(inversion != 0 && SIZE==40)      //only show chord inversion if there is one and we are low res
                  {
                     g.setColor(Color.black);	
                     g.drawString(""+inversion, x+(SIZE/2)+(SIZE/3), y+(SIZE/3));
                  }
                  if(difference != 0)
                  {
                     g.setColor(Color.black);
                     if(SIZE==40)                     //low res	
                     {
                        if(difference > 0)
                           g.drawString(""+difference, x+(SIZE/2)+(SIZE/10), y+(SIZE/2)+(SIZE/2));
                        else
                           g.drawString(""+difference, x+(SIZE/2), y+(SIZE/2)+(SIZE/2));
                     }
                     else
                     {
                        if(difference > 0)
                           g.drawString(""+difference, x+(SIZE/4), y+(SIZE/2)+(SIZE/2));
                        else
                           g.drawString(""+difference, x+(SIZE/10), y+(SIZE/2)+(SIZE/2));
                     }
                  }
               }
               else
                  if(difference != 0)
                  {
                     g.setColor(Color.black);
                     if(SIZE == 20)                   //high res
                     {
                        int xPos = x + (SIZE/10);
                        if(difference > 0)
                           xPos = x + (SIZE/4); 
                        g.drawString(""+difference, xPos, y+(SIZE/2)+(SIZE/4));
                     }
                     else
                     {
                        int rightShift = (SIZE/3);
                        if(difference > 0)            //shift to the right less if the difference doesn't have a negative sign
                           rightShift = (SIZE/3)+(SIZE/10);	   
                        g.drawString(""+difference, x+rightShift, y+(SIZE/2)+(SIZE/8));
                     }
                  }
            }
            else
            {
               if(r == highLightRow)
               {
                  if(shiftIsPressed)
                     g.setColor(Color.red.darker().darker());
                  else
                     g.setColor(Color.green.darker().darker());
               }	
               else  //no note, chord or highlight in this cell
               {
                  Chord chordInCol = null;
                  for(int row=0; row<chart.length; row++)   //see if there is a chord in that col to highlight chord notes
                     if(chart[row][c] != null && chart[row][c].getChord() != null)
                        chordInCol = chart[row][c].getChord();
                  if(chordInCol != null)
                  {
                     int [] chordNotes = chordInCol.getNormalizedNotes();
                     boolean found = false;
                     for(int i=0; i<chordNotes.length; i++)
                     {
                        if(chordNotes[i] == normalize(noteThere))
                        {
                           found = true;
                           break;
                        }
                     }
                     if(found)
                        g.setColor(Color.magenta.darker().darker().darker());	
                     else
                     {
                        if(dissonantCols.contains(c))
                           g.setColor(Color.red.darker().darker().darker());	
                        else
                           g.setColor(Color.gray.darker().darker().darker());	
                     }
                  }
                  else
                  {
                     if(dissonantCols.contains(c))
                        g.setColor(Color.red.darker().darker().darker());	
                     else
                        g.setColor(Color.gray.darker().darker().darker());		
                  }
               }
               g.fillRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
            }
         
            g.setColor(Color.black);		
            g.drawRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
            x+=SIZE;
         }
         y+=SIZE;
      }
      x = 0;
      for(int r = 0; r < highlight.length; r++)     //time bar - block highlighted to show which column is being played
      {
         if(highlight[r] == true)
            g.setColor(Color.white);		
         else
            g.setColor(Color.black);
         if(SIZE==40)	                           //low res	
            g.fillRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
         else
            g.fillRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), (SIZE*2)-(SIZE/10));
         x+=SIZE;
      }
      y=0;
   
      for(int r = startRow; r <= endRow; r++)        //draw column of note names to the right of the chart
      {
         g.setColor(Color.black);
         if(SIZE==40)	                           //low res	
            g.fillRect(x+(SIZE/10), y+(SIZE/10), SIZE-(SIZE/10), SIZE-(SIZE/10));
         else
            g.fillRect(x+(SIZE/10), y+(SIZE/10), (SIZE*2)-(SIZE/10), SIZE-(SIZE/10));
         g.setColor(Color.white);
         String note = intToKey(scale.getNotes()[chart.length - 1 - r]);  
         g.drawString(note, x+(SIZE/2), y+(SIZE/2)+(SIZE/8));
         y+=SIZE;
      }
      
      if(!playChart)
         g.setColor(Color.red.darker().darker());		//draw red background
      else
         g.setColor(Color.green.darker().darker());	//draw green background
   
      if(SIZE == 40)                                  //low res
      {
         g.fillRect(((numCols+1)*SIZE), 0, (11*SIZE), ((numRows+1)*SIZE));
         x = ((numCols+1)*SIZE);
      }
      else
      {
         g.fillRect(((numCols+2)*SIZE), 0, (22*SIZE), ((numRows+2)*SIZE));
         x = ((numCols+2)*SIZE);
      }
      y =-(TEXT_SIZE/3);
      
      g.setFont(new Font("Monospaced", Font.PLAIN, TEXT_SIZE));
      g.setColor(Color.yellow);
    
      String instrumentName = instr[INSTRUMENT].toString().substring(11,24);
      g.drawString("key (A-G, SHIFT to sharp):" + intToKey(key), x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("scale (<,>): " + scale.getName(), x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("all scales/chords (X):"+useAllChords, x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("change note length   (UP,DN):" + WHOLENOTE_TIME, x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("playback delay (SHIFT-UP/DN):"+DELAY, x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("instr (LEFT,RIGHT):"+ instrumentName, x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("freeplay(# keys)  (S)ilence", x, y+=((int)(TEXT_SIZE*1.5)));
   
      g.drawString("start,stop song (SPACE) (P)lay scale", x, y+=((int)(TEXT_SIZE*1.5)));
      String numRowStr = "16";
      if(SIZE == 20)
         numRowStr = "32";
      g.drawString("(T)oggle number of rows   (16/32):"+numRowStr, x, y+=((int)(TEXT_SIZE*1.5)));
      String numColStr = ""+numCols;
      if(chart[0].length < 10)
         numColStr = numColStr+" ";
      g.drawString("column size([,]):"+numColStr+"   Undo(BACKSPACE)", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(R)eset chart  (SHIFT-R)emove chords", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("random song(V)   rand chords(SHIFT-V)", x, y+=((int)(TEXT_SIZE*1.5)));
   
      ArrayList<Chord>[] chordSet = commonChords;
      if(useAllChords)
         chordSet = allChords;
   
      if(mouseRow >=0 && mouseCol >= 0 && mouseRow+rowShift < chart.length && mouseCol < numCols)
      {
         g.drawString("draw note/chord   (LEFT/RIGHT CLICK)", x, y+=((int)(TEXT_SIZE*1.5)));
         int index = chart.length - 1 - mouseRow;
         String name = "none";
         if(index >= 0 && chordIndex>=0 && chordIndex < chordSet[index%chordSet.length].size())
            name = chordSet[index%chordSet.length].get(chordIndex).getName();
         g.drawString("Chord select (N/M):"+name, x, y+=((int)(TEXT_SIZE*1.5)));
      
         if(chart[mouseRow+rowShift][mouseCol]!= null && chart[mouseRow+rowShift][mouseCol].getChord() == null)
            g.setColor(Color.yellow);
         else
            g.setColor(Color.yellow.darker().darker());
      
         g.drawString("flat,sharp current note (-,+)", x, y+=((int)(TEXT_SIZE*1.5)));
      
         if(chart[mouseRow+rowShift][mouseCol]!= null)
            g.setColor(Color.yellow);
         else
            g.setColor(Color.yellow.darker().darker());
      
         g.drawString("(O)ctave up, (SHIFT-O)ctave down", x, y+=((int)(TEXT_SIZE*1.5)));
         g.drawString("increase note length (PAGE UP)", x, y+=((int)(TEXT_SIZE*1.5)));
         g.drawString("decrease note length (PAGE DOWN)", x, y+=((int)(TEXT_SIZE*1.5)));
         
         if(chart[mouseRow+rowShift][mouseCol]!= null && chart[mouseRow+rowShift][mouseCol].getChord() != null)
            g.setColor(Color.yellow);
         else
            g.setColor(Color.yellow.darker().darker());
      
         g.drawString("(I)nversion on current chord", x, y+=((int)(TEXT_SIZE*1.5)));
         
         g.setColor(Color.yellow);        
         String note = intToKey(scale.getNotes()[chart.length - 1 - mouseRow]);  
         g.setColor(Color.cyan);
         g.drawString(note, mouseX, mouseY);
      }
      else
      {
         y+=((int)(TEXT_SIZE*1.5)*7);
      }
      g.setColor(Color.yellow);
      g.drawString("(L)oad from file (W)rite to file", x, y+=((int)(TEXT_SIZE*1.5)));
   
      if(writeToFile || readFromFile)
      {
         g.drawString("enter file name:", x, y+=((int)(TEXT_SIZE*1.5)));
      }
      else
         y+=((int)(TEXT_SIZE*1.5));
      if(message.length() > 0 || textInput == true)
      {
         if(messageTime != -1)
            g.setColor(Color.blue);		   
         else
            g.setColor(Color.yellow);		//draw a bar behind message window
         if(SIZE == 40)                                  //low res
            g.fillRect(x, y+(SIZE/4), (10*SIZE), SIZE-(SIZE/3)-(SIZE/10));
         else
            g.fillRect(x, y+(SIZE/4), (20*SIZE), SIZE + (SIZE/3));
      
         if(messageTime != -1)
            g.setColor(Color.yellow);		   
         else
            g.setColor(Color.red);
         
         g.drawString(message, x, y+=((int)(TEXT_SIZE*1.5)));
      }
   
      y = 0;
      if(!playChart)
         g.setColor(Color.magenta.darker().darker());		
      else
         g.setColor(Color.cyan.darker().darker());	
   
      if(SIZE == 40)                                  //low res
      {
         g.fillRect(((numCols+1)*SIZE)+(11*SIZE), 0, (14*SIZE)/2, ((numRows+1)*SIZE));
         x = ((numCols+1)*SIZE)+(11*SIZE);
      }
      else
      {
         g.fillRect(((numCols+2)*SIZE)+(22*SIZE), 0, (28*SIZE)/2, ((numRows+2)*SIZE));
         x = ((numCols+2)*SIZE)+(22*SIZE);
      }
      y =-(TEXT_SIZE/3);
   
      g.setColor(Color.yellow);
      g.drawString("Chart Transformation:", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(F1) flip horizontal", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F1)  vertical", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(F2)  fit note/chord", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F2)chord/note", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(H)ard fit:"+hardFitNotes, x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(F3) scale octave up", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F3)  oct down", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(F4) chart octave up", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F4)  oct down", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(F5)   chart step up", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F5) step down", x, y+=((int)(TEXT_SIZE*1.5)));
      /* TO DO:
      g.drawString("(F6)add harmony line", x, y+=((int)(TEXT_SIZE*1.5)));
      g.drawString("(SHIFT-F6)structured", x, y+=((int)(TEXT_SIZE*1.5)));
      */
   }
    
    //post:  copy elements from chart into undo
   public void updateUndo()
   {
      undo = new Note[chart.length][chart[0].length];
      for(int r=0; r<chart.length; r++)
         for(int c=0; c<chart[0].length; c++)
            undo[r][c] = chart[r][c];
   }
   
    //post:  copy elements from undo into chart
   public void restoreToUndo()
   {
      chart = undo;
      highlight = new boolean[numCols];
   }

   //reverses the columns in the chart
   public void reverse()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
   
      for(int r=startRow; r<=endRow; r++)
         for(int c=0; c<numCols/2; c++)
         {
            Note temp = null;
            if(chart[r][c] != null)
            {
               temp = chart[r][c].copy();
               temp.setCol(numCols - c - 1);
            }
            chart[r][c] = chart[r][numCols - c - 1];
            chart[r][numCols - c - 1] = temp;
            
         }
   }
   
   //flip the rows in the chart
   public void flip()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
             
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      for(int c=0; c<numCols; c++)
         for(int r=startRow; r<=(endRow+startRow)/2; r++)
         {
            int flipRow = endRow - (r-startRow);
            Note noteAtR = null;
            if(chart[r][c]!=null)
            {
               int index = chart.length - 1 - flipRow;
               int track = chart[r][c].getTrack();
               int mod = chart[r][c].getMod();
               double durration = chart[r][c].getDurration();
               Chord ch = chart[r][c].getChord();
               if(ch==null)      //just a note here to make
               {
                  if(r >=0 && r < chart.length)
                  {
                     noteAtR = new Note(scale.getNotes()[index], track, durration, flipRow, c);
                     noteAtR.setMod(mod);
                  }
               }
               else     //flip chord
               {
                  ArrayList<Chord>[] chordSet = allChords;
                  ArrayList<Chord> availChords = chordSet[index % chordSet.length];
                  int chIndex = chart[r][c].getChordIndex();
                  if(availChords.size()==0)    //no chords for this key, so just place the root note
                     noteAtR = new Note(ch.getNotes()[0], track, durration, flipRow, c);
                  else
                  {
                     if(index % chordSet.length < chordSet.length)
                     {
                        //find a newChordIndex that is closest to chIndex
                        int newChordIndex = 0;
                        if(chIndex>=0 && chIndex < availChords.size())
                           newChordIndex = chIndex;
                        else
                           newChordIndex = availChords.size()-1;        
                        if(newChordIndex >=0 && newChordIndex < availChords.size())
                        {
                           Chord current = availChords.get(newChordIndex);
                           noteAtR = new Note(current, track, newChordIndex, durration, flipRow, c);
                        
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                                  //move chord to the octave of the cell it is being placed in
                           int difference = note - noteAtR.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 noteAtR.octaveDown();
                              else
                                 if(difference > 0)
                                    noteAtR.octaveUp();
                           }   
                           noteAtR.setMod(mod);             
                        }
                     }
                  
                  }
               }               
            }
            
            Note noteAtFlipR = null;
            if(chart[flipRow][c]!=null)
            {
               int index = chart.length - 1 - r;
               int track = chart[flipRow][c].getTrack();
               int mod = chart[flipRow][c].getMod();
               double durration = chart[flipRow][c].getDurration();
               Chord ch = chart[flipRow][c].getChord();
               if(ch==null)   //just a note to get
               {
                  if(flipRow >=0 && flipRow < chart.length)
                  {
                     noteAtFlipR = new Note(scale.getNotes()[index], track, durration, r, c);
                     noteAtFlipR.setMod(mod);
                  }
               }
               else     //flip chord
               {
                  ArrayList<Chord>[] chordSet = allChords;
                  ArrayList<Chord> availChords = chordSet[index % chordSet.length];
                  int chIndex = chart[flipRow][c].getChordIndex();
                  if(availChords.size()==0)    //no chords for this key, so just place the root note
                     noteAtFlipR = new Note(ch.getNotes()[0], track, durration, r, c);
                  else
                  {
                     if(index % chordSet.length < chordSet.length)
                     {
                        //find a newChordIndex that is closest to chIndex
                        int newChordIndex = 0;
                        if(chIndex>=0 && chIndex < availChords.size())
                           newChordIndex = chIndex;
                        else
                           newChordIndex = availChords.size()-1;        
                        if(newChordIndex >=0 && newChordIndex < availChords.size())
                        {
                           Chord current = availChords.get(newChordIndex);
                           noteAtFlipR = new Note(current, track, newChordIndex, durration, r, c);
                        
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                                  //move chord to the octave of the cell it is being placed in
                           int difference = note - noteAtFlipR.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 noteAtFlipR.octaveDown();
                              else
                                 if(difference > 0)
                                    noteAtFlipR.octaveUp();
                           }   
                           noteAtFlipR.setMod(mod);             
                        }
                     }
                  
                  }
               }
            
            }
            chart[r][c] = noteAtFlipR;
            chart[flipRow][c] = noteAtR;
            
         }
   }

   //post:  if there is dissonance, it shifts the melody note to the closest one that is in the chord
   public void fitMelodyToChords()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      ArrayList<Integer> dissonantCols = dissonantColumns();   //collection of columns with dissonant notes in them
     
      for(int c=0; c<numCols; c++)
      {
         if(!dissonantCols.contains(c) && !hardFitNotes)       //don't skip the column if we want to hardFitNotes into any chord that is in the same column
            continue;                                          //because a major chord with a suspended note will not register as dissonance
         for(int r=startRow; r<=endRow; r++)
            fitMelodyToChords(r,c);
      }
   }
   
   //post:  looks for any melody notes and chord that are in the same column as c
   //       if there is dissonance with the note at (r,c), it shifts the melody note at (r,c) to the closest one that is in the chord
   public void fitMelodyToChords(int r, int c)
   {
      int rowShift = r;
      Note current = chart[r][c];
      if(current!=null && current.getChord() == null)    //we have just a note. 
      {
         ArrayList<Integer> allNotesInCol = new ArrayList();
               //collection of all the notes including chord notes in that column except the one at row r (the note we might shift to end dissonance)
               //used to see if allNotesInCol can be a chord - that way, we can pick it and shift the note at row r to the closest one in the chord
         Chord chord = null;                             //see if there is a chord in the current column
         for(int r2=0; r2<chart.length; r2++)
         {
            if(r == r2)                //skip the current note we are looking at to see if we need to shift it
               continue;
            Note curr = chart[r2][c];
            if(curr!=null)
            {
               if(curr.getChord()!=null)
               {
                  chord = curr.getChord();
                  for(int n: chord.getNotes())
                     allNotesInCol.add(n);
               }
               else
                  allNotesInCol.add(curr.getNote());
            }
         }
         if(allNotesInCol.size() <= 1)
            return;
         if(!hardFitNotes || chord == null)
         {
         //now find the collection of chords that can be made with allNotesInCol
            ArrayList<Point> chInCol = chordsThatHaveNotes(allNotesInCol, commonChords);
            if(chInCol.size() > 0)
            {
               Point randPoint = chInCol.get((int)(Math.random()*chInCol.size()));
               int foundIndex = (int)(randPoint.getX());
               int foundChordIndex = (int)(randPoint.getY());
               chord = commonChords[foundIndex % commonChords.length].get(foundChordIndex);
            }
         }  
         if(chord != null)                               //if there is a chord there, move the note to the closest one that is in that chord
         {
            int note = scale.getNotes()[chart.length - 1 - r];
            if(!noteInChord(note, chord))
            {
               int higherRow = -1;
               int lowerRow = - 1;
               for(int r3=r+1; r3<chart.length; r3++)
               {
                  int higherNote = scale.getNotes()[chart.length - 1 - r3];
                  if(noteInChord(higherNote, chord))
                  {
                     higherRow = r3;
                     break;
                  }
               }
               for(int r3=r-1; r3>=0; r3--)
               {
                  int lowerNote = scale.getNotes()[chart.length - 1 - r3];
                  if(noteInChord(lowerNote, chord))
                  {
                     lowerRow = r3;
                     break;
                  }
               }
               if(higherRow >=0 && lowerRow >= 0)
               {
                  if(Math.abs(r-higherRow) < Math.abs(r-lowerRow))
                  {
                     if(chart[higherRow][c] == null)
                        rowShift = higherRow;
                     else
                        rowShift = lowerRow;
                  }
                  else
                     if(Math.abs(r-higherRow) > Math.abs(r-lowerRow))
                     {
                        if(chart[lowerRow][c] == null)
                           rowShift = lowerRow;
                        else
                           rowShift = higherRow;
                     }
                     else
                     {
                        if(Math.random() < 0.5)
                        {
                           if(chart[higherRow][c] == null)
                              rowShift = higherRow;
                           else
                              rowShift = lowerRow;
                        }
                        else
                        {
                           if(chart[lowerRow][c] == null)
                              rowShift = lowerRow;
                           else
                              rowShift = higherRow;
                        }
                     }
               }
               else
                  if(higherRow >=0)
                  {
                     rowShift = higherRow;
                  }
                  else
                     if(lowerRow >= 0)
                     {
                        rowShift = lowerRow;
                     }
            }
         }
      }
      if(r != rowShift)
      {
         chart[r][c] = null;
         if(chart[rowShift][c] == null)   //only shift the note if there is not one in the spot we want to shift to
         {
            int note = scale.getNotes()[chart.length - 1 - rowShift];
            chart[rowShift][c] = new Note(note, current.getTrack(), current.getDurration(), rowShift, c);
            chart[rowShift][c].setMod(current.getMod());
         }
      }
           
   }

   
   //post:  looks for any chord that is in the same column as melody notes
   //       if there is dissonance, it shifts the chord to one that matches the melody notes
   //       returns false if there is dissonance but no change could be made, true if change was made or no dissonance
   public boolean fitChordToMelody()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      ArrayList<Integer> dissonantCols = dissonantColumns();   //collection of columns with dissonant notes in them
   
      for(int c=0; c<numCols; c++)
      {
         if(!dissonantCols.contains(c))                        //skip this col if there is no dissonance there
            continue;
         for(int r=startRow; r<=endRow; r++)
         {
            Note current = chart[r][c];
            if(current!=null && current.getChord() != null)       //we have a chord to examine in col c 
            {
               //get all regular notes in the col and see what chords we can make out of them
               ArrayList<Integer> notesInCol = new ArrayList();      //collection of any notes in this column
            
               for(int r2=startRow; r2<=endRow; r2++)
                  if(chart[r2][c]!=null && chart[r2][c].getChord() == null)    //we have a note. 
                     notesInCol.add(chart[r2][c].getNote());
                          
               if(notesInCol.size() > 0)                          //pick a chord that has these notes within it
               {
                  ArrayList<Point> chordInfo  = chordsThatHaveNotes(notesInCol, commonChords, r); 
                  if(chordInfo.size() > 0)
                  {
                     Point randPoint = chordInfo.get((int)(Math.random()*chordInfo.size()));
                     int foundIndex = (int)(randPoint.getX());
                     int foundChordIndex = (int)(randPoint.getY());
                     Chord ch = null;
                     ch = commonChords[foundIndex % commonChords.length].get(foundChordIndex);
                     chart[r][c].setChord(ch);
                     chart[r][c].setChordIndex(foundChordIndex);
                  }  
                  else
                     return false;             
               }
            }
         }
      }
      return true;
   }

  //post:  move all chart notes an octave up 
   public void chartOctaveUp()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      for(int c=0; c<numCols; c++)
      {
         for(int r=startRow; r<=endRow; r++)
         {
            int octaveRow = r - (scale.getNumNotes()-1);
            if(chart[r][c]!=null)
            {
               Note current = chart[r][c].copy();
            
               Chord ch = current.getChord();
               if (ch==null)     //just a note
               {
                  if(octaveRow >= startRow && octaveRow <= endRow)
                  {
                     chart[r][c] = null;
                     chart[octaveRow][c] = new Note(current.getNote()-current.getMod()+OCTAVE, current.getTrack(), current.getDurration(), octaveRow, c);
                     chart[octaveRow][c].setMod(current.getMod());
                  }
               }
               else              //a chord
               {
                  if(octaveRow >= startRow && octaveRow <= endRow)
                  {
                     chart[r][c] = null;
                     current.octaveUp();
                     chart[octaveRow][c] = new Note(current.getChord(), current.getTrack(), current.getDurration(), octaveRow, c);
                     chart[octaveRow][c].setInversionType(current.getInversionType());
                  }
               }
            }
         }
      }
   }
  
   //post:  move all chart notes an octave down
   public void chartOctaveDown()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      for(int c=0; c<numCols; c++)
      {
         for(int r=endRow; r>=startRow; r--)
         {
            int octaveRow = r + (scale.getNumNotes()-1);
            if(chart[r][c]!=null)
            {
               Note current = chart[r][c].copy();
               Chord ch = current.getChord();
               if (ch==null)     //just a note
               {
                  if(octaveRow >= startRow && octaveRow <= endRow)
                  {
                     chart[r][c] = null;
                     chart[octaveRow][c] = new Note(current.getNote()-current.getMod()-OCTAVE, current.getTrack(), current.getDurration(), octaveRow, c);
                     chart[octaveRow][c].setMod(current.getMod());
                  }
               }
               else              //a chord
               {
                  if(octaveRow >= startRow && octaveRow <= endRow)
                  {
                     chart[r][c] = null;
                     current.octaveDown();
                     chart[octaveRow][c] = new Note(current.getChord(), current.getTrack(), current.getDurration(), octaveRow, c);
                     chart[octaveRow][c].setInversionType(current.getInversionType());
                  }
               }
            }
         }
      }
   
   }

 //post:  move all chart notes a step up 
   public void chartStepUp()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      //find highest note.  If it is in startRow, return out
      for(int c=0; c<numCols; c++)
         if(chart[startRow][c] != null)
            return;
      
      for(int c=0; c<numCols; c++)
      {
         for(int r=startRow; r<=endRow; r++)
         {
            int stepRow = r - 1;
            if(chart[r][c]!=null)
            {
               Note current = chart[r][c].copy();
               chart[r][c] = null;
               Chord ch = current.getChord();
               if (ch==null)     //just a note
               {
                  if(stepRow >= startRow && stepRow <= endRow)
                  {
                     int index = chart.length - 1 - stepRow;
                     if(index >= 0 && index < (scale.getNotes()).length)
                     {
                        int note = scale.getNotes()[index];
                        chart[stepRow][c] = new Note(note, current.getTrack(), current.getDurration(), stepRow, c);
                        chart[stepRow][c].setMod(current.getMod());
                     }
                  }
               }
               else              //a chord
               {
                  if(stepRow >= startRow && stepRow <= endRow)
                  {
                     //find a chord for the new row
                     int track = current.getTrack();
                     int mod = current.getMod();
                     double durration = current.getDurration();
                     int inversionType = current.getInversionType();
                     //chart[stepRow][c] = new Note(current.getChord(), track, durration, stepRow, c);
                     ArrayList<Chord>[] chordSet = commonChords;
                     int index = (chart.length - 1 - stepRow);
                     int chIndex = current.getChordIndex();           //TO DO: get a proper chordIndex
                     if(chIndex < 0 || chIndex >= chordSet[index % chordSet.length].size())
                        chIndex = 0;
                     if(index % chordSet.length < chordSet.length && chIndex >=0 && chIndex < chordSet[index % chordSet.length].size())
                     {
                        Chord currentChord = chordSet[index % chordSet.length].get(chIndex);
                        Note newNote = new Note(currentChord, track, durration, stepRow, c);
                        if(index >= 0 && index < scale.getNotes().length)
                        {
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                                  //move chord to the octave of the cell it is being placed in
                           int difference = note - newNote.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 newNote.octaveDown();
                              else
                                 if(difference > 0)
                                    newNote.octaveUp();
                           }                      
                        }
                        chart[stepRow][c] = newNote;
                        chart[stepRow][c].setInversionType(inversionType);
                        chart[stepRow][c].setChordIndex(chIndex);
                        //chart[stepRow][c].setMod(mod);
                     }
                     //******************
                  
                  }
               }
            }
         }
      }
   }

//post:  move all chart notes a step down 
   public void chartStepDown()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
      //find lowest note.  If it is in endRow, return out
      for(int c=0; c<numCols; c++)
         if(chart[endRow][c] != null)
            return;
      
      for(int c=0; c<numCols; c++)
      {
         for(int r=endRow; r>=startRow; r--)
         {
            int stepRow = r + 1;
            if(chart[r][c]!=null)
            {
               Note current = chart[r][c].copy();
               chart[r][c] = null;
               Chord ch = current.getChord();
               if (ch==null)     //just a note
               {
                  if(stepRow >= startRow && stepRow <= endRow)
                  {
                     int index = chart.length - 1 - stepRow;
                     if(index >= 0 && index < (scale.getNotes()).length)
                     {
                        int note = scale.getNotes()[index];
                        chart[stepRow][c] = new Note(note, current.getTrack(), current.getDurration(), stepRow, c);
                        chart[stepRow][c].setMod(current.getMod());
                     }
                  }
               }
               else              //a chord
               {
                  if(stepRow >= startRow && stepRow <= endRow)
                  {
                     //find a chord for the new row
                     int track = current.getTrack();
                     int mod = current.getMod();
                     double durration = current.getDurration();
                     int inversionType = current.getInversionType();
                     //chart[stepRow][c] = new Note(current.getChord(), track, durration, stepRow, c);
                     ArrayList<Chord>[] chordSet = commonChords;
                     int index = (chart.length - 1 - stepRow);
                     int chIndex = current.getChordIndex();           //TO DO: get a proper chordIndex
                     if(chIndex < 0 || chIndex >= chordSet[index % chordSet.length].size())
                        chIndex = 0;
                     if(index % chordSet.length < chordSet.length && chIndex >=0 && chIndex < chordSet[index % chordSet.length].size())
                     {
                        Chord currentChord = chordSet[index % chordSet.length].get(chIndex);
                        Note newNote = new Note(currentChord, track, durration, stepRow, c);
                        if(index >= 0 && index < scale.getNotes().length)
                        {
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                                  //move chord to the octave of the cell it is being placed in
                           int difference = note - newNote.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 newNote.octaveDown();
                              else
                                 if(difference > 0)
                                    newNote.octaveUp();
                           }                      
                        }
                        chart[stepRow][c] = newNote;
                        chart[stepRow][c].setInversionType(inversionType);
                        chart[stepRow][c].setChordIndex(chIndex);
                        //chart[stepRow][c].setMod(mod);
                     }
                     //******************
                  
                  }
               }
            }
         }
      }
   
   }
    
    //post:  returns a one-octave scale
   public static int [] trimScale(int[]notes)
   {
      int root = notes[0];
      int count = 1;
      for(int i=1; i<notes.length; i++)
      {
         count++;
         if((notes[i] - root) == OCTAVE)
            break;
      }
      int[] ans = new int[count];
      for(int i=0; i<ans.length; i++)
         ans[i] = notes[i];
      return ans;
   }
    
   //pre:  oldNumNotes is the number of notes in the scale we are switching from
   //      newNumNotes is the number of notes in the scale we are switching to 
   //post: builds chart using the current scale
   public static void rebuildChart(int oldNumNotes, int newNumNotes)
   {
      Note [][] newChart = new Note[chart.length][chart[0].length];
      int rowDifference = newNumNotes - oldNumNotes;  //if we are viewing zoomed in (16 rows):  if the number of notes in the scale
      if(numRows==32)                                 //we are switching from is different from the number of notes in the scale we 
         rowDifference = 0;                           //are switching to, then we need to shift the row of the note in the remapped 
      for(int r = 0; r < newChart.length; r++)        //chart, since the whole chart has 32 rows, but the zoomed chart has 16 rows,
      {                                               //the zoomed chart has a shifted bottom row that is pushed to the second octave
         for(int c = 0; c < newChart[0].length; c++)  //of the whole chart.
         {
            if(chart[r][c] != null)
            {
               int index = chart.length - 1 - (r-rowDifference);
               if(chart[r][c].getChord()!=null) //we have a chord - switch it to another one
               {
                  int track = chart[r][c].getTrack();
                  ArrayList<Chord>[] chordSet = allChords;
                  ArrayList<Chord> availChords = chordSet[index % chordSet.length];
                  int chIndex = chart[r][c].getChordIndex();
                  
                  if(availChords.size()==0)    //no chords for this key, so just place the root note
                     newChart[r][c] = new Note(chart[r][c].getChord().getNotes()[0], track, chart[r][c].getDurration(), r, c);
                  else
                     if(index % chordSet.length < chordSet.length)
                     {
                        //find a newChordIndex that is closest to chIndex
                        int newChordIndex = 0;
                        if(chIndex>=0 && chIndex < availChords.size())
                           newChordIndex = chIndex;
                        else
                           newChordIndex = availChords.size()-1;        
                        if(newChordIndex >=0 && newChordIndex < availChords.size() && (r-rowDifference >=0 && r-rowDifference < chart.length))
                        {
                           Chord current = availChords.get(newChordIndex);
                           Note newNote = new Note(current, track, newChordIndex, chart[r][c].getDurration(), r-rowDifference, c);
                        
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                                  //move chord to the octave of the cell it is being placed in
                           int difference = note - newNote.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 newNote.octaveDown();
                              else
                                 if(difference > 0)
                                    newNote.octaveUp();
                           }   
                           newNote.setMod( chart[r][c].getMod());             
                           newChart[r-rowDifference][c] = newNote;
                        }
                     }
               }
               else        //we just have a regular note
               {
                  int track = chart[r][c].getTrack();
                  int mod = chart[r][c].getMod();
                  double durration = chart[r][c].getDurration();
                  if(r-rowDifference >=0 && r-rowDifference < chart.length)
                  {
                     Note newNote = new Note(scale.getNotes()[index], track, durration, r-rowDifference, c);
                     newNote.setMod(mod);
                     newChart[r-rowDifference][c] = newNote;
                  }
               }
            }
         }
      }
      chart = newChart;
   }
    
    
    //pre:  num >= 0 and is a MIDI note value
    //post: return its corresponding key (multiples of 12 are C)
    //      returns "?" if it is not found
    //      used in paintComponent to show the scale as musical notes
   public static String intToKey(int num)
   {
      while(num>=12)			//strip out any octaves
         num-=OCTAVE;    	
      switch(num)
      {
         case 0: 
            return "C";
         case 1: 
            return "C#";
         case 2: 
            return "D";
         case 3: 
            return "D#";
         case 4: 
            return "E";
         case 5: 
            return "F";
         case 6: 
            return "F#";
         case 7: 
            return "G";
         case 8: 
            return "G#";
         case 9: 
            return "A";
         case 10: 
            return "A#";
         case 11: 
            return "B";
      }
      return "?";			//unknown note value sent
   }

//given a ourNote, return its normalized value where 0 is the first ourNote in the scale (C)
   public static int normalize(int ourNote)
   {
      while(ourNote>=OCTAVE)			//strip out any octaves
         ourNote-=OCTAVE;   	
      return ourNote;
   }  

//creates a sequence of notes that user selects in the chart
   public static ArrayList<Note> [] chartToNotes()
   {
      ArrayList<Note> [] notes = new ArrayList[numCols];
      for(int i=0; i<notes.length; i++)
         notes[i] = new ArrayList();
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }    
      for(int c = 0; c < numCols; c++)
         for(int r = startRow; r <= endRow; r++)       
            if(chart[r][c] != null)
               notes[c].add(chart[r][c]);        
      return notes;
   }

//pre:  scale != null and is non-empty, comprised of values >= 0
//post: plays the scale while program is being executed
   public void playNow(int[]scale)
   {
      if(scale == null)
         return;
      try 
      {
         int noteLength = WHOLENOTE_TIME*2;
         for(int i=0; i<scale.length; i++)
         {
            if(scale[i] > 0)
               channels[0].noteOn(scale[i], VOLUME);
            if(i==scale.length-1)
               Thread.sleep( noteLength * 2);	//make the last note longer
            else
               Thread.sleep( noteLength );
            channels[0].allNotesOff(); 				//turn sounds off
         }
      }
      catch (Exception e) 
      {
         message = "error playing scale";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
      channels[0].allNotesOff(); 				//turn sounds off
   }

   
   //post: writes the chart to a MIDI file
   public static void writeToMidiFile()
   {
      //String filename = "BLANK";
      Sequencer sequencer = null;
      Sequence song = null;
      try
      {
         sequencer = MidiSystem.getSequencer(); 
         sequencer.open();
      }
      catch (MidiUnavailableException e) 
      {
         message = "error writing MIDI";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
   
      try 
      {
         song = new Sequence(javax.sound.midi.Sequence.PPQ,TEMPO/4);
      }
      catch (InvalidMidiDataException e) 
      {
         message = "error writing MIDI";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
   
      Track music = song.createTrack();	//the MIDI track we write our notes into
      //setTempo(WHOLENOTE_TIME, 0, music);
      //setTempo(TEMPO, 0, music);
   
      music.add(new MidiEvent(ChangeInstrument(INSTRUMENT, 0), 0));	  //melody track on channel 0
      final int TEXT = 0x01;
      addEvent(music, TEXT, (scale.getName()).getBytes(), 0);   //add the name of the track to the MIDI file
      playToTrack(0, music, WHOLENOTE_TIME);
   
    //****  set track name (meta event)  ****
      MetaMessage mmessage = new MetaMessage();
      try 
      {
         mmessage.setMessage(0x03 ,fileName.getBytes(), fileName.length());
      }
      catch (InvalidMidiDataException e) 
      {
         message = "error writing MIDI";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
   
      music.add(new MidiEvent(mmessage,(long)0));
   
      String songTitle = "songs/"+fileName+".mid";
      /*
      while(new File("songs/"+fileName+".mid").isFile())        //if that file already exists, get a file name that is unique
      {
         fileName = fileName + (int)(Math.random()*10);
      }
    */
      int[] allowedTypes = MidiSystem.getMidiFileTypes(song); 
   	
      if (allowedTypes.length == 0) 
      { 
         //System.err.println("No supported MIDI file types.");
         message = "error writing MIDI";
         messageTime = frame + MESSAGE_DELAY;
      } 
      else 
      { 
         try
         {
            File temp = new File(songTitle);
            MidiSystem.write(song, allowedTypes[0],temp);//write to the file
            message = "file writing complete";
            messageTime = frame + MESSAGE_DELAY;
            
         }
         catch (java.io.FileNotFoundException e) 
         {
            message = "file not found error";
            messageTime = frame + MESSAGE_DELAY;
            e.printStackTrace();
         }
         catch(java.io.IOException ex) 
         {
            message = "error writing MIDI";
            messageTime = frame + MESSAGE_DELAY;
            ex.printStackTrace();
         }
      }
   }
   
   //pre:  newTempo > 0, where >=0, music!=null
   //post: adds a TEMPO meta event to set the speed of the song
   private static void setTempo(int newTempo, int where, Track music)
   {
      MetaMessage mmessage = new MetaMessage();
      int l = 60*1000000/newTempo;
      try 
      {
         mmessage.setMessage(0x51,new byte[]{(byte)(l/65536), (byte)(l%65536/256), (byte)(l%256)}, 3);
      }
      catch (InvalidMidiDataException e) 
      {
         message = "MIDI error";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
      music.add(new MidiEvent(mmessage, where));
   }

   //pre:  inst>=0 && inst<=127, channel>=0
   //post: returns a message to set instrument for a MIDI file
   private static ShortMessage ChangeInstrument(int inst, int channel)
   {
      ShortMessage temp=new ShortMessage();
      try 
      {
         temp.setMessage(ShortMessage.PROGRAM_CHANGE, channel, inst, 100);
      }
      catch (InvalidMidiDataException e) 
      {
         message = "MIDI error";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
      return temp;
   }
   
   private static void addEvent(Track track, int type, byte[] data, long where)
   {
      MetaMessage meta = new MetaMessage();
      try
      {
         meta.setMessage(type, data, data.length);
         MidiEvent event = new MidiEvent( meta, where );
         track.add(event);
      }
      catch (InvalidMidiDataException e)
      {
         message = "MIDI error";
         messageTime = frame + MESSAGE_DELAY;
         e.printStackTrace();
      }
   }
      
   //plays the melody sent with sent durations
   public static void playToTrack(int chnl,  Track music, int wholeNote)
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }    
   
      int where = 0;
      for(int c=0; c<numCols; c++)
      {
         try
         {
            int noteLength = 0;
            for(int r = startRow; r <= endRow; r++)  
            {   
               Note note = chart[r][c];
               if(note != null)
               {
                  noteLength = (int)(wholeNote * note.getDurration());
                  if(c == numCols - 1)		//make the last note longer
                     noteLength = wholeNote;
               
                  Chord chord = note.getChord();
                  if(chord == null)
                     playNote(note.getNote(), noteLength, VOLUME, where, chnl,  music);
                  else
                  {
                     for(int chordNote: chord.getNotes())
                        playNote(chordNote, noteLength, (int)(VOLUME*0.8), where, chnl,  music);
                  }
               }
            }
            where += wholeNote/4;      //each col is a quarter note
         }
         catch (InvalidMidiDataException e)
         {
            message = "MIDI error";
            messageTime = frame + MESSAGE_DELAY;
            e.printStackTrace();
         }
         catch (MidiUnavailableException e)
         {
            message = "MIDI error";
            messageTime = frame + MESSAGE_DELAY;
            e.printStackTrace();
         }
      
      }
   }

//writes a sound of pitch 'note', durartion 'noteLength', volume 'VELOCITY' at location 'where' in chanel 'chnl' in the Track 'music'
	//returns the updated tracking position - doesn't add a sound if 'note' is <= 0 but places a rest
   private static void playNote(int note, int noteLength, int myVelocity, int where, int chnl, Track music) throws InvalidMidiDataException, MidiUnavailableException
   {	
      if(noteLength < 0)
         noteLength = Math.abs(noteLength);
      if(note>0)
      {
         ShortMessage on = new ShortMessage();
         ShortMessage off = new ShortMessage();
         on.setMessage(ShortMessage.NOTE_ON, chnl, note, myVelocity);
         off.setMessage(ShortMessage.NOTE_OFF,chnl, note, myVelocity);   
         music.add(new MidiEvent(on, where));
         music.add(new MidiEvent(off, where+noteLength));
      }  
   }

   private static Map buildScaleMap(int TONE)
   {
      Map<Integer, Scale> allScales = new HashMap();
      allScales.put(0,null);
      allScales.put(1,new Major(TONE));
      allScales.put(2,new Dorian(TONE));
      allScales.put(3,new Phrygian(TONE));
      allScales.put(4,new Lydian(TONE)); 
      allScales.put(5,new Mixolydian(TONE));			
      allScales.put(6,new Aeolian(TONE));	
      allScales.put(7, new Locrian(TONE));
      allScales.put(8,new Minor(TONE));
      allScales.put(9,new Dorianb2(TONE));	
      allScales.put(10,new LydianAug(TONE));			
      allScales.put(11,new LydianDom(TONE));			
      allScales.put(12,new Mixolydianb6(TONE));			
      allScales.put(13,new LocrianS2(TONE));			
      allScales.put(14,new Altered(TONE));			
      allScales.put(15,new HarmonicMinor(TONE)); 
      allScales.put(16,new Locrian6(TONE));
      allScales.put(17,new IonianS5(TONE)); 
      allScales.put(18,new DorianS4(TONE));	
      allScales.put(19,new PhrygianMaj(TONE));	
      allScales.put(20,new LydianS2(TONE)); 
      allScales.put(21,new Ultralocrian(TONE)); 
      allScales.put(22,new Augmented(TONE));
      allScales.put(23,new Chromatic(TONE));
      allScales.put(24,new DiminishedDominant(TONE));
      allScales.put(25,new DiminishedMinor(TONE));
      allScales.put(26,new HalfWholeDiminished(TONE));
      allScales.put(27,new WholeHalfDiminished(TONE));
      allScales.put(28,new WholeTone(TONE));
      allScales.put(29,new MajorPentatonic(TONE));		
      allScales.put(30,new MinorPentatonic(TONE));				
      allScales.put(31,new DominantPentatonic(TONE));				
      allScales.put(32,new Balinese(TONE)); 
      allScales.put(33,new Chinese(TONE));
      allScales.put(34,new Chinese2(TONE)); 
      allScales.put(35,new Egyptian(TONE));	
      allScales.put(36,new Hirajoshi(TONE));
      allScales.put(37,new Hirajoshi2(TONE));
      allScales.put(38,new Iwato(TONE));
      allScales.put(39,new Japanese(TONE));
      allScales.put(40,new Kumoi(TONE));
      allScales.put(41,new Mongolian(TONE));
      allScales.put(42,new Pelog(TONE));
      allScales.put(43,new Blues(TONE));
      allScales.put(44,new BluesVar1(TONE));
      allScales.put(45,new BluesVar2(TONE));
      allScales.put(46,new BluesVar3(TONE));
      allScales.put(47,new MajorBlues(TONE));
      allScales.put(48,new BebopMajor(TONE));
      allScales.put(49,new BebopMinor(TONE));
      allScales.put(50,new BebopDominant(TONE));
      allScales.put(51,new BebopHalfDim(TONE));
      allScales.put(52,new BebopDorian(TONE));
      allScales.put(53,new EightToneSpanish(TONE));
      allScales.put(54,new Algerian(TONE));
      allScales.put(55,new Arabian(TONE));
      allScales.put(56,new Byzantine(TONE));
      allScales.put(57,new Enigmatic(TONE));
      allScales.put(58,new Flamenco(TONE));
      allScales.put(59,new HarmonicMajor(TONE));
      allScales.put(60,new HungarianGypsy(TONE));
      allScales.put(61,new HungarianMajor(TONE));
      allScales.put(62,new LeadingWholeTone(TONE));
      allScales.put(63,new LydianMinor(TONE));
      allScales.put(64,new NeopolitanMajor(TONE));
      allScales.put(65,new NeopolitanMinor(TONE));
      allScales.put(66,new Oriental(TONE));
      allScales.put(67,new Persian(TONE));
      allScales.put(68,new Spanish(TONE));	
      
      for(Integer i: allScales.keySet())
      {
         Scale current = allScales.get(i);
         if(current != null)
            current.octaveDown();
      }
      
       //{1,2,6,8,15,23,29,30,43,47}
      commonScaleIndex = new ArrayList();
      commonScaleIndex.add(1);
      commonScaleIndex.add(1);
      commonScaleIndex.add(1);
      commonScaleIndex.add(2);
      commonScaleIndex.add(6);
      commonScaleIndex.add(8);
      commonScaleIndex.add(15);
      commonScaleIndex.add(23);
      commonScaleIndex.add(29);
      commonScaleIndex.add(30);
      commonScaleIndex.add(43);
      commonScaleIndex.add(47);
   
      return allScales;
   }

 //***BEGIN MOUSE STUFF***
   private class Listener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)	//this is called for each timer iteration
      {
         if(message.length() > 0 && frame > messageTime && textInput==false)
         {
            messageTime = -1;
            message  = "";
         }
         frame++;
         if(frame == Integer.MAX_VALUE)
            frame = 1;
         if(highLightRow != -1 && frame > highLightTime + 1000)   
         {                                         //unhighlight active rows that have been highlighted
            highLightRow = -1;
            highLightTime = -1;
         }
         if(realTimeNote != -1)                    //turn on any notes played in real time
         {
            channels[0].noteOn(realTimeNote, 100);
            System.out.println(realTimeNote + "");
            realTimeNote = -1;
         }   
         if(realTimeChord != null)                 //turn on any chords played in real time
         {
            for(int note: realTimeChord.getNotes())
               channels[0].noteOn(note, VOLUME);
            realTimeChord = null;
         }
         //stop notes on any channels that have passed their durration
         for(int i=1; i<channelTimes.length; i++)  //channel 0 is used for real-time notes and chords, so skip it
         {
            if(frame > channelTimes[i].getEndTime())
            {
               channels[channelTimes[i].getChannel()].allNotesOff();
               channelTimes[i].free();
            }
         }
         
         if(playChart)
         {
            ArrayList<Note> [] melody = chartToNotes();  //each array element is the collection of notes in a column in chart
            int noteLength = WHOLENOTE_TIME/4;
            if(chartIndex >= melody.length)
            {
               for(int i=1; i<highlight.length; i++)
                  highlight[i] = false;
               chartIndex = 0;   
            }
            if(frame % noteLength == 0)
            {
               int availChannel = 0;
               for(int i=1; i<channelTimes.length; i++)
                  if(channelTimes[i].isFree())
                     availChannel = i;
               channels[availChannel].programChange(instr[INSTRUMENT].getPatch().getProgram());
               
               ArrayList<Note> currentCol = melody[chartIndex];
               
               if(currentCol!=null &&  currentCol.size()> 0)
               {
                  for(Note n: currentCol)
                  {/*  //TO DO:  assigning a different track for multiple notes in the same column fixes the problem of clipping durrations if one note
                        //       has a longer durration than another, but for some reason introduces sound artifacts (sound like cymbal and high-hat
                     int availChannel = 0;
                     for(int i=1; i<channelTimes.length; i++)
                        if(channelTimes[i].isFree())
                           availChannel = i;
                     channels[availChannel].programChange(instr[INSTRUMENT].getPatch().getProgram());
                  */
                     Chord chord = n.getChord();
                     if (chord == null)
                        channels[availChannel].noteOn(n.getNote(), VOLUME);
                     else
                     {
                        for(int note: chord.getNotes())
                           channels[availChannel].noteOn(note, (int)(VOLUME*0.8));                   
                     }
                     channelTimes[availChannel].setStartTime(frame);
                     channelTimes[availChannel].setEndTime(frame + (int)(WHOLENOTE_TIME * n.getDurration()));
                  }
               }
               if(chartIndex >= 0 && chartIndex < highlight.length)
                  highlight[chartIndex] = true;
            } 
            else if(frame % noteLength == noteLength - 1)
            {
               chartIndex++;
            } 
         }
         repaint();
      }
   }

   public void mouseClicked( MouseEvent e )
   {
      int SIZE = 40;
      int rowShift = scale.getNumNotes(); 
      if(numRows == 32)
      {
         SIZE = 20;
         rowShift = 0;       
      }
                 
      channels[0].allNotesOff(); 				//turn sounds off
      int button = e.getButton();
      if(button == MouseEvent.BUTTON1 || button == MouseEvent.BUTTON3)
      {
         int mouseR = (mouseY/SIZE);
         if(numRows==16)
         {
            int maxRow = 32 - rowShift;
            int minRow = maxRow - 15;
            mouseR += (minRow);
         }
         int mouseC = (mouseX/SIZE);
         //System.out.println(rowShift+" "+mouseR+":"+mouseC);
         if(mouseR >=0 && mouseC >= 0 && mouseR < chart.length && mouseC < numCols)
         {
            updateUndo();
            mouseRow = mouseR;
            mouseCol = mouseC;
            if(button == MouseEvent.BUTTON1)	//button 1 toggles the note
            {
               if(chart[mouseRow][mouseCol] != null)
                  chart[mouseRow][mouseCol] = null;
               else
               {
                  int index = chart.length - 1 - mouseRow;
                  if(index >= 0 && index < (scale.getNotes()).length)
                  {
                     int note = scale.getNotes()[index];
                     int nextTrack = 0;     //find the next available track
                     for(int r=0; r<chart.length; r++)
                     {
                        if(chart[r][mouseCol]!=null)
                        {
                           if(chart[r][mouseCol].getTrack() > nextTrack)
                              nextTrack = chart[r][mouseCol].getTrack();
                        }
                     }                                                    //quarter note
                     chart[mouseRow][mouseCol] = new Note(note, nextTrack, QUARTERNOTE, mouseRow, mouseCol);   
                     realTimeNote = note;
                  }
               }
            }
            else										//button 2 places a chord
            {
               if(mouseRow >=0 && mouseCol >= 0 && mouseRow < chart.length && mouseCol < chart[0].length)
               {
                  if(chart[mouseRow][mouseCol] != null)
                     chart[mouseRow][mouseCol] = null;   //if there is already a chord there, toggle it off
                  else
                  {
                     for(int r=0; r<chart.length; r++)   //clear out any chords in that col
                        if(chart[r][mouseCol] != null && chart[r][mouseCol].getChord() != null)
                           chart[r][mouseCol] = null;
                     ArrayList<Chord>[] chordSet = commonChords;
                     if(useAllChords)
                        chordSet = allChords;
                     int index = (chart.length - 1 - mouseRow);
                     if(index % chordSet.length < chordSet.length && chordIndex >=0 && chordIndex < chordSet[index % chordSet.length].size())
                     {
                        int nextTrack = 0;            //find the next available track
                        for(int r=0; r<chart.length; r++)
                        {
                           if(chart[r][mouseCol]!=null)
                           {
                              if(chart[r][mouseCol].getTrack() > nextTrack)
                                 nextTrack = chart[r][mouseCol].getTrack();
                           }
                        }
                        Chord current = chordSet[index % chordSet.length].get(chordIndex);
                        Note newNote = new Note(current, nextTrack, chordIndex, WHOLENOTE, mouseRow, mouseCol);
                        if(index >= 0 && index < scale.getNotes().length)
                        {
                           int note = scale.getNotes()[index];    //used to get chord to correct octave
                                                            //move chord to the octave of the cell it is being placed in
                           int difference = note - newNote.getChord().getNotes()[0];
                           int numOctaves = difference / OCTAVE;
                           for(int i=0; i < Math.abs(numOctaves); i++)
                           {
                              if(difference < 0)
                                 newNote.octaveDown();
                              else
                                 if(difference > 0)
                                    newNote.octaveUp();
                           }                      
                        }
                        chart[mouseRow][mouseCol] = newNote;
                        realTimeChord = newNote.getChord();
                     }
                  }
               }
            
            }
         }
         else
         {
            mouseRow = -1;//chart.length/2;
            mouseCol = -1;//chart[0].length/2;
         
         }
      
      } 
      repaint();
   }

   public void mousePressed( MouseEvent e )
   {}

   public void mouseReleased( MouseEvent e )
   {}

   public void mouseEntered( MouseEvent e )
   {}

   public void mouseMoved( MouseEvent e)
   {
      int SIZE = 40;
      int rowShift = scale.getNumNotes(); 
      if(numRows == 32)
      {
         SIZE = 20;
         rowShift = 0;       
      }
   
      mouseX = e.getX();
      mouseY = e.getY();
      
      int mouseR = (mouseY/SIZE);
      if(numRows==16)
      {
         int maxRow = 32 - rowShift;
         int minRow = maxRow - 15;
         mouseR += (minRow);
      }
      int mouseC = (mouseX/SIZE);
      
      if(mouseR >=0 && mouseC >= 0 && mouseR < chart.length && mouseC < chart[0].length)
      {
         mouseRow = mouseR;
         mouseCol = mouseC;
      }
      else
      {
         mouseRow = -1;//chart.length/2;
         mouseCol = -1;//chart[0].length/2;
      
      }
      repaint();			//refresh the screen
   }

   public void mouseDragged( MouseEvent e)
   {}

   public void mouseExited( MouseEvent e )
   {}

//*********************TXT FILE I/O
   /*
   *NUMROWS* 16
   *NUMCOLUMNS* 16
   *KEY* 60
   *SCALE* 1
   *WHOLENOTE_TIME* 170
   *INSTRUMENT* 0
   //---THEN ALL NOTES---ONE PER LINE
   *NOTE* note track chord inversionType chordIndex mod durration row col
   */
//pre:  fileName ends with .txt
//post: writes song information to fileName
   public static void writeSongToFile()
   {
      if(fileName == null || fileName.length() ==0)
      {
         message = "invalid file name";
         messageTime = frame + MESSAGE_DELAY;
         return;
      }
      try
      {
         System.setOut(new PrintStream(new FileOutputStream("songs/"+fileName+".txt")));
         System.out.println("*NUMROWS* "+numRows);          //resolution of chart
         System.out.println("*NUMCOLUMNS* "+numCols);
         System.out.println("*KEY* "+key);
         System.out.println("*SCALE* "+scaleChoice);
         System.out.println("*WHOLENOTE* "+WHOLENOTE_TIME);
         System.out.println("*INSTRUMENT* "+INSTRUMENT);
         System.out.println("//note track chord inversionType chordIndex mod durration row col");
         for(int r=0; r<chart.length; r++)
            for(int c=0; c<chart[0].length; c++)
               if(chart[r][c]!=null)
                  System.out.println("*NOTE* "+chart[r][c].allInfo());
         writeToMidiFile();
      }
      catch (IOException ignored)
      {
         message = "error writing to file";
         messageTime = frame + MESSAGE_DELAY;
      } 
   
   }
   
//pre:  fileName exists as a text file and contains properly formatted song information
//post: sets up the song according to the file information
   public static void loadSongFromFile()
   {
      if(fileName == null || fileName.length() == 0)
      {
         message = "invalid file name";
         messageTime = frame + MESSAGE_DELAY;
         return;
      }
      try
      {
         Scanner input = new Scanner(new FileReader("songs/"+fileName+".txt"));
         int lineNum = 0;                                   //keep track of line number for debugging bad txt file
         while (input.hasNextLine())		                  //while there is another line in the file
         {
            try
            {
               String sentence = input.nextLine();
               String [] parts = sentence.split(" ");
               lineNum++;
               if(parts[0].startsWith("//"))
               {
                 //comment - do nothing with this
               }
               else 
                  if(parts[0].equals("*NUMROWS*"))
                  {
                     numRows = Integer.parseInt(parts[1]);
                     if(numRows != 16 && numRows != 32)
                     {
                        //System.err.println("Invalid row size on line "+lineNum);
                     }	
                  }
                  else if(parts[0].equals("*NUMCOLUMNS*"))
                  {
                     numCols = Integer.parseInt(parts[1]);	
                     if(numCols >= 1 && numCols <= 64)
                     {
                        highlight = new boolean[numCols];
                     }  
                     else
                     {
                        //System.err.println("Invalid column size on line "+lineNum);
                     }
                  }
                  
                  else 
                     if(parts[0].equals("*KEY*"))
                     {
                        key = Integer.parseInt(parts[1]);	
                        allScales = buildScaleMap(key);
                     }
                     else if(parts[0].equals("*SCALE*"))
                     {
                        scaleChoice = Integer.parseInt(parts[1]);	
                        if(!allScales.containsKey(scaleChoice))
                        {
                           scaleChoice = 1;
                        //System.err.println("Invalid scale choice on line "+lineNum);
                        }
                        scale = allScales.get(scaleChoice);
                        chordIndex = 0;
                        commonChords = scale.getChordSetsA();
                        allChords = scale.getChordSets();
                        if(!commonScaleIndex.contains(scaleChoice))
                        {
                           useAllChords = true;
                        }
                     }
                     else if(parts[0].equals("*WHOLENOTE*"))
                     {
                        if(WHOLENOTE_TIME >= 40 && WHOLENOTE_TIME <= 1000)
                           WHOLENOTE_TIME = Integer.parseInt(parts[1]);	
                        else
                        {
                        //System.err.println("Invalid WHOLENOTE on line "+lineNum);
                        }
                     }
                     else if(parts[0].equals("*INSTRUMENT*"))
                     {
                        INSTRUMENT = Integer.parseInt(parts[1]);	
                        if(INSTRUMENT >= 0 && INSTRUMENT <= 127)
                           channels[0].programChange(instr[INSTRUMENT].getPatch().getProgram());
                        else
                        {
                        //System.err.println("Invalid INSTRUMENT on line "+lineNum);
                           INSTRUMENT = 0;
                        }
                     }
                     else if(parts[0].equals("*NOTE*"))
                     {
                        if(parts.length == 10)
                        {
                           int note = Integer.parseInt(parts[1]);
                           int track = Integer.parseInt(parts[2]);
                           String chord = parts[3];
                           int inversionType = Integer.parseInt(parts[4]);
                           int chordIndex = Integer.parseInt(parts[5]);
                           int mod = Integer.parseInt(parts[6]);
                           double durration = Double.parseDouble(parts[7]);
                           int row = Integer.parseInt(parts[8]);
                           int col = Integer.parseInt(parts[9]);
                           if(row>= 0 && row < chart.length && col>=0 && col < chart[0].length)
                           {
                              if(chord.equals("null"))
                              {
                                 chart[row][col] = new Note(note, track, durration, row, col);
                                 chart[row][col].setMod(mod);
                              }
                              else
                              {
                                 ArrayList<Chord>[] chordSet = allChords;
                                 int index = (chart.length - 1 - row);
                                 if(index % chordSet.length < chordSet.length && chordIndex >=0 && chordIndex < chordSet[index % chordSet.length].size())
                                 {
                                 
                                    Chord current = chordSet[index % chordSet.length].get(chordIndex);
                                    Note newNote = new Note(current, track, chordIndex, durration, row, col);
                                    if(index >= 0 && index < scale.getNotes().length)
                                    {
                                       note = scale.getNotes()[index];     //used to get chord to correct octave
                                                                           //move chord to the octave of the cell it is being placed in
                                       int difference = note - newNote.getChord().getNotes()[0];
                                       int numOctaves = difference / OCTAVE;
                                       for(int i=0; i < Math.abs(numOctaves); i++)
                                       {
                                          if(difference < 0)
                                             newNote.octaveDown();
                                          else
                                             if(difference > 0)
                                                newNote.octaveUp();
                                       }                      
                                    }
                                    newNote.setMod(mod);
                                    newNote.setInversionType(inversionType);
                                    chart[row][col] = newNote;
                                 }
                              }
                           }
                        }
                        else
                        {
                           message = "invalid file format";
                           messageTime = frame + MESSAGE_DELAY;
                        }
                     }
            }
            catch (java.util.InputMismatchException ex1)			//file is corrupted or doesn't exist
            {
               message = "invalid file format";
               messageTime = frame + MESSAGE_DELAY;
               return;
            }			
            catch (java.util.NoSuchElementException ex2)			
            {
               message = "invalid file format";
               messageTime = frame + MESSAGE_DELAY;
               return;
            }			
         }
         input.close();	
         message = "";
         fileName = ""; 
      }
      catch (IOException ex3)			//file is corrupted or doesn't exist
      {
         message = "invalid file name";
         messageTime = frame + MESSAGE_DELAY;
         return;
      }				
   }
   
   //post:  returns a collection of the index of each octave of the note in the current chart
   public int[] octavesInChart(int ourNote)
   {
      int maxRow = 32 - scale.getNumNotes();
      int minRow = maxRow - 15;
      if(numRows==32)
      {
         maxRow = chart.length-1;
         minRow = 0;
      }
   
      ArrayList<Integer>indexes = new ArrayList();
      int rootNote = normalize(ourNote); 
      for(int r=minRow; r<=maxRow; r++) 
      {
         int note = scale.getNotes()[chart.length - 1 - r];
         if(normalize(note) == rootNote)
            indexes.add(r);
      }
      int [] ans = new int[indexes.size()];
      int i=0;
      for(int num: indexes)
         ans[i++] = num;
      return ans;
   
   }

   //returns true if ourNote is within chord	
   public static boolean noteInChord(int ourNote, Chord chord)
   {
      int[] chordArray = chord.getNotes();
      ourNote = normalize(ourNote);
      for(int i=0; i<chordArray.length; i++)
         if (ourNote==normalize(chordArray[i]))
            return true;
      return false;
   }
    
    //post:  returns a collection of column numbers that contain a collection of notes for which no common chord can be found
   public ArrayList<Integer> dissonantColumns()
   {
      int startRow = 0;                //the note at the top of the scale
      int endRow = chart.length-1;     //the note at the bottom of the scale (lowest root)
      if(numRows==16)
      {
         endRow = (chart.length)-scale.getNumNotes();
         startRow = endRow - 15;       //show 16 notes starting from the 2nd octave of the root
      }
   
      ArrayList<Integer> cols = new ArrayList();
      for(int c=0; c<numCols; c++)
      {
         ArrayList<Integer>rowNotes = new ArrayList();
         for(int r=startRow; r<=endRow; r++)
         {
            Note current = chart[r][c];
            if(current!=null)
            {
               Chord ch = current.getChord();
               if(ch == null)
               {
                  rowNotes.add(current.getNote());
               }
               else
               {
                  for(int note: ch.getNotes())
                     rowNotes.add(note);
               }
            }
         }
         if(rowNotes.size() == 2)      //add a third note as an octave of the first (the chords we will be comparing with have at least 3 notes)
            rowNotes.add(rowNotes.get(0)+OCTAVE);
         ArrayList<Point> foundChords = chordsThatHaveNotes(rowNotes, commonChords);
         if(foundChords.size() == 0)
            cols.add(c);
      }
      return cols;
   }
       
   //pre:  ourNotes != null and non-empty, chords!=null and non-empty
   //post: returns a collection of Chord information that contain all of the notes in ourNotes in a particular row: each Point contains the chord (row index, chord index)
   public static ArrayList<Point> chordsThatHaveNotes(ArrayList<Integer> ourNotes, ArrayList<Chord>[] chords, int ourRow)
   {
      ArrayList<Point> ans = new ArrayList();
      //if(ourNotes==null || ourNotes.size()== 0)
         //return ans;
      int index = chart.length - 1 - ourRow;
      ArrayList<Chord> chordSet = chords[index % chords.length];
      for(int chIndex=0; chIndex < chordSet.size(); chIndex++)
      {
         Chord ch = chordSet.get(chIndex);
         
         boolean inChord = true;
         for(int note: ourNotes)
         {
            if(noteInChord(note, ch)==false)
            {
               inChord = false;
               break;
            }
         }
         if(inChord == true)
            ans.add(new Point(index % chords.length, chIndex));
      }
      return ans;
   }
      
   //pre:  ourNotes != null and non-empty, chords!=null and non-empty
   //post: returns a collection of Chord information that contain all of the notes in ourNotes: each Point contains the chord (row index, chord index)
   public static ArrayList<Point> chordsThatHaveNotes(ArrayList<Integer> ourNotes, ArrayList<Chord>[] chords)
   {
      ArrayList<Point> ans = new ArrayList();
      //if(ourNotes==null || ourNotes.size()== 0)
         //return ans;
      for(int row=0; row < chords.length; row++)
      {
         ArrayList<Chord> chSet = chords[row];
         for(int chIndex=0; chIndex < chSet.size(); chIndex++)
         {
            Chord ch = chSet.get(chIndex);
         
            boolean inChord = true;
            for(int note: ourNotes)
            {
               if(noteInChord(note, ch)==false)
               {
                  inChord = false;
                  break;
               }
            }
            if(inChord == true)
               ans.add(new Point(row, chIndex));
         }
      }
      return ans;
   }
   
   //returns an array of chord durations with a total duration of 4 whole notes
   public static double[] makeChordDurations(int numColumns)
   {									//each string below represents 4 beats (1 whole note)
      String[]chordPatterns4 = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
      String[]chordPatterns3 = {"000", "001", "010", "011", "100", "101", "110", "111"};
      String[]chordPatterns2 = {"00", "01", "10", "11"};
   
      String[]chordPatterns = null;
      
      double [] durrations = {QUARTERNOTE, HALFNOTE, HALFNOTE, HALFNOTE, WHOLENOTE};
      double segmentLength = durrations[(int)(Math.random()*durrations.length)];
   
      if(numColumns%4==0)
         chordPatterns = chordPatterns4;
      else
         if(numColumns%3==0)
            chordPatterns = chordPatterns3;
         else
            if(numColumns%2==0)
               chordPatterns = chordPatterns2;
   
      if(chordPatterns == null)
      {
      //TO DO: handle this case reasonably
         return null;
      }      
      							//'0' refer to no chord being hit here.  '1' refers to a chord being hit here
      int chordIndex1 = (int)(Math.random()*(chordPatterns.length-1))+1;	//pick any except "0000"
      int chordIndex2 = (int)(Math.random()*chordPatterns.length);
      while(chordIndex1==chordIndex2)
         chordIndex2 = (int)(Math.random()*chordPatterns.length);
      int chordIndex3 = (int)(Math.random()*chordPatterns.length);
      while(chordIndex1==chordIndex3 || chordIndex2==chordIndex3)
         chordIndex3 = (int)(Math.random()*chordPatterns.length);
      String chordPattern1 = chordPatterns[chordIndex1];
      String chordPattern2 = chordPatterns[chordIndex2];
      String chordPattern3 = chordPatterns[chordIndex3];
   
      String chordPattern = "";					
      
      if(numColumns%2==0)
      {
         int whichPattern =  (int)(Math.random()*20);
         if(whichPattern == 0)						//A-A-A-A
            chordPattern = chordPattern1+chordPattern1+chordPattern1+chordPattern1;
         else if(whichPattern == 1)					//A-A-A-B
            chordPattern = chordPattern1+chordPattern1+chordPattern1+chordPattern2;
         else if(whichPattern == 2)					//A-A-B-A
            chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern1;
         else if(whichPattern == 3)					//A-A-B-B
            chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern2;
         else if(whichPattern == 4)					//A-B-A-A
            chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern1;
         else if(whichPattern == 5)					//A-B-A-B
            chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern2; 
         else if(whichPattern == 6)					//A-B-B-A
            chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern1;
         else if(whichPattern == 7)					//A-B-B-B
            chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern2;
         else if(whichPattern == 8)					//A-A-B-C
            chordPattern = chordPattern1+chordPattern1+chordPattern2+chordPattern3;
         else if(whichPattern == 9)					//A-B-B-C
            chordPattern = chordPattern1+chordPattern2+chordPattern2+chordPattern3;
         else if(whichPattern == 10)				//A-B-C-C
            chordPattern = chordPattern1+chordPattern2+chordPattern3+chordPattern3;
         else if(whichPattern == 11)				//A-B-A-C
            chordPattern = chordPattern1+chordPattern2+chordPattern1+chordPattern3;
         else if(whichPattern == 12)				//A-B-C-B
            chordPattern = chordPattern1+chordPattern2+chordPattern3+chordPattern2;
         else  //make it more likely that it is a standard repeating pattern with less chaos
            chordPattern = chordPattern1+chordPattern1+chordPattern1+chordPattern1;
      }
      else
         if(numColumns%3==0)
         {
            int whichPattern =  (int)(Math.random()*10);
            if(whichPattern == 0)						//A-A-A
               chordPattern = chordPattern1+chordPattern1+chordPattern1;
            else if(whichPattern == 1)					//A-A-B
               chordPattern = chordPattern1+chordPattern1+chordPattern2;
            else if(whichPattern == 2)					//A-B-A
               chordPattern = chordPattern1+chordPattern2+chordPattern1;
            else if(whichPattern == 3)					//A-B-B
               chordPattern = chordPattern1+chordPattern2+chordPattern2;
            else if(whichPattern == 4)					//A-B-C
               chordPattern = chordPattern1+chordPattern2+chordPattern3;
            else
               chordPattern = chordPattern1+chordPattern1+chordPattern1;
         }
                                   
      int numOnes = 0;								//count the number of chord hits
      for(int i=0; i<chordPattern.length(); i++)
         if(chordPattern.charAt(i)=='1')
            numOnes++;
      if(chordPattern.startsWith("0"))			//starts with a rest      
         numOnes++;									//so the first 'chord' is a rest
      double [] chordDurations = new double[numOnes];
      int index = 0;									//index for chordDurations as we add elements
      for(int i=0; i<chordPattern.length(); i++)
      {
         if(index==0 && chordPattern.startsWith("0"))	//starts with a rest
         {
            int numZeros = 0;						//what duration to make the rest
            for(int j=0; j<chordPattern.length(); j++)
            {				
               if(chordPattern.charAt(j)=='0')
                  numZeros++;
               else
                  break; 
            }				
            chordDurations[index++] = segmentLength*(numZeros)*(-1);
         }
         else
            if(chordPattern.charAt(i)=='1')		//after each chord hit, count the number of '0's 
            {												//before there is another chord hit so that we know
               int numZeros = 0;						//what duration to make the chord
               for(int j=i+1; j<chordPattern.length(); j++)
               {				
                  if(chordPattern.charAt(j)=='0')
                     numZeros++;
                  else
                     break; 
               }											//each '0' after a chord will add a 1/4 note to the duration
               chordDurations[index++] = segmentLength*(1+numZeros);
            }
      } 
      removeDoubleRests(chordDurations);
      while(chordDurations.length < numColumns)
         chordDurations = doubleArray(chordDurations); 
      return chordDurations;
   }

//given an array of durations, looks for two rests (negative values) in a row, and makes one of the two positive
   public static void removeDoubleRests(double[]durations)
   {
      for(int i=0; i<durations.length-1; i++)
      {
         if(durations[i]<0 && durations[i+1]<0)	//side by side rests
         {	//pick one of the two rests to become non-rests
            if(Math.random() < .5)
               durations[i] *= -1;
            else
               durations[i+1] *= -1;
            i--;   
         }
      }
   }

//returns an array twice the size with two copies of the original back to back
   public static double[] doubleArray(double[]array)
   {
      double[]ans=new double[array.length*2];
      int index=0;
      for(int times=0; times<2; times++)
         for(int i=0; i<array.length; i++)
            ans[index++] = array[i];
      return ans;
   }

 //makes an array of melody durations with some repeating patterns
   public static double[] makeMelodyDurations(double total)
   {
      double [] duration = {QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, HALFNOTE, HALFNOTE, WHOLENOTE};
      ArrayList<Double> dur = new ArrayList();
      double[] times = {WHOLENOTE + HALFNOTE, WHOLENOTE, HALFNOTE + QUARTERNOTE, HALFNOTE};
      double time1 = times[(int)(Math.random()*times.length)];
      double[] segment1 = makeRiffDurations(time1,  -1, -1);
      double time2 = times[(int)(Math.random()*times.length)];
      double[] segment2 = makeRiffDurations(time2,  -1, -1);
      double timeSoFar = 0;
      while(timeSoFar < total)
      {
         double whatWillWeDo = Math.random();
         if(whatWillWeDo < .4)
         {
            for(double i:segment1)
               dur.add(i);
            timeSoFar += time1;   
         }
         else
            if(whatWillWeDo < .8)
            {
               for(double i:segment2)
                  dur.add(i);
               timeSoFar += time2;  
            }         
            else
            {
               double t = duration[(int)(Math.random()*duration.length)];
               dur.add(t);
               timeSoFar += t;  
            } 
      }
      double[] retVal = new double[dur.size()];
      int index = 0;
      for(Double i:dur)
         retVal[index++] = i;
      retVal = equalizeDurations(retVal, total);
      removeDoubleRests(retVal);  
      return retVal;
   }

//returns a collection of durations that can be used as a repeating riff of summed duration 'total'
//negative values will signify a rest
//forceDurationLength of -1 will choose random durations.  Otherwise, it forces all durations the same (except possibly the last)
 //if timeSig ==3 (in 3/4 time), the longest duration should be a dotted half-note
   public static double[] makeRiffDurations(double total, double forceDurationLength, int timeSig)
   {
      double durationFormat = .5;		//% of time that when selecting the shortest duration note, it groups them in even numbers
      double busyNess = .75;		//% of note events to rest events (i.e, 90% notes to 10% rests)
   
      double [] duration = {QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, HALFNOTE, HALFNOTE, WHOLENOTE};
   
      if(timeSig == 3)  
      {  //if timeSig ==3 (in 3/4 time), the longest duration should be a dotted half-note
         double longestNote = duration[duration.length-1];
         duration[duration.length-1] = longestNote/2 + longestNote/4;
      }
      double [] riff = new double[50];         			//a collection of durations in a riff
      double doWeDurFormat = Math.random();	//do we want to make it so that any notes of the shortest duration come in a group of an even number of notes?
      for(int i=0; i<riff.length && total > 0; i++)
      {
         double dur = duration[(int)(Math.random()*duration.length)];	//pick a random duration from the array of durations
         if(forceDurationLength != -1)
            dur = forceDurationLength;   
         if(total - dur > 0)
         {	
            if(dur == duration[0] && doWeDurFormat < durationFormat) 
            {	//if our duration is the shortest duration and we want all of the smallest durations to come in groups of even numbers
               if(total - dur*2 > 0)
               {
                  for(int j=0; j<2 && i<riff.length && total > 0; j++)
                  {
                     riff[i++] = dur;
                     total -= dur;
                  }
                  i--;
               }
            }
            else
            {
               riff[i] = dur;
               total -= dur;
            }
         }
         else
         {
            if(i != 0)  
               riff[i-1] = riff[i-1]+total;
            else
               riff[i] = total;
            total = 0;
         }
      }
      ArrayList<Double>riffTemp=new ArrayList();
      for(int i=0; i<riff.length; i++)
         if(riff[i]!=0)
            riffTemp.add(riff[i]);
      double [] theRiff = new double[riffTemp.size()];
      for(int i=0; i<theRiff.length; i++)		//clean out the non-used elements
      {
         theRiff[i] = riffTemp.get(i);
         if(Math.random()>=busyNess && theRiff[i]<WHOLENOTE)
         {	//allow it to be a rest if it is the first note OR the last note is not a rest
         //this way, we will not have two rests in a row 
            if(i==0 || (i > 0 && theRiff[i-1]>0 )) 
               theRiff[i]*=(-1);  					//rest or not, n% chance that it will be a note	   
         }
      }
      removeDoubleRests(theRiff);    
      return theRiff;
   }

//returns a new set of melody Durations that has a total duration time of 'total'
//clips the array if it is too long, adds to it if it is too short
   public static double[] equalizeDurations(double[] melodyDurations, double total)
   {
      ArrayList<Double>myDurations = new ArrayList();
      int sum = 0;
      int index = 0;
      while (sum < total && index  < melodyDurations.length)
      {
         if(sum + melodyDurations[index] >= total && myDurations.size()>=1)
         {
            int lastIndex = myDurations.size()-1;
            double whatWeAdd = (myDurations.get(lastIndex) + (total - sum));
            myDurations.set(lastIndex, whatWeAdd);
            sum += whatWeAdd;
         }
         else
         {
            double whatWeAdd = melodyDurations[index++];
            myDurations.add(whatWeAdd);
            sum += whatWeAdd;   
         }
      }
      if (sum < total)
      {  
         double[] fillIn =  makeRiffDurations(total-sum,  -1, -1);
         //ArrayList<double[]> melodySets = makeMelodyNotes(fillIn, -1, -1, -1, -1);
         //int[] newNotes = melodySets.get((int)(rand.nextDouble()*melodySets.size()-1));
         for(int i=0; i<fillIn.length; i++)
            myDurations.add(fillIn[i]);
      }
      index = 0;
      double[]myDur = new double[myDurations.size()];  
      for(int i=0; i < myDurations.size(); i++)
         myDur[index++] = myDurations.get(i);
      return myDur;
   }

   public int getRandRow()
   {
      int maxRow = 32 - scale.getNumNotes();
      int minRow = maxRow - 15;
      if(numRows==32)
      {
         maxRow = chart.length-1;
         minRow = 0;
      }
      return (int)(Math.random()*(maxRow-minRow+1)) + minRow;
   }
   
      //post:  returns a collection of all notes (single and chord) in a specific column
   public ArrayList<Integer> notesInCol(int c)
   {
      ArrayList<Integer> allNotesInCol = new ArrayList();
      Chord chord = null;                             //see if there is a chord in the current column
      for(int r=0; r<chart.length; r++)
      {
         Note curr = chart[r][c];
         if(curr!=null)
         {
            if(curr.getChord()!=null)
            {
               chord = curr.getChord();
               for(int n: chord.getNotes())
                  allNotesInCol.add(n);
            }
            else
               allNotesInCol.add(curr.getNote());
         }
      }
      return allNotesInCol;
   }

   
   //returns the melody line in the chart if there is one.  Used for creating harmony or countermelody
   //the first row is the durrations, the second row is the notes
   //stores -1 if there is no note in that column
   public double[][] findMelody()
   {
      double [][] melody = new double[2][numCols];
      int maxRow = 32 - scale.getNumNotes();
      int minRow = maxRow - 15;
      if(numRows==32)
      {
         maxRow = chart.length-1;
         minRow = 0;
      }
      boolean firstNote = true;
      for(int c=0; c<numCols; c++)
      {
         ArrayList<Integer>notesInCol = new ArrayList(); //stores the row numbers of cells that have notes in them
         for(int r=minRow; r<=maxRow; r++)
         {
         //get a collection of all single notes in this col.
         //pick one that is the closes row to the previous note picked
            if(chart[r][c]!=null && chart[r][c].getChord()==null)
               notesInCol.add(r);
         }
         if(notesInCol.size()>0)
         {
            if(firstNote)     //pick a random note for the firstNote
            {
               int row = notesInCol.get((int)(Math.random()*notesInCol.size()));
               melody[0][c] = chart[row][c].getDurration();
               melody[1][c] = chart[row][c].getNote();
               firstNote = false;
            }
            else
            {
               double lastNote = melody[1][c-1];
               int closest = 0;
               for(int i=0; i<notesInCol.size(); i++)
               {
                  int curr = notesInCol.get(i);
                  if(Math.abs(lastNote - chart[curr][c].getNote()) < closest)
                     closest = i;
               }
               int row = notesInCol.get(closest);
               melody[0][c] = chart[row][c].getDurration();
               melody[1][c] = chart[row][c].getNote();
            }
         }
         else                 //no note in this col
         {
            melody[0][c] = -1;
            melody[1][c] = -1;
         }
      }
      if(firstNote==true)    //we never found any notes
         return null;
      return melody;
   }
   
   /* TO DO:
   //post:  adds a harmony line to the melody if there is one
   //       structure of true will try to have it follow the same pattern:  matched
   //       structure of false will have it randomly rise/fall, ascending, descending, flatline or mixed
   public void addHarmony(boolean structure)
   {
      double[][] melody = findMelody();
      int maxRow = 32 - scale.getNumNotes();
      int minRow = maxRow - 15;
      if(numRows==32)
      {
         maxRow = chart.length-1;
         minRow = 0;
      }
   
      if(melody == null)
      {
         message = "no melody found";
         messageTime = frame + MESSAGE_DELAY;
         return;
      }
      for(int c=0; c<melody[0].length; c++)
      {
         double durration = melody[0][c];
         int origNote = (int)(melody[1][c]);
         //ArrayList<Integer> allNotes = notesInCol(c);
         if(durration != -1)
         {
            //chart[maxRow][c] = new Note(scale.getRoot(), 0, durration, maxRow, c); 
         }
      }
   }
   */
   
   //if durr == null, it picks them randomly or composes them
   public void randomSong(boolean chords)
   {
      int maxRow = 32 - scale.getNumNotes();
      int minRow = maxRow - 15;
      if(numRows==32)
      {
         maxRow = chart.length-1;
         minRow = 0;
      }
      if(chords == false)              //place notes, note chords
      {
         double [] durrations= {EIGHTHNOTE, QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, QUARTERNOTE, HALFNOTE, HALFNOTE, WHOLENOTE};    //random durrations to pick from
         double [] melodyDurrations = null;
         double timeLeft = numCols/4.0;
         if(Math.random() < 0.5)
            melodyDurrations =  makeMelodyDurations(timeLeft);
         int durrationIndex = 0;   
         int col = 0;                     //column we are writing into in chart
         int lastNote = -1;               //keep track of index of last note played
         while(timeLeft > 0)
         {
            boolean skipNote = false;        //skip note if it is intended to be a rest
            if(col >= numCols)
               break;
            double durration = durrations[(int)(Math.random()*durrations.length)];
            if(melodyDurrations != null && durrationIndex < melodyDurrations.length)
               durration = melodyDurrations[durrationIndex++];
            if(durration < 0)             //a rest
               skipNote = true;   
            if((timeLeft - durration < 0) || col == numCols - 1 )  //clip the durration if it is the last note
               durration = timeLeft;
               
            int randRow = getRandRow();
            if(lastNote == -1)            //it is our first note
            {
               if(Math.random() < 0.5)    //make it the root
               {                          //start on a random octave of the root
                  int [] rootOctaves = octavesInChart(scale.getRoot()); 
                  if(rootOctaves.length > 0)
                     randRow = rootOctaves[(int)(Math.random()*rootOctaves.length)];
                  else
                     randRow = numRows - 1;
               }
            }
            else
            {  //75% of the time, make the next note a random note within 3 steps
               //otherwise, keep the next note within an octave of the previous
               int jumpRange = scale.getNumNotes();
               if(Math.random() < .75)
                  jumpRange = 4;
               int jump = (int)(Math.random() * jumpRange);
               if(Math.random() < 0.5)       //raise to the next note
               {
                  if((lastNote - jump) >=minRow && (lastNote - jump) <= maxRow)
                     randRow = lastNote - jump;
                  else
                     if((lastNote + jump) >=minRow && (lastNote + jump) <= maxRow)
                        randRow = lastNote + jump;
                     else
                        randRow = getRandRow();
               }
               else
               {
                  if((lastNote + jump) >=minRow && (lastNote + jump) <= maxRow)
                     randRow = lastNote + jump;
                  else
                     if((lastNote - jump) >=minRow && (lastNote - jump) <= maxRow)
                        randRow = lastNote - jump;
                     else
                        randRow = getRandRow();
               }                          
            } 
            if(randRow < minRow || randRow > maxRow)
               skipNote = true;              
            int note = scale.getNotes()[chart.length - 1 - randRow];
            int nextTrack = 0;            //find the next available track if there is bleedover from previous note to next
         
            if(chart[randRow][col] == null && !skipNote)
            {
               chart[randRow][col] = new Note(note, nextTrack, durration, randRow, col); 
               lastNote = randRow;
               fitMelodyToChords(randRow, col);
            }
            
            double temp = Math.abs(durration);     
            if((temp - (int)(temp)) == EIGHTHNOTE)    
               temp += EIGHTHNOTE;
            col += temp*4;   
            timeLeft -= temp;
         }
      }
      else                             //place chords, not notes
      {
         double[] pattern = null;
         int patternIndex = 0;
         boolean noNotes = true;       //see if the chart is empty - if so, we will use a pattern for chord durrations
         for(int r=0; r<chart.length; r++)
            for(int c=0; c<chart[0].length; c++)
               if(chart[r][c]!=null)
                  noNotes = false;
         if(noNotes)
            pattern = makeChordDurations(numCols);
         double [] durrations= {EIGHTHNOTE, QUARTERNOTE, HALFNOTE, HALFNOTE, HALFNOTE, WHOLENOTE, WHOLENOTE, WHOLENOTE};    //random durrations to pick from
         double timeLeft = numCols/4.0;
         int col = 0;                     //column we are writing into in chart
         int lastNote = -1;               //keep track of index of last note played
         boolean keepInOctave = false;    //keep chords within one octave of the root
         if(Math.random() < 0.66)
            keepInOctave = true;
         //if we chose to keep the chords within an octave, get a collection of all the rows we want to pick from   
         int [] rootOctaves = octavesInChart(scale.getRoot()); 
         if(rootOctaves[0] - scale.getNumNotes() < minRow)            //the first root row isn't a full octave, so eliminate it
         {
            int [] newRootOctaves = new int[rootOctaves.length-1];
            for(int i=0; i<newRootOctaves.length; i++)
               newRootOctaves[i] = rootOctaves[i+1];
            rootOctaves = newRootOctaves;
         }
         int whichOctave = rootOctaves[(int)(Math.random()*rootOctaves.length)];
         int [] chordRows = new int[scale.getNumNotes()-1];
         for(int i=0; i<chordRows.length; i++)
         {
            chordRows[i] = whichOctave-i;
         }
      
         while(timeLeft > 0)
         {
            if(col >= numCols)
               break;
            double durration = durrations[(int)(Math.random()*durrations.length)];
            if(pattern != null)
               durration = pattern[patternIndex];
            if((timeLeft - durration < 0) || col == numCols-1)  //clip the durration if it is the last chord
               durration = timeLeft;
            int randRow = getRandRow();
            if(keepInOctave)
               randRow = chordRows[(int)(Math.random()*chordRows.length)];
            int foundIndex = -1;
            int foundChordIndex = -1;
            boolean skipChord = false;    //skip placing this chord because there are notes in that col and no chord to match can be found
            if(pattern != null && pattern[patternIndex] < 0)
               skipChord=true;            //negative durration in pattern means that it is a rest, so skip the chord
            patternIndex++;
         
            ArrayList<Integer> notesInCol = new ArrayList();      //collection of any notes in this column
            for(int r=0; r<chart.length; r++)
            {
               if(chart[r][col]!=null)
               { 
                  if(chart[r][col].getChord()==null)
                     notesInCol.add(chart[r][col].getNote());
                  else
                     skipChord = true;                            //skip the chord if there is already one in that column
               }
            }
               
            if(notesInCol.size() > 0)                             //pick a chord that has these notes within it
            {
               ArrayList<Point> chordInfo = chordsThatHaveNotes(notesInCol, commonChords);
               if(chordInfo.size() > 0)
               {
               //make first chord the root if the note there is contained in the root
                  if(lastNote == -1)            //it is our first chord
                  {
                     ArrayList<Point> rootChords = new ArrayList();
                     for(Point p:chordInfo)     //build collection of all root chords
                     {
                        if(p.getX() == 0)
                           rootChords.add(p);
                     }
                     if(rootChords.size() > 0 && Math.random() < 0.5)
                     {
                        Point randPoint = rootChords.get((int)(Math.random()*rootChords.size()));
                        foundIndex = (int)(randPoint.getX());
                        foundChordIndex = (int)(randPoint.getY());
                     }
                     else
                     {
                        Point randPoint = chordInfo.get((int)(Math.random()*chordInfo.size()));
                        foundIndex = (int)(randPoint.getX());
                        foundChordIndex = (int)(randPoint.getY());
                     }
                  }
                  else
                  {  
                     Point randPoint = chordInfo.get((int)(Math.random()*chordInfo.size()));
                     foundIndex = (int)(randPoint.getX());
                     foundChordIndex = (int)(randPoint.getY());
                  }  
               }
               else
               {
                     //there are no chords with those notes, so skip it
                  skipChord = true;
               }
               
            }        
            else  //there are no notes in that column, so....make it the root if it is the first chord or
            {     //pick a random next chord within an ocatave of the last
               if(lastNote == -1)            //it is our first chord
               {
                  if(Math.random() < 0.66)    //make it the root
                  {                          //start on a random octave of the root
                     if(keepInOctave)
                        randRow = chordRows[0];
                     else
                     {
                        int [] octaves = octavesInChart(scale.getRoot()); 
                        if(octaves.length > 0)
                           randRow = octaves[(int)(Math.random()*octaves.length)];
                        else
                           randRow = numRows - 1;  //randRow = chart.length - 1;
                     }
                  }
               }
               else
               {
                  if(keepInOctave)
                     randRow = chordRows[(int)(Math.random()*chordRows.length)];
                  else
                  {
                     int jumpRange = scale.getNumNotes();
                     int jump = (int)(Math.random() * jumpRange);
                     if(Math.random() < 0.5)       //raise to the next chord
                     {
                        if((lastNote - jump) >=minRow && (lastNote - jump) <= maxRow)
                           randRow = lastNote - jump;
                        else
                           if((lastNote + jump) >=minRow && (lastNote + jump) <= maxRow)
                              randRow = lastNote + jump;
                     }
                     else
                     {
                        if((lastNote + jump) >=minRow && (lastNote + jump) <=maxRow)
                           randRow = lastNote + jump;
                        else
                           if((lastNote - jump) >=minRow && (lastNote - jump) <= maxRow)
                              randRow = lastNote - jump;
                     }   
                  }                       
               }
            }
         
            ArrayList<Chord>[] chordSet = commonChords;
            
            int index = (chart.length - 1 - randRow);
            int chordIndex = (int)(Math.random() * chordSet[index % chordSet.length].size());
           //50% chance a chord at the end of chordSet (less common) is set to one in the first half of the chordSet (more common chords)
            if((Math.random() < 0.5) &&  (chordSet[index % chordSet.length].size() > 0) && (chordIndex > (chordSet[index % chordSet.length].size())/2))
               chordIndex = (int)(Math.random() * ((chordSet[index % chordSet.length].size())/2));
            if(foundIndex >= 0 && foundChordIndex >= 0 && foundChordIndex < chordSet[foundIndex % chordSet.length].size())
            {
               index = foundIndex;
               randRow = chart.length - 1  - index;
               chordIndex = foundChordIndex;
               if(lastNote == -1)            //it is our first chord
               {                             //start on a random octave of the chord
                  int [] octaves = octavesInChart(scale.getNotes()[index]); 
                  if(octaves.length > 0)
                     randRow = octaves[(int)(Math.random()*octaves.length)];
               }
               else                          //there is a last chord (it is not the first chord)
               {
                //now move the chord within an octave of the lastNote if it is not -1 
                  int rowDifference = lastNote - randRow;
                  int numOctaves = Math.abs(rowDifference) / (scale.getNumNotes() - 1);
                  for(int i=0; i<numOctaves; i++)
                  {
                     if(rowDifference < 0)
                        randRow -= (scale.getNumNotes() - 1);
                     else
                        randRow += (scale.getNumNotes() - 1);
                  }
               }
            }
            if(randRow < minRow || randRow > maxRow)
               skipChord = true;         
            if(!skipChord && (index % chordSet.length < chordSet.length) && chordIndex >=0 && (chordIndex < chordSet[index % chordSet.length].size()))
            {
               int nextTrack = 0;                        //find the next available track if there is bleedover from previous note to next
               index = (chart.length - 1 - randRow);
               Chord current = chordSet[index % chordSet.length].get(chordIndex);
               Note newNote = new Note(current, nextTrack, chordIndex, durration, randRow, col);
               if(index >= 0 && index < scale.getNotes().length)
               {
                  int note = scale.getNotes()[index];       //used to get chord to correct octave
                                                            //move chord to the octave of the cell it is being placed in
                  int difference = note - newNote.getChord().getNotes()[0];
                  int numOctaves = difference / OCTAVE;
                  for(int i=0; i < Math.abs(numOctaves); i++)
                  {
                     if(difference < 0)
                        newNote.octaveDown();
                     else
                        if(difference > 0)
                           newNote.octaveUp();
                  } 
                                     
               }
               if(chart[randRow][col] == null)
               {
                  chart[randRow][col] = newNote; 
                  if(Math.random() < 0.5)    //chance we will consider a chord inversion
                  {
                     int numInversions = (int)(Math.random()*3);
                     for(int i=0; i<numInversions; i++)
                        chart[randRow][col].inversion();
                  }
                  lastNote = randRow;
               }
            }   
            double temp = Math.abs(durration);
            if((temp - (int)(temp)) == EIGHTHNOTE)    
               temp += EIGHTHNOTE;
            col += temp*4;   
            timeLeft -= temp;
         }
      
      }
   }
   
}

