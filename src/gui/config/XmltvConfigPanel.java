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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import core.grab.XmltvConfigWorker;
import core.grab.XmltvGrabberInfo;
import data.Options;

/**
 * Allows the user to configure the XMLTV grabber
 * 
 * This is used in XmltvConfigDialog and FirstTimeWizard
 * 
 * @author Louis Amstutz
 */
public class XmltvConfigPanel extends JPanel {
	private static final long serialVersionUID = -3173275114353281536L;

	private static final String INFO_TEXT = "This will launch the external XMLTV configuration " +
		"program.\n\nNOTE: On Linux XMLTV must be installed seperately.";
	
	private Options options;
	private XmltvConfigContainerDialog parent;
	private boolean showDeleteBox;
	
	/** List of sub grabbers and info about them */
	private List<XmltvGrabberInfo> xmltvGrabberList;
	
	/** Worker thread that will generate the XMLTV config file */
	private XmltvConfigWorker xmltvConfWorker;
	
	//////////////// UI Elements ////////////////////
	private JComboBox grabberChooser;
	private JLabel chooseGrabberLabel, urlLabel, notesLabel;
	private JTextArea infoArea;
	private JCheckBox deleteConfigBox;
	
	private XmltvConfigActionListener xcListener;
	
	/**
	 * Constructor
	 * 
	 * @param owner - Parent
	 * @param _options - Options
	 * @param _showDeleteBox - Whether to show the delete config file box
	 */
	public XmltvConfigPanel(Options _options, XmltvConfigContainerDialog _parent, boolean _showDeleteBox) {
		super();
		options = _options;
		parent = _parent;
		showDeleteBox = _showDeleteBox;
		
		xmltvGrabberList = new ArrayList<XmltvGrabberInfo>();
		try {XmltvGrabberInfo.loadGrabberList(xmltvGrabberList);}
		catch (Exception e) {
			String errorText = e.getMessage();
			if (errorText == null) errorText = "Error loading grabber list";
			JOptionPane.showMessageDialog(this, errorText, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		xcListener = new XmltvConfigActionListener();
		init();
		
		updateLabels();
	}
	
	/**
	 * Create the UI
	 */
	private void init() {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createLineBorder(Color.GRAY));
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		infoArea = new JTextArea(4, 30);
		infoArea.append(INFO_TEXT);
		infoArea.setEditable(false);
		infoArea.setBackground(this.getBackground());
		infoArea.setLineWrap(true);
		infoArea.setWrapStyleWord(true);
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,20,10);
		add(infoArea,c);
		
		chooseGrabberLabel = new JLabel("Choose Grabber");
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,0,10);
		add(chooseGrabberLabel,c);  

		grabberChooser = new JComboBox(xmltvGrabberList.toArray());
		grabberChooser.addActionListener(xcListener);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.insets = new Insets(0,10,10,10);
		add(grabberChooser,c);
		
		urlLabel = new JLabel("URL:");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,0,10);
		add(urlLabel,c);  
		
		notesLabel = new JLabel("Notes:");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.insets = new Insets(10,10,0,10);
		add(notesLabel,c); 
		
		
		deleteConfigBox = new JCheckBox("Delete config file first (full config)");
		deleteConfigBox.setSelected(true);
		if (showDeleteBox) {
			c.gridx = 0;
			c.gridy = 5;
			c.gridwidth = 2;
			c.insets = new Insets(10,10,10,10);
			add(deleteConfigBox, c);
		}
	}
	
	
	/**
	 * Action Listener for XmltvConfigDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class XmltvConfigActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == grabberChooser) {
				updateLabels();
			}
		}
	}
	
	/**
	 * Updates urlLabel and notesLabel based on the currently selected grabber
	 */
	private void updateLabels() {
		XmltvGrabberInfo currentInfo = (XmltvGrabberInfo)grabberChooser.getSelectedItem();
		urlLabel.setText("URL: " + currentInfo.url);
		notesLabel.setText("Notes: " + currentInfo.notes);
	}
	
	/**
	 * Start the XmltvConfgThread and update the dialog.  This should be called by the 
	 * parent dialog when the user clicks next or ok
	 */
	public void configXmltv() {
		String grabber = ((XmltvGrabberInfo)grabberChooser.getSelectedItem()).name;
		xmltvConfWorker = new XmltvConfigWorker(grabber, options, deleteConfigBox.isSelected(), this);
		xmltvConfWorker.execute();
		
		remove(chooseGrabberLabel);
		remove(grabberChooser);
		remove(deleteConfigBox);
		remove(urlLabel);
		remove(notesLabel);

		infoArea.setText("Please wait while XMLTV is configured...");
		
		repaint();
	}
	
	/**
	 * Reset the UI to it's original state
	 */
	public void reset() {
		removeAll();
		init();
		updateLabels();
	}
	
	/**
	 * Called when the thread is finished.  Updates the dialog with the status
	 */
	public void configFinishedSuccess() {
		parent.xmltvConfigFinished();

		infoArea.setForeground(Color.GREEN);
		infoArea.setText("XMLTV configured successfully.");
	}
	
	/**
	 * Called if if the XmltvConfigWorker terminates in error
	 * 
	 * @param e - the exception/error which was thrown
	 */
	public void configFinishedError(Throwable e) {
		infoArea.setForeground(Color.RED);
		String errorMessage = e.getMessage();
		if (errorMessage == null) errorMessage = e.getClass().getSimpleName();
		infoArea.setText(errorMessage);
	}
	
	/**
	 * Cancel the configuration
	 */
	public void cancelConfig() {
		if (xmltvConfWorker != null) xmltvConfWorker.cancel(true);
	}
}
