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

/**
 * This interface is implemented by any Dialog that contains an XmltvConfigPanel.  Currently
 * that is XmltvConfigDialog and FirstTimeWizard
 * 
 * @author Louis Amstutz
 */
public interface XmltvConfigContainerDialog {
	
	/**
	 * The XmltvConfigPanel will call this method when the configuration is complete,
	 * allowing the parent window to take any necessary action, such as updating buttons.
	 */
	public void xmltvConfigFinished();
}
