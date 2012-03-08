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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JButton;

// TODO: Auto-generated Javadoc
//Note - inspiration/code borrowed from : http://www.tek-tips.com/viewthread.cfm?qid=1408697&page=6
//Author: GrizzlyCrO

/**
 * The Class AcaciaGradientButton.
 */
public class AcaciaGradientButton extends JButton implements MouseListener 
{
	
	/** The Constant VERTICAL. */
	public static final int VERTICAL = 0;
	
	/** The Constant HORIZONTAL. */
	public static final int HORIZONTAL = 1;
	
	/** The Constant CENTERED. */
	public static final int CENTERED = 2;
	
	/** The Constant DIAGONAL. */
	public static final int DIAGONAL = 3;
	
	/** The Constant DEFAULT_BUTTON_WIDTH. */
	private static final int DEFAULT_BUTTON_WIDTH = 100;
	
	/** The Constant INACTIVE_BUTTON_COLOR. */
	public static final Color INACTIVE_BUTTON_COLOR = Color.decode("#CDCDCD");
	
	/** The first. */
	private Color first;
	
	/** The second. */
	private Color second;
	
	/** The direction. */
	private int direction;
	
	/** The border. */
	private GradientPaint border;
	
	/** The default border. */
	private Color defaultBorder;
	
    /**
     * Instantiates a new acacia gradient button.
     *
     * @param text the text
     * @param first the first
     * @param second the second
     * @param defaultBorder the default border
     * @param direction the direction
     */
    public AcaciaGradientButton(String text, Color first, Color second, Color defaultBorder, int direction) 
    {
        super(text);     
        
        this.first = first;
        this.second = second;
        this.direction = direction;
        this.defaultBorder = defaultBorder;

        
        this.setContentAreaFilled(false);
        this.setFocusPainted(false);     
        this.setBorderPainted(false);
        this.addMouseListener(this);
        
        
        Dimension d = this.getPreferredSize();
        d.width = DEFAULT_BUTTON_WIDTH;
        this.setPreferredSize(d);
        this.setSize(d);
        this.setMaximumSize(d);
        this.border = getDefaultBorder();
    }

    /**
     * Gets the default border.
     *
     * @return the default border
     */
    private GradientPaint getDefaultBorder()
    {
    	return this.makeGradientPaint(Color.decode("#BECC28"), this.defaultBorder, AcaciaGradientButton.DIAGONAL);
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D) g;
 
        Paint oldPaint = g2.getPaint();
        
        //do I change the background paint to grey?
        
        GradientPaint p;
        if(this.isEnabled())
        {
        	p = makeGradientPaint(this.first, this.second, this.direction);        	
        }
        else
        {
        	p = makeGradientPaint(INACTIVE_BUTTON_COLOR, Color.white, this.direction);
        }
        

        g2.setPaint(p);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(oldPaint);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if(this.border != null)
        {
        	this.drawBorder(g);
        }   
        
        super.paintComponent(g);
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) 
    {
        this.border = this.makeGradientPaint(Color.white, Color.orange, AcaciaGradientButton.DIAGONAL);
        this.repaint();
    }
    
    /**
     * Make gradient paint.
     *
     * @param pFirst the first
     * @param pSecond the second
     * @param pDirection the direction
     * @return the gradient paint
     */
    private GradientPaint makeGradientPaint(Color pFirst, Color pSecond, int pDirection)
    {
       GradientPaint p;
        
        int v1;
        int v2;
        int h1;
        int h2;
        
        boolean cyclic = false;
        
        int maxD = (this.getHeight() > this.getWidth())? this.getHeight() : this.getWidth();
        
        switch(pDirection)
        {
        	case AcaciaGradientButton.VERTICAL:
        		h1 = 0;
            	h2 = 0;
            	v1 = 0;
            	v2 = this.getHeight();
        		break;
        		
        	case AcaciaGradientButton.HORIZONTAL:
            	h1 = 0;
            	h2 = this.getWidth();
            	v1 = 0;
            	v2 = 0;        		
        		break;

        	case AcaciaGradientButton.CENTERED:	

        		h1 = 0;
            	h2 = 0;
            	
            	v1 = (int)(0.25 * this.getHeight());
            	v2 = this.getHeight();
            	cyclic = true;
        		break;
        		
        	case AcaciaGradientButton.DIAGONAL:
        		h1 = 0;
        		h2 = (int)(this.getWidth() * 0.50);
        		v1 = 0;
        		v2 = (int)(this.getHeight() * 0.5);
        		break;
        		
        	default:
        		Exception e = new Exception("Unknown option for border");
        		System.out.println(e.getMessage());
        		e.printStackTrace();
        		System.exit(1);
        		return null;
        		
        }
        p = new GradientPaint(h1, v1, pFirst, h2, v2, pSecond,cyclic);
        return p;
    }
    
    /**
     * Draw border.
     *
     * @param g the g
     */
    public void drawBorder(Graphics g)
    {
    	Paint oldPaint;
    	Stroke oldStroke;
    	
    	Graphics2D g2 = (Graphics2D)g;
    	oldPaint = g2.getPaint();
    	oldStroke = g2.getStroke();
    	
    	if(this.isEnabled())
    	{
    		g2.setPaint(this.border);
    	}
    	else
    	{
    		g2.setPaint(this.makeGradientPaint(Color.white, INACTIVE_BUTTON_COLOR, AcaciaGradientButton.DIAGONAL));
    	}
        g2.setStroke(new BasicStroke(3));
    	Rectangle2D rect = new Rectangle2D.Double(0,0,this.getWidth()-1 , this.getHeight()-1);
    	g2.draw(rect);
    	g2.setPaint(oldPaint);
    	g2.setStroke(oldStroke);
    }

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) 
	{
		//perhaps should make the button lighter???
		this.border = this.getDefaultBorder();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) 
	{
		this.border = this.getDefaultBorder();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) 
	{
		this.border = this.makeGradientPaint(Color.orange, Color.orange, AcaciaGradientButton.DIAGONAL);
		this.repaint();
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) 
	{
		this.border = this.getDefaultBorder();
		this.repaint();
	}

}
