package ph.phstorage.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

public class UnplacableBlockItem extends BlockItem {
	public UnplacableBlockItem(Block block, Settings settings) {
		super(block, settings);
	}
	
	@Override
	public ActionResult place(ItemPlacementContext context) {
		return ActionResult.PASS;
	}
}
