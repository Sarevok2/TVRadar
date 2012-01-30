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

import gui.StorageDialog;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import data.Options;
import data.guide.GuideData;

/**
 * Thread to store GuideData to disk.
 * Uses either StorageSQL or StorageSerialized
 * 
 * @author Louis Amstutz
 */
public class StorageWorker extends SwingWorker<Void, Void> {
	Options options;
	GuideData data;
	StorageDialog storageDialog;
	
	/**
	 * Constructor
	 * 
	 * @param _options - Options
	 * @param _data - Data to be stored
	 */
	public StorageWorker(Options _options, GuideData _data, StorageDialog _sd) {
		options = _options;
		data = _data;
		storageDialog = _sd;
	}
	
	@Override
	public Void doInBackground() throws Exception {
		StorageInterface storage;
		
		if (options.isUseSQL()) 
			storage = new StorageSQL(options.getMySqlUrl(), options.getMySqlPwd(), options.isSmallerSqlPackets());
		else storage = new StorageSerialized();

		storage.store(data, this);
		
		return null;
	}
	
	@Override
	public void done() {
		try {
			get();//Called just so we can catch any exceptions here
			storageDialog.finishStoreSuccess();
		}
		catch (java.util.concurrent.CancellationException e) {}
		catch (InterruptedException e) {} 
		catch (ExecutionException e) {storageDialog.finishStoreError(e.getCause());}
	}
	
	public void setProgressPublic(int progress) {
		setProgress(Math.min(progress, 100));
	}
}
