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
package gui.config;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.URI;
import data.Options;

/**
 * This panel allows the user to configure the Schedules Direct Grabber
 * 
 * It is used in both SDConfigDialog and FirstTimeWizard
 * 
 * @author Louis Amstutz
 */
public class SDConfigPanel extends JPanel {
	private static final long serialVersionUID = 883972006636703401L;

	private static final String SD_SIGNUP_URL = "https://www.schedulesdirect.org/signup";
	
	private static final String INFO_TEXT = 
		"<html>Schedules Direct is a non-profit organization that provides TV listings for <br>" +
		"a small fee ($20/year at the moment).  7 day free trial accounts are available.</html>";
	
	private Options options;

	//////////////////// UI Elements /////////////////////
	private JLabel infoLabel, sdServerUrlLabel, usernameLabel, passwordLabel;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JCheckBox rememberCredentialsBox;
	private JTextField sdURLField;
	private JButton defaultURLButton, freeTrialButton;
	
	private SDPanelActionListener sdpListener;

	/**
	 * Constructor
	 * 
	 * @param parent - parent component
	 * @param _options - Options
	 */
	public SDConfigPanel(Options _options) {
		super();
		
		options = _options;
		
		sdpListener = new SDPanelActionListener();
		
		init();
		
		loadOptions();
	}
	

	/**
	 * Creates all the UI components
	 */
	private void init() {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		infoLabel = new JLabel(INFO_TEXT);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.insets = new Insets(10,10,3,5);
		add(infoLabel, c);
		
		sdServerUrlLabel = new JLabel("Schedules Direct server URL:");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.insets = new Insets(10,10,3,5);
		add(sdServerUrlLabel, c);
		
		sdURLField = new JTextField();
		sdURLField.setPreferredSize(new Dimension(350,25));
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.insets = new Insets(0,10,5,5);
		add(sdURLField, c);
		
		defaultURLButton = new JButton("Default");
		defaultURLButton.addActionListener(sdpListener);
		defaultURLButton.setPreferredSize(new Dimension(80,25));
		c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(0,0,5,10);
		add(defaultURLButton, c);
		
		usernameLabel = new JLabel("Username:");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(5,10,5,5);
		add(usernameLabel, c);
		
		usernameField = new JTextField();
		usernameField.setPreferredSize(new Dimension(100,25));
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(5,0,5,5);
		add(usernameField, c);
		
		passwordLabel = new JLabel("Password:");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(0,10,5,5);
		add(passwordLabel, c);
		
		passwordField = new JPasswordField();
		passwordField.setPreferredSize(new Dimension(100,25));
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(0,0,5,5);
		add(passwordField, c);
		
		rememberCredentialsBox = new JCheckBox("Remember credentials");
		rememberCredentialsBox.addActionListener(sdpListener);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 3;
		c.insets = new Insets(0,10,5,5);
		add(rememberCredentialsBox, c);
		
		freeTrialButton = new JButton("Get free trial account");
		freeTrialButton.addActionListener(sdpListener);
		c.gridx = 2;
		c.gridy = 5;
		c.gridwidth = 1;
		c.insets = new Insets(0,20,5,0);
		add(freeTrialButton, c);
	}

	
	/**
	 * Action Listener for SDConfigPanel
	 * 
	 * @author Louis Amstutz
	 */
	private class SDPanelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == defaultURLButton) {
				sdURLField.setText(Options.getDefaultSdServerUrl());
			}
			else if (e.getSource() == rememberCredentialsBox) {
				usernameField.setEnabled(rememberCredentialsBox.isSelected());
				usernameLabel.setEnabled(rememberCredentialsBox.isSelected());
				passwordField.setEnabled(rememberCredentialsBox.isSelected());
				passwordLabel.setEnabled(rememberCredentialsBox.isSelected());
			}
			else if (e.getSource() == freeTrialButton) {
				launchFreeTrialPage();
			}
		}
	}
	
	/**
	 * Launches a web browser to the Schedules Direct sign up page
	 */
	private void launchFreeTrialPage() {
		if (!Desktop.isDesktopSupported()) {
			JOptionPane.showMessageDialog(this, "Action not supported on this platform", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		Desktop desktop = Desktop.getDesktop();
		
		try {
			desktop.browse(new URI(SD_SIGNUP_URL));
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this, "Error Launching Browser", "Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		} 
	}
	
	/**
	 * Check if the state of the options is ok, and if so, updates the options object
	 * 
	 * @return true if successful, false otherwise
	 */
	public boolean checkAndSetOptions() {
		if (rememberCredentialsBox.isSelected() && (usernameField.getText().length() == 0 
				|| passwordField.getPassword().length == 0)) {
			JOptionPane.showMessageDialog(this, "Please enter username and password or " +
					"disable remember password");
			return false;
		}
		else {
			setOptions();
			return true;
		}
	}
	
	/**
	 * Set values of UI elements from options
	 */
	private void loadOptions() {
		sdURLField.setText(options.getSchedulesDirectURL());
		
		rememberCredentialsBox.setSelected(options.isRememberSDPassword());
		
		usernameField.setEnabled(options.isRememberSDPassword());
		usernameLabel.setEnabled(options.isRememberSDPassword());
		passwordField.setEnabled(options.isRememberSDPassword());
		passwordLabel.setEnabled(options.isRememberSDPassword());
		
		if (options.isRememberSDPassword()) {
			usernameField.setText(options.getSdUsername());
			passwordField.setText(options.getSdPassword());
		}
	}
	
	/**
	 * Sets the options to match what's on the panel
	 */
	private void setOptions() {
		options.setSchedulesDirectURL(sdURLField.getText());
		
		options.setRememberSDPassword(rememberCredentialsBox.isSelected());
		
		if (options.isRememberSDPassword()) {
			options.setSdUsername(usernameField.getText());
			options.setSdPassword(new String(passwordField.getPassword()));	
		}
		else {
			options.setSdUsername(null);
			options.setSdPassword(null);
		}
	}
}
