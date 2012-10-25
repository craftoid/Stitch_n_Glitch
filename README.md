Stitch ´n´ Glitch
=================

*******************************************************

*Open source image corruption and cross stitch pattern technology*


![image](http://25.media.tumblr.com/tumblr_maijihXlVZ1qa76y8o1_400.jpg)


*by Andrew Healy [stitch-n-glitch.andrewhealy.com](http://stitch-n-glitch.andrewhealy.com)*

*******************************************************


OVERVIEW
--------

This program was intended as a combined image-corruption and cross-stitch pattern generator. Along the way some other functionalities were added but it remains a simple, small application to preform this very specific task. There are other, more powerful cross-stitch pattern generators available both online and off as well as various image-corruption facilities. 

This application was never intended as the fastest, most powerful or fullest-featured pattern generator - rather a product of my personal artistic exploration into the aesthetic possibilities of mixing the electronic "glitched" image with a hand-crafted cross-stitch.

This is an open-source project in appreciation of textile art's history of sharing techniques and patterns. Feel free to modify for your own use in keeping with the same spirit. 

### RUN IT WITH PROCESSING

This repository contains the source of the sketch including all libraries,
so you can directly open it inside the Processing IDE and run it.

Please use the most recent version of Processing (currently 2.0b3) due to important changes.  

### RUN THE STANDALONE APP
If you just want to use Stitch'n'Glitch you can download it [here](http://stitch-n-glitch.andrewhealy.com/).

Windows and Linux Users should make sure Java is installed on their computer. Mac users can check if their Java is up to date by requesting a software update check.

Windows and Linux users should keep the "lib","data", and "source" folders in the same folder as the actual application file.

*******************************************************

USING THE SOFTWARE
------------------

### 1. Choose an image

On launching the application, you will be asked to choose an image file through your system's file interface. "Stitch'n'Glitch" can process .jpg(RGB), .gif, or .png files. The image is displayed in the left square of the interface.  

**NOTE 1:** If you choose a jpeg that is not in the RGB colorspace (i.e. CMYK), the preview in the interface will be entirely black or you will be asked to choose another file.

**NOTE 2:** If you choose a file that does not end with the above extensions, you will be asked to choose another file.

### 2. Adjust the original image (optional)

The top tab on the left gives options to adjust the file you have uploaded before it is glitched:
 
If you have selected image that is very large, you may be shown a message in the status bar (at the bottom). It is recommended that you reduce the size before adjusting or glitching the image. It may still work, but it will likely take quite a long time and put strain on your computer's memory from dealing with many hundreds of thousands (or even millions) of pixels. Nobody is really going to be cross-stitching anything 1000 pixels wide anyway. 

The "Reduce colours" slider values range from 1-50 (although these do not correspond to the amount of colours in the image after adjustment). The default it 50 (no reduction). A value of 1 will produce a black and white (not greyscale) image.

The "Dither (Colour)" and "Dither (B&W)" checkboxes are self explanatory. Experiment in combination with the colour-reduction slider to see the effect. These checkboxes only come into effect if the colour-reduction is below 50 and above 0.

On the left are "Load file" and "Reset" buttons. "Load file" will open a new file chooser (this one, unlike the one at launch, can be cancelled). "Reset" will cancel all adjustments made to the original image and reload the file you chose. 

Press "Adjust" to see the results of your adjustment in the left square.

**NOTE:** On pressing the "Adjust" button, you may be prompted to choose a location to save "Stitch'n'Glitch" files. In addition to any files that you export, a folder called "applicationData" will be created in this location to store temporary files "Stitch'n'Glitch" needs to operate. You will only be asked to choose a location once per session.

### 3. Glitch the (adjusted) image

The middle tab on the left will open options to glitch, dither, add noise and reduce colours in the file that will be exported. This part cannot be skipped because it is where the image's colours are converted into the colours of the DMC-branded cotton for cross-stitching (this is the only brand supported at the moment due to availability of data). The colour-reduction and dither options work in the same way as above. The "Add noise" will replace a percentage of the image's pixels with grey pixels at random. This feature was intended to make the grey pixels an "empty" or un-stitched part of the finished piece.

The options to set the fabric count are for convenience only and don't change the image. They are just to let you know how big the piece will be when stitched on a particular fabric.  

The "glitch amount" slider value is set to 15 (out of 30) by default although for some smaller images this is far too much and will break the image (making it unreadable). If this happens an error will appear in the right-hand square where you would expect the glitched image to be displayed. Sometimes a glitch value will break an image when most of the time the same value will be fine. It is by definition a random function and the application is designed to quickly glitch again until you get the result you want. Glitching is NOT required however - by setting the slider value to 0 no image-corruption will occur.

**NOTE 1:** On pressing "Glitch", if you have not first adjusted or glitched another image in the session, you will be prompted to choose a save location (see above).

**NOTE 2:** .png images cannot be corrupted using "Stitch'n'Glitch" at this time. The "Glitch Amount" slider has no effect for these images.

**NOTE 3:** Even if you have set the colour-reduction slider to 50 (i.e. no reduction) you may still see a reduction in colours in the glitched image. This is due to the limited palette (447 colours) of DMC stranded cotton in comparison to electronic imagery.

### 4. Export

There are 3 options for exporting glitched images (i.e. the image on the right). All files will be saved in the folder you have previously chosen.

**1. IMAGE PDF**

This will create a pdf of the image. Each pixel is drawn as a vector square with the appropriate fill. The image can be scaled and printed at no loss of quality.

**2. CSV FILE**

Choosing this option will produce a Comma Separated Values (.csv) file of the hexadecimal color value of all the pixels of the glitched image. 

Each line of pixels in the image (i.e. the width) is separated by a line-break in the .csv file.  This file might come in useful if using another brand of cotton or if visualising the image data in a different, less literal, way. Both the options above have the advantage of being able to deal with very large files.

**3. CROSS-STITCH PATTERN**

If the glitched image is greater than 90kB or if the width multiplied by the height is greater than 200,000 pixels, the image will be resized to prevent the application from crashing while the following files are being created.

All these files are saved in a folder called "SnG" plus the day and the month.

1. Reference image (.jpg, .png, .gif) as an aid to stitching.
2. Colour List detailing the DMC reference no., corresponding symbol in the pattern, and number of stitches for each colour in the pattern.
3. Pattern PDF file. This file is a single pdf (i.e. not in pages) so may require editing in Inkscape/Illustrator before it can be printed and used a reference for stitching.

**NOTE 1:** The symbols used cannot be edited before the pattern has been created. I tried to make the symbols as distinct as possible. The same symbol will always be used for the same colour for every pattern you generate with "Stitch'n'Glitch".

**NOTE 2:** It is safest to export as PDF image (if required) BEFORE exporting as a pattern. If the image is resized it may be distorted in the preview window.

### GENERAL NOTES          

- All images are resized for preview purposes to fit the pale pink box (which is 400px sq.). This does not alter the image or its proportions. 
- By rolling-over the images in the preview boxes, a label is displayed with the closest DMC tread colour. This is a nice feature which perhaps doesn't have much of a practical application. 

*******************************************************

### LICENSE

This code is licensed under the GNU General Public License 3.0 by Andrew Healy. For licenses of the included libraries please check the links below.


### ACKNOWLEDGEMENTS


- Built with Processing (processing.org) using the Toxi Color Utilities libraries (toxiclibs.org) for colour-management and the GUI4PROCESSING library (lagers.org.uk/g4p/) for GUI components.
- Image-corruption code adapted from the "Corrupt" project by Benjamin Gaulon (recyclism.com).
- DMC floss to RGB/HEX conversion from a document at http://www.xstitchtreasures.com/DMCFloss-RGBvalues.html
- Many thanks to the Processing community for sharing their knowledge.



