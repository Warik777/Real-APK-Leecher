/*
 * APKDownloaderView.java
 */
package apkdownloader;

import apkdownloader.dao.ConvertURL;
import apkdownloader.dao.ListAppsDAO;
import apkdownloader.dao.MyTableFilter;
import apkdownloader.dao.ScanFolderDAO;
import apkdownloader.model.ListAppsModel;
import apkdownloader.model.ScanFolderModel;
import com.gc.android.market.api.LoginException;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

/**
 * The application's main frame.
 * 
 * 339 строка
 * А по поводу желательных доработок, так немного напрягает, что по ПКМ на приложении оно не выбирается в списке найденных.
 */
public class APKDownloaderView extends FrameView {

    ListAppsModel model = new ListAppsModel();
    ScanFolderModel scan = null;
    public static String searchString = "";
    private Highlighter simpleStripHL = HighlighterFactory.createSimpleStriping();
    JMenuItem download = new JMenuItem("Скачать");
    JMenuItem downloadIcon = new JMenuItem("Скачать иконку");
    JMenuItem browseToWeb = new JMenuItem("Посмотреть на Google Play");
    JMenuItem scanFolder = new JMenuItem("Сканировать папку");
    JMenuItem copyLink = new JMenuItem("Копировать ссылку");
    JMenuItem searchRelate = new JMenuItem("Поиск похожих приложений");
    JMenuItem deleteFile = new JMenuItem("Удалить");
    public static LoadTask load = null;
    MyTableFilter filterController = null;
    public boolean isFirstCheck = true;
    DocumentUndoManager undo = DocumentUndoManager.getInstance();
    
