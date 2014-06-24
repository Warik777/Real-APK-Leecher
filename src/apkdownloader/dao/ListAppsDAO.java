/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader.dao;

import apkdownloader.APKDownloaderApp;
import apkdownloader.APKDownloaderView;
import apkdownloader.DownloadBox;
import apkdownloader.entities.Config;
import com.gc.android.market.api.LoginException;
import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.MarketSession.Callback;
import com.gc.android.market.api.model.Market.App;
import com.gc.android.market.api.model.Market.AppsRequest;
import com.gc.android.market.api.model.Market.AppsResponse;
import com.gc.android.market.api.model.Market.GetAssetResponse.InstallAsset;
import com.gc.android.market.api.model.Market.GetImageRequest;
import com.gc.android.market.api.model.Market.GetImageRequest.AppImageUsage;
import com.gc.android.market.api.model.Market.GetImageResponse;
import com.gc.android.market.api.model.Market.ResponseContext;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author CodeBlue
 */
public class ListAppsDAO {

    public static List<App> responseApps = new ArrayList<App>();
    private int percent = 0;
    private static DecimalFormat df = new DecimalFormat("#,###,###,##0.00");

    private static void loadApps() {
        String strSearch = APKDownloaderView.searchString;
        if (!strSearch.isEmpty()) {
            MarketSession session = new MarketSession(false);
            APKDownloaderView.load.firePropertyChange("message", "", "Попытка входа в учетную запись...");
            Config cf = APKDownloaderApp.config;
            session.login(cf.getEmail(), cf.getPassword(), cf.getDeviceId());
            APKDownloaderView.load.firePropertyChange("message", "", "Вход успешный!");
            session.getContext().setUserLanguage(cf.getLocale().getLanguageCode());
            session.getContext().setUserCountry(cf.getLocale().getCountryCode());
            String sim = cf.getSimNumberic();
            if (sim != null) {
                session.getContext().setOperatorNumeric(sim);
                session.getContext().setSimOperatorNumeric(sim);
            }
            responseApps.clear();
            for (int i = 0; i < cf.getRecordShow(); i += 10) {
                AppsRequest appsRequest = AppsRequest.newBuilder().setQuery(strSearch).setStartIndex(i).setEntriesCount(10).setWithExtendedInfo(true).build();
                session.append(appsRequest, new Callback<AppsResponse>(){
//                Callback<AppsResponse> callback = new MarketSession.Callback<AppsResponse>() {

                    @Override
                    public void onResult(ResponseContext context, AppsResponse response) {
                        List<App> list = response.getAppList();
                        for (App app : list) {
                            responseApps.add(app);
                        }
                        APKDownloaderView.load.firePropertyChange("message", "", "Получение списка приложений ...");
                        if (responseApps.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Не удалось найти приложения с заданными параметрами поиска!\r\n", "Совпадений не найдено", JOptionPane.OK_OPTION);
                            return;
                        }
                    }
                });
//                session.append(appsRequest, callback);
            }
            session.flush();
        }
    }

    public static ArrayList loadFullApps() {
        loadApps();
        final ArrayList list = new ArrayList();
        if (responseApps != null) {
            if (!responseApps.isEmpty()) {
                MarketSession session = new MarketSession(false);
                Config cf = APKDownloaderApp.config;
                session.login(cf.getEmail(), cf.getPassword(), cf.getDeviceId());
                int i = 0;
                for (final App app : responseApps) {
                    i++;
                    GetImageRequest imgReq = GetImageRequest.newBuilder().setAppId(app.getId()).setImageUsage(AppImageUsage.ICON).setImageId("1").build();

                    session.append(imgReq, new Callback<GetImageResponse>() {
//                    Callback<GetImageResponse> callback = new Callback<GetImageResponse>() {

                        @Override
                        public void onResult(ResponseContext context, GetImageResponse response) {
                            Image img = Toolkit.getDefaultToolkit().createImage(response.getImageData().toByteArray());
                            ArrayList temp = new ArrayList();
                            temp.add(img);
                            temp.add(app.getTitle());
                            temp.add(app.getPackageName());
                            temp.add(app.getCreator());
                            temp.add(app.getVersion());
                            double dd = (double) (app.getExtendedInfo().getInstallSize()) / (1024 * 1024);
                            temp.add(df.format(dd) + " MB");
                            temp.add((app.getPrice().equals("") || app.getPrice() == null) ? "Бесплатно" : app.getPrice());
                            list.add(temp);
                        }
                    });
//                    session.append(imgReq, callback);
                    if (i == 19) {
                        session.flush();
                    }
                }

                session.flush();
            }
        }
        return list;
    }

