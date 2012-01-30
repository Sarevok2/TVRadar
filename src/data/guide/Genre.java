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
 * Representation of a Genre
 * 
 * @author Louis Amstutz
 */
public class Genre implements Comparable<Genre>, Serializable{
	private static final long serialVersionUID = -3086639964669537439L;
	
	/** Name of the genre */
	private String genre;
	
	/** Program ID of program this genre is associated with.  Higher numbers are more relevant */
	private String progId;
	
	/** How relevent this genre is to the program */
	private int relevance=0;
	
	public String getGenre(){return genre;}
	public String getProgId(){return progId;}
	public int getRelevance() {return relevance;}
	
	public void setGenre(String _genre) {genre = _genre;}
	public void setRelevance(int _rel) {relevance = _rel;}
	public void setProgId(String _progId) {progId = _progId;}
	
	@Override
	public String toString() {return genre;}
	
	@Override
	public int compareTo(Genre gen) {
		if (this.relevance > gen.getRelevance()) return 1;
		if (this.relevance < gen.getRelevance()) return -1;
		return 0;
	}
}
