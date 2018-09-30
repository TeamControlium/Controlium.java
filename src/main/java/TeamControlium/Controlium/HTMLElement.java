package TeamControlium.Controlium;

import TeamControlium.Controlium.Exception.InvalidElementState;
import TeamControlium.Utilities.Logger;
import org.apache.commons.lang3.time.StopWatch;
//import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HTMLElement {

    private ObjectMapping _mappingDetails;
    private Object _webElement;
    private Object _parentElementOrDriver;

    private long elementDefaultChangeDeltaTimemS = 200; // Time to wait between samples when checking if an element is changing (IE. moving on screen)

    private enum StabilityType {
        HEIGHT("Height"), WIDTH("Width"), SIZE("Size"), POSITION("Position");
        private String asString;

        private StabilityType(String stringRepresentation) {
            this.asString = stringRepresentation;
        }

        @Override
        public String toString() {
            return asString;
        }
    }


    // Constructors

    //
    // Create an instance of an Element.  It has no mapping, no Parent and has not yet been located in a DOM
    //
    //
    public HTMLElement() {
    }

    public HTMLElement(Object parent, Object underlyingWebElement, ObjectMapping mapping) {
        setParentOfThisElement(parent);
        setUnderlyingWebElement(underlyingWebElement,mapping);
    }

    //
    // Create an instance of an Element.  The mapping is set but it has no Parent and has not yet been located in a DOM
    //
    //
    public HTMLElement(ObjectMapping mapping) {
        setParentOfThisElement(null);
        setMappingDetails(mapping);
        setUnderlyingWebElement(null);
    }


    // PROPERTIES
    public String getFriendlyName() {
        ObjectMapping objectMapping = getMappingDetails();

        if (objectMapping == null) {
            return "No mapping details for element!";
        } else {
            return objectMapping.getFriendlyName();
        }
    }

    public Object getUnderlyingWebElement() {
        return _webElement;
    }

    public Object setUnderlyingWebElement(Object webElement) {
        return setUnderlyingWebElement(webElement,null);
    }

    public Object setUnderlyingWebElement(Object webElement,ObjectMapping mapping) {
        _webElement = webElement;
        _mappingDetails = mapping==null?new ObjectMapping(null, String.format("Wired directly to underlying UI driver WebElement [%s]", webElement.getClass().getName())):mapping;
        return _webElement;
    } // Manually wiring to WebElement so we have no mapping details!

    public ObjectMapping getMappingDetails() {
        return _mappingDetails;
    }

    public ObjectMapping setMappingDetails(ObjectMapping mappingDetails) {
        if ((mappingDetails != null) && (_webElement != null)) {
            Logger.WriteLine(Logger.LogLevels.Error, "Trying to set Mapping Details after Element ([%s]) has already been bound (found by Selenium).  So cannot change mapping details.", getFriendlyName());
            throw new RuntimeException("Cannot set mapping logic after element has been bound.  See log!");
        }
        _mappingDetails = mappingDetails;
        return _mappingDetails;
    }

    // Returns the instance of SeleniumDriver this Element is part of
    public SeleniumDriver getSeleniumDriver() {
        if (getParentOfThisElement() == null) {
            Logger.WriteLine(Logger.LogLevels.Error, "Trying to get parent Element of [%s]. However, element has no Parent (it has not yet be found!)", getFriendlyName());
            throw new RuntimeException("Cannot get an instance of the Selenium Driver as this element (or a Parent of) does not have a Parent!");
        }

        // Itterate to the top level element - which is SeleniumDriver type
        if (getParentOfThisElement().getClass() == SeleniumDriver.class) {
            return (SeleniumDriver) getParentOfThisElement();
        } else {
            return ((HTMLElement) getParentOfThisElement()).getSeleniumDriver();
        }
    }

    public Object getParentOfThisElement() {
        return _parentElementOrDriver;
    }

    public Object setParentOfThisElement(Object parentOfThisElement) {
        //
        // Make sure that we are only setting the parent to SeleniumDriver or another element.  We assume this within the framework, so lets be sure....
        //
        if (parentOfThisElement == null) {
            _parentElementOrDriver = (HTMLElement) null;  //  If it is null, force the issue....
        } else {
            if ((parentOfThisElement.getClass() != SeleniumDriver.class) &&
                    (parentOfThisElement.getClass() != HTMLElement.class))
                throw new RuntimeException(String.format("An element can only have another Element or the SeleniumDriver as a parent. Type [%s] is invalid!", parentOfThisElement.getClass().getName()));
            _parentElementOrDriver = parentOfThisElement;
        }
        return getParentOfThisElement();
    }

    public boolean hasAParent() {
        return (_parentElementOrDriver != null);
    }

    public boolean hasMappingDetails() {
        return (getMappingDetails() != null && getMappingDetails().getOriginalFindLogic() != null);
    }

    public boolean isBoundToAWebElement() {
        return (_webElement != null);
    }

    public boolean isVisible() {
        return isVisible(false);
    }

    public boolean isVisible(boolean checkIfElementIsInViewport) {
        throwIfUnbound(); // We need this to be bound to an element!
        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Verifying if element is visible");
        boolean seleniumStatesElementDisplayed = getSeleniumDriver().isDisplayed(_webElement);

        if (checkIfElementIsInViewport && seleniumStatesElementDisplayed) {
            boolean elementWithinViewport = false;
            String sResult = null;
            try {
                sResult = getSeleniumDriver().executeJavaScript(String.class, "var rect = arguments[0].getBoundingClientRect(); return ( rect.top >= 0 && rect.left >= 0 && rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && rect.right <= (window.innerWidth || document.documentElement.clientWidth));", _webElement);
                return (sResult.trim().toLowerCase() == "true");
            } catch (Exception e) {
                throw new RuntimeException(String.format("Exception executing Javascript to find status of element [%s]", getFriendlyName()));
            }
        } else {
            return checkIfElementIsInViewport;
        }
    }

    public boolean isHeightStable(Duration deltaTime) {
        return !isAttributeChanging("offsetHeight", deltaTime);
    }

    public boolean isHeightStable() {
        return isHeightStable(Duration.ofMillis(elementDefaultChangeDeltaTimemS));
    }

    public boolean isWidthStable(Duration deltaTime) {
        return !isAttributeChanging("offsetWidth", deltaTime);
    }

    public boolean isWidthStable() {
        return isWidthStable(Duration.ofMillis(elementDefaultChangeDeltaTimemS));
    }

    public boolean isSizeStable(Duration deltaTime) {
        return !isAttributeChanging(new String[]{"offsetWidth", "offsetHeight"}, deltaTime);
    }

    public boolean isSizeStable() {
        return isSizeStable(Duration.ofMillis(elementDefaultChangeDeltaTimemS));
    }

    public boolean isPositionStable(Duration timeDelta) {
        boolean isChanging;
        String jsGetPositionalData = "var rect = arguments[0].getBoundingClientRect(); return '' + rect.left + ',' + rect.top + ',' + rect.right + ',' + rect.bottom";
        try {
            String first = getSeleniumDriver().executeJavaScript(String.class, jsGetPositionalData, _webElement);
            try {
                Thread.sleep(timeDelta.toMillis());
            } catch (Exception e) {
                Logger.WriteLine(Logger.LogLevels.Error, "Exception sleeping during change monitoring of attribute.");
                throw new RuntimeException(String.format("Exception sleeping during time stop-start delta", e));
            }
            String second = getSeleniumDriver().executeJavaScript(String.class, jsGetPositionalData, _webElement);
            isChanging = first.equals(second);
            Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Time Delta [%dmS], First State [%s], Second State [%s] - %s",
                    getFriendlyName(),
                    timeDelta.toMillis(),
                    first,
                    second,
                    (isChanging) ? "Is changing" : "Is not changing");
            return (first.equals(second));
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Exception monitoring position of element [%s]: %s", this.getFriendlyName(), e.toString());
            throw new RuntimeException(String.format("Exception monitoring element attribute. See log.", e));
        }
    }

    public boolean isPositionStable() {
        return isPositionStable(Duration.ofMillis(elementDefaultChangeDeltaTimemS));
    }

    public boolean isElementEnabled() {
        boolean elementEnabled = false;
        if (_webElement != null) {
            elementEnabled = getSeleniumDriver().isEnabled(_webElement);
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, String.format("Element [%s] enabled = [%s]", getFriendlyName(), (elementEnabled) ? "true" : "false"));
            return elementEnabled;
        } else {
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element enabled = [Web or element is NULL, returning false]");
            return false;
        }
    }

    public Size getSize() {
        String height = getSeleniumDriver().executeJavaScript(String.class, "return arguments[0].clientHeight", _webElement);
        String width = getSeleniumDriver().executeJavaScript(String.class, "return arguments[0].clientWidth", _webElement);
        int heightInt;
        int widthInt;

        try {
            heightInt = Integer.parseInt(height);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error parsing height [%s] from Javascript execution for element [%s]", height, getFriendlyName()));
        }

        try {
            widthInt = Integer.parseInt(width);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error parsing height [%s] from Javascript execution for element [%s]", width, getFriendlyName()));
        }
        Size size = new Size(heightInt, widthInt);
        Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Height [%d], Width [%s]", getFriendlyName(), size.getHeight(), size.getWidth());
        return size;
    }

    private boolean isAttributeChanging(String attributeName, Duration timeDelta) {
        boolean isChanging;
        try {
            String first = getSeleniumDriver().executeJavaScript(String.class, String.format("return argumaents[0].%s;", attributeName), _webElement);
            try {
                Thread.sleep(timeDelta.toMillis());
            } catch (Exception e) {
                Logger.WriteLine(Logger.LogLevels.Error, "Exception sleeping during change monitoring of attribute.");
                throw new RuntimeException(String.format("Exception sleeping during time stop-start delta", e));
            }
            String second = getSeleniumDriver().executeJavaScript(String.class, String.format("return arguments[0].%s;", attributeName), _webElement);
            isChanging = first.equals(second);
            Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Attribute [%s], Time Delta [%dmS], First State [%s], Second State [%s] - %s",
                    getFriendlyName(),
                    attributeName,
                    timeDelta.toMillis(),
                    first,
                    second,
                    (isChanging) ? "Is changing" : "Is not changing");
            return (first.equals(second));
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Exception monitoring attribute [%s] of element [%s]: %s", attributeName, this.getFriendlyName(), e.toString());
            throw new RuntimeException(String.format("Exception monitoring element attribute. See log.", e));
        }
    }

    private boolean isAttributeChanging(String[] attributeNames, Duration timeDelta) {
        HashMap<String, String> attributeFirstStates = new HashMap<String, String>();
        boolean isChanging;

        for (String attributeName : attributeNames) {
            attributeFirstStates.put(attributeName, getSeleniumDriver().executeJavaScript(String.class, String.format("return arguments[0].%s;", attributeName), _webElement));
        }

        try {
            Thread.sleep(timeDelta.toMillis());
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Exception sleeping during change monitoring of attribute.");
            throw new RuntimeException(String.format("Exception sleeping during time stop-start delta", e));
        }

        for (Map.Entry<String, String> attributeFirstState : attributeFirstStates.entrySet()) {
            String second = getSeleniumDriver().executeJavaScript(String.class, String.format("return argumaents[0].%s;", attributeFirstState.getKey()), _webElement);
            isChanging = attributeFirstState.getValue().equals(second);

            if (isChanging) {
                Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Attribute [%s], Time Delta [%dmS], First State [%s], Second State [%s] - Is changing",
                        getFriendlyName(),
                        attributeFirstState.getKey(),
                        timeDelta.toMillis(),
                        attributeFirstState.getValue(),
                        second);
                return true;
            }
        }

        Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Attributes [%s], Time Delta [%dmS].  All first states equal second states.  Element NOT changing",
                getFriendlyName(),
                String.join(", ", attributeNames),
                timeDelta.toMillis());
        return false;
    }



    // METHODS

    public List<HTMLElement> findAllElements(ObjectMapping mapping) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElements(this,mapping);
    }

    public HTMLElement findElement(ObjectMapping mapping) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElement(this,mapping);
    }
    public HTMLElement findElement(ObjectMapping mapping,boolean allowMultipleMatches) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElement(this,mapping,allowMultipleMatches);
    }
    public HTMLElement findElement(ObjectMapping mapping,boolean waitUntilSingle,boolean waitUntilStable) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElement(this,mapping,waitUntilSingle,waitUntilStable);
    }

    public HTMLElement findElementOrNull(ObjectMapping mapping) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElementOrNull(this,mapping);
    }
    public HTMLElement findElementOrNull(ObjectMapping mapping,boolean allowMultipleMatches) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElementOrNull(this,mapping,allowMultipleMatches);
    }
    public HTMLElement findElementOrNull(ObjectMapping mapping,boolean waitUntilSingle,boolean waitUntilStable) {
        // We can only do this if we have an instance of SeleniumDriver
        throwIfUnbound();
        return getSeleniumDriver().findElementOrNull(this,mapping,waitUntilSingle,waitUntilStable);
    }
    public Exception getLastFindException() {
        return getSeleniumDriver().getLastException();
    }


    public boolean waitForHeightStable(Duration timeout) {
        return waitForElementStable(StabilityType.HEIGHT, timeout);
    }

    public boolean waitForWidthStable(Duration timeout) {
        return waitForElementStable(StabilityType.WIDTH, timeout);
    }

    public boolean waitForSizeStable(Duration timeout) {
        return waitForElementStable(StabilityType.SIZE, timeout);
    }

    public boolean waitForPositionStable(Duration timeout) {
        return waitForElementStable(StabilityType.POSITION, timeout);
    }

    public HTMLElement findElementAndBind() {
        // We can only do this if this element has a Parent (DOM or another element) and has Mapping details
        // We might do this in a scenario whereby we have created an HTMLElement with a parent and find logic, but havent actually yet
        // bound it to a real element on the web-page.
        if (hasAParent())
            throw new RuntimeException(String.format("[%s]: Cannot find an element without having a parent (either SeleniumDriver or another Element)!", getFriendlyName()));
        if (hasMappingDetails()) {
            throw new RuntimeException(String.format("[%s]: Cannot find a WebElement, mapping details (find logic) are unknown!", getFriendlyName()));
        }

        HTMLElement foundElement;
        try {
            if (getParentOfThisElement().getClass() == SeleniumDriver.class) {
                foundElement = ((SeleniumDriver) getParentOfThisElement()).findElement(getMappingDetails());
            } else {
                foundElement = ((HTMLElement) getParentOfThisElement()).findElement(getMappingDetails());
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Unable to bind element [%s] as child of [%s] (Find Logic [%s])", getMappingDetails().getFriendlyName(), (getParentOfThisElement().getClass() == SeleniumDriver.class) ? "Driver - IE. a Root element" : ((HTMLElement) getParentOfThisElement()).getMappingDetails().getFriendlyName(), getMappingDetails().getOriginalFindLogic()), ex);
        }
        this.setUnderlyingWebElement(foundElement.getUnderlyingWebElement());
        return this;
    }

    public void clear() {
        throwIfUnbound();
        try {
            getSeleniumDriver().clear(this.getUnderlyingWebElement());
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error thrown clearing text in [%s]: %s",getFriendlyName(),e.getMessage());
            throw new RuntimeException(String.format("Error thrown clearing text in [%s] ([%s]).",getFriendlyName(),getMappingDetails().getActualFindLogic()),e);

        }
    }

    public void setText(String text) {
        setText(text,1,null);}
    public void setText(String text,int maxTries) {
        setText(text,maxTries,null);}
    public void setText(String text,Duration retryInterval) {
        setText(text,1,retryInterval);}
    public void setText(String text,int maxTries,Duration retryInterval)
    {
        RuntimeException lastException=null;
        throwIfUnbound();
        if (maxTries<1) throw new RuntimeException(String.format("Maximum tries [%d].  Cannot be less than 1.",maxTries));
        int tryIndex = 0;
        Duration interval = (retryInterval==null) ? Duration.ofMillis(200) : retryInterval;
        try
        {
            // Loop until maximum retry count
            while (tryIndex++ <= maxTries)
            {
                try
                {
                    getSeleniumDriver().clear(this.getUnderlyingWebElement());
                    enterText(text);
                    if (tryIndex > 1) Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "{0} attempt attempt good.)", tryIndex);
                    return;
                }
                catch (InvalidElementState e)
                {
                    Thread.sleep(retryInterval.toMillis());
                    lastException = e;
                }
            }
            throw lastException;
        }
        catch (InvalidElementState ex) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error thrown setting text (clear then enter) in [%s] (Tried %d times): %s",getFriendlyName(),tryIndex,ex.getMessage());
            throw new InvalidElementState(String.format("Error thrown setting text (clear then enter) in [%s] ([%s]).  Tried %d times).",getFriendlyName(),getMappingDetails().getActualFindLogic(),tryIndex),ex);

        }
        catch (Exception e)
        {
            Logger.WriteLine(Logger.LogLevels.Error,"Error thrown setting text (clear then enter) in [%s] (Tried %d times): %s",getFriendlyName(),tryIndex,e.getMessage());
            throw new RuntimeException(String.format("Error thrown setting text (clear then enter) in [%s] ([%s]).  Tried %d times).",getFriendlyName(),getMappingDetails().getActualFindLogic(),tryIndex),e);
        }
    }


    public void enterText(String text) {
        enterText(text,1,null);}
    public void enterText(String text,int maxTries) {
        enterText(text,maxTries,null);}
    public void enterText(String text,Duration retryInterval) {
        enterText(text,1,retryInterval);}
    public void enterText(String text,int maxTries,Duration retryInterval) {
        RuntimeException lastException=null;
        throwIfUnbound();
        if (maxTries<1) throw new RuntimeException(String.format("Maximum tries [%d].  Cannot be less than 1.",maxTries));
        int tryIndex = 0;
        Duration interval = (retryInterval==null) ? Duration.ofMillis(200) : retryInterval;
        try
        {
            // Loop until maximum retry count
            while (tryIndex++ <= maxTries)
            {
                try
                {
                    getSeleniumDriver().setText(this.getUnderlyingWebElement(),(text==null)?"":text);
                    if (tryIndex > 1) Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "{0} attempt attempt good.)", tryIndex);
                    return;
                }
                catch (InvalidElementState e)
                {
                    Thread.sleep(retryInterval.toMillis());
                    lastException = e;
                }
            }
            throw lastException;
        }
        catch (InvalidElementState ex) {
            Logger.WriteLine(Logger.LogLevels.Error,"Error thrown entering text in [%s] (Tried %d times): %s",getFriendlyName(),tryIndex,ex.getMessage());
            throw new InvalidElementState(String.format("Error thrown entering text in [%s] ([%s]).  Tried %d times).",getFriendlyName(),getMappingDetails().getActualFindLogic(),tryIndex),ex);

        }
        catch (Exception e)
        {
            Logger.WriteLine(Logger.LogLevels.Error,"Error thrown entering text in [%s] (Tried %d times): %s",getFriendlyName(),tryIndex,e.getMessage());
            throw new RuntimeException(String.format("Error thrown entering text in [%s] ([%s]).  Tried %d times).",getFriendlyName(),getMappingDetails().getActualFindLogic(),tryIndex),e);
        }
    }

    public String getText() {
        throwIfUnbound();
        return getText(true);
    }
    public String getText(boolean includeDesendants) {
        throwIfUnbound();
        try {
            return getSeleniumDriver().getText(getUnderlyingWebElement(),includeDesendants,false,false);
        }
        catch (InvalidElementState ex) {
            throw ex;
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown getting text from [%s]: %s",getFriendlyName(),e.getMessage());
            throw new RuntimeException(String.format("Error thrown getting text from [%s] ([%s])",getFriendlyName(),getMappingDetails().getActualFindLogic()),e);
        }
    }

    public String scrollIntoViewAndGetText() {
        throwIfUnbound();
        return scrollIntoViewAndGetText(true);
    }
    public String scrollIntoViewAndGetText(boolean includeDesendants) {
        throwIfUnbound();
        try {
            return getSeleniumDriver().getText(getUnderlyingWebElement(),includeDesendants,true,false);
        }
        catch (InvalidElementState ex) {
            throw ex;
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown scrolling into view and getting text from [%s]: %s",getFriendlyName(),e.getMessage());
            throw new RuntimeException(String.format("Error thrown scrolling into view and getting text from [%s] ([%s])",getFriendlyName(),getMappingDetails().getActualFindLogic()),e);
        }
    }

    public String getAttribute(String attribute) {
        throwIfUnbound();
        try {
            return getSeleniumDriver().getAttribute(getUnderlyingWebElement(),attribute);
        }
        catch (InvalidElementState ex) {
            throw ex;
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown getting attribute [%s] from [%s]: %s",getFriendlyName(),e.getMessage());
            throw new RuntimeException(String.format("Error thrown getting attribute [%s] from [%s] ([%s])",attribute==null?"Null":attribute,getFriendlyName(),getMappingDetails().getActualFindLogic()),e);
        }
    }

    public boolean hasAttribute(String attribute) {
        throwIfUnbound();
        try {
            return getSeleniumDriver().hasAttribute(getUnderlyingWebElement(),attribute);
        }
        catch (InvalidElementState ex) {
            throw ex;
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestDebug,"Error thrown getting attribute [%s] from [%s]. Assume does not have attribute: %s",getFriendlyName(),e.getMessage());
            return false;
        }
    }

    public void click() {
        throwIfUnbound();
        try {
            getSeleniumDriver().click(getUnderlyingWebElement());
        }
        catch (InvalidElementState ex) {
            throw ex;
        }
        catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown clicking [%s]: %s",getFriendlyName(),e.getMessage());
            throw new RuntimeException(String.format("Error thrown clicking [%s] ([%s])",getFriendlyName(),getMappingDetails().getActualFindLogic()),e);
        }
    }

    // MAT CARRY ON HERE WITH 'SelectedItem' (Element.cs)


    private boolean waitForElementStable(StabilityType stabilityType) {
        return waitForElementStable(stabilityType, null);
    }
    private boolean waitForElementStable(StabilityType stabilityType, Duration timeout) {
        throwIfUnbound();
        boolean didStabilzeBeforeTimeout = false;
        int iterations = 0;
        Duration actualTimeout = (timeout == null) ? getSeleniumDriver().getElementFindTimeout() : timeout;
        long actualTimeoutMillis = actualTimeout.toMillis();

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Wait %dms for element [%s] to become [%s] stable", actualTimeoutMillis, getFriendlyName(), stabilityType.toString());

        StopWatch timeWaited = StopWatch.createStarted();
        while (timeWaited.getTime(TimeUnit.MILLISECONDS) < actualTimeoutMillis) {
            //
            // We dont have a poll delay as the Height stabalization monitor uses a time delta to see if the height is stable.  That in effect
            // is a poll interval (which will also work on the cloud)
            //
            try {
                iterations++;
                boolean isStable = false;
                switch (stabilityType) {
                    case HEIGHT:
                        isStable = isHeightStable(timeout);
                        break;
                    case WIDTH:
                        isStable = isWidthStable(timeout);
                        break;
                    case POSITION:
                        isStable = isPositionStable(timeout);
                        break;
                    case SIZE:
                        isStable = isSizeStable(timeout);
                        break;
                    default:
                        throw new RuntimeException(String.format("Stability type [%s] unknown!", stabilityType.toString()));
                }
                if (isStable) {
                    didStabilzeBeforeTimeout = true;
                    break;
                }
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Cannot determine if element [%s] is height-stable.", getFriendlyName()), ex);
            }
        }

        Logger.WriteLine(Logger.LogLevels.TestDebug, "Element %s %s stable after %dms (%d iterations)", getFriendlyName(), (didStabilzeBeforeTimeout) ? "is" : "NOT", timeWaited.getTime(TimeUnit.MILLISECONDS), iterations);
        return didStabilzeBeforeTimeout;
    }


    /// <summary>
    /// Throws exception if Element is not bound to a Selenium IWebElement.
    /// </summary>
    private void throwIfUnbound() {
        if (!hasAParent())
            throw new RuntimeException("Cannot identify Selenium Driver as no parent set (SeleniumDriver or Element)!");
        if (!isBoundToAWebElement())
            throw new RuntimeException("Not bound to a Selenium Web Element");
    }

    //////////////////// STATICS
    /// <summary>Tests if element is currently visible to the user.</summary>
    /// <param name="Element">Element to test</param>
    /// <returns>True if visible, false if not (or if element is null)</returns>
    /// <remarks>
    /// This uses the Selenium Displayed boolean property.
    /// </remarks>
    public static boolean isVisible(HTMLElement element) {
        return isVisible(element, false);
    }
    public static boolean isVisible(HTMLElement element, boolean CheckIfElementIsInViewport) {
        return element.isVisible(CheckIfElementIsInViewport);
    }

 //   public static boolean isElementEnabled(HTMLElement element) {
 //       boolean elementEnabled = false;
 //       if ((element != null) && element.getUnderlyingWebElement() != null) {
 //           elementEnabled =  element.getUnderlyingWebElement().isEnabled();
 //           Logger.WriteLine(Logger.LogLevels.FrameworkInformation, String.format("Element [%s] enabled = [%s]", element.getFriendlyName(), (elementEnabled) ? "true" : "false"));
 //           return elementEnabled;
 //       } else {
 //           Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element enabled = [Web or element is NULL, returning false]");
 //           return false;
 //       }
 //   }


}
