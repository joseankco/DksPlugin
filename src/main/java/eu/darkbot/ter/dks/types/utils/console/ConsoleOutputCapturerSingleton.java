package eu.darkbot.ter.dks.types.utils.console;

public class ConsoleOutputCapturerSingleton {

    private static ConsoleOutputCapturer capturer;

    public static ConsoleOutputCapturer getCapturer() {
        if (capturer == null) {
            capturer = new ConsoleOutputCapturer();
        }
        return capturer;
    }
}
