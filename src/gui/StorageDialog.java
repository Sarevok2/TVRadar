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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import core.TVRadar;
import core.store.StorageWorker;

/**
 * Dialog to store TV Listings to disk
 * 
 * It is a small, undecorated, non modal dialog that appears on the bottom right
 * corner of the screen after Listings are grabbed.  It shows the progress of
 * storing the listings to disk.  User can view the guide while listings are 
 * stored in the background.
 * 
 * @author Louis Amstutz
 */
public class StorageDialog extends JDialog {
	private static final long serialVersionUID = -7938448193143682482L;
	
	private TVRadar tvRadar;
	private MainGui mainGui;
	
	/** Worker thread that stores the listings */
	private StorageWorker storageWorker;
	
	///////////////////UI Elements//////////////////////
	private JPanel mainPanel;
	private JLabel statusLabel;
	private JButton cancelButton;
	private JProgressBar progressBar;
	
	private StorageActionListener saListener;
	private StoragePropertyChangeListener spcListener;
	
	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to TVRadar object which holds data
	 * @param mainGui - Main Gui and parent
	 */
	public StorageDialog(TVRadar _tvr, MainGui _mg) {
		super(_mg, null, false);
		
		mainGui = _mg;
		if (mainGui != null) mainGui.setStorageInProgress(true);
		tvRadar = _tvr;
		
		saListener = new StorageActionListener();
		spcListener = new StoragePropertyChangeListener();
		
		createPanel();
		getContentPane().add(mainPanel);
		startStorageThread();

		setUndecorated(true);
		pack();
		if (mainGui != null) {
			
			Dimension parentSize = mainGui.getSize();
			Dimension size = this.getSize();
			setLocationRelativeTo(mainGui);
			setLocation(parentSize.width-size.width-25, parentSize.height-size.height-25);
		}
		else {
			setLocation(300, 300);
			setAlwaysOnTop(true);
		}
		setVisible(true);
	}
	
	/**
	 * Create the panel
	 */
	private void createPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		statusLabel = new JLabel("Storing listings");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(3,3,2,0);
		mainPanel.add(statusLabel, c);

		progressBar = new JProgressBar(0,100);
		progressBar.setPreferredSize(new Dimension(200,10));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(3,0,5,5);
		mainPanel.add(progressBar,c);
		
		cancelButton = new JButton("Cancel");
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.insets = new Insets(0,2,2,2);
		mainPanel.add(cancelButton, c);
		cancelButton.addActionListener(saListener);
		
	}
	
	/**
	 * Action Listener for StorageDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class StorageActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				cancelStore();
			}
		}
	}
	
	/**
	 * Property change listener for this dialog
	 * 
	 * Monitors property change events of the StorageWorker which are
	 * fired when the progress is updated.
	 * 
	 * @author Louis Amstutz
	 */
	private class StoragePropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName() == "progress") {
				int progress = (Integer) e.getNewValue();
				progressBar.setValue(progress);
			}
		}
	}
	
	/**
	 * Start the storage thread and timer
	 */
	private void startStorageThread() {
		storageWorker = new StorageWorker(tvRadar.getOptions(), tvRadar.getData(), this);
		storageWorker.addPropertyChangeListener(spcListener);
		storageWorker.execute();
	}
	
	/**
	 * Called when the storage thread finishes successfully
	 */
	public void finishStoreSuccess() {
		if (mainGui != null) mainGui.setStorageInProgress(false);
		dispose();
	}
	
	/**
	 * Called when StorageWorker terminates in error
	 * 
	 * @param e - The exception/error that was thrown
	 */
	public void finishStoreError(Throwable e) {
		if (mainGui != null) mainGui.setStorageInProgress(false);
		cancelButton.setText("Close");
		statusLabel.setForeground(Color.RED);
		String errorMessage = e.getMessage();
		if (errorMessage == null) errorMessage = e.getClass().getSimpleName();
		statusLabel.setText(errorMessage);
	}
	
	/** 
	 * Cancels the storage operation
	 */
	public void cancelStore() {
		if (mainGui != null) mainGui.setStorageInProgress(false);
		if (storageWorker != null) storageWorker.cancel(false);
		dispose();
	}
}
