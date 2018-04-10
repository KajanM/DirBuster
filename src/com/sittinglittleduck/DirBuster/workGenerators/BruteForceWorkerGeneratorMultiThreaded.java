/*
 * BruteForceWorkerGeneratorMultiThreaded.java
 *
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
import com.sittinglittleduck.DirBuster.bruteForceIterator.BruteForceIterator;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;

/**
 * Produces the work to be done, when we are reading from a list
 */
public class BruteForceWorkerGeneratorMultiThreaded extends MultiThreadedGenerator
{
    private int minLen;
    private int maxLen;
    private String[] list;
    public static final int doDIR = 0;
    public static final int doFile = 1;
    private Manager manager = Manager.getInstance();
    private BlockingQueue<WorkUnit> workQueue;
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
    public BruteForceWorkerGeneratorMultiThreaded(String startpoint, String fileExt, int type, boolean first)
    {
        this.maxLen = manager.getMaxLen();
        this.minLen = manager.getMinLen();
        this.list = manager.getCharSet();

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

        BruteForceIterator bfi = new BruteForceIterator(minLen, maxLen, list);

        /*
         * set how many we are going to do.
         */
        manager.setTotalPass(bfi.getTotalToDo());

        while(bfi.hasNext())
        {
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

            if(manager.getAuto() &&  ! baseCaseObj.useContentAnalysisMode() &&  ! baseCaseObj.isUseRegexInstead())
            {
                method = "HEAD";
            }
            else
            {
                method = "GET";
            }
            String item = bfi.next();

            URL currentURL;
            try
            {
                if(type == doDIR)
                {
                    currentURL = new URL(firstPart + startpoint + item + "/");
                }
                else
                {
                    currentURL = new URL(firstPart + startpoint + item + fileExt);
                }
                workQueue.put(new WorkUnit(currentURL, type == doDIR, method, baseCaseObj, item));
                counter ++;
            }
            catch(InterruptedException ex)
            {
                Logger.getLogger(BruteForceURLFuzz.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(MalformedURLException ex)
            {
                Logger.getLogger(BruteForceURLFuzz.class.getName()).log(Level.SEVERE, null, ex);
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