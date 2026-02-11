package nofrills.hud.elements;

import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.MapColor;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import nofrills.config.*;
import nofrills.hud.HudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.List;

import static nofrills.Main.mc;

public class DungeonMap extends HudElement {
    private final SpriteAtlasTexture atlasTexture = mc.getAtlasManager().getAtlasTexture(Atlases.MAP_DECORATIONS);
    private final NativeImageBackedTexture mapTexture = new NativeImageBackedTexture(() -> "NoFrills Dungeon Map", new NativeImage(128, 128, true));
    private final SettingDouble selfMarkerScale = new SettingDouble(7.0, "selfMarkerScale", this.instance);
    private final SettingDouble playerMarkerScale = new SettingDouble(1.5, "playerMarkerScale", this.instance);
    private final SettingDouble playerNameScale = new SettingDouble(0.8, "playerNameScale", this.instance);
    private final SettingEnum<NameMode> playerNameMode = new SettingEnum<>(NameMode.Normal, NameMode.class, "playerNameMode", this.instance);
    private final SettingColor healColor = new SettingColor(RenderColor.fromHex(0xecb50c), "healerColor", this.instance);
    private final SettingColor mageColor = new SettingColor(RenderColor.fromHex(0x1793c4), "mageColor", this.instance);
    private final SettingColor bersColor = new SettingColor(RenderColor.fromHex(0xe7413c), "bersColor", this.instance);
    private final SettingColor archColor = new SettingColor(RenderColor.fromHex(0x4a14b7), "archColor", this.instance);
    private final SettingColor tankColor = new SettingColor(RenderColor.fromHex(0x768f46), "tankColor", this.instance);
    private final SettingBool debug = new SettingBool(false, "debug", this.instance);
    private List<DungeonUtil.Teammate> teammates = List.of();
    private MapParameters parameters = null;

