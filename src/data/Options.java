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

package data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import core.TVRadar;


/**
 * Stores the user options of the program
 * 
 * @author Louis Amstutz
 */
public class Options implements Serializable {
	private static final long serialVersionUID = 8185122251661664322L;
	
	private static final String OPTIONS_FILENAME = "options.dat";
	
	/** Default server URL for Schedules Direct */
	private static final String DEFAULT_SD_SERVER_URL = "http://webservices.schedulesdirect.tmsdatadirect.com/" +
			"schedulesdirect/tvlistings/xtvdService";

	/** URL for local MySQL server */
	private static final String LOCAL_MYSQL_URL = "jdbc:mysql://localhost:3306";
	
	/** Which grabber to use */
	public enum GrabberType {SD, XMLTV};
	private GrabberType grabber;
	
	/** User's choices on whether the crew is shown */
	public enum ShowCrew {YES, NO, MOVIES};
	private ShowCrew showCrew;
	
	/** How many days ahead to grab listings */
	private int daysAhead;
	
	/** Delay in milliseconds for the tooltip popups to show up when hovering the mouse on a program/channel */
	private int popupDelay;
	
	/** The over size of the UI in the guide view */
	private int uiSize;
	
	/** If true then use MySQL to store listings, otherwise use a serialized file */
	private boolean useSQL;
	
	/** If true then delete all data from SQL tables every time new listings are downloaded.  
	 * Otherwise leave the old data there. */
	private boolean deleteSQL;
	
	/** If true, then instead of insert data into MySQL in one large SQL statement, it will divide it into 
	 * smaller statements to avoid max_allowed_packet errors */
	private boolean smallerSqlPackets;
	
	/** True if the user set the program to remember Schedules Direct password */
	private boolean rememberSDPassword;
	
	/** Schedules Direct username */
	private String sdUsername;
	
	/** Schedules Direct password */
	private String sdPassword;
	
	/** Schedules Direct source URL */
	private String schedulesDirectURL;
	
	/** MySQL URL */
	private String mySqlUrl;
	
	/** Password of mySql user account the will be used by this program */
	private String mySqlPwd;
	
	/** Current XMLTV grabber that XMLTV was configured to use */
	private String currentXmltvGrabber;
	
	/**
	 * Default Constructor
	 */
	public Options() {}
	
	public static String getDefaultSdServerUrl() {return DEFAULT_SD_SERVER_URL;}
	
	/**
	 * Saves the options and if necessary, creates the user directory
	 * @throws IOException 
	 */
	public void initOptions() throws IOException {
		if (!initUserDir()) throw new IOException("Could not create user directory: " + TVRadar.getUserDir());
		Options.saveOptions(this);
	}
	
	/**
	 * Restores all options to defaults
	 */
	public void restoreDefaults() {
		uiSize=150;
		popupDelay=200;
		showCrew=ShowCrew.MOVIES;
		grabber=GrabberType.SD;
		daysAhead=1;
		useSQL=false;
		deleteSQL=true;
		rememberSDPassword=false;
		sdUsername=null;
		sdPassword=null;
		schedulesDirectURL=DEFAULT_SD_SERVER_URL;
		mySqlUrl=LOCAL_MYSQL_URL;
		mySqlPwd=null;
		smallerSqlPackets=false;
	}
	
	/**
	 * Creates the user directory if it doesn't already exist
	 * 
	 * @return Returns false if failed to create user directory
	 */
	public boolean initUserDir() {
		File userDirFile = new File(TVRadar.getUserDir());
		if (!userDirFile.exists()) return userDirFile.mkdir();
		else return true;
	}
	
	/**
	 * Loads options from a file
	 * 
	 * @return Options object
	 */
	public static Options loadOptions() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(TVRadar.getUserDir() + OPTIONS_FILENAME);
			in = new ObjectInputStream(fis);
			Options options = (Options)in.readObject();
			in.close();
			return options;
		}
		catch(IOException ex){return null;}
		catch(ClassNotFoundException ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Saves options to a file
	 * 
	 * @param options - Options object to be saved
	 */
	public static void saveOptions(Options options) {		
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(TVRadar.getUserDir() + OPTIONS_FILENAME);
			out = new ObjectOutputStream(fos);
			out.writeObject(options);
			out.close();
		}
		catch(IOException ex){ex.printStackTrace();}
	}

	public GrabberType getGrabber() {
		return grabber;
	}

	public ShowCrew getShowCrew() {
		return showCrew;
	}

	public int getDaysAhead() {
		return daysAhead;
	}

	public int getPopupDelay() {
		return popupDelay;
	}

	public int getUiSize() {
		return uiSize;
	}

	public boolean isUseSQL() {
		return useSQL;
	}

	public boolean isDeleteSQL() {
		return deleteSQL;
	}

	public boolean isRememberSDPassword() {
		return rememberSDPassword;
	}

	public boolean isSmallerSqlPackets() {
		return smallerSqlPackets;
	}

	public String getSdUsername() {
		return sdUsername;
	}

	public String getSdPassword() {
		return sdPassword;
	}

	public String getSchedulesDirectURL() {
		return schedulesDirectURL;
	}

	public String getMySqlUrl() {
		return mySqlUrl;
	}

	public String getMySqlPwd() {
		return mySqlPwd;
	}

	public String getCurrentXmltvGrabber() {
		return currentXmltvGrabber;
	}

	public void setGrabber(GrabberType grabber) {
		this.grabber = grabber;
	}

	public void setShowCrew(ShowCrew showCrew) {
		this.showCrew = showCrew;
	}

	public void setDaysAhead(int daysAhead) {
		this.daysAhead = daysAhead;
	}

	public void setPopupDelay(int popupDelay) {
		this.popupDelay = popupDelay;
	}

	public void setUiSize(int uiSize) {
		this.uiSize = uiSize;
	}

	public void setUseSQL(boolean useSQL) {
		this.useSQL = useSQL;
	}

	public void setDeleteSQL(boolean deleteSQL) {
		this.deleteSQL = deleteSQL;
	}

	public void setRememberSDPassword(boolean rememberSDPassword) {
		this.rememberSDPassword = rememberSDPassword;
	}

	public void setSmallerSqlPackets(boolean smallerSqlPackets) {
		this.smallerSqlPackets = smallerSqlPackets;
	}

	public void setSdUsername(String sdUsername) {
		this.sdUsername = sdUsername;
	}

	public void setSdPassword(String sdPassword) {
		this.sdPassword = sdPassword;
	}

	public void setSchedulesDirectURL(String schedulesDirectURL) {
		this.schedulesDirectURL = schedulesDirectURL;
	}

	public void setMySqlUrl(String mySqlUrl) {
		this.mySqlUrl = mySqlUrl;
	}

	public void setMySqlPwd(String mySqlPwd) {
		this.mySqlPwd = mySqlPwd;
	}

	public void setCurrentXmltvGrabber(String currentXmltvGrabber) {
		this.currentXmltvGrabber = currentXmltvGrabber;
	}
}
