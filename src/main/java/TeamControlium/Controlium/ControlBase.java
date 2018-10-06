package TeamControlium.Controlium;


import TeamControlium.Utilities.Logger;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.MethodNotSupportedException;

import java.util.List;
import java.util.Objects;



/// <summary>An abstract Core control all other Core controls inherit from.  Contains all methods and properties that are common to TeamControlium controls.
/// <para/><para/>It should be noted that it is possible to perform an illegal action against a control.  As an example, calling SetText (See <see cref="SetText(FindBy, string, string)"/> or <see cref="SetText(string)"/>) against a TeamControlium button
/// control will result in an exception.  It is the responsibility of the Test code to call only legal actions against a TeamControlium control.  Controls may extend these methods and properties
/// depending on the functionality of the control; for example, a Dropdown control driver may have a SelectItem(Iten identification) method to select a dropdown item.</summary>
public abstract class ControlBase {

    protected ObjectMapping _Mapping;
    public ObjectMapping getMapping() {
        return _Mapping;
    }
    protected ObjectMapping setMapping(ObjectMapping mapping) {
        _Mapping = mapping;
        return _Mapping;
    }

    protected HTMLElement _RootElement;

    public HTMLElement getRootElement() {
        if (_RootElement == null) {
            throw new RuntimeException(String.format("Control [%s] root element NULL.  Has control been located on the page (IE. SetControl(...)?", getMapping().getFriendlyName()));
        } else {
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

    public SeleniumDriver setSeleniumDriver(SeleniumDriver seleniumDriver) {
        _SeleniumDriver = seleniumDriver;
        return _SeleniumDriver;
    }

    private ControlBase _parentControl;

    public ControlBase getParentControl() {
        return _parentControl;
    }

    public ControlBase setParentControl(ControlBase parentControl) {
        _parentControl = parentControl;
        return _parentControl;
    }


    public static void clearCache() {
        throw new RuntimeException("No caching implemented yet!");
    }


    //
    // Sets on a child control with the child's find logic being applied to this controls root element.
    // Method can be overridden by control implementations.
    //
    public <T extends ControlBase> T setControl(T newControl) {
        return setControl(this, newControl);
    }

    public static <T extends ControlBase> T setControl(ControlBase parentControl, T newControl) {
        return setControl(parentControl.getSeleniumDriver(), parentControl, newControl);
    }
    public static <T extends ControlBase> T setControl(SeleniumDriver seleniumDriver, T newControl) {
        return setControl(seleniumDriver, null, newControl);
    }
    public static <T extends ControlBase> T setControl(SeleniumDriver seleniumDriver, ControlBase parentControl, T newControl) {
        if (newControl == null) throw new RuntimeException("newControl Null!");

        StopWatch timeWaited = StopWatch.createStarted();

        try {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Setting on Control [%s] from Parent [%s]",
                    newControl.getMapping() == null ? "No mapping logic!" : newControl.getMapping().getFriendlyName(),
                    parentControl == null ? "No parent Control - So Top Level control" : parentControl.getMapping() == null ? "No mapping logic!" : parentControl.getMapping().getFriendlyName());

            //
            // Check if ParentControl has become stale (has been redrawn).  If so, refresh it (force a new findElement on it).  Note that this
            // will effectively ripple up to the top level
            //
            if (parentControl != null && parentControl.isStale()) {
                Logger.WriteLine(Logger.LogLevels.TestInformation, "Parent control is stale. Refreshing");
                parentControl.setRootElement((HTMLElement) null);
                ControlBase refreshedParentControl = ControlBase.setControl(parentControl.getSeleniumDriver(), parentControl.getParentControl(), parentControl);
                parentControl = refreshedParentControl;
            }

            //
            // We may just be wrapping an Element in a Control that has already been found.  In which case, dont bother
            // to do a find for it....
            //
            if (newControl._RootElement == null || !newControl.getRootElement().isBoundToAWebElement()) {
                Logger.WriteLine(Logger.LogLevels.TestDebug, "New control root element is null or unbound to a Selenium element.  So finding element");

                //
                // If the control is top level we have to use the driver find.  If not then we apply the find from the root of the parent control
                //
                HTMLElement element;
                if (parentControl==null) {
                    element = seleniumDriver.findElement(newControl.getMapping());
                }
                else
                {
                    element = parentControl.getRootElement().findElement(newControl.getMapping());
                }
                newControl.setRootElement(element);
            }

            //
            // Populate new Control object.
            //
            newControl.setSeleniumDriver(seleniumDriver);
            newControl.setParentControl(parentControl); // This may be null.  So, new control is top level....

            //
            // THIS IS WHERE THE CACHE CONTROL WILL BE DONE....  No NEED FOR MVP. WE ARE SOOOO AGILE! lol
            // For now, we will just assume Cache Miss
            //
            newControl.controlBeingSet(true);

            return newControl;
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Error setting on control: %s", e.getMessage());
            throw new RuntimeException(String.format("Error setting on control: %s", e.getMessage()));
        }
    }

    public static <T extends ControlBase> boolean controlExists(ControlBase parentControl,T control) { return parentControl.elementExists(control.getMapping());}
    public static <T extends ControlBase> boolean controlExists(SeleniumDriver seleniumDriver, T control) { return seleniumDriver.findElementOrNull(control.getMapping())!=null;}
    public static <T extends ControlBase> boolean controlExists(SeleniumDriver seleniumDriver, ControlBase parentControl, T control) { return seleniumDriver.findElement(parentControl.getMapping()).findElementOrNull(control.getMapping())!=null;}

    public <T extends ControlBase> boolean controlExists(T control) {return this.elementExists(control.getMapping());}
    //
    // All Controls must implement a ControlBeingSet.  This is called when the Control is set upon (IE. Find logic applied and bound to a Selenium element).  It really
    // will become useful when caching is implemented.  It is used by a Control to do stuff when located in the Dom - IE. A dropdown control may click on it whenever
    // SET on to expose the dropdown...
    //
    protected abstract void controlBeingSet(boolean isFirstSetting);

    public boolean isStale() {
        try
        {
            //
            // We do a dummy action to force Selenium to report any stagnantation....
            //
            getRootElement().isElementEnabled();
            return false;
        }
        catch (Exception ex)
        {
            //
            // TODO: Might need to analyse the error to see if it is actually a Stale element exception.....
            //
            Logger.LogException(ex);
            return true;
        }
    }

    public boolean elementExists(ObjectMapping mapping) {
        return getRootElement().findElementOrNull(mapping)!=null;
    }

    public void clearElement(ObjectMapping mapping) {
        findElement(mapping).clear();
    }
    public void clearElement() {
        getRootElement().clear();
    }

    //
    // Click root element of Control.  Most times controls will override this and click on the most appropriate
    // element in the control
    //
    public void click() {
        getRootElement().click();
    }
    public void click(ObjectMapping mapping) {
        findElement(mapping).click();
    }

    public void clickIfExists(ObjectMapping mapping) {
        HTMLElement element;
        if ((element = getRootElement().findElementOrNull(mapping))==null) {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "[%s] didnt find any match.  No click.",mapping==null?"Null mapping!":mapping.getFriendlyName());
        }
        else {
            element.click();
        }
    }

