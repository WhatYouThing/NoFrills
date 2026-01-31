package nofrills.hud.elements;

import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.render.state.TextGuiElementRenderState;
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
import nofrills.config.Feature;
import nofrills.config.SettingDouble;
import nofrills.hud.HudElement;
import nofrills.hud.clickgui.Settings;
import nofrills.misc.DungeonUtil;
import nofrills.misc.Utils;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.List;

import static nofrills.Main.mc;

public class DungeonMap extends HudElement {
    private final SpriteAtlasTexture atlasTexture = mc.getAtlasManager().getAtlasTexture(Atlases.MAP_DECORATIONS);
    private final SettingDouble selfMarkerScale;
    private final SettingDouble otherMarkerScale;
    private final SettingDouble markerNameScale;
    private MapParameters parameters = null;

    public DungeonMap() {
        super(new Feature("dungeonMapElement"), "Dungeon Map Element");
        this.layout.sizing(Sizing.fixed(128), Sizing.fixed(128));
        this.selfMarkerScale = new SettingDouble(8.0, "selfMarkerScale", this.instance);
        this.otherMarkerScale = new SettingDouble(5.0, "otherMarkerScale", this.instance);
        this.markerNameScale = new SettingDouble(0.75, "markerNameScale", this.instance);
        this.options = this.getBaseSettings(List.of(
                new Settings.SliderDouble("Self Marker Scale", 0.0, 10.0, 0.01, this.selfMarkerScale, "The scale of your own player marker on the map."),
                new Settings.SliderDouble("Other Marker Scale", 0.0, 10.0, 0.01, this.otherMarkerScale, "The scale of the markers of your teammates."),
                new Settings.SliderDouble("Marker Name Scale", 0.0, 1.0, 0.01, this.markerNameScale, "The scale of the name displayed below teammate markers.")
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
        List<String> team = DungeonUtil.getTeamCache();
        if (mapState != null && this.parameters != null) {
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            float scale = this.scale.valueFloat();
            if (scale != 1.0f && !this.isEditingHud()) {
                this.applyScaling(context, scale);
            }
            matrices.translate(this.x(), this.y());
            Identifier textureID = mc.getMapTextureManager().getTextureId(DungeonUtil.getMapId(), mapState);
            GpuTextureView textureView = mc.getTextureManager().getTexture(textureID).getGlTextureView();
            context.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, textureView, 0, 0, 128, 128, 0.0F, 1.0F, 0.0F, 1.0F, -1);
            int index = 1;
            for (MapDecoration decor : mapState.decorations.values()) {
                if (this.isMarkerSelf(decor)) {
                    byte[] pos = parameters.getPlayerMarkerPos(delta);
                    byte rot = parameters.getPlayerMarkerRot(delta);
                    this.drawMarker(context, decor, pos[0], pos[1], rot, this.selfMarkerScale.valueFloat());
                } else {
                    this.drawMarker(context, decor, decor.x(), decor.z(), decor.rotation(), this.otherMarkerScale.valueFloat());
                    if (index < team.size()) {
                        this.drawMarkerLabel(context, team.get(index), decor.x(), decor.z(), this.markerNameScale.valueFloat());
                    }
                    index += 1;
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

    protected void drawMarkerLabel(OwoUIDrawContext context, String text, byte x, byte z, float scale) {
        float width = mc.textRenderer.getWidth(text);
        OrderedText orderedText = Text.literal(text).asOrderedText();
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x / 2.0F + 64.0F - width * scale / 2.0F, z / 2.0F + 64.0F + 8.0F);
        matrices.scale(scale, scale);
        context.state.addText(new TextGuiElementRenderState(mc.textRenderer, orderedText, new Matrix3x2f(matrices), 0, 0, -1, Integer.MIN_VALUE, false, context.scissorStack.peekLast()));
        matrices.popMatrix();
    }

    public void loadMapParameters(MapUpdateS2CPacket packet) {
        if (this.parameters == null && packet.mapId().equals(DungeonUtil.getMapId())) {
            packet.decorations().ifPresent(decors -> {
                for (MapDecoration decor : decors) {
                    if (this.isMarkerSelf(decor)) {
                        this.parameters = this.getMapParameters().adjustCenter(decor);
                    }
                }
            });
        }
    }

    public void reset() {
        this.parameters = null;
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

        public MapParameters adjustCenter(MapDecoration playerMarker) {
            Vec3d pos = mc.player.getEntityPos();
            byte currentX = this.toMarkerPos(this.toCoordX(pos, this.centerX));
            byte currentZ = this.toMarkerPos(this.toCoordZ(pos, this.centerY));
            if (Utils.difference(currentX, playerMarker.x()) > 4) {
                Utils.infoFormat("map x center {} is off, adjusting to -120", this.centerX);
                this.centerX = -120;
            }
            if (Utils.difference(currentZ, playerMarker.z()) > 4) {
                Utils.infoFormat("map y center {} is off, adjusting to -120", this.centerY);
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
