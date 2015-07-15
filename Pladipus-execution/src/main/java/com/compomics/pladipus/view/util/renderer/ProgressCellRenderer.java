/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.pladipus.view.util.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A custom renderer to show progress bars in a jTable
 * @author Kenneth Verheggen
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
        this.setBackground(Color.GREEN);
        double parseDouble = 0;
        if (value != null) {
            parseDouble = Double.parseDouble(String.valueOf(value));

            int actualValue = Math.min(100, (int) parseDouble);
            //TODO make this a color index?        
            this.setValue(actualValue);
            this.setString(actualValue + "%");
        }else{
            this.setVisible(false);
        }
        return this;
    }

}
