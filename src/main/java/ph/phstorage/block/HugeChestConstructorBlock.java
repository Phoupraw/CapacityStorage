package ph.phstorage.block;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.block.entity.HugeChestConstructorBlockEntity;
import ph.phstorage.item.ItemsRegistry;

public class HugeChestConstructorBlock extends BlockWithEntity implements AttributeProvider {
	public HugeChestConstructorBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			((HugeChestConstructorBlockEntity) world.getBlockEntity(pos)).onDestroyed();
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof HugeChestConstructorBlockEntity) {
			HugeChestConstructorBlockEntity thisBlockEntity = (HugeChestConstructorBlockEntity) blockEntity;
//			ItemStack handStack = player.getStackInHand(hand);
//			if (handStack.getItem() == ItemsRegistry.HUGE_CHEST_WALL) {
//				ActionResult actionResult = thisBlockEntity.onPlaced(player);
//				if (actionResult.isAccepted())
//					handStack.decrement(1);
//				else //if (actionResult != ActionResult.SUCCESS)
//					player.openHandledScreen(thisBlockEntity);
//			} else
				player.openHandledScreen(thisBlockEntity);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new HugeChestConstructorBlockEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void addAllAttributes(World world, BlockPos blockPos, BlockState blockState, AttributeList<?> attributeList) {
		attributeList.offer(world.getBlockEntity(blockPos));
	}
}
