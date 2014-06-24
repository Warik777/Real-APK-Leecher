/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader.model;

import apkdownloader.APKDownloaderApp;
import apkdownloader.dao.ScanFolderDAO;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author CodeBlue
 */
public class ScanFolderModel extends AbstractTableModel {

    private String[] columnNames = { "Имя файла", "Размер", "Название пакета", "Имя", "Цена", "Текущая версия", "Последняя версия", " " };
    private ArrayList data = ScanFolderDAO.scanFolder();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.isEmpty() ? "Null" : ((ArrayList) data.get(rowIndex)).get(columnIndex);
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class getColumnClass(int c) {
        if (getValueAt(0, c) == null) {
            return String.class;
        }
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return col == 0;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (!value.toString().toLowerCase().endsWith(".apk")) {
            JOptionPane.showMessageDialog(null, "Имя файла должно быть с расширением .apk", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            return;
        }
        String prefix = APKDownloaderApp.so.folder + "\\";
        String old = getValueAt(row, col).toString();
        if (old.equals(value.toString())) {
            return;
        }
        File oldFile = new File(prefix + old);
        File newFile = new File(prefix + value.toString());
        if (!oldFile.renameTo(newFile)) {
            JOptionPane.showMessageDialog(null, "Переименовать" + old + " failed due to wrong filename format or filename was exist", "Сообщение об ошибке", JOptionPane.OK_OPTION);
            return;
        }
        ((ArrayList) data.get(row)).set(col, value);
        fireTableCellUpdated(row, col);

    }

    public void removeRow(int row) {
        data.remove(row);
        fireTableDataChanged();
    }

}