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

package core.load;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import data.guide.GuideData;

/**
 * Classes that load listings from disk implement this
 * 
 * @author Louis Amstutz
 */
public interface ListingLoader {
	
	/**
	 * Load the data and generate a GuideData object
	 * 
	 * @param worker - Reference to parent LoadListingsWorker for progress updates
	 * @return - The GuideData object which was generated
	 */
	public GuideData loadData(LoadListingsWorker worker) throws FileNotFoundException,
			IOException, ClassNotFoundException, SQLException;
	
	/**
	 * Check if there is any data to be loaded
	 * 
	 * @return - Whether there is any data
	 */
	public boolean dataExists();
}
