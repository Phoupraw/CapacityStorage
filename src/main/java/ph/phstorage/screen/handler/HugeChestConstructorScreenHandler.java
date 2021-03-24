package ph.phstorage.screen.handler;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import ph.phstorage.api.CodeUtil;
import ph.phstorage.Initializer;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.HugeChestConstructorBlockEntity;

public class HugeChestConstructorScreenHandler extends ScreenHandler {
	public static final int CONSTRUCT_BUTTON_ID = CodeUtil.nextSyncId();
	public static final Identifier CHANNEL =Initializer.wrap(Registry.BLOCK,BlocksRegistry.HUGE_CHEST_CONSTRUCTOR);//  new Identifier(Initializer.NAMESPACE, Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_CONSTRUCTOR).getPath());
	private final HugeChestConstructorBlockEntity thisBlockEntity;
	private final PlayerEntity player;
	
	public HugeChestConstructorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, new HugeChestConstructorBlockEntity());
		for (Direction direction : Direction.values())
			thisBlockEntity.setExtension(direction, buf.readInt());
	}
	
	public HugeChestConstructorScreenHandler(int syncId, PlayerInventory playerInventory, HugeChestConstructorBlockEntity thisBlockEntity) {
		super(ScreenHandlerTypesRegistry.HUGE_CHEST_CONSTRUCTOR, syncId);
		this.thisBlockEntity = thisBlockEntity;
		player = playerInventory.player;
		CodeUtil.addPlayerSlots(this::addSlot, playerInventory);
//		if (player instanceof ServerPlayerEntity) {
//			ServerPlayNetworking.registerReceiver(((ServerPlayerEntity) player).networkHandler, CHANNEL, (server, player1, networkHandler, buf1, sender) -> {
//				if (player1.currentScreenHandler instanceof HugeChestConstructorScreenHandler)
//					((HugeChestConstructorScreenHandler) player1.currentScreenHandler).thisBlockEntity.setExtension(Direction.byId(buf1.readInt()), buf1.readInt());
//			});
//		}
	}
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public void close(PlayerEntity player) {
		//		dropInventory(player, player.world, thisBlockEntity);
		super.close(player);
	}
	
	@Override
	public boolean onButtonClick(PlayerEntity player, int id) {
		if (id == CONSTRUCT_BUTTON_ID) {
			tryConstruct();
			return true;
		}
		return super.onButtonClick(player, id);
	}
	
	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		//		Slot slot = getSlot(index);
		//		ItemStack stack = slot.getStack();
		//		if (index < 36) {
		//			if (stack.getItem() == ItemsRegistry.HUGE_CHEST_WALL) {
		//				insertItem(stack, 36, 37, false);
		//			}
		//		} else if (index == 36) {
		//			insertItem(stack, 0, 36, false);
		//		}
		return ItemStack.EMPTY;
	}
	
	@Environment(EnvType.CLIENT)
	public void sendExtension(Direction direction, int extension) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(direction.getId());
		buf.writeInt(extension);
		ClientPlayNetworking.send(CHANNEL, buf);
	}
	
	public boolean tryConstruct() {
		int a = 1 + thisBlockEntity.getExtension(Direction.EAST) + thisBlockEntity.getExtension(Direction.WEST);
		int b = 1 + thisBlockEntity.getExtension(Direction.UP) + thisBlockEntity.getExtension(Direction.DOWN);
		int c = 1 + thisBlockEntity.getExtension(Direction.SOUTH) + thisBlockEntity.getExtension(Direction.NORTH);
		int cost = a * b * c - Math.max(a - 2, 0) * Math.max(b - 2, 0) * Math.max(c - 2, 0);
		//		int count = mSlot.getStack().getCount();
		//		if (count < cost) {
		//
		//		}
		return false;
	}
	
	public int getExtension(Direction direction) {
		return thisBlockEntity.getExtension(direction);
	}
	public static void loadClass(){}
	static {
		ServerPlayConnectionEvents.INIT.register((networkHandler, server) -> ServerPlayNetworking.registerReceiver(networkHandler, CHANNEL, (server1, player1, networkHandler1, buf1, sender1) -> {
			if (player1.currentScreenHandler instanceof HugeChestConstructorScreenHandler)
				((HugeChestConstructorScreenHandler) player1.currentScreenHandler).thisBlockEntity.setExtension(Direction.byId(buf1.readInt()), buf1.readInt());
		}));
	}
}
