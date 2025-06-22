package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import nofrills.events.*;
import nofrills.misc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.Config;
import static nofrills.Main.mc;

public class SlayerFeatures {
    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
    private static final Pattern chaliceRegex = Pattern.compile("[0-9]*\\.[0-9]*s");
    private static final SlayerBoss[] slayerBosses = {
            new SlayerBoss("Revenant Horror",
                    new String[]{"Revenant Horror", "Atoned Horror"},
                    new EntityType<?>[]{EntityType.ZOMBIE}
            ),
            new SlayerBoss("Tarantula Broodfather",
                    new String[]{"Tarantula Broodfather"},
                    new EntityType<?>[]{EntityType.SPIDER}
            ),
            new SlayerBoss("Sven Packmaster",
                    new String[]{"Sven Packmaster"},
                    new EntityType<?>[]{EntityType.WOLF}
            ),
            new SlayerBoss("Voidgloom Seraph",
                    new String[]{"Voidgloom Seraph"},
                    new EntityType<?>[]{EntityType.ENDERMAN}
            ),
            new SlayerBoss("Riftstalker Bloodfiend",
                    new String[]{"Bloodfiend"},
                    new EntityType<?>[]{EntityType.PLAYER}
            ),
            new SlayerBoss("Inferno Demonlord",
                    new String[]{"Inferno Demonlord", "ⓉⓎⓅⒽⓄⒺⓊⓈ", "ⓆⓊⒶⓏⒾⒾ"},
                    new EntityType<?>[]{EntityType.BLAZE, EntityType.ZOMBIFIED_PIGLIN, EntityType.WITHER_SKELETON}
            ),
    };
    private static final RenderColor ashenColor = RenderColor.fromHex(0x000000);
    private static final RenderColor spiritColor = RenderColor.fromHex(0xffffff);
    private static final RenderColor auricColor = RenderColor.fromHex(0xffff00);
    private static final RenderColor crystalColor = RenderColor.fromHex(0x00ffff);
    private static final RenderColor defaultColor = RenderColor.fromHex(0x00ffff);
    private static final RenderColor hitsColor = RenderColor.fromHex(0xff55ff);
    private static final RenderColor steakColor = RenderColor.fromHex(0xaf00ff);
    private static final EntityCache chaliceData = new EntityCache();
    private static final List<Vec3d> pillarData = new ArrayList<>();
    private static int pillarClearTicks = -1;
    private static CurrentBoss currentBoss = null;
    private static CurrentBoss currentBossPartial = null;
    private static int bossAliveTicks = 0;
    private static boolean springsActive = false;
    private static boolean blockNextUse = false;

    private static boolean isEntityValid(Entity entity, EntityType<?>[] validTypes) {
        if (entity.getType() == EntityType.ARMOR_STAND) {
            return entity.hasCustomName();
        } else {
            for (EntityType<?> type : validTypes) {
                if (entity.getType() == type) {
                    return true;
                }
            }
            return false;
        }
    }

    private static List<Entity> getNearby(Entity ent, EntityType<?>[] validTypes) {
        return Utils.getNearbyEntities(ent, 0.6, 2, 0.6, entity -> isEntityValid(entity, validTypes));
    }

    private static void render(Entity ent, boolean render, RenderColor color) {
        Rendering.Entities.drawOutline(ent, render, color);
        RenderColor copy = RenderColor.fromFloat(color.r, color.g, color.b, 0.33f);
        Rendering.Entities.drawFilled(ent, render, copy);
    }

    private static void renderBlaze(Entity ent, String customName) {
        String name = Formatting.strip(customName);
        if (name.startsWith("IMMUNE")) {
            render(ent, false, defaultColor);
        } else if (name.startsWith("ASHEN")) {
            render(ent, true, ashenColor);
        } else if (name.startsWith("SPIRIT")) {
            render(ent, true, spiritColor);
        } else if (name.startsWith("AURIC")) {
            render(ent, true, auricColor);
        } else if (name.startsWith("CRYSTAL")) {
            render(ent, true, crystalColor);
        } else {
            render(ent, true, defaultColor);
        }
    }

