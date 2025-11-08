package nofrills.features.solvers;

import com.google.common.collect.Sets;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import nofrills.config.Feature;
import nofrills.config.SettingBool;
import nofrills.config.SettingColor;
import nofrills.config.SettingKeybind;
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static nofrills.Main.mc;

public class DianaSolver {
    public static final Feature instance = new Feature("dianaSolver");

    public static final SettingBool guessTracer = new SettingBool(true, "guessTracer", instance);
    public static final SettingColor guessColor = new SettingColor(RenderColor.fromArgb(0xaaffffff), "guessColor", instance);
    public static final SettingColor guessTracerColor = new SettingColor(RenderColor.fromArgb(0xff00ff00), "guessTracerColor", instance);
    public static final SettingColor treasureColor = new SettingColor(RenderColor.fromArgb(0xaaffaa00), "treasureColor", instance);
    public static final SettingColor enemyColor = new SettingColor(RenderColor.fromArgb(0xaaff5555), "enemyColor", instance);
    public static final SettingColor startColor = new SettingColor(RenderColor.fromArgb(0xaa55ff55), "startColor", instance);
    public static final SettingKeybind warpKey = new SettingKeybind(GLFW.GLFW_KEY_UNKNOWN, "warpKey", instance);
    public static final SettingBool hubToggle = new SettingBool(true, "hubToggle", instance);
    public static final SettingBool stonksToggle = new SettingBool(true, "stonksToggle", instance);
    public static final SettingBool museumToggle = new SettingBool(true, "museumToggle", instance);
    public static final SettingBool castleToggle = new SettingBool(false, "castleToggle", instance);
    public static final SettingBool wizardToggle = new SettingBool(true, "wizardToggle", instance);
    public static final SettingBool daToggle = new SettingBool(false, "daToggle", instance);
    public static final SettingBool cryptToggle = new SettingBool(false, "cryptToggle", instance);

    private static final List<DianaWarp> warps = List.of(
            new DianaWarp("Hub", "hub", hubToggle, -3, 70, -70),
            new DianaWarp("Stonks Auction", "stonks", stonksToggle, -53, 72, -53),
            new DianaWarp("Museum", "museum", museumToggle, -76, 76, 80),
            new DianaWarp("Castle", "castle", castleToggle, -250, 130, 45),
            new DianaWarp("Wizard Tower", "wizard", wizardToggle, 42, 122, 69),
            new DianaWarp("Dark Auction", "da", daToggle, 91, 74, 173),
            new DianaWarp("Crypt", "crypt", cryptToggle, -190, 74, -88)
    );
    private static final HashSet<String> spoonDrawer = Sets.newHashSet(
            "ANCESTRAL_SPADE",
            "ARCHAIC_SPADE",
            "DEIFIC_SPADE"
    );
    private static final CurveSolver solver = new CurveSolver();
    private static final List<Burrow> burrowsList = new ArrayList<>();
    private static int ticks = 0;

    private static List<Burrow> getBurrowsList() {
        return new ArrayList<>(burrowsList);
    }

    private static boolean isHoldingSpoon() {
        return spoonDrawer.contains(Utils.getSkyblockId(Utils.getHeldItem()));
    }

    private static void onSpooningStart() {
        solver.resetFitter();
        solver.resetSolvedPos();
        ticks = 10;
    }

    private static DianaWarp findWarp(Vec3d pos) {
        double lowestDist = mc.player.getPos().distanceTo(pos);
        DianaWarp closestWarp = null;
        for (DianaWarp warp : warps) {
            Vec3d warpPos = warp.pos.toCenterPos();
            double warpDist = warpPos.distanceTo(pos);
            if (warp.toggle.value() && warpDist < lowestDist) {
                lowestDist = warpDist;
                closestWarp = warp;
            }
        }
        return closestWarp;
    }

    private static BurrowType getTypeFromPacket(ParticleS2CPacket packet) {
        ParticleType<?> type = packet.getParameters().getType();
        int count = packet.getCount();
        float speed = packet.getSpeed();
        float offsetX = packet.getOffsetX();
        float offsetY = packet.getOffsetY();
        float offsetZ = packet.getOffsetZ();
        if (type.equals(ParticleTypes.DRIPPING_LAVA)) {
            if (count == 2 && speed == -0.5f && offsetX == 0.0f && offsetY == 0.0f && offsetZ == 0.0f) {
                return BurrowType.Guess;
            }
            if (count == 2 && speed == 0.01f && offsetX == 0.35f && offsetY == 0.1f && offsetZ == 0.35f) {
                return BurrowType.Treasure;
            }
        }
        if (type.equals(ParticleTypes.CRIT) && count == 3 && speed == 0.01f && offsetX == 0.5f && offsetY == 0.1f && offsetZ == 0.5f) {
            return BurrowType.Enemy;
        }
        if (type.equals(ParticleTypes.ENCHANTED_HIT) && count == 4 && speed == 0.01f && offsetX == 0.5f && offsetY == 0.1f && offsetZ == 0.5f) {
            return BurrowType.Start;
        }
        return BurrowType.None;
    }

