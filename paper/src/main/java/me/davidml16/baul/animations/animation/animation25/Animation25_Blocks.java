package me.davidml16.baul.animations.animation.animation25;

import com.cryptomorin.xseries.XMaterial;
import me.davidml16.baul.animations.AnimationBlocks;
import me.davidml16.baul.animations.FakeBlock;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class Animation25_Blocks extends AnimationBlocks {

    public Animation25_Blocks(Location location) {

        super(location);

        setStepFakeBlocks(1, new FakeBlock[]{
            new FakeBlock(getLocation(0), XMaterial.SOUL_SOIL),
            new FakeBlock(getLocation(1), XMaterial.SOUL_SOIL),
            new FakeBlock(getLocation(2), XMaterial.SOUL_SOIL),
            new FakeBlock(getLocation(3), XMaterial.SOUL_SOIL),
            new FakeBlock(getLocation(4), XMaterial.SOUL_SOIL)
        });

        setStepFakeBlocks(2, new FakeBlock[]{
            new FakeBlock(getLocation(5), XMaterial.ANCIENT_DEBRIS),
            new FakeBlock(getLocation(6), XMaterial.ANCIENT_DEBRIS),
            new FakeBlock(getLocation(7), XMaterial.ANCIENT_DEBRIS),
            new FakeBlock(getLocation(8), XMaterial.ANCIENT_DEBRIS)
        });

        setStepFakeBlocks(3, new FakeBlock[]{
            new FakeBlock(getLocation(9), XMaterial.DARK_OAK_STAIRS, BlockFace.WEST),
            new FakeBlock(getLocation(10), XMaterial.DARK_OAK_STAIRS, BlockFace.EAST),
            new FakeBlock(getLocation(11), XMaterial.DARK_OAK_STAIRS, BlockFace.NORTH),
            new FakeBlock(getLocation(12), XMaterial.DARK_OAK_STAIRS, BlockFace.SOUTH)
        });

        setStepFakeBlocks(4, new FakeBlock[]{
            new FakeBlock(getLocation(13), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.WEST),
            new FakeBlock(getLocation(14), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.EAST),
            new FakeBlock(getLocation(15), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.NORTH),
            new FakeBlock(getLocation(16), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.SOUTH),
            new FakeBlock(getLocation(17), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.WEST),
            new FakeBlock(getLocation(18), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.EAST),
            new FakeBlock(getLocation(19), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.NORTH),
            new FakeBlock(getLocation(20), XMaterial.POLISHED_BLACKSTONE_STAIRS, BlockFace.SOUTH)
        });

        setStepFakeBlocks(5, new FakeBlock[]{
            new FakeBlock(getLocation(21), XMaterial.CRYING_OBSIDIAN),
            new FakeBlock(getLocation(22), XMaterial.CRYING_OBSIDIAN),
            new FakeBlock(getLocation(23), XMaterial.CRYING_OBSIDIAN),
            new FakeBlock(getLocation(24), XMaterial.CRYING_OBSIDIAN)
        });

        setStepFakeBlocks(6, new FakeBlock[]{
            new FakeBlock(getLocation(25), XMaterial.SOUL_CAMPFIRE),
            new FakeBlock(getLocation(26), XMaterial.SOUL_CAMPFIRE),
            new FakeBlock(getLocation(27), XMaterial.SOUL_CAMPFIRE),
            new FakeBlock(getLocation(28), XMaterial.SOUL_CAMPFIRE)
        });

    }

}
