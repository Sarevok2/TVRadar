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

/**
 * Maps a channel to a channel number.
 * 
 * Note that a Channel can be mapped to multiple channel numbers
 * 
 * @author Louis Amstutz
 */
public class Mapping implements Comparable<Mapping>, Serializable{
	private static final long serialVersionUID = 4296438965845151696L;
	
	private Channel channel;
	private String chanID;
	private int chanNum=0;
	
	/**
	 * Default constructor
	 */
	public Mapping() {}
	
	/**
	 * Constructor
	 * 
	 * @param _channel
	 * @param _chanNum
	 */
	public Mapping(Channel _channel, int _chanNum) {
		channel = _channel;
		chanNum = _chanNum;
	}
	
	public Channel getChannel() {return channel;}
	public String getChanID() {return chanID;}
	public int getChanNum() {return chanNum;}
	
	public void setChannel(Channel c) {channel = c;}
	public void setChanID(String c) {chanID = c;}
	public void setChanNum(int n) {chanNum = n;}
	
	/**
	 * Return an HTML string representation of this channel mapping
	 * 
	 * This is used for the tooltip popup when the user hovers the mouse over the channel
	 * 
	 * @param fontSize - Font size to use
	 * @return - HTML String
	 */
	public String toHtml(int fontSize) {
		StringBuilder result = new StringBuilder();
		result.append("<html><p style=font-size:" + fontSize + ">");
		if (chanNum != 0) result.append("Channel No: " + chanNum + "<br>");
		if (channel != null) result.append(channel.toHtml(fontSize));
		result.append("<p></html>");
		
		return result.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		if (chanNum != 0) result.append(chanNum + " ");
		if (channel != null) result.append(channel.toString());
		return result.toString();
	}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Mapping) return chanNum == ((Mapping)object).chanNum;
		else return false;
	}
	
	@Override
	public int compareTo(Mapping map) {
		if (chanNum < map.chanNum) return -1;
		else if (chanNum > map.chanNum) return 1;
		else return 0;
	}
}
