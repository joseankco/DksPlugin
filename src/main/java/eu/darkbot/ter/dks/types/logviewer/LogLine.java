package eu.darkbot.ter.dks.types.logviewer;

import java.util.Date;

public class LogLine {
    public final Date date;
    public final String line;
    public LogLine(Date date, String line) {
        this.date = date;
        this.line = line;
    }

    public Date getDate() {
        return this.date;
    }

    public String getLine() {
        return this.line;
    }
}
