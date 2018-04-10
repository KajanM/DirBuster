/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sittinglittleduck.DirBuster.gui.tableModels;

import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.workGenerators.MultiThreadedGenerator;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGeneratorMultiThreaded;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author James
 */
public class ScanInfoTableModel extends AbstractTableModel
{
    /*
     * Table column name
     */
    private String[] columnNames =
    {
        new String("Task"), new String("Progress"), new String("Control")
    };
    /*
     * Store of the data
     */
    private Vector<ErrorTableObject> data;
    private JTable table;
    Manager manager;

    public ScanInfoTableModel(JTable table)
    {
        manager = Manager.getInstance();
        this.table = table;
        this.data = manager.errors;
    }

    /*
     * Function to set the table column names
     */
    public void setColumnName(int index, String name)
    {
        if(index < columnNames.length)
        {
            columnNames[index] = name;
        }
    }

    /*
     * Set the column names
     */
    @Override
    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    /*
     * Cells are not to be editited
     */
    @Override
    public boolean isCellEditable(int row, int col)
    {
        if(col == 2)
        {
            return true;
        }
        return false;
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        MultiThreadedGenerator gen = manager.genThreads.elementAt(row);
        
        //if(row < 0 || row >= data.size())
        //{
        //    return null;
        //}
        if(col == 0)
        {
            if(gen.getType() == WorkerGeneratorMultiThreaded.doDIR)
            {
                return "Testing for dirs in " + gen.getStartpoint();
            }
            else if(gen.getType() == WorkerGeneratorMultiThreaded.doFile)
            {
                return "Testing for files in " + gen.getStartpoint() + " with extention " + gen.getFileExt();
            }
            else
            {
                return null;
            }
            
            //return 
        }
        else if(col == 1)
        {
            //TODO complete this to use a progress bar
            //return gen.getCurrentPoint() + "/" + manager.getTotalPass();
            return gen.getCurrentPoint();
        }
        else if(col == 2)
        {
            //TODO complete this to have the buttons on 
            return "";
        }
        else
        {
            return null;
        }
    }

    /*
     * 
     * 
    public void addRow(ErrorTableObject object)
    {

        //check the item is not already in the table
        if( ! data.contains(object))
        {
            data.addElement(object);
            if(table.getRowSorter() != null)
            {
                int location = table.getRowSorter().convertRowIndexToView(data.size() - 1);
                
                if(location > 0)
                {
                    this.fireTableRowsInserted(location - 1, location - 1);
                }
                else
                {
                    this.fireTableDataChanged();
                }
            }
            else
            {
                this.fireTableDataChanged();
            }
        }
        else
        {
            return;
        }
    }
     */

    public int getRowCount()
    {
        return data == null ? 0 : manager.genThreads.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Class getColumnClass(int c)
    {
        //return getValueAt(0, c).getClass();
        return columnNames[c].getClass();
    }

    public void clearAllResults()
    {
        data.removeAllElements();
        fireTableDataChanged();
    }
    
    public void refreshTable()
    {
        fireTableDataChanged();
    }
}
