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

import processing.pdf.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import toxi.color.*;
import toxi.color.theory.*;
import toxi.math.*;
import java.util.List;
import guicomponents.*;

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
color[] myColor = new color [447];
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

color palePink, warmGrey, niceBrown, buttonGreen, brightPink;

int pixelLength, imgWidth, imgHeight;
int numReplacements = 15;
int aidaCt = 14;
int margin = 50;

PImage img, workImg, corrupt, brokenimage, bg, noPNG, origImg, adjustImg;
PFont georgia = createFont("Georgia", 18, true);
PFont pdfFont = createFont("Baskerville", 12, true);


void setup() {

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

void loadNewFile(File selection) {

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

void loadFile(File selection) {

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

void checkFolder(File folder) {

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

void checkFolderAgain(File folder) {

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


void draw() {


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

void resizeForPattern(){
 
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
    corrupt.resize(int(imgWidth-imgWidth/10.), int(imgHeight-imgHeight/10.));
    imgWidth = int(imgWidth-imgWidth/10.);
    imgHeight = int(imgHeight-imgHeight/10.);
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

    color blah = corrupt.pixels[j];
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


void drawScaleImage(PImage pic, int tl_x, int tl_y, int max_dem) {

  float ratio;

  fill(palePink);
  rect(tl_x, tl_y, max_dem, max_dem);


  if (pic.width>0 && pic.height>0) {

  if (broken) {

      try {

        if (pic.width > pic.height) { //A landscape

          ratio = float(max_dem)/float(pic.width);
          int imgY = int(tl_y + ((max_dem/2) - (float(pic.height)*ratio)/2.0));
          copy(pic, 0, 0, pic.width, pic.height, tl_x, imgY, max_dem, int(float(pic.height)*ratio));
        }
        else { // A portrait or square

            ratio = float(max_dem)/float(pic.height);
          int imgX = tl_x + int((max_dem/2) - ((float(pic.width)*ratio)/2.0));
          copy(pic, 0, 0, pic.width, pic.height, imgX, tl_y, int(float(pic.width)*ratio), max_dem);
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

          ratio = float(max_dem)/float(pic.width);
          int imgY = int(tl_y + ((max_dem/2) - (float(pic.height)*ratio)/2.0));
          copy(pic, 0, 0, pic.width, pic.height, tl_x, imgY, max_dem, int(float(pic.height)*ratio));
        }
        else { // A portrait or square

            ratio = float(max_dem)/float(pic.height);
          int imgX = tl_x + int((max_dem/2) - ((float(pic.width)*ratio)/2.0));
          copy(pic, 0, 0, pic.width, pic.height, imgX, tl_y, int(float(pic.width)*ratio), max_dem);
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



boolean overImage() {

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

void displayLabel(boolean over) {

  if (!actBar.isVisible() && !makePattern.isRunning() && !adjustThis.isRunning() && !glitchThis.isRunning()) {

    try {
      if (overImage()) {

        color mouseC;
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

void saveCSVfile() {

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

void savePDFimage() {

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

    color tempCol = workImg.pixels[i];
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

void handleCheckboxEvents(GCheckbox checkbox) {


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

void handleSliderEvents(GSlider slider) {
  if (resizeImg == slider) {

    int newWidth = int(imgWidth*(resizeImg.getValue()/100.0));
    int newHeight = int(imgHeight*(resizeImg.getValue()/100.0));

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

