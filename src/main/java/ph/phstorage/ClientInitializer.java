package ph.phstorage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.util.Identifier;
import ph.phstorage.block.entity.BlockEntityTypesRegistry;
import ph.phstorage.block.entity.render.HugeChestBlockEntityRenderer;
import ph.phstorage.block.entity.render.HugeChestModelProvider;
import ph.phstorage.screen.ChestConstructorScreen;
import ph.phstorage.screen.ChestCoreScreen;
import ph.phstorage.screen.handler.ScreenHandlerTypesRegistry;
@Environment(EnvType.CLIENT)
public final class ClientInitializer implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ScreenRegistry.register(ScreenHandlerTypesRegistry.HUGE_CHEST_CONSTRUCTOR, ChestConstructorScreen::new);
		ScreenRegistry.register(ScreenHandlerTypesRegistry.HUGE_CHEST_CORE, ChestCoreScreen::new);
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new HugeChestModelProvider());
		BlockEntityRendererRegistry.INSTANCE.register(BlockEntityTypesRegistry.HUGE_CHEST_CORE, HugeChestBlockEntityRenderer::new);
	}
	
	public static Identifier toGuiTexture(Identifier identifier) {
		return new Identifier(identifier.getNamespace(), "textures/gui/" + identifier.getPath() + ".png");
	}
}
