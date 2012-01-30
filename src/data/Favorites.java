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

package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import core.TVRadar;
import data.guide.Mapping;

/**
 * Stores the user's favorite programs and channels
 * 
 * Note that we only store the title of programs, not the entire object,
 * because that why we can find different episodes/events of the same program
 * 
 * @author Louis Amstutz
 */
public class Favorites implements Serializable {
	private static final long serialVersionUID = -7591691746264413243L;

	/** Filename where favorites will be stored */
	private static final String FAVORITES_FILENAME = "favorites.dat";
	
	private List<Mapping> favoriteChannels;
	private List<String> favoritePrograms;
	
	/**
	 * Constructor
	 */
	public Favorites() {
		favoriteChannels = new ArrayList<Mapping>();
		favoritePrograms = new ArrayList<String>();
	}
	
	public List<Mapping> getFavoriteChannels() {return favoriteChannels;}

	public List<String> getFavoritePrograms() {return favoritePrograms;}
	
	/**
	 * Add a channel to favorites
	 * 
	 * @param channel - Channel to add
	 */
	public void addChannel(Mapping map) {
		if (!favoriteChannels.contains(map)) favoriteChannels.add(map);
	}
	
	/**
	 * Add a program to favorites
	 * 
	 * @param programTitle - Title of program to add
	 */
	public void addProgram(String programTitle) {
		if (!favoritePrograms.contains(programTitle)) favoritePrograms.add(programTitle);
	}
	
	/**
	 * Load Favorites from disk
	 * 
	 * @return - Favorites object that was loaded
	 */
	public static Favorites loadFavorites() {
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(TVRadar.getUserDir() + FAVORITES_FILENAME);
			in = new ObjectInputStream(fis);
			Favorites favorites = (Favorites)in.readObject();
			in.close();
			return favorites;
		}
		catch(IOException ex){return new Favorites();}
		catch(ClassNotFoundException ex){
			ex.printStackTrace();
			return new Favorites();
		}
	}
	
	/**
	 * Saves favorites to disk
	 * 
	 * @param favorites - Favorites object to save
	 */
	public static void saveFavorites(Favorites favorites) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(TVRadar.getUserDir() + FAVORITES_FILENAME);
			out = new ObjectOutputStream(fos);
			out.writeObject(favorites);
			out.close();
		}
		catch(IOException ex){ex.printStackTrace();}
	}
}
