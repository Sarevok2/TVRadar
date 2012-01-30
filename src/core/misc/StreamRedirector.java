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

package core.misc;

import java.io.*;

/**
 * Thread which redirects an input stream to an output stream
 * If the output stream is null it will simply consume the input stream discarding everything
 * 
 * @author Louis Amstutz
 */
public class StreamRedirector extends Thread {
	private InputStream is;
	private OutputStream os;
 
    /**
     * Constructor
     * 
     * @param _is - input stream
     * @param _os - output stream
     */
	public StreamRedirector(InputStream _is, OutputStream _os) {
		is = _is;
		os = _os;
	}
	
	@Override
	public void run() {
		try {
			if (os != null) {
				int charRead;

				while ( (charRead = is.read()) != -1) { 
					os.write(charRead);
				}
			}
			//If there is a null ouputStream, just keep clearing the inputStream
			else while (is.read() != -1) {}
        } 
		catch (IOException e) {}
	}
}

