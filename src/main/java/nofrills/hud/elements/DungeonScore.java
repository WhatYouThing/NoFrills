package nofrills.hud.elements;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.text.Text;
import nofrills.config.Feature;
import nofrills.hud.SimpleTextElement;
import nofrills.misc.NoFrillsAPI;
import nofrills.misc.SkyblockData;
import nofrills.misc.Utils;

import java.util.Optional;

public class DungeonScore extends SimpleTextElement {
    private int deaths = 0;
    private boolean bloodDone = false;
    private boolean mimicKilled = false;
    private boolean princeKilled = false;

    public DungeonScore() {
        super(Text.literal("Score: §fN/A"), new Feature("dungeonScoreElement"), "Dungeon Score Element");
        this.options = this.getBaseSettings();
        this.setDesc("Calculates and displays your current dungeon score. Requires connectivity to the NoFrills API.");
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.shouldRender()) {
            return;
        } else if (!this.isEditingHud() && !Utils.isInDungeons()) {
            return;
        }
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }

    public double getClearedPercent() {
        for (String line : SkyblockData.getLines()) {
            if (line.startsWith("Cleared: ")) {
                Optional<Double> value = Utils.parseDouble(this.getLineValue(line));
                if (value.isPresent()) {
                    return value.get() * 0.01;
                }
            }
        }
        return 1.0;
    }

    public double getSecretsFound() {
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Secrets Found: ") && line.endsWith("%")) {
                return Utils.parseDouble(this.getLineValue(line)).orElse(0.0) * 0.01;
            }
        }
        return 0.0;
    }

    public double getSecretsNeeded() {
        for (int i = 1; i <= 5; i++) { // handles F1-F5 since the difference is 10%
            if (Utils.isOnDungeonFloor("F" + i)) {
                return 0.2 + 0.1 * i;
            }
        }
        if (Utils.isOnDungeonFloor("F6")) {
            return 0.85;
        }
        return 1.0;
    }

    public int getClearedRooms() {
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Completed Rooms: ")) {
                return Utils.parseInt(this.getLineValue(line)).orElse(0);
            }
        }
        return 0;
    }

    public int getTotalClearedRooms() {
        int rooms = this.getClearedRooms();
        if (rooms > 0) {
            if (!this.isBloodCleared()) {
                rooms += 1;
            }
            if (!this.isBossCleared()) {
                rooms += 1;
            }
            return rooms;
        }
        return 0;
    }

    public int calcSkillScore() {
        double totalRooms = this.getTotalRooms();
        double clearedRooms = this.getTotalClearedRooms();
        if (totalRooms == 0) return 0;
        int score = Math.min((int) Math.floor(80.0 * clearedRooms / totalRooms), 80);
        return 20 + Math.clamp(score - this.getPuzzlePenalty() - this.getDeathPenalty(), 0, 80);
    }

    public int calcExploreScore() {
        double totalRooms = this.getTotalRooms();
        double clearedRooms = this.getTotalClearedRooms();
        double secretsFound = this.getSecretsFound();
        double secretsNeeded = this.getSecretsNeeded();
        if (totalRooms == 0 || secretsNeeded == 0) return 0;
        int clearScore = Math.min((int) Math.floor(60.0 * clearedRooms / totalRooms), 60);
        int secretScore = Math.min((int) Math.floor(40.0 * secretsFound / secretsNeeded), 40);
        return Math.clamp(clearScore + secretScore, 0, 100);
    }

    public int calcSpeedScore() {
        return 100;
    }

    public int calcBonusScore() {
        int bonus = 0;
        if (this.mimicKilled) {
            bonus += 2;
        }
        if (this.princeKilled) {
            bonus += 1;
        }
        if (NoFrillsAPI.electionPerks.contains("EZPZ")) {
            bonus += 10;
        }
        for (String line : SkyblockData.getTabListLines()) {
            if (line.startsWith("Crypts: ")) {
                bonus += Math.clamp(Utils.parseInt(this.getLineValue(line)).orElse(0), 0, 5);
            }
        }
        return bonus;
    }

    public int getTotalRooms() {
        return (int) Math.round(this.getClearedRooms() / this.getClearedPercent());
    }

    public int getDeathPenalty() {
        if (this.deaths > 0) { // need to assume spirit pet on 1st death, api key application got declined a month after applying
            return this.deaths * 2 - 1;
        }
        return 0;
    }

    public int getPuzzlePenalty() {
        int failed = 0;
        for (String line : SkyblockData.getTabListLines()) {
            if (line.contains(": [✖]") || line.contains(": [✦]")) {
                failed += 1;
            }
        }
        return 10 * failed;
    }

    public String getScoreColor(int score) {
        if (score < 100) return "§c";
        if (score < 160) return "§b";
        if (score < 230) return "§a";
        if (score < 270) return "§d";
        if (score < 300) return "§e";
        return "§e§l";
    }

    public void update() {
        int total = this.calcSkillScore() + this.calcExploreScore() + this.calcSpeedScore() + this.calcBonusScore();
        this.setText(Utils.format("Score: {}{}", this.getScoreColor(total), total));
    }

    public String getLineValue(String line) {
        if (line.contains("%")) {
            line = line.substring(0, line.indexOf("%"));
        }
        return line.substring(line.indexOf(":") + 1).trim();
    }

    public boolean isBloodCleared() {
        return this.bloodDone;
    }

    public boolean isBossCleared() {
        for (int i = 1; i <= 7; i++) {
            if (Utils.isInDungeonBoss(i)) {
                return true;
            }
        }
        return false;
    }

    public void setMimicKilled() {
        this.mimicKilled = true;
    }

    public void setPrinceKilled() {
        this.princeKilled = true;
    }

    public void reset() {
        this.deaths = 0;
        this.bloodDone = false;
        this.mimicKilled = false;
        this.princeKilled = false;
        this.setDefaultText();
    }

    public void onMessage(String message) {
        if (message.equals("[BOSS] The Watcher: You have proven yourself. You may pass.")) {
            this.bloodDone = true;
        } else if (message.trim().startsWith(Utils.Symbols.skull)) {
            if (message.endsWith("ghost.") || message.endsWith("died.")) {
                this.deaths += 1;
            }
        }
    }

    public void onPartyMessage(String message) {
        String msg = Utils.toLower(message);
        if (msg.contains("kill") || msg.contains("dead")) {
            if (msg.contains("mimic")) {
                this.setMimicKilled();
            } else if (msg.contains("prince")) {
                this.setPrinceKilled();
            }
        }
    }
}
