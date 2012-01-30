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
import java.util.ArrayList;

/**
 * Representation of a TV channel
 * 
 * @author Louis Amstutz
 */
public class Channel implements Comparable<Channel>, Serializable {
	private static final long serialVersionUID = 7018991422831941420L;
	
	/** The possible values for sortComparable */
	// static final int SORT_BY_CHANNUM = 0, SORT_BY_CHANID = 1, SORT_BY_TITLE = 2;
	
	/** The unique channel ID of this channel */
	private String chanId="";
	
	/** The title of this channel */
	private String title="Untitled";
	
	/** Call sign of this channel */
	private String callSign="";
	
	/** Schedule programs on this channel */
	private ArrayList<Schedule> schedules = new ArrayList<Schedule>();
	
	
	public String getChanId() {return chanId;}
	public String getCallSign() {return callSign;}
	public String getTitle() {return title;}
	public ArrayList<Schedule> getSchedules () {return schedules;}
	
	public void setTitle(String _title) {title = _title;}
	public void setId(String _id) {chanId = _id;}
	public void setCallSign(String _callSign) {callSign = _callSign;}
	
	/**
	 * Add a scheduled program to this channel
	 * 
	 * @param sched - Schedule to add
	 */
	public void addSchedule(Schedule sched) {
		schedules.add(sched);
	}
	
	/**
	 * Return an HTML string representation of this Channel
	 * 
	 * This is used for the tooltip popup when the user hovers the mouse over the channel
	 * 
	 * @param fontSize - Font size to use
	 * @return - HTML String
	 */
	public String toHtml(int fontSize) {
		StringBuilder result = new StringBuilder();
		if (title != null && !title.equals(""))result.append("Title: " + title + "<br>");
		if (callSign != null && !callSign.equals("")) result.append("Callsign: " + callSign);
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (callSign != null && !callSign.equals("")) result.append(callSign);
		if (result.length() == 0 && title != null) result.append(title);
		return result.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Channel) return this.chanId.equals( ((Channel)object).getChanId() );
		else return false;
	}
	
	@Override
	public int compareTo(Channel ch) {
		return this.getChanId().compareTo(ch.getChanId());
	}

	//public static int getComparable() {return sortComparable;}
	
	//public static void setComparable(int comp){sortComparable = comp;}
	
	/**
	 * Searches for a channel in a list
	 * 
	 * @param sortedList - List of channels to search
	 * @param chanID - Channel ID of channel to find
	 * @return Channel object found.  Null of none found
	 */
	public static Channel findChannel(ArrayList<Channel> sortedList, String chanID) {
		if (sortedList == null || sortedList.size() == 0) return null;
		int startIndex = 0;
		int endIndex = sortedList.size() - 1;
		int currentIndex, previousIndex = -1;
		
		if (chanID.equals(sortedList.get(endIndex).getChanId())) {
			return sortedList.get(endIndex);
		}
		while (true) {
			currentIndex = (startIndex + endIndex) / 2;
			if (currentIndex == previousIndex) return null;
			Channel currentChan = sortedList.get(currentIndex);
			if (chanID.equals(currentChan.getChanId())){
				return currentChan;
			}
			else {
				if (chanID.compareTo(sortedList.get(currentIndex).getChanId()) > 0){
					startIndex = currentIndex;
				}
				else {
					endIndex = currentIndex;
				}
				previousIndex = currentIndex;
			}
		}	
	}
	
	/**
	 * Inserts channel into list in sorted order
	 * The list must already be in sorted order for this to work
	 * 
	 * @param sortedList - List to insert channel into
	 * @param chan - Channel to insert
	 */
	public static void insertSorted(ArrayList<Channel> sortedList, Channel chan) {
		
		for (int i=0; i<sortedList.size(); i++) {
			if (chan.compareTo(sortedList.get(i)) < 0) {
				sortedList.add(i, chan);
				return;
			}
		}
		sortedList.add(chan); //Add it if sortedList is empty
	}
	
	
}
