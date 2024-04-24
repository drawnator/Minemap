package com.seedfinding.minemap.ui.map;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.minemap.ui.map.fragment.Fragment;
import com.seedfinding.minemap.ui.map.icon.*;
import com.seedfinding.minemap.util.data.DrawInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IconManager {

    private final MapContext context;
    public Map<Feature<?, ?>, AbstractIconRenderer> renderers;

    public IconManager(MapContext context) {
        this.context = context;
        this.renderers = context.getSettings().getAllFeatures().stream()
            .collect(Collectors.toMap(f -> f, f -> NullIcon.INSTANCE));

        this.override(
            SpawnIcon::new,
            RegionIcon::new,
            MineshaftIcon::new,
            SlimeIcon::new,
            EndGatewayIcon::new,
            OWNetherIcon::new,
            NEOverworldIcon::new,
            EndCityIcon::new,
            IglooIcon::new,
            VillageIcon::new,
            StrongholdIcon::new
        );
    }

    public MapContext getContext() {
        return this.context;
    }

    public IconManager clear() {
        return this.override(c -> NullIcon.INSTANCE);
    }

    public AbstractIconRenderer getFor(Feature<?, ?> feature) {
        return this.renderers.get(feature);
    }

    public AbstractIconRenderer getFor(Class<? extends Feature<?, ?>> feature) {
        return this.getFor(this.context.getSettings().getFeatureOfType(feature));
    }

    @SafeVarargs
    public final IconManager override(Function<MapContext, AbstractIconRenderer>... renderers) {
        for (Function<MapContext, AbstractIconRenderer> factory : renderers) {
            AbstractIconRenderer renderer = factory.apply(this.getContext());
            for (Feature<?, ?> feature : new ArrayList<>(this.renderers.keySet())) {
                if (!renderer.isValidFeature(feature)) continue;
                this.renderers.replace(feature, renderer);
            }
        }
        return this;
    }

    public List<BPos> getPositions(Feature<?, ?> feature, Fragment fragment) {
        List<BPos> positions = new ArrayList<>();
        this.renderers.get(feature).addPositions(feature, fragment, positions);
        return positions;
    }

    public void render(Graphics graphics, DrawInfo info, Feature<?, ?> feature, Fragment fragment, BPos pos, boolean hovered) {
        this.renderers.get(feature).render(graphics, info, feature, fragment, pos, hovered);
    }

    public Comparator<Feature<?, ?>> getZValueSorter() {
        return Comparator.comparing(feature -> this.renderers.get(feature).getZValue());
    }

}
