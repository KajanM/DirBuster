/*
 * WorkerGenerator.java
 *
 * Copyright 2008 James Fisher
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
package com.sittinglittleduck.DirBuster.workGenerators;

import com.sittinglittleduck.DirBuster.BaseCase;
import com.sittinglittleduck.DirBuster.Config;
import com.sittinglittleduck.DirBuster.GenBaseCase;
import com.sittinglittleduck.DirBuster.utils.HeadRequestCheck;
import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.WorkUnit;
import com.sittinglittleduck.DirBuster.utils.Utils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;

/**
 * Produces the work to be done, when we are reading from a list
 */
public class WorkerGeneratorMultiThreaded extends MultiThreadedGenerator
{

    public static final int doDIR = 0;
    public static final int doFile = 1;
    private Manager manager;
    private BlockingQueue<WorkUnit> workQueue;
    private String inputFile;
    private String firstPart;
    private boolean stopMe = false;
    private boolean isWorking = true;
    private boolean first = false;
    HttpClient httpclient;
    /*
     * stores the type of work we need to generate
     */
    int type;
    /*
     * store the file extention we are going to use
     */
    String fileExt;
    /*
     * stores the point we are to start at
     */
    String startpoint;
    private int counter = 0;

    /*
     * flag to tell the pause this thread
     */
    private boolean pleasewait = false;

    /**
     * Creates a new instance of WorkerGenerator
     * @param manager Manager object
     */
    public WorkerGeneratorMultiThreaded(String startpoint, String fileExt, int type, boolean first)
    {
        manager = Manager.getInstance();
        workQueue = manager.workQueue;
        this.type = type;
        this.startpoint = startpoint;
        this.first = first;
        if(type == doFile)
        {
            if(fileExt.equals(""))
            {
                this.fileExt = fileExt;
            }
            else
            {
                this.fileExt = "." + fileExt;
            }
        }

        //get the vector of all the file extention we need to use
        //extToCheck = manager.getExtToUse();
        inputFile = manager.getInputFile();
        firstPart = manager.getFirstPartOfURL();

        httpclient = manager.getHttpclient();
    }

    /**
     * Thread run method
     */
    public void run()
    {

        isWorking = true;
        //System.out.println("Multi threaded work gen started for : " + startpoint + " type:" + type + " fileext: " + fileExt);

        /*
         * produce the for for the items with the list
         */

        BufferedReader d = null;
        String line = "";
        try
        {
            

            /*
             * tests if head requests work
             */
            HeadRequestCheck.test(firstPart);

            BaseCase baseCaseObj = null;
            try
            {
                if(type == doDIR)
                {
                    baseCaseObj = GenBaseCase.genBaseCase(firstPart + startpoint, true, null);
                }
                else
                {
                    baseCaseObj = GenBaseCase.genBaseCase(firstPart + startpoint, false, fileExt);
                }
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            String method;
            if(manager.getAuto() &&  ! baseCaseObj.useContentAnalysisMode() &&  ! baseCaseObj.isUseRegexInstead())
            {
                method = "HEAD";
            }
            else
            {
                method = "GET";
            }
            d = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));

            /*
             * stores how many we have added
             */


            /*
             * TODO deal with the very very first case
             */
            if(first)
            {
                try
                {

                    URL currentURL = new URL(firstPart + startpoint);
                    if( ! manager.isHeadLessMode())
                    {
                        if(startpoint.equals(manager.gui.jPanelSetup.jTextFieldDirToStart.getText()))
                        {
                            //manager.updateTable("", startpoint);
                        }
                    }
                    workQueue.put(new WorkUnit(currentURL, true, "GET", baseCaseObj, null));
                    if(Config.debug)
                    {
                        System.out.println("DEBUG WokerGen: adding first dir to work list " + method + " " + startpoint);
                    }
                }
                catch(MalformedURLException ex)
                {
                    ex.printStackTrace();
                }
                catch(InterruptedException ex)
                {
                    ex.printStackTrace();
                }

            }

            /*
             * loop through the file
             */

            while((line = d.readLine()) != null)
            {
                URL currentURL = null;
                /*
                 * if stop is called
                 */
                if(stopMe)
                {
                    isWorking = false;
                    return;
                }

                /*
                 * if we need to pause the thread
                 */
                synchronized(this)
                {
                    while(pleasewait)
                    {
                        try
                        {
                            wait();
                        }
                        catch(InterruptedException e)
                        {
                            return;
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                /*
                 * skip blank and commented lines
                 */
                if( ! line.equalsIgnoreCase("") &&  ! line.startsWith("#"))
                {
                    line = line.trim();
                    line = Utils.makeItemsafe(line);

                    /*
                     * create the required URL
                     */
                    boolean isDir = false;
                    if(type == doDIR)
                    {
                        currentURL = new URL(firstPart + startpoint + line + "/");
                        try
                        {
                            URI testURI = new URI(firstPart + startpoint + line + "/");
                        }
                        catch(URISyntaxException ex)
                        {
                            Logger.getLogger(WorkerGeneratorMultiThreaded.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        isDir = true;
                    }
                    else
                    {
                        currentURL = new URL(firstPart + startpoint + line + fileExt);
                    }

                    /*
                     * add it to the queue
                     */
                    workQueue.put(new WorkUnit(currentURL, isDir, method, baseCaseObj, line));
                    counter ++;
                }
            }

        }
        catch(FileNotFoundException ex)
        {
            Logger.getLogger(WorkerGeneratorMultiThreaded.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(InterruptedException ex)
        {
            Logger.getLogger(WorkerGeneratorMultiThreaded.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException ex)
        {
            Logger.getLogger(WorkerGeneratorMultiThreaded.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                d.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(WorkerGeneratorMultiThreaded.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        isWorking = false;
        counter = -1;
        return;
    }

    

    /**
     * Method to stop the manager while it is working
     */
    public void stopMe()
    {
        stopMe = true;
        counter = -1;
    }

    public boolean isWorking()
    {
        return isWorking;
    }

    public int getCurrentPoint()
    {
        return counter;
    }

    public String getFileExt()
    {
        return fileExt;
    }

    public String getStartpoint()
    {
        return startpoint;
    }

    public int getType()
    {
        return type;
    }

    public void pause()
    {
        this.pleasewait = true;
    }

    public void unPause()
    {
        this.pleasewait = false;
    }

    public boolean isPaused()
    {
        return pleasewait;
    }
}