package eu.darkbot.ter.dks.tasks;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.ter.dks.types.VerifierChecker;
import eu.darkbot.ter.dks.types.config.LiveLogsConfig;
import eu.darkbot.ter.dks.types.utils.Formatter;
import eu.darkbot.ter.dks.types.utils.console.ConsoleOutputCapturer;
import eu.darkbot.ter.dks.types.utils.console.ConsoleOutputCapturerSingleton;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Feature(name = "Live Logs Viewer", description = "Allows you to monitor DarkBot logs in real time")
public class LiveLogs implements Task, ExtraMenus, Configurable<LiveLogsConfig> {

    protected final I18nAPI i18n;
    protected final ExtensionsAPI extensions;
    protected final PluginInfo plugin;
    protected final Main main;
    protected LiveLogsConfig config;

    protected final ConsoleOutputCapturer capturer;
    protected JScrollPane scrollSysOut;
    protected JTextArea textSysOut;

    protected JScrollPane scrollSysErr;
    protected JTextArea textSysErr;
    protected JTabbedPane tabs;

    protected JLabel status;

    public LiveLogs(AuthAPI auth, I18nAPI i18n, ExtensionsAPI extensions, Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.i18n = i18n;
        this.extensions = extensions;
        this.plugin = Objects.requireNonNull(this.extensions.getFeatureInfo(getClass())).getPluginInfo();
        this.main = main;

        this.capturer = ConsoleOutputCapturerSingleton.getCapturer();
        this.capturer.setMaxLinesStd(100);
        this.capturer.setMaxLinesErr(100);
        this.initCapturers();

        // TODO: botones para closear los stream
        // Usar isEnabled FeatureRegistry para ver si esta habilitado otro capturer y si es diferente de SysOut reestablecer a SysOut
    }

    public void initCapturers() {
        this.status = new JLabel();
        this.tabs = new JTabbedPane();

        this.capturer.start();

        this.textSysOut = this.getTextArea(50, 15,false);
        this.scrollSysOut = new JScrollPane(this.textSysOut);

        this.textSysErr = this.getTextArea(50, 15, true);
        this.scrollSysErr = new JScrollPane(this.textSysErr);

        this.tabs.add(this.i18n.get(this.plugin, "live_logs.lines.std"), this.scrollSysOut);
        this.tabs.add(this.i18n.get(this.plugin, "live_logs.lines.err"), this.scrollSysErr);
    }

    public JTextArea getTextArea(int cols, int rows, boolean isError) {
        JTextArea textArea = new JTextArea();
        textArea.setColumns(cols);
        textArea.setRows(rows);
        if (isError) {
            textArea.setForeground(Color.RED);
            textArea.setDisabledTextColor(Color.RED);
        }
        textArea.setEnabled(false);
        return textArea;
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI pluginAPI) {
        Collection<JComponent> components = new ArrayList<>();
        if (this.shouldCreateExtraMenuSeparator()) {
            components.add(createSeparator(this.i18n.get(this.plugin, "plugin.separator")));
        }
        components.add(create(this.getPopupTitle(), e -> this.showPopup()));
        return components;
    }

    public String getPopupTitle() {
        return "Live Logs Viewer";
    }

    public void showPopup() {
        Object[] objects = new Object[] { status, tabs };
        JOptionPane options = new JOptionPane(objects);

        JButton clear = new JButton(this.i18n.get(this.plugin, "live_logs.buttons.clear"));
        clear.addActionListener(e -> {
            this.clearCaptures();
        });

        JButton startStop = new JButton(this.capturer.isCapturing() ? "Stop" : "Start");
        startStop.addActionListener(e -> {
            if (this.capturer.isCapturing()) {
                this.capturer.stop();
                startStop.setText("Start");
            } else {
                this.capturer.start();
                startStop.setText("Stop");
            }
        });

        options.setOptions(new Object[] { startStop, clear, "OK" });
        Popups.showMessageSync(this.getPopupTitle(), options);
    }

    public boolean shouldCreateExtraMenuSeparator() {
        return !Objects.requireNonNull(this.main.featureRegistry.getFeatureDefinition("eu.darkbot.ter.dks.tasks.RemoteStats")).isEnabled();
    }

    @Override
    public void onTickTask() {
        this.capturer.setMaxLinesStd(this.config.MAX_STD_LINES);
        this.capturer.setMaxLinesErr(this.config.MAX_ERR_LINES);
        this.capturer.setMaxLinesStd(this.config.MAX_STD_LINES);
        this.capturer.setTimed(this.config.TIMED);
        this.capturer.updateLines();
        this.setCapturersLines();
    }

    @Override
    public void onBackgroundTick() {
        this.onTickTask();
    }

    public void setCapturersLines() {
        this.textSysOut.setText(this.capturer.getLinesStringStd(true));
        this.textSysErr.setText(this.capturer.getLinesStringErr(false));
    }

    public void clearCaptures() {
        this.capturer.reset();
        this.setCapturersLines();
    }

    @Override
    public void setConfig(ConfigSetting<LiveLogsConfig> arg0) {
        this.config = arg0.getValue();
    }
}
