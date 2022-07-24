package eu.darkbot.ter.dks.tasks;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.utils.RuntimeUtil;
import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.extensions.*;
import eu.darkbot.api.managers.*;
import eu.darkbot.ter.dks.utils.BotRemoteController;
import eu.darkbot.ter.dks.utils.VerifierChecker;
import eu.darkbot.ter.dks.types.config.RemoteStatsConfig;
import eu.darkbot.ter.dks.types.remotestats.*;
import eu.darkbot.ter.dks.utils.Http;
import eu.darkbot.ter.dks.utils.plugin.DksPluginInfo;
import eu.darkbot.ter.dks.utils.plugin.DksPluginSingleton;
import eu.darkbot.util.Popups;
import eu.darkbot.util.SystemUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Feature(name = "Remote Stats", description = "Allows you to monitor your botting session remotelly")
public class RemoteStats implements Task, Configurable<RemoteStatsConfig>, ExtraMenus, InstructionProvider {

    protected final HeroAPI heroAPI;
    protected final StarSystemAPI mapAPI;
    protected final BotAPI botAPI;
    protected final StatsAPI statsAPI;
    protected final EntitiesAPI entitiesAPI;
    protected final I18nAPI i18n;
    protected final ExtensionsAPI extensions;
    protected final PluginInfo plugin;
    protected final Main main;
    protected final RepairAPI repair;
    protected final BoosterAPI booster;
    protected final ConfigAPI configAPI;
    protected final BackpageAPI backpage;
    protected BotRemoteController controller;

    private RemoteStatsConfig config;
    private long nextTick = 0;
    private final JLabel lastSuccededTime;
    private final JLabel lastRequestStatus;
    private final DksPluginInfo dksPluginInfo;

