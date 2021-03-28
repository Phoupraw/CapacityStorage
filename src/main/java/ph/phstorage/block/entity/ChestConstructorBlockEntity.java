package ph.phstorage.block.entity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.ItemInsertable;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.ChestWallBlock;
import ph.phstorage.item.ItemsRegistry;
import ph.phstorage.screen.handler.ChestConstructorScreenHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestConstructorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, BlockEntityClientSerializable, ItemInsertable {
	private Map<Direction, Integer> extensions = Maps.newEnumMap(Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), d -> 0)));
	private CompoundTag coreBlockEntityTag;
	
	public ChestConstructorBlockEntity() {
		super(BlockEntityTypesRegistry.HUGE_CHEST_CONSTRUCTOR);
	}
	
	@Override
	public Text getDisplayName() {
		return new TranslatableText(BlocksRegistry.CHEST_CONSTRUCTOR.getTranslationKey());
	}
	
	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ChestConstructorScreenHandler(syncId, inv, this);
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (Map.Entry<Direction, Integer> entry : extensions.entrySet()) {
			tag.putInt(entry.getKey().getName(), entry.getValue());
		}
		if (coreBlockEntityTag != null)
			tag.put("coreBlockEntityTag", coreBlockEntityTag);
		return super.toTag(tag);
	}
	
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		for (Direction direction : Direction.values()) {
			extensions.put(direction, tag.getInt(direction.getName()));
		}
		if (tag.contains("coreBlockEntityTag", 10))
			coreBlockEntityTag = tag.getCompound("coreBlockEntityTag");
		else
			coreBlockEntityTag = null;
	}
	
	public void setExtension(Direction direction, int extension) {
		if (extension >= 0) {
			extensions.put(direction, extension);
			if (world != null && !world.isClient())
				((ServerWorld) world).getChunkManager().markForUpdate(getPos());
		}
	}
	
	public int getExtension(Direction direction) {
		return extensions.getOrDefault(direction, 0);
	}
	
	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		fromTag(getCachedState(), compoundTag);
	}
	
	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return toTag(compoundTag);
	}
	
	@Override
	public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
		for (Direction direction : Direction.values()) {
			packetByteBuf.writeInt(extensions.get(direction));
		}
	}
	
	/**
	 * @param player 放置的玩家
	 *
	 * @return 0b1表示放置了；0b10表示箱子是完整的
	 */
	public int onPlaced(@Nullable PlayerEntity player) {
		int e = extensions.get(Direction.EAST), w = -extensions.get(Direction.WEST), u = extensions.get(Direction.UP), d = -extensions.get(Direction.DOWN), s = extensions.get(Direction.SOUTH), n = -extensions.get(Direction.NORTH);
		boolean entire = true;
		boolean placed = false;
		for1:
		for (int i = w; i <= e; i++) {
			for (int j = d; j <= u; j++) {
				for (int k = n; k <= s; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					BlockPos pos = getPos().add(i, j, k);
					BlockState state = Objects.requireNonNull(world,"world").getBlockState(pos);
					if (i > w && i < e && j > d && j < u && k > n && k < s) {
						if (state.isOf(BlocksRegistry.CHEST_LINING)) {
							continue;
						} else {
							if (placed) {
								entire = false;
								break for1;
							}
							if (state.isAir() || state.canReplace(new ItemPlacementContext(world, player, Hand.MAIN_HAND, ItemsRegistry.CHEST_WALL.getDefaultStack(), new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, true)) {
							})) {
								world.setBlockState(pos, BlocksRegistry.CHEST_LINING.getDefaultState());
								placed = true;
							}
						}
						continue;
					}
					BlockEntity blockEntity0 = world.getBlockEntity(pos);
					boolean placable = state.isAir() && !placed;
					if (blockEntity0 instanceof ChestWallBlockEntity) {
						ChestWallBlockEntity wallBlockEntity = (ChestWallBlockEntity) blockEntity0;
						ChestCoreBlockEntity coreBlockEntity = wallBlockEntity.getCoreBlockEntity();
						if (coreBlockEntity == null) {
							wallBlockEntity.setCorePos(getPos());
						} else {
							if (!getPos().equals(coreBlockEntity.getPos())) {
								entire = false;
							}
						}
					}
					if (placable) {
						world.setBlockState(pos, BlocksRegistry.CHEST_WALL.getDefaultState());
						BlockEntity blockEntity1 = world.getBlockEntity(pos);
						if (blockEntity1 instanceof ChestWallBlockEntity) {
							ChestWallBlockEntity wallBlockEntity = (ChestWallBlockEntity) blockEntity1;
							wallBlockEntity.setCorePos(getPos());
						}
						placed = true;
					} else if (!state.isOf(BlocksRegistry.CHEST_WALL)) {
						entire = false;
					}
				}
			}
		}
		if (entire) {
			placeEntireChest();
			return placed ? 0b11 : 0b10;
		}
		return placed ? 0b01 : 0b00;
	}
	
	private void placeEntireChest() {
		int e = extensions.get(Direction.EAST), w = -extensions.get(Direction.WEST), u = extensions.get(Direction.UP), d = -extensions.get(Direction.DOWN), s = extensions.get(Direction.SOUTH), n = -extensions.get(Direction.NORTH);
		//放置外壁和内衬
		for (int i = w; i <= e; i++) {
			for (int j = d; j <= u; j++) {
				for (int k = n; k <= s; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					BlockPos pos1 = getPos().add(i, j, k);
					if ((i == w || i == e) || (j == d || j == u) || (k == n || k == s)) {
						Objects.requireNonNull(world,"world").setBlockState(pos1, BlocksRegistry.CHEST_WALL.getDefaultState().with(ChestWallBlock.PROPERTIES.get(Direction.EAST), i != e).with(ChestWallBlock.PROPERTIES.get(Direction.WEST), i != w).with(ChestWallBlock.PROPERTIES.get(Direction.UP), j != u).with(ChestWallBlock.PROPERTIES.get(Direction.DOWN), j != d).with(ChestWallBlock.PROPERTIES.get(Direction.SOUTH), k != s).with(ChestWallBlock.PROPERTIES.get(Direction.NORTH), k != n));
						BlockEntity blockEntity0 = world.getBlockEntity(pos1);
						if (blockEntity0 instanceof ChestWallBlockEntity) {
							ChestWallBlockEntity wallBlockEntity = (ChestWallBlockEntity) blockEntity0;
							wallBlockEntity.setCorePos(getPos());
						}
					} else {
						world.setBlockState(pos1, BlocksRegistry.CHEST_LINING.getDefaultState());
					}
				}
			}
		}
		//放置核心
		CompoundTag coreBlockEntityTag = this.coreBlockEntityTag;
		this.coreBlockEntityTag = null;
		Objects.requireNonNull(world,"world").setBlockState(getPos(), BlocksRegistry.CHEST_CORE.getDefaultState().with(ChestWallBlock.PROPERTIES.get(Direction.EAST), 0 != e).with(ChestWallBlock.PROPERTIES.get(Direction.WEST), 0 != w).with(ChestWallBlock.PROPERTIES.get(Direction.UP), 0 != u).with(ChestWallBlock.PROPERTIES.get(Direction.DOWN), 0 != d).with(ChestWallBlock.PROPERTIES.get(Direction.SOUTH), 0 != s).with(ChestWallBlock.PROPERTIES.get(Direction.NORTH), 0 != n));
		BlockEntity blockEntity0 = world.getBlockEntity(getPos());
		if (blockEntity0 instanceof ChestCoreBlockEntity) {
			ChestCoreBlockEntity core = (ChestCoreBlockEntity) blockEntity0;
			final int stackCapacity = 27 * (e - w + 1) * (u - d + 1) * (s - n + 1);
			if (coreBlockEntityTag != null) {
				core.fromTag(core.getCachedState(), coreBlockEntityTag);
				core.setStackSpace(core.getStackSpace() + (stackCapacity - core.getStackCapacity()));
			} else {
				core.setStackSpace(stackCapacity);
			}
			core.setStackCapacity(stackCapacity);
			core.setExtensions(extensions);
		}
	}
	
	 void setExtensions(Map<Direction, Integer> extensions) {
		this.extensions = extensions;
	}
	
	 void setCoreBlockEntityTag(CompoundTag coreBlockEntityTag) {
		this.coreBlockEntityTag = coreBlockEntityTag;
	}
	
	public void onDestroyed() {
		if (coreBlockEntityTag != null) {
			ChestCoreBlockEntity coreBlockEntity = new ChestCoreBlockEntity();
			coreBlockEntity.setLocation(world, pos);
			coreBlockEntity.fromTag(coreBlockEntity.getCachedState(), coreBlockEntityTag);
			coreBlockEntity.onDestroyed();
		}
	}
	
	@Override
	public ItemStack attemptInsertion(ItemStack itemStack, Simulation simulation) {
		itemStack = itemStack.copy();
		if (itemStack.getItem() == ItemsRegistry.CHEST_WALL) {
			if (simulation.isAction()) {
				while (!itemStack.isEmpty() && (onPlaced(null) & 0b1) == 1)
					itemStack.decrement(1);
				return itemStack;
			}
			return ItemStack.EMPTY;
		}
		return itemStack;
	}
}
