package ph.phstorage.block;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.block.entity.ChestWallBlockEntity;
import ph.phstorage.block.entity.ChestCoreBlockEntity;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestWallBlock extends BlockWithEntity implements AttributeProvider {
	public static final ImmutableMap<Direction, BooleanProperty> PROPERTIES = ImmutableMap.copyOf(Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), d -> BooleanProperty.of(d.getName()))));
	
	public ChestWallBlock(Settings settings) {
		super(settings);
		BlockState defaultState = getDefaultState();
		for (BooleanProperty property : PROPERTIES.values()) {
			defaultState = defaultState.with(property, false);
		}
		setDefaultState(defaultState);
	}
	
	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		for (BooleanProperty property : PROPERTIES.values()) {
			builder.add(property);
		}
	}
	@Override
	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity0 = world.getBlockEntity(pos);
			if (blockEntity0 instanceof ChestWallBlockEntity) {
				ChestWallBlockEntity thisBlockEntity = (ChestWallBlockEntity) blockEntity0;
				ChestCoreBlockEntity coreBlockEntity = thisBlockEntity.getCoreBlockEntity();
				if (coreBlockEntity != null) {
					coreBlockEntity.onWallBroken();
				}
			}
		}
		super.onStateReplaced(state, world, pos, newState, moved);
	}
	
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity blockEntity0 = world.getBlockEntity(pos);
		if (blockEntity0 instanceof ChestWallBlockEntity) {
			BlockPos corePos = ((ChestWallBlockEntity) blockEntity0).getCorePos();
			if (corePos != null) {
				return BlocksRegistry.CHEST_CORE.onUse(world.getBlockState(corePos), world, corePos, player, hand, hit);
			}
		}
		return super.onUse(state, world, pos, player, hand, hit);
	}
	
	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new ChestWallBlockEntity();
	}
	
	@Override
	public void addAllAttributes(World world, BlockPos blockPos, BlockState blockState, AttributeList<?> attributeList) {
		BlockEntity blockEntity0 = world.getBlockEntity(blockPos);
		if (blockEntity0 instanceof ChestWallBlockEntity) {
			ChestCoreBlockEntity coreBlockEntity = ((ChestWallBlockEntity) blockEntity0).getCoreBlockEntity();
			if (coreBlockEntity != null)
				attributeList.offer(coreBlockEntity);
		}
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Nullable
	public ChestCoreBlockEntity getCoreBlockEntity(World world, BlockPos pos) {
		BlockEntity blockEntity0 = world.getBlockEntity(pos);
		if (blockEntity0 instanceof ChestWallBlockEntity) {
			return ((ChestWallBlockEntity) blockEntity0).getCoreBlockEntity();
		}
		return null;
	}
}
