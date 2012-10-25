import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import processing.pdf.*; 
import java.awt.image.BufferedImage; 
import javax.imageio.ImageIO; 
import java.awt.color.ColorSpace; 
import toxi.color.*; 
import toxi.color.theory.*; 
import toxi.math.*; 
import java.util.List; 
import guicomponents.*; 

import guicomponents.*; 
import org.monte.cmykdemo.*; 
import org.monte.media.jpeg.*; 
import org.monte.media.io.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Stitch_n_Glitch extends PApplet {

/**********************************************************************************************************************
*    "Stitch'n'Glitch" by Andrew Healy.                                                                               * 
*    Image corruption and processing to produce cross-stitch patterns and other export options                        *             *
*    Copyright (C) 2012  Andrew Healy                                                                                 *
*                                                                                                                     *
*    This program is free software: you can redistribute it and/or modify                                             *
*    it under the terms of the GNU General Public License as published by                                             *
*    the Free Software Foundation, either version 3 of the License, or                                                *
*    (at your option) any later version.                                                                              *
*                                                                                                                     *
*    This program is distributed in the hope that it will be useful,                                                  *
*    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                   *
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                    *
*    GNU General Public License for more details.                                                                     *
*                                                                                                                     *
*    You should have received a copy of the GNU General Public License                                                *
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.                                            *
*                                                                                                                     *
*    Author: Andrew Healy. Website: http://www.andrewhealy.com Email: werdnah19@gmail.com                             *
**********************************************************************************************************************/











PGraphicsPDF colsNeeded;
PGraphicsPDF pdfPattern;
Histogram hist1, hist2;
PShape[]symbols = new PShape [447];
HashMap allColors; //
HashMap skeins = new HashMap(); //a container for all the flosses (color, name, count)
Floss[]flosses = new Floss[447];

File folder;
ColorList palette = new ColorList();
ColorList used = new ColorList();
int[] myColor = new int [447];
TColor col=TColor.BLACK.copy();
TColor liveCol = col;
TColor[]cols=new TColor[447];
boolean broken = false;
boolean showRight = false;
boolean drawing = false;
boolean inputSelected = false;
boolean imageLoaded = false;
boolean outputSelected = false;
boolean adjusted = false;
boolean glitched = false;
String loadpath, format, sameTime;
String outPath = "";
boolean landscape;
byte[] byteSize;
int byteLength;
//ImageIcon titlebaricon = new ImageIcon(loadBytes("logo128.png"));

ThreadGlitch glitchThis = new ThreadGlitch(this);
ThreadPattern makePattern = new ThreadPattern(this);
ThreadAdjust adjustThis = new ThreadAdjust(this);

//TColor[]cols=new TColor[447];

// G4P Buttons
GButton adjustPanel, adjustButton, chooseButton, resetButton, glitchButton, glitchPanel, exportPanel, exportButton;
GWSlider origColours, resizeImg, deadAmt, glitchAmt, patColours;
GCombo aida;
GCheckbox origDitherBW, glitchDitherBW, origDitherCol, glitchDitherCol;
GOptionGroup exportOptions, origDitherOps, glitchDitherOps ;
GOption asPDF, asCSV, asPat; //origDitherBW, origDitherCol, glitchDitherBW, glitchDitherCol;
GActivityBar actBar;
GLabel origLab, resizeLab, glitchLab, patLab, deadLab, aidaLab, pdfLab, csvLab, patternLab, ditherLab, statusLab, actLab; 

int palePink, warmGrey, niceBrown, buttonGreen, brightPink;

int pixelLength, imgWidth, imgHeight;
int numReplacements = 15;
int aidaCt = 14;
int margin = 50;

PImage img, workImg, corrupt, brokenimage, bg, noPNG, origImg, adjustImg;
PFont georgia = createFont("Georgia", 18, true);
PFont pdfFont = createFont("Baskerville", 12, true);


public void setup() {

  size(876, 584, JAVA2D);
  noStroke();
  smooth();

  //imageMode(CENTER);
  textFont(georgia);
  
  
  frame.setTitle("Stitch'n'Glitch");
  //frame.setIconImage(titlebaricon.getImage());

  palePink = color(255, 237, 251, 127);
  buttonGreen = color(55, 97, 39);
  warmGrey = color(39, 30, 17, 155);
  niceBrown = color(56, 27, 4);
  brightPink = color(222, 87, 205);

  selectInput("Select an image (RGB jpeg preferred)", "loadFile");
  //selectFolder("Choose a location to save Stitch'n'Glitch files:", "checkFolder");

  brokenimage = loadImage("brokenimage.png");
  noPNG = loadImage("pngPreview.png");
  bg = loadImage("bg.jpg");
  background(bg);
  allColors = new HashMap();

  String[]tempStrings = loadStrings("straightnumbers.txt");

  for (int i = 0; i < 447; i++) {

    String[]lineString = split(tempStrings[i], ',');

    myColor[i] = color(unhex(lineString[1]));

    cols[i]=col.newARGB(myColor[i]);
    //palette.add(col.newARGB(myColor[i]));
    palette.add(cols[i]);
    symbols[i]=loadShape("symbol"+i+".svg");

    flosses[i] = new Floss(lineString[0], myColor[i], symbols[i]);
    allColors.put(cols[i], flosses[i]);
  }

  GComponent.globalColor = GCScheme.getColor(this, 7);
  guicomponents.G4P.setMouseOverEnabled(true);

  createSliders();
  createAllButtons();
  createFooter();
  createCombo();
  createDitherBoxes();
  //createDitherOptions();
  createExportOptions();

  fill(warmGrey);
  rect(25, 0, width-51, 109);
}

public void loadNewFile(File selection) {

  if (selection == null) {
    imageLoaded = true;
    return;//cancelled
  }
  else {

    adjusted = false;
    println("User selected " + selection.getAbsolutePath());
    loadpath = selection.getAbsolutePath();
    String [] splits = split (selection.getAbsolutePath(), '/');
    String filename = splits[splits.length - 1];
    String [] getformat = split (filename, '.');
    format = getformat[getformat.length-1];
    println ("Filename: "+filename+" : Format: "+format);

    if (format.equalsIgnoreCase("png") || format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("gif") || format.equalsIgnoreCase("jpeg")) {


      if (isCMYK(loadpath)) {
        selectInput("Select an image (RGB jpeg preferred)", "loadNewFile");
      }
      else {
        img = loadImage(loadpath);
        imgWidth = img.width;
        imgHeight = img.height;
        pixelLength = img.pixels.length;
        if (imgWidth>imgHeight) {
          landscape = true;
        }
        else {
          landscape = false;
        }
        workImg=new PImage(imgWidth, imgHeight, ARGB);

        //img.save("./data/origImg."+format);
        byteSize = loadBytes(loadpath);
        byteLength = byteSize.length;
        //100000 || pixelLength > 200000
        if(byteLength > 90000 || pixelLength > 200000){
         
          statusLab.setText("This large image will take a long time to Stitch'n'Glitch");
          
        }
        
        inputSelected = true;
        imageLoaded = true;
        origColours.setValue(50);
        resizeImg.setValue(100);
        deadAmt.setValue(0);
        glitchAmt.setValue(15);
        patColours.setValue(50);
      }
    }
    else {//not an image
      selectInput("Select an image (RGB jpeg preferred)", "loadNewFile");
    } 

    showRight = false;
  }
}

public void loadFile(File selection) {

  if (selection == null) {

    println("Window was closed or the user hit cancel.");
    img = loadImage("origImg.jpg");
    format = "jpg";
    imgWidth = img.width;
    imgHeight = img.height;
    pixelLength = img.pixels.length;
    if (imgWidth>imgHeight) {
      landscape = true;
    }
    else {
      landscape = false;
    }
    workImg=new PImage(imgWidth, imgHeight, ARGB);

    inputSelected = false;
    imageLoaded = true;
  } 
  else {

    println("User selected " + selection.getAbsolutePath());
    loadpath = selection.getAbsolutePath();
    String [] splits = split (selection.getAbsolutePath(), '/');
    String filename = splits[splits.length - 1];
    String [] getformat = split (filename, '.');
    format = getformat[getformat.length-1];
    println ("Filename: "+filename+" : Format: "+format);

    if (format.equalsIgnoreCase("png") || format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("gif") || format.equalsIgnoreCase("jpeg")) {


      if (isCMYK(loadpath)) {
        selectInput("Select an image (RGB jpeg preferred)", "loadFile");
      }
      else {
        img = loadImage(loadpath);
        imgWidth = img.width;
        imgHeight = img.height;
        pixelLength = img.pixels.length;
        if (imgWidth>imgHeight) {
          landscape = true;
        }
        else {
          landscape = false;
        }
        workImg=new PImage(imgWidth, imgHeight, ARGB);

        //img.save("./data/origImg."+format);
        byteSize = loadBytes(loadpath);
        byteLength = byteSize.length;
        //100000 || pixelLength > 200000
        if(byteLength > 90000 || pixelLength > 200000){
         
          statusLab.setText("This large image will take a long time to Stitch'n'Glitch");
          
        }
        inputSelected = true;
        imageLoaded = true;
      }
    }
    else {//not an image
      selectInput("Select an image (RGB jpeg preferred)", "loadFile");
    }
  }
}

public void checkFolder(File folder) {

  if (folder == null) {

    selectFolder("Choose a location to save Stich'n'Glitch files:", "checkFolder");
  }
  else {

    outputSelected = true;    
    actLab.setText("Adjusting original image");
    actBar.start(0);
    actLab.setVisible(true);
    outPath = folder.getAbsolutePath();
    adjustThis.start();
  }
}

public void checkFolderAgain(File folder) {

  if (folder == null) {

    selectFolder("Choose a location to save Stitch'n'Glitch files:", "checkFolderAgain");
  }
  else {

    outputSelected = true;  
    outPath = folder.getAbsolutePath(); 

    if (adjusted) {

      actLab.setText("Glitching Image");
      actBar.start(0);
      actLab.setVisible(true);
      glitchThis.start();
    }
    else {

      adjustImg = loadImage(loadpath);
      adjustImg.save(outPath+"/applicationData/adjustedImg."+format);
      actLab.setText("Glitching Image");
      actBar.start(0);
      actLab.setVisible(true);
      glitchThis.start();
    }
  }
}


public void draw() {


  background(bg);

  fill(warmGrey);
  rect(25, 0, width-51, 109);
  fill(warmGrey);
  rect(0, height-25, width, 25);

  // if(ready){
  if (imageLoaded) {

    if (adjusted) {
      drawScaleImage(adjustImg, 25, 134, 400);
    }
    else {
      drawScaleImage(img, 25, 134, 400);
    }
  }


  if (showRight) {
    drawScaleImage(workImg, 450, 134, 400);
  }
  else {
    fill(palePink);
    rect(450, 134, 400, 400);
  }

  displayLabel(overImage());

  if (!adjustButton.isVisible()) {
    adjustPanel.setColours(niceBrown, color(70, 43, 22), brightPink);
  }
  else {
    adjustPanel.setColorScheme(7);
  }
  if (!glitchButton.isVisible()) {
    glitchPanel.setColours(niceBrown, color(70, 43, 22), brightPink);
  }
  else {
    glitchPanel.setColorScheme(7);
  }
  if (!exportButton.isVisible()) {
    exportPanel.setColours(niceBrown, color(70, 43, 22), brightPink);
  }
  else {
    exportPanel.setColorScheme(7);
  }

  // }
}

public void resizeForPattern(){
 
  byte[]b = loadBytes(outPath+"/applicationData/corruptImg."+format);
  int newWidth;
  int newHeight;
  ColorList some = new ColorList();
  TColor col=TColor.BLACK.copy();
  TColor liveCol = col; 
  skeins.clear();
  
  for (int i=0; i < allColors.size(); i++) {

      flosses[i].resetCount();
  }
  
 while (b.length > 90000 || pixelLength > 200000) {
    corrupt.resize(PApplet.parseInt(imgWidth-imgWidth/10.f), PApplet.parseInt(imgHeight-imgHeight/10.f));
    imgWidth = PApplet.parseInt(imgWidth-imgWidth/10.f);
    imgHeight = PApplet.parseInt(imgHeight-imgHeight/10.f);
    pixelLength = imgWidth*imgHeight;
    println("New pixelLength: "+pixelLength);
    corrupt.save(outPath+"/applicationData/corruptImg."+format);
    b = loadBytes(outPath+"/applicationData/corruptImg."+format);
    println("New byte length: "+b.length);
  }

  
  newWidth = imgWidth;
  newHeight = imgHeight;
  pixelLength = newWidth*newHeight;
  statusLab.setText("Resized image to "+newWidth+" x "+newHeight+" (pixels)");
  corrupt.loadPixels();
  workImg.loadPixels();


  for (int j = 0; j < pixelLength; j++) {

    int blah = corrupt.pixels[j];
    liveCol = col.newARGB(blah);
    some = palette.sortByProximityTo(liveCol, false);
    liveCol = some.get(0);
    //used.add(liveCol);
    blah = liveCol.toARGB();
    String temp = liveCol.toString();
    // println("working key: "+temp);
    if (skeins.containsKey(blah)) {
      Floss f = (Floss) skeins.get(blah);
      f.count();
    }
    else {
      Floss f = (Floss) allColors.get(liveCol);
      skeins.put(blah, f);
    }
    workImg.pixels[j] = blah;//liveCol.toARGB();
  }


  workImg.updatePixels();
  corrupt.updatePixels();

}


public void drawScaleImage(PImage pic, int tl_x, int tl_y, int max_dem) {

  float ratio;

  fill(palePink);
  rect(tl_x, tl_y, max_dem, max_dem);


  if (pic.width>0 && pic.height>0) {

  if (broken) {

      try {

        if (pic.width > pic.height) { //A landscape

          ratio = PApplet.parseFloat(max_dem)/PApplet.parseFloat(pic.width);
          int imgY = PApplet.parseInt(tl_y + ((max_dem/2) - (PApplet.parseFloat(pic.height)*ratio)/2.0f));
          copy(pic, 0, 0, pic.width, pic.height, tl_x, imgY, max_dem, PApplet.parseInt(PApplet.parseFloat(pic.height)*ratio));
        }
        else { // A portrait or square

            ratio = PApplet.parseFloat(max_dem)/PApplet.parseFloat(pic.height);
          int imgX = tl_x + PApplet.parseInt((max_dem/2) - ((PApplet.parseFloat(pic.width)*ratio)/2.0f));
          copy(pic, 0, 0, pic.width, pic.height, imgX, tl_y, PApplet.parseInt(PApplet.parseFloat(pic.width)*ratio), max_dem);
        }
      }
      catch(Exception e) {

        println("Exception caught in method: "+e);
        broken = true;
        imageMode(CENTER);
        image(brokenimage, tl_x+(max_dem/2), tl_y+(max_dem/2)); 
        imageMode(CORNER);
      }

      imageMode(CENTER);
      image(brokenimage, width-225, height-250); 
      imageMode(CORNER);
    }
    else {

      try {

        if (pic.width > pic.height) { //A landscape

          ratio = PApplet.parseFloat(max_dem)/PApplet.parseFloat(pic.width);
          int imgY = PApplet.parseInt(tl_y + ((max_dem/2) - (PApplet.parseFloat(pic.height)*ratio)/2.0f));
          copy(pic, 0, 0, pic.width, pic.height, tl_x, imgY, max_dem, PApplet.parseInt(PApplet.parseFloat(pic.height)*ratio));
        }
        else { // A portrait or square

            ratio = PApplet.parseFloat(max_dem)/PApplet.parseFloat(pic.height);
          int imgX = tl_x + PApplet.parseInt((max_dem/2) - ((PApplet.parseFloat(pic.width)*ratio)/2.0f));
          copy(pic, 0, 0, pic.width, pic.height, imgX, tl_y, PApplet.parseInt(PApplet.parseFloat(pic.width)*ratio), max_dem);
        }

        //broken = false;
      }
      catch(Exception e) {

        println("Exception caught in method: "+e);
        broken = true;
        imageMode(CENTER);
        image(brokenimage, tl_x+(max_dem/2), tl_y+(max_dem/2)); 
        imageMode(CORNER);
      }
    }
  }
  else {

    broken = true;
    imageMode(CENTER);
    image(brokenimage, tl_x+(max_dem/2), tl_y+(max_dem/2)); 
    imageMode(CORNER);
  }
}



public boolean overImage() {

  if (mouseY > 134 && mouseY < height-50) {

    if ((mouseX > 25 && mouseX < 425) || (mouseX > 450 && mouseX < width-25)) {

      return true;
    }
    else {
      return false;
    }
  }
  else {

    return false;
  }
}

public void displayLabel(boolean over) {

  if (!actBar.isVisible() && !makePattern.isRunning() && !adjustThis.isRunning() && !glitchThis.isRunning()) {

    try {
      if (overImage()) {

        int mouseC;
        ColorList sorted = new ColorList();
        mouseC = get(mouseX, mouseY);
        liveCol = col.newARGB(mouseC);
        sorted = palette.sortByProximityTo(liveCol, false);
        liveCol = sorted.get(0);
        textFont(georgia);
        Floss f = (Floss) allColors.get(liveCol);

        fill(255);
        rect(mouseX+10, mouseY, 45, 20);
        fill(niceBrown);
        text(f.identifier, mouseX+10, mouseY+15);
      }
    }
    catch(Exception e) {

      println("Exception caught");
    }
  }
}

public void saveCSVfile() {

  String time = str(hour())+str(minute());
  PrintWriter output = createWriter(outPath+"/colourData_"+time+".csv");
  int x_pos = 0;


  workImg.loadPixels();

  for (int i = 0; i < pixelLength; i++) {

    String temp = hex(workImg.pixels[i]);

    if (x_pos>imgWidth) {
      output.println(temp+",");
      x_pos = 0;
    }
    else {
      output.print(temp+",");
    }

    x_pos++;
  }

  workImg.updatePixels();
  output.close();
  String[]folders = split(outPath, "/");
  statusLab.setText("file saved at .../"+folders[folders.length-2]+"/"+folders[folders.length-1]+"/colourData_"+time+".csv");
  actBar.stop();
  actLab.setVisible(false);
}

public void savePDFimage() {

  //TColor c = TColor.BLACK.copy(); 
  //color tempCol;
  int margin = 50; 
  String time = str(hour())+str(minute());

  //String extension = new String ("Stitch_n_Glitch-"+month()+day()+minute());
  PGraphicsPDF pdf = (PGraphicsPDF) createGraphics((margin*2)+imgWidth, (margin*2)+imgHeight, PDF, outPath+"/image_"+time+".pdf"); 

  int my_x = margin;
  int my_y = margin; 

  workImg.loadPixels();

  pdf.beginDraw();
  pdf.smooth();
  pdf.noStroke();

  for (int i=0; i< pixelLength; i++) {

    int tempCol = workImg.pixels[i];
    pdf.fill(tempCol);
    pdf.rect(my_x, my_y, 1, 1);

    my_x = my_x+1;

    if (my_x > (pdf.width-51)) {

      my_x=margin;
      my_y=my_y+1;
    }
  }

  workImg.updatePixels();

  pdf.dispose();
  pdf.endDraw();
  String[]folders = split(outPath, "/");
  statusLab.setText("Image PDF saved at .../"+folders[folders.length-2]+"/"+folders[folders.length-1]+"/image_"+time+".pdf");
  actBar.stop();
  actLab.setVisible(false);
}



/*********Controller Methods************************/


//PANEL BUTTONS
public void createAllButtons() {

  adjustPanel = new GButton(this, "Adjust Image", 25, 5, 125, 25);
  adjustPanel.setBorder(0);
  resetButton = new GButton(this, "Reset", width-150, 40, 124, 25);
  resetButton.setVisible(false);
  resetButton.setBorder(0);
  glitchButton = new GButton(this, "Glitch", width-150, 75, 124, 25);
  glitchButton.setVisible(false);
  glitchButton.setBorder(0);
  glitchButton.fireAllEvents(true);
  glitchButton.setColours(brightPink, color(228, 118, 214), niceBrown);
  glitchPanel = new GButton(this, "Glitch Image", 25, 40, 125, 25);
  glitchPanel.setBorder(0);
  exportPanel = new GButton(this, "Export File", 25, 75, 125, 25);
  exportPanel.setBorder(0);
  exportButton = new GButton(this, "Export", width-150, 75, 124, 25);
  exportButton.setVisible(false);
  exportButton.setBorder(0);
  exportButton.setColours(brightPink, color(228, 118, 214), niceBrown);
  adjustButton = new GButton(this, "Adjust", width-150, 75, 124, 25);
  adjustButton.setVisible(false);
  adjustButton.setBorder(0);
  adjustButton.setColours(brightPink, color(228, 118, 214), niceBrown);
  chooseButton = new GButton(this, "Load new", width-150, 5, 124, 25);
  chooseButton.setVisible(false);
  chooseButton.setBorder(0);
}

public void createSliders() {

  origColours = new GWSlider(this, "SnGsliders11px", 247, 40, 178);
  origColours.setLimits(50, 50, 1);
  origColours.setVisible(false);
  origLab = new GLabel(this, "Reduce colours"+ "\n"+"in original", 153, 20, 100, 35);
  origLab.setVisible(false);

  resizeImg = new GWSlider(this, "SnGsliders11px", 525, 40, 178);
  resizeImg.setLimits(100, 1, 100);
  resizeImg.setVisible(false);
  resizeLab = new GLabel(this, "Resize Original", 435, 25, 100, 35);
  resizeLab.setVisible(false);

  patColours = new GWSlider(this, "SnGsliders11px", 247, 24, 178);
  patColours.setLimits(50, 50, 1);
  patColours.setVisible(false);
  patLab = new GLabel(this, "Reduce colours"+ "\n"+"in glitched image", 153, 0, 100, 35);
  patLab.setVisible(false);

  glitchAmt = new GWSlider(this, "SnGsliders11px", 247, 76, 178);
  glitchAmt.setLimits(15, 0, 30);
  glitchAmt.setVisible(false);
  glitchLab = new GLabel(this, "Glitch amount", 160, 58, 100, 35);
  glitchLab.setVisible(false);

  deadAmt = new GWSlider(this, "SnGsliders11px", 525, 76, 178);
  deadAmt.setLimits(0, 0, 100);
  deadAmt.setVisible(false);
  deadLab = new GLabel(this, "Add noise (%)", 438, 60, 100, 35);
  deadLab.setVisible(false);
}

public void createFooter() {

  statusLab = new GLabel(this, "", 25, height-25, 800);
  statusLab.setFont("georgia", 18);
  actBar = new GActivityBar(this, width-125, height-22, 100, 20);
  //actBar.start(0);
  actLab = new GLabel(this, "Glitcting in progress", width-350, height-22, 200);
  actLab.setTextAlign(GAlign.RIGHT);
  actLab.setVisible(false);
}

public void createCombo() {

  String [] counts = new String[] {
    "9 ct", "11 ct", "14 ct", "16 ct", "18 ct", "20 ct"
  };
  aida = new GCombo(this, counts, 6, 550, 20, 100 );
  aida.setSelected(2);
  aida.setColorScheme(GCScheme.GREY_SCHEME);
  aida.setVisible(false);
  aidaLab = new GLabel(this, "Fabric Count", 470, 20, 100 );
  aidaLab.setVisible(false);
}

public void createExportOptions() {

  exportOptions = new GOptionGroup();
  asPDF = new GOption(this, "Export as PDF image (good for large scale printing)", 230, 25, 400);
  asPDF.setFont("SansSerif", 13);
  asPDF.setVisible(false);
  exportOptions.addOption(asPDF);
  asCSV = new GOption(this, "Export as .csv (comma-separated values) for data visualisation", 230, 50, 400);
  asCSV.setFont("SansSerif", 13);
  asCSV.setVisible(false);
  exportOptions.addOption(asCSV);
  asPat = new GOption(this, "Export pattern for cross-stitching (large images may be scaled)", 230, 75, 400);
  exportOptions.addOption(asPat);
  asPat.setFont("SansSerif", 13);
  asPat.setVisible(false);
  exportOptions.setSelected(asPDF);
}

public void createDitherBoxes() {

  //GCheckbox origDither, patDither, origDitherCol, patDitherCol;
  origDitherBW = new GCheckbox(this, "Dither Original (B&W)", 450, 80, 200);
  origDitherCol = new GCheckbox(this, "Dither Original (Colour)", 247, 80, 200);
  glitchDitherBW = new GCheckbox(this, "Dither Glitch (B&W)", width-200, 20, 200);
  glitchDitherCol = new GCheckbox(this, "Dither Glitch (Colour)", width-200, 40, 200);
  origDitherBW.setVisible(false);
  origDitherCol.setVisible(false);
  glitchDitherBW.setVisible(false);
  glitchDitherCol.setVisible(false);
}


public boolean isPanelVisible(GButton button) {
  if (adjustPanel == button) {
    if (adjustButton.isVisible() && resetButton.isVisible()) {
      return true;
    }
    else {
      return false;
    }
  }
  else if (glitchPanel == button) {
    if (glitchButton.isVisible()) {
      return true;
    }
    else {
      return false;
    }
  }
  else if (exportPanel == button) {
    if (exportButton.isVisible()) {
      return true;
    }
    else {
      return false;
    }
  }
  else {
    return false;
  }
}


public void changeVisibility(GButton button) {
  if (adjustPanel == button) {
    adjustButton.setVisible(!adjustButton.isVisible());
    resetButton.setVisible(!resetButton.isVisible());
    origColours.setVisible(!origColours.isVisible());
    origLab.setVisible(!origLab.isVisible());
    resizeImg.setVisible(!resizeImg.isVisible());
    resizeLab.setVisible(!resizeLab.isVisible());
    origDitherBW.setVisible(!origDitherBW.isVisible());
    origDitherCol.setVisible(!origDitherCol.isVisible());
    chooseButton.setVisible(!chooseButton.isVisible());
  }
  else if (glitchPanel == button) {
    glitchButton.setVisible(!glitchButton.isVisible());
    glitchAmt.setVisible(!glitchAmt.isVisible());
    deadAmt.setVisible(!deadAmt.isVisible());
    deadLab.setVisible(!deadLab.isVisible());
    glitchLab.setVisible(!glitchLab.isVisible());
    patColours.setVisible(!patColours.isVisible());
    patLab.setVisible(!patLab.isVisible());
    aida.setVisible(!aida.isVisible());
    aidaLab.setVisible(!aidaLab.isVisible());
    glitchDitherBW.setVisible(!glitchDitherBW.isVisible());
    glitchDitherCol.setVisible(!glitchDitherCol.isVisible());
  }
  else if (exportPanel == button) {
    exportButton.setVisible(!exportButton.isVisible());
    asPat.setVisible(!asPat.isVisible());
    asPDF.setVisible(!asPDF.isVisible());
    asCSV.setVisible(!asCSV.isVisible());
  }
}

public void handleComboEvents(GCombo combo) {

  // "9 ct", "11 ct", "14 ct", "16 ct", "18 ct", "20 ct"
  if (aida == combo) { 

    println(aida.selectedIndex());
    if (aida.selectedIndex() == 0) {
      aidaCt = 9;
    }
    else if (aida.selectedIndex() == 1) {
      aidaCt = 11;
    }
    else if (aida.selectedIndex() == 2) {
      aidaCt = 14;
    }
    else if (aida.selectedIndex() == 3) {
      aidaCt = 16;
    }
    else if (aida.selectedIndex() == 4) {
      aidaCt = 18;
    }
    else if (aida.selectedIndex() == 5) {
      aidaCt = 20;
    }
  }
}

public void handleButtonEvents(GButton button) {

  drawing = true;

  if (!actBar.isVisible()) {

    if (adjustPanel == button && button.eventType == GButton.CLICKED) {
      if (!isPanelVisible(adjustPanel)) {
        changeVisibility(adjustPanel);
      } 
      if (isPanelVisible(glitchPanel)) {
        changeVisibility(glitchPanel);
      }
      if (isPanelVisible(exportPanel)) {
        changeVisibility(exportPanel);
      }
    }
    else if (glitchPanel == button && button.eventType == GButton.CLICKED) {
      if (!isPanelVisible(glitchPanel)) {
        changeVisibility(glitchPanel);
      } 
      if (isPanelVisible(adjustPanel)) {
        changeVisibility(adjustPanel);
      }
      if (isPanelVisible(exportPanel)) {
        changeVisibility(exportPanel);
      }
    }
    else if (exportPanel == button && button.eventType == GButton.CLICKED) {
      if (!isPanelVisible(exportPanel)) {
        changeVisibility(exportPanel);
      } 
      if (isPanelVisible(glitchPanel)) {
        changeVisibility(glitchPanel);
      }
      if (isPanelVisible(adjustPanel)) {
        changeVisibility(adjustPanel);
      }
    }
    else if (glitchButton == button && button.eventType == GButton.CLICKED) {

      broken = false;

      if (!outputSelected) {

        selectFolder("Choose a location to save Stitch'n'Glitch files:", "checkFolderAgain");
      }
      else {

        if (adjusted) {

          actLab.setText("Glitching Image");
          actBar.start(0);
          actLab.setVisible(true);
          glitchThis.start();
        }
        else {

          adjustImg = loadImage(loadpath);
          actLab.setText("Glitching Image");
          actBar.start(0);
          actLab.setVisible(true);
          glitchThis.start();
        }
      }
    }
    else if (exportButton == button && button.eventType == GButton.CLICKED) {

      byte[]b = loadBytes(outPath+"/applicationData/corruptImg."+format);
      
      if (glitched && !broken) {

        if (asPDF.isSelected()) {
          actLab.setText("Creating PDF image");
          actBar.start(0);
          actLab.setVisible(true);
          thread("savePDFimage");
          //thread("savePointImage");
        }
        else if (asCSV.isSelected()) {
          actLab.setText("Creating .csv file");
          actBar.start(0);
          actLab.setVisible(true);
          thread("saveCSVfile");
        }
        else if (asPat.isSelected()) {
          sameTime = new String(str(hour())+str(minute()));
          actBar.start(0);
          actLab.setVisible(true);
          if (b.length > 100000 || pixelLength > 200000) {
            
            actLab.setText("Resizing image");
            resizeForPattern();
            
          }
          colsNeeded = (PGraphicsPDF) createGraphics((margin*2)+450, (margin*2)+((skeins.size()/2)*35), PDF, outPath+"/SnG"+day()+month()+"/Colour_Chart_"+sameTime+".pdf");
          pdfPattern = (PGraphicsPDF) createGraphics((margin*2)+(imgWidth*10), (margin*2)+(imgHeight*10), PDF, outPath+"/SnG"+day()+month()+"/PDFpattern_"+sameTime+".pdf");
          actLab.setText("Creating Reference Image");
          
          makePattern.start();
        }
      }
      else {

        statusLab.setText("You must Glitch before you can Stitch...");
      }
      //
    }
    else if (adjustButton == button && button.eventType == GButton.CLICKED) {

      if (!outputSelected) {

        selectFolder("Choose a location to save Stitch'n'Glitch files:", "checkFolder");
      }
      else {

        actLab.setText("Adjusting original image");
        actBar.start(0);
        actLab.setVisible(true);
        adjustThis.start();
      }
    }
    else if (resetButton == button && button.eventType == GButton.CLICKED) {

      if (inputSelected) {

        img = loadImage(loadpath);
      }
      else {

        img = loadImage("origImg.jpg");
      }

      imgWidth = img.width;
      imgHeight = img.height;
      pixelLength = img.pixels.length;
      //workImg.resize(imgWidth,imgHeight);
      workImg = new PImage(imgWidth, imgHeight, ARGB);
      byteSize = loadBytes(loadpath);
      byteLength = byteSize.length;
        //100000 || pixelLength > 200000
      if(byteLength > 90000 || pixelLength > 200000){
         
        statusLab.setText("This large image will take a long time to Stitch'n'Glitch");
          
      }
      inputSelected = true;
      imageLoaded = true;
      
      adjusted = false;
      showRight = false;
      origColours.setValue(50);
      resizeImg.setValue(100);
      deadAmt.setValue(0);
      glitchAmt.setValue(15);
      patColours.setValue(50);
    }
    else if (chooseButton == button && button.eventType == GButton.CLICKED) {
      imageLoaded = false;
      selectInput("Select an image (RGB jpeg preferred)", "loadNewFile");
    }
  }
  else {

    statusLab.setText("Please wait for the current process to finish");
  }
}

public void handleCheckboxEvents(GCheckbox checkbox) {


  if (origDitherCol == checkbox && origDitherBW.isSelected()) {

    origDitherBW.setSelected(false);
  }
  else if (origDitherBW == checkbox && origDitherCol.isSelected()) {

    origDitherCol.setSelected(false);
  }
  else if (glitchDitherCol == checkbox && glitchDitherBW.isSelected()) {

    glitchDitherBW.setSelected(false);
  }
  else if (glitchDitherBW == checkbox && glitchDitherCol.isSelected()) {

    glitchDitherCol.setSelected(false);
  }
}

public void handleSliderEvents(GSlider slider) {
  if (resizeImg == slider) {

    int newWidth = PApplet.parseInt(imgWidth*(resizeImg.getValue()/100.0f));
    int newHeight = PApplet.parseInt(imgHeight*(resizeImg.getValue()/100.0f));

    statusLab.setText("Resize by "+resizeImg.getValue()+"% from "+imgWidth+" x "+imgHeight+" to "+newWidth+" x "+newHeight+" (pixels)");
  }
  else if (glitchAmt == slider) {
    if (format.equals("png")) {
      statusLab.setText("PNG files will not be glitched...");
    }
  }
}


// Stuff for CMYK images

private static boolean isCMYK(String fileName) {
  boolean result = false;
  BufferedImage img = null;
  File f = new File(fileName);
  if (f.exists()) {
    try {
      img = ImageIO.read(f);
    } 
    catch (IOException e) {
      // System.out.println(e.getMessage() + ": " + filename);
      return true;
    }
  } 
  else {
    System.out.println(fileName + " does not exist");
  }
  if (img != null) {
    int colorSpaceType = img.getColorModel().getColorSpace().getType();
    result = colorSpaceType == ColorSpace.TYPE_CMYK;
  }
  return result;
}

/**********************************************************************************************************************
*    "Stitch'n'Glitch" by Andrew Healy.                                                                               * 
*    Image corruption and processing to produce cross-stitch patterns and other export options                        *             *
*    Copyright (C) 2012  Andrew Healy                                                                                 *
*                                                                                                                     *
*    This program is free software: you can redistribute it and/or modify                                             *
*    it under the terms of the GNU General Public License as published by                                             *
*    the Free Software Foundation, either version 3 of the License, or                                                *
*    (at your option) any later version.                                                                              *
*                                                                                                                     *
*    This program is distributed in the hope that it will be useful,                                                  *
*    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                   *
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                    *
*    GNU General Public License for more details.                                                                     *
*                                                                                                                     *
*    You should have received a copy of the GNU General Public License                                                *
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.                                            *
*                                                                                                                     *
*    Author: Andrew Healy. Website: http://www.andrewhealy.com Email: werdnah19@gmail.com                             *
**********************************************************************************************************************/

class Floss{
  
   int stitches; 
   String identifier;
   int DMCcolor;
   PShape symbol;


  
  //Constructor will also take a PShape argument
  Floss (String temp_id, int temp_DMC,  PShape temp_sym){
    
    
    identifier = temp_id;
    DMCcolor = temp_DMC;
    stitches = 1;
    symbol = temp_sym;
    
  }  
  
  public void count(){
   
   stitches++; 
  }  
  
  public void resetCount(){
  
    stitches = 1;
  }
  
}
/**********************************************************************************************************************
*    "Stitch'n'Glitch" by Andrew Healy.                                                                               * 
*    Image corruption and processing to produce cross-stitch patterns and other export options                        *             *
*    Copyright (C) 2012  Andrew Healy                                                                                 *
*                                                                                                                     *
*    This program is free software: you can redistribute it and/or modify                                             *
*    it under the terms of the GNU General Public License as published by                                             *
*    the Free Software Foundation, either version 3 of the License, or                                                *
*    (at your option) any later version.                                                                              *
*                                                                                                                     *
*    This program is distributed in the hope that it will be useful,                                                  *
*    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                   *
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                    *
*    GNU General Public License for more details.                                                                     *
*                                                                                                                     *
*    You should have received a copy of the GNU General Public License                                                *
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.                                            *
*                                                                                                                     *
*    Author: Andrew Healy. Website: http://www.andrewhealy.com Email: werdnah19@gmail.com                             *
**********************************************************************************************************************/

public class ThreadAdjust implements Runnable {

  Thread thread;
  HashMap convien = new HashMap();
  boolean running = false;
  

  public ThreadAdjust(PApplet parent) {

    parent.registerDispose(this);
  } 

  public void start() {

    thread = new Thread(this);
    thread.start();
    running = true;
  }

  public void run() {

  
    //Adjust image here
    float tolerance = 0.5f - (origColours.getValue()/100.0f);
    convien.clear();
    
    //resize Image first for speed
      if(resizeImg.getValue() < 100){
        
        int newWidth = PApplet.parseInt(imgWidth*(resizeImg.getValue()/100.0f));
        int newHeight = PApplet.parseInt(imgHeight*(resizeImg.getValue()/100.0f));
        
        img.resize(newWidth, newHeight);
        workImg.resize(newWidth, newHeight);
        imgWidth = newWidth;
        imgHeight = newHeight;
        pixelLength = img.pixels.length;
        
      }
    
    img.loadPixels();  

      if(origDitherBW.isSelected() || origDitherCol.isSelected() ){
        
        //Dither code from http://www.geocities.jp/classiclll_newweb/DitherTest/applet/index.html
        
        int n;
        
        if(origColours.getValue() == 50){
         n = 5; 
        }else if (origColours.getValue() < 10){
         n = 0; 
        }else if (origColours.getValue() < 20){
         n = 1; 
        }else if (origColours.getValue() < 30){
         n = 2; 
        }else if (origColours.getValue() < 40){
         n = 3; 
        }else{
         n = 4; 
        }
        
        float f=255.f/(pow(2,2*n)+1);
        
        for (int x=0;x<imgWidth;x++) {  
          for (int y=0;y<imgHeight;y++) {  
      
            int c = img.get(x,y);
            float t=(n>0?dizza(x,y,n)*f:128);
            
            if(n == 5){//no dither
            
            }else{
              if(origDitherCol.isSelected()){ //colour
                
                int r=(t>=red(c)?0:255);
                int g=(t>=green(c)?0:255);
                int b=(t>=blue(c)?0:255);
                c=color(r,g,b);
                
              }else{ //B+W
                
                 c=color(t>=(red(c)+green(c)+blue(c))/3.f?0:255);
              }
              
              workImg.pixels[x+y*imgWidth] = c;   
            }
      
          }
        }
          
      }else if (origColours.getValue() < 50) {
          
          
        if(tolerance<0.025f){
         tolerance=0.025f; 
        }
        
          //img.filter(POSTERIZE,origColours.getValue());
          Histogram hist = Histogram.newFromARGBArray(img.pixels, pixelLength, tolerance, false);
          println("tolerance: "+tolerance);
            TColor c2=TColor.BLACK.copy();

          for (int i=0; i< pixelLength; i++) {

            c2.setARGB(img.pixels[i]);

            TColor closest=c2;
            float minD=1;
            for (HistEntry e : hist) {


              float d=c2.distanceToRGB(e.getColor());

              if (d<minD) {
                minD=d;
                closest=e.getColor();
                }
            }

            workImg.pixels[i]=closest.toARGB();
          
            }
      }else { //do not reduce colours

      }
    
      
      //Find Number of colours (for convienience)
      for (int j = 0; j < pixelLength; j++) {

        int blah = workImg.pixels[j];

        String temp = hex(blah);
        if (!convien.containsKey(temp)) {
          convien.put(temp, blah);
        }
      }
      
      

      println("Number of colours in adjusted: "+convien.size());
      img.updatePixels();
      workImg.updatePixels();
      workImg.save(outPath+"/applicationData/adjustedImg."+format);
      adjustImg = loadImage(outPath+"/applicationData/adjustedImg."+format);;

      statusLab.setText("Number of colours in adjusted image: "+convien.size()+" ("+imgWidth+" x "+imgHeight+" pixels)");
      actBar.stop();
      actLab.setVisible(false);
      adjusted = true;
      running = false;
    }
    
    public int dizza(int i, int j, int n) {
      if (n==1) {
        return (i%2!=j%2 ? 2 : 0) + j%2;
      }else{
        return 4*dizza(i%2, j%2, n-1) + dizza(PApplet.parseInt(i/2), PApplet.parseInt(j/2), n-1);
      }
    }
    
    public boolean isRunning(){
     
     if(running){
      
      return true;
     } else{
      return false; 
     }
      
    }

    public void stop() {

      thread = null;
    }

    public void dispose() {

      running = false;
      stop();
      
    }
  }

/**********************************************************************************************************************
*    "Stitch'n'Glitch" by Andrew Healy.                                                                               * 
*    Image corruption and processing to produce cross-stitch patterns and other export options                        *             *
*    Copyright (C) 2012  Andrew Healy                                                                                 *
*                                                                                                                     *
*    This program is free software: you can redistribute it and/or modify                                             *
*    it under the terms of the GNU General Public License as published by                                             *
*    the Free Software Foundation, either version 3 of the License, or                                                *
*    (at your option) any later version.                                                                              *
*                                                                                                                     *
*    This program is distributed in the hope that it will be useful,                                                  *
*    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                   *
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                    *
*    GNU General Public License for more details.                                                                     *
*                                                                                                                     *
*    You should have received a copy of the GNU General Public License                                                *
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.                                            *
*                                                                                                                     *
*    Author: Andrew Healy. Website: http://www.andrewhealy.com Email: werdnah19@gmail.com                             *
**********************************************************************************************************************/

public class ThreadGlitch implements Runnable {

  Thread thread;
  boolean running = false;

  public ThreadGlitch(PApplet parent) {

    parent.registerDispose(this);
  }

  public void start() {

    thread = new Thread(this);
    thread.start();
    running = true;
    
  }

  public void run() {

    
    workImg.loadPixels();
    //the main glitch code
    showRight = true;
    ColorList some = new ColorList();
    float tolerance = 0.5f - (patColours.getValue()/100.0f);
    println("allColors.size()= "+allColors.size());
    //reseting frequency of colours
    for (int i=0; i < allColors.size(); i++) {

      flosses[i].resetCount();
    }
    
    skeins.clear();
    //displayLabel(overImage());
    byte bits [] = loadBytes(outPath+"/applicationData/adjustedImg."+format);
    byte bCopy [] = new byte[bits.length];
    println("byte length: "+bits.length);
    arrayCopy(bits, bCopy);
    int scrambleStart = 10;
    int scrambleEnd = bits.length;
    int numReplacements = glitchAmt.getValue();
    println(numReplacements);
      
    if(format.equals("png")){
        
     numReplacements = 0; 
    }
    
    for (int i = 0; i < numReplacements; i++) {

      int PosA = PApplet.parseInt(random (scrambleStart, scrambleEnd));

      int PosB = PApplet.parseInt(random (scrambleStart, scrambleEnd));

      byte tmp = bCopy[PosA];

      bCopy[PosA] = bCopy[PosB];

      bCopy[PosB] = tmp;
    }

    saveBytes(outPath+"/applicationData/corruptImg."+format, bCopy);
    corrupt=loadImage(outPath+"/applicationData/corruptImg."+format);
    corrupt.loadPixels();
    
    
    try {
      
      if(glitchDitherBW.isSelected() || glitchDitherCol.isSelected() ){
        
        //Dither code from http://www.geocities.jp/classiclll_newweb/DitherTest/applet/index.html
        
        int n;
        
        if(patColours.getValue() == 50){
         n = 5; 
        }else if (patColours.getValue() < 10){
         n = 0; 
        }else if (patColours.getValue() < 20){
         n = 1; 
        }else if (patColours.getValue() < 30){
         n = 2; 
        }else if (patColours.getValue() < 40){
         n = 3; 
        }else{
         n = 4; 
        }
        
        float f=255.f/(pow(2,2*n)+1);
        
        for (int x=0;x<imgWidth;x++) {  
          for (int y=0;y<imgHeight;y++) {  
      
            int c = corrupt.get(x,y);
            float t=(n>0?dizza(x,y,n)*f:128);
            
            if(n == 5){//no dither

            
              
            }else{
              if(glitchDitherCol.isSelected()){ //colour
                
                int r=(t>=red(c)?0:255);
                int g=(t>=green(c)?0:255);
                int b=(t>=blue(c)?0:255);
                c=color(r,g,b);
                
              }else{ //B+W
                
                 c=color(t>=(red(c)+green(c)+blue(c))/3.f?0:255);
              }
              
              corrupt.pixels[x+y*imgWidth] = c;   
            }
      
          }
        }
          
      

    }else if (patColours.getValue() < 50) {
        
        if(tolerance<0.025f){
         tolerance=0.025f; 
        }
        
        //corrupt.filter(POSTERIZE, patColours.getValue());
         Histogram hist = Histogram.newFromARGBArray(corrupt.pixels, pixelLength, tolerance, false);
          println("tolerance: "+tolerance);
            TColor c2=TColor.BLACK.copy();

          for (int i=0; i< pixelLength; i++) {

            c2.setARGB(corrupt.pixels[i]);

            TColor closest=c2;
            float minD=1;
            for (HistEntry e : hist) {


              float d=c2.distanceToRGB(e.getColor());

              if (d<minD) {
                minD=d;
                closest=e.getColor();
              }
            }

            corrupt.pixels[i]=closest.toARGB();
          }
        
        
      }else if(glitchDitherBW.isSelected()){ //do not reduce colours
 
               
      }
      
    //Dead Pixels
    int deadPixels = PApplet.parseInt(pixelLength*(deadAmt.getValue()/100.0f));
    println(deadPixels);



      
      for (int d = 0; d < deadPixels; d++) {

      corrupt.pixels[PApplet.parseInt(random(pixelLength))] = color(128);
      }

      for (int j = 0; j < pixelLength; j++) {

        int blah = corrupt.pixels[j];
        liveCol = col.newARGB(blah);
        some = palette.sortByProximityTo(liveCol, false);
        liveCol = some.get(0);
        //used.add(liveCol);
        blah = liveCol.toARGB();
        String temp = liveCol.toString();
       // println("working key: "+temp);
        if (skeins.containsKey(blah)) {
          Floss f = (Floss) skeins.get(blah);
          f.count();
        }
        else {
          Floss f = (Floss) allColors.get(liveCol);
          skeins.put(blah, f);
        }
        workImg.pixels[j] = blah;//liveCol.toARGB();
      }


      workImg.updatePixels();
      corrupt.updatePixels();
      
      float sW = PApplet.parseFloat(imgWidth)/PApplet.parseFloat(aidaCt);
      float sH = PApplet.parseFloat(imgHeight)/PApplet.parseFloat(aidaCt);
      String stitchWidth = nf(sW,2,2)+"\'\'";
      String stitchHeight = nf(sH,2,2)+"\'\'";
      
      println("Number of colours in Glitch: "+skeins.size());
      statusLab.setText("Number of colours in glitch: "+skeins.size()+". Size: "+stitchWidth+" x "+stitchHeight+" on "+aidaCt+"ct fabric");

      if (skeins.size()==1) {
        broken=true;
      }
      
      
      
    }
    catch(Exception e) {

      showRight = true;
      println("Another exception caught: "+e);
      
      broken = true;
      imageMode(CENTER);
      image(brokenimage, width - 225, height-250);
      imageMode(CORNER);
    }

    actLab.setVisible(false);
    glitched = true;
    running = false;
    actBar.stop();
  }
  
  public int dizza(int i, int j, int n) {
    if (n==1) {
       return (i%2!=j%2 ? 2 : 0) + j%2;
    }else{
       return 4*dizza(i%2, j%2, n-1) + dizza(PApplet.parseInt(i/2), PApplet.parseInt(j/2), n-1);
    }
 
  }
  
  public boolean isRunning(){
     
     if(running){
      
      return true;
     } else{
      return false; 
     }
      
    }

  public void stop() {

    thread = null;
  }

  //call this function to stop the thread
  public void dispose() {
    
    running = false;
    stop();
    
  }
}

/**********************************************************************************************************************
*    "Stitch'n'Glitch" by Andrew Healy.                                                                               * 
*    Image corruption and processing to produce cross-stitch patterns and other export options                        *             *
*    Copyright (C) 2012  Andrew Healy                                                                                 *
*                                                                                                                     *
*    This program is free software: you can redistribute it and/or modify                                             *
*    it under the terms of the GNU General Public License as published by                                             *
*    the Free Software Foundation, either version 3 of the License, or                                                *
*    (at your option) any later version.                                                                              *
*                                                                                                                     *
*    This program is distributed in the hope that it will be useful,                                                  *
*    but WITHOUT ANY WARRANTY; without even the implied warranty of                                                   *
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                                    *
*    GNU General Public License for more details.                                                                     *
*                                                                                                                     *
*    You should have received a copy of the GNU General Public License                                                *
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.                                            *
*                                                                                                                     *
*    Author: Andrew Healy. Website: http://www.andrewhealy.com Email: werdnah19@gmail.com                             *
**********************************************************************************************************************/

public class ThreadPattern implements Runnable{
 
 Thread thread;
 
 int margin = 50;
 int my_x = margin;
 int my_y = margin;
 int colsX = margin;
 int colsY = margin;
 int rectwidth = 25;
 int rectheight = 20;
 TColor col=TColor.BLACK.copy();
TColor liveCol = col; 
 boolean running = false;

 
 public ThreadPattern(PApplet parent){
  parent.registerDispose(this); 
 }
 
 public void start(){
  thread = new Thread(this);
  running = true;
  thread.start(); 
 }
 
 public void run(){
  //code to make the PDF pattern 
  my_y = 50;
  my_x = 50;
  colsX = 50;
  colsY = 50;
  //ColorList some = new ColorList();
 
 String[]folders = split(outPath,"/");
 //int newPixelLength = pixelLength;
 

  //save a reference image
  corrupt.save(outPath+"/Sng"+day()+month()+"/Reference_Image_"+sameTime+"."+format);
  statusLab.setText("Reference ."+format+" created at .../"+folders[folders.length-1]+"/SnG"+day()+month()+"/Reference_Image_"+sameTime+"."+format);
  actLab.setText("Creating Thread List");
  //make the color list
  colsNeeded.beginDraw();
  colsNeeded.smooth();

    Iterator j = skeins.values().iterator();

    while (j.hasNext ()) {

      Floss used2 = (Floss) j.next(); 
      colsNeeded.noStroke();
      colsNeeded.fill(used2.DMCcolor);
      colsNeeded.rect(colsX, colsY, rectwidth, rectheight);
      colsNeeded.noFill();
      colsNeeded.shape(used2.symbol, colsX+32, colsY, 20, 20);
      colsNeeded.textFont(pdfFont); 
      colsNeeded.text ("DMC: "+used2.identifier+"; No. of stitches: "+used2.stitches, colsX+60, colsY+15);

      colsY = colsY+35;

      if (colsY>(colsNeeded.height-75)) {

        colsX = colsX + (colsNeeded.width/2)-20;
        colsY = margin;
      }
    }

    colsNeeded.dispose();
    colsNeeded.endDraw();
    
    statusLab.setText("Colour List created at .../"+folders[folders.length-1]+"/SnG"+day()+month()+"/Colour_Chart_"+sameTime+".pdf");
    actLab.setText("Creating PDF pattern");
    //now make PDF pattern
    pdfPattern.beginDraw();
    pdfPattern.smooth();
    pdfPattern.noFill();

    TColor c5=TColor.BLACK.copy();
    
//    try{
    
    for (int i = 0; i < pixelLength; i++) {

      liveCol = c5.newARGB(workImg.pixels[i]);

      String tempS = liveCol.toString();
      int blah = liveCol.toARGB();
      
      if(skeins.containsKey(blah)){
         Floss myFloss = (Floss) skeins.get(blah);
      //pdf.rect(my_x,my_y,50,50);

      pdfPattern.shape(myFloss.symbol, my_x, my_y, 10, 10);
      }else{
        //println("tag not in skeins: "+tempS);
      }
      pdfPattern.strokeWeight(1);
      pdfPattern.stroke(70);
      pdfPattern.rect(my_x, my_y, 10, 10);

      my_x = my_x+10;
      

      if (my_x == pdfPattern.width-50) {

        my_x=margin;
        my_y=my_y+10;
      }
    }
    
/*    }catch(Exception e){
     
     pdfPattern.dispose();
    pdfPattern.endDraw();
    println("pattern created in spite of exception: "+e);
    return;
      
    }
*/


    pdfPattern.dispose();
    pdfPattern.endDraw();
    println("pattern created");
    //statusLab.setText("List of required colours created");
    
    statusLab.setText("PDF pattern created at .../"+folders[folders.length-1]+"/SnG"+day()+month()+"/PDFpattern_"+sameTime+".pdf");
    
    actLab.setVisible(false);
    running = false;
    actBar.stop();

 }
 
 public boolean isRunning(){
     
     if(running){
      
      return true;
     } else{
      return false; 
     }
      
    }
 
 public void stop(){
  thread = null; 
 }
 
 public void dispose(){
   running = false;
   stop(); 
 }
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Stitch_n_Glitch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
