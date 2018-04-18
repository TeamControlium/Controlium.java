package TeamControlium.Controlium;

import TeamControlium.Utilities.General;
import TeamControlium.Utilities.Logger;
import TeamControlium.Utilities.TestData;
import jdk.internal.jline.internal.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import sun.jvm.hotspot.utilities.Interval;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.System.in;

public class SeleniumDriver {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //region From MainDriver.cs

    // CONSTANT FIELDS
    private final String[] SeleniumServerFolder = { "Selenium", "SeleniumServerFolder" };      // Location of the local Selenium Servers (Only required if running locally
    private final String[] ConfigTimeout = { "Selenium", "ElementFindTimeout" };               // Timeout when waiting for Elements to be found (or go invisible) in seconds
    private final String[] ConfigPollingInterval = { "Selenium", "PollInterval" };             // When looping on an event wait, this is the loop interval; trade off between keeping wire traffic down and speed of test
    private final String[] ConfigPageLoadTimeout = { "Selenium", "PageLoadTimeout" };          // Timeout waiting for a page to load
    private static final String[] ConfigBrowser = { "Selenium", "Browser" };                   // Browser used for the UI endpoint we are testing with.

    private final String[] ConfigDevice = { "Selenium", "Device" };                            // Device hosting the UI endpoint we are testing with (If Local, usually Win7)
    private final String[] ConfigHost = { "Selenium", "Host" };                               // Who is hosting the selenium server.  Either localhost (or 127.0.0.1) for locally hosted.  Or a named Cloud provider (IE. BrowserStack or SauceLabs) or Custom.
    private final String[] ConfigHostURI = { "Selenium", "HostURI" };                         // If not locally hosted, this is the full URL or IPaddress:Port to access the Selenium Server
    private final String[] ConfigConnectionTimeout = { "Selenium", "ConnectionTimeout" };     // Timeout when waiting for a response from the Selenium Server (when remote)
    private final String[] SeleniumDebugMode = { "Selenium", "DebugMode" };                   // If yes, Selenium is started in debug mode...
    private final String[] SeleniumLogFilename = { "Selenium", "LogFile" };                   // Path and file for Selenium Log file.  Default is the console window

    private final int defaultTimeout = 60000; // 1 Minute
    private final int defaultPollInterval = 500; // 500mS

    // FIELDS
    private WebDriver _WebDriver;
    //   private WebDriverWait elementFindTimings;
    private ObjectMappingDetails _MappingDetails = new ObjectMappingDetails("//", "Top Level of DOM");

    private Duration _ElementPollInterval;
    private Duration _ElementFindTimeout;   // In the C# implementation we use WebDriverWait.  However, in Java we cant get the timings out so we used descretes and JIT instantiation
    private Duration _PageLoadTimeout;

    // CONSTRUCTORS
    /// <summary>Instantiates SeleniumDriver</summary>
    public SeleniumDriver()
    {
        ConfigureRun();
    }

    /// <summary>Passes basic user credentials around in plain text format</summary>
    static public Credentials _Credentials;

    // ENUMS
    /// <summary>Types of validation for textual compares in tests</summary>
    public enum ValidationCompareTypes
    {
        /// <summary>
        ///  Actual Text must match the Required text exactly.
        /// </summary>
        Exact,
        /// <summary>
        /// Actual text starts with the Required text.
        /// </summary>
        StartsWith,
        /// <summary>
        /// Actual text ends with the Required text.
        /// </summary>
        EndsWith,
        /// <summary>
        /// Actual text contains any instances of the Required text.
        /// </summary>
        Contains,
        /// <summary>
        /// Actual text is not equal to or contains any instances of the Required text.
        /// </summary>
        DoesntContain,
        /// <summary>
        /// Actual text is not equal to Required text.  This does not match if the Actual contains an instance of the Required text.  EG. If IsNot 'hello' would not match on 'hello' but would match on 'hello byebye'.
        /// </summary>
        IsNot
    }


    // PROPERTIES
    /// <summary>
    /// Maximum time to wait, and polling interval, when locating an element using the find logic
    /// </summary>
    public Duration getFindTimeout() { return _ElementFindTimeout; }
    private Duration setFindTimeout(Duration value) { _ElementFindTimeout = value; return _ElementFindTimeout;}
    public Duration getFindPollInterval() { return _ElementPollInterval; }
    private Duration setFindPollInterval(Duration value) { _ElementPollInterval = value; return _ElementPollInterval;}
    /// <summary>
    /// Poll time used when waiting for a FindElement command to match an element
    /// </summary>
    // public Duration getPollInterval() { return _PollInterval; } not possible in Java to split them out

