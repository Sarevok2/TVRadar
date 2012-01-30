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

import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

import data.Options.ShowCrew;

/**
 * Representation of a TV program
 * 
 * This doesn't contain any scheduling information.  Program objects are contained by one or
 * more Schedule objects which contain the scheduling information.
 * 
 * @author Louis Amstutz
 */
public class Program implements Comparable<Program>, Serializable{
	private static final long serialVersionUID = 1720416545444387835L;
	
	/** Line length till word wrap when formatting HTML String representation of this program */
	private static final int MAX_LINE_LENGTH=60;
	
	/** Title */
	private String title="N/A";
	
	/** Subtitle */
	private String subtitle="N/A";
	
	/** Description */
	private String description="N/A";
	
	/** Unique program ID */
	private String programID;
	
	/** Original air date */
	private String originalAirDate="N/A";
	
	/** MPAA, VCHIP or other rating */
	private String rating="N/A";
	
	/** Year released.  Mostly for movies */
	private String year="N/A";
	
	/** Episode number */
	private String episodeNo="N/A";
	
	/** Star rating.  Mostly for movies */
	private String starRating="N/A";
	
	/** True if this is a movie */
	private boolean isMovie=false;
	
	/** List of genres for this program stored in order of relevance */
	private ArrayList<Genre> genres;
	
	/** List of crew members */
	private ArrayList<Crew> crew;
	
	/** List of advisories (violence, nudity, etc.) */
	private ArrayList<String> advisories;
	
	/**
	 * Constructor
	 */
	public Program() {
		genres = new ArrayList<Genre>();
		crew = new ArrayList<Crew>();
		advisories = new ArrayList<String>();
	}
	
	public String getTitle() {return title;}
	public String getSubtitle() {return subtitle;}
	public String getDescription() {return description;}
	public String getProgramId() {return programID;}
	public String getYear() {return year;}
	public String getRating() {return rating;}
	public String getStarRating() {return starRating;}
	public String getEpisodeNo() {return episodeNo;}
	public String getOriginalAirDate() {return originalAirDate;}
	public boolean getMovie() {return isMovie;}
	public ArrayList<Crew> getCrew() {return crew;}
	public ArrayList<String> getAdvisories() {return advisories;}
	public ArrayList<Genre> getGenres() {return genres;}
	public Genre getBestGenre() {
		if (genres.size()>0) return genres.get(0);
		else return null;
	}
	
	public void setTitle(String _title) {title = _title;}
	public void setSubtitle(String _subtitle) {subtitle = _subtitle;}
	public void setDesc(String _desc) {description = _desc;}
	public void setProgramId(String _pid) {programID = _pid;}
	public void setOriginalAirDate(String _oad) {originalAirDate = _oad;}
	public void setYear(String _year) {year = _year;}
	public void setRating(String _rating) {rating = _rating;}
	public void setStarRating(String _rating) {starRating = _rating;}
	public void setEpisodeNo(String _episodeNo) {episodeNo = _episodeNo;}
	public void setMovie(boolean _movie) {isMovie = _movie;}
	
	/**
	 * Check to see if this program contains a genre
	 * 
	 * @param genre - Name of genre
	 * @return true if program contains the genre, false otherwise
	 */
	public boolean hasGenre(String genre) {
		for (int i=0; i<genres.size(); i++) {
			if (genres.get(i).getGenre().equalsIgnoreCase(genre)) return true;
		}
		return false;
	}
	
	public void addCrew(Crew _crew) {
		crew.add(_crew);
	}
	
	public void addGenre(Genre genre) {
		genres.add(genre);
	}
	
	public void addAdvisory(String advisory) {
		advisories.add(advisory);
	}
	
	/** Sort genres by relevance */
	public void sortGenres() {
		Collections.sort(genres);
	}
	
