package TeamControlium.Controlium;

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


}
