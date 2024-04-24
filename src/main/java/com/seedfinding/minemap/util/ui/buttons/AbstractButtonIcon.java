package com.seedfinding.minemap.util.ui.buttons;

import com.seedfinding.minemap.init.Icons;
import com.seedfinding.minemap.util.ui.graphics.Graphic;
import com.seedfinding.minemap.util.ui.graphics.Icon;
import com.seedfinding.minemap.util.ui.graphics.RoundedBorder;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractButtonIcon extends JButton {
    public final int size;
    public final int inset;
    public final float factor;
    public boolean background;
    public final boolean border;
    public Color backgroundColor;

    public AbstractButtonIcon(int size, int inset, float factor, boolean background, Color backgroundColor) {
        this(size, inset, factor, background, backgroundColor, true);
    }

    public AbstractButtonIcon(int size, int inset, float factor, boolean background, Color backgroundColor, boolean border) {
        super();
        this.size = size;
        this.inset = inset;
        this.factor = factor;
        this.border = border;
        this.background = background;
        this.backgroundColor = backgroundColor;
    }

    public AbstractButtonIcon(int size, int inset, float factor) {
        this(size, inset, factor, false, Color.WHITE);
    }

    public AbstractButtonIcon(int size, int inset) {
        this(size, inset, 1.7F);
    }

    public AbstractButtonIcon(int size) {
        this(size, 1);
    }

    public AbstractButtonIcon() {
        this(16);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(30, 30);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void shouldBackground(boolean b) {
        this.background = b;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        this.setOpaque(false);
        this.setContentAreaFilled(false);
        if (border) this.setBorder(new RoundedBorder(size - 2, 30));
        this.setForeground(Color.DARK_GRAY);

        Graphics2D g2d = Graphic.setGoodRendering(Graphic.withoutDithering(g));

        if (this.background) {
            Color old = g2d.getColor();
            g2d.setColor(backgroundColor);
            int bSize = 30;
            int bRadius = size - 2;
            int bDiff = bSize / 2 - bRadius;
            g2d.fillRoundRect(bDiff, bDiff, bRadius * 2 - 2, bRadius * 2 - 2, bSize, bSize);

            g2d.setColor(old);
        }
        Icon.paintImage(Icons.get(this.getClass()), g2d, size, factor, inset);
    }

    public void changeBColor(Color color) {
        this.backgroundColor = color;
        this.revalidate();
        this.repaint();
    }
}
