package FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    private final static String basePath = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";

    public static List<File> getUserFilesByToken(String userName) {
        File dir = new File(basePath + userName);
        File[] arrFiles = dir.listFiles();
        return Arrays.asList(arrFiles);
    }

    public static void createUserDir(String userName) {
        File file = new File(basePath + userName);

        if(!file.exists())
        {
            file.mkdir();
            System.out.println("Folder for " + userName + " was created...");
        }
    }



//    public static void main(String[] args) {
//
//        String path = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";
//
//        try {
//
//            File newFilePath = new File(path + "file.txt");
//            newFilePath.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        File dir = new File(path); //path указывает на директорию
//        File[] arrFiles = dir.listFiles();
//        List<File> lst = Arrays.asList(arrFiles);
//        for (File f : lst) {
//            System.out.println("File: " + f.getName() + " " + new Date(f.lastModified()));
//            System.out.println(f.getAbsolutePath());
//        }
//    }
}
