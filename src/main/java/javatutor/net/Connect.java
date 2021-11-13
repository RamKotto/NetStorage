package javatutor.net;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connect {
    private static final int LOOP_COUNT = 10;
    private static final int SLEEP_TIME = 500;
    private static final int PORT = 9876;

    public static void main(String args[])
            throws IOException, InterruptedException {
        for (int i = 0; i < LOOP_COUNT; i++) {
            Socket socket = new Socket("localhost", PORT);
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            Writer writer = new OutputStreamWriter(os, StandardCharsets.US_ASCII);
            PrintWriter out = new PrintWriter(writer, true);
            out.println("Hello, World");
            BufferedReader in =
                    new BufferedReader
                            (new InputStreamReader(is, StandardCharsets.US_ASCII));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(i + ": " + line);
            }
            socket.close();
            Thread.sleep(SLEEP_TIME);
        }
    }
}
