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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;


import data.guide.*;

/**
 * Parses TV listings in the Schedules Direct XML format
 * 
 * @author Louis Amstutz
 */
public class SDParser extends TVParser {
	/** The dates and times of XML listings are in UTC, so this time zone is used to parse them */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private static final TimeZone timezone = new SimpleTimeZone(0, "UTC");
	
	private Crew currentCrew;
	private Genre currentGenre;
	
	private ArrayList<Schedule> schedules = new ArrayList<Schedule>();
	private ArrayList<Program> programs = new ArrayList<Program>();
	
	
	@Override
	public void startDocument(){
		dateFormat.setTimeZone(timezone);
	}


	@Override
	public void startElement(String uri, String local, 
			String raw, Attributes attrs)  throws SAXException {
		
		if (grabWorker.isCancelled()) throw new SAXException();
		
		currentString = "";

		if (local.equals("xtvd")) {
			long start=0, stop=0;
			for (int i=0; i<attrs.getLength(); i++) {
				
				if (attrs.getQName(i).equalsIgnoreCase("from")) {
					try {
						start = dateFormat.parse(attrs.getValue(i)).getTime();
					}catch (ParseException e) { e.printStackTrace();}
				}
				if (attrs.getQName(i).equalsIgnoreCase("to")) {
					try {
						stop = dateFormat.parse(attrs.getValue(i)).getTime();
					}catch (ParseException e) { e.printStackTrace();}
				}
			}
			data.setStartTime(start);
			data.setStopTime(stop);
		}
		
		if (local.equals("stations")) {
			grabWorker.setProgressPublic(20);
			grabWorker.publishPublic("Downloading channels");
		}
		
		if (local.equals("station")) {
			channelCount++;
			currentChannel = new Channel();
			if (attrs != null && attrs.getQName(0).equalsIgnoreCase("id")) {
				currentChannel.setId(attrs.getValue(0));
			}
			
		}
		
		if (local.equals("map")) {
			if (attrs != null) {
				String chanId = "";
				int chanNum = 0;
				for (int i=0; i<attrs.getLength(); i++) {
					if (attrs.getQName(i).equals("station")) chanId = attrs.getValue(i);
					if (attrs.getQName(i).equals("channel")) {
						try {chanNum = Integer.parseInt(attrs.getValue(i));	}
						catch (NumberFormatException e) {System.out.println(e.toString());}
					}
				}
				Channel chan = Channel.findChannel(channels, chanId);
				Mapping map = new Mapping(chan, chanNum);
				if (chan != null && !data.getChannelMappings().contains(map)) 
					data.getChannelMappings().add(map);
			}
		}
		
		if (local.equals("schedules")) {
			grabWorker.publishPublic("Downloading Schedules");
			numScheduleEstimate = (int)(channelCount*numHours*1.1);//1.1 programs an hour on average
		}
		
		if (local.equals("schedule")) {
			grabWorker.setProgressPublic(25 + (int)( (double)scheduleCount++/numScheduleEstimate*15));
			Schedule currentSchedule = new Schedule();
			
			if (attrs != null) {
				for (int i=0; i<attrs.getLength(); i++) {
					if (attrs.getQName(i).equals("time")) {
						try {
							Date date = dateFormat.parse( (attrs.getValue(i)) );
							currentSchedule.setStart(date.getTime());
						}
						catch (ParseException e) {e.printStackTrace();}
					}
					
					if (attrs.getQName(i).equals("duration")) {
						long duration=0;
						int hours = Integer.parseInt(attrs.getValue(i).substring(2, 4));
						int minutes = Integer.parseInt(attrs.getValue(i).substring(5, 7));
						duration += (hours * 3600000) + (minutes * 60000);
						currentSchedule.setStop(currentSchedule.getStart() + duration);
					}
					
					if (attrs.getQName(i).equals("station")) {
						currentSchedule.setChanId(attrs.getValue(i));
					}	
					if (attrs.getQName(i).equals("program")) {
						currentSchedule.setProgId(attrs.getValue(i));
					}
					if (attrs.getQName(i).equals("rating")) {
						currentSchedule.setRating(attrs.getValue(i));
					}
					if (attrs.getQName(i).equals("hdtv")) {
						if (attrs.getValue(i).equals("true")) currentSchedule.setHdtv(true);
						else currentSchedule.setHdtv(false);
					}
				}
			}
			schedules.add(currentSchedule);
		}
		
		if (local.equals("programs")) {
			grabWorker.publishPublic("Downloading Programs");
			numProgramsEstimate = (int)(scheduleCount/2.2);//1 program per 2.2 schedules on average
		}
		
		if (local.equals("program")) {
			grabWorker.setProgressPublic(40 + (int)( (double)programCount++/numProgramsEstimate*20));
			currentProgram = new Program();
			if (attrs != null && attrs.getQName(0).equals("id")) {
				currentProgram.setProgramId(attrs.getValue(0));
				if (attrs.getValue(0).substring(0, 2).equals("MV")) currentProgram.setMovie(true);
			}		
		}
		
		if (local.equals("productionCrew")) {
			numCrewEstimate = (int)(programCount*1.1);//1.1 crew per program on average
			grabWorker.publishPublic("Downloading Crew");
		}
		
		if (local.equals("crew")) {
			grabWorker.setProgressPublic(60 + (int)( (double)crewCount++/numCrewEstimate*20));
			if (attrs != null && attrs.getQName(0).equals("program"))
				currentProgram = Program.findProgram(programs, attrs.getValue(0));	
		}
		
		if (local.equals("programGenre")) {
			grabWorker.setProgressPublic(80 + (int)( (double)genreCount++/numGenreEstimate*20));
			if (attrs != null && attrs.getQName(0).equals("program"))
				currentProgram = Program.findProgram(programs, attrs.getValue(0));	
		}
		
		if (local.equals("member")) {
			currentCrew = new Crew();	
			currentCrew.setProgId(currentProgram.getProgramId());
		}
		
		if (local.equals("genres")) {
			numGenreEstimate = (int)(programCount*1.3);//1.3 genres per program on average
			grabWorker.publishPublic("Downloading Genres");
		}
		
		if (local.equals("genre")) {
			grabWorker.setProgressPublic(80 + (int)( (double)genreCount++/numGenreEstimate*20));
			currentGenre = new Genre();	
			currentGenre.setProgId(currentProgram.getProgramId());
		}
	}
	
