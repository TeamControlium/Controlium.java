package TeamControlium.Controlium;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.print.attribute.standard.PagesPerMinute;
import java.time.Duration;
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

        MAT THIS IS WHERE WE ARE - DOING THIS SO THAT THE ELEMENTS WORK CAN CALL FOR TIMEOUTS.......
        NEED TO PUT LOTS OF COMMENTS IN AROUND WHY ARE DOING THE TIMEOUTS AND NOT LETTING SELENIUM



    }



    // PROPERTIES
    public Duration setFindTimeout(Duration findTimeout) { _findTimeout = findTimeout; return getFindTimeout();}
    public Duration getFindTimeout() { return _findTimeout;}
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






    //////////// JAVASCRIPT EXECUTION

    /// <summary>Injects and executes Javascript in the currently active Selenium browser with no exception thrown. Javascript has return object which is passed back as a string</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    public <T> T ExecuteJavaScript(Class<T> type,String script, Object... args)
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
                    exceptionString = String.format("ExecuteJavaScript(\"%s\")-(Args: \"%s\"", script, arg.getClass().getName());
                else
                    exceptionString = String.format("%s, \"%s\"", exceptionString, arg.getClass().getName());
            }
            if (exceptionString.isEmpty())
                exceptionString = String.format("ExecuteJavaScript(\"%s\"): %s",script, exceptionString);
            throw new RuntimeException(exceptionString,ex);
        }
    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed.</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    public void ExecuteJavaScriptNoReturnData(String script, Object[] args)
    {
        Object dummy = ExecuteJavaScript(Object.class,script,args);
    }
}

