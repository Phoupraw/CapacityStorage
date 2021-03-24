package ph.phstorage.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import ph.phstorage.Initializer;
import ph.phstorage.item.ItemsRegistry;

public final class BlocksRegistry {
	public static final AbstractBlock.Settings SETTINGS = AbstractBlock.Settings.of(Material.WOOD).strength(2.5F).sounds(BlockSoundGroup.WOOD).allowsSpawning((state, world, pos, type) -> false);
	
	public static final HugeChestWallBlock HUGE_CHEST_WALL = register("huge_chest_wall", new HugeChestWallBlock(SETTINGS));
	public static final HugeChestCoreBlock HUGE_CHEST_CORE = register("huge_chest_core", new HugeChestCoreBlock(SETTINGS));
	public static final HugeChestConstructorBlock HUGE_CHEST_CONSTRUCTOR = register("huge_chest_constructor", new HugeChestConstructorBlock(SETTINGS));
	public static final Block HUGE_CHEST_LINING = register("huge_chest_lining", new Block(SETTINGS){
		@Override
		public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
			return ItemsRegistry.HUGE_CHEST_WALL.getDefaultStack();
		}
	});
	
	static <T extends Block> T register(String path, T block) {
		return Registry.register(Registry.BLOCK, new Identifier(Initializer.NAMESPACE, path), block);
	}
	
}
