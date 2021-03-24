package ph.phstorage.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class HugeChestWallBlockEntity extends BlockEntity implements BlockEntityClientSerializable {
	private BlockPos corePos;
	
	public HugeChestWallBlockEntity() {
		super(BlockEntityTypesRegistry.HUGE_CHEST_WALL);
	}
	
	@Override
	public CompoundTag toTag(CompoundTag tag) {
		if (getCorePos() != null)
			tag.put("corePos", NbtHelper.fromBlockPos(getCorePos()));
		return super.toTag(tag);
	}
	
	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		super.fromTag(state, tag);
		setCorePos(NbtHelper.toBlockPos(tag.getCompound("corePos")));
	}
	
	@Nullable
	public HugeChestCoreBlockEntity getCoreBlockEntity() {
		if (corePos != null) {
			BlockEntity blockEntity0 = world.getBlockEntity(getCorePos());
			if (blockEntity0 instanceof HugeChestCoreBlockEntity) {
				return (HugeChestCoreBlockEntity) blockEntity0;
			}
		}
		return null;
	}
	
	public BlockPos getCorePos() {
		return corePos;
	}
	
	protected void setCorePos(BlockPos corePos) {
		this.corePos = corePos;
		if (world!=null&& !world.isClient)
			sync();
	}
	
	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		fromTag(getCachedState(), compoundTag);
	}
	
	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return toTag(compoundTag);
	}
}
