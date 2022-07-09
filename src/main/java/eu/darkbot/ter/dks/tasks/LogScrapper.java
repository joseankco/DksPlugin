package eu.darkbot.ter.dks.tasks;

import com.github.manolo8.darkbot.Main;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.AuthAPI;
import eu.darkbot.api.managers.ExtensionsAPI;
import eu.darkbot.api.managers.I18nAPI;
import eu.darkbot.ter.dks.types.VerifierChecker;
import eu.darkbot.ter.dks.types.config.LogScrapperConfig;
import eu.darkbot.ter.dks.types.utils.Formatter;
import eu.darkbot.ter.dks.types.utils.console.ConsoleOutputCapturer;
import eu.darkbot.ter.dks.types.utils.console.ConsoleOutputCapturerSingleton;
import eu.darkbot.util.Popups;
import eu.darkbot.util.SystemUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Feature(name = "Log Scrapper", description = "Allows you to extract patterns from DarkBot logs in real time")
public class LogScrapper implements Task, ExtraMenus, Configurable<LogScrapperConfig>, InstructionProvider {

    protected final I18nAPI i18n;
    protected final ExtensionsAPI extensions;
    protected final PluginInfo plugin;
    protected final Main main;
    protected final long INNER_UI_REFRESH_RATE_S = 1000L;
    protected Duration runningTime;
    protected long lastTick;
    protected long lastTickUiRefresh;

    protected final ConsoleOutputCapturer capturer;
    protected LogScrapperConfig config;
    protected HashMap<String, int[]> map;

    protected JLabel runningLabel;
    protected JTabbedPane tabs;

    // TABLE
    protected JFrame tableFrame;
    protected DefaultTableModel model;
    protected JTable table;
    protected JScrollPane tableScroll;

    // MANAGE PATTERNS
    protected JScrollPane manageScroll;
    protected JPanel managePanel;
    protected JFrame manageFrame;

    public LogScrapper(AuthAPI auth, I18nAPI i18n, ExtensionsAPI extensions, Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.i18n = i18n;
        this.extensions = extensions;
        this.plugin = Objects.requireNonNull(this.extensions.getFeatureInfo(getClass())).getPluginInfo();
        this.main = main;

        this.capturer = ConsoleOutputCapturerSingleton.getCapturer();
        this.map = new HashMap<>();

        this.initUi();

        this.runningTime = Duration.ofSeconds(0);
        this.lastTick = System.currentTimeMillis();
    }

    public void initUi() {
        // TABLE
        this.model = new DefaultTableModel();
        this.table = new JTable(this.model);
        this.table.setFocusable(false);
        this.tableScroll = new JScrollPane(this.table);
        this.tableFrame = new JFrame();
        this.tableFrame.add(this.tableScroll);

        // MANAGE
        this.managePanel = new JPanel();
        this.manageScroll = new JScrollPane(this.managePanel);
        this.manageFrame = new JFrame();
        this.manageFrame.add(this.manageScroll);

        // TABS
        this.tabs = new JTabbedPane();
        tabs.add(this.tableFrame.getContentPane(), this.i18n.get(this.plugin, "log_scrapper.buttons.status.desc"));
        tabs.add(this.manageFrame.getContentPane(), this.i18n.get(this.plugin, "log_scrapper.buttons.patterns.desc"));
        tabs.setPreferredSize(new Dimension(800, 300));

        // RUNNING TIME
        this.runningLabel = new JLabel();
    }

    public JButton getAddPatternButton(JTextField text) {
        JButton add = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.add"));
        add.addActionListener(e -> {
            if (!text.getText().trim().equals("")) {
                this.config.addPattern(text.getText().trim());
                this.refreshUi(true);
            }
        });
        add.setPreferredSize(new Dimension(80, 25));
        return add;
    }

    public JButton getDeletePatternButton(String pattern) {
        JButton delete = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.delete"));
        delete.addActionListener(e -> {
            this.config.removePattern(pattern);
            this.refreshUi(true);
        });
        delete.setPreferredSize(new Dimension(80, 25));
        return delete;
    }

    public JButton getEditPatternButton(String pattern, JTextField text) {
        JButton edit = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.edit"));
        edit.addActionListener(e -> {
            String newPattern = text.getText().trim();
            if (!newPattern.equals("")) {
                this.config.editPattern(pattern, newPattern);
                this.refreshUi(true);
            }
        });
        edit.setPreferredSize(new Dimension(80, 25));
        return edit;
    }

    public JButton getUpPatternButton(String pattern) {
        // JButton up = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.up"));
        JButton up = new JButton("^");
        up.addActionListener(e -> {
            this.config.patternUp(pattern);
            this.refreshUi(true);
        });
        up.setPreferredSize(new Dimension(30, 25));
        return up;
    }

    public JButton getDownPatternButton(String pattern) {
        // JButton down = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.down"));
        JButton down = new JButton("v");
        down.addActionListener(e -> {
            this.config.patternDown(pattern);
            this.refreshUi(true);
        });
        down.setPreferredSize(new Dimension(30, 25));
        return down;
    }

