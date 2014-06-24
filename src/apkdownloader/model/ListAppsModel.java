/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader.model;

import apkdownloader.dao.ListAppsDAO;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author CodeBlue
 */
public class ListAppsModel extends AbstractTableModel {
    
    private String[] columnNames = { "Иконка", "Имя приложения", "Название пакета", "Разработчик", "Версия", "Рамер", "Цена" };
    private ArrayList data = ListAppsDAO.loadFullApps();

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
        return false;
    }

}