	/**
	 * Return an HTML String representation of this program
	 * 
	 * @param showCrew - Whether to show the crew, don't show the crew, or only show crew if this is a movie
	 * @return - The HTML String of this program
	 */
	public String toHtml(ShowCrew showCrew) {
		StringBuilder result = new StringBuilder();
		if (!title.equals("N/A")) result.append("<b>Title: " + title + "</b>");
		if (!subtitle.equals("N/A")) result.append("<br>Subtitle: " + subtitle);
		result.append("<br>");
		if (!description.equals("N/A")) result.append("<br>Description:<br>" + 
				insertLineBreaks(description) + "<br>");
		if (!originalAirDate.equals("N/A")) result.append("<br>Original Air Date: " + originalAirDate);
		if (!year.equals("N/A")) result.append("<br>Year: " + year);
		if (!starRating.equals("N/A")) result.append("<br>Star Rating: " + starRating);
		if (!rating.equals("N/A")) result.append("<br>Rating: " + rating);
		if (!episodeNo.equals("N/A")) result.append("<br>Episode No.: " + episodeNo);
		
		if (genres.size()>0) {
			result.append("<br>Genres: ");
			for (int i=0; i<genres.size(); i++) {
				result.append(genres.get(i).toString() + ", ");
			}
			result.delete(result.length()-2, result.length()-1);
		}
		
		
		if (advisories.size()>0) {
			result.append("<br><br>Advisories: ");
			for (int i=0; i<advisories.size(); i++) {
				result.append(advisories.get(i).toString() + ", ");
			}
			result.delete(result.length()-2, result.length()-1);
		}	
		
		if ( (showCrew == ShowCrew.YES || 
				(showCrew == ShowCrew.MOVIES && isMovie)) && crew.size()>0) {
			result.append("<br><br>Crew: ");
			for (int i=0; i<crew.size(); i++) {
				result.append("<br>" + crew.get(i).toString());
			}
		}
		
		
		return result.toString();
	}
	
	/**
	 * Insert HTML line breaks into the string based on the value of MAX_LINE_LENGTH
	 * 
	 * @param text - Text to insert line breaks into
	 * @return Resulting HTML String
	 */
	public static String insertLineBreaks(String text) {
		StringBuilder result = new StringBuilder(text);
		int currentIndex = 0, prevIndex = 0, numLineBreaks=0;
		
		while ( (currentIndex += MAX_LINE_LENGTH) < text.length()) {
			
			while (text.charAt(currentIndex) != ' ' && currentIndex > prevIndex) {currentIndex--;}
			if (currentIndex == prevIndex) {
				currentIndex += MAX_LINE_LENGTH;
				while (text.charAt(currentIndex) != ' ' && currentIndex < text.length()) {currentIndex++;}
			}
			
			if (currentIndex < text.length() - 1) 
				result.insert(currentIndex + (numLineBreaks++ * 4), "<br>");

			prevIndex = currentIndex;
		}
		return result.toString();
	}
	
	@Override
	public String toString() {return title;}
	
	@Override
	public boolean equals(Object object) {
		if (object instanceof Program) 
			return this.programID.equals( ((Program)object).getProgramId() );
		else return false;
	}

	public int compareTo(Program prog) {
		return this.getProgramId().compareTo(prog.getProgramId());
	}
	
	/**
	 * Search for a program in a list
	 * 
	 * @param sortedList - List to search in
	 * @param progId - Program ID of program to search for
	 * @return - Found program.  Null if none was found
	 */
	public static Program findProgram(ArrayList<Program> sortedList, String progId) {
		if (sortedList == null || sortedList.size() == 0) return null;
		int startIndex = 0;
		int endIndex = sortedList.size() - 1;
		int currentIndex, previousIndex = -1;
		
		if (progId.equals(sortedList.get(endIndex).getProgramId())) {
			return sortedList.get(endIndex);
		}
		
		while (true) {
			currentIndex = (startIndex + endIndex) / 2;
			if (currentIndex == previousIndex) return null;
			Program currentProg = sortedList.get(currentIndex);
			if (progId.equals(currentProg.getProgramId())){
				return currentProg;
			}
			else {
				if (progId.compareTo(sortedList.get(currentIndex).getProgramId()) > 0){
					startIndex = currentIndex;
				}
				else {
					endIndex = currentIndex;
				}
				previousIndex = currentIndex;
			}
		}	
	}
}
