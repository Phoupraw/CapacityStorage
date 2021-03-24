package ph.phstorage.screen.handler;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ph.phstorage.Initializer;
import ph.phstorage.api.CodeUtil;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.HugeChestCoreBlockEntity;

public class HugeChestCoreScreenHandler extends ScreenHandler {
	public static final Identifier CHANNEL =Initializer.wrap(Registry.BLOCK,BlocksRegistry.HUGE_CHEST_CORE);// Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_CORE);
	public final HugeChestCoreBlockEntity thisBlockEntity;
	private final PlayerInventory playerInventory;
	
	public HugeChestCoreScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, (HugeChestCoreBlockEntity) playerInventory.player.getEntityWorld().getBlockEntity(buf.readBlockPos()));
		
	}
	
	public HugeChestCoreScreenHandler(int syncId, PlayerInventory playerInventory, HugeChestCoreBlockEntity thisBlockEntity) {
		super(ScreenHandlerTypesRegistry.HUGE_CHEST_CORE, syncId);
		this.playerInventory = playerInventory;
		this.thisBlockEntity = thisBlockEntity;
		CodeUtil.addPlayerSlots(this::addSlot, playerInventory);
	}
	
	public void refreshSlots() {
	}
	
	@Override
	protected Slot addSlot(Slot slot) {
		
		return super.addSlot(slot);
	}
	
	@Override
	public void onContentChanged(Inventory inventory) {
		super.onContentChanged(inventory);
	}
	
	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		Slot slot = getSlot(index);
		ItemStack stack = slot.getStack().copy();
		if (!stack.isEmpty()) {
			if (slot.inventory == playerInventory) {
				stack = thisBlockEntity.insert(stack);
				refreshSlots();
			} else {
				
				//			insertItem(stack, 0, 36, false);
			}
			if (stack.isEmpty()) {
				slot.takeStack(slot.getStack().getCount());
			} else {
				slot.takeStack(slot.getStack().getCount() - stack.getCount());
			}
		}
		return ItemStack.EMPTY;
	}
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public ItemStack onSlotClick(int slotId, int key, SlotActionType actionType, PlayerEntity playerEntity) {
		return super.onSlotClick(slotId, key, actionType, playerEntity);
	}
	
	@Override
	public boolean onButtonClick(PlayerEntity player, int buttonId) {
		return super.onButtonClick(player, buttonId);
	}
	
	public void syncPutStack(int code) {
		putStack(code);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(0);
		buf.writeInt(code);
		ClientPlayNetworking.send(CHANNEL, buf);
	}
	public void putStack(SlotActionType from, int count, int slotId) {//TODO
		ItemStack stack = ItemStack.EMPTY;
		switch (from){
			case PICKUP:
				if (!playerInventory.getCursorStack().isEmpty())
					stack=playerInventory.getCursorStack().split(count);
				break;
			case QUICK_MOVE:
				Slot slot=getSlot(slotId);
				stack=slot.getStack().split(count);
				break;
		}
	}
	public void putStack(int code) {
//		System.out.println("putStack");
		if (code == 0 || code == 1) {
			ItemStack stack = playerInventory.getCursorStack();
			if (!stack.isEmpty()) {
				int count = 0;
				if (code == 1) {
					count = stack.getCount() - 1;
					stack.setCount(1);
				}
				stack = thisBlockEntity.insert(stack);
				stack.increment(count);
				playerInventory.setCursorStack(stack);
			}
		} else if (code == 2) {
			if (playerInventory.player.isCreative()) {
				thisBlockEntity.insert(playerInventory.getCursorStack());
			}
		}
	}
	
	public void syncTakeStack(ItemStack stack, int count, SlotActionType to) {
		takeStack(stack,count,to);
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeByte(1);
		buf.writeItemStack(stack);
		buf.writeInt(count);
		buf.writeEnumConstant(to);
		ClientPlayNetworking.send(CHANNEL, buf);
	}
	
	public void takeStack(ItemStack stack, int count, SlotActionType to) {
		if (count < 0 && playerInventory.player.isCreative()) {
			stack.setCount(stack.getMaxCount());
		} else if (count > 0) {
			stack = thisBlockEntity.extract(stack, count);
		}
		switch (to) {
			case PICKUP:
				if (!playerInventory.getCursorStack().isEmpty())
					playerInventory.setCursorStack(thisBlockEntity.insert(playerInventory.getCursorStack()));
				if (playerInventory.getCursorStack().isEmpty())
					playerInventory.setCursorStack(stack);
				else
					thisBlockEntity.insert(stack);
				break;
			case QUICK_MOVE:
				insertItem(stack , 0, 36, false);
				if (!stack.isEmpty())
					thisBlockEntity.insert(stack);
				break;
			case THROW:
				playerInventory.player.dropItem(stack, true);
				break;
		}
	}
	
	public void receive(PacketByteBuf buf) {
		byte code = buf.readByte();
		if ((code & 1) == 0) {
			putStack(buf.readInt());
		} else {
			takeStack(buf.readItemStack(),buf.readInt(),buf.readEnumConstant(SlotActionType.class));
		}
	}
	
	static {
		ServerPlayConnectionEvents.INIT.register((networkHandler, server) -> ServerPlayNetworking.registerReceiver(networkHandler, CHANNEL, (server1, player1, networkHandler1, buf1, sender1) -> {
			if (player1.currentScreenHandler instanceof HugeChestCoreScreenHandler) {
				((HugeChestCoreScreenHandler) player1.currentScreenHandler).receive(buf1);
			}
		}));
	}
}
