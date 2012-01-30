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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import core.TVRadar;
import data.Options;
import gui.MainGui;

/**
 * Dialog to configure options
 * 
 * @author Louis Amstutz
 */
public class OptionsDialog extends JDialog {
	private static final long serialVersionUID = -7942539588136135355L;

	private static final String LOCAL_SQL_SERVER_URL = "jdbc:mysql://localhost:3306";

	private static final String UI_SIZE_HELP = "Overall size of the program grid and text";

	private static final String POPUP_DELAY_HELP = "<html>The delay before " +
			"a tooltip popup opens when you hover the mouse <br>over a program or channel</html>";

	private static final String SHOW_CREW_HELP = "<html>Whether the tooltip " +
			"popup about a program will <br>include a list of the crew</html>";

	private static final String GRABBER_HELP = "<html>Choose the grabber for the TV listings. " +
			"<br><br>If you are in North America choose Schedules Direct.  Schedules Direct " +
			"is a non-profit organization <br>that provides TV listings for a small fee " +
			"($20/year at the time of writing).  You can get a 7 day free <br>trial account.<br><br>" +
			"If you are outside North America, choose XMLTV.  XMLTV is an external program which " +
			"can grab listings <br>from many sources all over the world.  Note that XMLTV can grab " +
			"listings from Schedules Direct as well, <br>but the built in grabber is faster</html>";
			
	private static final String DAYS_AHEAD_HELP = "How many days worth of listings to get";

	private static final String STORAGE_HELP = "<html>Choose the method of " +
			"storing the listing data on disk.<br><br> If you choose a binary file, " + TVRadar.getProgramName() + 
			" will storage all the data in a single file in your user directory.<br><br>If you want to store " +
			"the listings in a MySQL database you will need to set up MySQL first.<br>Once you do this " +
			TVRadar.getProgramName() + " can automatically create the MySQL database and user account</html>";

	private static final String MYSQL_SERVER_HELP = "<html>The URL for the " +
			"MySQL server.<br>If the server is on the same computer, click local to set a local URL</html>";

	private static final String DELETE_SQL_HELP = "<html>If checked, all existing " +
			"program data will be deleted whenever you download new data,<br> which is usually what you want." +
			"Otherwise all old program data will be kept</html>";

	private static final String SMALLER_PACKETS_HELP = "<html>If checked, this will divide all SQL inserts " +
			"into smaller packets.<br>Check this if you are getting max_allowed_packets errors in<br>" +
			"the console while storing data.</html>";
	
	private Options options;
	private TVRadar tvRadar;
	private MainGui mainGui;
	
	/** Set to true if user moves the uiSizeSlider or changes show crew settings.  This means the GuidePane
	 * will get redrawn after exiting the OptionsDialog */
	private boolean redrawGuidePane=false;
	
	///////////////////// UI Elements //////////////////////////
	private JPanel mainPanel, uiPanel, grabberPanel, storagePanel;
	private JTabbedPane tabbedPane;
	private JRadioButton useSerializedButton, useSQLButton, xmltvButton, sdButton, crewYesButton, 
		crewNoButton, crewMovieButton;
	private ButtonGroup grabberGroup, storageGroup, crewGroup;
	private JButton okButton, applyButton, cancelButton, restoreDefaultsButton, configXmltvButton, 
		configSDButton, configSqlButton, localSqlButton;
	private JLabel grabberLabel, storageLabel, daysAheadLabel, mySqlServerLabel, crewLabel, 
		uiSizeLabel, popupDelayLabel, uiSizeNumberLabel, popupDelayNumberLabel;
	private JCheckBox deleteSQLBox, useSmallerSqlPacketsBox;
	private JTextField mySqlServerField;
	private JSpinner daysAheadSpinner;
	private JSlider uiSizeSlider, popupDelaySlider;
	
	private OptionsActionListener oaListener;
	private OptionsChangeListener ocListener;
	
	/**
	 * Constructor
	 * 
	 * @param _tvr - Reference to TVRadar object
	 * @param _mg - Main gui and parent
	 */
	public OptionsDialog (TVRadar _tvr, MainGui _mg) {
		super(_mg, "Options", true);

		tvRadar = _tvr;
		mainGui = _mg;
		options = tvRadar.getOptions();
	
		oaListener = new OptionsActionListener();
		ocListener = new OptionsChangeListener();
		
		createMainPanel();
		getContentPane().add(mainPanel);
		
		loadOptions();
		
		pack();	
		setVisible(true);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {	
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		createUIPanel();
		createStoragePanel();
		createGrabberPanel();
		createTabbedPane();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		c.insets = new Insets(10,10,10,10);
		mainPanel.add(tabbedPane, c);
		
		restoreDefaultsButton = new JButton("Restore Defaults");
		restoreDefaultsButton.addActionListener(oaListener);
		restoreDefaultsButton.setPreferredSize(new Dimension(135,26));
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(0,10,10,10);
		mainPanel.add(restoreDefaultsButton, c);
		
		JLabel emptyLabel = new JLabel();
		emptyLabel.setPreferredSize(new Dimension(50,25));
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 5;
		mainPanel.add(emptyLabel, c);
		
		okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(75,26));
		okButton.addActionListener(oaListener);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(0,0,10,0);
		mainPanel.add(okButton, c);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setPreferredSize(new Dimension(75,26));
		cancelButton.addActionListener(oaListener);
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(0,0,10,0);
		mainPanel.add(cancelButton, c);
		
		applyButton = new JButton("Apply");
		applyButton.setPreferredSize(new Dimension(75,26));
		applyButton.addActionListener(oaListener);
		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(0,0,10,10);
		mainPanel.add(applyButton, c);
	}
	
