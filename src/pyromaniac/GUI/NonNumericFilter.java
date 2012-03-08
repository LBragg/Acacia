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

import javax.swing.*;  
import javax.swing.text.*;  

// TODO: Auto-generated Javadoc
/**
 * The Class NonNumericFilter.
 */
public class NonNumericFilter extends DocumentFilter 
{  

	/* (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#insertString(javax.swing.text.DocumentFilter.FilterBypass, int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	public void insertString(DocumentFilter.FilterBypass fb, int offset,  
			String text, AttributeSet attr) throws BadLocationException 
			{  
				System.out.println("in insert string");

			}  

	// no need to override remove(): inherited version allows all removals  

	/* (non-Javadoc)
	 * @see javax.swing.text.DocumentFilter#replace(javax.swing.text.DocumentFilter.FilterBypass, int, int, java.lang.String, javax.swing.text.AttributeSet)
	 */
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length,  
			String text, AttributeSet attr) throws BadLocationException 
			{  
			System.out.println("In replace string");
		fb.replace(offset, length, text.replaceAll("\\d", ""), attr);  
			}  

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) 
	{  
		DocumentFilter dfilter = new NonNumericFilter();  

		JTextArea jta = new JTextArea("foo\nbar");  
		JTextField jtf = new JTextField("foo");  
		((AbstractDocument)jta.getDocument()).setDocumentFilter(dfilter);  
		((AbstractDocument)jtf.getDocument()).setDocumentFilter(dfilter);  

		JFrame frame = new JFrame("NonNumericFilter");  
		frame.getContentPane().add(jta, java.awt.BorderLayout.CENTER);  
		frame.getContentPane().add(jtf, java.awt.BorderLayout.SOUTH);  
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		frame.setSize(240, 120);  
		frame.setVisible(true);  
	}
}