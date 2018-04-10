/*
 * BruteForceURLFuzz.java
 *
 *
 * Copyright 2006 James Fisher
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
import com.sittinglittleduck.DirBuster.bruteForceIterator.BruteForceIterator;
import com.sittinglittleduck.DirBuster.GenBaseCase;
import com.sittinglittleduck.DirBuster.HTTPHeader;
import com.sittinglittleduck.DirBuster.Manager;
import com.sittinglittleduck.DirBuster.WorkUnit;
import com.sittinglittleduck.DirBuster.utils.HeadRequestCheck;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 *
 * @author James
 */
public class BruteForceURLFuzz implements Runnable
{

    private String[] list;
    private int minLen;
    private int maxLen;
    private Manager manager;
    private BlockingQueue<WorkUnit> workQueue;

    private String firstPart;
    private String finished;
    private String started;
    
    HttpClient httpclient;
    private String urlFuzzStart;
    private String urlFuzzEnd;

    private int counter = 0;
    private boolean isWorking = true;

    /** Creates a new instance of BruteForceWorkGenerator */
    public BruteForceURLFuzz()
    {
        manager = Manager.getInstance();

        this.maxLen = manager.getMaxLen();
        this.minLen = manager.getMinLen();
        this.list = manager.getCharSet();

        workQueue = manager.workQueue;
        
        firstPart = manager.getFirstPartOfURL();

        httpclient = manager.getHttpclient();

        urlFuzzStart = manager.getUrlFuzzStart();
        urlFuzzEnd = manager.getUrlFuzzEnd();

    }

    public void run()
    {
        //checks if the server surports heads requests

        HeadRequestCheck.test(firstPart);

        System.out.println("Starting fuzz on " + firstPart + urlFuzzStart + "{dir}" + urlFuzzEnd);
        manager.setStatus("Starting fuzz on " + firstPart + urlFuzzStart + "{dir}" + urlFuzzEnd);
        //manager.updateTable(finished, started);


        //store for the basecase object set to null;
        BaseCase baseCaseObj = null;
        try
        {
            baseCaseObj = GenBaseCase.genURLFuzzBaseCase(firstPart + urlFuzzStart, urlFuzzEnd);
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }



        //baseCaseObj = new BaseCase(null, failcode, true, failurl, baseCase);
        //call function to generate the brute force
        BruteForceIterator bfi = new BruteForceIterator(minLen, maxLen, list);
        
        /*
         * set how many we are going to do.
         */
        manager.setTotalPass(bfi.getTotalToDo());

        while(bfi.hasNext())
        {
            String method;
            if(manager.getAuto() && !baseCaseObj.useContentAnalysisMode() && !baseCaseObj.isUseRegexInstead())
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
                currentURL = new URL(firstPart + urlFuzzStart + item + urlFuzzEnd);
                workQueue.put(new WorkUnit(currentURL, true, method, baseCaseObj, item));
                counter++;
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
        //manager.youAreFinished();
        isWorking = false;
    }

    public int getCurrentDone()
    {
        return counter;
    }

    public boolean isWorking()
    {
        return isWorking;
    }

    public String getStartPoint()
    {
        return urlFuzzStart + "{dir}" + urlFuzzEnd;
    }
}
