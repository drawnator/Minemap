package com.seedfinding.minemap.ui.map.fragment;

import com.seedfinding.mccore.util.data.ThreadPool;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.init.Logger;
import com.seedfinding.minemap.ui.map.MapPanel;
import com.seedfinding.minemap.ui.map.tool.AbstractTool;
import com.seedfinding.minemap.util.data.DrawInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FragmentScheduler {

    protected final Map<RPos, Fragment> fragments = new ConcurrentHashMap<>();
    private final AtomicBoolean scheduledModified = new AtomicBoolean(false);
    public static Fragment LOADING_FRAGMENT = new Fragment(0, 0, 0, null) {
        @Override
        public void drawBiomes(Graphics graphics, DrawInfo info) {}

        @Override
        public void drawFeatures(Graphics graphics, DrawInfo info) {}

        @Override
        public void drawTools(Graphics graphics, DrawInfo info, ArrayList<AbstractTool> tools) {}
    };
    public List<RPos> scheduledRegions = Collections.synchronizedList(new ArrayList<>());
    protected ThreadPool executor;
    protected MapPanel listener;

    public FragmentScheduler(MapPanel listener, int threadCount) {
        this.listener = listener;
        this.executor = new ThreadPool(threadCount + 1);

        this.executor.run(() -> {
            while (!this.executor.getExecutor().isShutdown()) {
                RPos nearest = this.getNearestScheduled();

                if (nearest == null) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Logger.LOGGER.severe(e.toString());
                        e.printStackTrace();
                    }
                    continue;
                } else if (!this.isInBounds(nearest)) {
                    this.fragments.remove(nearest);
                    this.scheduledRegions.remove(nearest);
                    continue;
                }

                this.scheduledRegions.remove(nearest);

                try {
                    this.executor.run(() -> {
                        Fragment fragment = new Fragment(nearest, this.listener.getContext());
                        this.fragments.put(nearest, fragment);
                        SwingUtilities.invokeLater(() -> this.listener.repaint());
                    });
                } catch (RejectedExecutionException ignored) {

                }

                this.executor.awaitFreeThread();
            }
        });
    }

    public void forEachFragment(Consumer<Fragment> consumer) {
        this.fragments.values().forEach(consumer);
    }

    public void terminate() {
        this.executor.shutdown();
    }

    public void purge() {
        this.scheduledRegions.removeIf(region -> !this.isInBounds(region));
        this.fragments.entrySet().removeIf(e -> !this.isInBounds(e.getKey()));
    }

    public RPos getNearestScheduled() {
        if (this.scheduledModified.getAndSet(false)) {
            SwingUtilities.invokeLater(() -> this.scheduledRegions.sort(Comparator.comparingDouble(this::distanceToCenter)));
        }

        if (!this.scheduledRegions.isEmpty()) {
            return this.scheduledRegions.get(0);
        }

        return null;
    }

    public double distanceToCenter(RPos regionPos) {
        return regionPos.distanceTo(this.listener.getManager().getCenterPos()
                .toRegionPos(this.listener.getManager().blocksPerFragment),
            Configs.USER_PROFILE.getUserSettings().getFragmentMetric());
    }

    public boolean isInBounds(RPos region) {
        BPos min = this.listener.getManager().getPos(0, 0);
        BPos max = this.listener.getManager().getPos(this.listener.getWidth(), this.listener.getHeight());
        RPos regionMin = min.toRegionPos(this.listener.getManager().blocksPerFragment);
        RPos regionMax = max.toRegionPos(this.listener.getManager().blocksPerFragment);
        if (region.getX() < regionMin.getX() - 40 || region.getX() > regionMax.getX() + 40) return false;
        return region.getZ() >= regionMin.getZ() - 40 && region.getZ() <= regionMax.getZ() + 40;
    }

    public Fragment getFragmentAt(int regionX, int regionZ) {
        return this.getFragmentAt(regionX, regionZ, 1);
    }

    public Fragment getFragmentAt(int regionX, int regionZ, int factor) {
        RPos regionPos = new RPos(regionX, regionZ, this.listener.getManager().blocksPerFragment * factor);

        if (!this.fragments.containsKey(regionPos) && !this.scheduledRegions.contains(regionPos)) {
            this.fragments.put(regionPos, LOADING_FRAGMENT);
            this.scheduledRegions.add(regionPos);
            this.scheduledModified.set(true);
        }

        return this.fragments.get(regionPos);
    }
}