    public JPanel getCombinedPanel(Component a, Component b) {
        JPanel combined = new JPanel();
        combined.add(a);
        combined.add(b);
        return combined;
    }

    public void buildPanelManagePatterns() {
        List<String> patterns = this.config.getPatterns();
        this.managePanel.setLayout(new BoxLayout(this.managePanel, BoxLayout.Y_AXIS));
        this.managePanel.removeAll();

        JTextField text = new JTextField();
        text.setPreferredSize(new Dimension(600, 25));
        JButton add = this.getAddPatternButton(text);

        JPanel addPatternPanel = this.getCombinedPanel(text, add);
        this.managePanel.add(addPatternPanel);

        for (String pattern : patterns) {
            JTextField patternField = new JTextField(pattern);
            patternField.setPreferredSize(new Dimension(500, 25));

            JButton delete = this.getDeletePatternButton(pattern);
            JButton edit = this.getEditPatternButton(pattern, patternField);
            JPanel delEditPanel = this.getCombinedPanel(edit, delete);

            JButton up = this.getUpPatternButton(pattern);
            JButton down = this.getDownPatternButton(pattern);
            JPanel movePanel = this.getCombinedPanel(up, down);

            JPanel actionPanel = this.getCombinedPanel(movePanel, delEditPanel);
            JPanel patternPanel = this.getCombinedPanel(patternField, actionPanel);
            this.managePanel.add(patternPanel);
        }
        this.manageScroll.repaint();
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI pluginAPI) {
        Collection<JComponent> components = new ArrayList<>();
        if (this.shouldCreateExtraMenuSeparator()) {
            components.add(createSeparator(this.i18n.get(this.plugin, "plugin.separator")));
        }
        components.add(create(this.getPopupTitle(), e -> this.showStatusPopup()));
        return components;
    }

    public String getPopupTitle() {
        return "Log Scrapper";
    }

    protected String getTitle(String title) {
        return "<html><b><h2 style=\"margin-bottom: 1px; margin-top: 1px\">" + title + "</h2></b></html>";
    }

