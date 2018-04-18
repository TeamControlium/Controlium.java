package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import com.sun.tools.javac.util.Pair;
import org.openqa.selenium.By;

public class ObjectMappingDetails {
    public enum ByType { Id, Class, Css, LinkText, Name, Partial, Tag, XPath, Unknown };

    /// <summary>
    /// Find logic type of mapped object
    /// </summary>
    /// <example><code language="C#" title="Using XPath"> mappedObject.FindLogic = ".\div[@class='MyClass'];"</code>
    /// </example>
    public ByType getFindType() { return _FindType; }
    private ByType setFindType(ByType value) { _FindType = value; return value;}
    private ByType _FindType;

    /// <summary>
    /// Find logic of mapped object
    /// </summary>
    /// <example><code language="C#" title="Using XPath"> mappedObject.FindLogic = ".\div[@class='MyClass'];"</code>
    /// </example>
    public String getFindLogic() { return _FindLogic;}
    public String setFindLogic(String value) { _FindLogic=value; return value;}
    private String _FindLogic;

    /// <summary>
    /// Actual Find logic of mapped object
    /// </summary>
    /// <example><code language="C#" title="Using XPath"> mappedObject.FindLogic = ".\div[@class='MyClass'];"</code>
    /// </example>
    /// <remarks>Find logic used may not be actual Find Logic defined. This may occure when multiple
    /// elements are found and an indexed one is used.  The actual find logic can be used to locate the indexed
    /// element if wanted to traverse up the find tree.
    /// </remarks>
    public String getFindLogicUsed() { return _FindLogicUsed;}
    public String setFindLogicUsed(String value) { _FindLogicUsed=value; return value;}
    private String _FindLogicUsed;

    /// <summary>
    /// Friendly Name of object defined by find logic
    /// </summary>
    /// <remarks>
    /// Objects are defined by their friendly names in object-related errors reported to the user rather than
    /// only by their find logic.
    /// </remarks>
    public String getFriendlyName() { return _FriendlyName;}
    public String setFriendlyName(String value) { _FriendlyName=value; return value;}
    private String _FriendlyName;

    public By getSeleniumBy() { return _SeleniumBy; }
    private By setSeleniumBy(By value) { _SeleniumBy = value; return _SeleniumBy;}
    private By _SeleniumBy;

    /// <summary>
    /// If Findlogic has parameters (ResolveParameters method called with parameters resolved) store original FindLogic here for future use).
    /// </summary>
    private String FindLogicWithParameters;

    /// <summary>
    /// If FriendlyName has parameters (ResolveParameters method called with parameters resolved) store original FriendlyName here for future use).
    /// </summary>
    private String FriendlyNameWithParameters;


    public ObjectMappingDetails Copy() throws Exception {
        return (ObjectMappingDetails)clone();
    }
    /// <summary>
    /// Defines an unspecified object.  Type and Name are empty strings and Logic is null
    /// </summary>
    public ObjectMappingDetails()
    {
        setFindType(ByType.Unknown);
        setFindLogic(null);
        FindLogicWithParameters = null;
        setFriendlyName("");
        FriendlyNameWithParameters = null;
    }

    /// <summary>
    /// Defines an object using string-defined logic (default xpath) and with a friendly name of the Find logic
    /// </summary>
    /// <example>
    /// <code language="C#">
    /// // myObject points to HTML link with text View PDF Documents
    /// MappedObject myObject = new MappedObject("linktext=View PDF Documents");
    /// </code></example>
    /// <param name="FindLogic">Logic to use.  If no type defaults to XPath</param>
    public ObjectMappingDetails(String FindLogic)
    {
        setFindLogic(FindLogic);
        FindLogicWithParameters = null;
        setFriendlyName(FindLogic);
        FriendlyNameWithParameters = null;
        setSeleniumBy(ProcessFindLogic(FindLogic));
    }

    /// <summary>
    /// Defines an object using string-defined logic (default xpath) with passed Freindly name
    /// </summary>
    /// <example>
    /// <code language="C#">
    /// // myObject points to HTML link with text View PDF Documents
    /// MappedObject myObject = new MappedObject("linktext=View PDF Documents","PDF Document link");
    /// </code></example>
    /// <param name="FindLogic">Logic to use.  If no type defaults to XPath</param>
    /// <param name="FriendlyName">Text to use in logs and error reporting</param>
    public ObjectMappingDetails(String FindLogic, String FriendlyName)
    {
        setFindLogic(FindLogic);
        FindLogicWithParameters = null;
        setFriendlyName(FriendlyName);
        FriendlyNameWithParameters = null;
        setSeleniumBy(ProcessFindLogic(FindLogic));
    }

    /// <summary>
    /// Resolves parameters if used
    /// </summary>
    /// <param name="args">arguments for resolution</param>
    /// <returns>Instance</returns>
    public ObjectMappingDetails ResolveParameters(String... args)
    {
        String newFindLogic = null;
        String newFriendlyName = null;
        try
        {
            newFindLogic = String.format(FindLogicWithParameters==null?getFindLogic():FindLogicWithParameters, args);
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Error resolving parameters in find logic [%s]: %s", (getFindLogic()==null) ? "<Null!>":getFindLogic(),ex);
        }

        try
        {
            newFriendlyName = String.format(FriendlyNameWithParameters==null?getFriendlyName():FriendlyNameWithParameters, args);
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Error resolving parameters in find logic [{0}]: {1}", (getFriendlyName()==null) ? "<Null!>":getFriendlyName(),ex);
        }
        if (newFindLogic != null)
        {
            if (FindLogicWithParameters == null)
                FindLogicWithParameters = getFindLogic();
            setFindLogic(newFindLogic);
            setSeleniumBy(ProcessFindLogic(getFindLogic()));
        }
        if (newFriendlyName != null)
        {
            if (FriendlyNameWithParameters == null)
                FriendlyNameWithParameters = getFriendlyName();
            setFriendlyName(newFriendlyName);
        }
        setSeleniumBy(ProcessFindLogic(getFindLogic()));
        return this;
    }

    private By ProcessFindLogic(String property)
    {
        By returnValue;
        Pair<String, String> ByValue;

        if (property.contains("="))
            ByValue = new Pair<String, String>(property.split("=")[0].trim().toLowerCase(), property.substring(property.indexOf("=") + 1));
        else
            ByValue = new Pair<String, String>("xpath", property);

        switch (ByValue.fst)
        {
            case "id": returnValue = By.id(ByValue.snd); setFindType(ByType.Id);  break;
            case "class": returnValue = By.className(ByValue.snd); setFindType(ByType.Class); break;
            case "css": returnValue = By.cssSelector(ByValue.snd); setFindType(ByType.Css); break;
            case "linktext": returnValue = By.linkText(ByValue.snd); setFindType(ByType.LinkText); break;
            case "name": returnValue = By.name(ByValue.snd); setFindType(ByType.Name); break;
            case "partial": returnValue = By.partialLinkText(ByValue.snd); setFindType(ByType.Partial); break;
            case "tag": returnValue = By.tagName(ByValue.snd); setFindType(ByType.Tag); break;
            case "xpath": returnValue = By.xpath(ByValue.snd); setFindType(ByType.XPath); break;
            default: returnValue = By.xpath(property); setFindType(ByType.XPath); break;
        }
        return returnValue;
    }
}

