package models;

import java.util.Date;

public class Item {
    private String fileName;
    private Date lastModified;
    private String userPath;
    private final static String basePath = System.getProperty("user.dir") + "\\AlfaStorageServer\\src\\main\\resources\\";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getUserPath() {
        return userPath;
    }

    public void setUserPath(String userPath) {
        this.userPath = userPath;
    }

    public String getBasePath() {
        return basePath;
    }
}
