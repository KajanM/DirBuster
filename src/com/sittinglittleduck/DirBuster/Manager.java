/*
 * Manager.java
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
package com.sittinglittleduck.DirBuster;

import com.sittinglittleduck.DirBuster.monitorThreads.ProcessEnd;
import com.sittinglittleduck.DirBuster.monitorThreads.ProcessChecker;
import com.sittinglittleduck.DirBuster.gui.JPanelScanInfo;
import com.sittinglittleduck.DirBuster.gui.tableModels.ErrorTableObject;
import com.sittinglittleduck.DirBuster.workGenerators.BruteForceWorkGenerator;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGenerator;
import com.sittinglittleduck.DirBuster.gui.StartGUI;
import com.sittinglittleduck.DirBuster.utils.Utils;
import com.sittinglittleduck.DirBuster.workGenerators.BruteForceURLFuzz;
import com.sittinglittleduck.DirBuster.workGenerators.BruteForceWorkerGeneratorMultiThreaded;
import com.sittinglittleduck.DirBuster.workGenerators.MultiThreadedGenerator;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGeneratorMultiThreaded;
import com.sittinglittleduck.DirBuster.workGenerators.WorkerGeneratorURLFuzz;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;
import java.util.Date;
import java.util.Timer;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class Manager implements ProcessChecker.ProcessUpdate
{
    
    /*
     * State the type of test
     */
    public static final int LIST_BASED = 0;
    public static final int LIST_BASED_FUZZ = 1;
    public static final int BRUTE_BASED = 2;
    public static final int BRUTE_BASED_FUZZ = 3;

    int workerCount = 150;
    //the two work queues
    public BlockingQueue<WorkUnit> workQueue;
    public BlockingQueue<DirToCheck> dirQueue;
    public BlockingQueue<HTMLparseWorkUnit> parseQueue;

    public Thread workGenThread;
    public WorkerGenerator workGen;
    public BruteForceWorkGenerator workGenBrute;
    public WorkerGeneratorURLFuzz workGenFuzz;
    public BruteForceURLFuzz workGenBruteFuzz;


    private String inputFile;
    private String firstPartOfURL;
    private String extention;
    private Timer timer;
    private ProcessChecker task;
    private ProcessEnd task2;
    private String protocol;
    private String host;
    private int port;
    private String startPoint;
    public StartGUI gui;
    private boolean doDirs,  doFiles;
    private int totalDone = 0;
    private Vector workers = new Vector(100, 10);
    private Vector parseWorkers = new Vector(100, 10);
    private String[] charSet;
    private int maxLen,  minLen;
    boolean pureBrute = false;
    boolean urlFuzz = false;
    boolean pureBrutefuzz = false;
    boolean recursive = true;
    //flag for if we are auto switching between HEADS and GETS
    private boolean auto = true;
    //used for storing total numbers of trys per pass
    private double totalPass;
    //used to record the total number of dirs that have been found
    //set to 1 as we there must always at least 1
    private int totalDirsFound = 1;
    public Vector HTTPheaders = new Vector(10, 5);
    //proxy detials
    private boolean useProxy = false;
    private String proxyHost = "";
    private int proxyPort = 0;
    //proxy credentials detials
    private boolean useProxyAuth = false;
    private String proxyUsername = "";
    private String proxyPassword = "";
    private String proxyRealm = "";
    //setting for using a blank extention
    private boolean blankExt = false;
    //store of all extention that are to be tested
    private Vector extToUse = new Vector(10, 5);
    //variable for httpclient
    private HttpClient httpclient;
    private HttpState initialState;
    //singleton instance of the object
    private static Manager manager = null;
    private Vector producedBasesCases = new Vector(10, 10);
    //used to store all the links that have parsed, will not contain a list a all items, processed
    //as this will consume to much memory.  There for there is a chance of some duplication.
    private Vector processedLinks = new Vector(100, 100);
    //not all base case requests are processed so this will ensure the stats stay correct
    private int baseCaseCounterCorrection = 0;
    //used to store the value of items that will have been skipped
    private int workAmountCorrection = 0;
    //total number of links pasrsed from the HTML that have been added to the work queue
    private int parsedLinksProcessed = 0;
    //total number of basecases produced
    private int numberOfBaseCasesProduced = 0;
    //exts that are not to be added to the work queue if found by the HTML parser
    public Vector extsToMiss = new Vector(10, 10);
    //Vector to store all the html elements that are to be parsed.
    public Vector elementsToParse = new Vector(10, 10);
    //Used to store a string of what we are currently processing
    private String currentlyProcessing = "";
    //variables used to store information about the http athentifcation that is to be used
    private boolean useHTTPauth = false;
    private String userName = "";
    private String password = "";
    private String realmDomain = "";
    private String authType = "";
    //Variables to store information used for the URL fuzzing
    private String urlFuzzStart;
    private String urlFuzzEnd;
    //var to note when the fuzz generator has finished
    private boolean urlFuzzGenFinished = false;
    /*
     * time at which the fuzzing started
     */
    private long timestarted;
    /*
     * store of information about request limiting
     */
    private boolean limitRequests = false;
    private int limitRequestsTo = 50;
    /*
     * Stores setting for is dirbuster allowed to check for updates
     */
    private boolean checkForUpdates = true;
    /*
     * user pref object
     */
    Preferences userPrefs;
    /*
     * Date when the last check was performed
     */
    Date lastUpdateCheck;
    /*
     * flag to state if the inconsistent fail case dialog is visable
     */
    private boolean failCaseDialogVisable = false;
    
    /*
     * stores the default number of threads to be used
     */
    private int defaultNoThreads;
    
    /*
     * stores the default list to use
     */
    private String defaultList;
    
    /*
     * stores the default exts to use
     */
    private String defaultExts;

    public boolean isHeadLessMode()
    {
        return headLessMode;
    }

    public void setHeadLessMode(boolean headLessMode)
    {
        this.headLessMode = headLessMode;
    }
    /*
     * this stores all the regexes that have been used when we get inconsistent base cases
     */
    private Vector<String> failCaseRegexes = new Vector(10, 10);
    /*
     * Are we running without a gui?
     */
    private boolean headLessMode = false;

    /*
     * stores of information used to transer data to the gui when started with console args
     * 
     */
    private URL targetURL = null;
    private String fileLocation = null;
    private String reportLocation = null;
    private String fileExtentions = null;
    private String pointToStartFrom = null;
    
    /*
     * Vector to store all error produced
     */
    public Vector<ErrorTableObject> errors = new Vector<ErrorTableObject>(10,10);
    
    /*
     * store the number of consecerative error that happend
     */
    int noConsecErrors = 0;
    
    /*
     * stores all the threads for the new work generators
     */
    public Vector<MultiThreadedGenerator> genThreads = new Vector<MultiThreadedGenerator>(100,100);
    
    /*
     * stores the current scan type
     */
    
    private int scanType;

    /*
     * stores counters on the number of dir and files that have been found
     */
    private int numOfFiles = 0;
    private int numOfDirs = 0;

    /*
     * stores all the results, this is used to display both the findings list and table tree
     */

    public Vector<Result> results = new Vector<Result>(100,100);
    
    private Manager()
    {
        //add the default file extentions for links to be process if found during a HTML parse
        extsToMiss.addElement("jpg");
        extsToMiss.addElement("gif");
        extsToMiss.addElement("jpeg");
        extsToMiss.addElement("ico");
        extsToMiss.addElement("tiff");
        extsToMiss.addElement("png");
        extsToMiss.addElement("bmp");

        elementsToParse.addElement(new HTMLelementToParse("a", "href"));
        elementsToParse.addElement(new HTMLelementToParse("img", "src"));
        elementsToParse.addElement(new HTMLelementToParse("form", "action"));
        elementsToParse.addElement(new HTMLelementToParse("script", "src"));
        elementsToParse.addElement(new HTMLelementToParse("iframe", "src"));
        elementsToParse.addElement(new HTMLelementToParse("div", "src"));
        elementsToParse.addElement(new HTMLelementToParse("frame", "src"));
        elementsToParse.addElement(new HTMLelementToParse("embed", "src"));

        /*
         * load the manager prefs
         */
        loadPrefs();

        /*
         * create the httpclient
         */
        createHttpClient();
    }

    public static Manager getInstance()
    {
        if(manager == null)
        {
            manager = new Manager();
        }

        return manager;
    }

    //set up dictionay based attack with normal start
    public void setupManager(String startPoint,
            String inputFile,
            String protocol,
            String host,
            int port,
            String extention,
            StartGUI gui,
            int ThreadNumber,
            boolean doDirs,
            boolean doFiles,
            boolean recursive,
            boolean blankExt,
            Vector extToUse)
    {
        totalDone = 0;
        this.startPoint = startPoint;
        this.inputFile = inputFile;
        this.firstPartOfURL = protocol + "://" + host + ":" + port;
        this.extention = extention;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        workerCount = ThreadNumber;
        this.gui = gui;
        this.doFiles = doFiles;
        this.doDirs = doDirs;
        this.recursive = recursive;
        this.blankExt = blankExt;
        this.extToUse = extToUse;
        URL url;

        //add the start point to the running list
        //TODO change this so it sctually checks for it
        try
        {
            url = new URL(firstPartOfURL + startPoint);
        //gui.addResult(new ResultsTableObject("Dir", url.getPath(), "---", "Scanning", url.toString(), "Start point of testing", null, null, this.recursive, null));
        }
        catch(MalformedURLException ex)
        {
            ex.printStackTrace();
        }


        System.out.println("Starting dir/file list based brute forcing");
        if(!headLessMode)
        {
            gui.setStatus("Starting dir/file list based brute forcing");
        }

        setpUpHttpClient();
        createTheThreads();
        workGen = new WorkerGenerator();

        scanType = LIST_BASED;

        
    }

    public HttpClient getHttpclient()
    {
        return httpclient;
    }

    //setup for purebrute force with normal start
    public void setupManager(String startPoint,
            String[] charSet,
            int minLen,
            int maxLen,
            String protocol,
            String host,
            int port,
            String extention,
            StartGUI gui,
            int ThreadNumber,
            boolean doDirs,
            boolean doFiles,
            boolean recursive,
            boolean blankExt,
            Vector extToUse)
    {
        totalDone = 0;
        this.startPoint = startPoint;
        this.firstPartOfURL = protocol + "://" + host + ":" + port;
        this.extention = extention;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        workerCount = ThreadNumber;
        this.gui = gui;
        this.doFiles = doFiles;
        this.doDirs = doDirs;
        this.charSet = charSet;
        this.maxLen = maxLen;
        this.minLen = minLen;
        pureBrute = true;
        this.recursive = recursive;
        this.blankExt = blankExt;
        this.extToUse = extToUse;
        URL url;

        //add the start point to the running list
        try
        {
            url = new URL(firstPartOfURL + startPoint);
        //gui.addResult(new ResultsTableObject("Dir", url.getPath(), "---", "Scanning", url.toString(), "Start point of testing", null, null, this.recursive, null));
        }
        catch(MalformedURLException ex)
        {
            ex.printStackTrace();
        }

        System.out.println("Starting dir/file pure brute forcing");

        if(!headLessMode)
        {
            gui.setStatus("Starting dir/file pure brute forcing");
        }

        setpUpHttpClient();
        createTheThreads();
        workGenBrute = new BruteForceWorkGenerator();
        
        scanType = BRUTE_BASED;

    }

    /*
     * Used to setup the manager when we are URL fuzzing
     */
    public void setUpManager(String inputFile,
            String protocol,
            String host,
            int port,
            StartGUI gui,
            int ThreadNumber,
            String urlFuzzStart,
            String urlFuzzEnd)
    {
        totalDone = 0;
        this.inputFile = inputFile;
        this.firstPartOfURL = protocol + "://" + host + ":" + port;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        workerCount = ThreadNumber;
        this.gui = gui;
        this.urlFuzzStart = urlFuzzStart;
        this.urlFuzzEnd = urlFuzzEnd;

        urlFuzz = true;

        System.out.println("Starting URL fuzz");

        if(!headLessMode)
        {
            gui.setStatus("Starting URL fuzz");
        }

        setpUpHttpClient();
        createTheThreads();
        workGenFuzz = new WorkerGeneratorURLFuzz();
        
        scanType = LIST_BASED_FUZZ;
    }

    /*
     * set up manager for bruteforce fuzzing
     */
    public void setUpManager(String[] charSet,
            int minLen,
            int maxLen,
            String protocol,
            String host,
            int port,
            StartGUI gui,
            int ThreadNumber,
            String urlFuzzStart,
            String urlFuzzEnd)
    {
        /*
         * arguments for the fuzzing
         */
        this.charSet = charSet;
        this.maxLen = maxLen;
        this.minLen = minLen;

        /*
         * test details
         */
        totalDone = 0;
        this.firstPartOfURL = protocol + "://" + host + ":" + port;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        workerCount = ThreadNumber;
        this.gui = gui;

        /*
         * fuzzing points
         */

        this.urlFuzzStart = urlFuzzStart;
        this.urlFuzzEnd = urlFuzzEnd;

        pureBrutefuzz = true;

        System.out.println("Starting URL fuzz");
        if(!headLessMode)
        {
            gui.setStatus("Starting URL fuzz");
        }

        setpUpHttpClient();
        createTheThreads();
        workGenBruteFuzz = new BruteForceURLFuzz();
        
        scanType = BRUTE_BASED_FUZZ;
    }

    private void createHttpClient()
    {
        Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", easyhttps);
        initialState = new HttpState();

        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(1000);
        connectionManager.getParams().setMaxTotalConnections(1000);

        //connectionManager.set


        httpclient = new HttpClient(connectionManager);
    //httpclient.


    }

    private void setpUpHttpClient()
    {
        if(httpclient != null)
        {
            //add the proxy setting is required
            if(this.isUseProxy())
            {
                httpclient.getHostConfiguration().setProxy(this.getProxyHost(), this.getProxyPort());
                if(this.isUseProxyAuth())
                {
                    httpclient.getState().setProxyCredentials(this.getProxyRealm(), this.getProxyHost(),
                            new UsernamePasswordCredentials(this.getProxyUsername(), this.getProxyPassword()));
                }
            }


            httpclient.getHttpConnectionManager().
                    getParams().setConnectionTimeout(Config.connectionTimeout * 1000);
            httpclient.setState(initialState);
            httpclient.getParams().setParameter("http.useragent", Config.userAgent);

            /*
             * Code to deal with http auth
             *
             */

            if(useHTTPauth)
            {
                //Credentials creds = new Credentials();
                //creds.
                NTCredentials ntCreds = new NTCredentials(this.userName, this.password, "", this.realmDomain);
                httpclient.getState().setCredentials(AuthScope.ANY, ntCreds);
            }

        /*
         * Custom code to add ntlm auth
         */

        //NTCredentials ntCreds = new NTCredentials("username", "password", "", "");
        //httpclient.getState().setCredentials(AuthScope.ANY, ntCreds);
        }

    }

    private void createTheThreads()
    {
        //workers = new Worker[workerCount];


        workers.removeAllElements();
        parseWorkers.removeAllElements();
        genThreads.removeAllElements();

        for(int i = 0; i < workerCount; i++)
        {
            workers.addElement(new Worker(i));
        //workers[i] = new Worker(this, i);
        //tpes.execute(workers[i]);
        }

        //create the htmlparse threads
        for(int i = 0; i < workerCount; i++)
        {
            parseWorkers.addElement(new HTMLparse());
        }
        //work queue
        workQueue = new ArrayBlockingQueue<WorkUnit>(workerCount * 3);

        //dir to be processed
        dirQueue = new ArrayBlockingQueue<DirToCheck>(100000);

        //queue to hold a list of items to parsed
        parseQueue = new ArrayBlockingQueue<HTMLparseWorkUnit>(200000);

        timer = new Timer();

        //add the fist string on to the queue
        try
        {
            Vector tempext = extToUse;
            //extToUse.clone().
            dirQueue.put(new DirToCheck(startPoint, tempext));
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        try
        {
            timestarted = System.currentTimeMillis();

            totalDirsFound = 0;
            numOfFiles = 0;
            producedBasesCases.clear();
            numberOfBaseCasesProduced = 0;
            parsedLinksProcessed = 0;
            processedLinks.clear();


            if(!headLessMode)
            {
                /*
                 * clear the table tree
                 */
                gui.jPanelRunning.tableTreeModel.cleartable();
            }


            task = new ProcessChecker();
            timer.scheduleAtFixedRate(task, 1000L, 1000L);

            task2 = new ProcessEnd();
            timer.scheduleAtFixedRate(task2, 10000L, 10000L);

            //start the pure brute force thread
            if(pureBrute)
            {

                /*
                 * new multi threaded work gnerators
                 */
                if(doDirs)
                {
                    genThreads.addElement(new BruteForceWorkerGeneratorMultiThreaded(startPoint, "", WorkerGeneratorMultiThreaded.doDIR, true));
                    gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, Manager.BRUTE_BASED));
                }

                if(doFiles)
                {
                    for(int a = 0; a < extToUse.size(); a++)
                    {
                        ExtToCheck ext = (ExtToCheck) extToUse.elementAt(a);
                        genThreads.addElement(new BruteForceWorkerGeneratorMultiThreaded(startPoint, ext.getName(), WorkerGeneratorMultiThreaded.doFile, false));
                        gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, Manager.BRUTE_BASED));

                    }

                }
                //start the work generator
                //workGenThread = new Thread(workGen);
                //workGenThread.start();

                /*
                 * start all the generator threads
                 */
                for(int a = 0; a < genThreads.size(); a++)
                {
                    new Thread(genThreads.elementAt(a)).start();
                }

                //start the work generator
                //workGenThread = new Thread(workGenBrute);
                //workGenThread.start();
            }
            //start the 
            else if(urlFuzz)
            {
                /*
                 * find the number of line in the
                 */
                this.setTotalPass(Utils.getNumberOfLineInAFile(inputFile));

                workGenThread = new Thread(workGenFuzz);
                gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(-1, Manager.LIST_BASED_FUZZ));
                workGenThread.start();

            }
            else if(pureBrutefuzz)
            {
                workGenThread = new Thread(workGenBruteFuzz);
                gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(-1, Manager.BRUTE_BASED_FUZZ));
                workGenThread.start();
            }
            else
            {
                /*
                 * find the number of line in the
                 */
                this.setTotalPass(Utils.getNumberOfLineInAFile(inputFile));

                /*
                 * new multi threaded work gnerators
                 */
                if(doDirs)
                {
                    genThreads.addElement(new WorkerGeneratorMultiThreaded(startPoint, "", WorkerGeneratorMultiThreaded.doDIR, true));
                    gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, Manager.LIST_BASED));
                }
                
                if(doFiles)
                {
                    for(int a = 0; a < extToUse.size(); a++)
                    {
                        ExtToCheck ext = (ExtToCheck) extToUse.elementAt(a);
                        genThreads.addElement(new WorkerGeneratorMultiThreaded(startPoint, ext.getName(), WorkerGeneratorMultiThreaded.doFile, false));
                        gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, Manager.LIST_BASED));

                    }
                    
                }
                //start the work generator
                //workGenThread = new Thread(workGen);
                //workGenThread.start();
                
                /*
                 * start all th generator threads
                 */
                for(int a = 0; a < genThreads.size(); a++)
                {
                    new Thread(genThreads.elementAt(a)).start();
                }
            }

            //add the worker and parseWorker threads
            for(int i = 0; i < workers.size(); i++)
            {
                new Thread(((Worker) workers.elementAt(i))).start();
                ((HTMLparse) parseWorkers.elementAt(i)).start();
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean hasWorkLeft()
    {
        //TODO  finish
        return true;
    }

    public BlockingQueue<WorkUnit> getWorkQueue()
    {
        return workQueue;
    }

    public BlockingQueue<DirToCheck> getDirQueue()
    {
        return dirQueue;
    }


    public synchronized void foundDir(URL url, int statusCode, String responseHeader, String responseBody, BaseCase baseCaseObj)
    {

        boolean isStartPoint;

        /*
         * test if we have already found the file
         */
        if(Config.caseInsensativeMode)
        {
            isStartPoint = url.getPath().equalsIgnoreCase(startPoint);

            for(int a = 0; a < results.size(); a++)
            {
                /*
                 * if the URL is already in the results return
                 */
                if(url.toString().equalsIgnoreCase(results.elementAt(a).getItemFound().toString()))
                {
                    return;
                }
            }
        }
        else
        {
            isStartPoint = url.getPath().equals(startPoint);

            for(int a = 0; a < results.size(); a++)
            {
                /*
                 * if the URL is already in the results return
                 */
                if(url.equals(results.elementAt(a).getItemFound()))
                {
                    return;
                }
            }

        }


        /*
         * create and start the new workgen threads, but not if we are fuzzing
         */
        if(!isStartPoint && isRecursive() && (manager.scanType == Manager.LIST_BASED || manager.scanType == Manager.BRUTE_BASED))
        {
            /*
             * if we are scanning dirs
             */
            if(doDirs)
            {
                /*
                 * add the new threads
                 */
                genThreads.addElement(new WorkerGeneratorMultiThreaded(url.getPath(), "", WorkerGeneratorMultiThreaded.doDIR, false));

                if(!headLessMode)
                {
                    gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, manager.scanType));
                }

                /*
                 * start the threads
                 */
                new Thread(genThreads.elementAt(genThreads.size() - 1)).start();
            }

            /*
             * if we are scanning files
             */
            if(doFiles)
            {
                for(int a = 0; a < extToUse.size(); a++)
                {
                    /*
                     * add the new threads
                     */
                    ExtToCheck ext = (ExtToCheck) extToUse.elementAt(a);


                    genThreads.addElement(new WorkerGeneratorMultiThreaded(url.getPath(), ext.getName(), WorkerGeneratorMultiThreaded.doFile, false));
                    if(!headLessMode)
                    {
                        gui.jPanelRunning.addScanInfoObject(new JPanelScanInfo(genThreads.size() - 1, manager.scanType));
                    }
                    /*
                     * start the threads
                     */
                    new Thread(genThreads.elementAt(genThreads.size() - 1)).start();
                }

            }
            totalDirsFound++;
        }


        /*
         * display the result to console
         */
        System.out.println("Dir found: " + url.getFile() + " - " + statusCode);
        //add to list of items that have already processed
        addParsedLink(url.getPath());

        /*
         * add the result
         */
        Result result = new Result(Result.DIR, url, statusCode, responseHeader, responseBody, baseCaseObj);

        results.addElement(result);

        /*
         * if it's not headless, we need to update the GUI
         */
        if(!headLessMode)
        {


            gui.jPanelRunning.resultsTableModel.addRow();
            addToTree(result);
        }
    }

    /*
    public void updateTable(String finished, String started)
    {
        if(!headLessMode)
        {
            gui.updateTable(finished, started);
        }
    }
     */


    public synchronized void foundFile(URL url, int statusCode, String responseHeader, String responseBody, BaseCase baseCaseObj)
    {

        /*
         * reset the error count
         */
        noConsecErrors = 0;

        /*
         * test if we have already found the file
         */
        if(Config.caseInsensativeMode)
        {
            for(int a = 0; a < results.size(); a++)
            {
                /*
                 * if the URL is already in the results return
                 */
                if(url.toString().equalsIgnoreCase(results.elementAt(a).getItemFound().toString()))
                {
                    return;
                }
            }
        }
        else
        {
            for(int a = 0; a < results.size(); a++)
            {
                /*
                 * if the URL is already in the results return
                 */
                if(url.equals(results.elementAt(a).getItemFound()))
                {
                    return;
                }
            }

        }


        System.out.println("File found: " + url.getFile() + " - " + statusCode);
        numOfFiles++;
        addParsedLink(url.getPath());


        Result result = new Result(Result.FILE, url, statusCode, responseHeader, responseBody, baseCaseObj);

        /*
         * add the result
         */
        results.addElement(result);

        /*
         * update the gui if we need to
         */
        if(!headLessMode)
        {

            addToTree(result);
            gui.jPanelRunning.resultsTableModel.addRow();
        }
    }

    public synchronized void foundError(URL url, String reason)
    {
        if(!headLessMode)
        {            
            gui.jPanelRunning.errorTableModel.addRow(new ErrorTableObject(url, reason));
            gui.jPanelRunning.jTabbedPaneViewResults.setTitleAt(3, "Errors: " + errors.size());
            

            /*
             * auto pause code
             */
            noConsecErrors++;
            
            if(noConsecErrors == 20)
            {
                pause();
                gui.jPanelRunning.jToggleButtonPause.setSelected(true);
                JOptionPane.showMessageDialog(gui, "DirBuster has paused it's self as 20 consecutive errors have happened", "DirBuster has been paused", JOptionPane.ERROR_MESSAGE);
                noConsecErrors = 0;
            }
            
        }
        else
        {
            //TODO deal with this better
            //headlessResult.addElement(new HeadlessResult(url.getFile() + ":" + reason, -1, HeadlessResult.ERROR));
        }
        System.err.println("ERROR: " + url.toString() + " - " + reason);
        
    }

    public void updateProgress(String current, String average, String total, String timeLeft, String parseQueueLength)
    {
        if(!headLessMode)
        {
            gui.updateProgress(current, average, total, workerCount, timeLeft, parseQueueLength);
        }
    }

    public String getInputFile()
    {
        return inputFile;
    }

    public String getFirstPartOfURL()
    {
        return firstPartOfURL;
    }

    public String getFileExtention()
    {
        return extention;
    }

    public void isAlive()
    {
    }

    public synchronized void workDone()
    {
        totalDone++;
    }

    public synchronized int getTotalDone()
    {
        return totalDone;
    }

    public String getProtocol()
    {
        return protocol;
    }

    public String getHost()
    {
        return host;
    }

    public int getPort()
    {
        return port;
    }

    public boolean getDoDirs()
    {
        return doDirs;
    }

    public boolean getDoFiles()
    {
        return doFiles;
    }

    public void pause()
    {
        for(int a = 0; a < workers.size(); a++)
        {
            synchronized((Worker) workers.elementAt(a))
            {
                ((Worker) workers.elementAt(a)).pause();
            }
        }
        gui.setStatus("Program paused!");
    }

    public void pauseAllWorkGen()
    {
        for(int a =0; a < genThreads.size(); a++)
        {
            synchronized(genThreads.elementAt(a))
            {
                genThreads.elementAt(a).pause();
            }
        }
    }

    public void unPause()
    {

        for(int a = 0; a < workers.size(); a++)
        {
            synchronized((Worker) workers.elementAt(a))
            {
                ((Worker) workers.elementAt(a)).unPause();
                ((Worker) workers.elementAt(a)).notify();
            }
        }
        gui.setStatus("Program running again");
    }

    public void unPauseAllWorkGen()
    {
        for(int a =0; a < genThreads.size(); a++)
        {
            synchronized(genThreads.elementAt(a))
            {
                genThreads.elementAt(a).unPause();
                genThreads.elementAt(a).notify();
            }
        }
    }

    /*
     * pause one of the work gen threads
     */
    public void pauseWorkGen(int id)
    {
        synchronized(genThreads.elementAt(id))
        {
            genThreads.elementAt(id).pause();
        }
    }

    /*
     * unpause one of the work gen threads
     */
    public void unPauseWorkGen(int id)
    {
        synchronized(genThreads.elementAt(id))
        {
            genThreads.elementAt(id).unPause();
            genThreads.elementAt(id).notify();
        }
    }

    public void setStatus(String status)
    {
        if(!headLessMode)
        {
            gui.setStatus(status);
        }
    }

    public int getMinLen()
    {
        return minLen;
    }

    public int getMaxLen()
    {
        return maxLen;
    }

    public String[] getCharSet()
    {
        return charSet;
    }

    public boolean isRecursive()
    {
        return recursive;
    }

    //TODO: check how  youAreFinished() is called and when it is called
    public void youAreFinished()
    {

        //clear all the queue
        workQueue.clear();
        dirQueue.clear();
        parseQueue.clear();

        //reset counters
        totalDirsFound = 0;
        producedBasesCases.clear();
        numberOfBaseCasesProduced = 0;
        parsedLinksProcessed = 0;
        processedLinks.clear();
        workAmountCorrection = 0;

        //kill all the running threads
        task.cancel();
        task2.cancel();



        if(pureBrute)
        {
            //TODO
        }
        else if(urlFuzz)
        {
            workGenFuzz.stopMe();
        }
        else if(pureBrutefuzz)
        {
            //TODO
        }
        else
        {
            workGen.stopMe();
        }

        //stop all the workers;
        for(int a = 0; a < workers.size(); a++)
        {
            synchronized((Worker) workers.elementAt(a))
            {
                ((Worker) workers.elementAt(a)).stopThread();
            }
        }

        //stops all the parsers
        for(int a = 0; a < this.parseWorkers.size(); a++)
        {
            synchronized((HTMLparse) parseWorkers.elementAt(a))
            {
                ((HTMLparse) parseWorkers.elementAt(a)).stopWorking();
                ((HTMLparse) parseWorkers.elementAt(a)).notify();
            }
        }

        /*
         * only if there is a gui update it
         */
        if(!headLessMode)
        {
            //reset the tree
            //gui.jPanelRunning.jTableTreeResults = new JTreeTable(new ResultsTableTreeModel(new ResultsNode("/")));

            //set the buttones when we are finished
            gui.jPanelRunning.jButtonReport.setEnabled(true);
            gui.jPanelRunning.jToggleButtonPause.setSelected(false);
            gui.jPanelRunning.jButtonStop.setEnabled(false);
            gui.jPanelRunning.jButtonBack.setEnabled(true);
            gui.setStatus("DirBuster Stopped");
        }
        
        /*
         * if we have finished and it's headless just exit, the exit thread will write the report
         */
        if(headLessMode)
        {
            System.exit(0);
        }
        System.out.println("DirBuster Stopped");
        /*
         * reset the all the markers for what type of test we are doing
         */
        urlFuzz = false;
        pureBrute = false;
        pureBrutefuzz = false;

    }

    public double getTotalPass()
    {
        return totalPass;
    }

    public void setTotalPass(double totalPass)
    {
        this.totalPass = totalPass;
    }

    public int getTotalDirsFound()
    {
        return totalDirsFound;
    }

    public int getTotalFilesFound()
    {
        return numOfFiles;
    }

    public int getWorkerCount()
    {
        return workerCount;
    }

    public Vector getWorkers()
    {
        return workers;
    }

    public boolean getAuto()
    {
        return auto;
    }

    public void setAuto(boolean b)
    {
        auto = b;
    }

    /*
     * used to add extra workers to the queue
     */
    public void addWrokers(int number)
    {
        int currentNumber = workers.size();
        for(int i = 0; i < number; i++)
        {
            int threadid = currentNumber + i;
            workers.addElement(new Worker(threadid));

            new Thread((Worker) workers.elementAt(threadid)).start();
        }
        workerCount = currentNumber + number;
    }

    /*
     * used to remove extra workers from the queue
     */
    public void removeWorkers(int number)
    {
        int currentNumber = workers.size();

        if(number >= currentNumber)
        {
            return;
        }

        for(int a = currentNumber - 1; a >= (currentNumber - number); a--)
        {
            ((Worker) workers.elementAt(a)).stopThread();
            workers.remove(a);
        }
        workerCount = currentNumber - number;
    }

    //used to remove stuff from the work queue, as a result of the request from a user;
    public synchronized void removeFromDirQueue(String dir)
    {


        /*
         *Convert item queue to an array
         */
        Object tempArray[] = dirQueue.toArray();
        DirToCheck dirToCheck = null;

        for(int b = 0; b < tempArray.length; b++)
        {
            dirToCheck = (DirToCheck) tempArray[b];
            String processWork = dirToCheck.getName();

            /*
             * find the object of all the ones we wish to remove
             */
            if(processWork.equals(dir))
            {
                /*
                 * remove the item
                 */
                if(dirQueue.remove(dirToCheck))
                {

                    totalDirsFound--;
                }
                else
                {
                    System.err.println("FAILED Removed " + processWork + " from dir queue");
                }

            }
        }

    }

    //used to re add stuff to the work as the reswult from a request from the work queue
    public synchronized void addToDirQueue(String dir)
    {
        try
        {

            dirQueue.put(new DirToCheck(dir, extToUse));
            totalDirsFound++;
        }
        catch(InterruptedException ex)
        {
            //ex.printStackTrace();
            return;
        }
    }

    public synchronized void addHTMLToParseQueue(HTMLparseWorkUnit parseWorkUnit)
    {
        try
        {
            parseQueue.put(parseWorkUnit);
        }
        catch(InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean isUseProxy()
    {
        return useProxy;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
        userPrefs.put("ProxyHost", proxyHost);
    }

    public void setUseProxy(boolean useProxy)
    {
        this.useProxy = useProxy;
        userPrefs.putBoolean("UseProxy", useProxy);

    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
        userPrefs.putInt("ProxyPort", proxyPort);
    }

    public boolean isUseProxyAuth()
    {
        return useProxyAuth;
    }

    public String getProxyUsername()
    {
        return proxyUsername;
    }

    public String getProxyPassword()
    {
        return proxyPassword;
    }

    public String getProxyRealm()
    {
        return proxyRealm;
    }

    public void setProxyPassword(String proxyPassword)
    {
        this.proxyPassword = proxyPassword;
    }

    public void setProxyRealm(String proxyRealm)
    {
        this.proxyRealm = proxyRealm;
    }

    public void setProxyUsername(String proxyUsername)
    {
        this.proxyUsername = proxyUsername;
    }

    public void setUseProxyAuth(boolean useProxyAuth)
    {
        this.useProxyAuth = useProxyAuth;
    }

    public boolean isBlankExt()
    {
        return blankExt;
    }

    public void addHTTPheader(HTTPHeader header)
    {
        HTTPheaders.addElement(header);
    }

    public Vector getHTTPHeaders()
    {
        return HTTPheaders;
    }

    public void addExt(ExtToCheck ext)
    {
        extToUse.addElement(ext);
    }

    public Vector getExtToUse()
    {
        return extToUse;
    }

    public synchronized BaseCase getBaseCase(String base, boolean isDir, String fileExt)
    {

        try
        {
            for(int a = 0; a < producedBasesCases.size(); a++)
            {
                BaseCase tempBaseCase = (BaseCase) producedBasesCases.elementAt(a);

                if(tempBaseCase.getBaseCaseURL().equals(new URL(base)) && tempBaseCase.isDir() == isDir)
                {
                    if(!isDir)
                    {
                        if(tempBaseCase.getFileExt().equals(fileExt))
                        {
                            return tempBaseCase;
                        }
                    }
                    else
                    {
                        return tempBaseCase;
                    }

                }


            }
        }
        catch(MalformedURLException ex)
        {
            //do nothing I dont care
        }

        return null;
    }

    public synchronized void addBaseCase(BaseCase baseCase)
    {
        if(!producedBasesCases.contains(baseCase))
        {
            producedBasesCases.addElement(baseCase);
        }
    }

    public synchronized boolean hasLinkBeenDone(String link)
    {

        if(processedLinks.contains(link))
        {
            return true;
        }

        return false;
    }

    public int getBaseCaseCounterCorrection()
    {
        return baseCaseCounterCorrection;
    }

    public int getParsedLinksProcessed()
    {
        return parsedLinksProcessed;
    }

    public synchronized boolean addParsedLink(String link)
    {
        /*
         * case insenataive mode
         */
        if(Config.caseInsensativeMode)
        {

            for(int a = 0; a < processedLinks.size(); a++)
            {
                if(link.equalsIgnoreCase((String) processedLinks.elementAt(a)))
                {
                    return false;
                }
            }
            processedLinks.addElement(link);

        }
        else
        /*
         * case sensative mode
         */
        {
            if(!processedLinks.contains(link))
            {
                processedLinks.addElement(link);
                return true;

            }
        }
        return false;
    }

    public synchronized void addParsedLinksProcessed()
    {
        parsedLinksProcessed++;
    }

    public int getNumberOfBaseCasesProduced()
    {
        return numberOfBaseCasesProduced;
    }

    //increments the correction counter
    public synchronized void addBaseCaseCounterCorrection()
    {
        baseCaseCounterCorrection++;
    }

    public Vector getElementsToParse()
    {
        return elementsToParse;
    }

    public synchronized void addNumberOfBaseCasesProduced()
    {
        numberOfBaseCasesProduced++;
    }

    public Vector getParseWorkers()
    {
        return parseWorkers;
    }

    public void skipCurrentWork()
    {
        /*
         * while this is a case snsative comparie, it should not require to be done both ways
         */
        //stop work gen from adding more the the queue
        workGen.skipCurrent();

        //remove all items in the current work queue that are no loger required.
        Object tempArray[] = workQueue.toArray();
        WorkUnit work = null;
        int totalRemoved = 0;
        for(int b = 0; b < tempArray.length; b++)
        {
            work = (WorkUnit) tempArray[b];
            String processWork = work.getWork().getPath();
            if(processWork.startsWith(currentlyProcessing))
            {
                workQueue.remove(work);
                totalRemoved++;
            }
        }
        addToWorkCorrection(totalRemoved);

    }

    public void setCurrentlyProcessing(String currentlyProcessing)
    {
        this.currentlyProcessing = currentlyProcessing;
    }

    public void addToWorkCorrection(int amount)
    {
        workAmountCorrection = workAmountCorrection + amount;
    }

    public String getAuthType()
    {
        return authType;
    }

    public String getPassword()
    {
        return password;
    }

    public String getRealmDomain()
    {
        return realmDomain;
    }

    public boolean isUseHTTPauth()
    {
        return useHTTPauth;
    }

    public String getUserName()
    {
        return userName;
    }

    public int getWorkAmountCorrection()
    {
        return workAmountCorrection;
    }

    public void setAuthDetails(String username, String password, String realmDomain, String type)
    {
        this.userName = username;
        this.password = password;
        this.realmDomain = realmDomain;
        this.authType = type;
        this.useHTTPauth = true;
    }

    public void setDoNotUseAuth()
    {
        this.useHTTPauth = false;
    }

    public String getUrlFuzzEnd()
    {
        return urlFuzzEnd;
    }

    public String getUrlFuzzStart()
    {
        return urlFuzzStart;
    }

    public boolean isURLFuzzGenFinished()
    {
        return urlFuzzGenFinished;
    }

    public void setURLFuzzGenFinished(boolean urlFuzzGenFinished)
    {
        this.urlFuzzGenFinished = urlFuzzGenFinished;
    }

    public long getTimestarted()
    {
        return timestarted;
    }

    public boolean isLimitRequests()
    {
        return limitRequests;
    }

    public void setLimitRequests(boolean limitRequests)
    {
        this.limitRequests = limitRequests;
    }

    public int getLimitRequestsTo()
    {
        return limitRequestsTo;
    }

    public void setLimitRequestsTo(int limitRequestsTo)
    {
        this.limitRequestsTo = limitRequestsTo;
    }

    /*
     * add items to the table tree view
     */
    private void addToTree(Result result)
    {
        gui.jPanelRunning.tableTreeModel.addRow(result);

    }

    public boolean areWorkersAlive()
    {
        for(int a = 0; a < workers.size(); a++)
        {
            if(((Worker) workers.elementAt(a)).isWorking())
            {
                //there is a worker still working so break
                return true;
            }
        }
        return false;
    }

    /*
     * this loads the user prefs for set for DirBuster
     */
    public void loadPrefs()
    {
        userPrefs = Preferences.userNodeForPackage(Manager.class);
        checkForUpdates = userPrefs.getBoolean("CheckForUpdates", true);
        lastUpdateCheck = new Date(userPrefs.getLong("LastUpdateCheck", 0l));
        defaultNoThreads = userPrefs.getInt("DefaultNoTreads", 10);
        defaultList = userPrefs.get("DefaultList", "");
        defaultExts = userPrefs.get("DefaultExts", "php");
        Config.userAgent = userPrefs.get("UserAgent", "DirBuster-" + Config.version + " (http://www.owasp.org/index.php/Category:OWASP_DirBuster_Project)");
        useProxy = userPrefs.getBoolean("UseProxy", false);
        proxyHost = userPrefs.get("ProxyHost", "");
        proxyPort = userPrefs.getInt("ProxyPort", 8080);
        proxyRealm = userPrefs.get("ProxyRealm", "");

    }

    public boolean isCheckForUpdates()
    {
        return checkForUpdates;
    }

    public void setCheckForUpdates(boolean checkForUpdates)
    {
        this.checkForUpdates = checkForUpdates;
        userPrefs.putBoolean("CheckForUpdates", checkForUpdates);

    }

    public boolean isFailCaseDialogVisable()
    {
        return failCaseDialogVisable;
    }

    public void setFailCaseDialogVisable(boolean failCaseDialogVisable)
    {
        this.failCaseDialogVisable = failCaseDialogVisable;
    }

    /*
     * function to check for updates
     */
    public void checkForUpdates(boolean informUser)
    {
        /*
         * the user has asked to check
         */
        if(informUser)
        {
            Thread update = new Thread(new CheckForUpdates(informUser));
            update.start();
        }
        /*
         * this an auto check
         */
        else
        {
            if(checkForUpdates)
            {
                /*
                 * get the date now
                 */
                Date now = new Date();
                long passedTime = now.getTime() - lastUpdateCheck.getTime();


                /*
                 * if the last check was over a day ago
                 */
                if(passedTime > (1000 * 60 * 60 * 24))
                {
                    Thread update = new Thread(new CheckForUpdates(informUser));
                    update.start();
                    /*
                     * save the time back!
                     */
                    userPrefs.putLong("LastUpdateCheck", now.getTime());
                }
            }
        }

    }

    /*
     * returns a vector of all the regexes we have already used
     */
    public Vector<String> getFailCaseRegexes()
    {
        return failCaseRegexes;
    }

    /*
     * adds a new regex fail case
     */
    public void addFailCaseRegex(String regex)
    {
        failCaseRegexes.addElement(regex);
    }

    public URL getTargetURL()
    {
        return targetURL;
    }

    public void setTargetURL(URL targetURL)
    {
        this.targetURL = targetURL;
    }

    public String getFileLocation()
    {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation)
    {
        this.fileLocation = fileLocation;
    }

    public String getReportLocation()
    {
        return reportLocation;
    }

    public void setReportLocation(String reportLocation)
    {
        this.reportLocation = reportLocation;
    }

    public String getFileExtentions()
    {
        return fileExtentions;
    }

    public void setFileExtentions(String fileExtentions)
    {
        this.fileExtentions = fileExtentions;
    }

    public String getPointToStartFrom()
    {
        return pointToStartFrom;
    }

    public void setPointToStartFrom(String pointToStartFrom)
    {
        this.pointToStartFrom = pointToStartFrom;
    }

    public String getDefaultExts()
    {
        return defaultExts;
    }

    public void setDefaultExts(String defaultExts)
    {
        this.defaultExts = defaultExts;
        userPrefs.put("DefaultExts", defaultExts);
    }

    public String getDefaultList()
    {
        return defaultList;
    }

    public void setDefaultList(String defaultList)
    {
        this.defaultList = defaultList;
        userPrefs.put("DefaultList", defaultList);
    }

    public int getDefaultNoThreads()
    {
        return defaultNoThreads;
    }

    public void setDefaultNoThreads(int defaultNoThreads)
    {
        this.defaultNoThreads = defaultNoThreads;
        userPrefs.putInt("DefaultNoTreads", defaultNoThreads);
    }
    
    public void resetConErrorCounter()
    {
        noConsecErrors = 0;
    }
    
    public int getScanType()
    {
        return scanType;
    }

    public void setGUI(StartGUI gui)
    {
        this.gui = gui;
    }
}
