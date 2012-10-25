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
    float tolerance = 0.5 - (origColours.getValue()/100.0);
    convien.clear();
    
    //resize Image first for speed
      if(resizeImg.getValue() < 100){
        
        int newWidth = int(imgWidth*(resizeImg.getValue()/100.0));
        int newHeight = int(imgHeight*(resizeImg.getValue()/100.0));
        
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
        
        float f=255./(pow(2,2*n)+1);
        
        for (int x=0;x<imgWidth;x++) {  
          for (int y=0;y<imgHeight;y++) {  
      
            color c = img.get(x,y);
            float t=(n>0?dizza(x,y,n)*f:128);
            
            if(n == 5){//no dither
            
            }else{
              if(origDitherCol.isSelected()){ //colour
                
                int r=(t>=red(c)?0:255);
                int g=(t>=green(c)?0:255);
                int b=(t>=blue(c)?0:255);
                c=color(r,g,b);
                
              }else{ //B+W
                
                 c=color(t>=(red(c)+green(c)+blue(c))/3.?0:255);
              }
              
              workImg.pixels[x+y*imgWidth] = c;   
            }
      
          }
        }
          
      }else if (origColours.getValue() < 50) {
          
          
        if(tolerance<0.025){
         tolerance=0.025; 
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

        color blah = workImg.pixels[j];

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

    public void dispose() {

      running = false;
      stop();
      
    }
  }

