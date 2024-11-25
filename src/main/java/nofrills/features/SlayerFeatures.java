package nofrills.features;

import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import nofrills.config.Config;
import nofrills.events.ChatMsgEvent;
import nofrills.events.EntityNamedEvent;
import nofrills.events.ReceivePacketEvent;
import nofrills.events.WorldTickEvent;
import nofrills.misc.RenderColor;
import nofrills.misc.Rendering;
import nofrills.misc.Utils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import static nofrills.Main.mc;

public class SlayerFeatures {
    private static final Pattern firePillarRegex = Pattern.compile("[0-9]s [0-9] hits");
    private static final Pattern bossTimerRegex = Pattern.compile(".*[0-9][0-9]:[0-9][0-9].*");
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
    private static final DecimalFormat killTimeFormat = new DecimalFormat("0.##");
    private static CurrentBoss currentBoss = null;
    private static int bossAliveTicks = 0;
    private static boolean springsActive = false;

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

    private static void render(Entity ent, boolean render, int r, int g, int b, int a) {
        Rendering.Entities.drawOutline(ent, render, new RenderColor(r, g, b, a));
        Rendering.Entities.drawFilled(ent, render, new RenderColor(r, g, b, 85));
    }

    private static void renderBlaze(Entity ent, String customName) {
        String name = Formatting.strip(customName);
        if (name.startsWith("IMMUNE")) {
            render(ent, false, 0, 0, 0, 0);
        } else if (name.startsWith("ASHEN")) {
            render(ent, true, 0, 0, 0, 255);
        } else if (name.startsWith("SPIRIT")) {
            render(ent, true, 255, 255, 255, 255);
        } else if (name.startsWith("AURIC")) {
            render(ent, true, 255, 255, 0, 255);
        } else if (name.startsWith("CRYSTAL")) {
            render(ent, true, 0, 255, 255, 255);
        } else {
            render(ent, true, 0, 255, 255, 255);
        }
    }

    @EventHandler
    public static void onNamed(EntityNamedEvent event) {
        if (Utils.scoreboardLines.contains("Slay the boss!")) {
            if (currentBoss == null) {
                for (SlayerBoss boss : slayerBosses) {
                    for (String line : Utils.scoreboardLines) {
                        if (line.startsWith(boss.scoreboardName)) {
                            String playerName = mc.player.getName().getString();
                            if (event.namePlain.equals("Spawned by: " + playerName)) {
                                List<Entity> otherEntities = getNearby(event.entity, boss.entityTypes);
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
                                                bossEnt = Utils.findNametagOwner(event.entity, otherEntities);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (nameEnt != null && statusEnt != null && bossEnt != null) {
                                    currentBoss = new CurrentBoss(boss, nameEnt, statusEnt, event.entity, bossEnt);
                                }
                            }
                        }
                    }
                }
            } else {
                if (currentBoss.bossData.scoreboardName.equals("Inferno Demonlord") && Config.slayerHitboxes) { // special boss needs special highlighting
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
                    if (Config.slayerBlazePillarWarn && firePillarRegex.matcher(event.namePlain).matches() && event.entity.distanceTo(mc.player) <= 16) {
                        Utils.showTitleCustom("Pillar: " + event.namePlain, 30, 25, 4.0f, 0xffff00);
                    }
                }
            }
        }
    }

    @EventHandler
    public static void onTick(WorldTickEvent event) {
        if (!Utils.scoreboardLines.contains("Slay the boss!")) {
            currentBoss = null;
            if (Config.slayerKillTime && bossAliveTicks > 0) {
                Utils.info(Utils.Symbols.format + "aSlayer boss took " + killTimeFormat.format(bossAliveTicks / 20.0f) + "s to kill.");
                bossAliveTicks = 0;
            }
            if (Utils.isRenderingCustomTitle()) {
                Utils.showTitleCustom("", 0, 0, 0, 0);
            }
        }
        if (currentBoss != null) {
            if (Config.slayerHitboxes) {
                if (!currentBoss.bossData.scoreboardName.equals("Inferno Demonlord") && !Rendering.Entities.isDrawingOutline(currentBoss.bossEntity)) {
                    render(currentBoss.bossEntity, true, 0, 255, 255, 255);
                }
            }
            if (Config.slayerEmanHitDisplay && currentBoss.bossData.scoreboardName.equals("Voidgloom Seraph")) {
                String name = Formatting.strip(currentBoss.nameEntity.getCustomName().getString());
                if (name.endsWith("Hits")) {
                    String[] parts = name.split(" ");
                    Utils.showTitleCustom("Shield: " + parts[parts.length - 2] + " hits", 100, 25, 4.0f, 0xff55ff);
                } else {
                    if (Utils.isRenderingCustomTitle()) {
                        Utils.showTitleCustom("", 0, 0, 0, 0);
                    }
                }
            }
            if (currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend")) {
                String statusName = Formatting.strip(currentBoss.statusEntity.getCustomName().getString());
                String bossName = Formatting.strip(currentBoss.nameEntity.getCustomName().getString());
                if (springsActive && !statusName.contains("KILLER SPRING")) {
                    springsActive = false;
                }
                if (Config.slayerVampIndicatorIce && statusName.contains("TWINCLAWS")) {
                    String time = statusName.split("TWINCLAWS")[1].trim().split(" ")[0];
                    Utils.showTitleCustom("Ice: " + time, 1, 25, 4.0f, 0x00ffff);
                } else if (Config.slayerVampIndicatorSteak && bossName.contains("҉")) {
                    Utils.showTitleCustom("Steak!", 1, 25, 4.0f, 0xff0000);
                }
            }
            if (Config.slayerKillTime) {
                bossAliveTicks++;
            }
        }
    }

    @EventHandler
    public static void onChat(ChatMsgEvent event) {
        if (Config.slayerBlazeNoSpam) {
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
    private static void onPacket(ReceivePacketEvent event) {
        if (event.packet instanceof PlaySoundS2CPacket soundPacket) {
            if (Utils.isInChateau()) {
                String soundName = soundPacket.getSound().value().getId().toString();
                if (Config.slayerVampManiaSilence && soundName.equalsIgnoreCase("minecraft:entity.elder_guardian.curse")) {
                    if (Config.slayerVampManiaReplace && currentBoss != null && currentBoss.bossData.scoreboardName.equals("Riftstalker Bloodfiend")) {
                        Utils.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.MASTER, 1, 0);
                    }
                    event.cancel();
                }
                if (Config.slayerVampSpringSilence && soundName.equalsIgnoreCase("minecraft:entity.wither.spawn") && currentBoss != null) {
                    String statusName = Formatting.strip(currentBoss.statusEntity.getCustomName().getString());
                    if (statusName.contains("KILLER SPRING")) {
                        if (Config.slayerVampSpringReplace && !springsActive) {
                            Utils.playSound(SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.MASTER, 1, -1);
                            springsActive = true;
                        }
                        event.cancel();
                    }
                }
            }
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
