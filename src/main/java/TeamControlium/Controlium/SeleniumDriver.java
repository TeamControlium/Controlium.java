package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;

import TeamControlium.Utilities.TestData;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class SeleniumDriver {
    // CONSTANT FIELDS
    private final String[] SeleniumServerFolder = { "Selenium", "SeleniumServerFolder" };      // Location of the local Selenium Servers (Only required if running locally
    private final String[] ConfigTimeout = { "Selenium", "ElementFindTimeout" };               // Timeout when waiting for Elements to be found (or go invisible) in seconds
    private final String[] ConfigPollingInterval = { "Selenium", "PollInterval" };             // When looping on an event wait, this is the loop interval; trade off between keeping wire traffic down and speed of test
    private final String[] ConfigPageLoadTimeout = { "Selenium", "PageLoadTimeout" };          // Timeout waiting for a page to load

    private final String[] ConfigDevice = { "Selenium", "Device" };                            // Device hosting the UI endpoint we are testing with (If Local, usually Win7)
    private final String[] ConfigHost = { "Selenium", "Host" };                               // Who is hosting the selenium server.  Either localhost (or 127.0.0.1) for locally hosted.  Or a named Cloud provider (IE. BrowserStack or SauceLabs) or Custom.
    private final String[] ConfigHostURI = { "Selenium", "HostURI" };                         // If not locally hosted, this is the full URL or IPaddress:Port to access the Selenium Server
    private final String[] ConfigConnectionTimeout = { "Selenium", "ConnectionTimeout" };     // Timeout when waiting for a response from the Selenium Server (when remote)
    private final String[] SeleniumDebugMode = { "Selenium", "DebugMode" };                   // If yes, Selenium is started in debug mode...
    private final String[] SeleniumLogFilename = { "Selenium", "LogFile" };                   // Path and file for Selenium Log file.  Default is the console window

    private WebDriver webDriver;


    private Duration _findTimeout = null;
    private Duration _pollInterval = null;
    private Duration _pageLoadTimeout = null;
    private static final long defaultTimeout = 60000; // 1 Minute
    private static final long defaultPollInterval = 500; // 500mS
    private Browsers _browser=null;
    private Devices _device=null;
    private String seleniumHost;

    public SeleniumDriver(String seleniumHost,String device,String browser) {
        commonConstructs();
        Browsers.SetTestBrowser(browser);
        Devices.SetTestDevice(device);
        setSeleniumHost(seleniumHost);
    }
    public SeleniumDriver(String seleniumHost) {
        commonConstructs();
        Browsers.SetTestBrowser();
        Devices.SetTestDevice();
        setSeleniumHost(null);
    }
    public SeleniumDriver(String device,String browser) {
        commonConstructs();
        Browsers.SetTestBrowser(browser);
        Devices.SetTestDevice(device);
        setSeleniumHost(seleniumHost);
    }
    public SeleniumDriver() {
        commonConstructs();
        Browsers.SetTestBrowser();
        Devices.SetTestDevice();
        setSeleniumHost(null);
    }

    private void commonConstructs() {
        // Initialise defaults
        setFindTimeout(Duration.ofMillis(defaultTimeout));
        setPollInterval(Duration.ofMillis(defaultPollInterval));
        setPageLoadTimeout(Duration.ofMillis(defaultTimeout));
    }


    // PROPERTIES
    public Duration setFindTimeout(Duration findTimeout) { _findTimeout = findTimeout; return getElementFindTimeout();}
    public Duration getElementFindTimeout() { return _findTimeout;}
    public Duration setPollInterval(Duration pollInterval) { _pollInterval = pollInterval; return getPollInterval();}
    public Duration getPollInterval() { return _pollInterval;}
    public Duration setPageLoadTimeout(Duration pageLoadTimeout) {
        if (webDriver!=null) {
            webDriver.manage().timeouts().pageLoadTimeout(pageLoadTimeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        // We keep a note of the page-load timeout as there is no way of getting it out of Seleniunium in Java....
        _pageLoadTimeout = pageLoadTimeout;
        return getPageLoadTimeout();
    }
    public Duration getPageLoadTimeout() { return _pageLoadTimeout;}

    public Browsers getBrowser() { return _browser; }
    public Browsers setBrowser(Browsers browser) { _browser=browser; return getBrowser();}

    public Browsers getDevice() { return _browser; }
    public Browsers setDevice(Devices device) { _device=device; return getDevice();}


    public HTMLElement findElement(ObjectMapping objectMapping) { return findElement(null,objectMapping, false, false, getElementFindTimeout(), getPollInterval(), false);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean waitUntilStable) { return findElement(null,objectMapping, false, false, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,Duration timeout) { return findElement(null,objectMapping, false, false, timeout, getPollInterval(), false);}
    public HTMLElement findElement(ObjectMapping objectMapping,Duration timeout,boolean waitUntilStable) { return findElement(null,objectMapping, false, false, timeout, getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,Duration timeout,Duration pollInterval) { return findElement(null,objectMapping, false, false, timeout, pollInterval, false);}
    public HTMLElement findElement(ObjectMapping objectMapping,Duration timeout,Duration pollInterval,boolean waitUntilStable) { return findElement(null,objectMapping, false, false, timeout, pollInterval, waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean waitUntilSingle, boolean waitUntilStable) { return findElement(null,objectMapping, false, waitUntilSingle, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,boolean waitUntilStable) { return findElement(null,objectMapping, allowMultipleMatches, waitUntilSingle, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout) { return findElement(null,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, getPollInterval(), false);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout,boolean waitUntilStable) { return findElement(null,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout,Duration pollInterval) { return findElement(null,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, pollInterval, false);}
    public HTMLElement findElement(ObjectMapping objectMapping, boolean allowMultipleMatches,boolean waitUntilSingle, Duration timeout, Duration pollInterval, boolean waitUntilStable) { return findElement(null,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, pollInterval, waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping) { return findElement(parentElement,objectMapping, false, false, getElementFindTimeout(), getPollInterval(), false);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean waitUntilStable) { return findElement(parentElement,objectMapping, false, false, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean waitUntilSingle, boolean waitUntilStable) { return findElement(parentElement,objectMapping, false, waitUntilSingle, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,Duration timeout) { return findElement(parentElement,objectMapping, false, false, timeout, getPollInterval(), false);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,Duration timeout,boolean waitUntilStable) { return findElement(parentElement,objectMapping, false, false, timeout, getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,Duration timeout,Duration pollInterval) { return findElement(parentElement,objectMapping, false, false, timeout, pollInterval, false);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,Duration timeout,Duration pollInterval,boolean waitUntilStable) { return findElement(parentElement,objectMapping, false, false, timeout, pollInterval, waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,boolean waitUntilStable) { return findElement(parentElement,objectMapping, allowMultipleMatches, waitUntilSingle, getElementFindTimeout(), getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout) { return findElement(parentElement,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, getPollInterval(), false);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout,boolean waitUntilStable) { return findElement(parentElement,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, getPollInterval(), waitUntilStable);}
    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping,boolean allowMultipleMatches,boolean waitUntilSingle,Duration timeout,Duration pollInterval) { return findElement(parentElement,objectMapping, allowMultipleMatches, waitUntilSingle, timeout, pollInterval, false);}


    public HTMLElement findElement(HTMLElement parentElement,ObjectMapping objectMapping, boolean allowMultipleMatches,boolean waitUntilSingle, Duration timeout, Duration pollInterval, boolean waitUntilStable) {
        List<HTMLElement> clauseResults = null;
        boolean multiLogShown=false;
        boolean showMultiMatches=true;
        boolean isStable=false;

        //
        // We look after our own implicitWait (timeout) and poll interval rather that Selenium's as we are using our FindElements to find the element we want.  We use our
        // FindElements so that we can control if ONLY a single match is allowed (Selenium FindElement allows multiple matches silently - see By class implementation) and extra debug logging.
        //
        long totalTimeoutMillis = timeout.toMillis();
        long pollIntervalMillis = pollInterval.toMillis();

        Logger.WriteLine(Logger.LogLevels.TestDebug, "Timeout - %s", durationFormatted(timeout));

        while (waitUntilStable && !isStable) {

            StopWatch timer = StopWatch.createStarted();

            // Loop while:-
            //  - we have no find results
            //  or
            //  - we have more than 1 match and we only want a single match but are ok waiting until we have only a single match
            // we do at least one find!
            clauseResults = getHtmlElements(parentElement,                                   // Parent element to search from.  If null searches from DOM root
                                            objectMapping,                                   // Find logic
                                            allowMultipleMatches,                            // If true allows multiple matches
                                            waitUntilSingle,                                 // If true and allowMultipleMatches true, will wait until timeout or a SINGLE match is found
                                            (clauseResults==null || clauseResults.size()<2), // Controls logging of multiple matches.  Only show once IF we are search first time OR have searched with only a single result
                                            totalTimeoutMillis,                              // Search timeout in milliseconds
                                            pollIntervalMillis,                              // Polling ionterval in milliseconds
                                            timer);                                          // Find stopwatch timing whole finding

            if (clauseResults.size()==0) {
                String errorText = String.format("Time reached and found 0 matching elements using ([%s] - %s) from [%s] (Waited upto %dmS).",
                        objectMapping.getActualFindLogic(),
                        objectMapping.getFriendlyName(),
                        (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                        timer.getTime());
                Logger.WriteLine(Logger.LogLevels.Error, errorText);
                throw new RuntimeException(errorText);

            }
            if (clauseResults.size() > 1 && !allowMultipleMatches) {
                String errorText = String.format("Found %d matching elements using ([%s] - %s) from [%s] (Waited upto %dmS). allowMultipleMatches=false so error!",
                        clauseResults.size(),
                        objectMapping.getActualFindLogic(),
                        objectMapping.getFriendlyName(),
                        (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                        timer.getTime());
                Logger.WriteLine(Logger.LogLevels.Error, errorText);
                throw new RuntimeException(errorText);
            }

            if (showMultiMatches && !waitUntilSingle) {
                Logger.WriteLine(Logger.LogLevels.TestDebug, "From [%s], find [%s (%s)] returned %d matches (%s multiple matches).",
                        (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                        objectMapping.getActualFindLogic(),
                        objectMapping.getFriendlyName(),
                        clauseResults.size(),
                        (allowMultipleMatches) ? "Allowing" : "Not");
                showMultiMatches=false;
            }

            if (waitUntilStable) {
                if (clauseResults.get(0).isPositionStable()) {
                    Logger.WriteLine(Logger.LogLevels.TestDebug, "From [%s], find [%s (%s)] returned %d matches (%s multiple matches.  Element 0 is stable, so returning...).",
                            (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                            objectMapping.getActualFindLogic(),
                            objectMapping.getFriendlyName(),
                            clauseResults.size(),
                            (allowMultipleMatches) ? "Allowing" : "Not");
                    return clauseResults.get(0);
                } else {
                    if (timer.getTime()>=totalTimeoutMillis) {
                        Logger.WriteLine(Logger.LogLevels.Error, "From [%s], find [%s (%s)] returned %d matches (%s multiple matches). We must wait until stable, element 0 is NOT stable and timeout reached so throwing",
                                (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                                objectMapping.getActualFindLogic(),
                                objectMapping.getFriendlyName(),
                                clauseResults.size(),
                                (allowMultipleMatches) ? "Allowing" : "Not");
                        throw new RuntimeException(String.format("From [%s], find [%s (%s)] returned %d matches (%s multiple matches). We must wait until stable and element 0 NOT stable and timeout reached after %dmS.",
                                (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                                objectMapping.getActualFindLogic(),
                                objectMapping.getFriendlyName(),
                                clauseResults.size(),
                                (allowMultipleMatches) ? "Allowing" : "Not",
                                timer.getTime()));
                    }
                    Logger.WriteLine(Logger.LogLevels.TestDebug, "From [%s], find [%s (%s)] returned %d matches (%s multiple matches).  Element 0 is NOT stable and we must wait until stable...",
                            (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                            objectMapping.getActualFindLogic(),
                            objectMapping.getFriendlyName(),
                            clauseResults.size(),
                            (allowMultipleMatches) ? "Allowing" : "Not");
                }
            }

        }
        return clauseResults.get(0);
    }

    private List<HTMLElement> getHtmlElements(HTMLElement parentElement, ObjectMapping objectMapping, boolean allowMultipleMatches, boolean waitUntilSingle, boolean showMultiFound, long totalTimeoutMillis, long pollIntervalMillis, StopWatch timer) {
        List<HTMLElement> clauseResults = new ArrayList<HTMLElement>();
        while (clauseResults.size() == 0 || (clauseResults.size() != 1 && !allowMultipleMatches && waitUntilSingle)) {
            clauseResults = findElements(parentElement, objectMapping);
            if (clauseResults.size() == 0 || (clauseResults.size() != 1 && !allowMultipleMatches && waitUntilSingle)) {
                if (clauseResults.size() > 0 && showMultiFound) {
                    Logger.WriteLine(Logger.LogLevels.TestDebug, "Found %d elements matching [%s].  Waiting until only a single element is found...", clauseResults.size(), objectMapping.getActualFindLogic());
                    showMultiFound = false;
                }
                try {
                    Thread.sleep(pollIntervalMillis);
                } catch (Exception e) {
                    Logger.WriteLine(Logger.LogLevels.Error, "Thread.sleep threw an exception after %s so aborting", durationFormatted(timer.getTime()));
                    throw new RuntimeException(String.format("Exception thrown while thread sleeping during Find Element (for [%s]) poll interval!", objectMapping.getFriendlyName()));
                }
                if (timer.getTime() >= totalTimeoutMillis) break;
            }
        }
        return clauseResults;
    }

    public List<HTMLElement> findElements(HTMLElement parentElement, ObjectMapping mapping) {

        List<WebElement> foundElements;
        List<HTMLElement> returnElements = new ArrayList<HTMLElement>();


        if (mapping==null) {
            Logger.WriteLine(Logger.LogLevels.Error,"ObjectMapping = null!");
            throw new RuntimeException("SeleniumDriver.FindElements called with mapping null!");
        }

        Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"ObjectMapping = [%s] (%s)",mapping.getOriginalFindLogic(),mapping.getFriendlyName());

        By seleniumFindBy = mapping.getSeleniumBy();


        try {
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"Calling Selenium WebDriver findElements with By = [%s]",seleniumFindBy.toString());
            foundElements = (parentElement==null) ? webDriver.findElements(seleniumFindBy) : parentElement.getSeleniumnWebElement().findElements(seleniumFindBy);
        }
        catch (Exception e) {
            if (parentElement==null) {
                Logger.WriteLine(Logger.LogLevels.Error, "Selenium error finding elements using find logic [%s] ([%s)]: %s", seleniumFindBy.toString(), mapping.getFriendlyName(), e.toString());
                throw new RuntimeException(String.format("Selenium error finding elements using find logic [%s] ([%s)]", seleniumFindBy.toString(), mapping.getFriendlyName()));
            }
            else {
                Logger.WriteLine(Logger.LogLevels.Error, "Selenium error finding elements offset from [%s (%s)] using find logic [%s] ([%s)]: %s",parentElement.getMappingDetails().getFriendlyName(),parentElement.getMappingDetails().getActualFindLogic(),seleniumFindBy.toString(), mapping.getFriendlyName(), e.toString());
                throw new RuntimeException(String.format("Selenium error finding elements offset from [%s (%s)] using find logic [%s] ([%s)]: %s",parentElement.getMappingDetails().getFriendlyName(),parentElement.getMappingDetails().getActualFindLogic(),seleniumFindBy.toString(), mapping.getFriendlyName()));
            }
        }

        if (parentElement==null)
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Found [%d] elements matching [%s] (%s)",foundElements.size(),mapping.getOriginalFindLogic(),mapping.getFriendlyName());
        else
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Found [%d] elements matching [%s] (%s) offset from [%s]",foundElements.size(),mapping.getOriginalFindLogic(),mapping.getFriendlyName(),parentElement.getMappingDetails().getFriendlyName());


        for(int index = 0;index<foundElements.size();index++) {
            ObjectMapping actualMapping = mapping.copy();
           if (actualMapping.getMappingType() == ObjectMapping.ByType.XPath) {
               // We do this for xPath but what about the other types...  Maybe we should ONLY support xpath???
               // Note that Selenium uses 1-based indexing so in the xpath we construct we add 1.
               actualMapping.setActualFindLogic(String.format("(%s)[%s]",actualMapping.getOriginalFindLogic(), Integer.toString(index+1)));
           }
           HTMLElement htmlElement = new HTMLElement(this,foundElements.get(index),actualMapping);
           returnElements.add(htmlElement);
        }
        return returnElements;
    }


    //////////// JAVASCRIPT EXECUTION

    /// <summary>Injects and executes Javascript in the currently active Selenium browser with no exception thrown. Javascript has return object which is passed back as a string</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    public <T> T executeJavaScript(Class<T> type, String script, Object... args)
    {
        Object result;
        try
        {
            result = ((JavascriptExecutor)webDriver).executeScript(script, args);
            return type.cast(result);
        }
        catch (Exception ex)
        {
            String exceptionString = "";
            for (Object arg : args)
            {
                if (exceptionString.isEmpty())
                    exceptionString = String.format("executeJavaScript(\"%s\")-(Args: \"%s\"", script, arg.getClass().getName());
                else
                    exceptionString = String.format("%s, \"%s\"", exceptionString, arg.getClass().getName());
            }
            if (exceptionString.isEmpty())
                exceptionString = String.format("executeJavaScript(\"%s\"): %s",script, exceptionString);
            throw new RuntimeException(exceptionString,ex);
        }
    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed.</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    public void executeJavaScriptNoReturnData(String script, Object[] args)
    {
        Object dummy = executeJavaScript(Object.class,script,args);
    }

    public static void resetSettings() {
    }

    private void setSeleniumHost(String host) {
        seleniumHost=host;
        try {
            String seleniumHostFromTestData = TestData.getItem(String.class, ConfigHost[0], ConfigHost[1]);
            if (!seleniumHostFromTestData.isEmpty()) {
                if (host!=null && !host.isEmpty()) {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, String.format("Selenium Host set in Test Data.  Overriding passed host with [%s] (Test data [%s.%s])",seleniumHostFromTestData, ConfigHost[0], ConfigHost[1]));
                }
                seleniumHost = seleniumHostFromTestData;
            }
        }
        catch (Exception e) {
            if (seleniumHost==null || seleniumHost.isEmpty()) {
                Logger.WriteLine(Logger.LogLevels.TestInformation, String.format("Selenium Host not set in Test data ([%s.%s]). Default to local.)", ConfigHost[0], ConfigHost[1]));
            }
            seleniumHost="localhost";
        }
    }

    private String durationFormatted(Duration duration) {
        return durationFormatted(duration.toMillis());
    }
    private String durationFormatted(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - (hours*60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - (hours*3600) - (minutes*60);
        long milliseconds = millis - (seconds*1000) - (minutes*60000) - (hours*3600000);
        return String.format("%02d:%02d:%02d.%03d",hours,minutes,seconds,milliseconds);
    }


}

