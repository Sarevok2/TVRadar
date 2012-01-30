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

import java.io.*;

import core.store.StorageSerialized;
import core.TVRadar;
import data.guide.GuideData;

/**
 * Loads TV listings from a serialised GuideData object stored as a file
 * 
 * @author Louis Amstutz
 */
public class SerializedLoader implements ListingLoader {
	
	@Override
	public GuideData loadData(LoadListingsWorker worker) throws FileNotFoundException, IOException, 
			ClassNotFoundException {
		
		MonitoredFileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new MonitoredFileInputStream(new File(TVRadar.getUserDir() + 
					StorageSerialized.STORAGE_FILENAME), worker);
			in = new ObjectInputStream(new BufferedInputStream(fis));
			return (GuideData)in.readObject();
		}
		catch (StreamCorruptedException e) {
			throw new IOException("Loading Error: " + StorageSerialized.STORAGE_FILENAME + 
					" may be corrupt.", e);
		}
		catch (ClassNotFoundException e) {
			throw new IOException("Loading Error: " + StorageSerialized.STORAGE_FILENAME + 
					" may be corrupt.", e);
		}
		catch (ClassCastException e) {
			throw new IOException("Loading Error: " + StorageSerialized.STORAGE_FILENAME + 
					" may be corrupt.", e);
		}
		finally {in.close();}
	}
	
	@Override
	public boolean dataExists() {
		File file = new File(TVRadar.getUserDir() + StorageSerialized.STORAGE_FILENAME);
		return file.exists();
	}
	
	/**
	 * An extension of FileInputStream that allows progress monitoring
	 * 
	 * @author Louis Amstutz
	 */
	private class MonitoredFileInputStream extends FileInputStream {
		private long maxBytes, readBytes=0;
		private LoadListingsWorker worker;
		
		public MonitoredFileInputStream(File file, LoadListingsWorker _worker) throws FileNotFoundException {
			super(file);
			maxBytes = file.length();
			worker = _worker;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int count = super.read(b, off, len);
			readBytes += count;
			worker.setProgressPublic(Math.min((int)((double)readBytes/maxBytes*95),100) );
			return count;
		}
	}
}
