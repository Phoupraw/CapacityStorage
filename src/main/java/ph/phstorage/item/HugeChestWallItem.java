package ph.phstorage.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.HugeChestConstructorBlockEntity;

public class HugeChestWallItem extends UnplacableBlockItem{
	public HugeChestWallItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world=context.getWorld();
		if(world.getBlockState(context.getBlockPos()).isOf(BlocksRegistry.HUGE_CHEST_CONSTRUCTOR)){
			HugeChestConstructorBlockEntity constructorBlockEntity = (HugeChestConstructorBlockEntity) world.getBlockEntity(context.getBlockPos());
			if(constructorBlockEntity.onPlaced(context.getPlayer()).isAccepted()){
				context.getStack().decrement(1);
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnBlock(context);
	}
}
