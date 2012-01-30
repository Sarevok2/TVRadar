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

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import data.Options;

/**
 * Dialog to configure XMLTV
 * 
 * @author Louis Amstutz
 */
public class XmltvConfigDialog extends JDialog implements XmltvConfigContainerDialog {
	private static final long serialVersionUID = 5703300700365722309L;
	
	private Options options;
	
	//////////////// UI Elements ////////////////////
	private JPanel mainPanel, bottomPanel;
	private XmltvConfigPanel xcPanel;
	private JButton configButton, cancelButton;
	
	private XmltvDialogActionListener xdListener;
	
	/**
	 * Constructor
	 * 
	 * @param owner - Parent
	 * @param _options - Options
	 */
	public XmltvConfigDialog(JDialog owner, Options _options) {
		super(owner, "Config XMLTV", true);
		
		xdListener = new XmltvDialogActionListener();
		
		options = _options;
		
		xcPanel = new XmltvConfigPanel(_options, this, true);
		createBottomPanel();
		createMainPanel();

		getContentPane().add(mainPanel);
		
		pack();
		setLocation(200, 150);
		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing ( WindowEvent w ) {
				cancelConfig();
			}
		});
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
		cancelButton.addActionListener(xdListener);
		bottomPanel.add(cancelButton);
		
		configButton = new JButton("Config");
		configButton.setPreferredSize(new Dimension(75,26));
		configButton.addActionListener(xdListener);
		bottomPanel.add(configButton);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.add(xcPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(bottomPanel);
	}
	
	/**
	 * Action Listener for XmltvConfigDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class XmltvDialogActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == configButton) {
				bottomPanel.remove(configButton);
				xcPanel.configXmltv();
				pack();
			}
			else if (e.getSource() == cancelButton) {
				cancelConfig();
			}
		}
	}
	
	/**
	 * Cancel the configuration
	 */
	public void cancelConfig() {
		xcPanel.cancelConfig();
		dispose();
	}

	@Override
	public void xmltvConfigFinished() {
		cancelButton.setText("Finish");
		Options.saveOptions(options);
	}
}
