package TeamControlium.Controlium;

import TeamControlium.Controlium.Exception.InvalidElementState;
import TeamControlium.Utilities.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.w3c.dom.NodeList;

import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

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
    private boolean isLocalSelenium=true;
    private String seleniumServerFolder=null;
    private boolean seleniumDebugMode=false;
    private String seleniumLogFilename=null;

    public SeleniumDriver(String seleniumHost,String device,String browser) {
        commonConstructs(false,seleniumHost,device,browser);
    }
    public SeleniumDriver(String seleniumHost) {
        commonConstructs(false,seleniumHost,null,null);
    }
    public SeleniumDriver(String device,String browser) {
        commonConstructs(false,null,device,browser);
    }
    public SeleniumDriver() {
        commonConstructs(false,null,null,null);
    }
    public SeleniumDriver(boolean killDriverInstancesFirst,String seleniumHost,String device,String browser) {
        commonConstructs(killDriverInstancesFirst,seleniumHost,device,browser);
    }
    public SeleniumDriver(boolean killDriverInstancesFirst,String seleniumHost) {
        commonConstructs(killDriverInstancesFirst,seleniumHost,null,null);
    }
    public SeleniumDriver(boolean killDriverInstancesFirst,String device,String browser) {
        commonConstructs(killDriverInstancesFirst,null,device,browser);
    }
    public SeleniumDriver(boolean killDriverInstancesFirst) {
        commonConstructs(killDriverInstancesFirst,null,null,null);
    }

    private void commonConstructs(boolean killFirst, String seleniumHost,String device,String browser) {
        // Initialise defaults
        setFindTimeout(Duration.ofMillis(defaultTimeout));
        setPollInterval(Duration.ofMillis(defaultPollInterval));
        setPageLoadTimeout(Duration.ofMillis(defaultTimeout));

        // Selenium Parameters
        try {
            seleniumServerFolder = TestData.getItem(String.class, SeleniumServerFolder[0], SeleniumServerFolder[1]);
        }
        catch (Exception e){
            Logger.WriteLine(Logger.LogLevels.TestDebug, "Selenium Server Folder not set in TestData (%s.%s).  Defaulting to local",SeleniumServerFolder[0], SeleniumServerFolder[1]);
            seleniumServerFolder = System.getProperty("user.dir");
        }
        Logger.WriteLine(Logger.LogLevels.TestInformation, "Selenium Server Folder [%s]",seleniumServerFolder);

        try {
            seleniumDebugMode = TeamControlium.Utilities.General.IsValueTrue(TestData.getItem(String.class, SeleniumDebugMode[0], SeleniumDebugMode[1]));
        }
        catch (Exception e){
            Logger.WriteLine(Logger.LogLevels.TestDebug, "Selenium Server debug mode not set in TestData (%s.%s).  Defaulting to off",SeleniumDebugMode[0], SeleniumDebugMode[1]);
            seleniumDebugMode=false;
        }
        Logger.WriteLine(Logger.LogLevels.TestInformation, "Selenium Debug Mode: [%s]",seleniumDebugMode?"on":"off");

        try {
            seleniumLogFilename = TestData.getItem(String.class, SeleniumLogFilename[0], SeleniumLogFilename[1]);
        }
        catch (Exception e){
            Logger.WriteLine(Logger.LogLevels.TestDebug, "Selenium Log filename not set in TestData (%s.%s).  Defaulting to stdio (console)",SeleniumLogFilename[0], SeleniumLogFilename[1]);
            seleniumLogFilename=null;
        }
        Logger.WriteLine(Logger.LogLevels.TestInformation, "Selenium Log filename: [%s]",seleniumLogFilename==null?"stdio (console)":seleniumLogFilename);

        Browsers.SetTestBrowser(browser);
        Devices.SetTestDevice(device);

        setSeleniumHost(seleniumHost);

        startOrConnectToSeleniumServer(killFirst);
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

    public void setIFrame(HTMLElement htmlElement) {
        WebElement webElement = ((WebElement)htmlElement.getUnderlyingWebElement());
        String tag = webElement.getTagName();
        if (tag.equalsIgnoreCase("iframe")) {
            try {
                webDriver.switchTo().frame(webElement);
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("Error setting element [%s] ([%s]) as iframe window in Selenium.",htmlElement.getMappingDetails().getOriginalFindLogic(),htmlElement.getMappingDetails().getFriendlyName()),e);
            }
        }
        else
        {
            throw new RuntimeException(String.format("Element [%s] ([%s)] is [%s].  Must be an iframe!",htmlElement.getMappingDetails().getOriginalFindLogic(),htmlElement.getMappingDetails().getFriendlyName(),tag));
        }
    }


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

        StopWatch timer = StopWatch.createStarted();
        while (true) {



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
                String errorText = String.format("Found %d matching elements using [%s] ([%s]) from [%s]. Not allowing mutiple matches and timeout reached after [%dmS]",
                        clauseResults.size(),
                        objectMapping.getActualFindLogic(),
                        objectMapping.getFriendlyName(),
                        (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                        timer.getTime());
                Logger.WriteLine(Logger.LogLevels.Error, errorText);
                throw new RuntimeException(errorText);
            }

            // At this point we have a single match OR multiple matches
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
                    break;
                } else {
                    if (timer.getTime()>=totalTimeoutMillis) {
                        Logger.WriteLine(Logger.LogLevels.Error, "From [%s], find [%s (%s)] returned %d matches (%sllowing multiple matches). Element NOT stable after timeout reached so throwing",
                                (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                                objectMapping.getActualFindLogic(),
                                objectMapping.getFriendlyName(),
                                clauseResults.size(),
                                (allowMultipleMatches) ? "A" : "Not A");
                        throw new RuntimeException(String.format("From [%s], find [%s (%s)] returned %d matches (%sllowing multiple matches). Element NOT stable after timeout reached ([%dmS]).",
                                (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                                objectMapping.getActualFindLogic(),
                                objectMapping.getFriendlyName(),
                                clauseResults.size(),
                                (allowMultipleMatches) ? "A" : "Not A",
                                timer.getTime()));
                    }
                    Logger.WriteLine(Logger.LogLevels.TestDebug, "From [%s], find [%s (%s)] returned %d matches (%s multiple matches).  Element 0 is NOT stable and we must wait until stable...",
                            (parentElement == null) ? "DOM Top Level" : parentElement.getMappingDetails().getFriendlyName(),
                            objectMapping.getActualFindLogic(),
                            objectMapping.getFriendlyName(),
                            clauseResults.size(),
                            (allowMultipleMatches) ? "Allowing" : "Not");
                }
            } else {
                break;
            }
        }
        return clauseResults.get(0);
    }

    public List<HTMLElement> findElements(HTMLElement parentElement, ObjectMapping mapping) {

        List<WebElement> foundElements=null;
        List<HTMLElement> returnElements = new ArrayList<HTMLElement>();

        if (mapping==null) {
            Logger.WriteLine(Logger.LogLevels.Error,"ObjectMapping = null!");
            throw new RuntimeException("SeleniumDriver.FindElements called with mapping null!");
        }

        Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"ObjectMapping = [%s] (%s)",mapping.getOriginalFindLogic(),mapping.getFriendlyName());

        By seleniumFindBy = mapping.getSeleniumBy();


        try {
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"Calling Selenium WebDriver findElements with By = [%s]",seleniumFindBy.toString());
            if (parentElement==null) {
                foundElements = webDriver.findElements(seleniumFindBy);
            }
            else {
                foundElements=((WebElement)parentElement.getUnderlyingWebElement()).findElements(seleniumFindBy);
            }
        }
        catch (InvalidSelectorException e) {
            throw new RuntimeException(String.format("Selenium Driver error. Find Logic [%s] for [%s] is invalid!",mapping.getActualFindLogic(),mapping.getFriendlyName(),e));
        }
        catch (WebDriverException e)
        {
            checkIfConnectionIssue(e);
            throw new RuntimeException("Selenium Driver error finding elements.  See inner exception.",e);
        }
        catch (Exception e) {
            if (parentElement==null) {
                Logger.WriteLine(Logger.LogLevels.Error, "Selenium error finding elements using find logic [%s] ([%s)]: %s", seleniumFindBy.toString(), mapping.getFriendlyName(), e.toString());
                throw new RuntimeException(String.format("Selenium error finding elements using find logic [%s] ([%s)]", seleniumFindBy.toString(), mapping.getFriendlyName()),e);
            }
            else {
                Logger.WriteLine(Logger.LogLevels.Error, "Selenium error finding elements offset from [%s (%s)] using find logic [%s] ([%s)]: %s",parentElement.getMappingDetails().getFriendlyName(),parentElement.getMappingDetails().getActualFindLogic(),seleniumFindBy.toString(), mapping.getFriendlyName(), e.toString());
                throw new RuntimeException(String.format("Selenium error finding elements offset from [%s (%s)] using find logic [%s] ([%s)]: %s",parentElement.getMappingDetails().getFriendlyName(),parentElement.getMappingDetails().getActualFindLogic(),seleniumFindBy.toString(), mapping.getFriendlyName()));
            }
        }

        if (parentElement==null)
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"Found [%d] elements matching [%s] (%s)",foundElements.size(),mapping.getOriginalFindLogic(),mapping.getFriendlyName());
        else
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug,"Found [%d] elements matching [%s] (%s) offset from [%s]",foundElements.size(),mapping.getOriginalFindLogic(),mapping.getFriendlyName(),parentElement.getMappingDetails().getFriendlyName());


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


    public void gotoURL(String fullURLPath) {
        try {
            webDriver.navigate().to(fullURLPath);
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Error browsing to [%s]: %s",(fullURLPath==null || fullURLPath.isEmpty())?"NO URL!!":fullURLPath,e.getMessage());
            checkIfConnectionIssue(e);
            throw e;
        }
    }

    public String getPageTitle() {
        try {
            String title = webDriver.getTitle();
            return (title==null?"":title);
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Error getting window title: %s",e.getMessage());
            checkIfConnectionIssue(e);
            throw e;
        }
    }


    //
    // ELEMENT Based interaction
    //

    public boolean isDisplayed(Object webElement) {
        if (webElement==null) throw new RuntimeException("webElement null!");
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Get element displayed status using Selenium IWebElement.isDisplayed");
        try {
            return ((WebElement) webElement).isDisplayed();
        }
        catch (InvalidElementStateException e) {
            //
            // Usually thrown by Selenium when element stale
            //
            throw new InvalidElementState("Unable to get element visibility.  See underlying cause.",e);
        }
    }
    public boolean isEnabled(Object webElement) {
        if (webElement==null) throw new RuntimeException("webElement null!");
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Get element enabled status using Selenium IWebElement.isEnabled");
        try {
            return ((WebElement) webElement).isEnabled();
        }
        catch (InvalidElementStateException e) {
            //
            // Usually thrown by Selenium when element stale
            //
            throw new InvalidElementState("Unable to get element enabled status.  See underlying cause.",e);
        }
    }

    public void clear(Object webElement) {
        if (webElement==null) throw new RuntimeException("webElement null!");
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Clearing element using Selenium IWebElement.Clear");
        try {
            ((WebElement) webElement).clear();
        }
        catch (InvalidElementStateException e) {
            //
            // Usually thrown by Selenium when element stale
            //
            throw new InvalidElementState("Unable to clear element.  See underlying cause.",e);
        }
    }

    public void setText(Object webElement, String text) {
        //
        // This method will probably start to expand as different browsers/devices highlight different issues with entering text....
        //
        // A possible need is to wait until text can be entered:-
        //IWebElement aa = ElementFindTimeout.Until((b) => { if (IsElementVisible(WebElement) && IsElementEnabled(WebElement)) return WebElement; else { Logger.WriteLn(this, "SetText", "Polling until Text can be entered"); return null; } });
        if (webElement==null) throw new RuntimeException("webElement null!");
        text = (text==null)?"":text;
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Entering text using Selenium IWebElement SendKeys: [%s].", text);
        try {
            ((WebElement) webElement).sendKeys(text);
        }
        catch (InvalidElementStateException e) {
            //
            // Usually thrown by Selenium when element stale
            //
            throw new InvalidElementState("Unable to set element text.  See underlying cause.",e);
        }
    }

    public String getText(Object webElement,boolean includeDescendantsText, boolean scrollIntoViewFirst, boolean useInnerTextAttribute) {
        if (webElement==null) throw new RuntimeException("webElement null!");
        //
        // This is a bit odd and there are two issues involved but which boil down to a single one.  GetText MUST only return text the user can see (or what
        // is the point of what we are trying to achive...).  So we must ensure we can see the text we are returning.  Note that there is one point being
        // deliberatly ignored; foreground/background text colour.  In an ideal world we should only return text where the colours are sufficiently different
        // for a human to read.  But, what is sufficient?  Should we take into account common colour-blindness issues etc etc etc....  So, just stay simple for
        // now and ignore text colours etc....
        //
        ///////
        //
        // We may want to scroll into view first; Because
        //          (a) A user would not be able to get/read the text if hidden so if they cannot see it then it would be a bad test that returned hidden text.
        //          (b) If the element is outside the viewport getText will return an empty string.
        // 
        if (scrollIntoViewFirst) scrollIntoView(webElement);
        ///////
        //
        // We may use innerText rather than TextContent (as the Text() property does).  InnerText returns the text being presented to the user
        // whereas TextContent returns the raw text in all nodes within the element - irrelevant of whether hidden or not.
        //
        StringBuilder text = new StringBuilder();
        if (includeDescendantsText)
        {
            if (useInnerTextAttribute)
            {
                text.append(((WebElement)webElement).getAttribute("innerText"));
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Get element text using element innerText attribute: [{0}]", text);
            }
            else
            {
                text.append(((WebElement)webElement).getText());
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Get element text using Selenium Text property: [{0}]", text);
            }
        }
        else
        {
            Reader stringReader = new StringReader(((WebElement)webElement).getAttribute("outerHTML"));
            HTMLEditorKit htmlKit = new HTMLEditorKit();
            HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
            HTMLEditorKit.Parser parser = new ParserDelegator();
            try {
                parser.parse(stringReader, htmlDoc.getReader(0), true);
            }
            catch (Exception e) {
                throw new RuntimeException(String.format("Error parsing HTML from element outerHTML!"),e);
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nl=null;
            try {
                XPathExpression exp = xPath.compile("/*/text()");
                nl = (NodeList)exp.evaluate(htmlDoc,XPathConstants.NODESET);
            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Error with XPath!"),e);
            }

            for(int index=0;index<nl.getLength();index++) {
                text.append(nl.item(index).getTextContent());
            }
        }
        return text.toString();
    }

    public void click(Object webElement) {
        if (webElement == null) throw new RuntimeException("webElement null!");

        try {
            ((WebElement) webElement).click();
        } catch (WebDriverException wde) {
            String errorMessage = wde.getMessage();
            if (errorMessage.toLowerCase().contains("other element would receive the click")) {
                WebElement offendingElement;
                try {
                    //
                    // This is an old chestnut... Have had many an argument with the Selenium team about the 'isClickable'
                    // property.  It simply doesnt work and they wont change it 'for historical reasons' Eeeesh (See
                    // https://code.google.com/p/selenium/issues/detail?id=6804).
                    // So, we need to throw a slightly more intelligent exception here so that the tester, or underlying
                    // tool can fathom out what is going on...
                    //
                    // We'll use Javascript to find out what the topmost element is at the location the click is firing at.  At least we
                    // will then know what element is getting that there click...
                    //
                    // Get the xy coordinates from string "Other element would receive the click at point (123,456)"
                    String[] xy = errorMessage.split("point [(]", 2)[1].split("[)]", 2)[0].replaceAll("\\s+", "").split("[,]", 2);

                    offendingElement = executeJavaScript(WebElement.class, "return document.elementFromPoint(arguments[0],arguments[1]);", xy[0], xy[1]);
                    if (offendingElement == null) throw new RuntimeException("No element returned from javascript!");
                } catch (Exception e) {
                    //
                    // We have an error!  We need to tell the caller what the problem was BUT then leave the underylying Click error trace......
                    //
                    throw new RuntimeException(String.format("Error determining click coordinates for click from WebDriverException message: %s", e.getMessage()), wde);
                }
                // Finally throw the exception correctly identifying the element that is covering our click attempt
                throw new InvalidElementState(String.format("Element could not be clicked as another element would get the click.  Offending element (Element and all descendants shown):\r\n%s", offendingElement.getAttribute("outerHTML")), wde);
            }

            // No idea what it is - so whatever, rethrow...
            throw wde;
        }
    }

    public String getAttribute(Object webElement,String attribute) {
        if (webElement==null) throw new RuntimeException("webElement null!");

        try {
            String attrib = ((WebElement)webElement).getAttribute(attribute);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Got attribute [%s]: [%s]", attribute,(attrib==null)?"Null":attrib);
            return (attrib==null)?"":attrib;

        }
        catch (InvalidElementStateException e) {
            //
            // Usually thrown by Selenium when element stale
            //
            throw new InvalidElementState(String.format("Unable to get element attribute [%s].  See underlying cause.",(attribute==null)?"Null!!":attribute),e);
        }
    }

    public void scrollIntoView(Object webElement) {
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Scrolling element in to view using JavaScript injection - [Element].scrollIntoView()");
        executeJavaScriptNoReturnData("arguments[0].scrollIntoView();", webElement);
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
        Object result=null;
        try
        {
            result = ((JavascriptExecutor)webDriver).executeScript(script, args);
        }
        catch (WebDriverException e)
        {
            checkIfConnectionIssue(e);
            throw new RuntimeException("Selenium Driver error executing javascript.  See inner exception.",e);
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
        return type.cast(result);
    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed.</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    public void executeJavaScriptNoReturnData(String script, Object... args)
    {
       Object dummy = executeJavaScript(Object.class,script,args);
    }


    public String TakeScreenshot() { return TakeScreenshot(null);}
    public String TakeScreenshot(String fileName)
    {
        String filename = null;
        String filepath = null;

        try
        {
            if (webDriver != null)
            {
                if (webDriver instanceof TakesScreenshot)
                {
                    try
                    {
                        fileName = TestData.getItem(String.class, "Screenshot", "Filename");
                    }
                    catch (Exception e){ }
                    try
                    {
                        filepath = TestData.getItem(String.class, "Screenshot", "Filepath");
                    }
                    catch (Exception e) { }

                    if (filename == null) filename = (fileName==null)?"Screenshot" : fileName;

                    //
                    // Ensure filename friendly
                    //
                    //filename = new Regex(string.Format("[{0}]", Regex.Escape(new string(Path.GetInvalidFileNameChars()) + new string(Path.GetInvalidPathChars())))).Replace(filename, "");
                    if (filepath == null)
                    {

                        filename = Paths.get(System.getProperty("user.dir"),"images", filename + ".jpg").toString();
                    }
                    else
                    {
                        filename = Paths.get(filepath, filename + ".jpg").toString();
                    }
                    File screenshot=null;
                    try {
                        screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
                        Logger.WriteLine(Logger.LogLevels.TestInformation, "Screenshot - {0}", filename);
                    }
                    catch (Exception e) {
                        checkIfConnectionIssue(e);
                        throw new RuntimeException("Selenium Driver error taking screenshot.  See inner exception.",e);
                    }

                    try {
                        FileUtils.copyFile(screenshot, new File(filename));
                    }
                    catch (Exception e) {
                        Logger.WriteLine(Logger.LogLevels.Error, "Saving Screenshot - %s: %s", filename,e);
                    }
                    return filename;
                }
                    else
                {
                    throw new RuntimeException("Error taking webDriver does not implement TakesScreenshot!  Is it RemoteWebDriver?");
                }
            }
            else
            {
                Logger.WriteLine(Logger.LogLevels.TestInformation,"webDriver is null!  Unable to take screenshot.");
            }
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Exception saving screenshot [{0}]", filename==null?"filename null!":filename);
            Logger.WriteLine(Logger.LogLevels.TestInformation, "> {0}", ex);
        }
        return "";
    }


    public void CloseDriver() {
        boolean TakeScreenshotOption = false;
        try {
            try {
                TakeScreenshotOption = General.IsValueTrue(TestData.getItem(String.class, "Debug", "TakeScreenshot"));
            } catch (Exception ex) {
                Logger.WriteLine(Logger.LogLevels.TestInformation, "RunCategory Option [Debug, TakeScreenshot] Exception ignored, defaults to false: %s", ex);
            }


            if (TakeScreenshotOption) {
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Debug.TakeScreenshot = {0} - Taking screenshot...", TakeScreenshotOption);
                TakeScreenshot(Detokenizer.ProcessTokensInString("{Date;today;yy-MM-dd_HH-mm-ssFFF}"));
            } else
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Debug.TakeScreenshot = {0} - NOT Taking screenshot...", TakeScreenshotOption);

            try {
                if (webDriver != null) webDriver.quit();
                webDriver = null;
            } catch (Exception e) {
                try {
                    checkIfConnectionIssue(e);
                    throw new RuntimeException("Selenium Driver error closing Selenium.  See inner exception.", e);
                } catch (Exception ex) {
                    Logger.WriteLine(Logger.LogLevels.Error, "Ignoring Error: %s", ex);
                    webDriver = null;
                }
            }
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,String.format("Error closing selenium driver: %s",e.getMessage()));
            // Dummy to be sure we dont pollute results!
        }
    }

    private void startOrConnectToSeleniumServer(boolean killFirst) {
        if (isLocalSelenium) {
            setupLocalRun(killFirst);
        } else {
            throw new RuntimeException("Remote server execution mot yet implemented!");
        }
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
        isLocalSelenium = (seleniumHost.equalsIgnoreCase("localhost") || seleniumHost.equals("127.0.0.1"));
    }

    private void setupLocalRun(boolean killFirst) {
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Running Selenium locally");

        try {
            if (Browsers.isInternetExplorer()) {
                //
                // See https://code.google.com/p/selenium/issues/detail?id=4403
                //
                String executable="IEDriver.exe";
                InternetExplorerOptions IEO = new InternetExplorerOptions();
                IEO.destructivelyEnsureCleanSession();

                setPathToDriverIfExistsAndIsExecutable(seleniumServerFolder, InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY,executable);
                if (seleniumDebugMode)
                    System.setProperty(InternetExplorerDriverService.IE_DRIVER_LOGLEVEL_PROPERTY, "INFO");


                if (seleniumLogFilename != null) {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to: %s", seleniumLogFilename);
                    System.setProperty(InternetExplorerDriverService.IE_DRIVER_LOGFILE_PROPERTY, CheckAndPreparSeleniumLogFile(seleniumLogFilename));
                } else {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to console");
                }

                InternetExplorerDriverService service = InternetExplorerDriverService.createDefaultService();

                IEO.setCapability("INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS", (boolean) true);  // Enabling this as part of #ITSD1-1126 - If any issues come back to request
                Logger.WriteLine(Logger.LogLevels.TestInformation, "IE Browser being used.  Setting INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS active. #ITSD1-1126");
                webDriver = new InternetExplorerDriver(service, IEO);
            }
            else if (Browsers.isChrome()) {
                String executable = "ChromeDriver.exe";
                ChromeOptions options = new ChromeOptions();



                setPathToDriverIfExistsAndIsExecutable(seleniumServerFolder, ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,executable);
                if (seleniumDebugMode) System.setProperty(ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");

                if (seleniumLogFilename != null) {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to: %s", seleniumLogFilename);
                    System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, CheckAndPreparSeleniumLogFile(seleniumLogFilename));
                } else {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to console");
                }

                if (killFirst) killAllProcesses(executable);
                webDriver = new ChromeDriver(ChromeDriverService.createDefaultService(), options);
            }
            else if (Browsers.isEdge()) {
                String executable = "EdgeDriver.exe";
                EdgeOptions options = new EdgeOptions();

                setPathToDriverIfExistsAndIsExecutable(seleniumServerFolder, EdgeDriverService.EDGE_DRIVER_EXE_PROPERTY,executable);
                if (seleniumDebugMode) System.setProperty(EdgeDriverService.EDGE_DRIVER_VERBOSE_LOG_PROPERTY, "true");

                if (seleniumLogFilename != null) {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to: %s", seleniumLogFilename);
                    System.setProperty(EdgeDriverService.EDGE_DRIVER_LOG_PROPERTY, CheckAndPreparSeleniumLogFile(seleniumLogFilename));
                } else {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to console");
                }

                webDriver = new EdgeDriver(EdgeDriverService.createDefaultService(), options);
            }
            else {
                throw new RuntimeException(String.format("Browser [%s] not yet implemented!",Browsers.getBrowserType().name()));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error instantiating [%s] (%s)", Browsers.isChrome() ? "Chrome" : Browsers.isEdge() ? "Edge" : Browsers.isInternetExplorer() ? "Internet Explorer" : Browsers.isSafari() ? "Safari" : "UNKNOWN!", seleniumServerFolder));
        }
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

    private void setPathToDriverIfExistsAndIsExecutable(final String pathToDriver, final String driverExeProperty,String executable) {
        final File driver = new File(pathToDriver,executable);
        if (driver.exists() && driver.canExecute()) {
            System.setProperty(driverExeProperty, driver.getAbsolutePath());
        } else {
            throw new IllegalArgumentException(String.format("Driver not found or is not executable in %s", pathToDriver));
        }
    }

    private String CheckAndPreparSeleniumLogFile(String SeleniumDebugFile) {
        String seleniumDebugFile = SeleniumDebugFile;
        String pathAndFile = "";

        if (seleniumDebugFile==null || seleniumDebugFile.isEmpty())
            return null;
        else
        {
            //
            // If path is relative, make it absolute..
            //
            final File debugFile = new File(seleniumDebugFile);
            String seleniumDebugFileFolder = debugFile.getAbsolutePath();

            // File path/name is passed on CMD line so remove all spaces
            String seleniumDebugFileName = FilenameUtils.removeExtension(seleniumDebugFile);
            String seleniumDebugFileExt = FilenameUtils.getExtension(seleniumDebugFile);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "SeleniumDebugFile: [%s]",seleniumDebugFile);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "seleniumDebugFileFolder: [%s]",seleniumDebugFileFolder);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "seleniumDebugFileName: [%s]",seleniumDebugFileName);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "seleniumDebugFileExt: [%s]",seleniumDebugFileExt);


            if (seleniumDebugFileFolder==null || seleniumDebugFileFolder.isEmpty())  seleniumDebugFileFolder = System.getProperty("user.dir");

            try
            {
                int TotalPathLength = seleniumDebugFileFolder.length() + seleniumDebugFileName.length() + seleniumDebugFileExt.length() + 2;
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Selenium Debug File - [%s %s.%s]", seleniumDebugFileFolder, seleniumDebugFileName, seleniumDebugFileExt);
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Selenium Debug File TotalPathLength = %d", TotalPathLength);
                if (TotalPathLength > 248)
                {
                    //
                    // Max path length is 248, so we need to fiddle....
                    //
                    if ((seleniumDebugFileFolder.length() - seleniumDebugFileName.length() - seleniumDebugFileExt.length() -2) > 248)
                    {
                        // Ok, we cant do it so bomb out.
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "seleniumDebugFileFolder length %d so cannot fix path length by truncating seleniumDebugFileName", seleniumDebugFileFolder.length());
                        throw new RuntimeException(String.format("Cannot Selenium Debug file.  Full path [%d] would have been too long (Max 248 chars)",TotalPathLength));
                    }
                    else
                    {
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Reducing path length by truncating seleniumDebugFileName (length currently %d)", seleniumDebugFileName.length());
                        // Ok, we can do it.  Just truncate the TestID the required length...
                        seleniumDebugFileName = seleniumDebugFileName.substring(0, seleniumDebugFileName.length() - (TotalPathLength - 248));
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Reduced to length %d", seleniumDebugFileName.length());
                    }
                }

                pathAndFile = Paths.get(seleniumDebugFileFolder,(seleniumDebugFileName + seleniumDebugFileExt)).toString();

                (new File(pathAndFile)).mkdirs();

                Files.write(Paths.get(pathAndFile), Arrays.asList("TeamControlium Selenium Debug File"));
                return pathAndFile;
            }
            catch (Exception ex)
            {
                throw new RuntimeException(String.format("Error creating Selenium Debug information file (%s): %s", pathAndFile,ex.getMessage()));
            }
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

    private void killAllProcesses(String name) {
        int matchingProcessCount = getProcessCount(name);
        int newProcessCount=0;
        String line;
        while (matchingProcessCount>0) {
            try {
                if (matchingProcessCount >= 0) {
                    Process p = Runtime.getRuntime().exec(System.getenv("windir") + String.format("\\system32\\" + "taskkill.exe /F /IM %s", name));
                    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    while ((line = input.readLine()) != null) {
                        int g = 99;
                    }
                    input.close();
                }
            } catch (Exception err) {
                Assertions.fail(String.format("Error killing [%s] processes ", name), err);
            }
            newProcessCount = getProcessCount(name);
            if (newProcessCount == matchingProcessCount) {
                Assertions.fail(String.format("Error killing [%s] processes.  Did not kill processes! ", name));
            }
            matchingProcessCount=newProcessCount;
        }
    }

    private int getProcessCount(String name) {
        int count=0;
        List<String[]> processInstanceCount = getProcessList();

        for(String[] line : getProcessList()) {
            if (line[0].equalsIgnoreCase(name)) count++;
        }
        return count;
    }

    private List<String[]> getProcessList() {
        boolean startLogging = false;
        // MAT GET THIS WORKING - Driver executable filesnames.........

        List<String[]> list = new ArrayList<String[]>();
        try {
            String line;
            //Process p = Runtime.getRuntime().exec("ps -e");
            Process p = Runtime.getRuntime().exec (System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                String[] arrayLine = line.split("\\s+");
                if (startLogging) list.add(arrayLine);
                if (line.contains("=======")) startLogging=true;
            }
            input.close();
        } catch (Exception err) {Assertions.fail("Error getting process list: ",err);
        }
        return list;
    }


    private String CallingMethodDetails(StackTraceElement methodBase)
    {
        String methodName="";
        if (methodBase != null)
        {
            methodName = methodBase.getMethodName();
            if (methodName==null) methodName = "<Unknown>";
        }
        return String.format("%s",methodName);
    }

    private void checkIfConnectionIssue(Exception e) throws RuntimeException {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        if (e.getClass()==ConnectException.class) {
            throw new RuntimeException(String.format("Selenium Driver method [%s] called but Selenium WebDriver not connected!",CallingMethodDetails(caller)),e);
        }
    }

}

