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

package core.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import core.grab.GrabListingsWorker;
import data.guide.*;

/**
 * Base class for parsing XML TV listings
 * It uses the Apache Xerces XML parser to parse the document.  Subclasses must override
 * abstract methods such as startElement to handle events from the parser.
 * 
 * @author Louis Amstutz
 */
public abstract class TVParser extends DefaultHandler {
	private static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
	
	/** Default values of parser */
	private static final boolean setValidation = false, setNameSpaces = true, 
		setSchemaSupport = true, setSchemaFullSupport = false;
    
	protected String currentString="";
	protected Channel currentChannel=null;
	protected Program currentProgram=null;
	protected ArrayList<Channel> channels = new ArrayList<Channel>();
	
	protected GuideData data;
	protected GrabListingsWorker grabWorker;
	
	protected int numHours=0;
	
	/** Variables used to estimate the progress of parsing */
	protected int channelCount=0, programCount=0, scheduleCount=0, genreCount=0, crewCount=0, 
		numProgramsEstimate=-1, numScheduleEstimate=-1, numGenreEstimate=-1, numCrewEstimate=-1;
	
	/** Start and end times of the listings in milliseconds */
	long start, end;	
	
	/**
	 * Parse Listings and put the data into a GuideData object
	 * 
	 * @param xmlListings - The source stream of XML listings that will be parsed
	 * @param _start - Start time in milliseconds
	 * @param _end - End time in milliseconds
	 * @return a GuideData object generated from the data in the XML file
	 */
	public GuideData parse(InputSource xmlListings, long _start, long _end, GrabListingsWorker _worker)
			throws SAXException, IOException {

		start = _start;
		end = _end;
		grabWorker = _worker;
		
		data = new GuideData();
		data.setChannelMappings(new ArrayList<Mapping>());
		
		
		XMLReader parser;
		try {parser = (XMLReader)Class.forName(DEFAULT_PARSER_NAME).newInstance();}
		catch (Exception e) {throw new SAXException("Unable to initialize the Xerces XML Parser",e);}

		parser.setFeature( "http://xml.org/sax/features/validation",setValidation);
		parser.setFeature( "http://xml.org/sax/features/namespaces",setNameSpaces );
		parser.setFeature( "http://apache.org/xml/features/validation/schema",setSchemaSupport );
		parser.setFeature( "http://apache.org/xml/features/validation/schema-full-checking",setSchemaFullSupport );
		
		parser.setContentHandler(this);
        parser.setErrorHandler(this);
        
        parser.setDTDHandler(new DTDHandler() {
        	public void notationDecl(String name,String publicId, 
        			String systemId)throws SAXException {
        		System.out.println("Notation: " + name + " " + publicId + " " + systemId);
        	}
        	public void unparsedEntityDecl(String name,String publicId, 
        			String systemId,String notationName)throws SAXException {
        		System.out.println("Unparsed: " + name + " " + publicId + " " + systemId);
        	}
        });
        
        numHours = (int)(end - start)/1000/60/60;
        
        parser.parse(xmlListings);
        finish();
        
		Collections.sort(data.getChannelMappings());
		grabWorker.setProgressPublic(100);

		return data;
	}
	
	@Override
	public abstract void startDocument();
	
	@Override
	 public abstract void startElement(String uri, String local, 
			 String raw, Attributes attrs) throws SAXException;
	
	@Override
	public abstract void endElement(String uri, String local, String raw) throws SAXException;
	
	@Override
	public void characters(char ch[], int start, int length) throws SAXException{
		if (Thread.interrupted()) throw new SAXException();
		
		for (int i=start; i<(start+length); i++) {
			currentString = currentString.concat(String.valueOf(ch[i]));
		}

	}
	
	@Override
	public void processingInstruction(String target, String data) {}
	
	@Override
	public void warning(SAXParseException e) {}
	
	@Override
	public void error(SAXParseException e) {}
	
	@Override
	public void fatalError(SAXParseException e) {}
	
	/**
	 * Will be called after parse is complete
	 */
	public abstract void finish();
}
