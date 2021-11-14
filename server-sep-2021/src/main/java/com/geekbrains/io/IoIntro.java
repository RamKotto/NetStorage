package com.geekbrains.io;

import java.io.*;

public class IoIntro {

    private static final byte[] buffer = new byte[1024];
    private static final String APP_NAME = "server-sep-2021\\";
    private static String clientDir;

    private String readAsString(String resourceName) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(resourceName);
        int read = inputStream.read(buffer);
        return new String(buffer, 0, read);
    }

    private void createClientDir(String clientName) {
        File root = new File(APP_NAME + clientName);
        if (!root.exists()) {
            root.mkdir();
        }
        clientDir = APP_NAME + "\\" + clientName + "\\";
    }

    private void transfer(File src, File dst) {
        try (FileInputStream is = new FileInputStream(src);
             FileOutputStream os = new FileOutputStream(dst)) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        IoIntro ioIntro = new IoIntro();
        System.out.println(ioIntro.readAsString("hello.txt"));

        ioIntro.createClientDir("d.saraev");

        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        ioIntro.transfer(new File(System.getProperty("user.dir") + "\\server-sep-2021\\src\\main\\resources\\com\\geekbrains\\io\\hello.txt"),
                new File(clientDir + "copy.txt")
        );
    }
}
