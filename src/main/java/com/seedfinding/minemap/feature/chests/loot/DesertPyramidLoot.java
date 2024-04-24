package com.seedfinding.minemap.feature.chests.loot;

import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.minemap.feature.chests.AbstractLoot;

public class DesertPyramidLoot extends AbstractLoot {

    @Override
    public boolean isCorrectInstance(Feature<?, ?> feature) {
        return feature instanceof DesertPyramid;
    }
}
