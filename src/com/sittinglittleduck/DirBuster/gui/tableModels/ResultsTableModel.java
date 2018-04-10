/*
 * ResultsTableModel.java
 *
* Copyright 2007 James Fisher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.sittinglittleduck.DirBuster.gui.tableModels;
import com.sittinglittleduck.DirBuster.*;
import java.util.List;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

public class ResultsTableModel extends DefaultTableModel
{
    Vector<Result> data;
    
    String[] columnNames = { new String("Type"), new String("Found"), new String("Response"), new String("Size")};
    
    Manager manager;

    private int numberOfFiles = 0;
    private int numberOfDirs = 0;
    
    /** Creates a new instance of ResultsTableModel */
    public ResultsTableModel()
    {
        
        this.manager = Manager.getInstance();
        data = manager.results;
    }
    
    
    public void setColumnName(int index, String name)
    {
        if (index < columnNames.length)
            columnNames[index] = name;
    }
    
    @Override
    public boolean isCellEditable(int row, int col)
    {
        /*
        if(col == 4)
        {
            ResultsTableObject tempObj = (ResultsTableObject) data.get(row);
            
            if(tempObj.getFieldStatus().equalsIgnoreCase("Finished") 
                || tempObj.getFieldStatus().equalsIgnoreCase("Scanning")
                || !tempObj.getFieldType().equalsIgnoreCase("dir"))
            {
                return false;
            }
                return true;
        }
         */
        
        return false;
    }
    
    public Class getColumnClass(int c) 
    {
            return getValueAt(0, c).getClass();
    }
    
    public void addRow()
    {
    //   //check the item is not already in the table
    //    if(!data.contains(object))
    //    {
            //data.add(object);
            this.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    //    }
        
    }
    
    public void updateRow(String dirFinished, String dirStarted)
    {
        /*
        ResultsTableObject[] tempArray = new ResultsTableObject[data.size()];
        if(!data.isEmpty())
        {
            tempArray = (ResultsTableObject[]) data.toArray(tempArray);
            for(int a = 0; a < tempArray.length; a++)
            {
                if(tempArray[a].getFieldFound().equals(dirFinished))
                {
                    tempArray[a].setFieldStatus("Finished");
                    
                }
                
                if(tempArray[a].getFieldFound().equals(dirStarted))
                {
                    tempArray[a].setFieldStatus("Scanning");
                }
            }
            
            
            while(!data.isEmpty())
            {
                data.remove(0);
            }
            
            for(int b = 0; b  < tempArray.length; b++)
            {
                ResultsTableObject temp = tempArray[b];
                data.add(temp);
            }
            
            this.fireTableDataChanged();
        }
         */
    }
    
    public void removeRow(int index)
    {
        data.remove(index);
        fireTableRowsDeleted(index, index);
    }
    
    //public Class getColumnClass(int c)
    //{
        //return getValueAt(0, c).getClass();
   // }
    
    public int getRowCount()
    {
        return data==null ? 0 : data.size();
    }
    
    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }
    
    public String getColumnName(int col)
    {
        return columnNames[col];
    }
    
    
    @Override
    public Object getValueAt(int row, int col)
    {
        if( row < 0 || row >= data.size())
        {
            return null;
        }

        if (col==0)
        {
            if(data.get(row).getType() == Result.DIR)
            {
                return "Dir";
            }
            else
            {
                return "File";
            }
        }
        else if (col==1)
        {
            return data.get(row).getItemFound().getPath();
        }
        else if (col==2)
        {
            return data.get(row).getResponceCode();
        }
        else if(col == 3)
        {
            if(data.get(row).getResponseHeader().equals("") && data.get(row).getResponseBody().equals(""))
            {
                return 0;
            }
            return data.get(row).getResponseHeader().length() + data.get(row).getResponseBody().length();
        }
        else
        {
            return null;
        }
    }

    /*
    public String getRowData(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldType() + temp.getFieldFound();
    }
    
    public String getRowResponceCode(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldFound();
    }
    
    public String getRowResponse(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getResponce();
    }
    
    public DirToCheck getDirToCheck(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getDirToCheck();
    }
    
    public String getRowRawResponse(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getRawResponce();
    }
    
    public String getBaseCase(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        //System.out.println("Getting Responce for row: " + row);
        //System.out.println("Responce = " + temp.getResponce());
        return temp.getBaseCase();
    }
    
    public BaseCase getBaseCaseObj(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getBaseCaseObj();
    }
    
    public String getSelectedURL(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFullURL();
    }
    public String getSelectedStatus(int row)
    {
        if ( row < 0 || row >= data.size() ) return null;
        ResultsTableObject temp = (ResultsTableObject) data.get(row);
        return temp.getFieldStatus();
    }
     */
    
    public void setValueAt(Object value, int row, int col) 
    {
        /*
        //data[row][col] = value;
        //fireTableCellUpdated(row, col);
        
        ResultsTableObject[] tempArray = new ResultsTableObject[data.size()];
        if(!data.isEmpty())
        {
            tempArray = (ResultsTableObject[]) data.toArray(tempArray);
            
            
            
            while(!data.isEmpty())
            {
                data.remove(0);
            }
            
            for(int b = 0; b  < tempArray.length; b++)
            {
                ResultsTableObject temp = tempArray[b];
                data.add(temp);
            }
            
            this.fireTableDataChanged();
        }
         */
    }
    
    public List getList()
    {
        return data;
    }
    
    public void clearData()
    {
        data.clear();
        
    }

    public void setManager(Manager manager)
    {
        this.manager = manager;
    }
    
    public int getNumberOfDirs()
    {
        return -1;
    }

}