	/**
	 * Create the tabbed panel where all the options are
	 */
	private void createTabbedPane() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Display", uiPanel);
		tabbedPane.addTab("Grabber", grabberPanel);
		tabbedPane.addTab("Storage", storagePanel);
	}
	
	/**
	 * Create the UI options tab
	 */
	private void createUIPanel() {
		uiPanel = new JPanel();
		uiPanel.setLayout(new GridBagLayout());
		uiPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		uiSizeLabel = new JLabel("Grid size:");
		uiSizeLabel.setToolTipText(UI_SIZE_HELP);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);
		uiPanel.add(uiSizeLabel, c);
		
		uiSizeSlider = new JSlider(80,300);
		uiSizeSlider.addChangeListener(ocListener);
		uiSizeSlider.setToolTipText(UI_SIZE_HELP);
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		uiPanel.add(uiSizeSlider, c);
		
		uiSizeNumberLabel = new JLabel();
		uiSizeNumberLabel.setPreferredSize(new Dimension(50,22));
		c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 1;
		c.insets = new Insets(5,0,5,5);
		uiPanel.add(uiSizeNumberLabel, c);
		
		popupDelayLabel = new JLabel("Popup Delay:");
		popupDelayLabel.setToolTipText(POPUP_DELAY_HELP);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);
		uiPanel.add(popupDelayLabel, c);
		
		popupDelaySlider = new JSlider(0,1500);
		popupDelaySlider.addChangeListener(ocListener);
		popupDelaySlider.setToolTipText(POPUP_DELAY_HELP);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		uiPanel.add(popupDelaySlider, c);
		
		popupDelayNumberLabel = new JLabel();
		popupDelayNumberLabel.setPreferredSize(new Dimension(50,22));
		c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5,0,5,5);
		uiPanel.add(popupDelayNumberLabel, c);
		
		crewLabel = new JLabel("Show crew:");
		crewLabel.setToolTipText(SHOW_CREW_HELP);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(15,5,0,5);
		uiPanel.add(crewLabel, c);
		
		crewYesButton = new JRadioButton("Yes");
		crewYesButton.setToolTipText(SHOW_CREW_HELP);
		crewYesButton.addActionListener(oaListener);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(15,5,0,5);
		uiPanel.add(crewYesButton, c);
		
		crewNoButton = new JRadioButton("No");
		crewNoButton.setToolTipText(SHOW_CREW_HELP);
		crewNoButton.addActionListener(oaListener);
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 2;
		c.insets = new Insets(0,5,0,5);
		uiPanel.add(crewNoButton, c);
		
		crewMovieButton = new JRadioButton("Only if Movie");
		crewMovieButton.setToolTipText(SHOW_CREW_HELP);
		crewMovieButton.addActionListener(oaListener);
		c.gridx = 1;
		c.gridy = 6;
		c.gridwidth = 2;
		c.insets = new Insets(0,5,0,5);
		uiPanel.add(crewMovieButton, c);
		
		crewGroup = new ButtonGroup();
		crewGroup.add(crewYesButton);
		crewGroup.add(crewNoButton);
		crewGroup.add(crewMovieButton);
	}
	
	/**
	 * Create the grabber options tab
	 */
	private void createGrabberPanel() {
		grabberPanel = new JPanel();
		grabberPanel.setLayout(new GridBagLayout());
		grabberPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		grabberLabel = new JLabel("Download source:");
		grabberLabel.setToolTipText(GRABBER_HELP);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,0,5);
		grabberPanel.add(grabberLabel, c);
		

		sdButton = new JRadioButton("Schedules Direct");
		sdButton.setToolTipText(GRABBER_HELP);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,5,5);
		grabberPanel.add(sdButton, c);
		
		configSDButton = new JButton("Configure");
		configSDButton.addActionListener(oaListener);
		c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,5,5);
		grabberPanel.add(configSDButton, c);
		
		xmltvButton = new JRadioButton("XMLTV grabber");
		xmltvButton.setToolTipText(GRABBER_HELP);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(5,5,0,5);
		grabberPanel.add(xmltvButton, c);
		
		configXmltvButton = new JButton("Configure");
		configXmltvButton.addActionListener(oaListener);
		c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		grabberPanel.add(configXmltvButton, c);
		
		grabberGroup = new ButtonGroup();
		grabberGroup.add(sdButton);
		grabberGroup.add(xmltvButton);
		
		daysAheadLabel = new JLabel("Number of days ahead to get listings:");
		daysAheadLabel.setToolTipText(DAYS_AHEAD_HELP);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(10,5,5,5);
		grabberPanel.add(daysAheadLabel, c);
		
		daysAheadSpinner = new JSpinner(new SpinnerNumberModel(options.getDaysAhead(),1,14,1));
		daysAheadSpinner.setToolTipText(DAYS_AHEAD_HELP);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(10,5,5,5);
		grabberPanel.add(daysAheadSpinner, c);
	}
	
	/**
	 * Create the storage options tab
	 */
	private void createStoragePanel() {
		storagePanel = new JPanel();
		storagePanel.setLayout(new GridBagLayout());
		storagePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		
		storageLabel = new JLabel("Storage Method:");
		storageLabel.setToolTipText(STORAGE_HELP);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.insets = new Insets(5,5,5,5);
		storagePanel.add(storageLabel, c);
		
		useSerializedButton = new JRadioButton("Store listings as a binary file (Recommended)");
		useSerializedButton.addActionListener(oaListener);
		useSerializedButton.setToolTipText(STORAGE_HELP);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.insets = new Insets(5,5,5,5);
		storagePanel.add(useSerializedButton, c);
		
		useSQLButton = new JRadioButton("Store listings in a MySQL database");
		useSQLButton.addActionListener(oaListener);
		useSQLButton.setToolTipText(STORAGE_HELP);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.insets = new Insets(5,5,5,5);
		storagePanel.add(useSQLButton, c);
		
		storageGroup = new ButtonGroup();
		storageGroup.add(useSerializedButton);
		storageGroup.add(useSQLButton);
		
		JLabel emptyLabel = new JLabel();
		emptyLabel.setPreferredSize(new Dimension(30,20));
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		storagePanel.add(emptyLabel, c);
		
		configSqlButton = new JButton("Set up Database");
		configSqlButton.addActionListener(oaListener);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		storagePanel.add(configSqlButton, c);
		
		mySqlServerLabel = new JLabel("MySQL Server URL:");
		mySqlServerLabel.setToolTipText(MYSQL_SERVER_HELP);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		storagePanel.add(mySqlServerLabel, c);
		
		mySqlServerField = new JTextField();
		mySqlServerField.setToolTipText(MYSQL_SERVER_HELP);
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		storagePanel.add(mySqlServerField, c);
		
		localSqlButton = new JButton("Local");
		localSqlButton.addActionListener(oaListener);
		localSqlButton.setPreferredSize(new Dimension(70,25));
		c.gridx = 3;
		c.gridy = 4;
		c.gridwidth = 1;
		c.insets = new Insets(5,0,0,5);
		storagePanel.add(localSqlButton, c);
		
		deleteSQLBox = new JCheckBox("Delete old listings");
		deleteSQLBox.setToolTipText(DELETE_SQL_HELP);
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		storagePanel.add(deleteSQLBox, c);
		
		useSmallerSqlPacketsBox = new JCheckBox("Use smaller packets");
		useSmallerSqlPacketsBox.setToolTipText(SMALLER_PACKETS_HELP);
		c.gridx = 2;
		c.gridy = 5;
		c.gridwidth = 1;
		c.insets = new Insets(5,5,0,5);
		storagePanel.add(useSmallerSqlPacketsBox, c);
	}
	
	/**
	 * Action Listener for OptionsDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class OptionsActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == useSerializedButton) {
				setStorageVisibility();
			}
			else if (e.getSource() == useSQLButton) {
				setStorageVisibility();
			}
			else if (e.getSource() == configXmltvButton) {
				openXmltvConfigDialog();
			}
			else if (e.getSource() == configSDButton) {
				openSDConfigDialog();
			}
			else if (e.getSource() == configSqlButton) {
				openConfigSqlDialog();
			}
			else if (e.getSource() == localSqlButton) {
				mySqlServerField.setText(LOCAL_SQL_SERVER_URL);
			}
			else if (e.getSource() == restoreDefaultsButton) {
				restoreDefaults();
			}
			else if (e.getSource() == cancelButton) {
				dispose();
			}
			else if (e.getSource() == crewYesButton ||
					e.getSource() == crewNoButton ||
					e.getSource() == crewMovieButton) {
				redrawGuidePane = true;
			}
			else if (e.getSource() == okButton) {
				if (optionsOk()) {
					saveOptions();
					dispose();
					if (redrawGuidePane) mainGui.addGuidePane();
				}
			}
			else if (e.getSource() == applyButton) {
				if (optionsOk()) {
					saveOptions();
					if (redrawGuidePane) mainGui.addGuidePane();
				}
			}
		}
	}
	
	/**
	 * Change listener for the sliders in OptionsDialog
	 */
	private class OptionsChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == uiSizeSlider) {
				uiSizeNumberLabel.setText(String.valueOf(uiSizeSlider.getValue()) + "%");
				redrawGuidePane = true;
			}
			else if (e.getSource() == popupDelaySlider) {
				popupDelayNumberLabel.setText(String.valueOf(popupDelaySlider.getValue()) + "ms");
			}
		}
	}
	
	/**
	 * Sets where the SQL options are enabled (if useSQLButton is selected)
	 */
	private void setStorageVisibility() {
		boolean isVisible = useSQLButton.isSelected();
		deleteSQLBox.setEnabled(isVisible);
		useSmallerSqlPacketsBox.setEnabled(isVisible);
		configSqlButton.setEnabled(isVisible);
		mySqlServerField.setEnabled(isVisible);
		mySqlServerLabel.setEnabled(isVisible);
		localSqlButton.setEnabled(isVisible);
	}
	
	/**
	 * Open an XmltvConfigDialog
	 */
	private void openXmltvConfigDialog() {
		new XmltvConfigDialog(this, options);
	}
	
	/**
	 * Open an SDConfigDialog
	 */
	private void openSDConfigDialog() {
		new SDConfigDialog(this, options);
	}
	
	/**
	 * Open an SqlConfigDialog
	 */
	private void openConfigSqlDialog() {
		new SqlConfigDialog(this, options);
	}
	
	/**
	 * Load options from disk
	 */
	private void loadOptions() {
		uiSizeSlider.setValue(options.getUiSize());
		redrawGuidePane = false;
		
		popupDelaySlider.setValue(options.getPopupDelay());
		
		if (options.getShowCrew() == Options.ShowCrew.YES) crewYesButton.setSelected(true);
		else if (options.getShowCrew() == Options.ShowCrew.NO) crewNoButton.setSelected(true);
		else crewMovieButton.setSelected(true);
		
		if (options.getGrabber() == Options.GrabberType.SD) sdButton.setSelected(true);
		else xmltvButton.setSelected(true);
				
		if (options.isUseSQL()) useSQLButton.setSelected(true);
		else useSerializedButton.setSelected(true);
		
		setStorageVisibility();
		
		mySqlServerField.setText(options.getMySqlUrl());
		
		deleteSQLBox.setSelected(options.isDeleteSQL());
		useSmallerSqlPacketsBox.setSelected(options.isSmallerSqlPackets());
		
		((SpinnerNumberModel)daysAheadSpinner.getModel()).setValue(new Integer(options.getDaysAhead()));
	}
	
	/**
	 * Saves otions to disk
	 */
	private void saveOptions() {
		options.setUiSize(uiSizeSlider.getValue());
		
		options.setPopupDelay(popupDelaySlider.getValue());
		ToolTipManager.sharedInstance().setInitialDelay(options.getPopupDelay());
		
		if (crewYesButton.isSelected()) options.setShowCrew(Options.ShowCrew.YES);
		else if (crewNoButton.isSelected()) options.setShowCrew(Options.ShowCrew.NO);
		else options.setShowCrew(Options.ShowCrew.MOVIES);
		
		if (useSerializedButton.isSelected()) options.setUseSQL(false);
		else options.setUseSQL(true);
		
		if (sdButton.isSelected()) options.setGrabber(Options.GrabberType.SD);
		else options.setGrabber(Options.GrabberType.XMLTV);
		
		options.setMySqlUrl(mySqlServerField.getText());
		
		options.setDeleteSQL(deleteSQLBox.isSelected());
		options.setSmallerSqlPackets(useSmallerSqlPacketsBox.isSelected());
		
		options.setDaysAhead(((Integer)(daysAheadSpinner.getValue())).intValue());
		
		Options.saveOptions(options);
	}
	
	/**
	 * Checks to make sure the state of the options is ok
	 * 
	 * @return whether options are ok
	 */
	private boolean optionsOk() {
		if (this.useSQLButton.isSelected() && this.mySqlServerField.getText().length() == 0) {
			JOptionPane.showMessageDialog(this, "Please enter the MySQL Server address");
			return false;
		}
		return true;
	}
	
	/**
	 * Restores default options
	 */
	public void restoreDefaults() {
		int response = JOptionPane.showConfirmDialog(this, "This will restore all options " +
				"to their defaults.  Are you sure?", "", JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION) {
			options.restoreDefaults();
			loadOptions();
		}
	}
}
