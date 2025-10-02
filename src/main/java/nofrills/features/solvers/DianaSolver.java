package nofrills.features.solvers;

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
import nofrills.events.*;
import nofrills.misc.CurveSolver;
import nofrills.misc.RenderColor;
import nofrills.misc.Utils;

import java.util.ArrayList;
import java.util.List;

import static nofrills.Main.mc;

public class DianaSolver {
    public static final Feature instance = new Feature("dianaSolver");

    public static final SettingBool guessTracer = new SettingBool(true, "guessTracer", instance);
    public static final SettingColor guessColor = new SettingColor(RenderColor.fromArgb(0xaaffffff), "guessColor", instance);
    public static final SettingColor guessTracerColor = new SettingColor(RenderColor.fromArgb(0xffffffff), "guessTracerColor", instance);
    public static final SettingColor treasureColor = new SettingColor(RenderColor.fromArgb(0xaaffaa00), "treasureColor", instance);
    public static final SettingColor enemyColor = new SettingColor(RenderColor.fromArgb(0xaaff5555), "enemyColor", instance);
    public static final SettingColor startColor = new SettingColor(RenderColor.fromArgb(0xaa55ff55), "startColor", instance);

    private static final CurveSolver solver = new CurveSolver();
    private static final List<Burrow> burrowsList = new ArrayList<>();
    private static int ticks = 0;

    private static List<Burrow> getBurrowsList() {
        return new ArrayList<>(burrowsList);
    }

    private static boolean isHoldingSpoon() {
        return Utils.getSkyblockId(Utils.getHeldItem()).equals("ANCESTRAL_SPADE");
    }

    private static void onSpooningStart() {
        solver.resetFitter();
        solver.resetSolvedPos();
        ticks = 20;
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
        if (instance.isActive()) {
            BurrowType type = getTypeFromPacket(event.packet);
            if (type.equals(BurrowType.Guess)) {
                if (ticks > 0 && solver.getLastDist(event.pos) <= 3.0) {
                    solver.addPos(event.pos);
                    ticks = 20;
                    if (solver.getSolvedPos() != null) {
                        Burrow guess = new Burrow(solver.getSolvedPos(), BurrowType.Guess);
                        burrowsList.removeIf(burrow -> burrow.type.equals(BurrowType.Guess) || burrow.equals(guess));
                        burrowsList.add(guess);
                    }
                }
            } else if (!type.equals(BurrowType.None) && isHoldingSpoon()) {
                BlockPos pos = BlockPos.ofFloored(event.pos.subtract(0, 0.5, 0));
                Burrow nearby = new Burrow(pos, type);
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.GRASS_BLOCK)) {
                    for (Burrow burrow : getBurrowsList()) {
                        if (burrow.type.equals(BurrowType.Guess)) {
                            if (burrow.getVec().distanceTo(nearby.getVec()) <= 4.0) {
                                burrowsList.remove(burrow);
                                break;
                            }
                        } else {
                            if (burrow.equals(nearby)) {
                                return;
                            }
                        }
                    }
                    burrowsList.add(nearby);
                }
            }
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (instance.isActive() && isHoldingSpoon()) {
            onSpooningStart();
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (instance.isActive() && isHoldingSpoon()) {
            Burrow target = new Burrow(event.blockHitResult.getBlockPos(), BurrowType.None);
            List<Burrow> burrowsList = getBurrowsList();
            for (Burrow burrow : burrowsList) {
                if (burrow.equals(target)) {
                    burrow.startTicking();
                    return;
                }
            }
            onSpooningStart();
        }
    }

    @EventHandler
    private static void onAttackBlock(AttackBlockEvent event) {
        if (instance.isActive() && isHoldingSpoon()) {
            Burrow target = new Burrow(event.blockHitResult.getBlockPos(), BurrowType.None);
            for (Burrow burrow : getBurrowsList()) {
                if (burrow.equals(target)) {
                    burrow.startTicking();
                    return;
                }
            }
        }
    }

    @EventHandler
    private static void onTick(ServerTickEvent event) {
        if (!instance.isActive()) return;
        if (ticks > 0) {
            ticks--;
            if (ticks == 0) {
                solver.resetFitter();
            }
        }
        for (Burrow burrow : getBurrowsList()) {
            if (burrow.ticks > 0) {
                burrow.tick();
                if (burrow.ticks == 0) {
                    burrowsList.remove(burrow);
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (!instance.isActive()) return;
        for (Burrow burrow : getBurrowsList()) {
            if (burrow.ticks > 0) {
                continue;
            }
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

    public static class Burrow {
        public BlockPos pos;
        public BurrowType type;
        public int ticks = 0;

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

        public void startTicking() {
            this.ticks = 20;
        }

        public Vec3d getVec() {
            return this.pos.toCenterPos().add(0, 0.5, 0);
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
