package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;

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
    private final long defaultTimeout = 60000; // 1 Minute
    private final long defaultPollInterval = 500; // 500mS

    public SeleniumDriver() {
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
    }
    public Duration getPageLoadTimeout() { return _pageLoadTimeout;}





    public HTMLElement FindElement(HTMLElement parentElement,ObjectMapping objectMapping, boolean allowMultipleMatches,boolean waitUntilSingle, Duration timeout, Duration pollInterval, boolean waitUntilStable) {
        List<HTMLElement> clauseResults = new ArrayList<HTMLElement>();

        //
        // We look after our own implicitWait (timeout) and poll interval rather that Selenium's as we are using our FindElements to find the element we want.  We use our
        // FindElements so that we can control if ONLY a single match is allowed (Selenium FindElement allows multiple matches silently - see By class implementation) and extra debug logging.
        //
        long totalTimeoutMillis = timeout.toMillis();
        long pollIntervalMillis = pollInterval.toMillis();

        Logger.WriteLine(Logger.LogLevels.TestDebug, "Timeout - %s",DurationFormatted(timeout));
        StopWatch timer = StopWatch.createStarted();

        // Loop while:-
        //  - we have no find results
        //  or
        //  - we have more than 1 match and we only want a single match but are ok waiting until we have only a single match
        // we do at least one find!
        while (clauseResults.size()==0 || (clauseResults.size()!=1 && !allowMultipleMatches  && waitUntilSingle)) {
            clauseResults = FindElements(parentElement,objectMapping);
            if (clauseResults.size()==0) {
                try {
                    Thread.sleep(pollIntervalMillis);
                }
                catch (Exception e) {
                    timer.stop();
                    Logger.WriteLine(Logger.LogLevels.Error,"Thread.sleep threw an exception after %s so aborting",DurationFormatted(timer.getTime()));
                    throw new RuntimeException(String.format("Exception thrown while thread sleeping during Find Element (for [%s]) poll interval!",objectMapping.getFriendlyName()));
                }
                if (timer.getTime()>=totalTimeoutMillis) break;
            }
        }


        MAT ENSURE THE waitUntilSingle WORKS OK!!!!

        timer.stop();

        if (clauseResults.size() > 1 && !allowMultipleMatches) {
            Logger.WriteLine(Logger.LogLevels.Error, "Found %d matching elements in %s. Do not allow multiple matches so error!", clauseResults.size(), DurationFormatted(timer.getTime()));
            throw new RuntimeException(String.format("Found %d elemments using [%s] find logic. allowMultipleMatches false so multiple matches not allowed!",
                                                      clauseResults.size(), objectMapping.getActualFindLogic()));



                    ((ParentElement == null) ? "DOM Top Level" : (string.IsNullOrEmpty(ParentElement.MappingDetails.FriendlyName) ? ("Unknown Parent (" + ParentElement.MappingDetails.FindLogic + ")") : ParentElement.MappingDetails.FriendlyName)), Mapping, clauseResults.Count);
            element = null;
            return false;

        }



    }


    public List<HTMLElement> FindElements(HTMLElement parentElement, ObjectMapping mapping) {

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

    private String DurationFormatted(Duration duration) {
        return DurationFormatted(duration.toMillis());
    }
    private String DurationFormatted(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - (hours*60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - (hours*3600) - (minutes*60);
        long milliseconds = millis - (seconds*1000) - (minutes*60000) - (hours*3600000);
        return String.format("%02d:%02d:%02d.%03d",hours,minutes,seconds,milliseconds);
    }


}

