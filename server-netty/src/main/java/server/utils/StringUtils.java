package server.utils;

public class StringUtils {

    public static String createOutputMessage(String clientName, String message) {
        return String.format("[%s]: %s\n", clientName, message);
    }

    public static String changeName(String message) {
        return message.split("\\s", 2)[1];
    }
}
