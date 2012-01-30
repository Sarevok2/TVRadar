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

package core.store;

import java.io.*;

import core.TVRadar;
import data.guide.GuideData;

/**
 * Stores a serialised GuideData object to disk
 * 
 * @author Louis Amstutz
 */
public class StorageSerialized implements StorageInterface {
	/** Filename that the object will be stored as */
	public static final String STORAGE_FILENAME = "listings.dat";

	@Override
	public void store(GuideData data, StorageWorker worker) throws IOException {
		MonitoredFileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		fos = new MonitoredFileOutputStream(new File(TVRadar.getUserDir() + STORAGE_FILENAME), worker);
		out = new ObjectOutputStream(new BufferedOutputStream(fos));
		out.writeObject(data);
		out.close();
	}
	
	/**
	 * Extension of the FileOutputStream that allows for monitoring progress
	 * 
	 * @author Louis Amstutz
	 */
	private class MonitoredFileOutputStream extends FileOutputStream {
		long maxBytes, writtenBytes=0;
		StorageWorker worker;
		
		//TODO: Estimate maxBytes instead of using size of previous file
		public MonitoredFileOutputStream(File file, StorageWorker _worker) throws FileNotFoundException {
			super(file);
			maxBytes = file.length();
			worker = _worker;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			super.write(b, off, len);
			writtenBytes += len;
			worker.setProgressPublic(Math.min((int)((double)writtenBytes/maxBytes*100),100));
		}
	}
}
