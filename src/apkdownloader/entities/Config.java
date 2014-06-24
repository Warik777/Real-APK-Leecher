/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader.entities;

import java.io.Serializable;

/**
 *
 * @author CodeBlue
 */
public class Config implements Serializable {
    
    public static String configFile = "app.config";
    private String email;
    private String password;
    private String deviceId;
    private int recordShow;
    private String folderToSave;
    private String defaultScanFolder;
    private int formatFileType;
    private CustomLocale locale;
    private String simNumberic;

    public Config(){}

    public Config(String email, String password, String deviceId) {
        this.email = email;
        this.password = password;
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRecordShow() {
        return recordShow;
    }

    public void setRecordShow(int recordShow) {
        this.recordShow = recordShow;
    }

    public String getFolderToSave() {
        return folderToSave;
    }

    public void setFolderToSave(String folderToSave) {
        this.folderToSave = folderToSave;
    }

    public String getDefaultScanFolder() {
        return defaultScanFolder;
    }

    public void setDefaultScanFolder(String defaultScanFolder) {
        this.defaultScanFolder = defaultScanFolder;
    }

    public int getFormatFileType() {
        return formatFileType;
    }

    public void setFormatFileType(int formatFileType) {
        this.formatFileType = formatFileType;
    }

    public CustomLocale getLocale() {
        return locale;
    }

    public void setLocale(CustomLocale locale) {
        this.locale = locale;
    }

    public String getSimNumberic() {
        return simNumberic;
    }

    public void setSimNumberic(String simNumberic) {
        this.simNumberic = simNumberic;
    }

}