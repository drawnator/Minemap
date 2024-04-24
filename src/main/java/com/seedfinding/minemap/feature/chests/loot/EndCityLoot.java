package com.seedfinding.minemap.feature.chests.loot;

import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.minemap.feature.chests.AbstractLoot;

public class EndCityLoot extends AbstractLoot {

    @Override
    public boolean isCorrectInstance(Feature<?, ?> feature) {
        return feature instanceof EndCity;
    }
}
