/*
 * Utils.java
 *
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

package com.sittinglittleduck.DirBuster.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James
 */
public class Utils
{

    public static int getNumberOfLineInAFile(String file)
    {
        BufferedReader d = null;
        String line;
        int passTotal = 0;
        try
        {
            d = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            passTotal = 0;
            while((line = d.readLine()) != null)
            {
                if( ! line.startsWith("#"))
                {
                    passTotal ++;
                }
            }

        }
        catch(FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try
            {
                d.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
            return passTotal;
        }
    }

    public static String makeItemsafe(String item)
    {
        //covert spaces
        item = item.replaceAll(" ", "%20");
        //remove "
        item = item.replaceAll("\"", "");
        //convert \ into /
        item = item.replaceAll("\\\\", "");


        if(item.length() > 2)
        {
            //remove / from the end
            if(item.endsWith("/"))
            {
                item = item.substring(1, item.length() - 1);
            }
            //remove / from the front
            if(item.startsWith("/"))
            {
                item = item.substring(2, item.length());
            }
        }
        else
        {
            //change a single / for DirBuster -> this stops errors and recursive loops
            if(item.startsWith("/"))
            {
                item = "DirBuster";
            }
        }
        return item;
    }
}
