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
import ph.phstorage.block.entity.ChestCoreBlockEntity;

public class ChestCoreScreenHandler extends ScreenHandler {
	public static final Identifier CHANNEL = Initializer.wrap(Registry.BLOCK, BlocksRegistry.CHEST_CORE);// Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_CORE);
	public static final int CURSOR_SLOT_ID = -70;
	public final ChestCoreBlockEntity thisBlockEntity;
	private final PlayerInventory playerInventory;
	
	public ChestCoreScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, (ChestCoreBlockEntity) playerInventory.player.getEntityWorld().getBlockEntity(buf.readBlockPos()));
	}
	
	public ChestCoreScreenHandler(int syncId, PlayerInventory playerInventory, ChestCoreBlockEntity thisBlockEntity) {
		super(ScreenHandlerTypesRegistry.HUGE_CHEST_CORE, syncId);
		this.playerInventory = playerInventory;
		this.thisBlockEntity = thisBlockEntity;
		CodeUtil.addPlayerSlots(this::addSlot, playerInventory);
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
	
	public void syncPutStack(int slotId, int count) {
		if (putStack(slotId, count)) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(0);
			buf.writeInt(slotId);
			buf.writeInt(count);
			ClientPlayNetworking.send(CHANNEL, buf);
		}
	}
	
	public void syncTakeStack(ItemStack stack, int count, SlotActionType to) {
		ItemStack stack1 = stack.copy();
		if (takeStack(stack, count, to)) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(1);
			buf.writeItemStack(stack1);
			buf.writeInt(count);
			buf.writeEnumConstant(to);
			ClientPlayNetworking.send(CHANNEL, buf);
		}
	}
	
	/**
	 * @param stack 要复制的物品
	 * @param to 去向：0->鼠标；1->物品栏；2->箱子；3->扔出
	 */
	public void syncCloneStack(ItemStack stack, int to) {
		if (cloneStack(stack, to)) {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			buf.writeByte(2);
			buf.writeItemStack(stack);
			buf.writeByte(to);
			ClientPlayNetworking.send(CHANNEL, buf);
		}
	}
	
	private boolean putStack(int slotId, int count) {
		ItemStack stack;
		if (slotId == CURSOR_SLOT_ID) {
			stack = playerInventory.getCursorStack();
		} else {
			stack = getSlot(slotId).getStack();
		}
		if (stack.isEmpty())
			return false;
		ItemStack stack2 = stack.split(count);
		ItemStack stack1 = thisBlockEntity.insert(stack2);
		if (!stack1.isEmpty()) {
			if (stack.isEmpty()) {
				stack = stack1;
			} else {
				stack.increment(stack1.getCount());
			}
		}
		if (slotId == CURSOR_SLOT_ID) {
			playerInventory.setCursorStack(stack);
		} else {
			getSlot(slotId).setStack(stack);
		}
		return !ItemStack.areEqual(stack1, stack2);
	}
	
	private boolean takeStack(ItemStack stack, int count, SlotActionType to) {
		stack = thisBlockEntity.extract(stack, count);
		if (stack.isEmpty())
			return false;
		switch (to) {
			case PICKUP:
				if (!playerInventory.getCursorStack().isEmpty())
					playerInventory.setCursorStack(thisBlockEntity.insert(playerInventory.getCursorStack()));
				if (playerInventory.getCursorStack().isEmpty()) {
					playerInventory.setCursorStack(stack);
					return true;
				} else {
					thisBlockEntity.insert(stack);
					return false;
				}
			case QUICK_MOVE:
				boolean b = insertItem(stack, 0, 36, false);
				if (!stack.isEmpty())
					thisBlockEntity.insert(stack);
				return b;
			case THROW:
				playerInventory.player.dropItem(stack, true);
				return true;
		}
		return false;
	}
	
	private boolean cloneStack(ItemStack stack, int to) {
		if (stack.isEmpty() || !playerInventory.player.isCreative())
			return false;
		switch (to) {
			case 0:
				if (playerInventory.getCursorStack().isEmpty())
					playerInventory.setCursorStack(stack);
				else
					return false;
				break;
			case 1:
				if (!insertItem(stack.copy(), 0, 36, false))
					return false;
				break;
			case 2:
				if (stack.equals(thisBlockEntity.insert(stack)))
					return false;
				break;
			case 3:
				playerInventory.player.dropItem(stack, false, true);
				break;
		}
		return true;
	}
	
	private void receive(PacketByteBuf buf) {
		switch (buf.readByte()) {
			case 0:
				putStack(buf.readInt(), buf.readInt());
				break;
			case 1:
				takeStack(buf.readItemStack(), buf.readInt(), buf.readEnumConstant(SlotActionType.class));
				break;
			case 2:
				cloneStack(buf.readItemStack(), buf.readByte());
		}
	}
	
	static {
		ServerPlayConnectionEvents.INIT.register((networkHandler, server) -> ServerPlayNetworking.registerReceiver(networkHandler, CHANNEL, (server1, player1, networkHandler1, buf1, sender1) -> {
			if (player1.currentScreenHandler instanceof ChestCoreScreenHandler) {
				((ChestCoreScreenHandler) player1.currentScreenHandler).receive(buf1);
			}
		}));
	}
}
