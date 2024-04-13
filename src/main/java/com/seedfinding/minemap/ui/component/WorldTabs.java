package com.seedfinding.minemap.ui.component;

import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.init.KeyShortcuts;
import com.seedfinding.minemap.listener.Events;
import com.seedfinding.minemap.ui.map.MapPanel;
import com.seedfinding.minemap.util.data.Str;
import com.seedfinding.minemap.util.ui.buttons.SquareCloseButton;
import com.seedfinding.minemap.util.ui.graphics.Graphic;
import com.seedfinding.minemap.util.ui.graphics.Icon;
import com.seedfinding.minemap.util.ui.interactive.Dropdown;
import com.seedfinding.minemap.util.ui.interactive.ExtendedTabbedPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorldTabs extends ExtendedTabbedPane {
    public static final Color BACKGROUND_COLOR = new Color(60, 63, 65);
    public static final Color BACKGROUND_COLOR_LIGHT = new Color(191, 191, 191);
    protected final Set<TabGroup> tabGroups = new HashSet<>();
    public final Dropdown<TabGroup> dropdown = new Dropdown<TabGroup>(
        // what is better than a hashcode that can roll? a timestamp!
        j -> String.format("%d [%s]::%s", j.getWorldSeed(), j.getVersion(), System.nanoTime()),
        (value, element) -> value != null ? ((String) value).split("::")[0] + (element != null && element.isLocked() ? " \uD83D\uDD12" : "") : null
    );
    public final JButton closeAllCurrent;
    public TabGroup current;
    private final Random rng;

    public WorldTabs() {
        super(ComponentOrientation.LEFT_TO_RIGHT);
        this.rng = new Random();
        //Copy seed to clipboard.
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getKeyCode() != KeyEvent.VK_C || (e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) == 0) return false;
            MapPanel map = this.getSelectedMapPanel();
            if (map == null) return false;
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(String.valueOf(map.getContext().worldSeed)), null);
            return true;
        });
        this.closeAllCurrent = new JButton(Icon.getIcon(SquareCloseButton.class, 28, 28, null));
        this.closeAllCurrent.setToolTipText("Close current seed");
        this.closeAllCurrent.addActionListener(e -> closeTabs());
        this.addSideComponent(closeAllCurrent, ButtonSide.TRAILING);
        this.addSideComponent(dropdown, ButtonSide.TRAILING);
        dropdown.addActionListener(e -> {
            if (dropdown.getSelected() != current && dropdown.getSelected() != null) {
                this.cleanSetTabGroup(dropdown.getSelected());
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                WorldTabs.super.repaint();
            }
        });
        this.getJTabbedPane().addChangeListener(e -> {
            MapPanel mapPanel = this.getSelectedMapPanel();
            if (mapPanel != null) {
                mapPanel.rightBar.searchBox.setVisible(!Configs.USER_PROFILE.getUserSettings().hideDockableContainer);
                mapPanel.rightBar.chestBox.setVisible(!Configs.USER_PROFILE.getUserSettings().hideDockableContainer);
            }
        });
        this.getJTabbedPane().addMouseListener(Events.Mouse.onPressed(e -> {
            if (getSelectedMapPanel() == null) {
                KeyShortcuts.ShortcutAction.NEW_SEED.action.run();
            }
        }));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getSelectedMapPanel() == null) {
            Font old = g.getFont();
            g.setFont(new Font(old.getName(), old.getStyle(), 20));
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            String text = "Click here to create a new seed";
            Graphics2D g2d = Graphic.setGoodRendering(Graphic.withDithering(g));
            int x = (this.getWidth() - metrics.stringWidth(text)) / 2;
            int y = (this.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g2d.drawString(text, x, y);
            g.setFont(old);
        }

    }

    @Override
    public TabHeader getSelectedHeader() {
        return (TabHeader) super.getSelectedHeader();
    }

    public TabGroup load(MCVersion version, String worldSeed, int threadCount, Collection<Dimension> dimensions, boolean shouldSwitch) {
        TabGroup tabGroup = new TabGroup(this.rng, version, worldSeed, threadCount, dimensions, !shouldSwitch);
        if (this.tabGroups.contains(tabGroup)) {
            return null;
        }
        this.tabGroups.add(tabGroup);
        this.dropdown.add(tabGroup);
        if (shouldSwitch) this.cleanSetTabGroup(tabGroup);
        return tabGroup;
    }

    public TabGroup load(MCVersion version, String worldSeed, int threadCount, Collection<Dimension> dimensions) {
        return this.load(version, worldSeed, threadCount, dimensions, true);
    }

    public void cleanSetTabGroup(TabGroup tabGroup) {
        // remove all elements in the jtabbedpane
        this.removeAll();
        if (tabGroup == null) return;
        this.dropdown.setDefault(tabGroup);
        this.current = tabGroup;
        this.setTabGroup(tabGroup);
    }

    private void setTabGroup(TabGroup tabGroup) {
        AtomicBoolean first = new AtomicBoolean(true);

        tabGroup.getPanels().forEach((dimension, mapPanel) -> {
            this.addMapTab(Str.prettifyDashed(dimension.getName()), tabGroup, mapPanel);
            if (first.get()) {
                this.setSelectedIndex(this.getTabCount() - 1);
                first.set(false);
            }
        });
    }

    @Override
    public void remove(Component component) {
        this.getJTabbedPane().remove(component);
    }

    public void remove(TabGroup tabGroup) {
        if (tabGroup == null) return;
        for (MapPanel mapPanel : new ArrayList<>(tabGroup.getMapPanels())) {
            this.remove(tabGroup, mapPanel);
        }
        if (tabGroup.getMapPanels().isEmpty()) {
            this.tabGroups.remove(tabGroup);
            this.dropdown.remove(tabGroup);
            current = this.dropdown.getSelected();
            this.repaint();
        }
    }

    public MapPanel getSelectedMapPanel() {
        Component component = this.getSelectedComponent();
        return component instanceof MapPanel ? (MapPanel) component : null;
    }

    public TabGroup getCurrentTabGroup() {
        return current;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (MineMap.isDarkTheme()) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        super.paintComponent(g);
    }

    public synchronized void invalidateAll() {
        this.tabGroups.forEach(TabGroup::invalidateAll);
    }

    public void remove(TabGroup tabGroup, MapPanel mapPanel) {
        if (tabGroup != null && mapPanel != null) {
            if (!mapPanel.getHeader().isSaved) {
                tabGroup.removeIfPresent(mapPanel);
                this.remove(mapPanel);
            }
            if (tabGroup.getMapPanels().isEmpty()) {
                this.tabGroups.remove(tabGroup);
                this.dropdown.remove(tabGroup);
                current = this.dropdown.getSelected();
                this.repaint();
            }

        }
    }

    public static void closeTab() {
        Component component = MineMap.INSTANCE.worldTabs.getSelectedComponent();
        TabGroup current = MineMap.INSTANCE.worldTabs.getCurrentTabGroup();
        if (component instanceof MapPanel && current != null) {
            MineMap.INSTANCE.worldTabs.remove(current, (MapPanel) component);
        }
    }

    public static void closeTab(Component component) {
        TabGroup current = MineMap.INSTANCE.worldTabs.getCurrentTabGroup();
        if (component instanceof MapPanel && current != null) {
            MineMap.INSTANCE.worldTabs.remove(current, (MapPanel) component);
        }
    }

    public static void closeTabs() {
        MineMap.INSTANCE.worldTabs.remove(MineMap.INSTANCE.worldTabs.getCurrentTabGroup());
    }

    public static void cycle(boolean isRight) {
        MineMap.INSTANCE.worldTabs.cycleSeed(isRight);
    }

    public void cycleSeed(boolean isRight) {
        TabGroup tabGroup = isRight ? this.dropdown.getCycleRight() : this.dropdown.getCycleLeft();
        if (tabGroup == null) return;
        if (tabGroup != current) {
            this.cleanSetTabGroup(tabGroup);
        }
    }

    public void addMapTab(String title, TabGroup tabGroup, MapPanel mapPanel) {
        mapPanel.updateInteractive();
        if (mapPanel.getHeader() != null) {
            this.addTab(title, mapPanel, mapPanel.getHeader());
        } else {
            TabHeader tabHeader = new TabHeader(title, e -> {
                if (e.isShiftDown()) closeTabs();
                else closeTab(mapPanel);
            });

            tabHeader.setComponentPopupMenu(createTabMenu(tabGroup, mapPanel));
            tabHeader.addMouseListener(Events.Mouse.onReleased(e -> {
                if (e.getSource() instanceof TabHeader) {
                    TabHeader source = (TabHeader) e.getSource();
                    this.setSelectedIndex(this.indexOfTab(source.getTabTitle().getText()));
                }
            }));
            mapPanel.setHeader(tabHeader);
            this.addTab(title, mapPanel, tabHeader);
        }

    }

    public JPopupMenu createTabMenu(TabGroup current, MapPanel mapPanel) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem removeOthers = new JMenuItem("Close Other Tab Groups");
        removeOthers.setBorder(new EmptyBorder(5, 15, 5, 15));

        removeOthers.addMouseListener(Events.Mouse.onReleased(e -> {
            int input = JOptionPane.showConfirmDialog(null, "Do you really want to CLOSE all the other opened seeds?", "Close other opened seeds", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (input != 0) {
                return;
            }
            this.tabGroups.clear();
            this.dropdown.removeAllItems();
            this.dropdown.elements.clear();
            this.dropdown.strings.clear();
            this.dropdown.order.clear();

            MineMap.INSTANCE.loadPinnedSeeds();
            this.dropdown.add(current);
            this.tabGroups.add(current);
            cleanSetTabGroup(current);

            this.repaint();
        }));
        popup.add(removeOthers);

        JMenuItem copySeed = new JMenuItem("Copy seed");
        copySeed.setBorder(new EmptyBorder(5, 15, 5, 15));

        copySeed.addMouseListener(Events.Mouse.onReleased(e -> {
            StringSelection content = new StringSelection(String.valueOf(mapPanel.getContext().worldSeed));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
        }));


        popup.add(copySeed);
        return popup;
    }
}


