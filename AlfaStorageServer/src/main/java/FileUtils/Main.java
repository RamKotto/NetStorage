package FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {

        try {
            String path = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";
            File newFilePath = new File(path + "file.txt");
            newFilePath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.setProperty("Super", "Man");
        MainTwo.print();

    }
}
