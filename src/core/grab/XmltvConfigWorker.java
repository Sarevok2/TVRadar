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

import gui.config.XmltvConfigPanel;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

import core.TVRadar;
import core.misc.StreamRedirector;
import data.Options;

/**
 * Thread that launches the XMLTV configuration processes
 * 
 * @author Louis Amstutz
 */
public class XmltvConfigWorker extends SwingWorker<Void, Void> {
	private boolean isWindows, deleteConfig;
	private Process prcs;
	private String grabberName;
	private StreamRedirector errorStream, inputStream;
	private Options options;
	private XmltvConfigPanel xmltvConfPanel;
	
	/**
	 * Constructor
	 * 
	 * @param _grabberName - Name of the XMLTV sub grabber
	 * @param _options - Options
	 * @param _deleteConfig - Whether to delete the config file first (doing a full reconfig)
	 *                        or just reprocessing the existing config file
	 * @param _xcp - parent
	 */
	public XmltvConfigWorker( String _grabberName, Options _options, boolean _deleteConfig,
			XmltvConfigPanel _xcp) {
		
		grabberName = _grabberName;
		options = _options;
		deleteConfig = _deleteConfig;
		xmltvConfPanel = _xcp;
	}
	
	@Override
	public Void doInBackground() throws Exception {
		
		String osName = System.getProperty("os.name");
		if (osName.length() > 6 && osName.substring(0,7).equalsIgnoreCase("windows")) isWindows = true;
		else isWindows = false;
		
		if (deleteConfig) deleteConfigFile();
		
		configXmltv();
		
		options.setCurrentXmltvGrabber(grabberName);
		
		return null;
	}
	
	@Override
	public void done() {
		try {
			get();
			xmltvConfPanel.configFinishedSuccess();
		} 
		catch (java.util.concurrent.CancellationException e) {cancelConfig();}
		catch (InterruptedException e) {cancelConfig();} 
		catch (ExecutionException e) {
			cancelConfig();
			xmltvConfPanel.configFinishedError(e.getCause());
		}
	}
	
	/**
	 * Configure XMLTV
	 */
	private void configXmltv() throws IOException, ConnectException {
		File userDirFile = new File(TVRadar.getUserDir());
		String command = grabberName +  " --configure --gui Tk --config-file " + 
			XmltvGrabber.getConfigFileName();
		if (isWindows) command = "resources\\xmltv\\xmltv " + command;

		prcs = Runtime.getRuntime().exec(command, null, userDirFile);
		
		//We don't need the output, but these threads make sure the input/error streams stay cleared
		errorStream = new StreamRedirector(prcs.getErrorStream(), null);
		errorStream.start();
		inputStream = new StreamRedirector(prcs.getInputStream(), null);
		inputStream.start();
		
		try {prcs.waitFor();} 
		catch (InterruptedException e) {
			cancelConfig();
			return;
		}
		
		int xmltvExitValue = prcs.exitValue();
		if (xmltvExitValue == 9) throw new ConnectException("Login failed.  Please check username/password");
		else if (xmltvExitValue == 22) throw new ConnectException("Unable to connect");
		else if (xmltvExitValue != 0) throw new IOException("Unknown XMLTV error");
	}
	
	/**
	 * Deletes the XMLTV config file
	 */
	public void deleteConfigFile() {
		File configFile = new File(TVRadar.getUserDir() + XmltvGrabber.getConfigFileName());
		configFile.delete();
	}
	
	/**
	 * Called when the user cancels the configuration
	 * 
	 * Destroys the external XMLTV process
	 */
	public void cancelConfig() {
		if (prcs != null) prcs.destroy();
		deleteConfigFile();
	}
}
