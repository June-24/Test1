package com.example.test1;

import java.awt.*;
import javax.swing.border.AbstractBorder;

public class RoundedBorder extends AbstractBorder {
    private int radius;
    private Color outlineColor;
    private int thickness;

    public RoundedBorder(int radius, Color outlineColor, int thickness) {
        this.radius = radius;
        this.outlineColor = outlineColor;
        this.thickness = thickness;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate the inner bounds for the filled area
        int innerX = x + thickness;
        int innerY = y + thickness;
        int innerWidth = width - 2 * thickness;
        int innerHeight = height - 2 * thickness;

        // Fill the internal rounded rectangle
        g2d.setColor(c.getBackground());
        g2d.fillRoundRect(innerX, innerY, innerWidth, innerHeight, radius, radius);

        // Draw the outline rounded rectangle
        g2d.setColor(outlineColor);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawRoundRect(innerX, innerY, innerWidth, innerHeight, radius, radius);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(radius + thickness, radius + thickness, radius + thickness, radius + thickness);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = radius + thickness;
        return insets;
    }
}


