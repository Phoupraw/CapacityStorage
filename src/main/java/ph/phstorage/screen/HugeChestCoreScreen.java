package ph.phstorage.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ph.phstorage.ClientInitializer;
import ph.phstorage.Initializer;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.HugeChestCoreBlockEntity;
import ph.phstorage.screen.handler.HugeChestCoreScreenHandler;

import java.util.List;

@Environment(EnvType.CLIENT)
public class HugeChestCoreScreen extends HandledScreen<HugeChestCoreScreenHandler> {
	public static final Identifier BACKGROUND = ClientInitializer.toGuiTexture(Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_CORE));
	private static int itemsX;
	private static int itemsY;
	private static int itemsWidth;
	private static int itemsHeight;
	private static int borderBreadth;
	private static int fontHeight;
	private static int viewingRow;
	private static int totalRows;
	private static int rowCapacity;
	private List<List<Pair<Integer, ItemStack>>> itemXes;
	
	public HugeChestCoreScreen(HugeChestCoreScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	
	@Override
	protected void init() {
		super.init();
		//		if (itemsX == 0)
		fontHeight = textRenderer.fontHeight;
		itemsX = x / 2 + borderBreadth + fontHeight;
		//		if (itemsY == 0)
		itemsY = y / 2 + borderBreadth;
		//		if (itemsWidth == 0)
		itemsWidth = (width - itemsX * 2) / 16 * 16;
		//		if (itemsHeight == 0)
		itemsHeight = (y + handler.getSlot(0).y - 16 - itemsY) / 16 * 16;
		//		if (borderWidth == 0)
		rowCapacity = itemsHeight / 16;
		viewingRow=0;
		borderBreadth = 8;
		titleX = itemsX - x;
		titleY = itemsY - textRenderer.fontHeight - 1 - y;
		//		playerInventoryTitleX=titleX;
		playerInventoryTitleY = handler.getSlot(0).y - textRenderer.fontHeight - 1;
	}
	
	@Override//必须重写，不然没有暗色背景和工具提示
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(BACKGROUND);
		drawTexture(matrices, x, y + 60, 0, 60, backgroundWidth, 106);
		drawTexture(matrices, itemsX - borderBreadth, itemsY - borderBreadth - fontHeight, 0, 0, borderBreadth, borderBreadth + fontHeight);
		drawTexture(matrices, itemsX + itemsWidth, itemsY - borderBreadth - fontHeight, borderBreadth + 16, 0, borderBreadth, borderBreadth + fontHeight);
		drawTexture(matrices, itemsX - borderBreadth, itemsY + itemsHeight, 0, borderBreadth + fontHeight + 16, borderBreadth, borderBreadth);
		drawTexture(matrices, itemsX + itemsWidth, itemsY + itemsHeight, borderBreadth + 16, borderBreadth + fontHeight + 16, borderBreadth, borderBreadth);
		int x1 = itemsX, y1;
		while (x1 < itemsX + itemsWidth) {
			drawTexture(matrices, x1, itemsY - borderBreadth - fontHeight, borderBreadth, 0, 16, borderBreadth + fontHeight);
			drawTexture(matrices, x1, itemsY + itemsHeight, borderBreadth, borderBreadth + fontHeight + 16, 16, borderBreadth);
			x1 += 16;
		}
		y1 = itemsY;
		while (y1 < itemsY + itemsHeight) {
			drawTexture(matrices, itemsX - borderBreadth, y1, 0, borderBreadth + fontHeight, borderBreadth, 16);
			drawTexture(matrices, itemsX + itemsWidth, y1, borderBreadth + 16, borderBreadth + fontHeight, borderBreadth, 16);
			y1 += 16;
		}
		y1 = itemsY;
		while (y1 < itemsY + itemsHeight) {
			x1 = itemsX;
			while (x1 < itemsX + itemsWidth) {
				drawTexture(matrices, x1, y1, borderBreadth, borderBreadth + fontHeight, 16, 16);
				x1 += 16;
			}
			y1 += 16;
		}
		x1 = itemsX + textRenderer.getWidth(title) + textRenderer.getWidth(" ");
		y1 = itemsY - textRenderer.fontHeight - 1;
		TranslatableText capacityText = new TranslatableText("gui." + Initializer.NAMESPACE + ".capacity", (int) handler.thisBlockEntity.getStackCapacity());
		TranslatableText spaceText = new TranslatableText("gui." + Initializer.NAMESPACE + ".space", (int) handler.thisBlockEntity.getStackSpace(), (int) ((handler.thisBlockEntity.getStackSpace() - (int) handler.thisBlockEntity.getStackSpace()) * 64));
		textRenderer.draw(matrices, capacityText, x1, y1, 4210752);
		x1 += textRenderer.getWidth(capacityText) + textRenderer.getWidth(" ");
		textRenderer.draw(matrices, spaceText, x1, y1, 4210752);
		x1 = itemsX;
		y1 = itemsY;
		itemXes = Lists.newArrayList();
		itemXes.add(Lists.newArrayList());
		int irow = 0;
		totalRows = 1;
		rowCapacity = itemsHeight / 16;
		for (ItemStack stack : handler.thisBlockEntity.getStoredStacks(HugeChestCoreBlockEntity.DEFAULT_COMPARATOR)) {
			String c = String.valueOf(stack.getCount());
			int w = Math.max(16, textRenderer.getWidth(c) + 2);
			if (x1 + w > itemsX + itemsWidth) {
				x1 = itemsX;
				if (irow >= viewingRow) {
					y1 += 16;
					totalRows++;
					if (y1 > itemsY + itemsHeight - 16) {
						totalRows++;
						break;
					}
					itemXes.add(Lists.newArrayList());
				}
				irow++;
			}
			if (irow >= viewingRow) {
				itemRenderer.renderInGuiWithOverrides(stack, x1 + w - 16, y1);
				itemRenderer.renderGuiItemOverlay(textRenderer, stack, x1 + w - 16, y1);
				if (mouseX >= x1 && mouseX < x1 + w && mouseY >= y1 && mouseY < y1 + 16) {
					matrices.translate(0, 0, getZOffset() + 200);
					fill(matrices, x1, y1, x1 + w, y1 + 16, -2130706433);
					matrices.translate(0, 0, getZOffset() - 200);
				}
				itemXes.get(itemXes.size() - 1).add(new Pair<>(w + x1, stack));
			}
			x1 += w;
			//			if ((x1 += w) > itemsX + itemsWidth - 16) {
			//				x1 = itemsX;
			//				if (irow >= viewingRow) {
			//					y1 += 16;
			//					totalRows++;
			//					if (y1 > itemsY + itemsHeight - 16) {
			//						totalRows++;
			//						break;
			//					}
			//					itemXes.add(Lists.newArrayList());
			//				}
			//				irow++;
			//			}
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(matrices, mouseX, mouseY);
	}
	
	@Override
	protected void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType) {
		super.onMouseClick(slot, invSlot, clickData, actionType);
	}
	
	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int x, int y, int key) {
		return (mouseX < itemsX - borderBreadth || mouseX >= itemsX + itemsWidth + borderBreadth || mouseY < itemsY - borderBreadth - fontHeight || mouseY >= itemsY + itemsHeight + borderBreadth) && super.isClickOutsideBounds(mouseX, mouseY, x, y, key);
	}
	
	/**
	 * @param mouseX 鼠标的x坐标
	 * @param mouseY 鼠标的y坐标
	 * @param key 0是左键，1是右键，2是中键
	 *
	 * @return 发生了鼠标事件
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int key) {
		if (mouseX >= itemsX - borderBreadth && mouseX < itemsX && mouseY >= itemsY && mouseY < itemsY + itemsHeight) {
			itemsX += 16 * (key == 0 ? -1 : 1);
			titleX = itemsX - x;
			itemsWidth -= 16 * (key == 0 ? -1 : 1);
			return true;
		}
		if (mouseX >= itemsX + itemsWidth && mouseX < itemsX + itemsWidth + borderBreadth && mouseY >= itemsY && mouseY < itemsY + itemsHeight) {
			itemsWidth -= 16 * (key == 0 ? -1 : 1);
			return true;
		}
		if (mouseX >= itemsX && mouseX < itemsX + itemsWidth && mouseY >= itemsY - borderBreadth - fontHeight && mouseY < itemsY) {
			itemsY += 16 * (key == 0 ? -1 : 1);
			titleY = itemsY - textRenderer.fontHeight - y;
			itemsHeight -= 16 * (key == 0 ? -1 : 1);
			return true;
		}
		if (mouseX >= itemsX && mouseX < itemsX + itemsWidth && mouseY >= itemsY && mouseY < itemsY + itemsHeight) {
			if (playerInventory.getCursorStack().isEmpty()) {
				ItemStack stack = getHoveredStack(mouseX, mouseY).copy();
				if (!stack.isEmpty()) {
					if (stack.getCount() > stack.getMaxCount())
						stack.setCount(stack.getMaxCount());
					int count = 0;
					switch (key) {
						case 0:
							count = stack.getCount();
							break;
						case 1:
							count = (stack.getCount() + 1) / 2;
							break;
						case 2:
							count = -1;
							break;
					}
					SlotActionType go = null;
					if (Screen.hasShiftDown()) {
						go = SlotActionType.QUICK_MOVE;
					} else
						go = SlotActionType.PICKUP;
					handler.syncTakeStack(stack, count, go);
					return true;
				}
			} else {
				handler.syncPutStack(key);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, key);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (isClickOutsideBounds(mouseX, mouseY, x, y, 0)) {
//			System.out.printf("totalRows=%d,rowCapacity=%d,viewingRow=%d%n", totalRows, rowCapacity, viewingRow);
			if (amount < 0 && totalRows > rowCapacity) {
				viewingRow -= amount;
			} else if (amount > 0) {
				viewingRow -= amount;
				if (viewingRow < 0)
					viewingRow = 0;
			}
		} else {
			if (focusedSlot != null) {
				ItemStack stack = focusedSlot.getStack();
				if (!stack.isEmpty()) {
					if (amount < 0) {
					
					}
				}
			}
		}
		return false;
	}
	
	@Override
	protected void drawMouseoverTooltip(MatrixStack matrices, int muoseX, int mouseY) {
		ItemStack stack = getHoveredStack(muoseX, mouseY);
		if (!stack.isEmpty()) {
			renderTooltip(matrices, stack, muoseX, mouseY);
			return;
		}
		super.drawMouseoverTooltip(matrices, muoseX, mouseY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return !(mouseX >= itemsX - borderBreadth && mouseX < itemsX + itemsWidth + borderBreadth && mouseY >= itemsY - borderBreadth - fontHeight && mouseY < itemsY + itemsHeight + borderBreadth) && super.mouseReleased(mouseX, mouseY, button);
	}
	
	public ItemStack getHoveredStack(double mouseX, double mouseY) {
		if (mouseY >= itemsY && mouseY < itemsY + 16 * (1 + itemXes.size()) && mouseX >= itemsX && mouseX < itemsX + itemsWidth) {
			int row = (int) ((mouseY - itemsY) / 16);
			if (row >= 0 && row < itemXes.size()) {
				List<Pair<Integer, ItemStack>> list = itemXes.get(row);
				int lastX = itemsX;
				for (Pair<Integer, ItemStack> pair : list) {
					if (mouseX >= lastX && mouseX < pair.getFirst()) {
						return pair.getSecond();
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}
}
