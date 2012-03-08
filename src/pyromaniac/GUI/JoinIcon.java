/*
 * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
 * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
 * 
 * 	This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package pyromaniac.GUI;

import java.awt.Graphics;
import java.awt.Point;

// TODO: Auto-generated Javadoc
/**
 * The Interface JoinIcon.
 */
public interface JoinIcon 
{
	
	/**
	 * Draw icon.
	 *
	 * @param g the g
	 */
	public void drawIcon(Graphics g);	
	
	/**
	 * Sets the start.
	 *
	 * @param start the new start
	 */
	public void setStart(Point start);
	
	/**
	 * Sets the end.
	 *
	 * @param end the new end
	 */
	public void setEnd(Point end);
}
