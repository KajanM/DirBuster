/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sittinglittleduck.DirBuster.gui.tableModels;

import java.net.URL;


/**
 *
 * @author james
 */
public class ErrorTableObject 
{

    
    private URL url;
    private String reason;
    
    public ErrorTableObject(URL url, String reason)
    {
        this.reason = reason;
        this.url = url;
    }

    public URL getUrl()
    {
        return url;
    }

    public String getReason()
    {
        return reason;
    }
    
    
    

    

}
