package com.dks.types;

import com.dks.types.config.SimpleStatsConfig;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.Behavior;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.ExtraMenus;
import eu.darkbot.api.extensions.InstructionProvider;
import eu.darkbot.api.managers.AuthAPI;
import eu.darkbot.api.utils.Inject;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;

public abstract class SimpleStats<T extends SimpleStatsConfig> implements Behavior, Configurable<T>, ExtraMenus, InstructionProvider {

    protected final PluginAPI api;

    protected T config;

    protected long lastRefresh;
    protected long lastTick;
    protected boolean isRunning = false;
    protected boolean isFirstTick = true;

    protected Duration runningTime = Duration.ofSeconds(0);
    protected double prevAmount;
    protected double totalCollected = 0;
    protected double collectedPerHour = 0;

    protected final JLabel statusMessage;

    public SimpleStats(PluginAPI api) { this(api, api.requireAPI(AuthAPI.class)); }

    @Inject
    public SimpleStats(PluginAPI api, AuthAPI auth) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.api = api;
        this.statusMessage = new JLabel(this.getStatusMessageForLabel());
    }

    @Override
    public void setConfig(ConfigSetting<T> arg0) {
        this.config = arg0.getValue();
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
        return Arrays.asList(
                this.shouldCreateExtraMenuSeparator() ? createSeparator("DksPlugin") : null,
                create(this.getPopupTitle(), e -> this.showStatusPopup())
        );
    }

    /* ABSTRACT METHODS */
    protected abstract int getCurrentAmount();
    protected abstract String getStatusMessageForLabel();
    protected abstract String getPopupTitle();
    protected abstract boolean shouldCreateExtraMenuSeparator();

    /* LOGIC */
    @Override
    public void onTickBehavior() {
        if (this.isFirstTick) {
            this.initTask();
        }
        if (!this.config.STOP) {
            if (this.isRunning || runIfPicked()) {
                refreshRunningTime();
                if (shouldRefreshData()) {
                    refreshData();
                }
            }
        }
        setStatusMessage();
    }

    protected boolean shouldRefreshData() {
        return (System.currentTimeMillis() - this.lastRefresh) >= (config.REFRESH_RATE_S * 1000L);
    }

    protected void refreshData() {
        int currentAmount = getCurrentAmount();
        if (currentAmount > this.prevAmount) {
            this.totalCollected = this.totalCollected + (currentAmount - this.prevAmount);
        }
        this.prevAmount = currentAmount;
        this.collectedPerHour = (this.totalCollected / ((double) this.runningTime.getSeconds())) * 3600;
        this.lastRefresh = System.currentTimeMillis();
    }

    protected boolean runIfPicked() {
        int currentAmount = getCurrentAmount();
        if (currentAmount != this.prevAmount) {
            if (currentAmount > this.prevAmount) {
                long now = System.currentTimeMillis();
                this.runningTime = Duration.ofSeconds(0);
                this.lastRefresh = now;
                this.lastTick = now;
                this.isRunning = true;
            }
            this.refreshData();
        }
        return this.isRunning;
    }

    protected void initTask() {
        this.isRunning = false;
        setStatusMessage();
        this.prevAmount = getCurrentAmount();
        this.totalCollected = 0;
        this.collectedPerHour = 0;
        this.isFirstTick = false;
    }

    protected void refreshRunningTime() {
        long now = System.currentTimeMillis();
        this.runningTime = this.runningTime.plus(Duration.ofMillis(now - this.lastTick));
        this.lastTick = now;
    }

    /* UI */
    public JComponent beforeConfig() {
        JComponent panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        JButton show = this.getShowButton();
        JButton reset = this.getResetButton();
        panel.add(show).setLocation(0, 0);
        panel.add(reset).setLocation(1, 0);
        return panel;
    }

    protected void setStatusMessage() {
        this.statusMessage.setText(getStatusMessageForLabel());
    }

    protected void showStatusPopup() {
        JOptionPane options = new JOptionPane(this.getExtraMenuPanel());
        options.setOptions(new Object[]{ this.getResetButton(), "OK"});
        Popups.showMessageSync(this.getPopupTitle(), options);
    }

    protected JButton getShowButton() {
        JButton show = new JButton("Show Stats");
        show.addActionListener(e -> this.showStatusPopup());
        return show;
    }

    protected JButton getResetButton() {
        JButton reset = new JButton("Reset Stats");
        reset.addActionListener(e -> this.initTask());
        return reset;
    }

    protected JComponent getExtraMenuPanel() {
        JComponent panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.statusMessage, BorderLayout.CENTER);
        return panel;
    }

    /* FORMATTING */
    protected String formatDuration(Duration duration) {
        long s = Math.abs(duration.getSeconds());
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    protected String dottedDouble(double value) {
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(value).replace(",", ".");
    }
}
