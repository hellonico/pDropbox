import net.hellonico.dropbox.*;
import net.hellonico.potato.*;

PDropboxLibrary api;

void setup() {
 api = new PDropboxLibrary(this);
  
 PImage img = createImage(66, 66, ARGB);
 img.loadPixels();
 for (int i = 0; i < img.pixels.length; i++) {
  img.pixels[i] = color(0, 90, 102, i % img.width * 2); 
 }
 img.updatePixels();

 // the format of the image is determined
 // from the extension, so here: 'png'
 api.store("image.png", img);
}