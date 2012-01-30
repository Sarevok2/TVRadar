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

import gui.LoadDialog;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import core.TVRadar;
import data.guide.GuideData;

public class LoadListingsWorker extends SwingWorker<GuideData, Void> {
	private TVRadar tvr;
	private LoadDialog ld;
	
	public LoadListingsWorker(TVRadar _tvr, LoadDialog _ld) {
		tvr = _tvr;
		ld = _ld;
	}
	
	@Override
	protected GuideData doInBackground() throws Exception {
		ListingLoader loader;
		if (tvr.getOptions().isUseSQL()) 
			loader = new SQLLoader(tvr.getOptions().getMySqlPwd(), tvr.getOptions().getMySqlUrl());
		else  loader = new SerializedLoader();
		
		return loader.loadData(this);
	}
	
	@Override
	protected void done() {
		try {ld.finishLoadSuccess(get());} 
		catch (java.util.concurrent.CancellationException e) {}
		catch (InterruptedException e) {} 
		catch (ExecutionException e) {
			ld.finishLoadError(e.getCause());
		}
	}

	/**
	 * Allows public access to set the progress
	 * 
	 * @param progress
	 */
	public void setProgressPublic(int progress) {
		setProgress(Math.min(progress, 100));
	}
}
