package kaptainwutax.minemap.ui;

import kaptainwutax.biomeutils.Biome;
import kaptainwutax.biomeutils.layer.BiomeLayer;
import kaptainwutax.biomeutils.layer.LayerStack;
import kaptainwutax.minemap.listener.Events;
import kaptainwutax.minemap.ui.component.Dropdown;
import kaptainwutax.minemap.util.Fragment;
import kaptainwutax.minemap.util.RegionScheduler;
import kaptainwutax.minemap.util.WorldInfo;
import kaptainwutax.seedutils.mc.pos.BPos;
import kaptainwutax.seedutils.util.math.Vec3i;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class MapPanel extends JPanel {

	public final int blocksPerFragment = 512;
	public double pixelsPerFragment = (int)(300.0D * (this.blocksPerFragment / 512.0D));

	public double centerX;
	public double centerY;

	public Point mousePointer;
	public String tooltip = null;

	public final WorldInfo info;
	public final int threadCount;
	public RegionScheduler scheduler;

	public MapPanel(WorldInfo info, int threadCount) {
		this.info = info;
		this.threadCount = threadCount;
		this.restart();

		this.setBackground(Color.BLACK);
		
		this.addMouseWheelListener(e -> {
			double newPixelsPerFragment = this.pixelsPerFragment;

			if(e.getUnitsToScroll() > 0) {
				newPixelsPerFragment /= e.getUnitsToScroll() / 2.0D;
			} else {
				newPixelsPerFragment *= -e.getUnitsToScroll() / 2.0D;
			}

			if(newPixelsPerFragment < 40.0D * (this.blocksPerFragment / 512.0D)) {
				newPixelsPerFragment = 40.0D * (this.blocksPerFragment / 512.0D);
			} else if(newPixelsPerFragment > 2000.0D * (this.blocksPerFragment / 512.0D)) {
				newPixelsPerFragment = 2000.0D * (this.blocksPerFragment / 512.0D);
			}

			double scaleFactor = newPixelsPerFragment / this.pixelsPerFragment;
			this.centerX *= scaleFactor;
			this.centerY *= scaleFactor;
			this.pixelsPerFragment = newPixelsPerFragment;
			MapPanel.this.repaint();
		});

		this.addMouseListener(Events.Mouse.onPressed(e -> {
			mousePointer = e.getPoint();
			setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			MapPanel.this.repaint();

			//TODO: move layers
			//if(e.isControlDown()) {
			//	MapPanel2.this.layer = MapPanel2.this.layer.getParent();
			//	MapPanel2.this.regions.clear();
			//	MapPanel2.this.repaint();
			//}
		}));

		this.addMouseListener(Events.Mouse.onReleased(mouseEvent -> {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}));

		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int dx = e.getX() - mousePointer.x;
				int dy = e.getY() - mousePointer.y;
				mousePointer = e.getPoint();
				centerX += dx;
				centerY += dy;
				MapPanel.this.repaint();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				BPos pos = getPos(e.getX(), e.getY());
				int x = pos.getX();
				int z = pos.getZ();

				Biome biome = info.getBiome(x, z);
				tooltip = String.format("Seed %d at (%d, %d): %s", info.worldSeed, x, z, biome == null ? "UNKNOWN" : biome.getName().toUpperCase());
				MapPanel.this.repaint();
			}
		});

		LayerStack<BiomeLayer> layers = this.info.getBiomeSource().getLayers();
		Dropdown<Integer> layerDropdown = new Dropdown<>(
				i -> "[" + i + "] " + layers.get(i).getClass().getSimpleName() + " " + layers.get(i).getScale() + ":1",
				IntStream.range(0, layers.size()).boxed());

		layerDropdown.selectIfPresent(this.info.layerId);

		layerDropdown.addActionListener(e1 -> {
			this.info.layerId = layerDropdown.getSelected();
			this.restart();
		});

		this.add(layerDropdown);
	}

	public BPos getPos(double mouseX, double mouseY) {
		Vec3i screenSize = this.getScreenSize();
		double x = (mouseX - screenSize.getX() / 2.0D - centerX) / screenSize.getX();
		double y = (mouseY - screenSize.getZ() / 2.0D - centerY) / screenSize.getZ();
		double blocksPerWidth = (screenSize.getX() / pixelsPerFragment) * (double) blocksPerFragment;
		double blocksPerHeight = (screenSize.getZ() / pixelsPerFragment) * (double) blocksPerFragment;
		x *= blocksPerWidth;
		y *= blocksPerHeight;
		int xi = (int)Math.round(x);
		int yi = (int)Math.round(y);
		return new BPos(xi, 0, yi);
	}

	public BPos getCenterPos() {
		Vec3i screenSize = this.getScreenSize();
		return getPos(screenSize.getX() / 2.0D, screenSize.getZ() / 2.0D);
	}

	public void setCenterPos(int posX, int posZ) {
		double scaleFactor = this.pixelsPerFragment / this.blocksPerFragment;
		this.centerX = -posX * scaleFactor;
		this.centerY = -posZ * scaleFactor;
		this.repaint();
	}

	public Vec3i getScreenSize() {
		return new Vec3i(this.getWidth(), 0, this.getHeight());
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		this.scheduler.purge();

		Map<Fragment, DrawInfo> drawQueue = this.getDrawQueue(g);
		drawQueue.forEach((fragment, d) -> fragment.drawBiomes(d.g, d.x, d.y, d.width, d.height));
		drawQueue.forEach((fragment, d) -> fragment.drawStructures(d.g, d.x, d.y, d.width, d.height));

		g.setColor(Color.CYAN);
		g.fillOval(this.getWidth() / 2 - 2, this.getHeight() / 2 - 2, 5, 5);

		if(this.tooltip != null) {
			g.setColor(Color.WHITE);
			g.drawString(this.tooltip, 20, 30);
		}
	}

	public Map<Fragment, DrawInfo> getDrawQueue(Graphics g) {
		Map<Fragment, DrawInfo> drawQueue = new HashMap<>();
		int w = this.getWidth(), h = this.getHeight();

		for(int x = -1; x < w / this.pixelsPerFragment + 1; x += Math.max(1, (int)(1.0D / this.pixelsPerFragment))) {
			for(int y = -1; y < h / this.pixelsPerFragment + 1; y += Math.max(1, (int)(1.0D / this.pixelsPerFragment))) {
				int rx = x - (int)((this.centerX + w / 2) / this.pixelsPerFragment);
				int ry = y - (int)((this.centerY + h / 2) / this.pixelsPerFragment);

				Fragment fragment = this.scheduler.getFragmentAt(rx, ry);

				int x1 = (int)(x * this.pixelsPerFragment + (this.centerX + w / 2) % this.pixelsPerFragment);
				int y1 = (int)(y * this.pixelsPerFragment + (this.centerY + h / 2) % this.pixelsPerFragment);

				if(fragment != null) {
					drawQueue.put(fragment, new DrawInfo(g, x1, y1, (int)this.pixelsPerFragment + 1, (int)this.pixelsPerFragment + 1));
				}
			}
		}

		return drawQueue;
	}

	public void restart() {
		this.scheduler = new RegionScheduler(this, this.threadCount);
		this.repaint();
	}

	public BufferedImage screenshot() {
		BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		Map<Fragment, DrawInfo> drawQueue = this.getDrawQueue(image.getGraphics());
		drawQueue.forEach((fragment, d) -> fragment.drawBiomes(d.g, d.x, d.y, d.width, d.height));
		drawQueue.forEach((fragment, d) -> fragment.drawStructures(d.g, d.x, d.y, d.width, d.height));
		return image;
	}

	public static class DrawInfo {
		public final Graphics g;
		public final int x;
		public final int y;
		public final int width;
		public final int height;

		public DrawInfo(Graphics g, int x, int y, int width, int height) {
			this.g = g;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
	}

}
