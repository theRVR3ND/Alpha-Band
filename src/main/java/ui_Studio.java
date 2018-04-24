/**
 * Alpha Band - Multiplayer Rythym Game | ui_Studio
 * Concept and game by Shae McMillan
 * Engine by Kelvin Peng
 * W.T.Woodson H.S.
 * 2017
 *
 * Song studio for transcribing/creating songs into game format.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class ui_Studio extends ui_Menu implements bg_Constants, KeyListener, MouseWheelListener{
   
   private ui_Textbox nameTextbox; //Name of song
   
   private ui_Slider bpmSlider, pageSlider;
   
   private ui_Table fileList; //List of song files
   
   private String message; //To tell user stuff
   
   private byte key, scale, instrument;
   
   private long playStartTime; //Time of preview play start
   
   private short playStartPage; //Page of notes play preview should start on
   
   private ArrayList<HashSet<Byte>> currNotes; //For playing song preview
   
   private ArrayList<HashMap<Short, HashSet<Byte>>> song; //Thing
   
   private final float areaX = 0.40f,
                       areaY = 0.03f,
                       areaW = 0.55f,
                       areaH = 0.55f;
   
   private static final String[] keys = new String[] {
      "C",
      "C#",
      "D",
      "D#",
      "E",
      "F",
      "F#",
      "G",
      "G#",
      "A",
      "A#",
      "B"
   };
   
   private static final String[] scales = new String[] {
      "Major",
      "Blues",
      "Minor",
      "Harmonic"
   };
   
   private static final String[] percussions = new String[] {
      "Bass",
      "Snare",
      "Cymbal"
   };
   
   private static final byte[] percussionInstrumentValues = new byte[] {
      35, 38, 49
   };
   
   /**
    * Constructor.
    */
   public ui_Studio(){
      buttons = new ui_Button[] {
         new ui_Button("SAVE", 0.50f, 0.7f),
         new ui_Button("OPEN", 0.08f, 0.78f),
         new ui_Button("NEW",  0.22f, 0.78f),
         new ui_Button("BACK", 0.50f, 0.85f)
      };
      
      nameTextbox = new ui_Textbox(
         0.02f, 0.03f,
         0.3f, 0.05f,
         MAX_PLAYER_NAME_LENGTH
      );
      nameTextbox.setContents("Song Name");
      
      bpmSlider = new ui_Slider(
         "BPM:",
         0.06f, 0.15f,
         0.2f, 0.02f,
         (short)60, (short)210
      );
      
      pageSlider = new ui_Slider(
         "Page:",
         areaX, 0.60f,
         areaW, 0.02f,
         (short)0, (short)5
      );
      pageSlider.setValue((short)0);
      
      fileList = new ui_Table(
         0.02f, 0.5f,
         0.26f, 0.2f,
         new String[] {"Song files"},
         new float[] {0.03f}
      );
      
      //Load editable song files
      File[] folder = (new File(util_Utilities.getDirectory() + "/resources/songs")).listFiles();
      ArrayList<String[]> songList = new ArrayList<String[]>(folder.length);
      
      for(File f : folder){
         String name = f.getName();
         name = name.substring(0, name.indexOf("."));
         
         if(name.equals("example"))
            continue;
         
         songList.add(new String[] {name}); //Read in each file's name
      }
      
      fileList.setContents(songList); //Add into table list
      
      //Initialize stuff
      message = null;
      key = 0;
      scale = 0;
      instrument = 0;
      playStartTime = Long.MAX_VALUE;
      playStartPage = 0;
      
      song = new ArrayList<>();
      currNotes = new ArrayList<>();
      for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
         song.add(new HashMap<Short, HashSet<Byte>>());
         currNotes.add(new HashSet<Byte>());
      }
      
      //Add listeners
      this.setFocusable(true);
      this.addKeyListener(this);
      this.addMouseWheelListener(this);
   }
   
   /**
    * Paint method for panel.
    *
    * @param g                   Graphics instance to paint into
    */
   public void paintComponent(Graphics g){
      super.paintComponent(g);
      
      //Improve rendering quality
      Graphics2D g2 = util_Utilities.improveQuality(g);
      
      //UI Tings
      nameTextbox.draw(g2);
      bpmSlider.draw(g2);
      pageSlider.draw(g2);
      fileList.draw(g2);
      
      //Draw key bind info
      g2.setColor(ui_Theme.getColor(ui_Theme.TEXT));
      g2.setFont(new Font(
         "Courier New",
         Font.PLAIN,
         util_Utilities.getFontSize()
      ));
      
      g2.drawString(
         "Key (- or +): " + keys[key],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.25f * cg_Client.SCREEN_HEIGHT)
      );
      
      g2.drawString(
         "Scale (< or >): " + scales[scale],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.3f * cg_Client.SCREEN_HEIGHT)
      );
      
      g2.drawString(
         "Instrument ([ or ]): " + util_Music.instruments[instrument],
         (int)(0.02f * cg_Client.SCREEN_WIDTH),
         (int)(0.35f * cg_Client.SCREEN_HEIGHT)
      );
      
      if(playStartTime == Long.MAX_VALUE){
         g2.drawString(
            "Play: [SPACE]",
            (int)(0.02f * cg_Client.SCREEN_WIDTH),
            (int)(0.4f * cg_Client.SCREEN_HEIGHT)
         );
      }else{
         g2.drawString(
            "Pause: [SPACE]",
            (int)(0.02f * cg_Client.SCREEN_WIDTH),
            (int)(0.4f * cg_Client.SCREEN_HEIGHT)
         );
      }
      
      //Sheet music area
      g2.drawRect(
         (short)(areaX * cg_Client.SCREEN_WIDTH),
         (short)(areaY * cg_Client.SCREEN_HEIGHT),
         (short)(areaW * cg_Client.SCREEN_WIDTH),
         (short)(areaH * cg_Client.SCREEN_HEIGHT)
      );
      
      //Draw note letter things?
      if(instrument != util_Music.DRUMS){
         //Draw regular note scale labels
         for(byte i = 0; i < 10; i++){
            g2.drawString(
               keys[(key + util_Music.INTERVALS[scale][i % util_Music.INTERVALS[scale].length]) % keys.length],
               (short)(0.38 * cg_Client.SCREEN_WIDTH),
               (short)((areaY + areaH - 0.0275 - 0.055 * i) * cg_Client.SCREEN_HEIGHT)
            );
         }
      }else{
         //Draw percussion stupid thing
         FontMetrics fm = g2.getFontMetrics();
         for(byte i = 0; i < percussions.length; i++){
            g2.drawString(
               percussions[i],
               (short)((areaX - 0.01) * cg_Client.SCREEN_WIDTH - fm.stringWidth(percussions[i])),
               (short)((areaY + areaH - 0.1925 - 0.11 * i) * cg_Client.SCREEN_HEIGHT)
            );
         }
      }
      
      //Draw beat demarcations
      for(byte i = 1; i < 11; i++){
         g2.drawLine(
            (short)((areaX + 0.05 * i) * cg_Client.SCREEN_WIDTH),
            (short)(areaY * cg_Client.SCREEN_HEIGHT),
            (short)((areaX + 0.05 * i) * cg_Client.SCREEN_WIDTH),
            (short)((areaY + areaH) * cg_Client.SCREEN_HEIGHT)
         );
      }
      
      //Draw notes
      for(byte note = 0; note < 10; note++){
         for(byte beat = 0; beat < 11; beat++){
            if(song.get(instrument).containsKey((short)(pageSlider.getValue() * 11 + beat)) &&
               song.get(instrument).get((short)(pageSlider.getValue() * 11 + beat)).contains((byte)(9 - note))){
               g2.fillRect(
                  (int)((areaX + (beat * areaW / 11)) * cg_Client.SCREEN_WIDTH),
                  (int)((areaY + (note * areaH / 10)) * cg_Client.SCREEN_HEIGHT),
                  (int)((areaW / 11) * cg_Client.SCREEN_WIDTH),
                  (int)((areaH / 10) * cg_Client.SCREEN_HEIGHT + 1)
               );
            }
         }
      }
      
      //Draw play bar thing and play notes
      if(playStartTime != Long.MAX_VALUE){
         while(true){
            final double millsPerBeat = 1000 / (bpmSlider.getValue() / 60.0);
            double currBeat = (System.currentTimeMillis() - (playStartTime + millsPerBeat * (pageSlider.getValue() - playStartPage) * 11)) / millsPerBeat ; //From 0 to 10
            
            //Visual demarcation
            short drawX = (short)((areaX + areaW * (currBeat / 11.0)) * cg_Client.SCREEN_WIDTH);
            
            if(drawX > (areaX + areaW) * cg_Client.SCREEN_WIDTH){
               if(pageSlider.getMaximum() <= pageSlider.getValue()){
                  playStartTime = Long.MAX_VALUE;
                  cg_MIDI.silence();
                  for(HashSet<Byte> noteBuffer : currNotes)
                     noteBuffer.clear();
                  break;
               }else
                  pageSlider.setValue((short)(pageSlider.getValue() + 1));
               continue;
            }
            
            g2.setColor(new Color(225, 200, 95));
            g2.drawLine(
               drawX,
               (int)(areaY * cg_Client.SCREEN_HEIGHT),
               drawX,
               (int)((areaY + areaH) * cg_Client.SCREEN_HEIGHT)
            );
            
            //Play notes
            currBeat += pageSlider.getValue() * 11;
            for(byte instrument = 0; instrument < util_Music.NUM_INSTRUMENTS; instrument++){
               //Play new notes
               if(song.get(instrument).containsKey((short)(currBeat))){
                  for(Byte note : song.get(instrument).get((short)(currBeat))){
                     if(!currNotes.get(instrument).contains(note)){
                        //Play note
                        if(instrument != util_Music.DRUMS){
                           byte octaves = (byte)(note / util_Music.INTERVALS[scale].length);
                           byte adjustedNote = (byte)(util_Music.INTERVALS[scale][note % util_Music.INTERVALS[scale].length] + 60 + octaves * 12 + key);
                           cg_MIDI.playNote(adjustedNote, instrument);
                           
                        }else{
                           cg_MIDI.playNote((byte)(percussionInstrumentValues[(note - 3) / 2]), instrument);
                        }
                        currNotes.get(instrument).add(note);
                     }
                  }
               }
               
               //Clear old notes
               if(currBeat > 1){
                  if(song.get(instrument).containsKey((short)(currBeat))){
                     try{
                        for(Byte note : currNotes.get(instrument)){
                           if(!song.get(instrument).get((short)(currBeat)).contains(note))
                              currNotes.get(instrument).remove(note);
                        }
                     }catch(ConcurrentModificationException e){}
                  }else
                     currNotes.get(instrument).clear();
               }
            }
            
            break;
         }
      }
      
      //Draw status message
      if(message != null){
         g2.drawString(
            message,
            (int)(0.020 * cg_Client.SCREEN_WIDTH),
            (int)(0.875 * cg_Client.SCREEN_HEIGHT)
         );
      }
      
      repaint();
   }
   
   /**
    * Process mouse click event.
    *
    * @param e                   Mouse click event to process
    */
   public void mouseClicked(MouseEvent e){
      super.mouseClicked(e);
      
      //Save current song
      if(buttons[0].isDown()){
         if(nameTextbox.getContents().equals("Song Name")){
            message = "Please enter a name for this song.";
            nameTextbox.setSelected(true);
            return;
         }else{
            try{
               saveToFile();
               message = "Song successfully saved.";
            }catch(IOException ex){
               message = "Song save failed.";
            }
         }
      
      //Open specified file
      }else if(buttons[1].isDown()){
         if(fileList.getHoverRow() >= 0){
            //Store old values in case of load failure
            ArrayList<HashMap<Short, HashSet<Byte>>> oldSong = song;
            String oldName = nameTextbox.getContents();
            short oldBPM = bpmSlider.getValue();
            byte oldScale = scale;
            byte oldKey = key;
            
            //Load file
            try{
               Scanner input = new Scanner(new File(
                  util_Utilities.getDirectory() + "/resources/songs/" +
                  fileList.getContents().get(fileList.getHoverRow())[0] + ".cfg"
               ));
               
               input.nextLine(); //Skip song difficulty
               input.nextLine(); //Skip song length
               
               nameTextbox.setContents(fileList.getContents().get(fileList.getHoverRow())[0]);
               bpmSlider.setValue((short)(input.nextInt()));
               scale = input.nextByte();
               key = (byte)(input.nextByte() - 60);
               fileList.setHoverRow((byte)-1);
               input.nextLine(); //Just because
               
               short songLength = 0;
               
               //Load notes
               song = new ArrayList<>();
               for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
                  song.add(new HashMap<Short, HashSet<Byte>>());
                  
                  while(true){
                     String[] line = input.nextLine().split(" ");
                     
                     if(line.length <= 1)
                        break;
                     
                     short beat = Short.parseShort(line[0]);
                     song.get(i).put(beat, new HashSet<Byte>());
                     
                     for(byte j = 1; j < line.length; j++){
                        if(i != util_Music.DRUMS){
                           song.get(i).get(beat).add((byte)(Byte.parseByte(line[j]) - 60));
                        }else{
                           byte noteVal = Byte.parseByte(line[j]);
                           for(byte k = 0; k < percussionInstrumentValues.length; k++){
                              if(percussionInstrumentValues[k] == noteVal){
                                 song.get(i).get(beat).add((byte)(k * 2 + 3));
                                 break;
                              }
                           }
                        }
                     }
                     
                     songLength = (short)(Math.max(beat, songLength));
                  }
               }
               
               pageSlider.setValue((short)0);
               pageSlider.setMaximum((short)(songLength / 11 + 1));
               
               message = null;
            
            }catch(FileNotFoundException ex){
               message = "Load failed. Could not find song file.";
               //System.out.println("Could not load song file.");
            
            }catch(NoSuchElementException ex){
               //Restore old values
               song = oldSong;
               nameTextbox.setContents(oldName);
               bpmSlider.setValue(oldBPM);
               scale = oldScale;
               key = oldKey;
               
               //Tell the user about it
               message = "Load failed. File format corrupted.";
            }
         }
      
      //Load blank file
      }else if(buttons[2].isDown()){
         if(e.getClickCount() == 2){
            //Reset defaults
            key = 0;
            scale = 0;
            instrument = 0;
            playStartTime = Long.MAX_VALUE;
            playStartPage = 0;
            
            song = new ArrayList<>();
            currNotes = new ArrayList<>();
            for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++){
               song.add(new HashMap<Short, HashSet<Byte>>());
               currNotes.add(new HashSet<Byte>());
            }
            
            nameTextbox.setContents("Song Name");
            pageSlider.setMaximum((short)5);
            pageSlider.setValue((short)0);
            
            message = null;
         
         }else{
            //Tell user to double click
            message = "Double click to load new file.";
         }
      
      //Redirect to main page
      }else if(buttons[3].isDown()){
         cg_Client.frame.setContentPane(ui_Menu.main);
      
      }else{
         nameTextbox.checkClick((short)e.getX(), (short)e.getY());
         fileList.checkHover((short)e.getX(), (short)e.getY());
         
         //Check for note placing/removal
         byte noteVal = (byte)(10 - 10 * (e.getY() - areaY * cg_Client.SCREEN_HEIGHT) / (areaH * cg_Client.SCREEN_HEIGHT));
         byte beatVal = (byte)(11 * (e.getX() - areaX * cg_Client.SCREEN_WIDTH) / (areaW * cg_Client.SCREEN_WIDTH));
         
         if(playStartTime == Long.MAX_VALUE && noteVal >= 0 && noteVal < 10 && beatVal >= 0 && beatVal < 11){
            //Placing note
            if(e.getButton() == MouseEvent.BUTTON1){
               if(!song.get(instrument).containsKey((short)(pageSlider.getValue() * 11 + beatVal)))
                  song.get(instrument).put((short)(pageSlider.getValue() * 11 + beatVal), new HashSet<Byte>());
               
               //Play note
               if(!song.get(instrument).get((short)(pageSlider.getValue() * 11 + beatVal)).contains(noteVal)){
                  //Normie instrument
                  if(instrument != util_Music.DRUMS){
                     byte octaves = (byte)(noteVal / util_Music.INTERVALS[scale].length);
                     cg_MIDI.playNote((byte)(util_Music.INTERVALS[scale][noteVal % util_Music.INTERVALS[scale].length] + 60 + octaves * 12 + key), instrument);
                  
                  //Percussin'
                  }else{
                     if(noteVal == 3 || noteVal == 5 || noteVal == 7)
                        cg_MIDI.playNote((byte)(percussionInstrumentValues[(noteVal - 3) / 2]), instrument);
                  }
               }
               
               if(instrument != util_Music.DRUMS || (noteVal == 3 || noteVal == 5 || noteVal == 7))
                  song.get(instrument).get((short)(pageSlider.getValue() * 11 + beatVal)).add((byte)(noteVal));
            
            //Removing note
            }else if(e.getButton() == MouseEvent.BUTTON3){
               if(song.get(instrument).containsKey((short)(pageSlider.getValue() * 11 + beatVal)))
                  song.get(instrument).get((short)(pageSlider.getValue() * 11 + beatVal)).remove(noteVal);
            }
         }
      }
      cg_Client.frame.revalidate();
   }
   
   public void mouseEntered(MouseEvent e){}
   
   public void mouseExited(MouseEvent e){}
   
   public void mousePressed(MouseEvent e){
      if(playStartTime == Long.MAX_VALUE){
         bpmSlider.checkPress((short)e.getX(), (short)e.getY());
         pageSlider.checkPress((short)e.getX(), (short)e.getY());
      }
   }
   
   public void mouseReleased(MouseEvent e){
      bpmSlider.release();
      pageSlider.release();
      
      //Extend number of pages if maximum reached
      if(pageSlider.getValue() == pageSlider.getMaximum()){
         pageSlider.setMaximum((short)(pageSlider.getMaximum() + 2));
         pageSlider.setValue((short)(pageSlider.getMaximum() - 2));
      }
   }
   
   public void mouseMoved(MouseEvent e){
      super.mouseMoved(e);
   }
   
   public void mouseDragged(MouseEvent e){
      bpmSlider.checkDrag((short)e.getX());
      pageSlider.checkDrag((short)e.getX());
   }
   
   public void keyPressed(KeyEvent e){
      if(nameTextbox.isSelected()){
         nameTextbox.keyPressed(e);
      }else{
         //Lower key
         if(e.getKeyCode() == KeyEvent.VK_MINUS){
            if(playStartTime == Long.MAX_VALUE){
               key--;
               if(key < 0)
                  key = (byte)(keys.length - 1);
            }
         
         //Raise key
         }else if(e.getKeyCode() == KeyEvent.VK_EQUALS){
            if(playStartTime == Long.MAX_VALUE){
               key = (byte)((key + 1) % keys.length);
            }
         
         //Cycle left through scales
         }else if(e.getKeyCode() == KeyEvent.VK_COMMA){
            if(playStartTime == Long.MAX_VALUE){
               scale--;
               if(scale < 0)
                  scale = (byte)(scales.length - 1);
            }
         
         //Cycle right through scales
         }else if(e.getKeyCode() == KeyEvent.VK_PERIOD){
            if(playStartTime == Long.MAX_VALUE){
               scale = (byte)((scale + 1) % scales.length);
            }
         
         //Cycle left through instruments
         }else if(e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET){
            instrument--;
            if(instrument < 0)
               instrument = (byte)(util_Music.instruments.length - 1);
         
         //Cycle right through instruments
         }else if(e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET){
            instrument = (byte)((instrument + 1) % util_Music.instruments.length);
         
         //Play song preview
         }else if(e.getKeyCode() == KeyEvent.VK_SPACE){
            if(playStartTime == Long.MAX_VALUE){
               //Visual markers
               playStartTime = System.currentTimeMillis();
               playStartPage = pageSlider.getValue();
            }else{
               playStartTime = Long.MAX_VALUE;
            }
            cg_MIDI.silence();
            for(HashSet<Byte> noteBuffer : currNotes)
               noteBuffer.clear();
         }
      }
   }
   
   public void keyReleased(KeyEvent e){}
   
   public void keyTyped(KeyEvent e){}
   
   public void mouseWheelMoved(MouseWheelEvent e){
      //Scroll through table
      fileList.checkScroll(
         (short)e.getX(),
         (short)e.getY(),
         (byte)e.getWheelRotation()
      );
   }
   
   //Save current song to file
   private void saveToFile() throws IOException{
      File songFile = new File(util_Utilities.getDirectory() + "/resources/songs/" + nameTextbox.getContents() + ".cfg");
      
      //Delete old song file
      if(songFile.exists()){
         songFile.delete();
         songFile = new File(util_Utilities.getDirectory() + "/resources/songs/" + nameTextbox.getContents() + ".cfg");
      }
      
      PrintWriter output = new PrintWriter(songFile);
      
      //Calculate approximate number of lines required in file
      int numLines = 0;
      for(byte i = 0; i < util_Music.NUM_INSTRUMENTS; i++)
         numLines += song.get(i).size();
      ArrayList<String> toWrite = new ArrayList<>(numLines + 9);
      
      //Track song difficulty parameters
      int numChordBeats = 0; //Number of beats that have a chord
      toWrite.add(""); //Difficulty value will go here
      
      short length = (short)(pageSlider.getMaximum() * 11 / (bpmSlider.getValue() / 60.0)); //in seconds
      toWrite.add(length / 60 + " " + length % 60);
      
      toWrite.add(bpmSlider.getValue() + " " + scale + " " + (key + 60));
      
      //Fill string buffer
      for(byte instrument = 0; instrument < util_Music.NUM_INSTRUMENTS; instrument++){
         for(Short beat : song.get(instrument).keySet()){
            if(song.get(instrument).get(beat).isEmpty())
               continue;
            else{ //Difficulty parameters
               if(song.get(instrument).get(beat).size() > 1)
                  numChordBeats++;
            }
            
            //Concatenate beat's notes
            String line = beat + "";
            for(Byte note : song.get(instrument).get(beat)){
               if(instrument != util_Music.DRUMS){
                  line += " " + (note + 60);
               }else{
                  line += " " + (percussionInstrumentValues[note / 2 - 1]);
               }
            }
            toWrite.add(line);
         }
         toWrite.add("");
      }
      
      //Calculate and add difficulty value to buffer
      byte noteDensity = (byte)(100 * numLines / (66.0 * pageSlider.getMaximum()));
      byte chordDensity = (byte)(100.0 * numChordBeats / numLines);
      final byte difficulty;
      
      if(noteDensity < 20){
         if(chordDensity < 5)
            difficulty = 0;
         else
            difficulty = 1;
      
      }else if(noteDensity < 50){
         if(chordDensity < 3)
            difficulty = 1;
         else
            difficulty = 2;
      
      }else if(noteDensity < 75){
         if(chordDensity < 2)
            difficulty = 3;
         else
            difficulty = 4;
      
      }else{
         difficulty = 4;
      }
      toWrite.set(0, difficulty + "");
      
      //Actually writing stuff
      for(String line : toWrite)
         output.println(line);
      output.close();
   }
}