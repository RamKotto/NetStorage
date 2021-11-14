import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
public class Client {
    private static Scanner scanner = new Scanner(System.in);
    private static Socket socket;
    private static DataOutputStream os;
    private static DataInputStream is;

    public static void main(String[] args) {
        try {
            socket = new Socket("localhost", 9090);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            log.debug("Initial mistake");
        }
        new Thread(() -> {
            try {
                while (true) {
                    log.debug("Received from server: " + is.readUTF());
                }
            } catch (IOException e) {
                log.debug("Input mistake");
            }
        }).start();

        new Thread(() -> {
            try {
                while (true) {
                    String msg = scanner.nextLine();
                    log.debug("Send to server: " + msg);
                    os.writeUTF(msg);
                }
            } catch (IOException e) {
                log.debug("Output mistake");
            }
        }).start();
    }
}
