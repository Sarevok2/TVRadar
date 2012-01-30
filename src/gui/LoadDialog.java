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

package gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import core.TVRadar;
import core.load.LoadListingsWorker;
import data.guide.GuideData;


/**
 * Dialog to load TV listings from disk
 * 
 * @author Louis Amstutz
 */
public class LoadDialog extends JDialog {
	private static final long serialVersionUID = 2091344641335021758L;

	private TVRadar tvRadar;
	private MainGui mainGui;
	
	/** Worker thread that will load the listings */
	private LoadListingsWorker loadWorker;
	
	
	///////////////////UI Elements//////////////////////
	private JPanel contentPanel;
	private JLabel showListingsLabel;
	private JButton cancelButton;
	private JProgressBar progressBar;
	
	private LoadActionListener laListener;
	private LoadPropertyChangeListener lpcListener;
	
	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to TVRadar object
	 * @param _mg - Main gui and parent of this dialog
	 */
	public LoadDialog(TVRadar _tvr, MainGui _mg) {
		super(_mg, "Loading", true);
		tvRadar = _tvr;
		mainGui = _mg;
		
		laListener = new LoadActionListener();
		lpcListener = new LoadPropertyChangeListener();
		
		createPanel();
		getContentPane().add(contentPanel);
		startLoadThread();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing ( WindowEvent w ) {
				cancelLoad();
			}
		});
		this.pack();
		this.setLocation(200, 200);
		this.setVisible(true);
	}
	
	/**
	 * Create the panel
	 */
	private void createPanel() {
		contentPanel = new JPanel();
		contentPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		showListingsLabel = new JLabel("Loading listings.  Please wait.");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,10,10);
		contentPanel.add(showListingsLabel, c);

		progressBar = new JProgressBar(0,100);
		progressBar.setPreferredSize(new Dimension(300,20));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,10,10);
		contentPanel.add(progressBar,c);	
		
		JLabel emptyLabel = new JLabel("");
		emptyLabel.setPreferredSize(new Dimension(200,26));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		contentPanel.add(emptyLabel,c);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(75,26));
		cancelButton.addActionListener(laListener);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		contentPanel.add(cancelButton,c);	
	}
	
	/**
	 * Action Listener for LoadDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class LoadActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				cancelLoad();
			}
		}
	}
	
	/**
	 * Property change listener for this dialog
	 * 
	 * Monitors property change events of the LoadListingsWorker which are
	 * fired when the progress is updated.
	 * 
	 * @author Louis Amstutz
	 */
	private class LoadPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName() == "progress") {
				int progress = (Integer) e.getNewValue();
				progressBar.setValue(progress);
			}
		}
	}
	
	/**
	 * Start the LoadListingsThread and timer
	 */
	private void startLoadThread() {
		loadWorker = new LoadListingsWorker(tvRadar, this);
		loadWorker.addPropertyChangeListener(lpcListener);
		loadWorker.execute();
	}
	
	/**
	 * Called when the LoadListingsWorker is finished
	 * 
	 * If successful it calls MainGui.addGuidePain() to display the listings.
	 * If the listings are out of date it prompts the user to download new ones
	 */
	public void finishLoadSuccess(GuideData data) {
		
		if (data.getStopTime() < System.currentTimeMillis()) {
			int response = JOptionPane.showConfirmDialog(this, "Listings are out of date.  " +
					"Would you like to download new ones?", null, JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) {
				dispose();
				mainGui.openGrabListingsDialog();
				return;
			}
		}
		tvRadar.setData(data);
		mainGui.addGuidePane();
		dispose();
	}
	
	/**
	 * Called if if the LoadListingsWorker threw an exception
	 * Displays the error
	 * @param e - the exception/error which was thrown
	 */
	public void finishLoadError(Throwable e) {
		showListingsLabel.setForeground(Color.RED);
		cancelButton.setText("Close");
		String errorMessage = e.getMessage();
		if (errorMessage == null) errorMessage = e.getClass().getSimpleName();
		showListingsLabel.setText(errorMessage);
	}
	
	/**
	 * Called if the use cancels the load operation
	 */
	public void cancelLoad() {
		if (loadWorker != null) loadWorker.cancel(false);
		dispose();
	}
}
