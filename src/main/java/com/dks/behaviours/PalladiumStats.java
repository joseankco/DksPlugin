package com.dks.behaviours;

import com.dks.types.SimpleStats;
import com.dks.types.config.PalladiumStatsConfig;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.api.utils.Inject;

@Feature(name = "Palladium Stats", description = "Dummy Palladium Stats")
public class PalladiumStats extends SimpleStats<PalladiumStatsConfig> implements Behavior, Configurable<PalladiumStatsConfig>, ExtraMenus, InstructionProvider {

    protected OreAPI ore;

    @Inject
    public PalladiumStats(PluginAPI api, AuthAPI auth) {
        super(api, auth);
        this.ore = api.getAPI(OreAPI.class);
        this.statusMessage.setText(this.getStatusMessageForLabel());
    }

    @Override
    protected int getCurrentAmount() {
        return this.ore.getAmount(OreAPI.Ore.PALLADIUM);
    }

    @Override
    protected String getPopupTitle() {
        return "Palladium Stats";
    }

    @Override
    protected boolean shouldCreateExtraMenuSeparator() {
        return true;
    }

    @Override
    protected String getStatusMessageForLabel() {
        String status = this.isFirstTick ? "task ready" : "waiting to collect palladium";
        String time = "-";
        String collected = "-";
        String hour = "-";
        String ee = "-";

        if (this.isRunning) {
            status = this.config.STOP ? "stopped" : "running";
            time = formatDuration(this.runningTime);
            collected = this.dottedDouble(this.totalCollected);
            hour = this.dottedDouble(this.collectedPerHour);
            ee = "~" + this.dottedDouble(this.collectedPerHour / 15D);
        }

        String message = "<html><b>status: </b>%s<br>" +
                "<b>running time: </b>%s<br>" +
                "<b>palladium collected: </b>%s<br>" +
                "<b>palladium/h: </b>%s<br>" +
                "<b>ee/h: </b>%s<br></html>";

        return String.format(message, status, time, collected, hour, ee);
    }
}
