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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.text.SimpleDateFormat;
import java.text.ParseException;

import data.guide.*;

/**
 * Parses TV listings in the XMLTV format
 * 
 * @author Louis Amstutz
 */
public class XmltvParser extends TVParser {
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss Z");
	
	/** XMLTV doesn't give unique program IDs, so they are create with this static variable */
    private static int progID = 0;
	
	private Schedule currentSchedule;
	private Mapping currentMapping;
	private boolean isAdvisory=false, readingCrew=false, readingRating=false, readingStarRating=false;
	
	/** 
	 * Since XMLTV doesn't tell as which property is which with regards to channels, assume the longest
	 * is the title and the shortest all caps, non number is the call sign.
	 */
	private int longestTitle = 0, shortestCallSign=999;
	
	@Override
	public void startDocument() {
		data.setStartTime(start);
		data.setStopTime(end);
	}
	
	@Override
	public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
		if (grabWorker.isCancelled()) throw new SAXException();
		
		currentString = "";
		
		if (local.equalsIgnoreCase("channel")) {
			grabWorker.setProgressPublic(20);
			grabWorker.publishPublic("Parsing channels");

			
			currentChannel = new Channel();
			currentMapping = new Mapping();
			currentMapping.setChannel(currentChannel);
			
			if (attrs != null && attrs.getQName(0).equalsIgnoreCase("id")) {
				currentChannel.setId(attrs.getValue(0));
			}
			channelCount++;
		}
		
		if (local.equalsIgnoreCase("programme")) {
			if (numProgramsEstimate == -1) {
				numProgramsEstimate = channelCount*numHours;//1 program an hour on average
				grabWorker.publishPublic("Parsing programs");
			}
			int progress = Math.min(30 + (int)( (double)programCount++/numProgramsEstimate*70), 100);
			grabWorker.setProgressPublic(progress);
			currentProgram = new Program();
			currentProgram.setProgramId(String.valueOf(progID));
			currentSchedule = new Schedule();
			currentSchedule.setProgId(String.valueOf(progID++));
			
			if (attrs != null) {
				for (int i=0; i<attrs.getLength(); i++) {
					if (attrs.getQName(i).equalsIgnoreCase("start")) {
						try {currentSchedule.setStart(parseXmltvDate(attrs.getValue(i)));}
						catch (ParseException e) { e.printStackTrace();}
					}
					
					if (attrs.getQName(i).equalsIgnoreCase("stop")) {
						try {currentSchedule.setStop(parseXmltvDate(attrs.getValue(i)));}
						catch (ParseException e) { e.printStackTrace();}
					}
					
					if (attrs.getQName(i).equalsIgnoreCase("channel")) {
						currentSchedule.setChanId(attrs.getValue(i));
					}	
				}
			}
		}
		
		if (local.equalsIgnoreCase("rating")) {
			readingRating = true;
			if (attrs != null && attrs.getValue(0).equalsIgnoreCase("advisory")) isAdvisory = true;
			else isAdvisory = false;
		}
		
		if (local.equalsIgnoreCase("star-rating"))
			readingStarRating = true;
		
