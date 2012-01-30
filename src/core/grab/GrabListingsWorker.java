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

import gui.GrabDialog;

import java.io.IOException;
import java.net.ConnectException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.naming.ConfigurationException;
import javax.swing.SwingWorker;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import core.parse.*;

import data.Options;
import data.guide.GuideData;

/**
 * Thread to grab the listings
 * Creates the appropriate grabber and parser based on what's selected in options
 * 
 * @author Louis Amstutz
 */
public class GrabListingsWorker extends SwingWorker<GuideData, String> {
	private long start, end;
	
	private String username, password;
	private Grabber grabber;
	private TVParser parser;
	private Options options;
	private GrabDialog grabDialog;
	
	/**
	 * Constructor
	 * 
	 * @param usr - Username - Only used with schedules direct grabber
	 * @param pwd - Password - Only used with schedules direct grabber
	 * @param _progress - Monitors progress
	 * @param _options - Options
	 */
	public GrabListingsWorker( String usr, String pwd, Options _options, GrabDialog _gd) {
		username = usr;
		password = pwd;
		options = _options;
		grabDialog = _gd;
	}
	
	@Override
	public GuideData doInBackground() throws Exception {
		initStartEnd();
		
		/*InputSource xmlListings=null;
		try {xmlListings = new InputSource(new java.io.FileInputStream("temp\\xmltv output.xml"));}
		catch (java.io.FileNotFoundException e) {e.printStackTrace();}*/
		
		InputSource xmlListings = downloadListings();
		GuideData data = parseListings(xmlListings);
		publish("Grabbed listings successfully");
		return data;
	}
	
	@Override
	protected void done() {
		try {grabDialog.finishGrabSuccess(get());} 
		catch (java.util.concurrent.CancellationException e) {grabber.cancelGrab();}
		catch (InterruptedException e) {grabber.cancelGrab();} 
		catch (ExecutionException e) {
			grabber.cancelGrab();
			grabDialog.finishGrabError(e.getCause());
		}
	}
	
	@Override
	protected void process(List<String> statusMessages) {
		grabDialog.updateStatusMessage(statusMessages.get(0));
	}
	
	public void setProgressPublic(int progress) {
		setProgress(Math.min(progress, 100));
	}
	
	public void publishPublic(String message) {
		publish(message);
	}
	
	/**
	 * Grabs the listings
	 * 
	 * @return - Contains input stream of the listings source.  This is passed to the parser
	 * @throws SocketTimeoutException
	 * @throws ConnectException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	private InputSource downloadListings() throws SocketTimeoutException, ConnectException, 
			ProtocolException, IOException, ConfigurationException {
		
		if (options.getGrabber() == Options.GrabberType.SD) 
			grabber = new SchedulesDirectGrabber(options.getSchedulesDirectURL(), username, password);
		else grabber = new XmltvGrabber(options.getCurrentXmltvGrabber());
		
		return grabber.grabListings(start, end,  this);
	}
	
	/**
	 * Parse the listings and create the GuideData object
	 * 
	 * @param xmlListings - Contains input stream to be parsed
	 * @return the GuideData object that was generated
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private GuideData parseListings(InputSource xmlListings) throws IOException, SAXException {
		if (options.getGrabber() == Options.GrabberType.SD) parser = new SDParser();
		else parser = new XmltvParser();

		try {return parser.parse(xmlListings, start, end, this);}
		catch (SAXException e) {
			if (options.getGrabber() == Options.GrabberType.XMLTV ) {
				int xmltvExitValue = ((XmltvGrabber)grabber).getXmltvExitValue();
				if (xmltvExitValue == 9) throw new ConnectException(
						"Login failed.  Please check username/password");
				else if (xmltvExitValue == 22) throw new ConnectException("Unable to connect");
			}
			throw e;
		}
	}
	
	/**
	 * Initialises the start and end times which will be used when requesting the
	 * listings from the source.
	 */
	private void initStartEnd() {
		Calendar startCal = new GregorianCalendar();
		if (startCal.get(Calendar.MINUTE) >= 30) startCal.set(Calendar.MINUTE, 30);
		else startCal.set(Calendar.MINUTE, 0);
		startCal.set(Calendar.SECOND, 0);
		startCal.set(Calendar.HOUR, startCal.get(Calendar.HOUR)-1);
		startCal.set(Calendar.MILLISECOND, 0);
		
		start = startCal.getTimeInMillis();
		end = start + (options.getDaysAhead() * 86400000);
	}
}
