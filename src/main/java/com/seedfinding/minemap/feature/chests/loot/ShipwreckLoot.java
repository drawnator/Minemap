package com.seedfinding.minemap.feature.chests.loot;

import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.Shipwreck;
import com.seedfinding.minemap.feature.chests.AbstractLoot;

public class ShipwreckLoot extends AbstractLoot {


    @Override
    public boolean isCorrectInstance(Feature<?, ?> feature) {
        return feature instanceof Shipwreck;
    }
}
