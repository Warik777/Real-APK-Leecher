/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.*;

/**
 * Выплывающее меню в текстовом поле
 * @author Penkov Vladimir (update Warik777)
 */
/*
 * Пример использования
 * пользоваться им просто: каждое текстовое поле, для которого нужно показывать всплывающее меню, регистрируется в менеджере:
 * 
 *         DocumentUndoManager undo = DocumentUndoManager.getInstance();
 *         undo.registerDocumentHolder(textfield1);
 *         undo.registerDocumentHolder(textfield2);
 * 
 * после этого будет появляться меню.
 * для каждого поля ведется лог изменений. Если необходимо для какого-то поля сбросить лог, вызывается метод clearChangeHistory, параметром ему передается список тех полей, для которых нужно сбросить лог.
 * 
 *             DocumentUndoManager undo = DocumentUndoManager.getInstance();
 *             List textHolders = new ArrayList();
 *             textHolders.add(textfield1);
 *             textHolders.add(textfield2);
 *             undo.clearChangeHistory(textHolders);
 */
public class DocumentUndoManager {

    private static DocumentUndoManager instanse;
    private UndoActionListener listener;
    private HashMap items = new HashMap();
    private EditPopup popup;

    private DocumentUndoManager() {

        listener = new UndoActionListener();
        popup = new EditPopup();
    }

    public static DocumentUndoManager getInstance() {
        if (instanse==null) {
            instanse = new DocumentUndoManager();
        }
        return instanse;
    }

    public void registerDocumentHolder(JTextComponent documentHolder) {
        //создаем новый менеджер изменений документа
        UndoManager undo = new UndoManager();
        //запоминаем его для данного текстового поля
        items.put(documentHolder, undo);
        //получаем модель документа такстового поля
        Document doc = documentHolder.getDocument();
        //добавляем слушатель изменений документа
        doc.addUndoableEditListener(undo);
        //добавляем слушатель нажатий клавиш (он будет обрабатывать ctrl+z)
        documentHolder.addKeyListener(listener);
        //добавляем слушатель нажатий кнопок мыши (он обработает нажатие правой кнопки)
        documentHolder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //если нажата правая кнопка мыши
                if (e.getButton()==MouseEvent.BUTTON3) {
                //if (e.isPopupTrigger()) { // <-- в линуксе (Fedora Core 5, KDE 3.5.3) это не работает
                    //устанавливаем текстовый компонент для меню
                    popup.setDocumentHolder((JTextComponent) e.getSource());
                    //отображаем всплывающее меню
                    popup.show((JTextComponent)e.getSource(), e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * очищает историю изменений для текстовых компонент
     * @param documentHolders список компонент (из JTextComponent)
     */
    public void clearChangeHistory(List documentHolders) {
        for (Iterator iterator = documentHolders.iterator(); iterator.hasNext();) {
            JTextComponent textComponent = (JTextComponent) iterator.next();
            UndoManager manager = (UndoManager) items.get(textComponent);
            manager.discardAllEdits();
        }
    }

    class EditPopup extends JPopupMenu {

        private JMenuItem copy = new JMenuItem("Копировать");
        private JMenuItem cut = new JMenuItem("Вырезать");
        private JMenuItem paste = new JMenuItem("Вставить");
        private JMenuItem clear = new JMenuItem("Очистить");
        private JMenuItem undo = new JMenuItem("Отмена");

        private JTextComponent documentHolder = null;

        public EditPopup() {

            copy.setIcon(new ImageIcon(getClass().getResource("/images/copy.png")));
            copy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
            copy.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (documentHolder!=null) {
                        documentHolder.copy();
                    }
                }
            });

            cut.setIcon(new ImageIcon(getClass().getResource("/images/cut.png")));
            cut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
            cut.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (documentHolder!=null) {
                        documentHolder.cut();
                    }
                }
            });

            paste.setIcon(new ImageIcon(getClass().getResource("/images/paste.png")));
            paste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
            paste.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (documentHolder!=null) {
                        documentHolder.paste();
                    }
                }
            });
            
            clear.setIcon(new ImageIcon(getClass().getResource("/images/clear.png")));
            clear.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (documentHolder!=null) {
                        documentHolder.setText("");
                    }
                }
            });

            undo.setIcon(new ImageIcon(getClass().getResource("/images/undo.png")));
            undo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
            undo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (documentHolder!=null) {
                        UndoManager manager = (UndoManager) items.get(documentHolder);
                        if (manager.canUndo()) {
                            manager.undo();
                        }
                    }
                }
            });

            add(copy);
            add(cut);
            add(paste);
            add(clear);
            addSeparator();
            add(undo);
        }

        public void setDocumentHolder(JTextComponent documentHolder) {
            this.documentHolder = documentHolder;
            //устанавливаем доступность пунктов "копировать" и "вырезать"
            if (documentHolder.getSelectedText()!= null && documentHolder.getSelectedText()!= null) {
                copy.setEnabled(true);
                cut.setEnabled(true);
//                clear.setEnabled(true);
            }
            else {
                copy.setEnabled(false);
                cut.setEnabled(false);
//                clear.setEnabled(false);
            }

            //устанавливаем доступность пункта "вставить"
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(this);
            DataFlavor flavor = DataFlavor.stringFlavor;
            if (contents!=null && contents.isDataFlavorSupported(flavor)) {
                paste.setEnabled(true);
            }
            else {
                paste.setEnabled(false);
            }

            //устанавливаем доступность пункта "отмена"
            UndoManager manager = (UndoManager) items.get(documentHolder);
            if (manager.canUndo()) {
                undo.setEnabled(true);
                if("".equals(documentHolder.getText())){
                    clear.setEnabled(false);
                }else {
                clear.setEnabled(true);
                }
            }
            else {
                undo.setEnabled(false);
                clear.setEnabled(false);
            }

        }

    }

    class UndoActionListener extends KeyAdapter {

        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_Z && e.isControlDown()) {
                UndoManager manager = (UndoManager) items.get(e.getSource());
                if (manager.canUndo()) {
                    manager.undo();
                }
            }

        }
    }
}