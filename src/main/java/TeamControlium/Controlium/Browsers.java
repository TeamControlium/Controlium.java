package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import TeamControlium.Utilities.TestData;

public class Browsers {

    private static final String[] ConfigBrowser = {"Selenium", "Browser"};                   // Browser used for the UI endpoint we are testing with.

    /// <summary>
    /// Supported Browsers.  Browser is set when SeleniumDriver is instantiated.  SeleniumDriver gets the Browser to be used from the run configuration
    /// option "Browser" in category "Selenium".
    /// <para/><para/>
    /// When running remote Selenium the browser/version is used in the Desired Capabilities to request
    /// that browser and version.
    /// <para/><para/>
    /// When running locally only Internet Explorer and Chrome are supported.  Any version can be stipulated but the version will actually be dependant on the installed version on the machine and no check is made.
    /// </summary>
    public enum BrowserType {
        NoneSelected,
        Chrome66,
        Chrome67,
        Chrome68,
        Chrome69,
        Chrome70,
        IE8,
        IE9,
        IE10,
        IE11,
        Edge,
        Safari4,
        Safari5,
        Safari9,
        Safari10,
        Safari11,
    }

    /// <summary>
    /// Browser Selenium script executing against
    /// </summary>
    /// <seealso cref="Browsers">Lists all possible Browser &amp; versions than can be returned.</seealso>
    private static BrowserType _TestBrowser;

    public static BrowserType getBrowserType() {
        return _TestBrowser;
    }

    /// <summary>
    /// Returns true if target browser is any version of Chrome.  Note that this is set when Test Browser is set
    /// </summary>
    private static boolean _IsChrome;

    public static boolean isChrome() {
        return _IsChrome;
    }


    /// <summary>
    /// Returns true if target browser is any version of Internet Explorer
    /// </summary>
    private static boolean _IsInternetExplorer;

    public static boolean isInternetExplorer() {
        return _IsInternetExplorer;
    }

    /// <summary>
    /// Returns true if target browser is any version of Edge
    /// </summary>
    private static boolean _isEdge;

    public static boolean isEdge() {
        return _isEdge;
    }

    /// <summary>
    /// Returns true if target browser is any version of Safari browser
    /// </summary>
    private static boolean _isSafari;

    public static boolean isSafari() {
        return _isSafari;
    }

    private static boolean testBrowserHasBeenSet = false;

    /// <summary>
    /// Sets the Browser being used for testing.  Expects RunSetting "Selenium", "Browser" to be set
    /// </summary>
    public static void SetTestBrowser() {
        SetTestBrowser(null);
    }
    public static void SetTestBrowser(String browser) {
        _IsChrome = false;
        _IsInternetExplorer = false;
        _isSafari = false;
        _isEdge = false;


        try {
            String browserFromTestData = TestData.getItem(String.class, ConfigBrowser[0], ConfigBrowser[1]);
            if (!browserFromTestData.isEmpty()) {
                if (browser!=null && !browser.isEmpty()) {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, String.format("Browser set in Test Data.  Overriding passed device with [%s] (Test data [%s.%s])",browserFromTestData, ConfigBrowser[0], ConfigBrowser[1]));
                }
                browser = browserFromTestData;
            }
        } catch (Exception e) {
            if (browser==null || browser.isEmpty()) {
                Logger.WriteLine(Logger.LogLevels.Error, String.format("Cannot get Browser type setting from test data: [%s],[%s]. Defaulting to Chrome", ConfigBrowser[0], ConfigBrowser[1], e.getMessage()));
                throw new RuntimeException(String.format("Error getting setting [%s],[%s].", ConfigBrowser[0], ConfigBrowser[1]), e);
            }
        }

        if (browser == null || browser.isEmpty()) {
            Logger.WriteLine(Logger.LogLevels.Error, String.format("Browser not defined and empty Browser type in test data: [%s],[%s].", ConfigBrowser[0], ConfigBrowser[1]));
            throw new RuntimeException(String.format("Browser not defined and empty Browser type in test data: [%s],[%s].", ConfigBrowser[0], ConfigBrowser[1]));
        }

        switch (browser.toLowerCase().replace(" ", "").replace("(", "").replace(")", "")) {
            case "chrome66":
                _TestBrowser = BrowserType.Chrome66;
                _IsChrome = true;
                break;
            case "chrome67":
                _TestBrowser = BrowserType.Chrome67;
                _IsChrome = true;
                break;
            case "chrome68":
                _TestBrowser = BrowserType.Chrome68;
                _IsChrome = true;
                break;
            case "chrome69":
                _TestBrowser = BrowserType.Chrome69;
                _IsChrome = true;
                break;
            case "chrome":
            case "chrome70":
                _TestBrowser = BrowserType.Chrome70;
                _IsChrome = true;
                break;
            case "ie8":
            case "internetexplorer8":
                _TestBrowser = BrowserType.IE8;
                _IsInternetExplorer = true;
                break;
            case "ie9":
            case "internetexplorer9":
                _TestBrowser = BrowserType.IE9;
                _IsInternetExplorer = true;
                break;
            case "ie10":
            case "internetexplorer10":
                _TestBrowser = BrowserType.IE10;
                _IsInternetExplorer = true;
                break;
            case "ie11":
            case "internetexplorer11":
            case "ie":                                     // Default if no IE version given.
            case "internetexplorer":                       //
                _TestBrowser = BrowserType.IE11;
                _IsInternetExplorer = true;
                break;
            case "edge":
                _TestBrowser = BrowserType.Edge;
                _isEdge = true;
                break;
            case "safari4":
                _TestBrowser = BrowserType.Safari4;
                _isSafari = true;
                break;
            case "safari5":
            case "safari":                              // Default if no Safari version given as that is what is on Windows
                _TestBrowser = BrowserType.Safari5;
                _isSafari = true;
                break;
            case "safari9":
                _TestBrowser = BrowserType.Safari9;
                _isSafari = true;
                break;
            case "safari10":
                _TestBrowser = BrowserType.Safari10;
                _isSafari = true;
                break;
            case "safari11":
                _TestBrowser = BrowserType.Safari11;
                _isSafari = true;
                break;
            default:
                //    throw new UnsupportedBrowser(browser);
                Logger.WriteLine(Logger.LogLevels.Error, String.format("Unsupported Browser: [%s]", browser));
                throw new RuntimeException(String.format("Unsupported Browser: [%s]", browser));

        }
        testBrowserHasBeenSet = true;
    }
}