    /// <summary>
    /// Sets/Gets page load timeout
    /// </summary>
    public Duration getPageLoadTimeout() { return _PageLoadTimeout; }
    public Duration setPageLoadTimeout(Duration value) {
        _PageLoadTimeout = value;
        if (_WebDriver != null) {
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Setting Page Load timeout to [%d mS]",getPageLoadTimeout().toMillis());
            _WebDriver.manage().timeouts().pageLoadTimeout(getPageLoadTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        return _PageLoadTimeout;
    }

    public ObjectMappingDetails MappingDetails() { return _MappingDetails; }

    /// <summary>
    /// Retruns the Selenium WebDriver instance we are using.
    /// </summary>
    public WebDriver WebDriver() { return _WebDriver; }
    public WebDriver WebDriver(WebDriver instance) { _WebDriver=instance; return _WebDriver; }

    // METHODS
    /// <summary>If Selenium Webdriver has the capabilty (either built in or with our SSRemoteWebdriver class)
    /// of taking a screenshot, do it.  Resulting JPG is saved to the image folder under the test
    /// suite's results folder.  Image file name is the test ID, with a suffix if passed.</summary>
    /// <remarks>
    /// This assumes webDriver implements ITakesScreen.  Implementation is by SSRemoteWebDriver so webDriver must be an instantation
    /// of that class.  If webDriver does not implement ITakesScreenshot an exception is thrown.</remarks>
    /// <param name="fileName">Optional. If populated and Test Data option [Screenshot, Filename] is not set sets the name of the file</param>
    /// <example>Take a screenshot:
    /// <code lang="C#">
    /// // Take screenshot and save to Results folder /images/MyName.jpg or path given by test data ["Screenshot", "Filepath"]
    /// SeleniumDriver.TakeScreenshot("MyName");
    /// </code></example>
    public String TakeScreenshot() { return TakeScreenshot(null);}
    public String TakeScreenshot(String fileName)
    {
        String filename = null;
        String filepath = null;

        try
        {
            if (WebDriver() != null)
            {
                if (WebDriver() instanceof TakesScreenshot)
                {
                    try
                    {
                        filename = TestData.getItem(String.class,"Screenshot", "Filename");
                    }
                    catch(Exception ex){ }
                    try
                    {
                        filepath = TestData.getItem(String.class,"Screenshot", "Filepath");
                    }
                    catch(Exception ex){ }

                    if (filename == null) filename = (fileName==null) ? "Screenshot" : fileName;

                    //
                    // Ensure filename friendly
                    //
                    //filename = new Regex(string.Format("[{0}]", Regex.Escape(new string(Path.GetInvalidFileNameChars()) + new string(Path.GetInvalidPathChars())))).Replace(filename, "");
                    if (filepath == null)
                    {
                        filename = Paths.get(System.getProperty("user.dir"),"images",filename + ".jpg").toString();
                    }
                    else
                    {
                        filename = Paths.get(filepath, filename + ".jpg").toString();
                    }
                    File screenshot = ((TakesScreenshot)WebDriver()).getScreenshotAs(OutputType.FILE);
                    Logger.WriteLine(Logger.LogLevels.TestInformation, "Screenshot - {0}", filename);
                    FileUtils.copyFile(screenshot, new File(filename));
                    return filename;
                }
                    else
                {
                    throw new UnsupportedOperationException("SeleniumWebDriver does not implement ITakesScreenshot!  Is it RemoteWebDriver?");
                }
            }
            else
            {
                Logger.WriteLine(Logger.LogLevels.TestInformation,"webDriver is null!  Unable to take screenshot.");
            }
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Exception saving screenshot [%s]", (filename==null)?"filename null!":filename);
            Logger.WriteLine(Logger.LogLevels.TestInformation, "> %s", ex);
        }
        return "";
    }

    /// <summary>Quit and close the WebDriver.</summary>
    /// <param name="CallEndTest">If true, tool's EndTest method is called.</param>
    /// <remarks>
    /// At the end of a test some housekeeping may need to be performed.  This performs those tasks;
    /// <list type="number">
    /// <item>If run configuration has option TakeScreenshot (in Debug category) set true Screenshot taken (no suffix)</item>
    /// <item>Selenium connection closed</item></list>
    /// <para/><para/>
    /// It is highly recommended to place the CloseSelenium method call in a finally clause of a test-global try/catch structure (or AfterScenario) to ensure it will always be called.
    /// </remarks>
    /// <example>
    /// <code lang="C#">
    /// try
    /// {
    ///   // test steps
    /// }
    /// catch (Exception ex)
    /// {
    ///   // Test abort and fatal error handling
    /// }
    /// finally
    /// {
    ///   // Lets end the test and have a cleanup
    ///   SeleniumDriver.CloseDriver(true);
    /// }</code></example>
    public void CloseDriver()
    {
        boolean TakeScreenshotOption = false;
        try
        {
            TakeScreenshotOption = General.IsValueTrue(TestData.getItem(String.class,"Debug", "TakeScreenshot"));
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"RunCategory Option [Debug, TakeScreenshot] Exception: {0}", ex);
        }

        if (TakeScreenshotOption)
        {
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Debug.TakeScreenshot = {0} - Taking screenshot...", TakeScreenshotOption);
            TakeScreenshot((new SimpleDateFormat("yy-MM-dd_HH-mm-ssFFF")).format(new Date()));
        }
        else
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Debug.TakeScreenshot = {0} - NOT Taking screenshot...", TakeScreenshotOption);

        if (WebDriver()!=null) WebDriver().quit();
        WebDriver(null);
    }


    public <T extends ControlBase> T SetControl(T NewControl)
    {
        return ControlBase.SetControl(this, NewControl);
    }

    /// <summary>
    /// Returns the title of the current browser window
    /// </summary>
    public String getPageTitle()

    {
        try {
            return WebDriver().getTitle()==null ?"":WebDriver().getTitle();
        } catch (Exception ex) {
            Logger.WriteLine(Logger.LogLevels.Error, "Error getting window title: %s", ex);
            return "";
        }
    }


    /// <summary>
    /// Displays a dialog requesting credentials from the person executing the automated test.
    /// </summary>
    /// <param name="Title">Title of dialog</param>
    /// <returns>Plain text credentials</returns>
    static public Credentials GetCredentials() { return GetCredentials(null);}
    static public Credentials GetCredentials(String Title)
    {
        String defaultTitle = "Site";
        String message = "%s requires authentication.  Enter valid username and password.";

        try (UserCredentialsDialog dialog = new UserCredentialsDialog())
        {
            String Message = String.format(message, Title==null?defaultTitle:Title);
            Message = StringUtils.isEmpty(Message) ? Message : Message.length() <= 100 ? Message : Message.substring(0, 100);
            dialog.Caption(Title==null ? defaultTitle: Title);
            dialog.Message(Message);
            if (dialog.createDialog() == JOptionPane.OK_OPTION)
            {
                // validate credentials against an authentication authority
                // ...
                // If credentials are valid
                // and the user checked the "remember my password" option
                if (dialog.SaveChecked())
                {
                    dialog.ConfirmCredentials(true);
                }
                return setCredentials(dialog.User(), dialog.PasswordAsString());
            }
            else
                return  setCredentials("", "");
        }
    }

    static public Credentials setCredentials(String Username, String Password) {
        if (_Credentials==null) {
            _Credentials = new Credentials(Username, Password);
        }
        else {
            _Credentials.setUsername(Username);
            _Credentials.setPassword(Password);
        }
        return _Credentials;
    }





    // This is kinda specific to an Odin Axe implementation.  However, it does raise the question of how to create
    // multiple token processing 'listeners' in the Token Processor.  Then we could have {Device} and {Browser} tokens
    // and use the generic Utilities token processor to do the work (having a local processor here doing the substitution).
    // Even listeners me thinks....
    private String DoRunConfigTokenSubstitution(String tokenisedString)
    {
        String returnString = tokenisedString;

        returnString = returnString.replace("%Device%", TestDevice.toString());
        returnString = returnString.replace("%Browser%", TestBrowser.toString());
        return returnString;
    }

    private DesiredCapabilities GetCapabilities(String HostName) throws Exception
    {
        DesiredCapabilities caps = new DesiredCapabilities();
        for (Map.Entry<String,? super Object> capability : TestData.getCategory(HostName).entrySet())
        {
            Class<?> capabilityType = capability.getValue().getClass();
            if (capabilityType == String.class)
            {
                String capValue = DoRunConfigTokenSubstitution(capability.getValue().toString());
                Logger.WriteLine(Logger.LogLevels.TestInformation,"Capabilities: [%s] [%s]", capability.getKey(), capValue);
                caps.setCapability(capability.getKey(), capValue);
            }
            else
                throw new Exception(String.format("Capability [%s.%s] is a %s! Must be a string", HostName, capability.getKey(), capabilityType.getTypeName()));
        }
        return caps;
    }
    private void SetupRemoteRun()
    {
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Running Selenium remotely");

        // Set target Device
        SetTestDevice();

        // Do desired capabilities first
        string seleniumHost = TestData.Repository[ConfigHost[0], ConfigHost[1]];
        DesiredCapabilities requiredCapabilities = GetCapabilities(seleniumHost);

        // Now get the Selenium Host details
        Uri uri;
        string seleniumHostURI = TestData.Repository[ConfigHostURI[0], ConfigHostURI[1]];
        if (!Uri.TryCreate(seleniumHostURI, UriKind.Absolute, out uri)) throw new InvalidHostURI(seleniumHostURI);

        // And connection timeout
        int connectionTimeout = TestData.Repository[ConfigConnectionTimeout[0], ConfigConnectionTimeout[1]];

        try
        {
            // And go (We use our own webdriver, inherited from RemoteWebDriver so that we can implement any extra stuff (IE. Screen snapshot)
            webDriver = new ExtendedRemoteWebDriver(uri, requiredCapabilities, connectionTimeout);
        }
        catch (Exception ex)
        {
            throw new SeleniumWebDriverInitError(uri.AbsoluteUri, requiredCapabilities.ToString(), ex);
        }

    }
    private bool IsLocalRun
    {
        get
        {
            string seleniumHost = TestData.Repository[ConfigHost[0], ConfigHost[1]];
            if (string.IsNullOrEmpty(seleniumHost))
            {
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation,"Run Parameter [{0}.{1}] not set.  Default to Local run.",ConfigHost[0], ConfigHost[1]);
                return true;
            }
            return (seleniumHost.ToLower().Equals("localhost") || seleniumHost.ToLower().StartsWith("127.0.0.1"));
        }
    }
    private string CheckAndPreparSeleniumLogFile(string SeleniumDebugFile)
    {
        string seleniumDebugFile = SeleniumDebugFile;

        if (string.IsNullOrWhiteSpace(seleniumDebugFile))
            return null;
        else
        {
            //
            // If path is relative, make it absolute..
            //
            string seleniumDebugFileFolder = (seleniumDebugFile.StartsWith(".")) ?
                    Path.GetDirectoryName(Path.GetFullPath(Path.GetDirectoryName(seleniumDebugFile))) :
                    Path.GetDirectoryName(seleniumDebugFile);
            // File path/name is passed on CMD line so remove all spaces
            string seleniumDebugFileName = Path.GetFileNameWithoutExtension(seleniumDebugFile).Replace(" ", "");
            string seleniumDebugFileExt = Path.GetExtension(seleniumDebugFile);
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, $"SeleniumDebugFile: [{seleniumDebugFile}]");
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, $"seleniumDebugFileFolder: [{seleniumDebugFileFolder}]");
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, $"seleniumDebugFileName: [{seleniumDebugFileName}]");
            Logger.WriteLine(Logger.LogLevels.FrameworkDebug, $"seleniumDebugFileExt: [{seleniumDebugFileExt}]");


            if (string.IsNullOrWhiteSpace(seleniumDebugFileFolder))
                seleniumDebugFileFolder = Environment.CurrentDirectory;
            string PathAndFile = "";
            try
            {
                int TotalPathLength = seleniumDebugFileFolder.Length + seleniumDebugFileName.Length + seleniumDebugFileExt.Length + 2;
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Selenium Debug File - [{0}]", Path.Combine(seleniumDebugFileFolder, (seleniumDebugFileName + seleniumDebugFileExt)));
                Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Selenium Debug File TotalPathLength = {0}", TotalPathLength);
                if (TotalPathLength > 248)
                {
                    //
                    // Max path length is 248, so we need to fiddle....
                    //
                    if ((seleniumDebugFileFolder.Length - seleniumDebugFileName.Length - seleniumDebugFileExt.Length -2) > 248)
                    {
                        // Ok, we cant do it so bomb out.
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "seleniumDebugFileFolder length {0} so cannot fix path length by truncating seleniumDebugFileName", seleniumDebugFileFolder.Length);
                        throw new Exception($"Cannot create screenshot.  Full path [{TotalPathLength}] would have been too long (Max 248 chars)");
                    }
                    else
                    {
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Reducing path length by truncating seleniumDebugFileName (length currently {0})", seleniumDebugFileName.Length);
                        // Ok, we can do it.  Just truncate the TestID the required length...
                        seleniumDebugFileName = seleniumDebugFileName.Substring(0, seleniumDebugFileName.Length - (TotalPathLength - 248));
                        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Reduced to length {0}", seleniumDebugFileName.Length);
                    }
                }

                PathAndFile = Path.Combine(seleniumDebugFileFolder, (seleniumDebugFileName + seleniumDebugFileExt));
                (new FileInfo(PathAndFile)).Directory.Create();
                //File.Delete(Folder);
                StreamWriter sw = File.CreateText(PathAndFile);
                sw.WriteLine("TeamControlium Selenium Debug File");
                sw.Close();
                return PathAndFile;
            }
            catch (Exception ex)
            {
                throw new Exception(string.Format("Error creating Selenium Debug information file ({0}): {1}", PathAndFile, ex.Message), ex.InnerException);
            }

        }

    }
    private void SetupLocalRun()
    {
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Running Selenium locally");
        // Running selenium locally.
        string seleniumFolder = TestData.Repository[SeleniumServerFolder[0], SeleniumServerFolder[1]]??".";
        bool seleniumDebugMode = General.IsValueTrue(TestData.Repository[SeleniumDebugMode[0], SeleniumDebugMode[1]]);
        string seleniumDebugFile;
        TestData.Repository.TryGetItem(SeleniumLogFilename[0], SeleniumLogFilename[1], out seleniumDebugFile);

        // Check the folder exists and chuck a wobbly if it doesnt...
        if (!Directory.Exists(seleniumFolder)) throw new SeleniumFolderError(seleniumFolder);

        try
        {
            if (IsInternetExplorer)
            {
                //
                // See https://code.google.com/p/selenium/issues/detail?id=4403
                //
                InternetExplorerOptions IEO = new InternetExplorerOptions();
                IEO.EnsureCleanSession = true;
                InternetExplorerDriverService service = InternetExplorerDriverService.CreateDefaultService(seleniumFolder);
                service.LoggingLevel = seleniumDebugMode ? InternetExplorerDriverLogLevel.Debug : InternetExplorerDriverLogLevel.Info;
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation,"Selenium Server Log Level: {0}", service.LoggingLevel.ToString());

                string ActualDebugFile = CheckAndPreparSeleniumLogFile(seleniumDebugFile);
                if (!string.IsNullOrWhiteSpace(ActualDebugFile))
                {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation,"Writing Selenium Server Output to: {0}", ActualDebugFile);
                    service.LogFile = ActualDebugFile;
                }
                else
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation,"Writing Selenium Server Output to console");

                // IEO.EnableNativeEvents = false;
                IEO.AddAdditionalCapability("INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS", (bool)true);  // Enabling this as part of #ITSD1-1126 - If any issues come back to request
                Logger.WriteLine(Logger.LogLevels.TestInformation, "IE Browser being used.  Setting INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS active. #ITSD1-1126");
                webDriver = new InternetExplorerDriver(service, IEO);
            }

            if (IsEdge)
            {
                throw new NotImplementedException("Edge has not yet been implemented.  Implement it then....");
                EdgeOptions EO = new EdgeOptions();
                EdgeDriverService service = EdgeDriverService.CreateDefaultService(seleniumFolder);
                service.UseVerboseLogging = seleniumDebugMode;
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Selenium Server Log Level: {0}", service.UseVerboseLogging?"Verbose":"Not verbose");

                string ActualDebugFile = CheckAndPreparSeleniumLogFile(seleniumDebugFile);
                if (!string.IsNullOrWhiteSpace(ActualDebugFile))
                {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to: {0}", ActualDebugFile);
                    // service.LogFile = ActualDebugFile; -- stuck here.  How to define the log file....
                }
                else
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to console");


                EO.PageLoadStrategy = (PageLoadStrategy)EdgePageLoadStrategy.Eager;
                webDriver = new EdgeDriver(seleniumFolder, EO);

            }

            if (IsChrome)
            {
                ChromeOptions options = new ChromeOptions();
                ChromeDriverService service = ChromeDriverService.CreateDefaultService(seleniumFolder);
                service.EnableVerboseLogging = seleniumDebugMode;
                string ActualDebugFile = CheckAndPreparSeleniumLogFile(seleniumDebugFile);
                if (!string.IsNullOrWhiteSpace(ActualDebugFile))
                {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to: {0}", ActualDebugFile);
                    service.LogPath = ActualDebugFile;
                }
                else
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Writing Selenium Server Output to console");
                webDriver = new ChromeDriver(service,options);
            }

            if (webDriver == null) throw new SeleniumWebDriverInitError(seleniumFolder, IsInternetExplorer ? "Internet Explorer" : IsChrome ? "Chrome" : IsEdge ? "Edge" : "Undefined!!");

        }
        catch (Exception ex)
        {
            throw new SeleniumWebDriverInitError(seleniumFolder, IsInternetExplorer ? "Internet Explorer" : IsChrome ? "Chrome" : IsEdge ? "Edge" : "Undefined!!", ex);
        }
    }
    private void SetTimeouts()
    {
        int dummy;

        elementFindTimings = new WebDriverWait(webDriver,TimeSpan.FromMilliseconds((TestData.Repository.TryGetItem(ConfigTimeout[0], ConfigTimeout[1], out dummy)) ? dummy : defaultTimeout));
        elementFindTimings.PollingInterval = TimeSpan.FromMilliseconds((TestData.Repository.TryGetItem(ConfigPollingInterval[0], ConfigPollingInterval[1], out dummy)) ? dummy : defaultPollInterval);

        if (TestData.Repository.TryGetItem(ConfigPageLoadTimeout[0], ConfigPageLoadTimeout[1], out dummy))
        {
            PageLoadTimeout = TimeSpan.FromMilliseconds(dummy);
        }
        else
        {
            // Default page load timeout 30 Seconds
            PageLoadTimeout = TimeSpan.FromMilliseconds(30000);
        }
    }
    private void ConfigureRun()
    {
        // Set browser if it has not already been done
        if (!TestBrowserHasBeenSet) SetTestBrowser();

        if (IsLocalRun)
        {
            //
            // Setup test run based on being local
            //
            SetupLocalRun();
        }
        else
        {
            //
            // It is a remote run
            //
            SetupRemoteRun();
        }

        // Set the timeouts
        SetTimeouts();
    }

    public static void ResetSettings()
    {
        SeleniumDriver.IsChrome = false;
        SeleniumDriver.IsEdge = false;
        SeleniumDriver.IsInternetExplorer = false;
        SeleniumDriver.IsSafari = false;
        SeleniumDriver.TestBrowserHasBeenSet = false;
    }

    //endregion


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //region From SeleniumDriver.cs
    // ENUMS
    /// <summary>Defines possible types of visibility for an element.</summary>
    public enum Visibility
    {
        /// <summary>
        /// Element is Visible - would be able to be seen by a user.
        /// <para/><para/>
        /// Does not take into account text/background colours etc..
        /// </summary>
        Visible,
        /// <summary>
        /// Element is Hidden - would not be able to be seen by a user
        /// </summary>
        Hidden,
        /// <summary>
        /// Elements visibility is unknown.
        /// </summary>
        Unknown
    };

    // PROPERTIES
    /// <summary>Reference to last Try method's exception, if thrown.</summary>
    /// <remarks>When a call to a TryMethod is performed this is set to null.  If the TryMethod throws an exception a boolean false is returned and this property set
    /// to reference it.</remarks>
    private Exception _TryException;
    public Exception TryException() { return _TryException; }  // Try methods set any exception they get so that non-try's can abort test if needed.

    // .
    // .
    // . Need to add rest.....

    //endregion


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //region From JavascriptExecution.cs
    ///
    /// <summary>Injects and executes Javascript in the currently active Selenium browser with no exception thrown. Javascript has return object which is passed back as a string</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="result">Data passed back from the Javascript when executed. Empty string if an exception occured</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryException">Property referencing exception if thrown</seealso>
    /// <seealso cref="ExecuteJavaScriptReturningString(string, object[])"/>
    /// <returns>True if no Selenium exception thrown, or false if exception thrown</returns>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// If, at any stage, an Exception is thrown - either in Selenium or bubbled through from the JavaScript execution - it is logged in
    /// TryException and a false is returned</remarks>
    /// <example>Clear local storage and verify it is empty:
    /// <code lang="C#">
    /// if (SeleniumDriver.TryExecuteJavaScript("window.localStorage.clear(); return window.localStorage.length;",out NumItemsLeft))
    /// {
    ///   if (int.Parse(NumItemsLeft)>0)
    ///   {
    ///     // Oh dear, we have not cleared the store!
    ///   }
    /// }
    /// else
    /// {
    ///   // We had a problem executing the script
    ///   SSDebug.WriteLine("MyTest","Test","JavaScript Error - {0}",SeleniumDriver.TryException.Message);
    /// }</code></example>
