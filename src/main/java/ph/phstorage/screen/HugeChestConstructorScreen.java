package ph.phstorage.screen;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import ph.phstorage.ClientInitializer;
import ph.phstorage.Initializer;
import ph.phstorage.block.BlocksRegistry;
import ph.phstorage.screen.handler.HugeChestConstructorScreenHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ph.phstorage.screen.HugeChestConstructorScreen.Finals.*;

@Environment(EnvType.CLIENT)
public class HugeChestConstructorScreen extends HandledScreen<HugeChestConstructorScreenHandler> {
	public static final Identifier BACKGROUND = ClientInitializer.toGuiTexture(Registry.BLOCK.getId(BlocksRegistry.HUGE_CHEST_CONSTRUCTOR));
	public static final Map<Direction, TranslatableText> DIRECTION_TEXTS = ImmutableMap.copyOf(Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), direction -> new TranslatableText("gui." + Initializer.NAMESPACE + "." + direction.getName()))));
	private Map<Direction, TextFieldWidget> textFields;
//	private ButtonWidget constructButton;
	@Nullable
	private TextFieldWidget uneditableTextField;
	
	public HugeChestConstructorScreen(HugeChestConstructorScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	
	@Override
	protected void init() {
		super.init();
		ImmutableMap.Builder<Direction, TextFieldWidget> builder = ImmutableMap.builder();
		int i = 0;
		for (Direction direction : new Direction[]{Direction.EAST, Direction.UP, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.NORTH}) {
			TextFieldWidget textFieldWidget = new TextFieldWidget(textRenderer, x + TFX + TFXD * (i % 3), y + TFY + TFYD * (i / 3), TFW, TFH, LiteralText.EMPTY) {
				
				@Override
				protected void onFocusedChanged(boolean bl) {
					super.onFocusedChanged(bl);
					if (!bl && getText().isEmpty()) {
						setText("0");
					} else if (bl && "0".equals(getText())) {
						setText("");
					}
				}
				
				@Override
				public void setSelected(boolean selected) {
					super.setSelected(selected);
					onFocusedChanged(selected);
				}
				
				@Override
				public boolean mouseClicked(double mouseX, double mouseY, int button) {
					if (uneditableTextField == this) {
						if (isFocused())
							setSelected(false);
						return false;
					} else if (super.mouseClicked(mouseX, mouseY, button)) {
						//为了让其它TextFieldWidget失去焦点
						for (TextFieldWidget textFieldWidget1 : textFields.values()) {
							if (textFieldWidget1 != this) {
								textFieldWidget1.mouseClicked(mouseX, mouseY, button);
							}
						}
						return true;
					}
					return false;
				}
			};
			textFieldWidget.setTextPredicate(string -> string.isEmpty() || StringUtils.isNumeric(string));
			textFieldWidget.setHasBorder(false);
			textFieldWidget.setMaxLength(5);
			textFieldWidget.setText(String.valueOf(handler.getExtension(direction)));
			textFieldWidget.setChangedListener(string -> {
				int c = 0;
				TextFieldWidget emptyTextField = null;
				for (Map.Entry<Direction, TextFieldWidget> entry : textFields.entrySet()) {
					entry.getValue().setEditable(true);
					if (entry.getValue().getText().isEmpty() || "0".equals(entry.getValue().getText())) {
						emptyTextField = entry.getValue();
					} else {
						c++;
					}
				}
				if (c >= 5) {
					if (emptyTextField != null) {
						(uneditableTextField = emptyTextField).setEditable(false);
					} else {
						(uneditableTextField = textFieldWidget).setEditable(false);
						textFieldWidget.setText(string= "0");
						textFieldWidget.setSelected(false);
					}
				} else {
					uneditableTextField = null;
				}
				handler.sendExtension(direction, string.isEmpty() ? 0 : Integer.parseInt(string));
			});
			builder.put(direction, textFieldWidget);
			addButton(textFieldWidget);
			i++;
		}
		textFields = builder.build();
//		addButton(constructButton = new ButtonWidget(x + 118, y + 51, 51, 20, new TranslatableText("gui." + Initializer.NAMESPACE + ".construct"), button -> {
			//			handler.onButtonClick(playerInventory.player,0);
//			client.interactionManager.clickButton(handler.syncId, HugeChestConstructorScreenHandler.CONSTRUCT_BUTTON_ID);
//		}));
	}
	
	@Override//必须重写，不然没有暗色背景和工具提示
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		drawMouseoverTooltip(matrices, mouseX, mouseY);
	}
	
	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		client.getTextureManager().bindTexture(BACKGROUND);
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		if (uneditableTextField != null) {
			drawTexture(matrices, uneditableTextField.x - 2, uneditableTextField.y - 2, 176, 0, uneditableTextField.getWidth() + 4, uneditableTextField.getHeight() + 4);
		}
		for (Map.Entry<Direction, TextFieldWidget> entry : textFields.entrySet()) {
			textRenderer.draw(matrices, DIRECTION_TEXTS.get(entry.getKey()), entry.getValue().x, entry.getValue().y - textRenderer.fontHeight - (TFX - TFTX), 0);
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(matrices, mouseX, mouseY);
	}
	
	static {
	}
	
	public static class Finals {
		public static final int TFX = 9;
		public static final int TFY = 34;
		public static final int TFW = 33;
		public static final int TFH = 9;
		public static final int TFTX = 7;
		public static final int TFTY = 32;
		public static final int TFTW = 37;
		public static final int TFTH = 13;
		public static final int TFXD = TFTW;
		public static final int TFYD = TFTH + 13;
	}
}
