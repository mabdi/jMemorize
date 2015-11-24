package jmemorize.gui.swing.panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import jmemorize.gui.swing.ColorConstants;

public class ScaledImagePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Image     m_image;
    private int       m_padding = 2;

    public ScaledImagePanel() {
    	setBackground(ColorConstants.CARD_PANEL_COLOR);
//        setForeground(m_textPane.getForeground());
	}
    
    public void setImageToDisplay(Image imageToDisplay)
    {
        m_image = imageToDisplay;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if (m_image == null)
            return;
        
        int imgWidth = m_image.getWidth(null);
        int imgHeight = m_image.getHeight(null);
        
        Dimension dimension = getSize();
        int w = dimension.width;
        int h = dimension.height;
        int padding = 0;
        
        if (imgWidth > w || imgHeight > h)
        {
            float ratio = imgWidth / (float)w;
            h = (int)(imgHeight / ratio);
            
            if (h > dimension.height)
            {
                h = dimension.height;
                ratio = imgHeight / (float)h;
                w = (int)(imgWidth/ ratio);
            }
            
            padding = m_padding;
        }
        else
        {
            w = imgWidth;
            h = imgHeight;
        }
        
        int left = padding + (dimension.width  - w) / 2;
        int top  = padding + (dimension.height - h) / 2;
        
        if (g instanceof Graphics2D)
        {
            Graphics2D g2d = (Graphics2D)g;
            
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        
        g.drawImage(m_image, 
            left, top, left + w - 2*padding, top + h - 2*padding, 
            0, 0, imgWidth, imgHeight, null);
    }

}
