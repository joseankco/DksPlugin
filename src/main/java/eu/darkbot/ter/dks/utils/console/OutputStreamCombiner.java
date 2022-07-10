package eu.darkbot.ter.dks.utils.console;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class OutputStreamCombiner extends OutputStream {
    private final List<OutputStream> outputStreams;

    public OutputStreamCombiner(List<OutputStream> outputStreams) {
        this.outputStreams = outputStreams;
    }

    public void write(int b) throws IOException {
        for (OutputStream os : outputStreams) {
            os.write(b);
        }
    }

    public void flush() throws IOException {
        for (OutputStream os : outputStreams) {
            os.flush();
        }
    }

    public void close() throws IOException {
        for (OutputStream os : outputStreams) {
            os.close();
        }
    }
}