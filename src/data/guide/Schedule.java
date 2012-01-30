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

package data.guide;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.Options.ShowCrew;

/**
 * Representation of a Scheduled time slot
 * Contains a Program object that is in the time slot
 * 
 * @author Louis Amstutz
 */
public class Schedule implements Serializable{
	private static final long serialVersionUID = -6119307686707434796L;
	
	/** Start and stop times of this schedule */
	private long start, stop;
	
	/** Program that is in this scheduled time slow */
	private Program program;
	
	/** Channel ID of the channel that contains this schedule */
	private String chanId;
	
	/** program ID of the program contained by this schedule.  This is kept to make it easier
	 * to load schedules from an SQL database, since when we load them we have the program ID
	 * but don't have the program object yet
	 */
	private String progId;
	
	/** TV Rating of this schedule.  Transfered to the program object when the program is added */
	private String rating=null;
	
	/** True if shown in high definition */
	private boolean hdtv=false;
	
	
	public long getStart() {return start;}
	public long getStop() {return stop;}
	public Program getProgram() {return program;}
	public String getChanId() {return chanId;}
	public String getProgId() {return progId;}
	public String getRating() {return rating;}
	public boolean getHdtv() {return hdtv;}
	
	public void setStart(long _start) {start = _start;}
	public void setStop(long _stop) {stop = _stop;}
	public void setProgram(Program _program) {
		program = _program;
		if (rating != null && program.getRating().equals("N/A")) program.setRating(rating);
	}
	public void setChanId(String _chanId) {chanId = _chanId;}
	public void setProgId(String _progId) {progId = _progId;}
	public void setRating(String _rating) {rating = _rating;}
	public void setHdtv(boolean _hdtv) {hdtv = _hdtv;}
	
	@Override
	public String toString(){
		if (program != null) return program.toString();
		else return "";
	}
	
	/** 
	 * Returns an HTML String representation of this object
	 * 
	 * @param showCrew - Whether to show the crew, don't show the crew, or only if movie
	 * @param fontSize - Font size
	 * @return The HTML String
	 */
	public String toHtml(ShowCrew showCrew, int fontSize) {
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		StringBuffer str = new StringBuffer();
		long duration = (stop - start)/60000;
		str.append("<html><p style=font-size:" + fontSize + ">Start time: " + 
				format.format(new Date(start)) + ", Duration: " + duration + " minutes<br>");
		if (hdtv) str.append("hdtv<br>");
		if (program != null) str.append("<br>" + program.toHtml(showCrew));
		str.append("<p></html>");
		return str.toString();
	}
}