    public static void downloadIcon(String assetId, String title) {
        try {
            MarketSession session = new MarketSession(false);

            Config cf = APKDownloaderApp.config;
            session.login(cf.getEmail(), cf.getPassword(), cf.getDeviceId());
            GetImageRequest imgReq = GetImageRequest.newBuilder().setAppId(assetId).setImageUsage(AppImageUsage.ICON).setImageId("1").build();
            final String fileToSave = cf.getFolderToSave() + (cf.getFolderToSave().equals("") ? "" : File.separator) + ConvertURL.toUrlFriendly(title).replaceAll(":", " -").replaceAll("/", " - ") + "_icon.png";
            session.append(imgReq, new Callback<GetImageResponse>() {

                @Override
                @SuppressWarnings("ConvertToTryWithResources")
                public void onResult(ResponseContext context, GetImageResponse response) {
                    try {
                        FileOutputStream fos = new FileOutputStream(fileToSave);
                        fos.write(response.getImageData().toByteArray());
                        fos.close();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Скачать иконку не удалось", "Сообщение об ошибке", JOptionPane.OK_OPTION);
                    }
                }
            });
            session.flush();
        } catch (LoginException le) {
            JOptionPane.showMessageDialog(null, "Войти не удалось! Пожалуйста, проверьте правильность ввода электронной почты или пароля в опциях.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Не удалось скачать из-за неправильного ID устройства или отсутствия соединения к интернету", "Сообщение об ошибке", JOptionPane.OK_OPTION);
        }
    }

    @SuppressWarnings("ConvertToTryWithResources")
    public void appDownload(String assetId, String title, int index) {
        try {
            MarketSession session = new MarketSession(true);

            Config cf = APKDownloaderApp.config;
            session.login(cf.getEmail(), cf.getPassword(), cf.getDeviceId());
            InstallAsset ia = session.queryGetAssetRequest(assetId).getInstallAsset(0);
            String cookieName = ia.getDownloadAuthCookieName();
            String cookieValue = ia.getDownloadAuthCookieValue();
            URL url = new URL(ia.getBlobUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Android-Market/2 (sapphire PLAT-RC33); gzip");
            conn.setRequestProperty("Cookie", cookieName + "=" + cookieValue);
            if (conn.getResponseCode() == 302) {
                String location = conn.getHeaderField("Location");
                url = new URL(location);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Android-Market/2 (sapphire PLAT-RC33); gzip");
                conn.setRequestProperty("Cookie", cookieName + "=" + cookieValue);
            }
            int appLength = conn.getContentLength();
            InputStream inputstream = (InputStream) conn.getInputStream();
            String fileToSave = cf.getFolderToSave() + (cf.getFolderToSave().equals("") ? "" : File.separator) + title;
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(fileToSave));
            byte[] buf = new byte[1024];
            int k = 0;
            int readed = 0;
            while ((k = inputstream.read(buf)) > 0) {
                stream.write(buf, 0, k);
                readed += k;
                percent = readed * 100 / appLength;
                if (appLength - readed > 1024) {
                    buf = new byte[1024];
                } else {
                    buf = new byte[appLength - readed];
                }
                DownloadBox.downloads.get(index).firePropertyChange("progress", 0, percent);
                if (DownloadBox.downloads.get(index).isCancelled()) {
                    break;
                }
            }
            inputstream.close();
            inputstream = null;
            stream.flush();
            stream.close();
            stream = null;
            System.gc();
            if (DownloadBox.downloads.get(index).isCancelled()) {
                File f = new File(fileToSave);
                f.delete();
            }
        } catch (LoginException le) {
            JOptionPane.showMessageDialog(null, "Войти не удалось! Пожалуйста, проверьте правильность ввода электронной почты или пароля в опциях.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "<html>Ошибка загрузки за счет: <br/>1. Неправильного ID устройства <br/>2. Отсутствия интернет соединения <br/>3. Вы не купили это приложение</html>", "Сообщение об ошибке", JOptionPane.OK_OPTION);
        }
    }

}