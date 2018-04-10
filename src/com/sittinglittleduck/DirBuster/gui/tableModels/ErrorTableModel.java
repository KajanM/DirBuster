/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sittinglittleduck.DirBuster.gui.tableModels;

import com.sittinglittleduck.DirBuster.Manager;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author James
 */
public class ErrorTableModel extends AbstractTableModel
{
    /*
     * Table column name
     */
    
    String start = "<html><font color=\"red\">";
    String end = "</font></html>";

    private String[] columnNames =
    {
        new String("Request"), new String("Error Message")
    };
    /*
     * Store of the data
     */
    private Vector<ErrorTableObject> data;
    private JTable table;
    Manager manager;

    public ErrorTableModel(JTable table)
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
        return false;
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        if(row < 0 || row >= data.size())
        {
            return null;
        }
        if(col == 0)
        {
            return start + data.elementAt(row).getUrl().toString() + end;
        }
        else if(col == 1)
        {
            return start + data.elementAt(row).getReason() + end;
        }
        else
        {
            return null;
        }
    }

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

    public int getRowCount()
    {
        return data == null ? 0 : data.size();
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }

    public void clearAllResults()
    {
        data.removeAllElements();
        fireTableDataChanged();
    }
}