    public RemoteStats(
            AuthAPI auth,
            HeroAPI hero,
            StarSystemAPI map,
            BotAPI bot,
            StatsAPI stats,
            EntitiesAPI entities,
            I18nAPI i18n,
            ExtensionsAPI extensions,
            Main main,
            RepairAPI repair,
            BoosterAPI booster,
            ConfigAPI config,
            BackpageAPI backpage
    ) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners()))
            throw new SecurityException();
        VerifierChecker.checkAuthenticity(auth);

        this.heroAPI = hero;
        this.mapAPI = map;
        this.botAPI = bot;
        this.statsAPI = stats;
        this.entitiesAPI = entities;
        this.i18n = i18n;
        this.extensions = extensions;
        this.plugin = Objects.requireNonNull(this.extensions.getFeatureInfo(getClass())).getPluginInfo();
        this.main = main;
        this.repair = repair;
        this.booster = booster;
        this.configAPI = config;
        this.backpage = backpage;

        this.lastRequestStatus = new JLabel("<html><b>" + this.i18n.get(this.plugin, "remote_stats.server.status.last_post") +  "</b> " + "-</html>");
        this.lastSuccededTime = new JLabel("<html><b>" + this.i18n.get(this.plugin, "remote_stats.server.status.last_success") + "</b> " + "-</html>");
        this.dksPluginInfo = DksPluginSingleton.getPluginInfo();

        this.controller = new BotRemoteController(
                this.botAPI,
                this.main,
                this.heroAPI,
                this.extensions,
                this.configAPI
        );
    }

    public String getURL() {
        String url = new StringBuilder("http://")
                .append(this.config.HOST)
                .append(":")
                .append(this.config.PORT)
                .toString();
        return url;
    }

    public String getMessage() {
        return new InfoDTO(
            new HeroDTO(this.heroAPI, this.booster),
            new StatsDTO(this.statsAPI),
            new ModuleDTO(this.botAPI, this.extensions, this.configAPI),
            new MapDTO(this.mapAPI, this.entitiesAPI),
            DksPluginSingleton.getPluginInfo(),
            new UserDataDTO(this.backpage),
            new DeathsDTO(this.repair),
            new ConfigDTO(this.configAPI, this.main)
        ).toJson();
    }

    public boolean checkTick() {
        boolean canTick = this.config.ACTIVE && this.nextTick <= System.currentTimeMillis();
        if (canTick) {
            this.nextTick = System.currentTimeMillis() + (this.config.REFRESH_RATE_S * 1000L);
        }
        return canTick;
    }

    @Override
    public void onTickTask() {
        if (this.checkTick()) {
            String url = this.getURL();
            String message = this.getMessage();
            if (Http.sendMessage(message, url, this.controller)) {
                this.lastRequestStatus.setText(
                        "<html><b>" +
                        this.i18n.get(this.plugin, "remote_stats.server.status.last_post") +  "</b> " +
                        this.i18n.get(this.plugin, "remote_stats.server.status.last_post.success") +
                        "</html>"
                );
                this.lastSuccededTime.setText(
                        "<html><b>" +
                        this.i18n.get(this.plugin, "remote_stats.server.status.last_success") + "</b> " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
                        "</html>"
                );
            } else {
                this.lastRequestStatus.setText(
                        "<html><b>" +
                        this.i18n.get(this.plugin, "remote_stats.server.status.last_post") +  "</b> " +
                        this.i18n.get(this.plugin, "remote_stats.server.status.last_post.fail") +
                        "</html>"
                );
            }
        } else if (!this.config.ACTIVE) {
            this.lastRequestStatus.setText(
                    "<html><b>" +
                    this.i18n.get(this.plugin, "remote_stats.server.status.last_post") +  "</b> " +
                    this.i18n.get(this.plugin, "remote_stats.server.status.plugin_stopped") +
                    "</html>"
            );
        }
    }

    @Override
    public void setConfig(ConfigSetting<RemoteStatsConfig> arg0) {
        this.config = arg0.getValue();
    }

    @Override
    public Collection<JComponent> getExtraMenuItems(PluginAPI pluginAPI) {
        Collection<JComponent> components = new ArrayList<>();
        if (this.shouldCreateExtraMenuSeparator()) {
            components.add(createSeparator(this.i18n.get(this.plugin, "plugin.separator")));
        }
        components.add(create(this.getServerPopupTitle(), e -> this.showServerPopup()));
        return components;
    }

    public boolean shouldCreateExtraMenuSeparator() {
        return true;
    }

    public String getServerPopupTitle() {
        return this.i18n.get(this.plugin, "remote_stats.popup.server");
    }

    public String getSetupPopupTitle() {
        return this.i18n.get(this.plugin, "remote_stats.popup.setup");
    }

    public JComponent beforeConfig() {
        JComponent panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        JButton setup = this.getSetupButton();
        JButton server = this.getServerButton();
        panel.add(setup).setLocation(0, 0);
        panel.add(server).setLocation(1, 0);
        return panel;
    }

    protected boolean checkIfServerFileExists() {
        return !this.config.SERVER_FILE.trim().equals("") && Files.exists(Paths.get(this.config.SERVER_FILE));
    }

    protected String getTitle(String title) {
        return "<html><b><h4 style=\"margin-bottom: 1px; margin-top: 1px\">" + title + "</h4></b></html>";
    }

    protected void showServerPopup() {
        JLabel runcmd = new JLabel(String.format("<html>cmd /k start \"DksPluginRemoteServer\" <br> cmd /c <br>%s <br> --host %s <br> --port %s <br> --auth %s%s</html>",
                this.config.SERVER_FILE,
                this.config.HOST,
                this.config.PORT,
                this.config.NGROK_AUTH.trim(),
                this.config.HASH ? "" : " <br> --no-hashed"
        ));
        runcmd.setOpaque(true);
        runcmd.setBackground(Color.black);
        runcmd.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel killcmd = new JLabel("<html>cmd /c <br> taskkill /F /FI \"WindowTitle eq DksPluginRemoteServer\"</html>");
        killcmd.setOpaque(true);
        killcmd.setBackground(Color.black);
        killcmd.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel message = new JLabel();
        Object[] objects;
        JOptionPane options;

        if (this.checkIfServerFileExists()) {
            if (!this.config.NGROK_AUTH.trim().equals("")) {
                JButton runServer = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.run_server"));
                runServer.addActionListener(e -> {
                    try {
                        RuntimeUtil.execute(
                            "cmd", "/k", "start", "\"DksPluginRemoteServer\"", "cmd", "/c",
                            this.config.SERVER_FILE.trim(),
                            "--host", this.config.HOST.trim(),
                            "--port", String.valueOf(this.config.PORT).trim(),
                            "--auth", this.config.NGROK_AUTH.trim(),
                            this.config.HASH ? "--hashed" : "--no-hashed"
                        );
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                JButton killServer = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.kill_server"));
                killServer.addActionListener(e -> {
                    try {
                        RuntimeUtil.execute("cmd", "/c", "taskkill", "/F", "/FI", "WindowTitle eq DksPluginRemoteServer");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                JLabel info = new JLabel(this.getTitle(this.i18n.get(this.plugin, "remote_stats.server.info_title")));
                JLabel status = new JLabel(this.getTitle(this.i18n.get(this.plugin, "remote_stats.server.status_title")));

                JLabel runButton = new JLabel(this.i18n.get(this.plugin, "remote_stats.buttons.run_server"));
                JLabel stopButton = new JLabel(this.i18n.get(this.plugin, "remote_stats.buttons.kill_server"));

                objects = new Object[] { status, this.lastRequestStatus, this.lastSuccededTime, " ", info, runButton, runcmd, stopButton, killcmd };
                options = new JOptionPane(objects);
                options.setOptions(new Object[] { runServer, killServer,  "OK" });
                Popups.showMessageSync(this.getServerPopupTitle(), options);
                return;
            } else {
                message.setText(this.i18n.get(this.plugin, "remote_stats.setup.no_auth_token"));
            }
        } else {
            message.setText(this.i18n.get(this.plugin, "remote_stats.setup.no_file_selected"));
        }
        JButton setup = this.getSetupButton();
        objects = new Object[] { message };
        options = new JOptionPane(objects);
        options.setOptions(new Object[] { setup, "OK" });
        Popups.showMessageSync(this.getServerPopupTitle(), options);
    }

    protected JLabel getServerFileLabel() {
        JLabel selected;
        if (!this.checkIfServerFileExists()) {
            selected = new JLabel(this.i18n.get(this.plugin, "remote_stats.setup.no_file_selected"));
            this.config.SERVER_FILE = "";
        } else {
            selected = new JLabel(this.config.SERVER_FILE);
        }
        return selected;
    }

    protected void showSetupPopup() {
        String downloadUrl = "https://gist.github.com/joseankco/bbddd86e6f2c12cf2fe81658b579587f/raw/RemoteStatsServer.exe";
        JButton download = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.download_server"));
        download.addActionListener(e ->  SystemUtils.openUrl(downloadUrl));
        JLabel selected = this.getServerFileLabel();

        JButton fileOpener = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.select_file"));
        fileOpener.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(this.config.SERVER_FILE);
            int option = fileChooser.showOpenDialog(fileOpener);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                this.config.setServerFile(file.getAbsolutePath());
                selected.setText(this.config.SERVER_FILE);
            }
        });

        JButton register = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.register_ngrok"));
        register.addActionListener(e ->  SystemUtils.openUrl("https://dashboard.ngrok.com/signup"));

        JButton token = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.get_auth_token"));
        token.addActionListener(e ->  SystemUtils.openUrl("https://dashboard.ngrok.com/get-started/your-authtoken"));

        Object[] options = new Object[]{
                this.i18n.get(this.plugin, "remote_stats.setup.download_server"),
                download,
                downloadUrl,
                " ",
                this.i18n.get(this.plugin, "remote_stats.setup.select_file"),
                fileOpener,
                selected,
                " ",
                this.i18n.get(this.plugin, "remote_stats.setup.register_ngrok"),
                register,
                "https://dashboard.ngrok.com/signup",
                " ",
                this.i18n.get(this.plugin, "remote_stats.setup.get_auth_token"),
                token,
                "https://dashboard.ngrok.com/get-started/your-authtoken",
                " ",
                this.i18n.get(this.plugin, "remote_stats.setup.run_server")
        };

        Popups.showMessageSync(this.getSetupPopupTitle(), new JOptionPane(options));
    }

    protected JButton getSetupButton() {
        JButton setup = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.setup"));
        setup.addActionListener(e -> this.showSetupPopup());
        return setup;
    }

    protected JButton getServerButton() {
        JButton server = new JButton(this.i18n.get(this.plugin, "remote_stats.buttons.server"));
        server.addActionListener(e -> this.showServerPopup());
        return server;
    }
}
