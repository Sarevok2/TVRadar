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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.naming.ConfigurationException;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import core.TVRadar;
import core.grab.GrabListingsWorker;
import gui.config.XmltvConfigDialog;
import data.Options;
import data.guide.GuideData;

/**
 * Dialog to grab new TV listings
 * 
 * @author Louis Amstutz
 */
public class GrabDialog extends JDialog {
	private static final long serialVersionUID = -8239819680006912917L;
	
	private TVRadar tvRadar;
	private MainGui mainGui;
	
	/** Worker thread that will grab the listings */
	private GrabListingsWorker grabListingsWorker;
	
	////////////UI Elements////////////////////
	private JProgressBar progressBar;
	private JPanel mainPanel, contentPanel, bottomPanel;
	private JLabel statusLabel;
	private JButton cancelButton;
	
	private GrabActionListener gaListener;
	private GrabPropertyChangeListener gpcListener;
	
	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to TVRadar object
	 * @param _mg - Reference to main gui
	 * @param username - username - Only used for Schedules Direct grabber
	 * @param password - password - Only used for Schedules Direct grabber
	 */
	public GrabDialog(TVRadar _tvr, MainGui _mg, String username, String password) {
		super(_mg, "Retrieve Listings", true);
		tvRadar = _tvr;
		mainGui = _mg;
		
		gaListener = new GrabActionListener();
		gpcListener = new GrabPropertyChangeListener();
		
		createContentPanel();
		createBottomPanel();
		createMainPanel();
		getContentPane().add(mainPanel);
		
		startGrabThread(username,password);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing ( WindowEvent w ) {
				cancelGrab();
			}
		});

		pack();
		setLocation(200, 150);
		setVisible(true);
	}
	
	/**
	 * create the main panel
	 */
	private void createContentPanel() {
		contentPanel = new JPanel();
		contentPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		contentPanel.setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		statusLabel = new JLabel("Downloading listings.  This may take several minutes.");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		contentPanel.add(statusLabel, c);

		progressBar = new JProgressBar(0,100);
		progressBar.setMinimumSize(new Dimension(500,20));
		progressBar.setPreferredSize(new Dimension(500,20));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		contentPanel.add(progressBar, c);	
	}
	
	/**
	 * Create the bottom panel with the cancel button
	 */
	private void createBottomPanel() {
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(75,26));
		cancelButton.addActionListener(gaListener);
		bottomPanel.add(cancelButton);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.add(contentPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(bottomPanel);
	}
	
	/**
	 * ActionListener for this dialog
	 * 
	 * @author Louis Amstutz
	 */
	private class GrabActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				cancelGrab();
			}
		}
	}
	
	/**
	 * Property change listener for this dialog
	 * 
	 * Monitors property change events of the GrabListingsWorker which are
	 * fired when the progress is updated.
	 * 
	 * @author Louis Amstutz
	 */
	private class GrabPropertyChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName() == "progress") {
				int progress = (Integer) e.getNewValue();
				progressBar.setValue(progress);
			}
		}
	}
	
	/**
	 * Starts the grab thread
	 * 
	 * @param username - Username - only used for schedules Direct grabber
	 * @param password - Password - only used for schedules Direct grabber
	 */
	private void startGrabThread(String username, String password) {
		grabListingsWorker = new GrabListingsWorker(username, password,  tvRadar.getOptions(), this);
		grabListingsWorker.addPropertyChangeListener(gpcListener);
		grabListingsWorker.execute();
	}
	
	/**
	 * Called when grab thread is finished
	 * 
	 * Shows any errors if there were any, otherwise calls the tvr.addGuidePane() method 
	 * to show the guide Once the listings are displayed it opens a StorageDialog to store 
	 * the listings
	 */
	public void finishGrabSuccess(GuideData data) {
		cancelButton.setText("Close");
		
		statusLabel.setForeground(Color.GREEN);
		statusLabel.setText("Download Successful");
		tvRadar.setData(data);
		if (mainGui != null) mainGui.addGuidePane();
		new StorageDialog(tvRadar, mainGui);
		if (mainGui == null) dispose();
	}
	
	/**
	 * Called when GrabListingsWorker terminates in error
	 * 
	 * @param e - The exception/error that was thrown
	 */
	public void finishGrabError(Throwable e) {
		if (e instanceof ConfigurationException && tvRadar.getOptions().getGrabber() == Options.GrabberType.XMLTV) {
			dispose();
			openConfigXmltvDialog();
		}
		else {
			cancelButton.setText("Close");
			statusLabel.setForeground(Color.RED);
			String errorMessage = e.getMessage();
			if (errorMessage == null) errorMessage = e.getClass().getSimpleName();
			statusLabel.setText(errorMessage);
		}
	}
	
	public void updateStatusMessage(String message) {
		statusLabel.setText(message);
	}

	/**
	 * Cancels the grab operation
	 */
	public void cancelGrab() {
		dispose();
		if (grabListingsWorker != null) grabListingsWorker.cancel(false);		
	}
	
	/**
	 * Called if the user tried to download listings using XMLTV but XMLTV is not configured yet
	 * 
	 * Gives user the option to configure XMLTV
	 */
	private void openConfigXmltvDialog() {
		int response = JOptionPane.showConfirmDialog(this, "XMLTV is not configured.  Would you like to configure it now?", "", 
				JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION)
			new XmltvConfigDialog(this, tvRadar.getOptions());
		dispose();
	}
}