	@Override
	public void endElement(String uri, String local, String raw) throws SAXException {
		if (grabWorker.isCancelled()) throw new SAXException();
		
		if (local.equals("station")) channels.add(currentChannel);
		
		if (local.equals("stations")) Collections.sort(channels);
		
		if (local.equals("callSign")) currentChannel.setCallSign(currentString);
		
		if (local.equals("name")) currentChannel.setTitle(currentString);
		
		if (local.equals("program")) programs.add(currentProgram);
		
		if (local.equals("title")) currentProgram.setTitle(currentString);
		
		if (local.equals("subtitle")) currentProgram.setSubtitle(currentString);
		
		if (local.equals("description")) currentProgram.setDesc(currentString);
		
		if (local.equals("originalAirDate")) currentProgram.setOriginalAirDate(currentString);
		
		if (local.equals("mpaaRating")) currentProgram.setRating(currentString);
		
		if (local.equals("year")) currentProgram.setYear(currentString);
		
		if (local.equals("syndicatedEpisodeNumber")) {currentProgram.setEpisodeNo(currentString);}
		
		if (local.equals("advisory")) currentProgram.addAdvisory(currentString);
		
		if (local.equals("role")) currentCrew.setRole(currentString);
		
		if (local.equals("givenname")) currentCrew.setFirstName(currentString);
		
		if (local.equals("surname")) currentCrew.setLastName(currentString);
		
		if (local.equals("member")) {
			if (currentProgram != null) {
				currentCrew.setProgId(currentProgram.getProgramId());
				currentProgram.addCrew(currentCrew);
			}
		}
		
		if (local.equals("class")) currentGenre.setGenre(currentString);
		
		if (local.equals("relevance")) currentGenre.setRelevance(Integer.parseInt(currentString));
		
		if (local.equals("genre")) {
			if (currentProgram != null) {
				currentGenre.setProgId(currentProgram.getProgramId());
				currentProgram.addGenre(currentGenre);
			}
		}
		
		currentString = "";
	}
	
	@Override
	public void finish() {
		for (int i=0; i<schedules.size(); i++) {
			Schedule sched = schedules.get(i);
			sched.setProgram(Program.findProgram(programs, sched.getProgId()));
			Channel.findChannel(channels, sched.getChanId()).addSchedule(sched);
		}
	}
}
