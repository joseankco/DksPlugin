package eu.darkbot.ter.dks.tasks;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.ter.dks.types.plugin.GameLogViewerDTO;
import eu.darkbot.ter.dks.utils.VerifierChecker;
import eu.darkbot.ter.dks.types.config.GameLogViewerConfig;
import eu.darkbot.ter.dks.utils.plugin.DksPluginInfo;
import eu.darkbot.ter.dks.utils.plugin.DksPluginSingleton;
import eu.darkbot.util.Popups;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature(name = "Game Log Viewer", description = "Allows you to monitor DarkOrbit logs in real time")
public class GameLogViewer implements Task, ExtraMenus, Configurable<GameLogViewerConfig>, Listener {

    protected final I18nAPI i18n;
    protected final ExtensionsAPI extensions;
    protected final PluginInfo plugin;
    protected final Main main;
    protected final GameLogAPI log;
    protected GameLogViewerConfig config;
    protected DksPluginInfo dksPluginInfo;

    protected JScrollPane scrollSysOut;
    protected JTextArea textSysOut;

    protected JPanel panelExtraMenu;
    protected JPanel panelFilter;
    protected JTextField filterStd;

    protected List<String> gameLogs;
    protected List<Date> gameLogTimes;
    protected SimpleDateFormat formatter;

    protected boolean capturing;

    protected long lastTickRefreshed = 0L;

    public GameLogViewer(AuthAPI auth, I18nAPI i18n, ExtensionsAPI extensions, Main main, GameLogAPI log) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.i18n = i18n;
        this.extensions = extensions;
        this.plugin = Objects.requireNonNull(this.extensions.getFeatureInfo(getClass())).getPluginInfo();
        this.main = main;
        this.log = log;
        this.capturing = true;
        this.gameLogs = new ArrayList<>();
        this.gameLogTimes = new ArrayList<>();
        this.formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        this.initGui();
        this.initDksPluginInfo();
    }

    @EventHandler
    public void onLogMessage(GameLogAPI.LogMessageEvent message) {
        if (this.isCapturing()) {
            String msg = message.getMessage();
            this.gameLogTimes.add(new Date());
            this.gameLogs.add(msg);
            if (this.shouldRefreshLogs()) {
                this.setGameLogs();
            }
        }
    }

    public boolean shouldRefreshLogs() {
        return this.textSysOut.isShowing() && (System.currentTimeMillis() - (this.lastTickRefreshed) > 1000L);
    }

    public void setGameLogs() {
        String filter = !this.filterStd.getText().trim().equals("") ? this.filterStd.getText().trim() : null;
        Pattern pattern = null;
        if (filter != null) {
            pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
        }
        Matcher matcher;
        StringBuilder sb = new StringBuilder();
        for (int i = this.gameLogs.size() - 1; i >= 0; i--) {
            String line = this.gameLogs.get(i);
            boolean matches = true;
            if (filter != null) {
                matcher = pattern.matcher(line);
                matches = matcher.find();
            }
            if (matches) {
                if (this.config.TIMED) {
                    sb.append(formatter.format(this.gameLogTimes.get(i))).append(" ");
                }
                sb.append(line).append("\n");
            }
        }
        this.textSysOut.setText(sb.toString());
        this.lastTickRefreshed = System.currentTimeMillis();
    }

    public void initGui() {
        this.panelExtraMenu = new JPanel();
        this.panelExtraMenu.setLayout(new BoxLayout(this.panelExtraMenu, BoxLayout.Y_AXIS));

        this.filterStd = new JTextField();
        this.filterStd.addActionListener(e -> this.setGameLogs());
        filterStd.addActionListener(e -> this.setGameLogs());
        filterStd.setPreferredSize(new Dimension(700, 25));
        this.panelFilter = new JPanel();
        this.panelFilter.add(this.filterStd);

        this.textSysOut = this.getTextArea(50, 15,false);
        this.scrollSysOut = new JScrollPane(this.textSysOut);

        this.panelExtraMenu.add(this.panelFilter);
        this.panelExtraMenu.add(this.scrollSysOut);

        this.panelExtraMenu.setPreferredSize(new Dimension(700, 300));
    }

    public void initDksPluginInfo() {
        this.dksPluginInfo = DksPluginSingleton.getPluginInfo();
        this.dksPluginInfo.setGameLogViewerDTO(new GameLogViewerDTO(this));
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
        return "Game Log Viewer";
    }

    public boolean isCapturing() {
        return this.capturing;
    }

    public void start() {
        this.capturing = true;
    }

    public void stop() {
        this.capturing = false;
    }

    public void showPopup() {
        Object[] objects = new Object[] { panelExtraMenu };
        JOptionPane options = new JOptionPane(objects);

        JButton clear = new JButton(this.i18n.get(this.plugin, "game_log_viewer.buttons.clear"));
        clear.addActionListener(e -> {
            this.clearCaptures();
        });

        JButton startStop = new JButton(this.isCapturing() ? "Stop" : "Start");
        startStop.addActionListener(e -> {
            if (this.isCapturing()) {
                this.stop();
                startStop.setText("Start");
            } else {
                this.start();
                startStop.setText("Stop");
            }
        });

        options.setOptions(new Object[] { startStop, clear, "OK" });
        Popups.showMessageSync(this.getPopupTitle(), options);
    }

    public boolean shouldCreateExtraMenuSeparator() {
        String[] precessors = new String[] {
                "eu.darkbot.ter.dks.tasks.RemoteStats"
        };
        String enabled = Arrays.stream(precessors).sequential().filter(f ->
                Objects.requireNonNull(this.main.featureRegistry.getFeatureDefinition(f)).isEnabled()
        ).findFirst().orElse(null);
        return enabled == null;
    }

    @Override
    public void onTickTask() {
        int size = this.gameLogs.size();
        int diff = size - this.config.MAX_STD_LINES;
        for (int i = 0; (diff > 0) && (i < diff); i++) {
            this.gameLogTimes.remove(0);
            this.gameLogs.remove(0);
        }
        if (this.shouldRefreshLogs()) {
            this.setGameLogs();
        }
        this.dksPluginInfo.getGameLogViewer().refresh();
    }

    @Override
    public void onBackgroundTick() {
        this.onTickTask();
    }

    public void clearCaptures() {
        this.gameLogs = new ArrayList<>();
        this.gameLogTimes = new ArrayList<>();
        this.setGameLogs();
    }

    @Override
    public void setConfig(ConfigSetting<GameLogViewerConfig> arg0) {
        this.config = arg0.getValue();
    }

    public List<String> getGameLogs(boolean b) {
        List<String> lines = new ArrayList<>();
        for (int i = this.gameLogs.size() - 1; i >= 0; i--) {
            Date time = this.gameLogTimes.get(i);
            String log = this.gameLogs.get(i);
            if (time != null && log != null) {
                String line = formatter.format(time) + " " + log;
                lines.add(line);
            }
        }
        return lines;
    }
}
