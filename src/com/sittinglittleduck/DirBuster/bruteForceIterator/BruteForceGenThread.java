/*
 * BruteForceGenThread.java
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

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author james
 */
public class BruteForceGenThread implements Runnable
{
    private int min;
    private int max;
    private String[] list;
    private BlockingQueue<String> queue;
    private int[] listindex;
    boolean isWorking = true;

    public BruteForceGenThread(int min, int max, String[] list, BlockingQueue<String> queue)
    {
        this.min = min;
        this.max = max;
        this.list = list;
        this.queue = queue;
        listindex = new int[list.length];
    }

    public void run()
    {
        makeList(min, max);
        isWorking = false;
    }

    private void makeList(int minLen, int maxLen)
    {
        for (int x = minLen; x <= maxLen; x++)
        {
            while (listindex[0] < list.length)
            {
                showString(x);
                incrementCounter(x);
            }
            /* re-initialize the index */
            initIndex();
        }
    }


    private void showString(int len)
    {
        int chrx, endchr;
        String temp = "";
        /* print the current index */
        StringBuffer buf = new StringBuffer();
        for (int x = 0; x < len; x++)
        {
            chrx = listindex[x];
            //printf("%c", charlist[chrx]);
            buf.append(list[chrx]);
            //temp = temp + list[chrx];
        }
        temp = buf.toString();
        try
        {
            //System.out.println("bfi gen: " + temp);
            queue.put(temp);
        }
        catch(InterruptedException ex)
        {
            Logger.getLogger(BruteForceGenThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void incrementCounter(int len)
    {

        int x, z;
        int limit, last, check;

        /* nasty kludge */
        len--;

        limit = list.length;
        //printf("Limit is %d\n", limit);

        /* this sets the last octet of the index up by one */

        last = listindex[len];
        //printf("Last index was %d\n", last);
        last++;
        listindex[len] = last;
        //printf("set index to %d\n", chrindex[len]);

        /* this loop goes backwards through the index */
        /* each time determining if the char limit is reached */

        for (x = len; x > 0; x--)
        {
            //printf("Checking index %d of chrindex which is set to %d\n", x, chrindex[x]);
            if (listindex[x] == limit)
            {
                /* set this index to 0 */
                listindex[x] = 0;
                /* increment the next index */
                z = x - 1;
                listindex[z] = listindex[z] + 1;
                /* this loop should continue */
                //printf("Set index %d to 0 and incremented index %d by 1\n", x, z);
            }
        }

    }

    private void initIndex()
    {
        for(int a = 0; a < listindex.length; a++)
        {
            listindex[a] = 0;
        }
    }

    public boolean isWorking()
    {
        return isWorking;
    }

}
