package nofrills.features.farming;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.events.WorldRenderEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static nofrills.Main.mc;

public class PlotBorders {
    public static final Feature instance = new Feature("plotBorders");

    public static final SettingBool infested = new SettingBool(false, "infested", instance.key());
    public static final SettingColor infestedColor = new SettingColor(RenderColor.fromArgb(0xffff5555), "infestedColor", instance.key());
    public static final SettingBool current = new SettingBool(false, "current", instance.key());
    public static final SettingColor currentColor = new SettingColor(RenderColor.fromArgb(0xff55ff55), "currentColor", instance.key());
    public static final SettingBool all = new SettingBool(false, "all", instance.key());
    public static final SettingColor allColor = new SettingColor(RenderColor.fromArgb(0xffffffff), "allColor", instance.key());

    private static final HashMap<String, Plot> plotData = buildPlotList();
    private static HashSet<String> infestedPlots = new HashSet<>();

    private static HashMap<String, Plot> buildPlotList() {
        HashMap<String, Plot> map = new HashMap<>();
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
    }

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
            if (all.value()) {
                for (Map.Entry<String, Plot> entry : plotData.entrySet()) {
                    if (!infestedPlots.contains(entry.getKey())) {
                        event.drawOutline(entry.getValue().boundingBox, true, allColor.value());
                    }
                }
            }
            if (current.value()) { // kinda scuffed but this ensures the rendering order is correct
                for (Map.Entry<String, Plot> entry : plotData.entrySet()) {
                    if (!infestedPlots.contains(entry.getKey()) && entry.getValue().isPlayerAbove()) {
                        event.drawOutline(entry.getValue().boundingBox, true, currentColor.value());
                        break;
                    }
                }
            }
            if (infested.value()) {
                for (String plot : infestedPlots) {
                    event.drawOutline(plotData.get(plot).boundingBox, true, infestedColor.value());
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (instance.isActive() && Utils.isInGarden() && infested.value()) {
            infestedPlots = getInfestedPlots();
        }
    }

    public static class Plot {
        public BlockPos center;
        public Box boundingBox;

        public Plot(int centerX, int centerZ) {
            this.center = new BlockPos(centerX, 66, centerZ);
            this.boundingBox = Box.of(this.center.toCenterPos().add(-0.5, 0.5, -0.5), 96, 0, 96);
        }

        public boolean isPlayerAbove() {
            Vec3d pos = mc.player.getPos();
            if (pos.getY() > 66 && pos.getY() < 142) {
                return pos.getX() > boundingBox.minX && pos.getX() < boundingBox.maxX && pos.getZ() > boundingBox.minZ && pos.getZ() < boundingBox.maxZ;
            }
            return false;
        }
    }
}