    public String getAttribute(ObjectMapping mapping, String attributeName) {
        HTMLElement element = findElement(mapping);
        try {
            return element.getAttribute(attributeName);
        }
        catch (Exception e) {
            return "";
        }
    }
    public String getAttribute(String attributeName) {
        try {
            return getRootElement().getAttribute(attributeName);
        }
        catch (Exception e) {
            return "";
        }
    }

    public boolean hasAttribute(ObjectMapping mapping, String attributeName) {
        HTMLElement element = findElement(mapping);
        try {
            return element.hasAttribute(attributeName);
        }
        catch (Exception e) {
            return false;
        }
    }
    public boolean hasAttribute(String attributeName) {
        try {
            return getRootElement().hasAttribute(attributeName);
        }
        catch (Exception e) {
            return false;
        }
    }

    public String getText(ObjectMapping mapping) {
        HTMLElement element = findElement(mapping);
        try {
            return element.getText();
        }
        catch (Exception e) {
            return "";
        }
    }
    public String getText() {
        try {
            return getRootElement().getText();
        }
        catch (Exception e) {
            return "";
        }
    }

    public void setText(ObjectMapping mapping, String text) {
        HTMLElement element = findElement(mapping);
        element.setText(text);
    }
    public void setText(String text) {
        getRootElement().setText(text);
    }


    public HTMLElement findElement(ObjectMapping mapping) {
        if (getRootElement()==null) {
            throw new RuntimeException(String.format("Control [%s] root element is null.  Has the Control been Set (SetControl) yet?",getMapping()==null?"Unknown":getMapping().getFriendlyName()==null?"Unknown":getMapping().getFriendlyName()));
        }
        return getRootElement().findElement(mapping);
    }

    public List<HTMLElement> findAllElements(ObjectMapping mapping) {
        if (getRootElement()==null) {
            throw new RuntimeException(String.format("Control [%s] root element is null.  Has the Control been Set (SetControl) yet?",getMapping()==null?"Unknown":getMapping().getFriendlyName()==null?"Unknown":getMapping().getFriendlyName()));
        }
        return getRootElement().findAllElements(mapping);
    }

}
