/*
 * BruteForceIterator.java
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

package com.sittinglittleduck.DirBuster.bruteForceIterator;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author james
 */
public class BruteForceIterator implements Iterator<String>
{
    /*
     * min size for the string
     */
    private int min;

    /*
     * max size for the string
     */
    private int max;

    /*
     * char set used for the brute force
     */
    private String[] list;

    /*
     * queue to hold all th string that are produced, by the thread
     */
    private BlockingQueue<String> queue;

    /*
     * thread that will generate the items;
     */

    private BruteForceGenThread genThread;

    public BruteForceIterator(int min, int max, String[] list)
    {
        this.min = min;
        this.max = max;
        this.list = list;

        queue = new ArrayBlockingQueue<String>(10);
        
        /*
         * create the thread and start it.
         */
        genThread = new BruteForceGenThread(this.min, this.max, this.list, queue);
        new Thread(genThread).start();

    }

    public boolean hasNext()
    {
        if(queue.size() > 0 || genThread.isWorking)
        {
            return true;
        }

        return false;
    }

    public String next()
    {
        try
        {
            /*
             * little hack to prevent deadlock
             */
            String item = null;
            while((item = queue.poll(1, TimeUnit.SECONDS)) == null)
            {

            }
            return item;
        }
        catch(InterruptedException ex)
        {
            Logger.getLogger(BruteForceIterator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getTotalToDo()
    {
        double total = 0;
        for(int a = min; a <= max; a++)
        {
            total = total + Math.pow(list.length, a);
        }
        return total;
    }

}