    @EventHandler
    private static void onParticle(SpawnParticleEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub")) {
            BurrowType type = getTypeFromPacket(event.packet);
            if (type.equals(BurrowType.Guess) && ticks > 0 && solver.getLastDist(event.pos) < 4.0) {
                solver.addPos(event.pos);
                ticks = 10;
                Vec3d pos = solver.getSolvedPos();
                if (pos != null) {
                    Burrow guess = new Burrow(pos, BurrowType.Guess);
                    burrowsList.removeIf(Burrow::isGuess);
                    if (getBurrowsList().stream().noneMatch(burrow -> burrow.equals(guess) && !burrow.isGuess())) {
                        burrowsList.add(guess);
                    }
                }
            }
            if (!type.equals(BurrowType.None) && isHoldingSpoon()) {
                BlockPos pos = BlockPos.ofFloored(event.pos.subtract(0, 0.5, 0));
                Burrow nearby = new Burrow(pos, type);
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.GRASS_BLOCK)) {
                    burrowsList.removeIf(burrow -> burrow.equals(nearby) || (burrow.isGuess() && burrow.isNear(nearby)));
                    burrowsList.add(nearby);
                }
            }
        }
    }

    @EventHandler
    private static void onInput(InputEvent event) {
        if (instance.isActive() && mc.currentScreen == null && warpKey.key() == event.key && Utils.isInArea("Hub")) {
            if (event.action == GLFW.GLFW_PRESS) {
                Optional<Burrow> burrow = getBurrowsList().stream().filter(Burrow::isGuess).findFirst();
                if (burrow.isPresent()) {
                    DianaWarp warp = findWarp(burrow.get().getVec());
                    if (warp != null) {
                        Utils.infoFormat("§aWarping to {}.", warp.name);
                        Utils.sendMessage(Utils.format("/warp {}", warp.id));
                    } else {
                        Utils.info("§7No closest warp found for the guess burrow, not warping.");
                    }
                } else {
                    Utils.info("§7No guess burrow exists, not warping.");
                }
            }
            event.cancel();
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub") && isHoldingSpoon() && ticks == 0) {
            onSpooningStart();
        }
    }

    @EventHandler
    private static void onInteractBlock(InteractBlockEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub") && isHoldingSpoon()) {
            burrowsList.removeIf(burrow -> burrow.isGuess() && burrow.pos.equals(event.blockHitResult.getBlockPos()));
        }
    }

    @EventHandler
    private static void onAttackBlock(AttackBlockEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub") && isHoldingSpoon()) {
            burrowsList.removeIf(burrow -> burrow.isGuess() && burrow.pos.equals(event.blockPos));
        }
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub")) {
            if (ticks > 0) {
                ticks--;
                if (ticks == 0) {
                    solver.resetFitter();
                }
            }
            for (Burrow burrow : getBurrowsList()) {
                if (burrow.ticks > 0 && !burrow.isGuess()) {
                    burrow.tick();
                    if (burrow.ticks == 0) {
                        burrowsList.remove(burrow);
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (instance.isActive() && Utils.isInArea("Hub")) {
            for (Burrow burrow : getBurrowsList()) {
                MutableText label = switch (burrow.type) {
                    case Guess -> Text.literal("Guess");
                    case Treasure -> Text.literal("Treasure");
                    case Enemy -> Text.literal("Enemy");
                    case Start -> Text.literal("Start");
                    default -> Text.literal("Unknown");
                };
                Vec3d pos = burrow.getVec();
                if (burrow.type.equals(BurrowType.Guess)) {
                    event.drawBeam(pos, 256, true, guessColor.value());
                    event.drawText(pos, label, 0.1f, true, RenderColor.white);
                    if (guessTracer.value()) {
                        event.drawTracer(pos, guessTracerColor.value());
                    }
                } else {
                    RenderColor color = switch (burrow.type) {
                        case Treasure -> treasureColor.value();
                        case Enemy -> enemyColor.value();
                        case Start -> startColor.value();
                        default -> RenderColor.white;
                    };
                    event.drawBeam(pos, 256, true, color);
                    event.drawText(pos, label, 0.1f, true, RenderColor.white);
                }
            }
        }
    }

    @EventHandler
    private static void onJoin(ServerJoinEvent event) {
        solver.resetFitter();
        solver.resetSolvedPos();
        burrowsList.clear();
        ticks = 0;
    }

    public enum BurrowType {
        Guess,
        Treasure,
        Enemy,
        Start,
        None
    }

    public static class DianaWarp {
        public String name;
        public String id;
        public SettingBool toggle;
        public BlockPos pos;

        public DianaWarp(String name, String id, SettingBool toggle, int x, int y, int z) {
            this.name = name;
            this.id = id;
            this.toggle = toggle;
            this.pos = new BlockPos(x, y, z);
        }
    }

    public static class Burrow {
        public BlockPos pos;
        public BurrowType type;
        public int ticks = 30;

        public Burrow(BlockPos pos, BurrowType type) {
            this.pos = pos;
            this.type = type;
        }

        public Burrow(Vec3d pos, BurrowType type) {
            this(BlockPos.ofFloored(pos), type);
        }

        public void tick() {
            if (this.ticks > 0) this.ticks--;
        }

        public Vec3d getVec() {
            return this.pos.toCenterPos().add(0, 0.5, 0);
        }

        public boolean isGuess() {
            return this.type.equals(BurrowType.Guess);
        }

        public boolean isNear(Burrow other) {
            return this.getVec().distanceTo(other.getVec()) <= 4.0;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Burrow burrow && this.getVec().equals(burrow.getVec());
        }

        @Override
        public int hashCode() {
            return this.getVec().hashCode();
        }
    }
}
