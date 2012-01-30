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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Dialog to login to Schedules Direct
 * 
 * @author Louis Amstutz
 */
public class SDLoginDialog extends JDialog {
	private static final long serialVersionUID = -4345504729427316256L;

	/** 0 if user successfully enter a username and password, -1 if not */
	private int result=-1;
	
	///////////////// UI Elements //////////////////////
	private JPanel mainPanel;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JLabel usernameLabel, passwordLabel, statusLabel;
	private JButton okButton, cancelButton;
	private JCheckBox rememberPasswordBox;
	
	private LoginActionListener laListener;
	
	/**
	 * Constructor
	 * 
	 * @param frame - Parent
	 */
	public SDLoginDialog(Frame frame) {
		super(frame, "Login", true);
		
		laListener = new LoginActionListener();
		createMainPanel();
		this.getContentPane().add(mainPanel);
		this.pack();
		this.setResizable(false);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setLocation(200, 200);
		this.setVisible(true);
	}
	
	/**
	 * create main Panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		
		statusLabel = new JLabel("Please enter your username and password.");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,20,10);
		mainPanel.add(statusLabel, c);
		
		usernameLabel = new JLabel("Username");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(0,10,0,10);
		mainPanel.add(usernameLabel, c);
		
		usernameField = new JTextField();
		usernameField.addActionListener(laListener);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(0,10,0,10);
		mainPanel.add(usernameField, c);
		
		passwordLabel = new JLabel("Password");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,0,10);
		mainPanel.add(passwordLabel, c);
		
		passwordField = new JPasswordField();
		passwordField.addActionListener(laListener);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(0,10,0,10);
		mainPanel.add(passwordField, c);
		
		rememberPasswordBox = new JCheckBox("Remember Password");
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;
		c.insets = new Insets(0,10,0,10);
		mainPanel.add(rememberPasswordBox, c);
		
		okButton = new JButton("Ok");
		okButton.addActionListener(laListener);
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		mainPanel.add(okButton, c);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(laListener);
		c.weightx = 1;
		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		mainPanel.add(cancelButton, c);
	}
	
	/**
	 * Action Listener for SDLoginDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class LoginActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				result = -1;
				closeDialog();
			}
			else if (e.getSource() == okButton) {
				confirmLogin();
			}
			else if (e.getSource() == usernameField) {
				confirmLogin();
			}
			else if (e.getSource() == passwordField) {
				confirmLogin();
			}
		}
	}
	
	/**
	 * Verify that the username and password fields aren't blank
	 */
	private void confirmLogin() {
		if (usernameField.getText().length() == 0 || passwordField.getPassword().length == 0) {
			statusLabel.setText("Invalid username or password");
		}
		else {
			result=0;
			closeDialog();
		}
	}
	
	private void closeDialog() {
		this.dispose();
	}
	
	public int getResult() {
		return result;
	}
	
	public String getUsername() {
		return usernameField.getText();
	}
	
	public String getPassword() {
		return new String(passwordField.getPassword());
	}
	
	public boolean getRememberPassword() {
		return rememberPasswordBox.isSelected();
	}
}
