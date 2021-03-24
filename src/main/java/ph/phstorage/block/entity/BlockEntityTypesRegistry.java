package ph.phstorage.block.entity;

import com.mojang.datafixers.types.Type;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import ph.phstorage.Initializer;
import ph.phstorage.block.BlocksRegistry;

import java.util.function.Supplier;

public final class BlockEntityTypesRegistry {
	public static final BlockEntityType<HugeChestCoreBlockEntity> HUGE_CHEST_CORE = register(HugeChestCoreBlockEntity::new, BlocksRegistry.HUGE_CHEST_CORE);
	public static final BlockEntityType<HugeChestWallBlockEntity> HUGE_CHEST_WALL = register(HugeChestWallBlockEntity::new, BlocksRegistry.HUGE_CHEST_WALL);
	public static final BlockEntityType<HugeChestConstructorBlockEntity> HUGE_CHEST_CONSTRUCTOR = register(HugeChestConstructorBlockEntity::new, BlocksRegistry.HUGE_CHEST_CONSTRUCTOR);
	
	@Deprecated
	static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType.Builder<T> builder) {
		String id = Initializer.NAMESPACE + ":" + path;
		Type<?> type = Util.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, builder.build(type));
	}
	
	@Deprecated
	static <T extends BlockEntity> BlockEntityType<T> register(String path, Supplier<T> supplier, Block... blocks) {
		return register(path, BlockEntityType.Builder.create(supplier, blocks));
	}
	
	static <T extends BlockEntity> BlockEntityType<T> register(Supplier<T> supplier, Block block) {
		String id = Registry.BLOCK.getId(block).toString();
//		Type<?> type = CodeUtil.getChoiceType(TypeReferences.BLOCK_ENTITY, id);
		return Registry.register(Registry.BLOCK_ENTITY_TYPE, id, BlockEntityType.Builder.create(supplier, block).build(null));
		//		return register(Registry.BLOCK.getId(block).getPath(), supplier, block);
	}
	
}
