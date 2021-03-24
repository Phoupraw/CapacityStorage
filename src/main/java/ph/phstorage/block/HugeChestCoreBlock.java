package ph.phstorage.block;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.block.entity.HugeChestCoreBlockEntity;
import ph.phstorage.item.ItemsRegistry;

public class HugeChestCoreBlock extends BlockWithEntity implements AttributeProvider {
	public HugeChestCoreBlock(Settings settings) {
		super(settings);
		BlockState defaultState = getDefaultState();
		for (BooleanProperty property :HugeChestWallBlock. PROPERTIES.values()) {
			defaultState = defaultState.with(property, false);
		}
		setDefaultState(defaultState);
	}
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		System.out.println(HugeChestWallBlock. PROPERTIES);
		for (BooleanProperty property : HugeChestWallBlock. PROPERTIES.values()) {
			builder.add(property);
		}
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new HugeChestCoreBlockEntity();
	}
	
	@Override
	public void addAllAttributes(World world, BlockPos pos, BlockState state, AttributeList<?> to) {
			to.offer(world.getBlockEntity(pos));
	}
	
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof HugeChestCoreBlockEntity) {
				HugeChestCoreBlockEntity thisBlockEntity = (HugeChestCoreBlockEntity) blockEntity;
				thisBlockEntity.onDestroyed();
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof HugeChestCoreBlockEntity) {
			HugeChestCoreBlockEntity thisBlockEntity = (HugeChestCoreBlockEntity) blockEntity;
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
		return ItemsRegistry.HUGE_CHEST_CONSTRUCTOR.getDefaultStack();
	}
}