		if (local.equalsIgnoreCase("credits")) 
			readingCrew = true;
	}
	
	@Override
	public void endElement(String uri, String local, String raw) throws SAXException{
		if (grabWorker.isCancelled()) throw new SAXException();
		
		if (local.equalsIgnoreCase("channel")) {
			if (!data.getChannelMappings().contains(currentMapping)) {
				data.getChannelMappings().add(currentMapping);
				channels.add(currentMapping.getChannel());
			}
			longestTitle = 0;
			shortestCallSign = 999;
		}
		
		if (local.equalsIgnoreCase("display-name")) {
			//XMLTV doesn't tell us what the title, call sign, or channel number is.  It just gives
			//a bunch of display-name tags, so we have to make educated guesses on which is which.
			if (currentString != null) {
				if (currentString.length() > longestTitle && 
						currentString.charAt(currentString.length()-2) != ':') {
					currentChannel.setTitle(currentString);
					longestTitle = currentString.length();
				}
				if (currentString.length() < shortestCallSign && 
						currentString.toUpperCase().equals(currentString)) {
					try {Integer.parseInt(currentString);}
					catch (NumberFormatException e) {
						currentChannel.setCallSign(currentString);
						shortestCallSign = currentString.length();
					}
				}
				if (currentMapping.getChanNum() == 0) {
					try {currentMapping.setChanNum(Integer.parseInt(currentString));} 
					catch (NumberFormatException e) {}
				}
			}
		}
		
		if (local.equalsIgnoreCase("programme")) {
			currentSchedule.setProgram(currentProgram);
			Channel channel = Channel.findChannel(channels, currentSchedule.getChanId());
			if (channel != null) channel.addSchedule(currentSchedule);
		}
		
		if (local.equalsIgnoreCase("title")) {
			if (currentString != null && currentProgram != null)
				currentProgram.setTitle(currentString);
		}
		
		if (local.equalsIgnoreCase("sub-title")) {
			if (currentString != null && currentProgram != null)
				currentProgram.setSubtitle(currentString);
		}
		
		if (local.equalsIgnoreCase("category")) {
			if (currentString != null && currentProgram != null) {
				if (currentString.equalsIgnoreCase("movie"))
					currentProgram.setMovie(true);
				else if (!currentProgram.hasGenre(currentString)) {//Check for duplicates, XMLTV has a lot
					Genre genre = new Genre();
					genre.setGenre(currentString);
					genre.setProgId(currentProgram.getProgramId());
					currentProgram.addGenre(genre);
				}
				
			}
		}
		
		if (local.equalsIgnoreCase("desc")) {
			if (currentString != null && currentProgram != null)
				currentProgram.setDesc(currentString);
		}
		
		if (local.equalsIgnoreCase("credits")) 
			readingCrew = false;
		
		if (readingCrew) {
			if (local.equalsIgnoreCase("director")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Director");
			}
			
			if (local.equalsIgnoreCase("actor")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Actor");
			}
			
			if (local.equalsIgnoreCase("producer")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Producer");
			}
			
			if (local.equalsIgnoreCase("writer")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Writer");
			}
			
			if (local.equalsIgnoreCase("presenter")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Presenter");
			}
			
			if (local.equalsIgnoreCase("guest")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Guest");
			}
			
			if (local.equalsIgnoreCase("commentator")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Commentator");
			}
			
			if (local.equalsIgnoreCase("adapter")) {
				if (currentString != null && currentProgram != null)
					addCrew(currentString, "Adapter");
			}
		}
		
		if (local.equalsIgnoreCase("rating"))
			readingRating = false;

		if (local.equalsIgnoreCase("value")) {
			if (currentString != null && currentProgram != null) {
				if (readingRating) {
					if (isAdvisory) currentProgram.addAdvisory(currentString);
					else currentProgram.setRating(currentString);
				}
				else if (readingStarRating)
					currentProgram.setStarRating(currentString);
			}
		}
		
		if (local.equalsIgnoreCase("star-rating"))
			readingStarRating = false;
		
		if (local.equalsIgnoreCase("quality")) {
			if (currentString != null && currentSchedule != null && 
					currentString.equalsIgnoreCase("hdtv"))
				currentSchedule.setHdtv(true);
		}
		
		currentString = "";
	}
	
	/**
	 * Parses a date in the XMLTV date format and returns the time in millis
	 * 
	 * @param date - The date string to be parsed
	 * @return - The time in millis
	 * @throws ParseException
	 */
	public static long parseXmltvDate(String date) throws ParseException {
    	if (date.split(" ").length == 1) date = date + " -0000";

		return format.parse(date).getTime();
    }
	
	
	/**
	 * Creates a Crew object and adds it to the current Program
	 * 
	 * @param name - The full name of the crew member.
	 * 				 This will be split into first and last name
	 * @param role - The role of the crew member (actor, director, etc.)
	 */
	public void addCrew(String name, String role) {
		Crew crew = new Crew();
		crew.setRole(role);
		String splitName[]  = name.split(" ", 2);
		crew.setFirstName(splitName[0]);
		if (splitName.length > 1)
			crew.setLastName(name.split(" ", 2)[1]);
		else crew.setLastName(" ");
		crew.setProgId(currentProgram.getProgramId());
		currentProgram.addCrew(crew);
	}
	
	@Override
	public void finish() {	}

}