    @EventHandler
    private static void onNamed(EntityNamedEvent event) {
        if (SkyblockData.getLines().contains("Slay the boss!")) {
            if (currentBoss == null) {
                for (SlayerBoss boss : slayerBosses) {
                    for (String line : SkyblockData.getLines()) {
                        if (line.startsWith(boss.scoreboardName)) {
                            String playerName = mc.player.getName().getString(); // should work while nicked
                            if (event.namePlain.equals("Spawned by: " + playerName) && currentBossPartial == null) {
                                currentBossPartial = new CurrentBoss(boss, null, null, event.entity, null);
                            }
                        }
                    }
                }
            } else {
                if (currentBoss.bossData.scoreboardName.equals("Inferno Demonlord")) { // special boss needs special highlighting
                    if (event.namePlain.equals("Spawned by: " + mc.player.getName().getString())) {
                        currentBoss.spawnerEntity = event.entity;
                    }
                    if (Config.slayerHitboxes()) {
                        if (bossTimerRegex.matcher(event.namePlain).matches() && event.entity.distanceTo(mc.player) <= 16) {
                            List<Entity> otherEntities = getNearby(event.entity, currentBoss.bossData.entityTypes);
                            for (Entity ent : otherEntities) {
                                for (EntityType<?> type : currentBoss.bossData.entityTypes) {
                                    if (ent.getType() == type) {
                                        Entity nearest = Utils.findNametagOwner(event.entity, otherEntities);
                                        if (nearest != null) {
                                            renderBlaze(nearest, event.namePlain);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (Config.blazePillarWarn() && !pillarData.isEmpty() && firePillarRegex.matcher(event.namePlain).matches()) {
                        double dist = Utils.horizontalDistance(event.entity.getPos(), pillarData.getLast());
                        if (dist <= 3) {
                            Utils.showTitleCustom("Pillar: " + event.namePlain, 30, 25, 4.0f, 0xffff00);
                            pillarClearTicks = 100;
                        }
                    }
                }
                if (currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend") && Config.vampChalice()) {
                    if (chaliceRegex.matcher(event.namePlain).matches()) {
                        if (event.entity.distanceTo(mc.player) <= 24) {
                            chaliceData.add(event.entity);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onTick(WorldTickEvent event) {
        if (currentBoss == null && currentBossPartial != null) {
            SlayerBoss boss = currentBossPartial.bossData;
            List<Entity> otherEntities = getNearby(currentBossPartial.spawnerEntity, boss.entityTypes);
            if (otherEntities.size() != 4) { // ensures that there are no other entities/bosses near our own boss to correctly locate it
                Entity nameEnt = null;
                Entity statusEnt = null;
                Entity bossEnt = null;
                for (Entity ent : otherEntities) {
                    if (ent.hasCustomName()) {
                        String name = Formatting.strip(ent.getCustomName().getString());
                        if (bossTimerRegex.matcher(name).matches()) {
                            statusEnt = ent;
                        } else {
                            for (String entName : boss.entityNames) {
                                if (name.contains(entName)) {
                                    nameEnt = ent;
                                    break;
                                }
                            }
                        }
                    } else {
                        for (EntityType<?> type : boss.entityTypes) {
                            if (ent.getType() == type) {
                                bossEnt = Utils.findNametagOwner(currentBossPartial.spawnerEntity, otherEntities);
                                break;
                            }
                        }
                    }
                }
                if (nameEnt != null && statusEnt != null && bossEnt != null) {
                    currentBoss = new CurrentBoss(boss, nameEnt, statusEnt, currentBossPartial.spawnerEntity, bossEnt);
                    currentBossPartial = null;
                }
            }
        }
        if (!SkyblockData.getLines().contains("Slay the boss!") && currentBoss != null) { // runs when boss is killed or if we fail the slayer quest
            currentBoss = null;
            chaliceData.clear();
            pillarData.clear();
            pillarClearTicks = -1;
            if (Config.slayerKillTime() && bossAliveTicks > 0) {
                Utils.info(Utils.Symbols.format + "aSlayer boss took " + Utils.formatDecimal(bossAliveTicks / 20.0f) + "s to kill.");
                bossAliveTicks = 0;
            }
            if (Utils.isRenderingCustomTitle()) {
                Utils.showTitleCustom("", 0, 0, 0, 0);
            }
            return;
        }
        if (currentBoss != null) {
            if (pillarClearTicks > 0) {
                pillarClearTicks--;
            } else if (pillarClearTicks == 0) {
                pillarData.clear();
                pillarClearTicks = -1;
            }
            if (Config.slayerHitboxes()) {
                if (!currentBoss.bossData.scoreboardName.equals("Inferno Demonlord") && !Rendering.Entities.isDrawingOutline(currentBoss.bossEntity)) {
                    render(currentBoss.bossEntity, true, defaultColor);
                }
            }
            if (Config.emanHitDisplay() && currentBoss.bossData.scoreboardName.equals("Voidgloom Seraph")) {
                String name = Formatting.strip(currentBoss.nameEntity.getCustomName().getString());
                if (name.endsWith("Hits")) {
                    String[] parts = name.split(" ");
                    Utils.showTitleCustom("Shield: " + parts[parts.length - 2] + " hits", 100, 25, 4.0f, 0xff55ff);
                    if (Config.slayerHitboxes()) {
                        render(currentBoss.bossEntity, true, hitsColor);
                    }
                } else {
                    if (Utils.isRenderingCustomTitle()) {
                        Utils.showTitleCustom("", 0, 0, 0, 0);
                    }
                    if (Config.slayerHitboxes()) {
                        render(currentBoss.bossEntity, true, defaultColor);
                    }
                }
            }
            if (currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend")) {
                String statusName = Formatting.strip(currentBoss.statusEntity.getCustomName().getString());
                String bossName = Formatting.strip(currentBoss.nameEntity.getCustomName().getString());
                if (springsActive && !statusName.contains("KILLER SPRING")) {
                    springsActive = false;
                }
                if (Config.vampIce() && statusName.contains("TWINCLAWS")) {
                    String time = statusName.split("TWINCLAWS")[1].trim().split(" ")[0];
                    Utils.showTitleCustom("Ice: " + time, 1, 25, 4.0f, 0x00ffff);
                }
                if (bossName.contains(Utils.Symbols.vampLow)) {
                    if (Config.vampSteak() && !statusName.contains("TWINCLAWS")) {
                        Utils.showTitleCustom("Steak!", 1, 25, 4.0f, 0xff0000);
                    }
                    if (Config.vampSteakHighlight()) {
                        render(currentBoss.bossEntity, true, steakColor);
                    }
                }
            }
            if (Config.slayerKillTime()) {
                bossAliveTicks++;
            }
        }
    }

    @EventHandler
    private static void onChat(ChatMsgEvent event) {
        if (Config.blazeNoSpam()) {
            String msg = event.getPlainMessage();
            if (msg.equals("Your hit was reduced by Hellion Shield!")) {
                event.cancel();
            }
            if (msg.startsWith("Strike using the") && msg.endsWith("attunement on your dagger!")) {
                event.cancel();
            }
        }
    }

    @EventHandler
    private static void onSound(PlaySoundEvent event) {
        if (Utils.isInChateau()) {
            if (Config.vampManiaSilence() && event.isSound(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE)) {
                if (Config.vampManiaReplace() && currentBoss != null && currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend")) {
                    Utils.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.MASTER, 1, 0);
                }
                event.cancel();
            }
            if (Config.vampSpringSilence() && event.isSound(SoundEvents.ENTITY_WITHER_SPAWN) && currentBoss != null) {
                String statusName = Formatting.strip(currentBoss.statusEntity.getCustomName().getString());
                if (statusName.contains("KILLER SPRING")) {
                    if (Config.vampSpringReplace() && !springsActive) {
                        Utils.playSound(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 1, -1);
                        springsActive = true;
                    }
                    event.cancel();
                }
            }
        }
        if (currentBoss != null) {
            if (Config.blazePillarWarn() && currentBoss.bossData.scoreboardName.equals("Inferno Demonlord")) {
                if (event.isSound(SoundEvents.ENTITY_CHICKEN_EGG)) {
                    Vec3d pos = new Vec3d(event.packet.getX(), event.packet.getY(), event.packet.getZ());
                    if (pillarData.isEmpty()) {
                        double dist = Utils.horizontalDistance(pos, currentBoss.spawnerEntity.getPos());
                        if (dist <= 1.5) {
                            pillarData.add(pos);
                            pillarClearTicks = 100;
                        }
                    } else {
                        double dist = Utils.horizontalDistance(pos, pillarData.getLast());
                        if (dist <= 4) {
                            pillarData.add(pos);
                            pillarClearTicks = 100;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private static void onRender(WorldRenderEvent event) {
        if (currentBoss != null) {
            if (currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend") && Config.vampChalice()) {
                for (Entity ent : chaliceData.get()) {
                    BlockPos blockPos = Utils.findGround(ent.getBlockPos(), 4);
                    Vec3d pos = ent.getPos();
                    Vec3d posAdjust = new Vec3d(pos.x, blockPos.up(1).getY() + 0.5, pos.z);
                    event.drawFilledWithBeam(Box.of(posAdjust, 1, 1.25, 1), 256, true, RenderColor.fromColor(Config.vampChaliceColor()));
                }
            }
        }
    }

    @EventHandler
    private static void onUseBlock(InteractBlockEvent event) {
        if (Config.blazeDaggerFix() && Utils.getRightClickAbility(Utils.getHeldItem()).contains("Attunement")) {
            blockNextUse = true;
        }
    }

    @EventHandler
    private static void onUseItem(InteractItemEvent event) {
        if (blockNextUse) {
            blockNextUse = false;
            event.cancel();
        }
    }

    private static class SlayerBoss {
        public String scoreboardName;
        public String[] entityNames;
        public EntityType<?>[] entityTypes;

        public SlayerBoss(String scoreboardName, String[] entityNames, EntityType<?>[] entityTypes) {
            this.scoreboardName = scoreboardName;
            this.entityNames = entityNames;
            this.entityTypes = entityTypes;
        }
    }

    private static class CurrentBoss {
        public SlayerBoss bossData;
        public Entity nameEntity;
        public Entity statusEntity;
        public Entity spawnerEntity;
        public Entity bossEntity;

        public CurrentBoss(SlayerBoss bossData, Entity nameEntity, Entity statusEntity, Entity spawnerEntity, Entity bossEntity) {
            this.bossData = bossData;
            this.nameEntity = nameEntity;
            this.statusEntity = statusEntity;
            this.spawnerEntity = spawnerEntity;
            this.bossEntity = bossEntity;
        }
    }
}