    public APKDownloaderView(SingleFrameApplication app) {
        super(app);

        initComponents();
        download.addActionListener(new ActionListener() {

            @Override
            @Action
            public void actionPerformed(ActionEvent e) {
                ShowDownloadBox downloadbox = new ShowDownloadBox(APKDownloaderApp.getApplication());
                getApplication().getContext().getTaskService().execute(downloadbox);
            }
        });
        download.setIcon(new ImageIcon(getClass().getResource("/images/download_16.png")));

        downloadIcon.addActionListener(new ActionListener() {

            @Override
            @Action
            public void actionPerformed(ActionEvent e) {
                DownloadIcon di = new DownloadIcon(APKDownloaderApp.getApplication());
                getApplication().getContext().getTaskService().execute(di);
            }
        });
        downloadIcon.setIcon(new ImageIcon(getClass().getResource("/images/download_icon.png")));

        copyLink.addActionListener(new ActionListener() {

            @Override
            @Action
            public void actionPerformed(ActionEvent e) {
                int realRow = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
                String packageId = jxTable.getModel().getValueAt(realRow, 2).toString();
                StringSelection stringSelection = new StringSelection("https://play.google.com/store/apps/details?id=" + packageId);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        copyLink.setIcon(new ImageIcon(getClass().getResource("/images/copy.png")));

        browseToWeb.addActionListener(new ActionListener() {

            @Override
            @Action
            public void actionPerformed(ActionEvent e) {
                int realRow = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
                String packageId = jxTable.getModel().getValueAt(realRow, 2).toString();
                try {
                    Desktop.getDesktop().browse(new URI("https://play.google.com/store/apps/details?id=" + packageId));
                } catch (URISyntaxException ex) {
                    Logger.getLogger(APKDownloaderView.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException io) {
                    Logger.getLogger(APKDownloaderView.class.getName()).log(Level.SEVERE, null, io);
                }
            }
        });
        browseToWeb.setIcon(new ImageIcon(getClass().getResource("/images/googleplay.png")));

        scanFolder.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
                APKDownloaderApp.so = new ScanOption(mainFrame, true);
                APKDownloaderApp.so.setLocationRelativeTo(mainFrame);
                APKDownloaderApp.getApplication().show(APKDownloaderApp.so);
                if (APKDownloaderApp.so.isSet) {
                    ScanTask scan = new ScanTask(APKDownloaderApp.getApplication());
                    getApplication().getContext().getTaskService().execute(scan);
                }
            }
        });
        scanFolder.setIcon(new ImageIcon(getClass().getResource("/images/radar.png")));

        searchRelate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchRelateApps();
            }
        });
        searchRelate.setIcon(new ImageIcon(getClass().getResource("/images/find.png")));

        deleteFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int realRow = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
                String filename = jxTable.getModel().getValueAt(realRow, 0).toString();
                if (!ScanFolderDAO.deleteFile(filename)) {
                    JOptionPane.showMessageDialog(null, "Удаление не удалось!", "Сообщение об ошибке", JOptionPane.OK_OPTION);
                    return;
                }
                ((ScanFolderModel) jxTable.getModel()).removeRow(realRow);
                jxTable.repaint();
                lbCount.setText("" + jxTable.getRowCount());
            }
        });
        deleteFile.setIcon(new ImageIcon(getClass().getResource("/images/delete.png")));

        popupMenu.add(download);
        popupMenu.add(downloadIcon);
        popupMenu.add(scanFolder);
        popupMenu.addSeparator();
        popupMenu.add(deleteFile);
        popupMenu.add(copyLink);
        popupMenu.add(searchRelate);
        popupMenu.add(browseToWeb);
        //download.setEnabled(false);
        browseToWeb.setEnabled(false);
        downloadIcon.setEnabled(false);
        copyLink.setEnabled(false);
        searchRelate.setEnabled(false);
        deleteFile.setEnabled(false);
        customizeTable();
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("Done");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            @Override
            @SuppressWarnings("ConvertToStringSwitch")
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        //CheckForUpdate check = new CheckForUpdate(APKDownloaderApp.getApplication());
        //getApplication().getContext().getTaskService().execute(check);
    }

    private void customizeTable() {
        jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jxTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, null, new Color(255, 102, 0)));
        filterController = new MyTableFilter(jxTable);
        BindingGroup filterGroup = new BindingGroup();
        filterGroup.addBinding(Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ, txtFilter, BeanProperty.create("text"), filterController, BeanProperty.create("filterString")));
        filterGroup.bind();
        TableColumnModel columnModel = jxTable.getColumnModel();
        
        jxTable.setTableHeader(new JXTableHeader(columnModel) {

            @Override
            public void updateUI() {
                super.updateUI();
                // need to do in updateUI to survive toggling of LAF 
                if (getDefaultRenderer() instanceof JLabel) {
                    ((JLabel) getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
                }
            }
        });

        jxTable.setDefaultRenderer("".getClass(), new MyTableRenderer.ColRenderer());
        rebuildSearchTableUI();
        jxTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

      @Override
      public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    download.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    browseToWeb.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    downloadIcon.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    copyLink.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    searchRelate.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    deleteFile.setEnabled(jxTable.getSelectedRowCount() == 0 ? false : true);
                    if (jxTable.getSelectedRowCount() > 0) {
                        int realRow = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
                        if (jxTable.getModel().getColumnCount() == 7) {
                            deleteFile.setEnabled(false);
                            if ("Бесплатно".equals(jxTable.getModel().getValueAt(realRow, 6).toString())) {
                                //download.setEnabled(false);
                            }
                        } else {
                            deleteFile.setEnabled(true);
                            if ("Бесплатно".equals(jxTable.getModel().getValueAt(realRow, 4).toString())) {
                                //download.setEnabled(false);
                            }
                        }
                        if (" ".equals(jxTable.getModel().getValueAt(realRow, 6))) {
                            browseToWeb.setEnabled(false);
                            downloadIcon.setEnabled(false);
                            copyLink.setEnabled(false);
                        }
                    }
                }
            }
        });
        jxTable.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                checkForTriggerEvent(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                checkForTriggerEvent(e);
            }

            @SuppressWarnings("CallToThreadDumpStack")
            private void checkForTriggerEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
            aboutBox = new APKDownloaderAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        APKDownloaderApp.getApplication().show(aboutBox);
    }

    @Action
    public void showDownloadBox(String appId, String title, String version) {
        JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
        DownloadBox downloadBox = new DownloadBox(appId, title, version);
        downloadBox.setLocationRelativeTo(mainFrame);
        APKDownloaderApp.getApplication().show(downloadBox);
    }

    @Action
    public void showOption() {
        JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
        Option option = new Option(mainFrame, true);
        option.setLocationRelativeTo(mainFrame);
        APKDownloaderApp.getApplication().show(option);
    }

    public void getSearchString() {
        String searchType = "";
        if (rbVendorSearch.isSelected()) {
            searchType = "pub:";
        }
        if (rbPackageSearch.isSelected()) {
            searchType = "pname:";
        }
        String txtMarketSearch = txtSearch.getText().trim();
        searchString = txtMarketSearch.isEmpty() ? "" : searchType + txtMarketSearch;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jXLabel1 = new org.jdesktop.swingx.JXLabel();
        txtFilter = new org.jdesktop.swingx.JXSearchField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jxTable = new org.jdesktop.swingx.JXTable();
        jXLabel2 = new org.jdesktop.swingx.JXLabel();
        txtSearch = new org.jdesktop.swingx.JXSearchField();
        rbCustomSearch = new javax.swing.JRadioButton();
        rbVendorSearch = new javax.swing.JRadioButton();
        rbPackageSearch = new javax.swing.JRadioButton();
        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        jXLabel3 = new org.jdesktop.swingx.JXLabel();
        lbCount = new org.jdesktop.swingx.JXLabel();
        jXLabel5 = new org.jdesktop.swingx.JXLabel();
        jXLabel4 = new org.jdesktop.swingx.JXLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        mnRenameAll = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnOption = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        mnCheckUpdate = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        buttonGroup1 = new javax.swing.ButtonGroup();
        popupMenu = new javax.swing.JPopupMenu();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(APKDownloaderView.class);
        mainPanel.setBackground(resourceMap.getColor("mainPanel.background")); // NOI18N
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        mainPanel.setName("mainPanel"); // NOI18N

        jXLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jXLabel1.setIcon(resourceMap.getIcon("jXLabel1.icon")); // NOI18N
        jXLabel1.setText(resourceMap.getString("jXLabel1.text")); // NOI18N
        jXLabel1.setName("jXLabel1"); // NOI18N

        txtFilter.setText(resourceMap.getString("txtFilter.text")); // NOI18N
        txtFilter.setToolTipText(resourceMap.getString("txtFilter.toolTipText")); // NOI18N
        txtFilter.setName("txtFilter"); // NOI18N
        txtFilter.setPrompt(resourceMap.getString("txtFilter.prompt")); // NOI18N
        txtFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFilterActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jxTable.setModel(model);
        jxTable.setToolTipText(resourceMap.getString("jxTable.toolTipText")); // NOI18N
        jxTable.setColumnControlVisible(true);
        jxTable.setGridColor(resourceMap.getColor("jxTable.gridColor")); // NOI18N
        jxTable.setHighlighters(simpleStripHL);
        jxTable.setName("jxTable"); // NOI18N
        jxTable.setRowHeight(48);
        jxTable.setShowGrid(true);
        jxTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jxTableMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jxTableMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jxTable);

        jXLabel2.setForeground(resourceMap.getColor("jXLabel2.foreground")); // NOI18N
        jXLabel2.setText(resourceMap.getString("jXLabel2.text")); // NOI18N
        jXLabel2.setName("jXLabel2"); // NOI18N

        txtSearch.setText(resourceMap.getString("txtSearch.text")); // NOI18N
        txtSearch.setToolTipText(resourceMap.getString("txtSearch.toolTipText")); // NOI18N
        txtSearch.setName("txtSearch"); // NOI18N
        txtSearch.setPrompt(resourceMap.getString("txtSearch.prompt")); // NOI18N
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSearchKeyPressed(evt);
            }
        });

        rbCustomSearch.setBackground(resourceMap.getColor("rbCustomSearch.background")); // NOI18N
        buttonGroup1.add(rbCustomSearch);
        rbCustomSearch.setSelected(true);
        rbCustomSearch.setText(resourceMap.getString("rbCustomSearch.text")); // NOI18N
        rbCustomSearch.setFocusable(false);
        rbCustomSearch.setName("rbCustomSearch"); // NOI18N

        rbVendorSearch.setBackground(resourceMap.getColor("rbVendorSearch.background")); // NOI18N
        buttonGroup1.add(rbVendorSearch);
        rbVendorSearch.setText(resourceMap.getString("rbVendorSearch.text")); // NOI18N
        rbVendorSearch.setFocusable(false);
        rbVendorSearch.setName("rbVendorSearch"); // NOI18N

        rbPackageSearch.setBackground(resourceMap.getColor("rbPackageSearch.background")); // NOI18N
        buttonGroup1.add(rbPackageSearch);
        rbPackageSearch.setForeground(resourceMap.getColor("rbPackageSearch.foreground")); // NOI18N
        rbPackageSearch.setText(resourceMap.getString("rbPackageSearch.text")); // NOI18N
        rbPackageSearch.setFocusable(false);
        rbPackageSearch.setName("rbPackageSearch"); // NOI18N

        jXTitledSeparator1.setForeground(resourceMap.getColor("jXTitledSeparator1.foreground")); // NOI18N
        jXTitledSeparator1.setFont(resourceMap.getFont("jXTitledSeparator1.font")); // NOI18N
        jXTitledSeparator1.setHorizontalAlignment(SwingConstants.CENTER);
        jXTitledSeparator1.setName("jXTitledSeparator1"); // NOI18N
        jXTitledSeparator1.setTitle(resourceMap.getString("jXTitledSeparator1.title")); // NOI18N

        jXLabel3.setForeground(resourceMap.getColor("jXLabel3.foreground")); // NOI18N
        jXLabel3.setText(resourceMap.getString("jXLabel3.text")); // NOI18N
        jXLabel3.setName("jXLabel3"); // NOI18N

        lbCount.setForeground(resourceMap.getColor("lbCount.foreground")); // NOI18N
        lbCount.setText(resourceMap.getString("lbCount.text")); // NOI18N
        lbCount.setName("lbCount"); // NOI18N

        jXLabel5.setText(resourceMap.getString("jXLabel5.text")); // NOI18N
        jXLabel5.setName("jXLabel5"); // NOI18N

        jXLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jXLabel4.setIcon(resourceMap.getIcon("jXLabel4.icon")); // NOI18N
        jXLabel4.setText(resourceMap.getString("jXLabel4.text")); // NOI18N
        jXLabel4.setName("jXLabel4"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 839, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtFilter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jXLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jXLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jXLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                        .addComponent(jXLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lbCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                            .addComponent(rbVendorSearch)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rbPackageSearch)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(rbCustomSearch))
                        .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(rbCustomSearch)
                                    .addComponent(rbPackageSearch)
                                    .addComponent(rbVendorSearch))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jXLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addComponent(jXLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jXLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lbCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jXLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jXLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 406, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        undo.registerDocumentHolder(txtFilter);
        undo.registerDocumentHolder(txtSearch);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(APKDownloaderView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
        exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);
        exitMenuItem.setText(resourceMap.getString("exitMenu.text"));

        menuBar.add(fileMenu);

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        mnRenameAll.setText(resourceMap.getString("mnRenameAll.text")); // NOI18N
        mnRenameAll.setName("mnRenameAll"); // NOI18N
        mnRenameAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnRenameAllActionPerformed(evt);
            }
        });
        jMenu1.add(mnRenameAll);

        jSeparator1.setName("jSeparator1"); // NOI18N
        jMenu1.add(jSeparator1);

        mnOption.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        mnOption.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/gear.png"))); // NOI18N
        mnOption.setText(resourceMap.getString("mnOption.text")); // NOI18N
        mnOption.setName("mnOption"); // NOI18N
        mnOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnOptionActionPerformed(evt);
            }
        });
        jMenu1.add(mnOption);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        mnCheckUpdate.setIcon(resourceMap.getIcon("mnCheckUpdate.icon")); // NOI18N
        mnCheckUpdate.setText(resourceMap.getString("mnCheckUpdate.text")); // NOI18N
        mnCheckUpdate.setName("mnCheckUpdate"); // NOI18N
        mnCheckUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnCheckUpdateActionPerformed(evt);
            }
        });
        helpMenu.add(mnCheckUpdate);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);
        aboutMenuItem.setText(resourceMap.getString("aboutMenu.text"));

        menuBar.add(helpMenu);

        statusPanel.setBackground(resourceMap.getColor("statusPanel.background")); // NOI18N
        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 841, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 671, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        popupMenu.setName("popupMenu"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    @Action
    private void txtSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == 10) {
            if (APKDownloaderApp.config == null) {
                JOptionPane.showMessageDialog(APKDownloaderApp.getApplication().getMainFrame(), "Вы должны заполнить информацию в опциях", "Сообщение об ошибке", 0);
                showOption();
                return;
            }
            load = new LoadTask(APKDownloaderApp.getApplication());
            getApplication().getContext().getTaskService().execute(load);
            jxTable.clearSelection();
            jxTable.repaint();
        }
    }//GEN-LAST:event_txtSearchKeyPressed

    private void mnOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnOptionActionPerformed
        // TODO add your handling code here:
        showOption();
    }//GEN-LAST:event_mnOptionActionPerformed

    private void txtFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFilterActionPerformed
        // TODO add your handling code here:
        jxTable.repaint();
    }//GEN-LAST:event_txtFilterActionPerformed

    private void mnRenameAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnRenameAllActionPerformed
        // TODO add your handling code here:
        renameAll();
    }//GEN-LAST:event_mnRenameAllActionPerformed

    private void mnCheckUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnCheckUpdateActionPerformed
        // TODO add your handling code here:
        isFirstCheck = false;
        CheckForUpdate check = new CheckForUpdate(APKDownloaderApp.getApplication());
        getApplication().getContext().getTaskService().execute(check);
    }//GEN-LAST:event_mnCheckUpdateActionPerformed

    private void jxTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jxTableMousePressed
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    RobotMousClick();
                    if(jxTable.getSelectedRows().length>0){
                        Point point = evt.getPoint();
                        int column = jxTable.columnAtPoint(point);
                        int row = jxTable.rowAtPoint(point);
                        jxTable.setColumnSelectionInterval(column, column);
                        jxTable.setRowSelectionInterval(row, row);
                    }
                }
    }//GEN-LAST:event_jxTableMousePressed

    private void jxTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jxTableMouseReleased
                if (evt.getButton() == MouseEvent.BUTTON3) {
                    if(jxTable.getSelectedRows().length>0){
                        Point point = evt.getPoint();
                        int column = jxTable.columnAtPoint(point);
                        int row = jxTable.rowAtPoint(point);
                        jxTable.setColumnSelectionInterval(column, column);
                        jxTable.setRowSelectionInterval(row, row);
                    }
                }
    }//GEN-LAST:event_jxTableMouseReleased

    public void RobotMousClick(){
                try {
                    Robot robot = new Robot();
                    // Simulate a mouse click
                    robot.mousePress(InputEvent.BUTTON1_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                    robot = null;
                } catch (AWTException ex) {
                    System.err.println(ex.getMessage());
                }
    }
    
    private void searchRelateApps() {
        int realRow = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
        String appName = jxTable.getModel().getValueAt(realRow, 3).toString();
        txtSearch.setText(appName);
        load = new LoadTask(APKDownloaderApp.getApplication());
        getApplication().getContext().getTaskService().execute(load);
        jxTable.repaint();
    }

    private void renameAll() {
        JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
        APKDownloaderApp.ro = new RenameOption(mainFrame, true);
        APKDownloaderApp.ro.setLocationRelativeTo(mainFrame);
        APKDownloaderApp.getApplication().show(APKDownloaderApp.ro);
        if (!APKDownloaderApp.ro.isSet) {
            return;
        }
        TableModel tmd = jxTable.getModel();
        for (int i = 0; i < tmd.getRowCount(); i++) {
            String name = tmd.getValueAt(i, 3).toString();
            String version = APKDownloaderApp.ro.renameType == 1 ? "" : " v" + tmd.getValueAt(i, 5).toString();
            tmd.setValueAt(ConvertURL.toUrlFriendly(name).replaceAll(":", " -") + version + ".apk", i, 0);
        }
    }

    public void rebuildSearchTableUI() {
        jxTable.getColumn(0).setCellRenderer(new MyTableRenderer.IconRenderer());
        jxTable.getColumn(1).setCellRenderer(new MyTableRenderer.ColorRenderer());
        jxTable.getColumn(5).setCellRenderer(new MyTableRenderer.ExtraRenderer());
        jxTable.getColumn(6).setCellRenderer(new MyTableRenderer.PriceRenderer());
        jxTable.getColumn(0).setMaxWidth(48);
        jxTable.getColumn(0).setMinWidth(48);
        jxTable.getColumn(4).setMaxWidth(80);
        jxTable.getColumn(6).setMaxWidth(60);
        jxTable.getColumn(5).setMaxWidth(100);
        lbCount.setText("" + jxTable.getRowCount());
        jxTable.setRowHeight(48);
        jxTable.setShowGrid(true);
    }

    public void rebuildScanTableUI() {
        jxTable.setRowHeight(24);
        jxTable.setShowGrid(false);
        jxTable.getColumn(0).setCellRenderer(new MyTableRenderer.ColorRenderer());
        jxTable.getColumn(1).setCellRenderer(new MyTableRenderer.ExtraRenderer());
        jxTable.getColumn(3).setCellRenderer(new MyTableRenderer.ColorRenderer());
        jxTable.getColumn(4).setCellRenderer(new MyTableRenderer.ExtraRenderer());
        jxTable.getColumn(7).setCellRenderer(new MyTableRenderer.StatusRenderer());
        jxTable.getColumn(0).setMinWidth(140);
        jxTable.getColumn(0).setMaxWidth(180);
        jxTable.getColumn(1).setMaxWidth(80);
        jxTable.getColumn(4).setMaxWidth(60);
        jxTable.getColumn(3).setMaxWidth(150);
        jxTable.getColumn(3).setMinWidth(120);
        jxTable.getColumn(5).setMaxWidth(80);
        jxTable.getColumn(6).setMaxWidth(80);
        jxTable.getColumn(7).setMaxWidth(30);
        jxTable.getColumn(7).setMinWidth(30);
        lbCount.setText("" + jxTable.getRowCount());
    }

    class DownloadIcon extends Task {

        public DownloadIcon(Application app) {
            super(app);
        }

        @Override
        protected Void doInBackground() {
            int realIndex = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
            String appId = "", title = "";
            if (jxTable.getModel().getColumnCount() == 7) {
                appId = ListAppsDAO.responseApps.get(realIndex).getId();
                title = ListAppsDAO.responseApps.get(realIndex).getTitle();
            } else {
                appId = jxTable.getModel().getValueAt(realIndex, 7).toString().replaceAll("#.*$", "");
                title = jxTable.getModel().getValueAt(realIndex, 3).toString();
            }

            firePropertyChange("message", "", "Загрузка иконки");
            ListAppsDAO.downloadIcon(appId, title);
            return null;
        }
    }

    class CheckForUpdate extends Task {

        public CheckForUpdate(Application app) {
            super(app);
        }

        @Override
        @SuppressWarnings({"UseSpecificCatch", "ConvertToTryWithResources"})
        protected Void doInBackground() {
            try {
        firePropertyChange("message", "", "Проверка обновлений...");
                URL url = new URL("https://raw.githubusercontent.com/Warik777/Real-APK-Leecher/master/version.txt");
                URLConnection con = url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine = in.readLine().replaceAll("^.*version:", "");
                if (!APKDownloaderApp.version.equals(inputLine)) {
                    JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
                    UpdateSoftware update = new UpdateSoftware(mainFrame, true, inputLine);
                    update.setLocationRelativeTo(mainFrame);
                    APKDownloaderApp.getApplication().show(update);
                }
                if (!isFirstCheck && APKDownloaderApp.version.equals(inputLine)) {
                    JOptionPane.showMessageDialog(null, "Ваше программное обеспечение уже обновлено", "Проверить обновления", JOptionPane.INFORMATION_MESSAGE);
                }
                in.close();
            } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Ошибка подключения к серверу.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            }
            return null;
        }
    }

    class ShowDownloadBox extends Task {

        public ShowDownloadBox(Application app) {
            super(app);
        }

        @Override
        protected Void doInBackground() {
            try {
                int realIndex = jxTable.convertRowIndexToModel(jxTable.getSelectedRow());
                String appId = "", title = "", version = "";
                if (jxTable.getModel().getColumnCount() == 7) {
                    appId = ListAppsDAO.responseApps.get(realIndex).getId();
                    title = ListAppsDAO.responseApps.get(realIndex).getTitle();
                    version = ListAppsDAO.responseApps.get(realIndex).getVersion();
                } else {
                    appId = jxTable.getModel().getValueAt(realIndex, 7).toString().replaceAll("#.*$", "");
                    title = jxTable.getModel().getValueAt(realIndex, 3).toString();
                    version = jxTable.getModel().getValueAt(realIndex, 6).toString();
                }
                if (APKDownloaderApp.config.getFormatFileType() == 3) {
                    JFrame mainFrame = APKDownloaderApp.getApplication().getMainFrame();
                    PutAName putName = new PutAName(mainFrame, true, title);
                    putName.setLocationRelativeTo(mainFrame);
                    putName.setVisible(true);
                    if (!putName.status) {
                        return null;
                    }
                    title = putName.appTitle;
                }
                showDownloadBox(appId, title, version);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Произошла ошибка! Закройте и попробуйте еще раз!", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            }
            return null;
        }
    }

    public class ScanTask extends Task {

        public ScanTask(Application app) {
            super(app);
        }

        @Override
        protected Void doInBackground() {
            try {
                firePropertyChange("message", "", "Сканирование ... Будьте терпеливы!");
                scan = new ScanFolderModel();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        firePropertyChange("message", "", "Загрузка данных в таблицу...");
                        jxTable.setModel(scan);
                        rebuildScanTableUI();
                    }
                });
      }catch (LoginException le) {
                JOptionPane.showMessageDialog(APKDownloaderApp.getApplication().getMainFrame(), "Войти не удалось! Пожалуйста, проверьте имя пользователя или пароль в настройках.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
      } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Не удается загрузить список приложений. Пожалуйста, проверьте соединение с интернетом.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            }
            return null;
        }
    }

    public class LoadTask extends Task {

        public LoadTask(Application app) {
            super(app);
        }

        @Override
        protected Void doInBackground() {
            try {
                getSearchString();
                model = new ListAppsModel();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        jxTable.setModel(model);
                        rebuildSearchTableUI();
                    }
                });


            } catch (LoginException le) {
                JOptionPane.showMessageDialog(APKDownloaderApp.getApplication().getMainFrame(), "Войти не удалось! Пожалуйста, проверьте имя пользователя или пароль в настройках.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                JOptionPane.showMessageDialog(null, "Не удается загрузить список приложений. Пожалуйста, проверьте соединение с интернетом.", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            }
            return null;
        }
    }
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private org.jdesktop.swingx.JXLabel jXLabel1;
    private org.jdesktop.swingx.JXLabel jXLabel2;
    private org.jdesktop.swingx.JXLabel jXLabel3;
    private org.jdesktop.swingx.JXLabel jXLabel4;
    private org.jdesktop.swingx.JXLabel jXLabel5;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private org.jdesktop.swingx.JXTable jxTable;
    private org.jdesktop.swingx.JXLabel lbCount;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mnCheckUpdate;
    private javax.swing.JMenuItem mnOption;
    private javax.swing.JMenuItem mnRenameAll;
    private javax.swing.JPopupMenu popupMenu;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JRadioButton rbCustomSearch;
    private javax.swing.JRadioButton rbPackageSearch;
    private javax.swing.JRadioButton rbVendorSearch;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private org.jdesktop.swingx.JXSearchField txtFilter;
    private org.jdesktop.swingx.JXSearchField txtSearch;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;

}
