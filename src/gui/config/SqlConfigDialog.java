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

import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.*;

import core.TVRadar;
import core.misc.SQLInterface;
import data.Options;

/**
 * Dialog to configure the MySQL database
 * 
 * @author Louis Amstutz
 */
public class SqlConfigDialog extends JDialog {
	private static final long serialVersionUID = 4595356124276068779L;

	private static final String AUTO_MESSAGE = "For auto configuration, " + TVRadar.getProgramName() +
		" will create the database " + SQLInterface.SQL_DATABASE_NAME + " and the user " + 
		SQLInterface.SQL_USERNAME + ", and give that user full " +
		"permissions on that database.  Please provide your MySQL root password as well as the " +
		"password you want the user account to use.  If you do not want to provide the root " +
		"password you will need to use manual config.";
	
	private static final String MANUAL_MESSAGE = "For manual configuration, you will need to login to " +
		"MySQL and \n" +
		"1. Create the user " + SQLInterface.SQL_USERNAME + "@localhost with a password of your choice. " +
		"(You will also need to create " + SQLInterface.SQL_USERNAME + "@% if you want remote access) \n" +
		"2. Create the database " + SQLInterface.SQL_DATABASE_NAME + "\n" +
		"3. Grant the user full permissions on the database\n" +
			"4. Enter the password for the user below";
	
	private SQLInterface si;
	private Options options;
	
	////////////////////// UI Elements ///////////////////////
	private JPanel autoPanel, manualPanel, mainPanel, bottomPanel;
	private JTabbedPane tabbedPane;
	private JButton okButton, cancelButton;
	private JTextArea autoInfoArea, manualInfoArea;
	private JLabel rootPwdLabel, autoUserPwdLabel, manualUserPwdLabel;
	private JPasswordField rootPwdField, autoUserPwdField, manualUserPwdField;
	
	private SqlConfigActionListener scListener;
	
	
	/**
	 * Constructor
	 * 
	 * @param parent - Parent
	 * @param _options - Options
	 */
	public SqlConfigDialog(JDialog parent, Options _options) {
		super(parent, "Config SQL", true);
		
		options = _options;
		
		scListener = new SqlConfigActionListener();
		
		createAutoPanel();
		createManualPanel();
		createTabbedPane();
		createBottomPanel();
		createMainPanel();
		getContentPane().add(mainPanel);
		
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	/**
	 * Create the tabbed pane
	 */
	private void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Auto Config", autoPanel);
		tabbedPane.addTab("Manual Config", manualPanel);
	}
	
	/**
	 * Create the tab for automatic configuration of the database
	 */
	private void createAutoPanel() {
		autoPanel = new JPanel();
		autoPanel.setLayout(new GridBagLayout());
		autoPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		autoInfoArea = new JTextArea(7,30);
		autoInfoArea.append(AUTO_MESSAGE);
		autoInfoArea.setEditable(false);
		autoInfoArea.setBackground(this.getBackground());
		autoInfoArea.setLineWrap(true);
		autoInfoArea.setWrapStyleWord(true);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		autoPanel.add(autoInfoArea, c);
		
		autoUserPwdLabel = new JLabel(SQLInterface.SQL_USERNAME + " password");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		autoPanel.add(autoUserPwdLabel, c);
		
		autoUserPwdField = new JPasswordField();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		autoUserPwdField.addActionListener(scListener);
		autoPanel.add(autoUserPwdField, c);
		
		rootPwdLabel = new JLabel("MySQL root password");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		autoPanel.add(rootPwdLabel, c);
		
		rootPwdField = new JPasswordField();
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		rootPwdField.addActionListener(scListener);
		autoPanel.add(rootPwdField, c);
		
	}
	
	/**
	 * Create the tab for manual configuration of the database
	 */
	private void createManualPanel() {
		manualPanel = new JPanel();
		manualPanel.setLayout(new GridBagLayout());
		manualPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		final GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		manualInfoArea = new JTextArea(8,30);
		manualInfoArea.append(MANUAL_MESSAGE);
		manualInfoArea.setEditable(false);
		manualInfoArea.setBackground(this.getBackground());
		manualInfoArea.setLineWrap(true);
		manualInfoArea.setWrapStyleWord(true);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		manualPanel.add(manualInfoArea, c);
		
		manualUserPwdLabel = new JLabel(SQLInterface.SQL_USERNAME + " password");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,0,10);
		manualPanel.add(manualUserPwdLabel, c);
		
		manualUserPwdField = new JPasswordField();
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(10,10,10,10);
		manualUserPwdField.addActionListener(scListener);
		manualPanel.add(manualUserPwdField, c);
	}
	
	/**
	 * Create the bottom panel with the OK and exit buttons
	 */
	private void createBottomPanel() {
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(75,26));
		cancelButton.addActionListener(scListener);
		bottomPanel.add(cancelButton);
		
		okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(75,26));
		okButton.addActionListener(scListener);
		bottomPanel.add(okButton);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.add(tabbedPane);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(bottomPanel);
	}
	
	/**
	 * Action Listener for SqlConfigDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class SqlConfigActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				dispose();
			}
			if (e.getSource() == okButton) {
				configSql();
			}
			if (e.getSource() == autoUserPwdField) {
				configSql();
			}
			if (e.getSource() == rootPwdField) {
				configSql();
			}
			if (e.getSource() == manualUserPwdField) {
				configSql();
			}
		}
	}
	
	/**
	 * Configure the database
	 */
	private void configSql() {
		if (tabbedPane.getSelectedIndex() == 0) {
			if (autoUserPwdField.getPassword().length == 0 || rootPwdField.getPassword().length == 0)
				JOptionPane.showMessageDialog(this, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
			else {
				options.setMySqlPwd(new String(autoUserPwdField.getPassword()));
				si = new SQLInterface();
				try {
					si.connect("root", new String(rootPwdField.getPassword()),options.getMySqlUrl());
					si.resetDatabase(new String(autoUserPwdField.getPassword()));
					si.createTables();
					si.disconnect();
				}
				catch (SQLException e) {
					String errorText = e.getMessage();
					if (errorText == null) errorText = "Unknown error";
					JOptionPane.showMessageDialog(this, errorText, "Error", JOptionPane.ERROR_MESSAGE);
				}
				dispose();
			}
		}
		else {
			if (manualUserPwdField.getPassword().length == 0) 
				JOptionPane.showMessageDialog(this, "Invalid password", "Error", JOptionPane.ERROR_MESSAGE);
			else {
				options.setMySqlPwd(new String(manualUserPwdField.getPassword()));
				dispose();
			}
		}
	}
}
