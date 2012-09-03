/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package net.hellonico.dropbox;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import processing.core.PApplet;
import processing.core.PImage;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;

public class PDropboxLibrary {

	public final static String VERSION = "##library.prettyVersion##";

	DropboxAPI<?> client;
	
	public PDropboxLibrary(PApplet parent) {
		try {
			Class klass = Class.forName("net.hellonico.potato.Potato");
			Constructor c = klass.getConstructor(PApplet.class);
			Object potato = c.newInstance(parent);
			Method m = klass.getMethod("getSettings", String.class);
			HashMap settings = (HashMap) m.invoke(potato, "dropbox");
			this.init(settings);
		} catch (Exception e) {
			throw new RuntimeException("This is carrot day." + e.getMessage());
		}
	}
	
	public PDropboxLibrary(HashMap settings) {
		init(settings);
	}
	
	private void init(HashMap settings) {
		if(settings.containsKey("userKey") && settings.containsKey("userSecret")) {
			this.initWithTokens(settings);
		} else {
			this.initWithoutTokens(settings);
		}
	}
	
	private void initWithoutTokens(HashMap<String,String> settings) {
		AppKeyPair appKeyPair = new AppKeyPair(
				settings.get("appKey"), 
				settings.get("appSecret"));
		WebAuthSession was = new WebAuthSession(appKeyPair,
				Session.AccessType.DROPBOX);

		try {
			WebAuthSession.WebAuthInfo authInfo = was.getAuthInfo();

			RequestTokenPair pair = authInfo.requestTokenPair;
			String url = authInfo.url;

			Desktop.getDesktop().browse(new URL(url).toURI());
			JOptionPane.showMessageDialog(null, "Press ok to continue once you have authenticated.");
			was.retrieveWebAccessToken(pair);

			AccessTokenPair tokens = was.getAccessTokenPair();
			System.out.println("userKey:\t"+tokens.key);
			System.out.println("userSecret:\t"+tokens.secret);

			client = new DropboxAPI<WebAuthSession>(was);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initWithTokens(HashMap<String,String> settings) {
		AppKeyPair appKeyPair = new AppKeyPair(
				settings.get("appKey"), 
				settings.get("appSecret"));
		WebAuthSession was = new WebAuthSession(
				appKeyPair, 
				Session.AccessType.DROPBOX);
		
		try {
			AccessTokenPair tokens = new AccessTokenPair(
					settings.get("userKey"), 
					settings.get("userSecret"));
			WebAuthSession.WebAuthInfo authInfo = was.getAuthInfo();
			was.setAccessTokenPair(tokens);
			client = new DropboxAPI<WebAuthSession>(was);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Entry store(String local) {
		try {
			File file = new File(local);
			FileInputStream inputStream = new FileInputStream(file);
			Entry newEntry = client.putFile(new File(local).getName(), inputStream, file.length(),
					null, null);
			return newEntry;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Entry store(String name, PImage p) {
		try {
			BufferedImage image = (BufferedImage) p.getImage();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			String extension = name.substring(name.lastIndexOf(".") + 1);
			System.out.println("Using extension:"+extension);
			ImageIO.write(image, extension, os);
			byte[] byteArray = os.toByteArray();
			InputStream is = new ByteArrayInputStream(byteArray);
			Entry newEntry = client.putFile(name, is, byteArray.length, null, null);
			return newEntry;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public File download(String remote, String local) {
		try {
			File file = new File(local);
			FileOutputStream outputStream = new FileOutputStream(file);
			DropboxFileInfo info = client.getFile(remote, null, outputStream,
					null);
			return file;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<Entry> search(String folder, String query) {
		try {
			List<Entry> entries = client.search(folder, query, 0, false);
			for(DropboxAPI.Entry entry : entries) {
				System.out.println(entry.path+"\t" +entry.size);
			}
			return entries;
		} catch (DropboxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public DropboxAPI getApi() {
		return this.client;
	}

	public static String version() {
		return VERSION;
	}

	public static void main(String[] args) {
		// https://www.dropbox.com/static/developers/dropbox-android-sdk-1.5.1-docs/
		
		HashMap settings = new HashMap();
		settings.put("appKey", "9v8w0awbmqbnapf");
		settings.put("appSecret", "rd1ae7ttwyythpz");
		settings.put("userKey", "s4sihmp7imaazr3");
		settings.put("userSecret", "82dq3iwbun4b8a7");
		
		PDropboxLibrary api = new PDropboxLibrary(settings);
		
		//api.store("license.txt");
		//api.download("license.txt", "/tmp/license.txt");
		api.search("/","txt");
	}

}
