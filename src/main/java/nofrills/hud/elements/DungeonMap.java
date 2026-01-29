package nofrills.hud.elements;

import com.mojang.blaze3d.textures.GpuTextureView;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.util.Atlases;
import net.minecraft.util.Identifier;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.Utils;
import org.joml.Matrix3x2fStack;

import static nofrills.Main.mc;

public class DungeonMap extends SimpleTextElement {
    private final MapIdComponent mapId = new MapIdComponent(1024);
    private final SpriteAtlasTexture atlasTexture = mc.getAtlasManager().getAtlasTexture(Atlases.MAP_DECORATIONS);

    public DungeonMap() {
        super(Text.empty(), new Feature("dungeonMapElement"), "Dungeon Map Element");
        this.layout.sizing(Sizing.fixed(128), Sizing.fixed(128));
        this.options = this.getBaseSettings();
        this.setDesc("Displays the dungeon map while in Dungeons.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender() || mc.world == null) {
            return;
        } else if (!this.isEditingHud() && !Utils.isInDungeons()) {
            return;
        }
        MapState mapState = mc.world.getMapState(this.mapId);
        if (mapState != null) {
            super.draw(context, mouseX, mouseY, partialTicks, delta);
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            float scale = this.scale.valueFloat();
            if (scale != 1.0f && !this.isEditingHud()) {
                this.applyScaling(context, scale);
            }
            matrices.translate(this.x(), this.y());
            Identifier textureID = mc.getMapTextureManager().getTextureId(mapId, mapState);
            GpuTextureView textureView = mc.getTextureManager().getTexture(textureID).getGlTextureView();
            context.drawTexturedQuad(RenderPipelines.GUI_TEXTURED, textureView, 0, 0, 128, 128, 0.0F, 1.0F, 0.0F, 1.0F, -1);
            for (MapDecoration decor : mapState.getDecorations()) {
                Sprite sprite = this.atlasTexture.getSprite(decor.getAssetId());
                matrices.pushMatrix();
                matrices.translate(decor.x() / 2.0F + 64.0F, decor.z() / 2.0F + 64.0F);
                matrices.rotate((float) (Math.PI / 180.0) * decor.rotation() * 360.0F / 16.0F);
                matrices.scale(6.0f, 6.0f);
                matrices.translate(-0.125F, 0.125F);
                GpuTextureView spriteTextureView = mc.getTextureManager().getTexture(sprite.getAtlasId()).getGlTextureView();
                context.drawTexturedQuad(
                        RenderPipelines.GUI_TEXTURED, spriteTextureView, -1, -1, 1, 1, sprite.getMinU(), sprite.getMaxU(), sprite.getMaxV(), sprite.getMinV(), -1
                );
                matrices.popMatrix();
            }
            matrices.popMatrix();
        }
    }
}