//    public boolean TryExecuteJavaScript(String script, ByReference<String> result, Object... args)
//    {
//        try
//        {
//            result.set((String)((JavascriptExecutor)WebDriver()).executeScript(script, args).toString());
//            return true;
//        }
//        catch (Exception ex)
//        {
//            String exceptionString = "";
//            for (Object arg : args)
//            {
//                if (StringUtils.isEmpty(exceptionString))
//                    exceptionString = String.format("TryExecuteJavaScript(\"%s\")-(Args: \"%s\"", script, arg.getClass().getTypeName());
//                else
//                    exceptionString = String.format("%s, \"%s\"", exceptionString, arg.getClass().getTypeName());
//            }
//            if (!StringUtils.isEmpty(exceptionString))
//                exceptionString = String.format("%s)", exceptionString);
//            else
//                exceptionString = String.format("TryExecuteJavaScript(\"%s\")", script);
//            _TryException = ex;
//            result.set("");
//            return false;
//        }
//    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser with no exception thrown. Javascript returns Element object</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="result">Element returned by Javascript</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryException">Property referencing exception if thrown</seealso>
    /// <seealso cref="ExecuteJavaScriptReturningWebElement(string, object[])"/>
    /// <returns>True if no Selenium exception thrown, or false if exception thrown</returns>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// If, at any stage, an Exception is thrown - either in Selenium or bubbled through from the JavaScript execution - it is logged in
    /// TryException and a false is returned</remarks>
    /// <example>Clear local storage and verify it is empty:
    /// <code lang="C#">
    /// if (SeleniumDriver.TryExecuteJavaScript("window.localStorage.clear(); return window.localStorage.length;",out NumItemsLeft))
    /// {
    ///   if (int.Parse(NumItemsLeft)>0)
    ///   {
    ///     // Oh dear, we have not cleared the store!
    ///   }
    /// }
    /// else
    /// {
    ///   // We had a problem executing the script
    ///   SSDebug.WriteLine("MyTest","Test","JavaScript Error - {0}",SeleniumDriver.TryException.Message);
    /// }</code></example>
