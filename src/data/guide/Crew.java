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

package data.guide;

import java.io.Serializable;

/**
 * Representation of a crew member
 * 
 * @author Louis Amstutz
 */
public class Crew implements Serializable{
	private static final long serialVersionUID = 4309234941704398841L;
	
	/** Name of crew member */
	private String firstName, lastName;
	
	/** Role of crew member */
	private String role;
	
	/** Program ID of program this crew member is associated with */
	private String progId;
	
	public String getFirstName() {return firstName;}
	public String getLastName() {return lastName;}
	public String getRole() {return role;}
	public String getProgId() {return progId;}
	
	public void setFirstName(String fname) {firstName = fname;}
	public void setLastName(String lname) {lastName = lname;}
	public void setRole(String _role) {role = _role;}
	public void setProgId(String pid) {progId = pid;}
	
	@Override
	public String toString() {return (role + ": " + firstName + " " + lastName);}
}
