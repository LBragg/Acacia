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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.RenderingHints;
import javax.swing.JButton;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JPanel;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.JComponent;
import javax.swing.BoxLayout;
import javax.swing.SpringLayout;
import javax.swing.BorderFactory;

import pyromaniac.AcaciaEngine;

// TODO: Auto-generated Javadoc
/**
 * The Class HelpButton.
 */
public class HelpButton extends JButton implements ActionListener
{
	
	/** The Constant HELP_ICON. */
	public static final String HELP_ICON = "/images/questionMarkGreen.png";
	
	/** The Constant HELP_FONT_DEFAULT. */
	public static final Font HELP_FONT_DEFAULT = new Font("Helvetica", Font.PLAIN, 14);
	
	/** The help image. */
	public static Image helpImage;
	
	/** The help message. */
	private String helpMessage;
	
	/** The parent frame. */
	private JFrame parentFrame;
	
	/**
	 * Instantiates a new help button.
	 *
	 * @param helpMessage the help message
	 * @param frame the frame
	 * @throws Exception the exception
	 */
	public HelpButton(String helpMessage, JFrame frame) throws MissingHelpImageException
	{
		super();
		
		URL imageURL;
		try
		{
			imageURL = AcaciaEngine.getImageUrl(HELP_ICON);
		}
		catch(Exception e)
		{
			throw new MissingHelpImageException(e);
		}
		
		helpImage = Toolkit.getDefaultToolkit().getImage(imageURL);		
		MediaTracker mTracker = new MediaTracker(this);
		mTracker.addImage(helpImage,1);
		
		this.parentFrame = frame;
		
		try
		{
			mTracker.waitForID(1);
		}
		catch(Exception e)
		{
			System.out.println("Was interrupted");
		}

		double height = helpImage.getHeight(null);
		double width = helpImage.getWidth(null);
		
		Dimension d = new Dimension( (int) width, (int) height);
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setMinimumSize(d);
		this.addActionListener(this);
		
		this.helpMessage = helpMessage;
	}
	
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY);
        
        g2.setColor(Color.green);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(helpImage,0 , 0, this.getWidth(), this.getHeight(), null);
    }

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent arg0) 
	{	
	
		JDialog dialog = new CustomDialog(this.parentFrame, false, this.helpMessage);
		dialog.setTitle("Help");
		dialog.setVisible(true);
	}

	public class MissingHelpImageException extends Exception
	{
		public MissingHelpImageException(Exception e)
		{
			super(e);
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
}
