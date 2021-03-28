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
import ph.phstorage.block.entity.ChestConstructorBlockEntity;

public class ChestConstructorScreenHandler extends ScreenHandler {
	public static final Identifier CHANNEL =Initializer.wrap(Registry.BLOCK,BlocksRegistry.CHEST_CONSTRUCTOR);
	private final ChestConstructorBlockEntity thisBlockEntity;
	
	public ChestConstructorScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
		this(syncId, playerInventory, new ChestConstructorBlockEntity());
		for (Direction direction : Direction.values())
			thisBlockEntity.setExtension(direction, buf.readInt());
	}
	
	public ChestConstructorScreenHandler(int syncId, PlayerInventory playerInventory, ChestConstructorBlockEntity thisBlockEntity) {
		super(ScreenHandlerTypesRegistry.HUGE_CHEST_CONSTRUCTOR, syncId);
		this.thisBlockEntity = thisBlockEntity;
		CodeUtil.addPlayerSlots(this::addSlot, playerInventory);
	}
	
	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}
	
	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		return ItemStack.EMPTY;
	}
	
	@Environment(EnvType.CLIENT)
	public void sendExtension(Direction direction, int extension) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(direction.getId());
		buf.writeInt(extension);
		ClientPlayNetworking.send(CHANNEL, buf);
	}
	
	public int getExtension(Direction direction) {
		return thisBlockEntity.getExtension(direction);
	}
	
	static {
		ServerPlayConnectionEvents.INIT.register((networkHandler, server) -> ServerPlayNetworking.registerReceiver(networkHandler, CHANNEL, (server1, player1, networkHandler1, buf1, sender1) -> {
			if (player1.currentScreenHandler instanceof ChestConstructorScreenHandler)
				((ChestConstructorScreenHandler) player1.currentScreenHandler).thisBlockEntity.setExtension(Direction.byId(buf1.readInt()), buf1.readInt());
		}));
	}
}
