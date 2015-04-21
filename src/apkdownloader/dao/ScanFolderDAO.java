/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader.dao;

import apkdownloader.APKDownloaderApp;
import apkdownloader.ScanOption;
import apkdownloader.entities.Config;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.ResponseContext;
import java.io.BufferedReader;
import java.io.File;
/*ADD*/import java.io.FileInputStream;
/*ADD*/import java.io.FileNotFoundException;
/*ADD*/import java.io.FileOutputStream;
/*ADD*/import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
/*ADD*/import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.JOptionPane;
/*ADD*/import com.sun.jna.Native;
/*ADD*/import com.sun.jna.platform.win32.Kernel32;

/**
 *
 * @author CodeBlue
 */
public class ScanFolderDAO {

    private static DecimalFormat df = new DecimalFormat("#,###,###,##0.00");

/*ADD*/    public static void copyFile( File fromFile, File toFile ) throws FileNotFoundException, IOException {
/*ADD*/        FileInputStream from = null;
/*ADD*/        FileOutputStream to = null;
/*ADD*/        try {
/*ADD*/            from = new FileInputStream(fromFile);
/*ADD*/            to = new FileOutputStream(toFile);
/*ADD*/            byte[] buffer = new byte[4096];
/*ADD*/            int bytesRead;
/*ADD*/            while ((bytesRead = from.read(buffer)) != -1) {
/*ADD*/                to.write(buffer, 0, bytesRead); // write
/*ADD*/            }
/*ADD*/        } finally {
/*ADD*/            if (from != null) {
/*ADD*/                try {
/*ADD*/                    from.close();
/*ADD*/                } catch (IOException e) {
/*ADD*/                    ;
/*ADD*/                }
/*ADD*/            }
/*ADD*/            if (to != null) {
/*ADD*/                try {
/*ADD*/                    to.close();
/*ADD*/                } catch (IOException e) {
/*ADD*/                    ;
/*ADD*/                }
/*ADD*/            }
/*ADD*/        }
/*ADD*/    }
/*ADD*/
/*ADD*/    static String getProcessOutput( Process p ) throws UnsupportedEncodingException, IOException{
/*ADD*/        String line = "";
/*ADD*/        InputStream is = p.getInputStream();
/*ADD*/        InputStreamReader isr = new InputStreamReader(is, "UTF8");
/*ADD*/        BufferedReader br = new BufferedReader(isr);
/*ADD*/        String temp;
/*ADD*/        while ((temp = br.readLine()) != null) {
/*ADD*/            line += temp;
/*ADD*/        }
/*ADD*/        br.close();
/*ADD*/        isr.close();
/*ADD*/        is.close();
/*ADD*/        return line;
/*ADD*/    }
/*ADD*/    
/*ADD*/    public static String GetShortPathName(String path) {
/*ADD*/        char[] result = new char[256];
/*ADD*/        Kernel32.INSTANCE.GetShortPathName(path, result, result.length);
/*ADD*/        return Native.toString(result);
/*ADD*/    }

