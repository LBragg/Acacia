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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;

// TODO: Auto-generated Javadoc
// This code was taken from Philip Isenhour (http://javatechniques.com/blog/gridbaglayout-example-a-simple-form-layout/)

/**
 * The Class GridBagUtility.
 */
public class GridBagUtility 
{

	/** The last constraints. */
	private GridBagConstraints lastConstraints = null;
	
	/** The middle constraints. */
	private GridBagConstraints middleConstraints = null;
	
	/** The label constraints. */
	private GridBagConstraints labelConstraints = null;

	/**
	 * Instantiates a new grid bag utility.
	 */
	public GridBagUtility() 
	{
		// Set up the constraints for the "last" field in each
		// row first, then copy and modify those constraints.

		// weightx is 1.0 for fields, 0.0 for labels
		// gridwidth is REMAINDER for fields, 1 for labels
		lastConstraints = new GridBagConstraints();

		// Stretch components horizontally (but not vertically)
		lastConstraints.fill = GridBagConstraints.HORIZONTAL;

		// Components that are too short or narrow for their space
		// Should be pinned to the northwest (upper left) corner
		lastConstraints.anchor = GridBagConstraints.NORTHWEST;

		// Give the "last" component as much space as possible
		lastConstraints.weightx = 1.0;

		// Give the "last" component the remainder of the row
		lastConstraints.gridwidth = GridBagConstraints.REMAINDER;

		// Add a little padding
		lastConstraints.insets = new Insets(2, 2, 3, 3);

		// Now for the "middle" field components
		middleConstraints =
			(GridBagConstraints) lastConstraints.clone();

		// These still get as much space as possible, but do
		// not close out a row
		middleConstraints.gridwidth = GridBagConstraints.RELATIVE;

		// And finally the "label" constrains, typically to be
		// used for the first component on each row
		labelConstraints =
			(GridBagConstraints) lastConstraints.clone();

		// Give these as little space as necessary
		labelConstraints.weightx = 0.0;
		labelConstraints.gridwidth = 1;
	}

	/**
	 * Adds a field component. Any component may be used. The
	 * component will be stretched to take the remainder of
	 * the current row.
	 *
	 * @param c the c
	 * @param parent the parent
	 */
	public void addLastField(Component c, Container parent)
	{
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
		gbl.setConstraints(c, lastConstraints);
		parent.add(c);
	}

	/**
	 * Adds an arbitrary label component, starting a new row
	 * if appropriate. The width of the component will be set
	 * to the minimum width of the widest component on the
	 * form.
	 *
	 * @param c the c
	 * @param parent the parent
	 */
	public void addLabel(Component c, Container parent) {
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
		gbl.setConstraints(c, labelConstraints);
		parent.add(c);
	}

	/**
	 * Adds a JLabel with the given string to the label column.
	 *
	 * @param s the s
	 * @param parent the parent
	 * @return the j label
	 */
	public JLabel addLabel(String s, Container parent) {
		JLabel c = new JLabel(s);
		c.setFont(parent.getFont());
		addLabel(c, parent);
		return c;
	}

	/**
	 * Adds a "middle" field component. Any component may be
	 * used. The component will be stretched to take all of
	 * the space between the label and the "last" field. All
	 * "middle" fields in the layout will be the same width.
	 *
	 * @param c the c
	 * @param parent the parent
	 */
	public void addMiddleField(Component c, Container parent) 
	{
		GridBagLayout gbl = (GridBagLayout) parent.getLayout();
		gbl.setConstraints(c, middleConstraints);
		parent.add(c);
	}
}