    public DungeonMap() {
        super(new Feature("dungeonMapElement"), "Dungeon Map Element");
        this.layout.sizing(Sizing.fixed(128), Sizing.fixed(128));
        this.options = this.getBaseSettings(List.of(
                new Settings.SliderDouble("Self Scale", 0.0, 10.0, 0.01, this.selfMarkerScale, "The scale of your own player marker on the map."),
                new Settings.SliderDouble("Player Scale", 0.0, 2.0, 0.01, this.playerMarkerScale, "The scale of the markers of your teammates."),
                new Settings.SliderDouble("Name Scale", 0.0, 1.0, 0.01, this.playerNameScale, "The scale of the name displayed below teammate markers."),
                new Settings.Dropdown<>("Name Mode", this.playerNameMode, "The mode of how the player names are displayed.\n\nNormal: The full name of the player is displayed.\nClass: A short player class label is displayed (\"Arch\", \"Bers\" etc.).\nDisabled: No names displayed."),
                new Settings.ColorPicker("Healer Color", false, this.healColor, "The color used for the Healer marker name text."),
                new Settings.ColorPicker("Mage Color", false, this.mageColor, "The color used for the Mage marker name text."),
                new Settings.ColorPicker("Bers Color", false, this.bersColor, "The color used for the Berserk marker name text."),
                new Settings.ColorPicker("Archer Color", false, this.archColor, "The color used for the Archer marker name text."),
                new Settings.ColorPicker("Tank Color", false, this.tankColor, "The color used for the Tank marker name text."),
                new Settings.Toggle("Debug", this.debug, "Outputs debug information about the map's behavior.")
        ));
        this.setDesc("Displays the dungeon map while in Dungeons.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender() || mc.world == null) {
            return;
        } else if (!this.isEditingHud() && (!Utils.isInDungeons() || DungeonUtil.isInBossRoom())) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        MapState mapState = DungeonUtil.getMap();
        if (mapState != null && this.parameters != null) {
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            float scale = this.scale.valueFloat();
            if (scale != 1.0f && !this.isEditingHud()) {
                this.applyScaling(context, scale);
            }
            matrices.translate(this.x(), this.y());
            context.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, this.mapTexture.getGlTextureView(), 0, 0, 128, 128, 0.0F, 1.0F, 0.0F, 1.0F, -1);
            int index = 0;
            ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
            for (MapDecoration decor : mapState.decorations.values()) {
                if (this.isMarkerSelf(decor)) {
                    byte[] pos = parameters.getPlayerMarkerPos(delta);
                    byte rot = parameters.getPlayerMarkerRot(delta);
                    this.drawMarker(context, decor, pos[0], pos[1], rot, this.selfMarkerScale.valueFloat());
                } else {
                    if (index < this.teammates.size()) {
                        DungeonUtil.Teammate teammate = this.teammates.get(index);
                        index += 1;
                        PlayerListEntry entry = networkHandler != null ? networkHandler.getPlayerListEntry(teammate.name()) : null;
                        Identifier texture = entry != null ? entry.getSkinTextures().body().texturePath() : null;
                        if (texture != null) {
                            this.drawMarkerHead(context, texture, decor.x(), decor.z(), decor.rotation(), this.playerMarkerScale.valueFloat());
                            this.drawMarkerLabel(context, teammate, decor.x(), decor.z(), this.playerNameScale.valueFloat());
                            continue;
                        }
                    }
                    this.drawMarker(context, decor, decor.x(), decor.z(), decor.rotation(), this.playerMarkerScale.valueFloat());
                }
            }
            matrices.popMatrix();
        } else if (this.isEditingHud()) {
            context.drawCenteredTextWithShadow(mc.textRenderer, "Dungeon Map", (int) (this.x + this.width * 0.5), (int) (this.y + this.height * 0.5) - 4, 0xffffffff);
        }
    }

    private MapParameters getMapParameters() {
        return switch (DungeonUtil.getCurrentFloor()) {
            case "E", "F1", "M1" -> new MapParameters(1.5f, 1.5f, -136);
            case "F2", "M2", "F3", "M3" -> new MapParameters(1.5f, 1.5f, -120);
            case "F4", "M4", "F6", "M6", "F7", "M7", "F5", "M5" -> new MapParameters(1.6f, 1.6f, -104);
            default -> new MapParameters(1.0f, 1.0f, -120);
        };
    }

    private boolean isMarkerSelf(MapDecoration decor) {
        return decor.type().value().equals(MapDecorationTypes.FRAME.value());
    }

    protected void drawMarker(OwoUIDrawContext context, MapDecoration decor, byte x, byte z, byte rot, float scale) {
        Sprite sprite = this.atlasTexture.getSprite(decor.getAssetId());
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x / 2.0F + 64.0F, z / 2.0F + 64.0F);
        matrices.rotate((float) (Math.PI / 180.0) * rot * 360.0F / 16.0F);
        matrices.scale(scale, scale);
        matrices.translate(-0.125F, 0.125F);
        GpuTextureView view = mc.getTextureManager().getTexture(sprite.getAtlasId()).getGlTextureView();
        context.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, view, -1, -1, 1, 1, sprite.getMinU(), sprite.getMaxU(), sprite.getMaxV(), sprite.getMinV(), -1);
        matrices.popMatrix();
    }

    protected void drawMarkerHead(OwoUIDrawContext context, Identifier texture, byte x, byte z, byte rot, float scale) {
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x / 2.0F + 64.0F, z / 2.0F + 64.0F);
        matrices.rotate((float) (Math.PI / 180.0) * rot * 360.0F / 16.0F);
        matrices.scale(scale, scale);
        matrices.translate(-0.125F * 24, -0.125F * 24);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, -1, -1, 8.0F, 16.0f, 8, 8, 8, -8, 64, 64, -1);
        matrices.popMatrix();
    }

    protected void drawMarkerLabel(OwoUIDrawContext context, DungeonUtil.Teammate teammate, byte x, byte z, float scale) {
        NameMode mode = this.playerNameMode.value();
        if (mode.equals(NameMode.Disabled)) {
            return;
        }
        String text = mode.equals(NameMode.Normal) ? teammate.name() : switch (teammate.selectedClass()) {
            case "Healer" -> "Heal";
            case "Berserk" -> "Bers";
            case "Archer" -> "Arch";
            default -> teammate.selectedClass();
        };
        float width = mc.textRenderer.getWidth(text);
        int color = switch (teammate.selectedClass()) {
            case "Healer" -> this.healColor.value().argb;
            case "Mage" -> this.mageColor.value().argb;
            case "Berserk" -> this.bersColor.value().argb;
            case "Archer" -> this.archColor.value().argb;
            case "Tank" -> this.tankColor.value().argb;
            default -> 0xffffff;
        };
        OrderedText orderedText = Text.literal(text).asOrderedText();
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x / 2.0F + 64.0F - width * scale / 2.0F, z / 2.0F + 64.0F + 8.0F);
        matrices.scale(scale, scale);
        context.state.addText(new TextGuiElementRenderState(mc.textRenderer, orderedText, new Matrix3x2f(matrices), 0, 0, color, Integer.MIN_VALUE, true, context.scissorStack.peekLast()));
        matrices.popMatrix();
    }

    public void onMapUpdate(MapUpdateS2CPacket packet) {
        if (packet.mapId().equals(DungeonUtil.getMapId()) && Utils.isInDungeons()) {
            this.teammates = DungeonUtil.getAliveTeammates(true);
            if (this.parameters == null) {
                for (MapDecoration decor : packet.decorations().orElse(List.of())) {
                    if (this.isMarkerSelf(decor)) {
                        this.parameters = this.getMapParameters().adjustCenter(decor, this.debug.value());
                        break;
                    }
                }
            }
            packet.updateData().ifPresent(data -> {
                byte[] colors = data.colors();
                NativeImage nativeImage = this.mapTexture.getImage();
                if (nativeImage != null) {
                    for (int i = 0; i < 128; i++) {
                        for (int j = 0; j < 128; j++) {
                            int k = j + i * 128;
                            nativeImage.setColorArgb(j, i, MapColor.getRenderColor(colors[k]));
                        }
                    }
                }
                this.mapTexture.upload();
            });
        }
    }

    public void reset() {
        this.teammates = List.of();
        this.parameters = null;
    }

    public enum NameMode {
        Normal,
        Class,
        Disabled
    }

    public static class MapParameters {
        public float scaleX;
        public float scaleY;
        public int centerX;
        public int centerY;

        public MapParameters(float scaleX, float scaleY, int center) {
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.centerX = center;
            this.centerY = center;
        }

        public MapParameters adjustCenter(MapDecoration playerMarker, boolean debug) {
            if (debug) {
                Utils.infoFormat("Got first map marker position: {} {}", playerMarker.x(), playerMarker.z());
                Utils.infoFormat("Map parameters: {} {} {} {}", this.scaleX, this.scaleY, this.centerX, this.centerY);
            }
            Vec3d pos = mc.player.getEntityPos();
            byte currentX = this.toMarkerPos(this.toCoordX(pos, this.centerX));
            byte currentZ = this.toMarkerPos(this.toCoordZ(pos, this.centerY));
            int diffX = Utils.difference(currentX, playerMarker.x());
            int diffZ = Utils.difference(currentZ, playerMarker.z());
            if (diffX > 4) {
                if (debug) {
                    Utils.infoFormat("Map parameter updated: X Center, {} -> -120 ({})", this.centerX, diffX);
                }
                this.centerX = -120;
            }
            if (diffZ > 4) {
                if (debug) {
                    Utils.infoFormat("Map parameter updated: Y Center, {} -> -120 ({})", this.centerY, diffZ);
                }
                this.centerY = -120;
            }
            return this;
        }

        public byte[] getPlayerMarkerPos(float delta) {
            Vec3d pos = mc.player.getLerpedPos(delta);
            return new byte[]{this.toMarkerPos(this.toCoordX(pos, this.centerX)), this.toMarkerPos(this.toCoordZ(pos, this.centerY))};
        }

        public byte getPlayerMarkerRot(float delta) {
            float rot = mc.player.getYaw(delta);
            return this.toMarkerDir(rot);
        }

        private float toCoordX(Vec3d pos, int center) {
            return (float) (pos.getX() - center) / this.scaleX;
        }

        private float toCoordZ(Vec3d pos, int center) {
            return (float) (pos.getZ() - center) / this.scaleY;
        }

        private byte toMarkerPos(float coord) {
            if (coord <= -63.0F) {
                return -128;
            } else {
                return coord >= 63.0F ? 127 : (byte) (coord * 2.0F + 0.5);
            }
        }

        private byte toMarkerDir(float rotation) {
            double offset = rotation < 0.0 ? rotation - 8.0 : rotation + 8.0;
            return (byte) (offset * 16.0 / 360.0);
        }
    }
}
