package ph.phstorage.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ph.phstorage.block.BlocksRegistry;

public final class ItemsRegistry {
	public static final UnplacableBlockItem HUGE_CHEST_WALL = register( Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_WALL),new HugeChestWallItem(BlocksRegistry.HUGE_CHEST_WALL,new Item.Settings().group(ItemGroup.DECORATIONS)));
	public static final BlockItem HUGE_CHEST_CONSTRUCTOR = register(BlocksRegistry.HUGE_CHEST_CONSTRUCTOR, ItemGroup.DECORATIONS);
	
	static BlockItem register(Block block, ItemGroup group) {
		return register(Registry.BLOCK.getId(block), new BlockItem(block, (new Item.Settings()).group(group)));
	}
	
	static UnplacableBlockItem registerUnplacable(Block block, ItemGroup group) {
		return register(Registry.BLOCK.getId(block), new UnplacableBlockItem(block, (new Item.Settings()).group(group)));
	}
	
	static <T extends Item> T register(Identifier id, T item) {
		if (item instanceof BlockItem) {
			((BlockItem) item).appendBlocks(Item.BLOCK_ITEMS, item);
		}
		return Registry.register(Registry.ITEM, id, item);
	}

}
