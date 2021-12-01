package FileUtils;

import org.w3c.dom.ls.LSOutput;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        String path = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";

        try {

            File newFilePath = new File(path + "file.txt");
            newFilePath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File dir = new File(path); //path указывает на директорию
        File[] arrFiles = dir.listFiles();
        List<File> lst = Arrays.asList(arrFiles);
        for (File f : lst) {
            System.out.println("File: " + f.getName());
            System.out.println(f.getAbsolutePath());
        }

    }
}
