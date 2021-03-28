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
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import ph.phstorage.ClientInitializer;
import ph.phstorage.Initializer;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.block.entity.ChestCoreBlockEntity;
import ph.phstorage.screen.handler.ChestCoreScreenHandler;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class ChestCoreScreen extends HandledScreen<ChestCoreScreenHandler> {
	public static final Identifier BACKGROUND = ClientInitializer.toGuiTexture(Registry.BLOCK.getId(BlocksRegistry.CHEST_CORE));
	private int itemsX;
	private int itemsY;
	private int itemsWidth;
	private int itemsHeight;
	private int borderBreadth;
	private int fontHeight;
	private int viewingRow;
	private int totalRows;
	private int rowCapacity;
	private List<List<Pair<Integer, ItemStack>>> itemXes;
	
	public ChestCoreScreen(ChestCoreScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	
	@Override
	protected void init() {
		super.init();
		fontHeight = textRenderer.fontHeight;
		itemsX = x / 2 + borderBreadth + fontHeight;
		itemsY = y / 2 + borderBreadth;
		itemsWidth = (width - itemsX * 2) / 16 * 16;
		itemsHeight = (y + handler.getSlot(0).y - 16 - itemsY) / 16 * 16;
		rowCapacity = itemsHeight / 16;
		viewingRow = 0;
		borderBreadth = 8;
		titleX = itemsX - x;
		titleY = itemsY - textRenderer.fontHeight - 1 - y;
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
		Objects.requireNonNull(client, "client").getTextureManager().bindTexture(BACKGROUND);
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
		for (ItemStack stack : handler.thisBlockEntity.getStoredStacks(ChestCoreBlockEntity.DEFAULT_COMPARATOR)) {
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
		}
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
		if (Screen.hasShiftDown()) {
			if (focusedSlot != null) {
				ItemStack stack = focusedSlot.getStack();
				if (!stack.isEmpty()) {
					if (key == 2) {
						handler.syncCloneStack(stack, 2);
					} else {
						handler.syncPutStack(focusedSlot.id, key == 0 ? stack.getCount() : stack.getCount()-1);
					}
					return true;
				}
			} else {
				ItemStack stack = getHoveredStack(mouseX, mouseY);
				if (!stack.isEmpty()) {
					if (stack.getCount() > stack.getMaxCount())
						stack.setCount(stack.getMaxCount());
					if (key == 2) {
						handler.syncCloneStack(stack, 1);
					} else {
						handler.syncTakeStack(stack, key == 0 ? stack.getCount() : (stack.getCount() + 1) / 2, SlotActionType.QUICK_MOVE);
					}
					return true;
				}
			}
		} else if (mouseX >= itemsX && mouseX < itemsX + itemsWidth && mouseY >= itemsY && mouseY < itemsY + itemsHeight) {
			ItemStack stack = playerInventory.getCursorStack();
			if (!stack.isEmpty()) {
				if (key == 2) {
					handler.syncCloneStack(stack, 2);
				} else {
					handler.syncPutStack(ChestCoreScreenHandler.CURSOR_SLOT_ID, key == 0 ? stack.getCount() : 1);
				}
				return true;
			} else {
				stack = getHoveredStack(mouseX, mouseY);
				if (!stack.isEmpty()) {
					if (stack.getCount() > stack.getMaxCount())
						stack.setCount(stack.getMaxCount());
					if (key == 2) {
						handler.syncCloneStack(stack, 0);
					} else {
						handler.syncTakeStack(stack, key == 0 ? stack.getCount() : (stack.getCount() + 1) / 2, SlotActionType.PICKUP);
					}
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, key);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		final int std = 0;
		if (isClickOutsideBounds(mouseX, mouseY, x, y, 0)) {
			if (amount < std && totalRows > rowCapacity) {
				viewingRow -= amount;
			} else if (amount > std) {
				viewingRow -= amount;
				if (viewingRow < 0)
					viewingRow = 0;
			}
		} else {
			if (focusedSlot != null) {
				ItemStack stack = focusedSlot.getStack();
				if (!stack.isEmpty()) {
					if (amount > std) {
						if (Screen.hasShiftDown()) {
							for (int i = 0; i < 27 && amount > std; i++) {
								if (i != focusedSlot.id && ScreenHandler.canStacksCombine(stack, handler.getSlot(i).getStack())) {
									amount--;
									handler.syncPutStack(i, handler.getSlot(i).getStack().getCount());
								}
							}
							if (amount > std) {
								handler.syncPutStack(focusedSlot.id, focusedSlot.getStack().getCount());
							}
						} else {
							while (!focusedSlot.getStack().isEmpty() && amount > std) {
								amount--;
								handler.syncPutStack(focusedSlot.id, 1);
							}
						}
					} else {
						while (amount < std) {
							amount++;
							handler.syncTakeStack(stack, Screen.hasShiftDown() ? stack.getMaxCount() : 1, SlotActionType.QUICK_MOVE);
						}
					}
				}
			} else {
				ItemStack stack = getHoveredStack(mouseX, mouseY);
				if (!stack.isEmpty()) {
					if (amount > std) {
						for (int i = 0; i < 27 && amount > std; i++) {
							if (ScreenHandler.canStacksCombine(stack, handler.getSlot(i).getStack())) {
								amount--;
								handler.syncPutStack(i, Screen.hasShiftDown() ? handler.getSlot(i).getStack().getCount() : 1);
							}
						}
					} else {
						while (amount < std) {
							amount++;
							handler.syncTakeStack(stack, Screen.hasShiftDown() ? stack.getMaxCount() : 1, SlotActionType.QUICK_MOVE);
						}
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
		if (mouseX >= itemsX - borderBreadth && mouseX < itemsX + itemsWidth + borderBreadth && mouseY >= itemsY - borderBreadth - fontHeight && mouseY < itemsY + itemsHeight + borderBreadth)
			return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	private ItemStack getHoveredStack(double mouseX, double mouseY) {
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
