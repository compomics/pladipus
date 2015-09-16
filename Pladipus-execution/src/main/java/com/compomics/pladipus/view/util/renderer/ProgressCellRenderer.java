package com.compomics.pladipus.view.util.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * A custom renderer to show progress bars in a jTable
 *
 * @author Kenneth Verheggen
 * @author Harald Barsnes
 */
public class ProgressCellRenderer extends JProgressBar implements TableCellRenderer {

    public ProgressCellRenderer() {
        super(0, 100);
        setValue(0);
        setStringPainted(true);
    }

    @Override
    public boolean isDisplayable() {
        // This does the trick. It makes sure animation is always performed 
        return true;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setForeground(Color.BLACK);
        JLabel label = (JLabel) new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        Color bg = label.getBackground();

        // We have to create a new color object because Nimbus returns
        // a color of type DerivedColor, which behaves strange, not sure why.
        this.setOpaque(true);
        this.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue()));

        double parseDouble = 0;
        if (value != null) {
            parseDouble = Double.parseDouble(String.valueOf(value));

            int actualValue = Math.min(100, (int) parseDouble);
            //TODO make this a color index?        
            this.setValue(actualValue);
            this.setString(actualValue + "%");
        } else {
            this.setVisible(false);
        }
        return this;
    }
}
