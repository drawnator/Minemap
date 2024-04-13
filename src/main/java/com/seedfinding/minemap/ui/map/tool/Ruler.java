package com.seedfinding.minemap.ui.map.tool;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.minemap.util.math.DisplayMaths;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ruler extends Tool {
    private BPos pos1 = null;
    private BPos pos2 = null;
    private int pointsTraced = 0;
    private Color color;
    private Random rng;

    public Ruler(Random rng) {
        this.rng = rng;
        color = DisplayMaths.getRandomColor(rng);
    }

    @Override
    public boolean addPoint(BPos bpos) {
        switch (pointsTraced) {
            case 0:
                pos1 = bpos;
                break;
            case 1:
                pos2 = bpos;
                break;
            default:
                return false;
        }
        pointsTraced++;
        return true;
    }

    @Override
    public Polygon getPartialShape() {
        int offset = 5;
        switch (this.getPointsTraced()) {
            case 1:
                return DisplayMaths.getPolygon(pos1, offset);
            case 2:
                return DisplayMaths.getPolygon(pos1, pos2, offset);
        }
        return null;
    }

    @Override
    public List<Shape> getPartialShapes() {
        return new ArrayList<>();
    }

    @Override
    public Polygon getExactShape() {
        int offset = 0;
        switch (this.getPointsTraced()) {
            case 1:
                return DisplayMaths.getPolygon(pos1, offset);
            case 2:
                return DisplayMaths.getPolygon(pos1, pos2, offset);
        }
        return null;
    }

    @Override
    public List<Shape> getExactShapes() {
        return new ArrayList<>();
    }

    @Override
    public int getPointsTraced() {
        return pointsTraced;
    }

    @Override
    public boolean isComplete() {
        return this.getPointsTraced() == 2 && pos1 != null && pos2 != null;
    }

    @Override
    public boolean isAcceptable() {
        return isComplete();
    }

    @Override
    public boolean isPartial() {
        switch (this.getPointsTraced()) {
            case 0:
                return false;
            case 1:
                return pos1 != null;
            case 2:
                return pos1 != null && pos2 != null;
        }
        return false;
    }

    @Override
    public void reset() {
        pointsTraced = 0;
        pos1 = null;
        pos2 = null;
    }

    @Override
    public double getMetric() {
        if (this.isComplete()) {
            double metric = DisplayMaths.getDistance2D(pos1, pos2);
            return DisplayMaths.round(metric, 2);
        }
        return 0;
    }

    @Override
    public String[] getMetricString() {
        return new String[] {
            String.format("Distance: %.2f blocks", this.getMetric())
        };
    }

    @Override
    public boolean shouldFill() {
        return true;
    }

    @Override
    public boolean shouldHideArtefact() {
        return false;
    }

    @Override
    public Tool duplicate() {
        return new Ruler(this.rng);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String getName() {
        return "Ruler";
    }

    @Override
    public boolean isMultiplePolygon() {
        return false;
    }

    @Override
    public String toString() {
        return "Ruler{" +
            "pos1=" + pos1 +
            ", pos2=" + pos2 +
            ", pointsTraced=" + pointsTraced +
            '}';
    }
}
