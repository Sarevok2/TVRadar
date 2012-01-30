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
 * Data structure which all listings data is stored in
 * 
 * @author Louis Amstutz
 */
public class GuideData implements Serializable  {
	private static final long serialVersionUID = 4457974021063932463L;
	
	/** Start and stop times of listing data in milliseconds */
	private long startTime, stopTime;
	
	/** List of channels.  The channels contain all other data, such as schedule programs, crew, etc. */
	private ArrayList<Mapping> channelMappings;
	
	
	public long getStartTime() {return startTime;}
	public long getStopTime() {return stopTime;}
	public ArrayList<Mapping> getChannelMappings() {return channelMappings;}
	
	public void setStartTime(long s) {startTime = s;}
	public void setStopTime(long s) {stopTime = s;}
	public void setChannelMappings(ArrayList<Mapping> c) {channelMappings = c;}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<channelMappings.size(); i++) {
			buffer.append(channelMappings.get(i).toString());
		}
		return buffer.toString();
	}
}
