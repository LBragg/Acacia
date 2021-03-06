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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

// TODO: Auto-generated Javadoc
/**
 * The Class CustomDialog.
 */
public class CustomDialog extends JDialog implements ActionListener
{
    
    /** The Constant MAXIMUM_WIDTH. */
    private static final int MAXIMUM_WIDTH = 300;
	
	/** The Constant PREFERRED_HEIGHT. */
	private static final int PREFERRED_HEIGHT = 150;
	
	/** The my panel. */
	private JPanel myPanel = null;
    
    /** The ok button. */
    private JButton okButton = null;
    
    /**
     * Instantiates a new custom dialog.
     *
     * @param frame the frame
     * @param modal the modal
     * @param myMessage the my message
     */
    public CustomDialog(JFrame frame, boolean modal, String myMessage) 
    {
        super(frame, modal);
        
        myPanel = new JPanel();
        
        this.getContentPane().add(myPanel);
        
        SpringLayout layoutManager = new SpringLayout();
        myPanel.setLayout(layoutManager);
        
        JLabel messageLabel = new JLabel( "<HTML>"+ myMessage + "</HTML>");
        messageLabel.setBorder(TagInputPanel.DEFAULT_BORDER);
        messageLabel.setOpaque(true);
        messageLabel.setBackground(TagInputPanel.FORM_BACKGROUND_COLOUR);
     //   messageLabel.setFont(TagInputPanel.DEFAULT);
        messageLabel.setAlignmentY(TOP_ALIGNMENT);
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        int preferredHeight = PREFERRED_HEIGHT;
        int maxWidth = (messageLabel.getPreferredSize().width > MAXIMUM_WIDTH)? MAXIMUM_WIDTH : messageLabel.getPreferredSize().width;
        
        Dimension mlMaximum = new Dimension(maxWidth, PREFERRED_HEIGHT);
        messageLabel.setMaximumSize(mlMaximum);
        messageLabel.setPreferredSize(new Dimension(maxWidth,PREFERRED_HEIGHT));
        
        
        myPanel.add(messageLabel);     
        myPanel.setBackground(TagInputPanel.WINDOW_BACKGROUND_COLOUR);
        
        okButton = new AcaciaGradientButton("OK", TagInputPanel.BUTTON_GRADIENT_COLOR1, TagInputPanel.BUTTON_GRADIENT_COLOR2, TagInputPanel.BUTTON_BORDER_COLOR, AcaciaGradientButton.CENTERED);
        okButton.addActionListener(this);
 //       okButton.setFont(TagInputPanel.DEFAULT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.setBackground(TagInputPanel.WINDOW_BACKGROUND_COLOUR);
        
        Dimension bpMaximumSize = buttonPanel.getMaximumSize();
        bpMaximumSize.height = okButton.getPreferredSize().height;
        
        myPanel.add(buttonPanel); 

        SpringUtilities.makeCompactGrid(myPanel, 2, 1, 5, 5, 5, 5);
        
        this.pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        if(e.getSource() == okButton)  
        {
            setVisible(false);
        }
    }
}
