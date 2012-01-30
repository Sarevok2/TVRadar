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

import javax.swing.*;

import data.Options;

/**
 * Dialog to configure Schedules Direct grabber
 * 
 * @author Louis Amstutz
 */
public class SDConfigDialog extends JDialog {
	private static final long serialVersionUID = 8270563327051394315L;

	private Options options;
	
	//////////////////// UI Elements /////////////////////
	private JPanel mainPanel, bottomPanel;
	private JButton okButton, cancelButton;
	private SDConfigPanel sdPanel;
	
	private SDConfigActionListener sdaListener;

	/**
	 * Constructor
	 * 
	 * @param parent - parent component
	 * @param _options - Options
	 */
	public SDConfigDialog(JDialog parent, Options _options) {
		super(parent, "Configure Schedules Direct Grabber", true);
		
		options = _options;
		
		sdaListener = new SDConfigActionListener();
		
		sdPanel = new SDConfigPanel(options);
		createBottomPanel();
		createMainPanel();
		getContentPane().add(mainPanel);
		
		pack();
		setLocation(200, 150);
		setVisible(true);
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
		cancelButton.addActionListener(sdaListener);
		bottomPanel.add(cancelButton);
		
		okButton = new JButton("OK");
		okButton.setPreferredSize(new Dimension(75,26));
		okButton.addActionListener(sdaListener);
		bottomPanel.add(okButton);
	}
	
	/**
	 * Create the main panel
	 */
	private void createMainPanel() {
		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		mainPanel.add(sdPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,6)));
		mainPanel.add(bottomPanel);
	}
	
	/**
	 * Action Listener for SDConfigDialog
	 * 
	 * @author Louis Amstutz
	 */
	private class SDConfigActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == cancelButton) {
				dispose();
			}
			else if (e.getSource() == okButton) {
				if (sdPanel.checkAndSetOptions()) {
					Options.saveOptions(options);
					dispose();
				}
			}
		}
	}
}
