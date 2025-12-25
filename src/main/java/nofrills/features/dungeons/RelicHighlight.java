package nofrills.features.dungeons;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import nofrills.config.Feature;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.DungeonUtil;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import static nofrills.Main.mc;

public class RelicHighlight {
    public static final Feature instance = new Feature("relicHighlight");

    private static final Relic green = new Relic(49, 7, 44, RenderColor.fromHex(0x00ff00));
    private static final Relic red = new Relic(51, 7, 42, RenderColor.fromHex(0xff0000));
    private static final Relic purple = new Relic(54, 7, 41, RenderColor.fromHex(0xaa00aa));
    private static final Relic orange = new Relic(57, 7, 42, RenderColor.fromHex(0xffaa00));
    private static final Relic blue = new Relic(59, 7, 44, RenderColor.fromHex(0x55ffff));

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && DungeonUtil.isInDragonPhase() && mc.player != null) {
            ItemStack stack = mc.player.getInventory().getStack(8);
            if (!stack.isEmpty() && stack.getItem().equals(Items.PLAYER_HEAD)) {
                String name = Utils.toPlain(stack.getName());
                Relic relic = switch (name) {
                    case "Corrupted Green Relic" -> green;
                    case "Corrupted Red Relic" -> red;
                    case "Corrupted Purple Relic" -> purple;
                    case "Corrupted Orange Relic" -> orange;
                    case "Corrupted Blue Relic" -> blue;
                    default -> null;
                };
                if (relic != null) event.drawFilled(relic.box, false, relic.color);
            }
        }
    }

    public static class Relic {
        public Box box;
        public RenderColor color;

        public Relic(int x, int y, int z, RenderColor color) {
            BlockPos pos = new BlockPos(x, y, z);
            this.box = Box.enclosing(pos, pos);
            this.color = color;
        }
    }
}
