package eu.darkbot.ter.dks.types;

import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.ExtensionsAPI;
import eu.darkbot.api.managers.I18nAPI;
import eu.darkbot.ter.dks.types.config.SimpleStatsConfig;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.managers.AuthAPI;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public abstract class SimpleStats<T extends SimpleStatsConfig> implements Behavior, Configurable<T>, ExtraMenus, InstructionProvider {

    protected final PluginAPI api;
    protected final I18nAPI i18n;
    protected final ExtensionsAPI extensions;
    protected final PluginInfo plugin;

    protected T config;

    protected long lastRefresh;
    protected long lastTick;
    protected boolean isRunning = false;
    protected boolean isFirstTick = true;
    protected boolean isStoppedTick = false;

    protected Duration runningTime = Duration.ofSeconds(0);
    protected double prevAmount;
    protected double totalCollected = 0;
    protected double collectedPerHour = 0;

    protected final JLabel statusMessage;

    public SimpleStats(PluginAPI api, AuthAPI auth, I18nAPI i18n, ExtensionsAPI extensions) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.api = api;
        this.i18n = i18n;
        this.extensions = extensions;
        this.plugin = Objects.requireNonNull(this.extensions.getFeatureInfo(getClass())).getPluginInfo();

        this.statusMessage = new JLabel(this.getStatusMessageForLabel());
    }

    @Override
    public void setConfig(ConfigSetting<T> arg0) {
        this.config = arg0.getValue();
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI api) {
        return Arrays.asList(
                this.shouldCreateExtraMenuSeparator() ? createSeparator(this.i18n.get(this.plugin, "plugin.separator")) : null,
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
        if (!this.isStoppedTick) {
            if (!this.config.getStop()) {
                if (this.isRunning || runIfPicked()) {
                    refreshRunningTime();
                    if (shouldRefreshData()) {
                        refreshData();
                    }
                }
            }
        } else {
            this.isStoppedTick = false;
        }
        setStatusMessage();
    }

    @Override
    public void onStoppedBehavior() {
        this.isStoppedTick = true;
        this.lastTick = System.currentTimeMillis();
    }

    protected boolean shouldRefreshData() {
        return (System.currentTimeMillis() - this.lastRefresh) >= (config.getRefreshRateSec() * 1000L);
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
        this.prevAmount = this.isStoppedTick ? Integer.MAX_VALUE : this.getCurrentAmount();
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
        JButton show = new JButton(this.i18n.get(this.plugin, "buttons.show_stats"));
        show.addActionListener(e -> this.showStatusPopup());
        return show;
    }

    protected JButton getResetButton() {
        JButton reset = new JButton(this.i18n.get(this.plugin, "buttons.reset_stats"));
        reset.addActionListener(e -> this.initTask());
        return reset;
    }

    protected JComponent getExtraMenuPanel() {
        JComponent panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(this.statusMessage, BorderLayout.CENTER);
        return panel;
    }
}