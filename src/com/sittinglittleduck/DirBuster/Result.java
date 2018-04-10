/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sittinglittleduck.DirBuster;

import java.net.URL;

/**
 *
 * @author james
 */
public class Result
{
    public static final int DIR = 0;
    public static final int FILE = 1;

    private int type = -1;
    private URL itemFound;
    private int responceCode = 0;
    private String responseHeader = "";
    private String responseBody = "";
    private BaseCase baseCaseObj = null;

    public Result(int type, URL itemFound, int responceCode, String responceHeader, String responseBody, BaseCase baseCaseObj)
    {
        this.type = type;
        this.itemFound = itemFound;
        this.responceCode = responceCode;
        this.responseHeader = responceHeader;
        this.responseBody = responseBody;
        this.baseCaseObj = baseCaseObj;
    }

    public BaseCase getBaseCaseObj()
    {
        return baseCaseObj;
    }

    public URL getItemFound()
    {
        return itemFound;
    }

    public String getResponseBody()
    {
        return responseBody;
    }

    public String getResponseHeader()
    {
        return responseHeader;
    }

    public int getResponceCode()
    {
        return responceCode;
    }

    public int getType()
    {
        return type;
    }
}