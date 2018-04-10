/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sittinglittleduck.DirBuster.gui.tableModels;

import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGeneratorMultiThreaded;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author james
 */
public class JButtonCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{

    private JButton b = new JButton("Stop");
    private Manager manager;
    private int action;

    public JButtonCellEditor()
    {
        manager = Manager.getInstance();
        b.setOpaque(true);
    
        //b.setFocusPainted(false);
        //b.setBorderPainted(false);  // fix problem 3
        b.addActionListener(this);

    }

   
    public Component getTableCellEditorComponent(JTable table, Object value,
                   boolean isSelected, int row, int column)
    {
     
        try {
            action = ((Integer)value).intValue();
        } catch (Exception e) {
            action = 0;
        }
        
        
            return b;
    }

    public void actionPerformed(ActionEvent e)
    {
        /*
        int rowclicked = manager.gui.jPanelRunning.jTableScanInformation.getSelectedRow();
        String scanningFor = "";
        if(manager.genThreads.elementAt(rowclicked).getType() == WorkerGeneratorMultiThreaded.doDIR)
        {
            scanningFor = "dirs in " + manager.genThreads.elementAt(rowclicked).getStartpoint();
        }
        else
        {
            scanningFor = "files with extention " +  manager.genThreads.elementAt(rowclicked).getFileExt() + " in " + manager.genThreads.elementAt(rowclicked).getStartpoint();
        }
        
        int n = JOptionPane.showConfirmDialog(
                manager.gui,
                "Are you sure you wish to stop scanning for: \n" + scanningFor, 
                "Are you sure?",
                JOptionPane.YES_NO_OPTION);
        //if the anwser is yes
        if (n == 0)
        {
            manager.genThreads.elementAt(rowclicked).stopMe();
        }
        
        this.fireEditingStopped();
         */
    }

    public Object getCellEditorValue()
    {
        return null;
    }
}
