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

package core.grab;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import javax.naming.ConfigurationException;

import org.xml.sax.InputSource;

import core.TVRadar;
import core.misc.StreamRedirector;

/**
 * Run XMLTV via command line and then get the XML listings from it
 * 
 * @author Louis Amstutz
 */
public class XmltvGrabber implements Grabber {
	
	/** File name of the XMLTV config file */
	private final static String CONFIG_FILE_NAME = "xmltv.conf";
	
	/** The XMLTV process */
	private Process prcs;

	/** The name of the XMLTV sub grabber */
	private String grabberName;

	/**
	 * Constructor
	 * 
	 * @param _grabberName - The name of the XMLTV sub grabber (eg. tv_grab_na_dd
	 */
	public XmltvGrabber(String _grabberName) {
		grabberName = _grabberName;
	}
	
	public static String getConfigFileName() {return CONFIG_FILE_NAME;}
	
	@Override
	public InputSource grabListings(long start, long end, GrabListingsWorker worker) 
			throws IOException, ConfigurationException {
		
		if (!checkConfiguration()) throw new ConfigurationException("XMLTV not configured");
		
		long numDays = (end - start)/86400000;
		File userDirFile = new File(TVRadar.getUserDir());
		
		String command = grabberName + " --days " + (numDays+1) + 
				" --gui Tk --config-file " + CONFIG_FILE_NAME;
		
		String osName = System.getProperty("os.name");
		if (osName.length() > 6 && osName.substring(0,7).equalsIgnoreCase("windows")) 
			command = "resources\\xmltv\\xmltv " + command;

		prcs = Runtime.getRuntime().exec(command, null, userDirFile);
		
		StreamRedirector errorStream = new StreamRedirector(prcs.getErrorStream(), null);
		errorStream.start();
		
		/*try {
			java.io.BufferedOutputStream os = new java.io.BufferedOutputStream(
					new java.io.FileOutputStream(new File("output2.xml")));
			
			StreamRedirector inputStream = new StreamRedirector(prcs.getInputStream(), os);
			inputStream.start();
			prcs.waitFor();
		}
		catch (Exception e) {e.printStackTrace();}*/
		
		InputSource listingSource = new InputSource(new BufferedInputStream(prcs.getInputStream()));
		
		//Set the system id of the InputSource to the directory that 
		//contains xmltv.dtd.  The parser will need this.
		listingSource.setSystemId(System.getProperty("user.dir") + System.getProperty("file.separator") +
				 "resources" + System.getProperty("file.separator"));
		
		return listingSource;
	}
	
	/**
	 * Get the exit value of the XMLTV process
	 * @return - Exit value
	 */
	public int getXmltvExitValue(){
		try {
			if (prcs != null) {
				prcs.waitFor();
				return prcs.exitValue();
			}
		} catch (InterruptedException e) {}
		return -1;
	}
	
	/**
	 * Checks if XMLTV is configured
	 * 
	 * @return true if it is configured, false otherwise
	 */
	public boolean checkConfiguration() {
		if ((new File(TVRadar.getUserDir() + CONFIG_FILE_NAME)).exists()) return true;
		else return false;
	}
	
	@Override
	public void cancelGrab() {
		if (prcs != null) prcs.destroy();
	}
}
