package ph.phstorage.screen.handler;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.registry.Registry;
import ph.phstorage.block.entity.BlockEntityTypesRegistry;

public final class ScreenHandlerTypesRegistry {
	public static final ScreenHandlerType<HugeChestConstructorScreenHandler> HUGE_CHEST_CONSTRUCTOR = ScreenHandlerRegistry.registerExtended(Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityTypesRegistry.HUGE_CHEST_CONSTRUCTOR), HugeChestConstructorScreenHandler::new);
	public static final ScreenHandlerType<HugeChestCoreScreenHandler> HUGE_CHEST_CORE = ScreenHandlerRegistry.registerExtended(Registry.BLOCK_ENTITY_TYPE.getId(BlockEntityTypesRegistry.HUGE_CHEST_CORE), HugeChestCoreScreenHandler::new);
	private ScreenHandlerTypesRegistry() {
	}
	
	static <T extends ScreenHandler> ScreenHandlerType<T> register(Block block, ScreenHandlerRegistry.SimpleClientHandlerFactory<T> factory) {
		return ScreenHandlerRegistry.registerSimple(Registry.BLOCK.getId(block), factory);
	}
	
	static <T extends ScreenHandler> ScreenHandlerType<T> register(Item item, ScreenHandlerRegistry.SimpleClientHandlerFactory<T> factory) {
		return ScreenHandlerRegistry.registerSimple(Registry.ITEM.getId(item), factory);
	}
	
	static <T extends ScreenHandler> ScreenHandlerType<T> register(BlockEntityType<?> blockEntityType, ScreenHandlerRegistry.SimpleClientHandlerFactory<T> factory) {
		return ScreenHandlerRegistry.registerSimple(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType), factory);
	}
}
