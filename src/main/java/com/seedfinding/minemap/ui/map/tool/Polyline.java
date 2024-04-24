package com.seedfinding.minemap.ui.map.tool;

import com.seedfinding.latticg.util.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.ui.map.MapManager;
import com.seedfinding.minemap.util.data.ListUtil;
import com.seedfinding.minemap.util.math.DisplayMaths;
import com.seedfinding.minemap.util.tsp.Program;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class Polyline extends AbstractTool {
    private final LinkedList<BPos> bPosList = new LinkedList<>();
    private BPos firstPoint = null;
    private int pointsTraced = 0;
    private Color color;
    private final Program program = new Program();
    private Random rng;

    public Polyline(Random rng) {
        this.rng = rng;
        color = DisplayMaths.getRandomColor(rng);
    }

    @Override
    public boolean addPoint(BPos bpos) {
        if (firstPoint == null) firstPoint = bpos;
        bPosList.add(bpos);
        pointsTraced++;
        MapManager manager = MineMap.INSTANCE.worldTabs.getSelectedMapPanel().getManager();
        SwingUtilities.invokeLater(() -> {
            Pair<Double, List<Integer>> res = program.startApplication(bPosList);
            ListUtil.reindexInPlace(bPosList, res.getSecond().toArray(new Integer[0]));
            ListUtil.cycleInPlace(bPosList, firstPoint);
            manager.getPanel().rightBar.tooltip.updateToolsMetrics();
        });
        return true;
    }

    @Override
    public Polygon getPartialShape() {
        return null;
    }

    @Override
    public List<Shape> getPartialShapes() {
        int offset = 5;
        if (bPosList.size() > 1) {
            return DisplayMaths.getPolylinePolygon(bPosList, offset);
        }
        return new ArrayList<>();
    }

    @Override
    public Polygon getExactShape() {
        return null;
    }

    @Override
    public List<Shape> getExactShapes() {
        int offset = 0;
        if (bPosList.size() > 1) {
            return DisplayMaths.getPolylinePolygon(bPosList, offset);
        }
        return new ArrayList<>();
    }

    @Override
    public int getPointsTraced() {
        return pointsTraced;
    }

    @Override
    public boolean isComplete() {
        return bPosList.size() > 1;
    }

    @Override
    public boolean isAcceptable() {
        return bPosList.size() > 1;
    }

    @Override
    public boolean isPartial() {
        return bPosList.size() > 1;
    }

    @Override
    public void reset() {
        pointsTraced = 0;
        bPosList.clear();
    }

    @Override
    public double getMetric() {
        double metric = 0.0D;
        for (int i = 0; i < bPosList.size() - 1; i++) {
            BPos pos1 = bPosList.get(i);
            BPos pos2 = bPosList.get(i + 1);
            metric += DisplayMaths.getDistance2D(pos1, pos2);
        }
        return DisplayMaths.round(metric, 2);
    }

    @Override
    public String[] getMetricString() {
        return new String[] {
            String.format("Polyline: %.2f blocks", this.getMetric())
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
    public AbstractTool duplicate() {
        return new Polyline(this.rng);
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
        return "Polyline";
    }

    @Override
    public boolean isMultiplePolygon() {
        return true;
    }

    @Override
    public String toString() {
        return "Polyline{" +
            "bPosList=" + bPosList +
            ", pointsTraced=" + pointsTraced +
            ", color=" + color +
            '}';
    }
}
