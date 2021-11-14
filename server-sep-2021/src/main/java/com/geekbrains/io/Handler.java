package com.geekbrains.io;

import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

@Slf4j
public class Handler implements Runnable {
    private final Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (DataOutputStream os = new DataOutputStream(socket.getOutputStream());
             DataInputStream is = new DataInputStream(socket.getInputStream())
        ) {
            while (true) {
                String message = is.readUTF();
                log.debug("Received from client: {}", message);
                os.writeUTF(message);
                os.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
