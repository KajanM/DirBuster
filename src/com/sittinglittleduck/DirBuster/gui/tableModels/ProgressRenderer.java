/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sittinglittleduck.DirBuster.gui.tableModels;

import com.sittinglittleduck.DirBuster.Manager;
import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author james
 */
public class ProgressRenderer extends DefaultTableCellRenderer
{

    private JProgressBar b = new JProgressBar();
    private Manager manager;

    public ProgressRenderer()
    {
        super();
        manager = Manager.getInstance();
        //setOpaque(true);
        b.setMaximum((int) manager.getTotalPass());
        b.setMinimum(0);

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column)
    {
        Integer i = (Integer) value;

        b.setMinimum(0);
        b.setMaximum((int) manager.getTotalPass());
        b.setStringPainted(true);
        if(i.intValue() == -1)
        {
            b.setValue((int) manager.getTotalPass());
            b.setString("Complete");
        }
        else
        {
            b.setValue(i); 
            int percent = (int) ((i.intValue() / manager.getTotalPass()) * 100);
            b.setString(percent + "%");
        }
        return b;
    }
}
