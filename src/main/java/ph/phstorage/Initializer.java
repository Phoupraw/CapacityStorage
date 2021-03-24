package ph.phstorage;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.BlockEntityTypesRegistry;
import ph.phstorage.item.ItemsRegistry;
import ph.phstorage.screen.handler.HugeChestConstructorScreenHandler;
import ph.phstorage.screen.handler.HugeChestCoreScreenHandler;

public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "phstorage";
	
	@Override
	public void onInitialize() {
		try {
			Class.forName(ItemsRegistry.class.getName());
			Class.forName(BlocksRegistry.class.getName());
			Class.forName(BlockEntityTypesRegistry.class.getName());
			Class.forName(HugeChestConstructorScreenHandler.class.getName());
			Class.forName(HugeChestCoreScreenHandler.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static <T> Identifier wrap(Registry<T> registry, T entry) {
		Identifier identifier = registry.getId(entry);
		identifier = new Identifier(identifier.getNamespace(), identifier.getPath() +"_"+ registry.getKey().getValue().getPath());
		System.out.println(identifier);
		return identifier;
	}
}