    @SuppressWarnings("UseSpecificCatch")
    public static ArrayList scanFolder() {
        final ArrayList list = new ArrayList();
        Runtime rt = Runtime.getRuntime();
        MarketSession session = new MarketSession(false);
        Config cf = APKDownloaderApp.config;
        ScanOption so = APKDownloaderApp.so;
        session.login(cf.getEmail(), cf.getPassword(), cf.getDeviceId());
/*ADD*/        String filenameCopy = null;
        try {
            File folder = new File("".equals(so.folder) ? File.separator : so.folder);
            File[] listOfFiles = folder.listFiles();
/*ADD*/            int enqueuedFiles = 0;

            for (int i = 0; i < listOfFiles.length; i++) {
                String files;
                String line = "";
                final String filename, packageName, localVersion, localName;
                final double filesize;

                if (listOfFiles[i].isFile()) {
                    files = listOfFiles[i].getAbsolutePath();
/*ADD filenameCopy =*/                    filename = filenameCopy = listOfFiles[i].getName();
                    filesize = (double) (listOfFiles[i].length()) / (1024 * 1024);
                   if (files.toLowerCase().endsWith(".apk")) {
                        Process p = rt.exec("lib" + File.separator + "aapt d badging \"" + files + "\"", null, new File("lib"));
/*ADD*/                        line = getProcessOutput( p );

///*DEL*/                        InputStream is = p.getInputStream();
///*DEL*/                        InputStreamReader isr = new InputStreamReader(is);
///*DEL*/                        BufferedReader br = new BufferedReader(isr);
///*DEL*/                        String temp;
///*DEL*/                        while ((temp = br.readLine()) != null) {
///*DEL*/                            line += temp;
///*DEL*/                        }
///*DEL*/                        br.close();
///*DEL*/                        isr.close();
///*DEL*/                        is.close();

/*ADD*/                        if( 0 != p.waitFor()) {
/*ADD*/                            try {
/*ADD*/                                File newFn = new File(GetShortPathName(files));
/*ADD*/                                p = rt.exec("lib" + File.separator + "aapt d badging \"" + newFn.getAbsolutePath() + "\"", null, new File("lib"));
/*ADD*/                                line = getProcessOutput( p );
/*ADD*/                            } catch( Exception e ) {}
/*ADD*/                        }

                        packageName = line.replaceAll("^package: name=\'|\'.*$", "");
                        if ("".equals(packageName)) {
                            final ArrayList tempAl = new ArrayList();
                            tempAl.add(filename);
                            tempAl.add(df.format(filesize) + " MB");
                            tempAl.add(" ");
                            tempAl.add(" ");
                            tempAl.add(" ");
                            tempAl.add(" ");
                            tempAl.add(" ");
                            tempAl.add("unknown");
                            list.add(tempAl);
                            continue;
                        }
                        localVersion = line.replaceAll("^.*versionName=\'|\'.*$", "");
                        localName = line.replaceAll("^.*application-label:\'|\'.*$", "");

                        AppsRequest appsRequest = AppsRequest.newBuilder().setQuery("pname:" + packageName).setStartIndex(0).setEntriesCount(1).setWithExtendedInfo(false).build();
                        Callback<AppsResponse> callback = new MarketSession.Callback<AppsResponse>() {

                            @Override
                            public void onResult(ResponseContext context, AppsResponse response) {
                                if (response.getAppCount() != 0) {
                                    App app = response.getApp(0);
                                    final ArrayList tempAl = new ArrayList();
                                    tempAl.add(filename);
                                    tempAl.add(df.format(filesize) + " MB");
                                    tempAl.add(packageName);
                                    tempAl.add(localName);
                                    tempAl.add((app.getPrice().equals("") || app.getPrice() == null) ? "Бесплатно" : app.getPrice());
                                    tempAl.add(localVersion);
                                    tempAl.add(app.getVersion());
                                    String stat = app.getVersion().equals(localVersion) ? "good" : "bad";
                                    tempAl.add(app.getId() + "#" + stat);
                                    list.add(tempAl);
                                } else {
                                    final ArrayList tempAl = new ArrayList();
                                    tempAl.add(filename);
                                    tempAl.add(df.format(filesize) + " MB");
                                    tempAl.add(packageName);
                                    tempAl.add(localName);
                                    tempAl.add(" ");
                                    tempAl.add(localVersion);
                                    tempAl.add(" ");
                                    tempAl.add("unknown");
                                    list.add(tempAl);
                                }
                            }
                        };
                        session.append(appsRequest, callback);
/*ADD*/                        ++enqueuedFiles;
/*ADD*/                        if (enqueuedFiles % 19 == 0 && enqueuedFiles != 0) {
                            session.flush();
/*ADD*/                            enqueuedFiles = 0;
/*ADD*/                        }

///*DEL*/                        if (i % 19 == 0 && i != 0) {
///*DEL*/                            session.flush();
///*DEL*/                        }
                    } // if
                } // if
/*ADD*/                else {
/*ADD*/                        final String s = filenameCopy;
/*ADD*/                    }
            } //for
/*ADD*/            if( 0 != enqueuedFiles )
            session.flush();

            if (list.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Нет такого .apk файла в папке!", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            }

        } catch (Exception e) {
/*ADD*/            if( null == filenameCopy )
                JOptionPane.showMessageDialog(null, "Произошла ошибка во время сканирования папки", "Сообщение об ошибке", JOptionPane.OK_OPTION);
/*ADD*/            else
/*ADD*/                JOptionPane.showMessageDialog(null, "Произошла ошибка во время сканирования папки:\nОшибка при обработке пакета: " + filenameCopy + "\n" + e.getMessage(), "Сообщение об ошибке", JOptionPane.OK_OPTION);
        }
        return list;
    }

    public static boolean deleteFile(String name) {
        String prefix = APKDownloaderApp.so.folder + File.separator;
        File file = new File(prefix + name);
        return file.delete();
    }

}