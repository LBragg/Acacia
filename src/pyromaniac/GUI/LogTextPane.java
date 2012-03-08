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

import java.awt.Color;
import java.awt.Rectangle;
import java.util.HashMap;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.StyleConstants;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import pyromaniac.IO.LoggerOutput;

// TODO: Auto-generated Javadoc
/**
 * The Class LogTextPane.
 */
public class LogTextPane implements LoggerOutput 
{
	
	/** The ta. */
	private JTextPane ta;
	
	/** The log type to style. */
	HashMap <Integer, Style> logTypeToStyle;
	
	/** The progress. */
	Style regular, error, debug, progress;
	
	/** The scroller. */
	JScrollPane scroller;
	
	/** The closed. */
	boolean closed;
	
	/**
	 * Instantiates a new log text pane.
	 *
	 * @param ta the ta
	 * @param scroller the scroller
	 */
	public LogTextPane(JTextPane ta, JScrollPane scroller)
	{
		this.ta = ta;
		this.scroller = scroller;
		StyledDocument doc = ta.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE );
		regular = doc.addStyle( "regular", def );
	
		error = doc.addStyle( "error",regular);
	    StyleConstants.setBold(error, true );
	    StyleConstants.setForeground(error, Color.red);
	    
	    debug = doc.addStyle("debug", regular);
	    StyleConstants.setBold(debug, true);
	    StyleConstants.setForeground(debug, Color.green);
	    
	    progress = doc.addStyle("progress", regular);
	    StyleConstants.setForeground(progress, Color.black);
	    closed = false;
	}
	
	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#write(java.lang.StringBuilder, int)
	 */
	public void write(StringBuilder buffer, int style) throws Exception
	{
		//maybe this should be invoke....after
		
		try 
		{
			StyledDocument doc = this.ta.getStyledDocument();
			doc.insertString(doc.getLength(), buffer.toString(), this.getStyle(style));
		}	 
		catch (BadLocationException e) 
		{
			e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
		}
		
		SwingUtilities.invokeLater(new UpdateTextRunnable(this.scroller, this.ta, buffer.toString(), this, style));
		
	}
	
	/**
	 * Gets the style.
	 *
	 * @param style the style
	 * @return the style
	 */
	private Style getStyle(int style) 
	{
		switch(style)
		{
			case LoggerOutput.LOG_STYLE_DEBUG:
				return this.debug;
			case LoggerOutput.LOG_STYLE_ERROR:
				return this.error;
			case LoggerOutput.LOG_STYLE_PROGRESS:
				return this.progress;
			default:
				return this.regular;
		}
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#doHTMLMarkup()
	 */
	public boolean doHTMLMarkup()
	{
		return true;
	}
    
    /**
     * The Class UpdateTextRunnable.
     */
    class UpdateTextRunnable implements Runnable
    {
    	
	    /** The scroller. */
	    private JScrollPane scroller;
    	
	    /** The new text. */
	    private String newText; 
    	
	    /** The ta. */
	    private JTextPane ta;
    	
	    /** The style. */
	    private int style;
    	
	    /** The pane. */
	    LogTextPane pane;
    	
    	/**
	     * Instantiates a new update text runnable.
	     *
	     * @param scroller the scroller
	     * @param ta the ta
	     * @param newText the new text
	     * @param pane the pane
	     * @param style the style
	     */
	    public UpdateTextRunnable(JScrollPane scroller, JTextPane ta, String newText, LogTextPane pane, int style)
    	{
    		super();
    		this.scroller = scroller;
    		this.newText = newText;
    		this.pane = pane;
    		this.style = style;
    		this.ta = ta;
    	}
    	
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			this.scroller.scrollRectToVisible(new Rectangle(0,ta.getHeight()-2,1,1));
			this.scroller.getVerticalScrollBar().setValue(scroller.getVerticalScrollBar().getMaximum());
		}
    }

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#closeHandle()
	 */
	public void closeHandle() throws Exception 
	{
		//do nothing
		this.closed = true;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#isClosed()
	 */
	public boolean isClosed() 
	{
		// TODO Auto-generated method stub
		return this.closed;
	}

	/* (non-Javadoc)
	 * @see pyromaniac.IO.LoggerOutput#flush()
	 */
	public void flush() throws Exception 
	{	
	}
}
