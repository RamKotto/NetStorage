package FileUtils;

import java.io.File;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";
        System.out.println(path);
        File file = new File(path + "f.txt");

    }
}
