package ph.phstorage.block.entity.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import ph.phstorage.block.entity.ChestCoreBlockEntity;

@Environment(EnvType.CLIENT)
public class HugeChestBlockEntityRenderer extends BlockEntityRenderer<ChestCoreBlockEntity> {
	public HugeChestBlockEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public void render(ChestCoreBlockEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, int overlay) {
		matrices.push();
		matrices.scale(1 + blockEntity.getExtension(Direction.EAST) + blockEntity.getExtension(Direction.WEST), 1 + blockEntity.getExtension(Direction.UP) + blockEntity.getExtension(Direction.DOWN), 1 + blockEntity.getExtension(Direction.SOUTH) + blockEntity.getExtension(Direction.NORTH));
		float scale = 1.15f;
		matrices.scale(scale, scale, scale);
		matrices.translate(.5,.5,.5);
//		matrices.translate(0.5 + (blockEntity.getExtension(Direction.EAST) - blockEntity.getExtension(Direction.WEST)) / 2d, 0.5  + (blockEntity.getExtension(Direction.UP) - blockEntity.getExtension(Direction.DOWN)) / 2d, 0.6 + (blockEntity.getExtension(Direction.SOUTH) - blockEntity.getExtension(Direction.NORTH)) / 2d);
		int lightAbove = WorldRenderer.getLightmapCoordinates(blockEntity.getWorld(), blockEntity.getPos().up());
//		MinecraftClient.getInstance().getItemRenderer().renderItem(Items.CHEST.getDefaultStack(), ModelTransformation.Mode.HEAD, 15 << 20, overlay, matrices, vertexConsumerProvider);
		matrices.pop();
	}
	
	@Override
	public boolean rendersOutsideBoundingBox(ChestCoreBlockEntity blockEntity) {
		return true;
	}
}