    public void showStatusPopup() {
        Object[] objects = new Object[] { runningLabel, tabs };
        JOptionPane options = new JOptionPane(objects);

        JButton reset = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.reset"));
        reset.addActionListener(e -> {
            this.resetAll();
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
        this.refreshUi(true);
        options.setOptions(new Object[] { startStop, reset, "OK" });
        Popups.showMessageSync(this.getPopupTitle(), options);
    }

    private void resetAll() {
        this.runningTime = Duration.ofSeconds(0);
        this.lastTick = System.currentTimeMillis();
        this.map = new HashMap<>();
        this.refreshUi(true);
    }

    public boolean shouldCreateExtraMenuSeparator() {
        String[] precessors = new String[] {
                "eu.darkbot.ter.dks.tasks.RemoteStats",
                "eu.darkbot.ter.dks.tasks.LiveLogs",
        };
        String enabled = Arrays.stream(precessors).sequential().filter(f ->
                Objects.requireNonNull(this.main.featureRegistry.getFeatureDefinition(f)).isEnabled()
        ).findFirst().orElse(null);
        return enabled == null;
    }

    protected void refreshRunningTime() {
        long now = System.currentTimeMillis();
        this.runningTime = this.runningTime.plus(Duration.ofMillis(now - this.lastTick));
        this.lastTick = now;
    }

    @Override
    public void onTickTask() {
        this.refreshPatternsMap();
        this.refreshUi(false);
    }

    public void refreshUi(boolean force) {
        long now = System.currentTimeMillis();
        if (force || (now - this.lastTickUiRefresh > this.INNER_UI_REFRESH_RATE_S)) {
            this.refreshRunningTime();
            this.runningLabel.setText(this.getTitle("Running: " + Formatter.formatDuration(this.runningTime)));
            this.buildTable();
            if (force || tabs.getSelectedIndex() != 1) {
                this.buildPanelManagePatterns();
            }
            this.removeUnusedPatterns();
            this.lastTickUiRefresh = now;
        }
    }

    public void refreshPatternsMap() {
        List<String> lines = this.capturer.getNewLinesStd();
        List<String> patterns = this.config.getPatterns();

        patterns.stream().filter(p -> !p.equals("")).forEach(pattern -> {
            try {
                if (!this.map.containsKey(pattern)) {
                    this.map.put(pattern, new int[] {0, 0});
                }
                Pattern regexPattern = this.config.getRegExPattern(pattern);
                int[] actual = this.map.get(pattern);
                for (String line: lines) {
                    Matcher matcher = regexPattern.matcher(line);
                    if (matcher.find()) {
                        actual[0]++;
                        if (this.config.isNumberedPattern(pattern)) {
                            int total = Integer.parseInt(matcher.group("ExtractedNumber").replace(".", ""));
                            actual[1] += total;
                        }
                    }
                }
                this.map.put(pattern, actual);
            } catch (Exception e) {
                System.err.println("Error Produced Capturing Pattern: " + pattern);
                e.printStackTrace();
            }
        });
    }

    private void removeUnusedPatterns() {
        this.map.keySet().removeIf(key -> !this.config.PATTERNS.contains(key));
    }

    public void buildTable() {
        if (this.model.getColumnCount() == 0) {
            this.model.addColumn(this.i18n.get(this.plugin, "log_scrapper.table.pattern"));
            this.model.addColumn(this.i18n.get(this.plugin, "log_scrapper.table.occurrences"));
            this.model.addColumn(this.i18n.get(this.plugin, "log_scrapper.table.occurrencesh"));
            this.model.addColumn(this.i18n.get(this.plugin, "log_scrapper.table.total"));
            this.model.addColumn(this.i18n.get(this.plugin, "log_scrapper.table.totalh"));
        }

        this.table.getColumnModel().getColumn(0).setMinWidth(350);
        for (int i = 1; i < this.model.getColumnCount(); i++) {
            this.table.getColumnModel().getColumn(i).setMinWidth(100);
        }

        List<String> patterns = this.config.getPatterns();
        this.model.setNumRows(patterns.size());
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            if (pattern != null) {
                int[] info = this.map.get(pattern);
                if (info != null) {
                    int occurrences = info[0], total = info[1];
                    double occurrencesHour = (occurrences / ((double) this.runningTime.getSeconds())) * 3600;
                    double totalHour = (total / ((double) this.runningTime.getSeconds())) * 3600;
                    boolean isNumbered = this.config.isNumberedPattern(pattern);
                    String[] columns = new String[] {
                            pattern,
                            Formatter.formatDoubleDots(occurrences),
                            Formatter.formatDoubleDots(occurrencesHour),
                            isNumbered ? Formatter.formatDoubleDots(total) : Formatter.formatDoubleDots(occurrences),
                            isNumbered ? Formatter.formatDoubleDots(totalHour) : Formatter.formatDoubleDots(occurrencesHour)
                    };
                    for (int j = 0; j < this.model.getColumnCount(); j++) {
                        this.model.setValueAt(columns[j], i, j);
                    }
                }
            }
        }
    }

    @Override
    public void onBackgroundTick() {
        this.onTickTask();
    }

    @Override
    public void setConfig(ConfigSetting<LogScrapperConfig> arg0) {
        this.config = arg0.getValue();
    }

    public JButton getTutorialButton() {
        JButton tutorial = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.tutorial"));
        tutorial.addActionListener(e -> {
            this.showTutorialPopup();
        });
        return tutorial;
    }

    public JButton getOverallStatusButton() {
        JButton status = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.status"));
        status.addActionListener(e -> {
            this.tabs.setSelectedIndex(0);
            this.showStatusPopup();
        });
        return status;
    }

    public JButton getMangePatternsButton() {
        JButton patterns = new JButton(this.i18n.get(this.plugin, "log_scrapper.buttons.patterns"));
        patterns.addActionListener(e -> {
            this.tabs.setSelectedIndex(1);
            this.showStatusPopup();
        });
        return patterns;
    }

    public void showTutorialPopup() {
        JButton info = new JButton(this.i18n.get(this.plugin, "log_scrapper.tutorial.button.info"));
        info.addActionListener(e -> {
            SystemUtils.openUrl("https://github.com/joseankco/DksPluginReleases/blob/main/info/LogScrapper.md");
        });
        Object[] objects = new Object[] {
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.info1")),
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.info2")),
                " ",
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.pattern_types")),
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.normal_pattern")),
                new JLabel("      " + this.i18n.get(this.plugin, "log_scrapper.tutorial.normal_example")),
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.numbered_pattern")),
                new JLabel("      " + this.i18n.get(this.plugin, "log_scrapper.tutorial.numbered_example")),
                new JLabel("      " + this.i18n.get(this.plugin, "log_scrapper.tutorial.numbered_example.desc1")),
                new JLabel("      " + this.i18n.get(this.plugin, "log_scrapper.tutorial.numbered_example.desc2")),
                " ",
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.note1")),
                new JLabel(this.i18n.get(this.plugin, "log_scrapper.tutorial.important1"))
        };
        JOptionPane options = new JOptionPane(objects);
        options.setOptions(new Object[] { info, "OK" });
        Popups.showMessageSync(this.getPopupTitle(), options);
    }

    @Override
    public JComponent beforeConfig() {
        JComponent panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        JButton tutorial = this.getTutorialButton();
        JButton status = this.getOverallStatusButton();
        JButton patterns = this.getMangePatternsButton();
        panel.add(tutorial).setLocation(0, 0);
        panel.add(status).setLocation(0, 1);
        panel.add(patterns).setLocation(0, 2);
        return panel;
    }
}
