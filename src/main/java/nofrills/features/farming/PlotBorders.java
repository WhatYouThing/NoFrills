package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.EventListener;
import nofrills.events.WorldRenderEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static nofrills.Main.mc;

@EventListener
public class PlotBorders {
    public static final Feature instance = new Feature("plotBorders");

    public static final SettingBool infested = new SettingBool(false, "infested", instance.key());
    public static final SettingColor infestedColor = new SettingColor(RenderColor.fromArgb(0xffff5555), "infestedColor", instance.key());
    public static final SettingBool current = new SettingBool(false, "current", instance.key());
    public static final SettingColor currentColor = new SettingColor(RenderColor.fromArgb(0xff55ff55), "currentColor", instance.key());
    public static final SettingBool all = new SettingBool(false, "all", instance.key());
    public static final SettingColor allColor = new SettingColor(RenderColor.fromArgb(0xffffffff), "allColor", instance.key());

    private static final Supplier<HashMap<String, Plot>> plotData = () -> {
        HashMap<String, Plot> map = new HashMap<>();
        map.put("0", new Plot(0, 0));
        map.put("1", new Plot(0, -96));
        map.put("2", new Plot(-96, 0));
        map.put("3", new Plot(96, 0));
        map.put("4", new Plot(0, 96));
        map.put("5", new Plot(-96, -96));
        map.put("6", new Plot(96, -96));
        map.put("7", new Plot(-96, 96));
        map.put("8", new Plot(96, 96));
        map.put("9", new Plot(0, -192));
        map.put("10", new Plot(-192, 0));
        map.put("11", new Plot(192, 0));
        map.put("12", new Plot(0, 192));
        map.put("13", new Plot(-96, -192));
        map.put("14", new Plot(96, -192));
        map.put("15", new Plot(-192, -96));
        map.put("16", new Plot(192, -96));
        map.put("17", new Plot(-192, 96));
        map.put("18", new Plot(192, 96));
        map.put("19", new Plot(-96, 192));
        map.put("20", new Plot(96, 192));
        map.put("21", new Plot(-192, -192));
        map.put("22", new Plot(192, -192));
        map.put("23", new Plot(-192, 192));
        map.put("24", new Plot(192, 192));
        return map;
    };

    private static HashSet<String> getInfestedPlots() {
        for (String line : Utils.getTabListLines()) {
            if (line.startsWith("Plots: ")) {
                String[] plots = line.substring(line.indexOf(":") + 1).split(",");
                HashSet<String> set = new HashSet<>();
                for (String plot : plots) {
                    set.add(plot.trim());
                }
                return set;
            }
        }
        return new HashSet<>();
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInGarden()) {
            HashSet<String> infestedPlots = getInfestedPlots();
            HashMap<Border, BorderType> queue = new HashMap<>();
            if (infested.value()) {
                infestedPlots.forEach(plot -> {
                    if (plotData.get().containsKey(plot)) {
                        plotData.get().get(plot).submitBorders(queue, BorderType.Infested);
                    }
                });
            }
            if (current.value()) {
                plotData.get().entrySet().stream()
                        .filter(e -> !infestedPlots.contains(e.getKey()) && e.getValue().isPlayerAbove())
                        .forEach(e -> e.getValue().submitBorders(queue, BorderType.Current));
            }
            if (all.value()) {
                plotData.get().entrySet().stream()
                        .filter(e -> !infestedPlots.contains(e.getKey()))
                        .forEach(e -> e.getValue().submitBorders(queue, BorderType.All));
            }
            queue.forEach((key, value) -> {
                RenderColor color = switch (value) {
                    case Infested -> infestedColor.value();
                    case Current -> currentColor.value();
                    case All -> allColor.value();
                };
                event.drawLine(key.x, key.y, 5.0, true, color);
            });
        }
    }

    public enum BorderType {
        Infested,
        Current,
        All
    }

    public static class Plot {
        public BlockPos center;
        public AABB boundingBox;

        public Plot(int centerX, int centerZ) {
            this.center = new BlockPos(centerX, 66, centerZ);
            this.boundingBox = AABB.ofSize(Vec3.atCenterOf(this.center).add(-0.5, 0.5, -0.5), 96, 0, 96);
        }

        public void submitBorders(HashMap<Border, BorderType> map, BorderType type) {
            AABB box = this.boundingBox;
            List<Border> borders = List.of(
                    new Border(box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ),
                    new Border(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ),
                    new Border(box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ),
                    new Border(box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ)
            );
            for (Border border : borders) {
                if (!map.containsKey(border)) {
                    map.put(border, type);
                }
            }
        }

        public boolean isPlayerAbove() {
            Vec3 pos = mc.player.position();
            if (pos.y() > 66 && pos.y() < 142) {
                return pos.x() > boundingBox.minX && pos.x() < boundingBox.maxX && pos.z() > boundingBox.minZ && pos.z() < boundingBox.maxZ;
            }
            return false;
        }
    }

    public static class Border {
        public final Vec3 x;
        public final Vec3 y;

        public Border(double x1, double y1, double z1, double x2, double y2, double z2) {
            this.x = new Vec3(x1, y1, z1);
            this.y = new Vec3(x2, y2, z2);
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Border border) {
                return (this.x.equals(border.x) && this.y.equals(border.y)) || (this.x.equals(border.y) && this.y.equals(border.x));
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.x.hashCode() + this.y.hashCode();
        }
    }
}
