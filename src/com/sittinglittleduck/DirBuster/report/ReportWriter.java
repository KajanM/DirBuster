/*
 * ReportWriter.java
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
package com.sittinglittleduck.DirBuster.report;

import com.sittinglittleduck.DirBuster.*;
import com.sittinglittleduck.DirBuster.gui.StartGUI;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Vector;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportWriter
{

    private String fileToWriteTo;
    private StartGUI gui;
    private Manager manager;
    private Vector<Result> data;

    /** Creates a new instance of ReportWriter */
    public ReportWriter(String fileToWriteTo, StartGUI gui)
    {
        this.fileToWriteTo = fileToWriteTo;
        this.gui = gui;
        manager = Manager.getInstance();
        data = manager.results;
    }
    
    public ReportWriter(String fileToWriteTo)
    {
        this.fileToWriteTo = fileToWriteTo;
        manager = Manager.getInstance();
        data = manager.results;
    }

    public void writeReport()
    {
        Vector<Result> data = manager.results;

        Vector<Result> dirs = new Vector<Result>(100, 10);
        Vector<Result> files = new Vector<Result>(100, 10);
        Vector errors = new Vector(100, 10);

        Vector dirCodes = new Vector(100, 10);
        Vector fileCodes = new Vector(100, 10);


        //split results
        for(int a = 0; a < data.size(); a ++)
        {

            if(data.elementAt(a).getType() == Result.FILE)
            {
                files.addElement(data.elementAt(a));
            }
            else if(data.elementAt(a).getType() == Result.DIR)
            {
                dirs.addElement(data.elementAt(a));
            }

        }

        //get responce codes for dirs
        for(int b = 0; b < dirs.size(); b ++)
        {
            Integer code = new Integer(dirs.elementAt(b).getResponceCode());
            if( ! dirCodes.contains(code))
            {
                dirCodes.addElement(code);
            }
        }

        //get responce codes for files
        for(int b = 0; b < files.size(); b ++)
        {
            Integer code = new Integer(files.elementAt(b).getResponceCode());
            if( ! fileCodes.contains(code))
            {
                fileCodes.addElement(code);
            }
        }

        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileToWriteTo + ".txt"));

            /*
             * write the report header
             */
            writeReportHeader(out);


            //section for reporting files
            if(dirs.size() > 0)
            {
                out.write("Directories found during testing:");
                out.newLine();
                out.newLine();
                for(int a = 0; a < dirCodes.size(); a ++)
                {
                    int code = ((Integer) dirCodes.elementAt(a)).intValue();
                    out.write("Dirs found with a " + code + " response:");
                    out.newLine();
                    out.newLine();
                    for(int b = 0; b < dirs.size(); b ++)
                    {
                        if(code == dirs.elementAt(b).getResponceCode() )
                        {
                            out.write(dirs.elementAt(b).getItemFound().getPath());
                            out.newLine();
                        }
                    }
                    out.newLine();

                }
                out.newLine();
                out.write("--------------------------------");
                out.newLine();
            }




            if(files.size() > 0)
            {
                out.write("Files found during testing:");
                out.newLine();
                out.newLine();
                for(int a = 0; a < fileCodes.size(); a ++)
                {
                    int code = ((Integer) fileCodes.elementAt(a)).intValue();
                    out.write("Files found with a " + code + " responce:");
                    out.newLine();
                    out.newLine();
                    for(int b = 0; b < files.size(); b ++)
                    {
                        if(code == files.elementAt(b).getResponceCode() )
                        {
                            out.write(files.elementAt(b).getItemFound().getPath());
                            out.newLine();
                        }

                    }
                    out.newLine();

                }
                out.newLine();
            }
            out.write("--------------------------------");
            out.newLine();

            if(errors.size() > 0)
            {
                out.write("Errors encountered during testing:");
                out.newLine();
                out.newLine();

                for(int a = 0; a < manager.errors.size(); a ++)
                {

                    out.write(manager.errors.elementAt(a).getUrl().toString() + " : " + manager.errors.elementAt(a).getReason());
                    out.newLine();

                }
                out.newLine();
            }

            out.flush();
            out.close();

        }
        catch(IOException e)
        {
            //handle error
        }
    }
    
    private void writeReportHeader(BufferedWriter out) throws IOException
    {

            out.write("DirBuster " + Config.version + " - Report");
            out.newLine();
            out.write("http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project");
            out.newLine();
            Date date = new Date(System.currentTimeMillis());
            out.write("Report produced on " + date);
            out.newLine();
            out.write("--------------------------------");
            out.newLine();
            out.newLine();


            out.write(manager.getProtocol() + "://" + manager.getHost() + ":" + manager.getPort());
            out.newLine();
            out.write("--------------------------------");
            out.newLine();
    }
    
    public void writeSimpleListDirsOnly()
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(fileToWriteTo + "-simple.txt"));
            writeSimpleListDirs(out);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeSimpleListFilesOnly()
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(fileToWriteTo + "-simple.txt"));
            writeSimpleListFiles(out);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeSimpleListFilesAndDirs()
    {

       BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(fileToWriteTo + "-simple.txt"));
            writeSimpleListDirs(out);
            writeSimpleListFiles(out);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    public void writeXML()
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(fileToWriteTo + ".xml"));
            
            /*
             * xml header
             */
            out.write("<?xml version=\"1.0>");
            out.newLine();

            /*
             * start of dirbuster results
             */
            out.write("<DirBusterResults>");
            out.newLine();

            /*
             * print all the results
             */
            for(int a = 0; a < data.size(); a++)
            {
                String type = "";

                if(data.elementAt(a).getType() == Result.DIR)
                {
                    type = "Dir";
                }
                else if(data.elementAt(a).getType() == Result.FILE)
                {
                    type = "File";
                }

                out.write("<Result type=\"" + type + "\" path=\"" + data.elementAt(a).getItemFound().getPath() + "\" responseCode=\"" + data.elementAt(a).getResponceCode() + "\" />");
                out.newLine();
            }

            /*
             * end of dirbuster results
             */
            out.write("</DirBusterResults>");
            out.newLine();

        }
        catch(IOException ex)
        {
            Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void writeCSV()
    {
        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter(fileToWriteTo + ".csv"));
            /*
             * write the csv header
             */

            out.write("\"Found\",\"Response Code\",\"Content Length\"");
            out.newLine();

            /*
             * write each item found
             */
            for(int a = 0; a < data.size(); a++)
            {
                out.write("\"" + data.elementAt(a).getItemFound().getPath() + "\",\"" + data.elementAt(a).getResponceCode() + "\",\"" + (data.elementAt(a).getResponseHeader() + data.elementAt(a).getResponseBody()).length() + "\"");
                out.newLine();
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ReportWriter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void writeSimpleListDirs(BufferedWriter out) throws IOException
    {
        for(int a = 0; a < data.size(); a ++)
        {
            if(data.elementAt(a).getType() == Result.DIR)
            {
                out.write(data.elementAt(a).getItemFound().getPath());
                out.newLine();
            }
        }
    }

    private void writeSimpleListFiles(BufferedWriter out) throws IOException
    {
        for(int a = 0; a < data.size(); a ++)
        {
            if(data.elementAt(a).getType() == Result.FILE)
            {
                out.write(data.elementAt(a).getItemFound().getPath());
                out.newLine();
            }
        }
    }
}
