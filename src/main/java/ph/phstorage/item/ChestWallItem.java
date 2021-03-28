package ph.phstorage.item;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.stat.Stats;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import ph.phstorage.block.entity.ChestConstructorBlockEntity;

import java.util.Objects;

public class ChestWallItem extends UnplacableBlockItem {
	public ChestWallItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockEntity blockEntity0 = world.getBlockEntity(context.getBlockPos());
		if (blockEntity0 instanceof ChestConstructorBlockEntity) {
			ChestConstructorBlockEntity constructorBlockEntity = (ChestConstructorBlockEntity) blockEntity0;
			int result = constructorBlockEntity.onPlaced(context.getPlayer());
			if ((result & 0b1) == 0b1) {
				context.getStack().decrement(1);
				if (context.getPlayer() != null)
					context.getPlayer().incrementStat(Stats.USED.getOrCreateStat(this));
				return ActionResult.SUCCESS;
			} else if ((result & 0b10) == 0b10) {
				return ActionResult.CONSUME;
			} else {
				Objects.requireNonNull(context.getPlayer(), "context.getPlayer()").sendMessage(new TranslatableText("actionbar.phstorage.hampered"), true);
				return ActionResult.FAIL;
			}
		}
		return super.useOnBlock(context);
	}
}
