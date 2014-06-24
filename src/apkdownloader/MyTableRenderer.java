/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package apkdownloader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.text.NumberFormat;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import org.jdesktop.swingx.JXLabel;

/**
 *
 * @author CodeBlue
 */
public class MyTableRenderer {
    // turn the column foreground in blue
    public static class ColorRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setForeground(Color.BLUE);
            super.getTableCellRendererComponent(table, " " + value.toString(), isSelected,
                    hasFocus, row, column);
            return this;
        }
    }

    // custom reward column
    public static class RewardRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JPanel pn = new JPanel(new BorderLayout());
            JLabel lb = new JXLabel();
            pn.add(lb, BorderLayout.CENTER);
            lb.setHorizontalAlignment(SwingConstants.CENTER);
            lb.setText(value.toString());
            lb.setForeground(new Color(0, 0, 102));
            if (value.toString().equals("Highest Earn")) {
                pn.setBackground(new Color(204, 0, 0));

            } else {
                pn.setBackground(new Color(102, 204, 0));
            }
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            return pn;
        }
    }

    // render image
    public static class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Image img = null;
            String stat = value.toString().replaceAll("^.*#", "");
            if (stat.equals("good")) {
                img = getToolkit().getImage(getClass().getResource("/images/ok.png"));
            } else if(stat.equals("bad")) {
                img = getToolkit().getImage(getClass().getResource("/images/not-ok.png"));
            } else {
                img = getToolkit().getImage(getClass().getResource("/images/unknown.png"));
            }
            JXLabel lb = new JXLabel();
            lb.setSize(30, 24);
            lb.setHorizontalAlignment(SwingConstants.CENTER);
            lb.setIcon(new ImageIcon(img));
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            return lb;
        }
    }

    // render image
    public static class PriceRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value.toString().equals("Бесплатно")) {
                Image img = getToolkit().getImage(getClass().getResource("/images/free.png"));
                JXLabel lb = new JXLabel();
                lb.setSize(60, 48);
                lb.setHorizontalAlignment(SwingConstants.CENTER);
                lb.setIcon(new ImageIcon(img));
                super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);
                return lb;
            }
            Image img = getToolkit().getImage(getClass().getResource("/images/currency_dollar_blue.png"));
            JXLabel lb = new JXLabel(value.toString());
            lb.setSize(60, 48);
            lb.setForeground(new Color(221, 5, 5));
            lb.setFont(new Font("Tahoma", Font.BOLD, 11));
            lb.setHorizontalAlignment(SwingConstants.CENTER);
            lb.setHorizontalTextPosition(SwingConstants.CENTER);
            lb.setIcon(new ImageIcon(img));
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            return lb;
        }
    }

    // render image
    public static class IconRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Image img = (Image) value;
            JXLabel lb = new JXLabel();
            lb.setSize(48, 48);
            lb.setIcon(new ImageIcon(img));
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            return lb;
        }
    }

    // left align a space
    public static class ColRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, " " + value.toString(), isSelected,
                    hasFocus, row, column);
            return this;
        }
    }

    // center column
    public static class ExtraRenderer extends DefaultTableCellRenderer {

        public ExtraRenderer() {
            super();
            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
    }

    // convert salary to currency format
    public static class CurrencyRenderer extends DefaultTableCellRenderer {

        public CurrencyRenderer() {
            super();
            setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        }

        @Override
        public void setValue(Object value) {
            if ((value != null) && (value instanceof Number)) {
                Number numberValue = (Number) value;
                NumberFormat formater = NumberFormat.getCurrencyInstance();
                value = formater.format(numberValue.doubleValue());
            }
            super.setValue(value);
        }
    }

    public static class IDRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(SwingConstants.CENTER);
            setForeground(new Color(153, 0, 0));
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            return this;
        }
    }

}
