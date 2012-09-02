import net.hellonico.dropbox.*;
import net.hellonico.potato.*;

import com.dropbox.client2.DropboxAPI.Entry;

PDropboxLibrary drop;

void setup() {
  size(800,800);
  background(0);
  smooth();
  noLoop();
  
  drop = new PDropboxLibrary(this);
  for(Entry o : drop.search("/", "txt")) {
    fill(random(255),random(255),random(255));
    text(o.path, 40, random(800));
   }
}

void draw() {
  
}
