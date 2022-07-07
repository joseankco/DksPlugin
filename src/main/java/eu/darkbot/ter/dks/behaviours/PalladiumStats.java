package eu.darkbot.ter.dks.behaviours;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.ter.dks.api.HangarAPI;
import eu.darkbot.ter.dks.api.hangar.LocalizationID;
import eu.darkbot.ter.dks.types.SimpleStats;
import eu.darkbot.ter.dks.types.config.PalladiumStatsConfig;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.ter.dks.types.utils.Formatter;

import java.util.Objects;

@Feature(name = "Palladium Stats", description = "Dummy Palladium Stats")
public class PalladiumStats extends SimpleStats<PalladiumStatsConfig> implements Behavior, Configurable<PalladiumStatsConfig>, ExtraMenus, InstructionProvider {

    protected OreAPI ore;
    protected boolean isRealZeroAmount = false;

    public PalladiumStats(PluginAPI api, AuthAPI auth, I18nAPI i18n, ExtensionsAPI extensions, OreAPI ore, Main main) {
        super(api, auth, i18n, extensions, main);
        this.ore = ore;
        this.statusMessage.setText(this.getStatusMessageForLabel());
        System.out.println(this.hangar);
    }

    @Override
    protected Integer getCurrentAmount() {
        int unestable = this.ore.getAmount(OreAPI.Ore.PALLADIUM);
        if (unestable == 0) {
            if (!this.isRealZeroAmount) {
                boolean success = this.hangar.updateCurrentHangar();
                Integer stable = null;
                if (success) {
                    Integer amount = this.hangar.getItem(LocalizationID.ORE_PALLADIUM).getQuantity();
                    stable = amount == null ? 0 : amount;
                    this.isRealZeroAmount = stable == 0;
                }
                return stable;
            } else {
                return unestable;
            }
        } else {
            this.isRealZeroAmount = false;
            return unestable;
        }
    }

    @Override
    protected String getPopupTitle() {
        return "Palladium Stats";
    }

    @Override
    protected boolean shouldCreateExtraMenuSeparator() {
        return !Objects.requireNonNull(this.main.featureRegistry.getFeatureDefinition("eu.darkbot.ter.dks.tasks.RemoteStats")).isEnabled();
    }

    @Override
    protected String getStatusMessageForLabel() {
        String status = this.isFirstTick ?
                this.i18n.get(this.plugin, "palladium_stats.status.initializing") :
                this.config.getStop() ?
                    this.i18n.get(this.plugin, "palladium_stats.status.stopped") :
                    this.i18n.get(this.plugin, "palladium_stats.status.waiting");
        String time = "-";
        String collected = "-";
        String hour = "-";
        String ee = "-";

        if (this.isRunning) {
            status = this.config.STOP ?
                    this.i18n.get(this.plugin, "palladium_stats.status.stopped") :
                    this.i18n.get(this.plugin, "palladium_stats.status.running");
            time = Formatter.formatDuration(this.runningTime);
            collected = Formatter.formatDoubleDots(this.totalCollected);
            hour = Formatter.formatDoubleDots(this.collectedPerHour);
            ee = "~" + Formatter.formatDoubleDots(this.collectedPerHour / 15D);
        }

        String message = Formatter.formatHtmlTag(
            Formatter.formatBoldTag(this.i18n.get(this.plugin, "palladium_stats.status") + ": ") + "%s<br>" +
            Formatter.formatBoldTag(this.i18n.get(this.plugin, "palladium_stats.status.running_time") + ": ") + "%s<br>" +
            Formatter.formatBoldTag(this.i18n.get(this.plugin, "palladium_stats.status.collected") + ": ") + "%s<br>" +
            Formatter.formatBoldTag(this.i18n.get(this.plugin, "palladium_stats.status.collected_hour") + ": ") + "%s<br>" +
            Formatter.formatBoldTag(this.i18n.get(this.plugin, "palladium_stats.status.ee_hour") + ": ") + "%s<br>"
        );

        return String.format(message, status, time, collected, hour, ee);
    }
}
