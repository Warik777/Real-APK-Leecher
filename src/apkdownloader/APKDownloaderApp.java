/*
 * APKDownloaderApp.java
 */
package apkdownloader;

import apkdownloader.entities.Config;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class APKDownloaderApp extends SingleFrameApplication {

    public static Config config = null;
    public static ScanOption so = null;
    public static RenameOption ro = null;
    public static String version = "1.3.7.1";

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    @SuppressWarnings({"UseSpecificCatch", "CallToThreadDumpStack", "ConvertToTryWithResources"})
    protected void startup() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            try {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File f = new File(Config.configFile);
        if (!f.exists()) {
            showOption();
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Config.configFile));
                config = (Config) ois.readObject();
                ois.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ошибка при чтении файла конфигурации.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
                showOption();
            }
        }
        APKDownloaderView view = new APKDownloaderView(this);
        view.getFrame().setResizable(false);
        view.getFrame().setTitle("Real APK Leecher v"+version+" by [CodeBlue | Dmytro Ovdiienko | Warik777]");
        Image icon = view.getFrame().getToolkit().getImage(getClass().getResource("/images/app_icon.png"));
        view.getFrame().setIconImage(icon);
        show(view);
  }

    private void showOption() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        Option o = new Option(null, true);
        int x = (d.width - o.getWidth()) / 2;
        int y = (d.height - o.getHeight()) / 2;
        o.setLocation(x, y);
        o.setVisible(true);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {}

    /**
     * A convenient static getter for the application instance.
     * @return the instance of APKDownloaderApp
     */
    public static APKDownloaderApp getApplication() {
        return Application.getInstance(APKDownloaderApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
//        File f = new File("Real APK Leecher.exe");
//        if(f.getPath().contains("!")){
//            System.err.println("В названии папки приложения находится запрещенный символ \"!\"");
//            return;
//        }else {
            launch(APKDownloaderApp.class, args);
//        }
    }

}
