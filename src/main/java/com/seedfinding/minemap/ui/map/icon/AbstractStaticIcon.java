package com.seedfinding.minemap.ui.map.icon;

import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.init.Icons;
import com.seedfinding.minemap.ui.map.MapContext;
import com.seedfinding.minemap.ui.map.fragment.Fragment;
import com.seedfinding.minemap.util.data.DrawInfo;
import com.seedfinding.minemap.util.ui.graphics.Graphic;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.function.Function;

import static com.seedfinding.minemap.util.ui.graphics.Icon.paintImage;

public abstract class AbstractStaticIcon extends AbstractIconRenderer {

    private static final int DEFAULT_VALUE = 24;
    private final int iconSizeX;
    private final int iconSizeZ;

    public AbstractStaticIcon(MapContext context) {
        this(context, DEFAULT_VALUE, DEFAULT_VALUE);
    }

    public AbstractStaticIcon(MapContext context, int iconSizeX, int iconSizeZ) {
        super(context);
        this.iconSizeX = iconSizeX;
        this.iconSizeZ = iconSizeZ;
    }

    public Function<BPos, String> getExtraInfo() {
        return null;
    }

    public Function<BPos, Pair<Color, BufferedImage>> getExtraIcon() {
        return null;
    }

    @Override
    public void render(Graphics graphics, DrawInfo info, Feature<?, ?> feature, Fragment fragment, BPos pos, boolean hovered) {
        float scaleFactor = (float) (getZoomScaleFactor() * Configs.ICONS.getSize(feature.getClass()) * (hovered ? this.getHoverScaleFactor() : 1));
        int sx = (int) ((double) (pos.getX() - fragment.getX()) / fragment.getSize() * info.width);
        int sy = (int) ((double) (pos.getZ() - fragment.getZ()) / fragment.getSize() * info.height);
        Graphics2D g2d = Graphic.setGoodRendering(Graphic.withoutDithering(graphics));
        BufferedImage icon = Icons.get(feature.getClass());
        paintImage(icon, g2d, DEFAULT_VALUE, new Pair<>(scaleFactor, scaleFactor), new Pair<>(info.x + sx, info.y + sy), true);

        if (getExtraInfo() != null && this.getContext().getSettings().showExtraInfos) {
            String stringInfo = getExtraInfo().apply(pos);
            if (stringInfo != null) {
                Color old = g2d.getColor();

                char[] charArray = stringInfo.toCharArray();
                int posX = info.x + sx + 5 * (charArray.length == 1 ? 1 : 0);
                int posY = (int) (info.y + sy - 5 + DEFAULT_VALUE * scaleFactor);

                // back characters (shadow effect)
                g2d.setColor(Color.BLACK);
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 14F * scaleFactor));
                g2d.drawChars(charArray, 0, charArray.length, posX - 1, posY - 1);

                // front characters (reading)
                g2d.setColor(Color.WHITE);
                g2d.setFont(g2d.getFont().deriveFont(Font.BOLD, 13 * scaleFactor));
                g2d.drawChars(charArray, 0, charArray.length, posX, posY);

                g2d.setColor(old);
            }
        }
        if (getExtraIcon() != null && this.getContext().getSettings().showExtraIcons) {
            Pair<Color, BufferedImage> extraIcon = getExtraIcon().apply(pos);
            if (extraIcon != null) {
                int posX = (int) (info.x + sx + (DEFAULT_VALUE - 16) * scaleFactor / 2);
                int posY = (int) (info.y + sy - (DEFAULT_VALUE + 16) * scaleFactor / 2);
                Shape oldClip = g2d.getClip();
                if (oldClip != null && oldClip.contains(posX - 5, posY - 5)) {
                    Color oldColor = g2d.getColor();
                    int size = (int) (16 * scaleFactor);
                    g2d.setClip(new Ellipse2D.Float(posX, posY, size, size));
                    g2d.setColor(extraIcon.getFirst());
                    g2d.fillRect(posX, posY, size, size);
                    int offset = Math.max(1, (int) (2 * scaleFactor));
                    paintImage(extraIcon.getSecond(), g2d, 12, new Pair<>(scaleFactor, scaleFactor), new Pair<>(posX + offset, posY + offset), false);
                    g2d.setColor(oldColor);
                    g2d.setClip(oldClip);
                }
            }
        }
    }


    @Override
    public boolean isHovered(Fragment fragment, BPos hoveredPos, BPos featurePos, int width, int height, Feature<?, ?> feature) {
        double scaleFactor = this.getHoverScaleFactor() * this.getZoomScaleFactor() * Configs.ICONS.getSize(feature.getClass()) / 2.0D;
        double distanceX = (fragment.getSize() / (double) width) * this.iconSizeX * scaleFactor;
        double distanceZ = (fragment.getSize() / (double) height) * this.iconSizeZ * scaleFactor;
        int dx = Math.abs(hoveredPos.getX() - featurePos.getX());
        int dz = Math.abs(hoveredPos.getZ() - featurePos.getZ());
        return dx < distanceX && dz < distanceZ;
    }

}
