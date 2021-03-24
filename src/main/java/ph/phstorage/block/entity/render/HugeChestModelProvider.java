package ph.phstorage.block.entity.render;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;
import ph.phstorage.Initializer;

public class HugeChestModelProvider implements ModelResourceProvider {
	public static final Identifier HUGE_CHEST = new Identifier(Initializer.NAMESPACE, "block/huge_chest");
	
	@Override
	public UnbakedModel loadModelResource(Identifier identifier, ModelProviderContext modelProviderContext) {
		if (identifier.equals(HUGE_CHEST)) {
			return new HugeChestModel();
		} else {
			return null;
		}
	}
}
