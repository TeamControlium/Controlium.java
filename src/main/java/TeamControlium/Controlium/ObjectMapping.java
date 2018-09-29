package TeamControlium.Controlium;

import java.lang.*;
import TeamControlium.Utilities.Logger;
import javafx.util.Pair;
import org.openqa.selenium.By;

public class ObjectMapping {

    public enum ByType { Id, Class, Css, LinkText, Name, Partial, Tag, XPath, Unknown };


    private ByType _mappingType;
    private String _findLogicOriginal;  // This is/was the find logic wanting to be used to identify the element
    private String _findLogicActual;    // Actual find logic used.
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
        _friendlyNameOriginal = (friendlyName==null) ? findLogic : friendlyName;
        _friendlyName = _friendlyNameOriginal;
    }


    public ByType getMappingType() { if (_mappingType==null) processFindLogic((_findLogicActual==null)?_findLogicOriginal:_findLogicActual); return _mappingType; }



    //
    // Original Find Logic is set by the test script; IE. "div[@id='hello']"
    //
    // Actual Find Logic is used by Controlium to identify the element.  In most cases it will be the same,
    // but in some instances it may be different; IE. Test may have used FindElements to find a range of
    // elements using the find logic, the actual element found will therefore have an array indexer added.
    // IE.
    //   List<HTMLElement> elements = FindElements(new ObjectMapping("div[@id='hello']");
    //   String actualFindLogic = elements[2].getActualFindLogic();
    //
    //   actualFindLogic is "(div[@id='hello'])[3]"  - NOTE the indexer is 3 not 2.  Selenium uses a 1-based indexing NOT 0!
    //
    //   When getActualFindLogic() is called the find logic cannot be changed as it may have been used in identification
    //   of an element; changing would prevent replay which would be an issue with Stalinity prevention.
    //


    public String getOriginalFindLogic() { return _findLogicOriginal;}
    public String setOriginalFindLogic(String findLogic)
    {
        if (_findLogicActual!=null) {
            Logger.WriteLine(Logger.LogLevels.Error,"[%s]: Cannot change Find Logic ([%s]) after Actual Find Logic used! ",getFriendlyName(),_findLogicActual);
            throw new RuntimeException(String.format("[%s]: Cannot change Find Logic ([%s]) after Actual Find Logic used! ",getFriendlyName(),_findLogicActual));
        }
        _findLogicOriginal = findLogic; return getOriginalFindLogic();}
    public String getActualFindLogic() {
        if (_findLogicActual==null) {
            _findLogicActual=_findLogicOriginal;
        }
        return _findLogicActual;
    }
    public String setActualFindLogic(String findLogic) {
        if (_findLogicActual!=null) {
            Logger.WriteLine(Logger.LogLevels.Error,"[%s]: Cannot change Actual Find Logic ([%s]) after Actual Find Logic used! ",getFriendlyName(),_findLogicActual);
            throw new RuntimeException(String.format("[%s]: Cannot change Actual Find Logic ([%s]) after Actual Find Logic used! ",getFriendlyName(),_findLogicActual));
        }
        _findLogicActual = findLogic; return getActualFindLogic();
    }


    public By getSeleniumBy() { return processFindLogic((getActualFindLogic())); }


    public String getFriendlyName() {
        return (_friendlyName==null) ?
                (_findLogicOriginal==null) ?
                        (_findLogicActual==null) ?
                        "No name or find logic!" :
                                _findLogicActual :
                        _findLogicOriginal :
                _friendlyName;
    }


    public ObjectMapping copy() {
        try {
            //
            ObjectMapping clone = new ObjectMapping(_findLogicActual,_friendlyName);
            return clone;
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
                newFindLogic = String.format(_findLogicOriginal,(Object[])params);
            }
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error resolving find logic parameters!");
            throw new RuntimeException("Error resolving find logic parameters! See log.");
        }

        try {
            if (_friendlyNameOriginal!=null) {
                newFriendlyName = String.format(_friendlyNameOriginal,(Object[])params);
            }
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error resolving friendly name parameters!");
            newFriendlyName = newFindLogic;
        }

        _findLogicActual = newFindLogic;
        _friendlyName = newFriendlyName;

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
