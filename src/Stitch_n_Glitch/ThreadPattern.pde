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
      color blah = liveCol.toARGB();
      
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
