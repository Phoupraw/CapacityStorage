package ph.phstorage.block;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.block.entity.ChestCoreBlockEntity;
import ph.phstorage.item.ItemsRegistry;

public class ChestCoreBlock extends BlockWithEntity implements AttributeProvider {
	public ChestCoreBlock(Settings settings) {
		super(settings);
		BlockState defaultState = getDefaultState();
		for (BooleanProperty property : ChestWallBlock. PROPERTIES.values()) {
			defaultState = defaultState.with(property, false);
		}
		setDefaultState(defaultState);
	}
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		for (BooleanProperty property : ChestWallBlock. PROPERTIES.values()) {
			builder.add(property);
		}
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new ChestCoreBlockEntity();
	}
	
	@Override
	public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
			to.offer(world.getBlockEntity(pos));
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof ChestCoreBlockEntity) {
				ChestCoreBlockEntity thisBlockEntity = (ChestCoreBlockEntity) blockEntity;
				thisBlockEntity.onDestroyed();
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ChestCoreBlockEntity) {
			ChestCoreBlockEntity thisBlockEntity = (ChestCoreBlockEntity) blockEntity;
			player.openHandledScreen(thisBlockEntity);
			return ActionResult.SUCCESS;
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return ItemsRegistry.CHEST_CONSTRUCTOR.getDefaultStack();
	}
}
