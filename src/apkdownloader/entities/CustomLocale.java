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
public class CustomLocale implements Serializable {
    
    private String displayName;
    private String countryCode;
    private String languageCode;

    public CustomLocale(String displayName, String countryCode, String languageCode) {
        this.displayName = displayName;
        this.countryCode = countryCode;
        this.languageCode = languageCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

}