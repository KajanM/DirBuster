/*
 * HeadRequestCheck.java
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

import com.sittinglittleduck.DirBuster.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.HeadMethod;

/**
 *
 * @author James
 */
public class HeadRequestCheck
{

    public static void test(String url)
    {

        Manager manager = Manager.getInstance();

        HttpClient httpclient = manager.getHttpclient();

        if(manager.getAuto())
        {
            try
            {
                URL headurl = new URL(url);

                HeadMethod httphead = new HeadMethod(headurl.toString());

                /*
                 * set the custom HTTP headers
                 */
                Vector HTTPheaders = manager.getHTTPHeaders();
                for(int a = 0; a < HTTPheaders.size(); a ++)
                {
                    HTTPHeader httpHeader = (HTTPHeader) HTTPheaders.elementAt(a);
                    httphead.setRequestHeader(httpHeader.getHeader(), httpHeader.getValue());
                }
                int responceCode = httpclient.executeMethod(httphead);

                /*
                 * if the responce code is method not implemented or fails
                 */
                if(responceCode == 501 || responceCode == 400)
                {
                    //switch the mode to just GET requests
                    manager.setAuto(false);
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
        }

    }
}
