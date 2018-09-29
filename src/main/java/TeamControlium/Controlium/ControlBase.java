package TeamControlium.Controlium;


import org.apache.http.MethodNotSupportedException;

import java.util.Objects;

/// <summary>An abstract Core control all other Core controls inherit from.  Contains all methods and properties that are common to TeamControlium controls.
/// <para/><para/>It should be noted that it is possible to perform an illegal action against a control.  As an example, calling SetText (See <see cref="SetText(FindBy, string, string)"/> or <see cref="SetText(string)"/>) against a TeamControlium button
/// control will result in an exception.  It is the responsibility of the Test code to call only legal actions against a TeamControlium control.  Controls may extend these methods and properties
/// depending on the functionality of the control; for example, a Dropdown control driver may have a SelectItem(Iten identification) method to select a dropdown item.</summary>
public abstract class ControlBase {


    private ObjectMapping _Mapping;
    public ObjectMapping getMapping() {
        return _Mapping;
    }
    protected ObjectMapping setMapping(ObjectMapping mapping) {
        _Mapping=mapping;
        return _Mapping;
    }

    private HTMLElement _RootElement;
    public HTMLElement getRootElement() {
        if (_RootElement == null) {
            throw new RuntimeException(String.format("Control [%s] root element NULL.  Has control been located on the page (IE. SetControl(...)?",getMapping().getFriendlyName()));
        }
        else {
            return _RootElement;
        }
    }
    protected HTMLElement setRootElement(HTMLElement element) {
        _RootElement = element;
        setMapping(element.getMappingDetails());
        return _RootElement;
    }
    protected HTMLElement setRootElement(ObjectMapping mapping) {
        setRootElement(new HTMLElement(mapping));
        return _RootElement;
    }

    private SeleniumDriver _SeleniumDriver;
    public SeleniumDriver getSeleniumDriver() {
        return _SeleniumDriver;
    }
    private SeleniumDriver setSeleniumDriver(SeleniumDriver seleniumDriver) {
        _SeleniumDriver = seleniumDriver;
        return _SeleniumDriver;
    }

    public static void clearCache() {
        throw new RuntimeException("No caching implemented yet!");
    }


    //
    // Sets on a child control with the childs find logic being applied this controls root element.
    // Method can be overridden by control implementations.
    //


}
