package com.dks.types.config;

import com.github.manolo8.darkbot.config.types.Num;
import com.github.manolo8.darkbot.config.types.Option;

public class PalladiumStatsConfig implements SimpleStatsConfig {
    @Option(value = "Stop", description = "Will stop the task")
    public boolean STOP;

    @Option(value = "Refresh Rate (s)", description = "Refresh Rate in Seconds to check Palladium")
    @Num(min = 1, max = 120, step = 1)
    public int REFRESH_RATE_S;

    // @Option(value = "Status")
    // public StatusSettings STATUS;

    public PalladiumStatsConfig() {
        this.REFRESH_RATE_S = 1;
        this.STOP = false;
        // this.STATUS = new StatusSettings();
    }

}

/*
public static class StatusSettings {

        @Option
        @Editor(value = JStatusComponent.class, shared = true)
        public transient JPanel STATUS_PANEL;
        public transient JLabel STATUS;
        public transient JButton RESET_BUTTON;

        public StatusSettings() {
            this.STATUS = new JLabel();
            this.STATUS.setPreferredSize(new Dimension(80, 80));
            this.STATUS.setText("");

            this.RESET_BUTTON = new JButton("Reset Metrics");
            this.RESET_BUTTON.setPreferredSize(new Dimension(50, 30));
            this.RESET_BUTTON.setMaximumSize(this.RESET_BUTTON.getPreferredSize());

            this.STATUS_PANEL = new JPanel();
            this.STATUS_PANEL.setPreferredSize(new Dimension(400, 80));
            this.STATUS_PANEL.setOpaque(false);
            this.STATUS_PANEL.setLayout(new GridLayout(1, 2));
            this.STATUS_PANEL.add(this.STATUS).setLocation(0, 0);
            this.STATUS_PANEL.add(this.RESET_BUTTON).setLocation(0, 1);
        }

        public void setStatus(String status) {
            this.STATUS.setText(status);
            this.STATUS_PANEL.revalidate();
            this.STATUS_PANEL.repaint();
        }

        public static class JStatusComponent extends JPanel implements OptionEditor {

            public JStatusComponent(final StatusSettings settings) {
                super();
                this.add(settings.STATUS_PANEL);
            }

            @Override
            public JComponent getComponent() {
                return this;
            }

            @Override
            public void edit(ConfigField configField) {

            }
        }
    }
*/