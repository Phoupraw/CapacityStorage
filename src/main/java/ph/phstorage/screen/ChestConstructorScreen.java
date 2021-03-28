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
import ph.phstorage.screen.handler.ChestConstructorScreenHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ChestConstructorScreen extends HandledScreen<ChestConstructorScreenHandler> {
	public static final Identifier BACKGROUND = ClientInitializer.toGuiTexture(Registry.BLOCK.getId(BlocksRegistry.CHEST_CONSTRUCTOR));
	public static final Map<Direction, TranslatableText> DIRECTION_TEXTS = ImmutableMap.copyOf(Arrays.stream(Direction.values()).collect(Collectors.toMap(Function.identity(), direction -> new TranslatableText("gui." + Initializer.NAMESPACE + "." + direction.getName()))));
	private Map<Direction, TextFieldWidget> textFields;
	@Nullable
	private TextFieldWidget uneditableTextField;
	
	public ChestConstructorScreen(ChestConstructorScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}
	
	@Override
	protected void init() {
		super.init();
		ImmutableMap.Builder<Direction, TextFieldWidget> builder = ImmutableMap.builder();
		int i = 0;
		for (Direction direction : new Direction[]{Direction.EAST, Direction.UP, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.NORTH}) {
			TextFieldWidget textFieldWidget = new TextFieldWidget(textRenderer, x + 9 + 54 * (i % 3), y + 34 + 26 * (i / 3), 50, 9, LiteralText.EMPTY) {
				
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
				public boolean mouseClicked(double mouseX, double mouseY, int mouse) {
					if (hovered) {
						super.mouseClicked(mouseX, mouseY, mouse);
						if (uneditableTextField == this) {//如果自己是不可编辑的，则失去焦点
							setText("0");
							setSelected(false);
						} else {
							if (mouse == 1) {//右键清除文本
								setText("");
							}
							setSelected(true);
							for (TextFieldWidget textFieldWidget1 : textFields.values()) {//为了让其它TextFieldWidget失去焦点
								if (textFieldWidget1 != this) {
									textFieldWidget1.setSelected(false);
								}
							}
						}
						return true;
					}else{
						setSelected(false);
					}
					return false;
				}
			};
			textFieldWidget.setTextPredicate(string -> string.isEmpty() || StringUtils.isNumeric(string));
			textFieldWidget.setHasBorder(false);
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
						textFieldWidget.setText(string = "0");
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
		for (TextFieldWidget textField:textFields.values()){
			textField.setText(textField.getText());
		}
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
		drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);
		for (Map.Entry<Direction, TextFieldWidget> entry : textFields.entrySet()) {
			drawTexture(matrices, entry.getValue().x - 2, entry.getValue().y - 2, 176, 0, entry.getValue().getWidth() + 4, entry.getValue().getHeight() + 4);
			if (entry.getValue() == uneditableTextField) {
				drawTexture(matrices, Objects.requireNonNull(uneditableTextField, "uneditableTextField").x - 2, uneditableTextField.y - 2, 176, uneditableTextField.getHeight()+4, uneditableTextField.getWidth() + 4, uneditableTextField.getHeight() + 4);
			} else {
				drawTexture(matrices, entry.getValue().x - 2, entry.getValue().y - 2, 176, 0, entry.getValue().getWidth() + 4, entry.getValue().getHeight() + 4);
			}
		}
		for (Map.Entry<Direction, TextFieldWidget> entry : textFields.entrySet()) {
			textRenderer.draw(matrices, DIRECTION_TEXTS.get(entry.getKey()), entry.getValue().x, entry.getValue().y - textRenderer.fontHeight - 2, 0);
		}
	}
	
	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		super.drawForeground(matrices, mouseX, mouseY);
	}
}
