package com.seedfinding.minemap.ui.menubar;

import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.init.KeyShortcuts;
import com.seedfinding.minemap.init.Logger;
import com.seedfinding.minemap.listener.Events;
import com.seedfinding.minemap.ui.dialog.EnterSeedDialog;
import com.seedfinding.minemap.ui.map.MapPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import static com.seedfinding.minemap.config.KeyboardsConfig.getKeyComboString;
import static com.seedfinding.minemap.config.UserProfileConfig.MAX_SIZE;

public class FileMenu extends Menu {
    private final JMenuItem loadSeed;
    private final JMenu recentSeeds;
    private final JMenuItem screenshot;
    private final JMenuItem screenshotFolder;
    private final JMenuItem close;
    public static boolean isClosing = false;

    public FileMenu() {
        this.menu = new JMenu("Home");
        this.menu.setMnemonic(KeyEvent.VK_H);

        this.loadSeed = new JMenuItem("New From Seed");
        this.addMouseAndKeyListener(this.loadSeed, newSeed(), newSeed(), true);

        this.recentSeeds = new JMenu("Recent Seeds");
        this.recentSeeds.addMenuListener(Events.Menu.onSelected(e -> this.addRecentSeedGroup()));

        this.screenshot = new JMenuItem("Screenshot");
        this.addMouseAndKeyListener(this.screenshot, screenshot(), screenshot(), true);

        this.screenshotFolder = new JMenuItem("Open Screenshot Folder");
        this.addMouseAndKeyListener(this.screenshotFolder, screenshotFolder(), screenshotFolder(), true);

        this.close = new JMenuItem("Close");
        this.addMouseAndKeyListener(this.close, close(true), close(true), true);

        this.menu.addMenuListener(Events.Menu.onSelected(e -> screenshot.setEnabled(MineMap.INSTANCE.worldTabs.getSelectedMapPanel() != null)));

        this.menu.add(this.loadSeed);
        this.menu.add(this.recentSeeds);
        this.menu.add(this.screenshot);
        this.menu.add(this.screenshotFolder);
        this.menu.add(this.close);
    }

    public void addRecentSeedGroup() {
        this.recentSeeds.removeAll();
        Object[] recentSeeds = Configs.USER_PROFILE.getRecentSeeds().toArray();
        if (recentSeeds.length > MAX_SIZE) {
            Logger.LOGGER.severe("This is not possible size of recent seeds is more than fixed " + Arrays.toString(recentSeeds) + " " + MAX_SIZE + " " + recentSeeds.length);
        }
        int len = Math.min(MAX_SIZE, recentSeeds.length);
        for (int i = 1; i <= len; i++) {
            String config = (String) recentSeeds[len - i];
            String[] split = config.split("::");
            if (split.length == 2) {
                String seed = split[0];
                String version = split[1];
                MCVersion mcVersion = MCVersion.fromString(version);
                if (mcVersion != null) {
                    JMenuItem item = new JMenuItem(seed + " [" + mcVersion + "]");
                    this.addMouseAndKeyListener(item, openSeed(seed, mcVersion), openSeed(seed, mcVersion), true);
                    this.recentSeeds.add(item);
                }
            }
        }
    }

    public Runnable openSeed(String seed, MCVersion version) {
        return () -> {
            MineMap.INSTANCE.worldTabs.load(
                version,
                seed,
                Configs.USER_PROFILE.getThreadCount(),
                Configs.USER_PROFILE.getEnabledDimensions()
            );
        };
    }

    public final Runnable newSeed() {
        return () -> {
            this.activate.run();
            EnterSeedDialog dialog = new EnterSeedDialog(this.deactivate);
            dialog.setVisible(true);
        };
    }

    public final Runnable screenshot() {
        return () -> {
            if (!this.screenshot.isEnabled()) return;

            MapPanel map = MineMap.INSTANCE.worldTabs.getSelectedMapPanel();
            if (map == null) return;
            BufferedImage image = map.getScreenshot();

            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            File dir = new File(MineMap.SCREENSHOTS_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                Logger.LOGGER.severe("Screenshot dir doesn't exists yet");
                return;
            }
            File file = new File(MineMap.SCREENSHOTS_DIR + File.separatorChar + fileName + ".png");
            try {
                ImageIO.write(image, "png", file);
            } catch (IOException e) {
                Logger.LOGGER.severe(e.toString());
                e.printStackTrace();
            }
        };
    }

    public final Runnable screenshotFolder() {
        return () -> {
            Desktop desktop = Desktop.getDesktop();
            File dir = new File(MineMap.SCREENSHOTS_DIR);
            if (!dir.exists() && !dir.mkdirs()) return;
            try {
                desktop.open(dir);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.LOGGER.warning("Screenshot folder could not be opened");
            }
        };
    }

    public final Runnable close(boolean displayMsg) {
        return () -> {
            if (isClosing) return;
            int input;
            if (displayMsg) {
                isClosing = true;
                this.activate.run();
                input = JOptionPane.showConfirmDialog(null, "Do you want to close MineMap?", "Close MineMap", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                this.deactivate.run();
                isClosing = false;
            } else {
                input = 0;
            }
            if (input == 0) {
                for (Frame frame : JFrame.getFrames()) {
                    frame.dispose();
                }
                System.exit(0);
            }

        };
    }

    @Override
    public void doDelayedLabels() {
        this.loadSeed.setText(String.format("New From Seed (%s)", getKeyComboString(KeyShortcuts.ShortcutAction.NEW_SEED)));
        this.screenshot.setText(String.format("Screenshot (%s)", getKeyComboString(KeyShortcuts.ShortcutAction.SCREENSHOT)));
        this.screenshotFolder.setText(String.format("Open Screenshot Folder (%s)", getKeyComboString(KeyShortcuts.ShortcutAction.SCREENSHOT_FOLDER)));
        this.close.setText(String.format("Close (%s)", getKeyComboString(KeyShortcuts.ShortcutAction.CLOSE)));
    }
}