//    public boolean TryExecuteJavaScript(String script, ByReference<WebElement> result, Object... args)
//    {
//        return TryExecuteJavaScript(script,result,args);
//
//
//        try
//        {
//            result.set((WebElement)((JavascriptExecutor)WebDriver()).executeScript(script, args));
//            return true;
//        }
//        catch (Exception ex)
//        {
//            String exceptionString = "";
//            for (Object arg : args)
//            {
//                if (StringUtils.isEmpty(exceptionString))
//                    exceptionString = String.format("TryExecuteJavaScript(\"%s\")-(Args: \"%s\"", script, (String)arg);
//                else
//                    exceptionString = String.format("%s, \"%s\"", exceptionString, (String)arg);
//            }
//            if (!StringUtils.isEmpty(exceptionString))
//                exceptionString = String.format("%s)", exceptionString);
//            else
//                exceptionString = String.format("TryExecuteJavaScript(\"%s\")", script);
//
//            _TryException = ex;
//            result = null;
//            return false;
//        }
//    }

    public <T> boolean TryExecuteJavaScript(String script, ByReference<T> result, Object... args)
    {
        try
        {
            result.set((T)((JavascriptExecutor)WebDriver()).executeScript(script, args));
            return true;
        }
        catch (Exception ex)
        {
            String exceptionString = "";
            for (Object arg : args)
            {
                if (StringUtils.isEmpty(exceptionString))
                    exceptionString = String.format("TryExecuteJavaScript(\"%s\")-(Args: \"%s\"", script, (String)arg);
                else
                    exceptionString = String.format("%s, \"%s\"", exceptionString, (String)arg);
            }
            if (!StringUtils.isEmpty(exceptionString))
                exceptionString = String.format("%s)", exceptionString);
            else
                exceptionString = String.format("TryExecuteJavaScript(\"%s\")", script);

            _TryException = ex;
            result = null; // hmmm, would this work?
            return false;
        }
    }



    /// <summary>Injects and executes Javascript in the currently active Selenium browser with no exception thrown. Javascript has no return object.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryException">Property referencing exception if thrown</seealso>
    /// <seealso cref="ExecuteJavaScriptNoReturnData(string, object[])"/>
    /// <returns>True if no Selenium exception thrown, or false if exception thrown</returns>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// If, at any stage, an Exception is thrown - either in Selenium or bubbled through from the JavaScript execution - it is logged in
    /// TryException and a false is returned</remarks>
    /// <example>Clear specific item (TelephoneNum) from local storage:
    /// <code lang="C#">
    /// if (SeleniumDriver.TryExecuteJavaScript("window.localStorage.removeItem(\"arguments[0]\");","TelephoneNum"))
    /// {
    ///   // We had a problem executing the script
    ///   SSDebug.WriteLine("MyTest","Test","JavaScript Error - {0}",SeleniumDriver.TryException.Message);
    /// }</code></example>
    public boolean TryExecuteJavaScriptNoReturnData(String script, Object... args)
    {
        return TryExecuteJavaScript(script,new ByReference<>(null),args);
    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryExecuteJavaScript(string, out string,object[])"/>
    /// <returns>Data passed back from the Javascript when executed.</returns>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// It is the responsibility of the tool (or wrapper) to gracefully handle the abort and throw an exception.  The test should have the steps wrapped to enable catching
    /// the abort exception to ensure graceful closure of the test.</remarks>
    /// <example>Clear local storage and verify it is empty:
    /// <code lang="C#">
    /// if (SeleniumDriver.ExecuteJavaScript("window.localStorage.clear(); return window.localStorage.length;")>0)
    /// {
    ///     // Oh dear, we have not cleared the store!
    /// }
    /// </code></example>

    public String ExecuteJavaScriptReturningString(String script, Object... args) throws Exception
    {
        String returnString="";
        ByReference<String> returnStringRef = new ByReference<>(returnString);
        if (!TryExecuteJavaScript(script, returnStringRef, args))
        {
            throw new Exception(_TryException.getMessage(),_TryException);
        }
        return returnString;
    }
    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed. Script must have a return instruction</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryExecuteJavaScript(string, out string,object[])"/>
    /// <returns>Element returned by Javascript</returns>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// It is the responsibility of the tool (or wrapper) to gracefully handle the abort and throw an exception.  The test should have the steps wrapped to enable catching
    /// the abort exception to ensure graceful closure of the test.</remarks>
    /// <example>Clear local storage and verify it is empty:
    /// <code lang="C#">
    /// if (SeleniumDriver.ExecuteJavaScript("window.localStorage.clear(); return window.localStorage.length;")>0)
    /// {
    ///     // Oh dear, we have not cleared the store!
    /// }
    /// </code></example>
    public WebElement ExecuteJavaScriptReturningWebElement(String script, Object... args) throws Exception
    {
        WebElement returnElement = null;
        ByReference<WebElement> WebElementRef = new ByReference<WebElement>(returnElement);

        if (!TryExecuteJavaScript(script, WebElementRef, args))
        {
            String scriptAndArgs = "";
            for (Object arg : args)
            {
                if (StringUtils.isEmpty(scriptAndArgs))
                    scriptAndArgs = String.format("\"%s\" -(Args: \"%s\"", script, (String)arg);
                else
                    scriptAndArgs = String.format("%s, \"%s\"", scriptAndArgs, (String)arg);
            }
            if (!StringUtils.isEmpty(scriptAndArgs))
                scriptAndArgs = String.format("%s)", scriptAndArgs);
            else
                scriptAndArgs = String.format("\"%s\"", script);

            throw new Exception(_TryException.getMessage(), _TryException);
        }
        return returnElement;
    }

    /// <summary>Injects and executes Javascript in the currently active Selenium browser. If Selenium throws an error, test is aborted.</summary>
    /// <param name="script">Javascript that will be injected into the DOM and executed.</param>
    /// <param name="args">Any arguments passed in to the Javascript</param>
    /// <seealso cref="TryExecuteJavaScriptNoReturnData(string,object[])"/>
    /// <remarks>
    /// Selenium and Appium servers may implement how Javascript execution is perfomed and data passed back.  Some implementations may not
    /// support it directly, requiring a local implementation of the IJavaScriptExecutor interface. This method guarantees a unified method
    /// of executing Javascript in automated tests.
    /// <para/><para/>
    /// It is the responsibility of the tool (or wrapper) to gracefully handle the abort and throw an exception.  The test should have the steps wrapped to enable catching
    /// the abort exception to ensure graceful closure of the test.</remarks>
    /// <example>Clear specific item (TelephoneNum) from local storage:
    /// <code lang="C#">
    /// if (SeleniumDriver.ExecuteJavaScript("window.localStorage.removeItem(\"arguments[0]\");","TelephoneNum"))
    /// {
    ///   // We had a problem executing the script
    ///   SSDebug.WriteLine("MyTest","Test","JavaScript Error - {0}",SeleniumDriver.TryException.Message);
    /// }</code></example>
    public void ExecuteJavaScriptNoReturnData(String script, Object... args) throws Exception {
        if (!TryExecuteJavaScriptNoReturnData(script, args))
        {
            String scriptAndArgs = "";
            for (Object arg : args)
            {
                if (StringUtils.isEmpty(scriptAndArgs))
                    scriptAndArgs = String.format("\"%s\" -(Args: \"%s\"", script, (String)arg);
                else
                    scriptAndArgs = String.format("%s, \"%s\"", scriptAndArgs, (String)arg);
            }
            if (!StringUtils.isEmpty(scriptAndArgs))
                scriptAndArgs = String.format("%s)", scriptAndArgs);
            else
                scriptAndArgs = String.format("\"%s\"", script);
            throw new Exception(_TryException.getMessage(), _TryException);
        }
    }

}
    //endregion



