package com.seedfinding.minemap.ui.map.icon;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.misc.SlimeChunk;
import com.seedfinding.minemap.ui.map.MapContext;
import com.seedfinding.minemap.ui.map.fragment.Fragment;

import java.util.List;

public class SlimeIcon extends AbstractDynamicIcon {

    public SlimeIcon(MapContext context) {
        super(context, 16);
    }

    @Override
    public float getZValue() {
        return -1.0F;
    }

    @Override
    public boolean isValidFeature(Feature<?, ?> feature) {
        return feature instanceof SlimeChunk;
    }

    @Override
    public void addPositions(Feature<?, ?> feature, Fragment fragment, List<BPos> positions) {
        ChunkRand rand = new ChunkRand();

        for (int x = fragment.getX() - 16; x < fragment.getX() + fragment.getSize() + 16; x += 16) {
            for (int z = fragment.getZ() - 16; z < fragment.getZ() + fragment.getSize() + 16; z += 16) {
                SlimeChunk.Data data = ((SlimeChunk) feature).at(x >> 4, z >> 4, true);
                if (!data.testStart(fragment.getContext().worldSeed, rand)) continue;
                positions.add(new BPos(x, 0, z).toChunkCorner());
            }
        }
    }

    @Override
    public boolean isHovered(Fragment fragment, BPos hoveredPos, BPos featurePos, int width, int height, Feature<?, ?> feature) {
        return false;
    }

}
