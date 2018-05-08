package TeamControlium.Controlium;

import java.lang.*;
import TeamControlium.Utilities.Logger;
import javafx.util.Pair;
import org.openqa.selenium.By;

public class ObjectMapping {

    public enum ByType { Id, Class, Css, LinkText, Name, Partial, Tag, XPath, Unknown };


    private ByType _mappingType;
    private String _findLogic;          // This is/was the find logic wanting to be used to identify the element with parameters resolved if any
    private String _findLogicOriginal;  // This is/was the find logic wanting to be used to identify the element
    private String _actualFindLogic;    // Actual find logic used.
    private String _friendlyName;       // Human readable name of the element with parameters resolved if any; for easy interpretations etc
    private String _friendlyNameOriginal;       // Original friendly name with parameters unresolved if any
    private By _seleniumBy;


    public ObjectMapping(String findLogic)
    {
        InitObjectMap(findLogic,null);
    }

    public ObjectMapping(String findLogic, String friendlyName)
    {
        InitObjectMap(findLogic,friendlyName);
    }

    private void InitObjectMap(String findLogic, String friendlyName)
    {
        this._findLogicOriginal = findLogic;
        _findLogic = null;
        _friendlyNameOriginal = (friendlyName==null) ? findLogic : friendlyName;
        _friendlyName = null;
        _seleniumBy = processFindLogic(findLogic);
    }




    public ByType getMappingType() { return _mappingType; }

    public String getFindLogic() { return _findLogicOriginal;}
    public String setFindLogic(String findLogic) { _findLogicOriginal = findLogic; return _findLogicOriginal; }

    public String getFindLogicUsed() { return _actualFindLogic;}

    public By getSeleniumBy() { return _seleniumBy; }


    public String getFriendlyName() {
        return (_friendlyName==null) ?
                (_findLogic==null) ?
                        "No name or find logic!" :
                        _findLogic :
                _friendlyName;
    }


    public ObjectMapping copy() {
        try {
            return (ObjectMapping) this.clone();
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Exception cloning current Object Map (%s): %s",getFriendlyName(),e);
            return null;
        }
    }

    public ObjectMapping ResolveParameters(String... params) {
        String newFindLogic = null;
        String newFriendlyName = null;

        try {
            if (_findLogicOriginal==null) {
                Logger.WriteLine(Logger.LogLevels.Error,"Cannot resolve parameters, find logic is null!");
                throw new RuntimeException("Cannot resolve parameters, find logic is null! See log.");
            } else {
                newFindLogic = String.format(_findLogicOriginal,params);
            }
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error resolving find logic parameters!");
            throw new RuntimeException("Error resolving find logic parameters! See log.");
        }

        try {
            if (_friendlyNameOriginal!=null) {
                newFriendlyName = String.format(_friendlyNameOriginal,params);
            }
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error resolving friendly name parameters!");
            newFriendlyName = newFindLogic;
        }

        _findLogic = newFindLogic;
        _friendlyName = newFriendlyName;

        _seleniumBy = processFindLogic(_findLogic);

        return this;
    }

    private By processFindLogic(String property) {
        By returnValue;
        Pair<String,String> findLogicNameValue;

        //
        // Get the find type and logic
        //
        if (property==null) {
            return null;
        } else {
            if (property.contains("=")) {
                String[] nameValue = property.split("=", 2);
                findLogicNameValue = new Pair<String, String>(nameValue[0], nameValue[1]);
            } else {
                // Default to xpath....
                findLogicNameValue = new Pair<String, String>("xpath", property);
            }

            switch (findLogicNameValue.getKey()) {
                case "id":
                    returnValue = By.id(findLogicNameValue.getValue());
                    _mappingType = ByType.Id;
                    break;
                case "class":
                    returnValue = By.className(findLogicNameValue.getValue());
                    _mappingType = ByType.Class;
                    break;
                case "css":
                    returnValue = By.cssSelector(findLogicNameValue.getValue());
                    _mappingType = ByType.Css;
                    break;
                case "linktext":
                    returnValue = By.linkText(findLogicNameValue.getValue());
                    _mappingType = ByType.LinkText;
                    break;
                case "name":
                    returnValue = By.name(findLogicNameValue.getValue());
                    _mappingType = ByType.Name;
                    break;
                case "partial":
                    returnValue = By.partialLinkText(findLogicNameValue.getValue());
                    _mappingType = ByType.Partial;
                    break;
                case "tag":
                    returnValue = By.tagName(findLogicNameValue.getValue());
                    _mappingType = ByType.Tag;
                    break;
                case "xpath":
                    returnValue = By.xpath(findLogicNameValue.getValue());
                    _mappingType = ByType.XPath;
                    break;
                default:
                    returnValue = By.xpath(property);
                    _mappingType = ByType.XPath;
                    break;
            }
            return returnValue;
        }
    }
}
