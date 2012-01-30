/**
 * TV Radar
 * Copyright 2009-2010 Louis Amstutz (louis34@gmail.com)
 * 
 *   This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import core.load.*;
import data.Favorites;
import data.Options;
import data.guide.GuideData;
import gui.GrabDialog;
import gui.LoadDialog;
import gui.SDLoginDialog;
import gui.MainGui;
import gui.config.FirstTimeWizard;

/**
 * The central class for the program
 * 
 * Contains the GuideData, Options, and Favorites objects
 * Also contains the application entry point
 * 
 * @author Louis Amstutz
 */
public class TVRadar {
	private static final String PROGRAM_NAME = "TV Radar";
	private static final String PROGRAM_VERSION = "0.611";

	private static final String sep = System.getProperty("file.separator");
	
	private static final String ICON_FILE_NAME = "resources" + sep + "icons" + sep + "tvradar_32.gif";
	
	private static final String WINDOWS_USER_DIR = System.getenv("appdata") + sep + PROGRAM_NAME + sep;
	
	private static final String LINUX_USER_DIR = System.getProperty("user.home") + 
		sep + "." + PROGRAM_NAME + sep;
	
	/** User directory where all user files are stored */
	private static String userDir;
	
	/** Contains all the listing data */
	private GuideData data;
	private Options options;
	private Favorites favorites;
	
	private MainGui mg;
	
	private BufferedImage icon;
	
	/**
	 * Constructor
	 */
	public TVRadar(boolean downloadOnly) {
		setUserDir();
		
		if (downloadOnly) initDownload();
		else initNormal();
	}
	
	public static String getProgramName() {return PROGRAM_NAME;	}
	public static String getProgramVersion() {return PROGRAM_VERSION;}
	public static String getUserDir() {return userDir;}
	public static void setUserDir(String userDir) {TVRadar.userDir = userDir;}
	
	private void initNormal() {
		favorites = Favorites.loadFavorites();
		
		loadIcon();
		
		mg = new MainGui(this);
		
		options = Options.loadOptions();
		if (options == null) {
			options = new Options();
			options.restoreDefaults();
			FirstTimeWizard fd = new FirstTimeWizard(mg, options);
			if (fd.isExitProgram()) System.exit(0);
		}
		else if (dataExists()) new LoadDialog(this, mg);
	}
	
	/**
	 * Download listings as per current settings in options and then terminate
	 */
	private void initDownload() {
		options = Options.loadOptions();
		if (options == null) System.exit(1);
			
		new GrabDialog(this, null, options.getSdUsername(), options.getSdPassword());
	}
	/**
	 * Gets the username and password for schedules direct
	 * 
	 * If the user has set the program to remember password, these are
	 * retrieved from the options.  Otherwise the user is prompted with
	 * a dialog
	 * 
	 * @param username - will be set to the username
	 * @param password - will be set to the password
	 */
	public void getSDLogin(StringBuilder username, StringBuilder password) {
		if (options.isRememberSDPassword() && options.getSdUsername() != null && options.getSdPassword() != null) {
			username.append(options.getSdUsername());
			password.append(options.getSdPassword());
		}
		else {
			SDLoginDialog dialog = new SDLoginDialog(mg);
			if (dialog.getResult() == -1) return;
			username.append(dialog.getUsername());
			password.append(dialog.getPassword());
			if (dialog.getRememberPassword()) {
				options.setSdUsername(username.toString());
				options.setSdPassword(password.toString());
				options.setRememberSDPassword(true);
				Options.saveOptions(options);
			}
		}
	}
	
	public GuideData getData() {return data;}
	public Options getOptions() {return options;}
	public Favorites getFavorites() {return favorites;}
	public BufferedImage getIcon() {return icon;}
	
	public void setData(GuideData _data) {data = _data;}
	
	/**
	 * Check if there is any data to load
	 * 
	 * @return true if there is data
	 */
	public boolean dataExists() {
		ListingLoader loader;
		if (options.isUseSQL()) loader = new SQLLoader(options.getMySqlPwd(), options.getMySqlUrl());
		else loader = new SerializedLoader();
		
		return loader.dataExists();
	}
	
	/**
	 * Sets the user directory based on the OS
	 */
	public void setUserDir() {
		String osName = System.getProperty("os.name");
		if (osName.length() >= 7 && osName.substring(0,7).equalsIgnoreCase("windows")) 
			userDir = WINDOWS_USER_DIR;
		else userDir = LINUX_USER_DIR;
	}
	
	/**
	 * Loads the icon for all the windows
	 */
	private void loadIcon() {
		try {icon = ImageIO.read(new File(ICON_FILE_NAME));} 
		catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Main method
	 * 
	 * @param args - Not used
	 */
	public static void main(String args[]) {
		if (args.length>0 && args[0].equalsIgnoreCase("-download")) {
			new TVRadar(true);
		}
		else {
			try {UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");} 
		    catch (Exception e) {}

			new TVRadar(false);
		}
		
	}
}
