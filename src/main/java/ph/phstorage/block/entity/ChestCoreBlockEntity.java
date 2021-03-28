package ph.phstorage.block.entity;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.GroupedItemInv;
import alexiil.mc.lib.attributes.item.filter.ItemFilter;
import com.google.common.collect.*;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.api.CodeUtil;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.screen.handler.ChestCoreScreenHandler;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChestCoreBlockEntity extends BlockEntity implements GroupedItemInv, ExtendedScreenHandlerFactory, BlockEntityClientSerializable {
	public static final double SINGLE_CAPACITY = 27;
	public static final Comparator<ItemStack> DEFAULT_COMPARATOR = (a, b) -> {
		if (a.getItem() != b.getItem())
			return Integer.compare(Item.getRawId(a.getItem()), Item.getRawId(b.getItem()));
		if (a.getTag() == null || b.getTag() == null)
			return Boolean.compare(a.hasTag(), b.hasTag());
		if (!a.hasEnchantments() || !b.hasEnchantments())
			return Boolean.compare(a.hasEnchantments(), b.hasEnchantments());
		if (!a.hasCustomName() || !b.hasCustomName())
			return Boolean.compare(a.hasCustomName(), b.hasCustomName());
		return a.getTag().toString().compareTo(b.getTag().toString());
	};
	public static final Comparator<ItemStack> COUNT_COMPARATOR = (a, b) -> {
		if (a.getCount() != b.getCount())
			return Integer.compare(a.getCount(), b.getCount());
		return DEFAULT_COMPARATOR.compare(a, b);
	};
	public static final Comparator<ItemStack> ID_COMPARATOR = (a, b) -> {
		if (a.getItem() != b.getItem())
			return Registry.ITEM.getId(a.getItem()).compareTo(Registry.ITEM.getId(b.getItem()));
		return DEFAULT_COMPARATOR.compare(a, b);
	};
	public static final CompoundTag NULL = new CompoundTag();
	private Map<Direction, Integer> extensions = Maps.newEnumMap(Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), d -> 0)));
	private double stackCapacity = SINGLE_CAPACITY;
	private double stackSpace = getStackCapacity();
	private Table<Item, CompoundTag, ItemStack> tabledItems = HashBasedTable.create();
	
	public ChestCoreBlockEntity() {
		super(BlockEntityTypesRegistry.HUGE_CHEST_CORE);
	}
	
	@Override
	public Set<ItemStack> getStoredStacks() {
		return Sets.newHashSet(tabledItems.values());
	}
	
	@Override
	public int getTotalCapacity() {
		return (int) (getStackCapacity() * 64);
	}
	
	@Override
	public ItemInvStatistic getStatistics(ItemFilter filter) {
		int amount = 0;
		int maxMaxCount = 1;
		for (ItemStack stack : tabledItems.values()) {
			if (filter.matches(stack)) {
				amount += stack.getCount();
				maxMaxCount = Math.max(maxMaxCount, stack.getMaxCount());
			}
		}
		return new ItemInvStatistic(filter, amount, (int) (getStackSpace() * maxMaxCount), (int) (getStackSpace() * maxMaxCount) + amount);
	}
	
	@Override
	public ItemStack attemptExtraction(ItemFilter filter, int count, Simulation simulation) {
		if (world != null && !world.isClient)
			sync();
		for (Iterator<Table.Cell<Item, CompoundTag, ItemStack>> iterator = tabledItems.cellSet().iterator(); iterator.hasNext(); ) {
			Table.Cell<Item, CompoundTag, ItemStack> cell = iterator.next();
			if (filter.matches(cell.getValue())) {
				if (count >= Objects.requireNonNull(cell.getValue(), "cell.getValue()").getCount()) {
					if (simulation.isAction()) {
						iterator.remove();
						addSpace(cell.getValue());
					}
					return cell.getValue();
				} else {
					ItemStack stack = cell.getValue().copy();
					stack.setCount(count);
					if (simulation.isAction()) {
						cell.getValue().decrement(count);
						addSpace(stack);
					}
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack attemptInsertion(ItemStack stack, Simulation simulation) {
		if (!stack.isEmpty()) {
			stack = stack.copy();
			int countLimited = Math.min(stack.getCount(), (int) (getStackSpace() * stack.getMaxCount()));
			if (countLimited > 0) {
				if (world != null && !world.isClient)
					sync();
				ItemStack stack1 = stack.split(countLimited);
				if (simulation.isAction()) {
					if (tabledItems.contains(stack1.getItem(), nullable(stack1.getTag())))
						stack1.increment(tabledItems.get(stack1.getItem(), nullable(stack1.getTag())).getCount());
					tabledItems.put(stack1.getItem(), nullable(stack1.getTag()), stack1);
					addSpace(stack1, -countLimited);
				}
			}
		}
		return stack;
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		for (Map.Entry<Direction, Integer> entry : extensions.entrySet()) {
			tag.putInt(entry.getKey().getName(), entry.getValue());
		}
		tag.putDouble("stackSpace", stackSpace);
		ListTag listTag = new ListTag();
		for (ItemStack stack : tabledItems.values()) {
			listTag.add(tagFromStack(stack));
		}
		tag.put("Items", listTag);
		return super.toTag(tag);
	}
	
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		for (Direction direction : Direction.values()) {
			extensions.put(direction, tag.getInt(direction.getName()));
		}
		stackCapacity = SINGLE_CAPACITY * (extensions.get(Direction.EAST) + extensions.get(Direction.WEST) + 1) * (extensions.get(Direction.UP) + extensions.get(Direction.DOWN) + 1) * (extensions.get(Direction.SOUTH) + extensions.get(Direction.NORTH) + 1);
		stackSpace = tag.getDouble("stackSpace");
		tabledItems = HashBasedTable.create();
		for (Tag tag0 : tag.getList("Items", 10)) {
			CompoundTag stackTag = (CompoundTag) tag0;
			ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(stackTag.getString("id"))), stackTag.getInt("Count"));
			if (stackTag.contains("tag", 10))
				stack.setTag(stackTag.getCompound("tag"));
			if (!stack.isEmpty())
				tabledItems.put(stack.getItem(), nullable(stack.getTag()), stack);
			
		}
	}
	
	@Override
	public ItemStack extract(ItemStack stack, int maxAmount) {
		if (world != null && !world.isClient)
			sync();
		stack = tabledItems.get(stack.getItem(), nullable(stack.getTag()));
		if (stack == null)
			return ItemStack.EMPTY;
		ItemStack stack1 = stack.split(maxAmount);
		if (stack.isEmpty()) {
			tabledItems.remove(stack1.getItem(), nullable(stack1.getTag()));
		}
		addSpace(stack1);
		return stack1;
	}
	
	@Override
	public ItemStack attemptAnyExtraction(int maxAmount, Simulation simulation) {
		return GroupedItemInv.super.attemptAnyExtraction(maxAmount, simulation);
	}
	
	private void clear() {
		tabledItems = HashBasedTable.create();
		stackSpace = getStackCapacity();
	}
	
	@Override
	public Text getDisplayName() {
		return new TranslatableText(BlocksRegistry.CHEST_CORE.getTranslationKey());
	}
	
	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ChestCoreScreenHandler(syncId, inv, this);
	}
	
	void setExtensions(Map<Direction, Integer> extensions) {
		this.extensions = extensions;
	}
	
	void setStackCapacity(double stackCapacity) {
		this.stackCapacity = stackCapacity;
	}
	
	void setStackSpace(double stackSpace) {
		this.stackSpace = stackSpace;
	}
	
	public double getStackCapacity() {
		return stackCapacity;
	}
	
	public double getStackSpace() {
		return stackSpace;
	}
	
	private List<ItemStack> clearToList() {
		List<ItemStack> list = Lists.newArrayList();
		for (ItemStack stack : tabledItems.values()) {
			while (!stack.isEmpty()) {
				list.add(stack.split(stack.getMaxCount()));
			}
		}
		clear();
		return list;
	}
	
	public void onWallBroken() {
		//把外壁的方块状态设为默认
		int e = extensions.get(Direction.EAST), w = -extensions.get(Direction.WEST), u = extensions.get(Direction.UP), d = -extensions.get(Direction.DOWN), s = extensions.get(Direction.SOUTH), n = -extensions.get(Direction.NORTH);
		for (int i = w; i <= e; i++) {
			for (int j = d; j <= u; j++) {
				for (int k = n; k <= s; k++) {
					if (i == 0 && j == 0 && k == 0)
						continue;
					if ((i == w || i == e) || (j == d || j == u) || (k == n || k == s)) {
						BlockPos pos1 = getPos().add(i, j, k);
						if (Objects.requireNonNull(world, "world").getBlockState(pos1).isOf(BlocksRegistry.CHEST_WALL)) {
							BlockEntity blockEntity0 = world.getBlockEntity(pos1);
							if (blockEntity0 instanceof ChestWallBlockEntity) {
								ChestWallBlockEntity wallBlockEntity = (ChestWallBlockEntity) blockEntity0;
								if (getPos().equals(wallBlockEntity.getCorePos())) {
									world.setBlockState(pos1, BlocksRegistry.CHEST_WALL.getDefaultState());
								}
							}
						}
					}
				}
			}
		}
		//把自身变成构造机
		CompoundTag coreBlockEntityTag = toTag(new CompoundTag());
		clear();
		Objects.requireNonNull(world, "world").setBlockState(getPos(), BlocksRegistry.CHEST_CONSTRUCTOR.getDefaultState());
		BlockEntity blockEntity0 = world.getBlockEntity(getPos());
		if (blockEntity0 instanceof ChestConstructorBlockEntity) {
			ChestConstructorBlockEntity constructor = (ChestConstructorBlockEntity) blockEntity0;
			constructor.setExtensions(extensions);
			constructor.setCoreBlockEntityTag(coreBlockEntityTag);
		}
	}
	
	public Iterable<ItemStack> getStoredStacks(Comparator<ItemStack> comparator) {
		List<ItemStack> list = new ArrayList<>(tabledItems.values());
		list.sort(comparator);
		return list;
	}
	
	@Deprecated
	public static ItemStack of(Map.Entry<Pair<Item, CompoundTag>, Integer> entry) {
		ItemStack stack = new ItemStack(entry.getKey().getFirst(), entry.getValue());
		stack.setTag(entry.getKey().getSecond());
		return stack;
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
//		if (tabledItems.isEmpty()) {
//			for (int i = 0; i < 1000; i++) {
//				Item item = Item.byRawId(i);
//				if (item == Items.AIR) {
//					System.out.println("一共有" + i + "个不同ID的物品");
//				} else {
//					insert(item.getDefaultStack());
//				}
//			}
//		}
		packetByteBuf.writeBlockPos(getPos());
	}
	
	public void onDestroyed() {
		CodeUtil.drop(world, new Vec3d(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5), clearToList());
	}
	
	void addSpace(ItemStack stack) {
		addSpace(stack, stack.getCount());
	}
	
	void addSpace(ItemStack stack, int count) {
		stackSpace += 1d * count / stack.getMaxCount();
	}
	
	public int getExtension(Direction direction) {
		return extensions.getOrDefault(direction, 0);
	}
	
	public static CompoundTag tagFromStack(ItemStack stack) {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", Registry.ITEM.getId(stack.getItem()).toString());
		tag.putInt("Count", stack.getCount());
		if (stack.hasTag())
			tag.put("tag", stack.getTag());
		return tag;
	}
	
	public static CompoundTag nullable(CompoundTag tag) {
		return tag == null ? NULL : tag;
	}
	
	static {
		NULL.putBoolean("NULL", true);
	}
}
