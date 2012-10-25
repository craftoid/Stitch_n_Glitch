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
    float tolerance = 0.5 - (patColours.getValue()/100.0);
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

      int PosA = int(random (scrambleStart, scrambleEnd));

      int PosB = int(random (scrambleStart, scrambleEnd));

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
        
        float f=255./(pow(2,2*n)+1);
        
        for (int x=0;x<imgWidth;x++) {  
          for (int y=0;y<imgHeight;y++) {  
      
            color c = corrupt.get(x,y);
            float t=(n>0?dizza(x,y,n)*f:128);
            
            if(n == 5){//no dither

            
              
            }else{
              if(glitchDitherCol.isSelected()){ //colour
                
                int r=(t>=red(c)?0:255);
                int g=(t>=green(c)?0:255);
                int b=(t>=blue(c)?0:255);
                c=color(r,g,b);
                
              }else{ //B+W
                
                 c=color(t>=(red(c)+green(c)+blue(c))/3.?0:255);
              }
              
              corrupt.pixels[x+y*imgWidth] = c;   
            }
      
          }
        }
          
      

    }else if (patColours.getValue() < 50) {
        
        if(tolerance<0.025){
         tolerance=0.025; 
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
    int deadPixels = int(pixelLength*(deadAmt.getValue()/100.0));
    println(deadPixels);



      
      for (int d = 0; d < deadPixels; d++) {

      corrupt.pixels[int(random(pixelLength))] = color(128);
      }

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
      
      float sW = float(imgWidth)/float(aidaCt);
      float sH = float(imgHeight)/float(aidaCt);
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
       return 4*dizza(i%2, j%2, n-1) + dizza(int(i/2), int(j/2), n-1);
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

