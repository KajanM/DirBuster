/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sittinglittleduck.DirBuster.gui.tableModels;

import com.sittinglittleduck.DirBuster.Manager;
import java.awt.Component;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author james
 */
public class ButtonRenderer extends JButton implements TableCellRenderer
{

    Manager manager = Manager.getInstance();
    public ButtonRenderer()
    {
        super("Stop");
        this.setHorizontalAlignment(JLabel.CENTER);
        setOpaque(true);  // so JLabel background is painted
        //addActionListener(this);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        //int newValue = ((Integer) value).intValue();

        //switch(newValue)
        //{
        //    case 1:
        //        setIcon(youricon);
        //    case 2:
        //        setIcon(youricon2);
        //}

        if(isSelected)
        {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        }
        else
        {
            setForeground(table.getForeground());
            setBackground(table.getBackground());
        }
        repaint();
        return this;
    }

    //public void actionPerformed(ActionEvent e)
    //{
     //   int rowclicked = manager.gui.jPanelRunning.jTableScanInformation.getSelectedRow();
     //   System.out.println("button clicked was on row " + rowclicked);
    //}
}
