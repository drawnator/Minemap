package com.seedfinding.minemap.ui.map.icon;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.structure.Mineshaft;
import com.seedfinding.minemap.ui.map.MapContext;
import com.seedfinding.minemap.ui.map.fragment.Fragment;

import java.util.List;

public class MineshaftIcon extends AbstractStaticIcon {

    public MineshaftIcon(MapContext context) {
        super(context);
    }

    @Override
    public boolean isValidFeature(Feature<?, ?> feature) {
        return feature instanceof Mineshaft;
    }

    @Override
    public void addPositions(Feature<?, ?> feature, Fragment fragment, List<BPos> positions) {
        ChunkRand rand = new ChunkRand();

        for (int x = fragment.getX() - 16; x < fragment.getX() + fragment.getSize() + 16; x += 16) {
            for (int z = fragment.getZ() - 16; z < fragment.getZ() + fragment.getSize() + 16; z += 16) {
                Feature.Data<Mineshaft> data = ((Mineshaft) feature).at(x >> 4, z >> 4);
                if (!data.testStart(this.getContext().worldSeed, rand)) continue;
                if (!data.testBiome(this.getContext().getBiomeSource())) continue;
                positions.add(new BPos((data.chunkX << 4) + 9, 0, (data.chunkZ << 4) + 9));
            }
        }
    }

}
