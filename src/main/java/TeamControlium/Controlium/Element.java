package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import jdk.internal.jline.internal.Nullable;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Element {
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //region From Element.cs

    private ObjectMappingDetails mappingDetails;
    private WebElement webElement; // This is the actual Selenium element.  To use this element, this must be set....
    private Object _ParentOfThisElement; //Parent of this element.  Must be either another element OR SeleniumDriver

    /// <summary>
    /// Sets or Get the instance of the Selenium Web-Element being used.
    /// </summary>
    public WebElement getWebElement() { return webElement; }
    public WebElement setWebElement(WebElement value) { webElement=value; return webElement;}

    /// <summary>
    /// Returns find logic used (or to be used) to locate this element either from the top level (See <see cref="Element(SeleniumDriver,IWebElement,string,FindBy)"/>) or from the parent element (See <see cref="Element(Element,IWebElement,string,FindBy)"/>)
    /// </summary>
    public ObjectMappingDetails getMappingDetails() { return mappingDetails; }
    public ObjectMappingDetails setMappingDetails(ObjectMappingDetails value) throws Exception { if ((mappingDetails != null) && (webElement != null)) throw new Exception("Mapping Details cannot be changed after binding to Selenium WebElement"); else mappingDetails = value; return mappingDetails;}


    /// <summary>
    /// Last Exception thrown from a Try method
    /// </summary>
    private Exception _TryException;
    public Exception getTryException() { return _TryException; }

    /// <summary>
    /// Gets or Sets the Parent of this element.  A Parent can be SeleniumDriver (if the element has no parent) or another Element
    /// </summary>
    public Object getParentOfThisElement() { return _ParentOfThisElement;}
    public Object setParentOfThisElement(Object value) throws Exception {

        //
        // Make sure that we are only setting the parent to SeleniumDriver or another element.  We assume this within the framework, so lets be sure....
        //
        if (value == null)
        {
            _ParentOfThisElement = null;  //  If it is null, force the issue....
        }
        else
        {
            if (!(value instanceof SeleniumDriver) && !(value instanceof Element)) {
                throw new Exception(String.format("An element can only have another Element or the SeleniumDriver as a parent. Type [%s] is invalid!", value.getClass().getTypeName()));
            }
            _ParentOfThisElement = value;
        }
        return _ParentOfThisElement;
    }

    public SeleniumDriver getSeleniumDriver() throws Exception {
        // We can only do this if this element has a Parent (DOM or another element)
        if (getParentOfThisElement() == null)
            throw new Exception("Cannot get an instance of the Selenium Driver as this element (or a Parent of) does not have a Parent!");
        //
        // parent is an Element all the way up the tree until the top level, when it is the SeleniumDriver....  We just
        // itterate up the tree recursivley until we hit the Selenium Driver
        //
        Object parentTally = getParentOfThisElement();
        if (parentTally instanceof SeleniumDriver)
            return ((SeleniumDriver)parentTally);
        else
            return ((Element)parentTally).getSeleniumDriver();
    }

    private Duration TimeWaited(Date startTime) {
        long diffInMilliSeconds = Calendar.getInstance().getTime().getTime() - startTime.getTime();
        return Duration.ofMillis(diffInMilliSeconds);
    }

    public boolean WaitForElementHeightStable() throws Exception { return WaitForElementHeightStable(null); }
    public boolean WaitForElementHeightStable(@Nullable Duration Timeout) throws Exception
    {
        ThrowIfUnbound();
        boolean didStabilzeBeforeTimeout = false;
        int Itterations = 0;
        Duration actualTimeout = Timeout==null ? getSeleniumDriver().getFindTimeout() : Timeout;

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, String.format("Wait %dms for element %s to become height stable", actualTimeout.toMillis(), getMappingDetails().getFriendlyName()));

        Date startTime = Calendar.getInstance().getTime();
        while (TimeWaited(startTime).toMillis() < actualTimeout.toMillis())
        {
            //
            // We dont have a poll delay as the Height stabalization monitor uses a time delta to see if the height is stable.  That in effect
            // is a poll interval (which will also work on the cloud)
            //
            try
            {
                Itterations++;
                if (IsHeightStable)
                {
                    didStabilzeBeforeTimeout = true;
                    break;
                }
            }
            catch (Exception ex)
            {
                throw new Exception($"Cannot determine if element [{MappingDetails.FriendlyName}] is height-stable: {ex}");
            }
        }

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element height stable after {0}ms ({1} itterations)", timeWaited.Elapsed.TotalSeconds.ToString(), Itterations.ToString());
        return didStabilzeBeforeTimeout;
    }

    public bool WaitForElementPositionStable(TimeSpan? Timeout = null)
    {
        ThrowIfUnbound();
        bool didStabilzeBeforeTimeout = false;
        int Itterations = 0;
        TimeSpan actualTimeout = Timeout ?? seleniumDriver.ElementFindTimeout;

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Wait {0}ms for element {1} to become position stable", actualTimeout.TotalMilliseconds, MappingDetails.FriendlyName);


        Stopwatch timeWaited = Stopwatch.StartNew();
        while (timeWaited.ElapsedMilliseconds < actualTimeout.TotalMilliseconds)
        {
            //
            // We dont have a poll delay as the Height stabalization monitor uses a time delta to see if the height is stable.  That in effect
            // is a poll interval (which will also work on the cloud)
            //
            try
            {
                Itterations++;
                if (IsPositionStable)
                {
                    didStabilzeBeforeTimeout = true;
                    break;
                }
            }
            catch (Exception ex)
            {
                throw new Exception($"Cannot determine if element [{MappingDetails.FriendlyName}] is position-stable: {ex}");
            }
        }

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element position stable after {0}ms ({1} itterations)", timeWaited.Elapsed.TotalSeconds.ToString(), Itterations.ToString());
        return didStabilzeBeforeTimeout;
    }


    /// <summary>
    /// Binds the Element to the WebElement
    /// </summary>
    /// <returns></returns>
    public Element BindWebElement()
    {
        // We can only do this if this element has a Parent (DOM or another element) and has Mapping details
        if (HasAParent)
            throw new Exception("Cannot find a WebElement without having a parent (SeleniumDriver or Element)!");
        if (HasMappingDetails)
        {
            throw new Exception("Cannot find a WebElement if mapping details (find logic) are unknown!");
        }

        Element foundElement;
        try
        {
            if (ParentOfThisElement.GetType() == typeof(SeleniumDriver))
            {
                foundElement = ((SeleniumDriver)ParentOfThisElement).FindElement(MappingDetails);
            }
            else
            {
                foundElement = ((Element)ParentOfThisElement).FindElement(MappingDetails);
            }
        }
        catch (Exception ex)
        {
            throw new Exception(string.Format("Unable to bind to element {0} (Find Logic ({1}): [{2}])", MappingDetails.FriendlyName, MappingDetails.FindType.ToString(), MappingDetails.FindLogic), ex);
        }
        WebElement = foundElement.WebElement;
        MappingDetails = foundElement.MappingDetails;
        return this;
    }

    /// <summary>
    /// Returns the selected item if a Select type element
    /// </summary>
    /// <returns></returns>
    public Element SelectedItem()
    {
        ThrowIfUnbound();
        try
        {
            SelectElement selectElement = new SelectElement(this.WebElement);
            IWebElement selectedElement = selectElement.SelectedOption;
            Element select = new Element(this);
            select.WebElement = selectedElement;
            return select;
        }
        catch (Exception ex)
        {
            throw new Exception("Cannot get selected item.", ex);
        }
    }



    /// <summary>Performs a clear action on the element</summary>
    public Element Clear()
    {
        ThrowIfUnbound();
        seleniumDriver.Clear(WebElement);
        return this;
    }

    /// <summary>
    /// Gets the selected status for the element
    /// </summary>
    /// <remarks>Assumes element is a type that can have a selected status (IE. Checkbox, list item etc...)</remarks>
    /// <returns>True if selected, false if not</returns>
    public bool Selected()
    {
        ThrowIfUnbound();
        return seleniumDriver.IsSelected(WebElement);
    }

    public void SetSelected(bool State)
    {
        ThrowIfUnbound();
        seleniumDriver.SetSelected(WebElement, State);
    }

    /// <summary>
    /// Performs a mouse hover-over the element
    /// </summary>
    public void Hover()
    {
        ThrowIfUnbound();
        (new Actions(seleniumDriver.WebDriver)).MoveToElement(webElement).Build().Perform();
    }

    /// <summary>Performs a left-mouse-button click in the element
    /// </summary>
    /// <exception cref="ElementCannotBeClicked">Thrown if element cannot be clicked or has not received the click event</exception>
    public void Click()
    {
        ThrowIfUnbound();
        seleniumDriver.Click(WebElement);
    }
    /// <summary>Performs a left-mouse-button click in the element.  Any exception is caught and not re-thrown.
    /// </summary>
    /// <returns>False in an exception was thrown.  See <see cref="Element.TryException"/> for actual exception if required.</returns>
    public bool TryClick()
    {
        ThrowIfUnbound();
        try
        {
            Click();
            TryException = null;
            return true;
        }
        catch (Exception ex)
        {
            TryException = ex;
            return false;
        }
    }

    /// <summary>Clears field then enters text, using keyboard, into element</summary>
    /// <param name="Text">Text to enter</param>
    /// <seealso cref="seleniumDriver.SetText(IWebElement,string)"/>
    public void SetText(string Text,int maxretries=3,TimeSpan? retryInterval=null)
    {
        ThrowIfUnbound();

        int retryIndex = 0;
        Exception lastException=null;
        TimeSpan interval = retryInterval ?? TimeSpan.FromMilliseconds(100);
        try
        {
            while (++retryIndex <= maxretries || maxretries == 0)
            {
                try
                {
                    seleniumDriver.Clear(this.WebElement);
                    EnterText(Text);
                    if (retryIndex > 1)
                        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "{0} attempts with InvalidElementStateException.  Last attempt good.)", retryIndex);
                    return;
                }
                catch (OpenQA.Selenium.InvalidElementStateException ex)
                {
                    Thread.Sleep(interval);
                    lastException = ex;
                }
            }
            throw lastException;
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "{0} failed attempts to set [{1}] to text [{2}]", retryIndex,this.MappingDetails?.FriendlyName??"???!",Text);
            throw new UnableToSetOrGetText(MappingDetails, Text, ex);
        }
    }

    /// <summary>Enters text, using keyboard, into element</summary>
    /// <param name="Text">Text to enter</param>
    /// <seealso cref="seleniumDriver.SetText(IWebElement,string)"/>
    public void EnterText(string Text,int maxretries= 3, TimeSpan? retryInterval = null)
    {
        ThrowIfUnbound();
        int retryIndex = 0;
        Exception lastException = null;
        TimeSpan interval = retryInterval ?? TimeSpan.FromMilliseconds(100);
        try
        {
            while (++retryIndex <= maxretries || maxretries == 0)
            {
                try
                {
                    seleniumDriver.SetText(this.WebElement, Text ?? string.Empty);
                    if (retryIndex > 1)
                        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "{0} attempts with InvalidElementStateException.  Last attempt good.)", retryIndex);
                    return;
                }
                catch (OpenQA.Selenium.InvalidElementStateException ex)
                {
                    Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Attempt {0} failed (InvalidElementStateException)", retryIndex);
                    Thread.Sleep(interval);
                    lastException = ex;
                }
            }
            throw lastException;
        }
        catch (Exception ex)
        {
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "{0} failed attempts to set [{1}] to text [{2}]", retryIndex, this.MappingDetails?.FriendlyName ?? "???!", Text);
            throw new UnableToSetOrGetText(MappingDetails, Text, ex);
        }
    }

    /// <summary>Enters text, using keyboard, into element, pressing Enter Key at the end</summary>
    /// <param name="Text">Text to enter</param>
    /// <seealso cref="seleniumDriver.SetText(IWebElement,string)"/>
    public void SetTextAndPressEnter(string Text)
    {
        ThrowIfUnbound();
        SetText((Text ?? string.Empty) + Keys.Enter);
    }


    /// <summary>Clears field then enters text, using keyboard, into element.  No exception is thrown.</summary>
    /// <param name="Text">Text to enter</param>
    /// <returns>False in any exception was thrown</returns>
    /// <seealso cref="seleniumDriver.SetText(IWebElement,string)"/>
    /// <seealso cref="seleniumDriver.TryException"/>
    public bool TrySetText(string Text)
    {
        ThrowIfUnbound();
        try
        {
            SetText(Text);
            TryException = null;
            return true;
        }
        catch (Exception ex)
        {
            TryException = ex;
            return false;
        }
    }

    /// <summary>
    /// Scroll the element into view
    /// </summary>
    /// <remarks>Uses Javascript inject to perform scroll</remarks>
    public void ScrollIntoView()
    {
        ThrowIfUnbound();
        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Scrolling [{0}] element into view", MappingDetails.FriendlyName);
        seleniumDriver.ScrollIntoView(WebElement);
    }

    /// <summary>Scrolls window so that target is in view and then gets visible text from element
    /// </summary>
    /// <remarks>Element is scrolled into view before text is harvested.  See <see cref="seleniumDriver.GetText(IWebElement,bool,bool)"/> for details.</remarks>
    /// <returns>Text from element</returns>
    public string ScrollIntoViewAndGetText()
    {
        ThrowIfUnbound();
        return ScrollIntoViewAndGetText(true);
    }


    /// <summary>Scrolls window so that target is in view and then gets visible text from element
    /// </summary>
    /// <param name="IncludeDesendants">If true all text is returned. If false only text from current element is returned.</param>
    /// <remarks>Element is scrolled into view before text is harvested.  See <see cref="seleniumDriver.GetText(IWebElement,bool,bool,bool)"/> for details.</remarks>
    /// <returns>Text from element</returns>
    public string ScrollIntoViewAndGetText(bool IncludeDesendants)
    {
        ThrowIfUnbound();
        //
        // Only get the visible text - ensure what IS visible by scrolling into view (but NOT if using IE8 as IE8 has an issue with scrolling if there is overflow into the x-axis)...
        //
        if (SeleniumDriver.TestBrowser == SeleniumDriver.Browsers.IE8)
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Not scrolling into view as Browser IE8!");
        return seleniumDriver.GetText(this.WebElement, IncludeDesendants, (SeleniumDriver.TestBrowser != SeleniumDriver.Browsers.IE8), false);
    }

    /// <summary>Gets visible text from element
    /// </summary>
    /// <remarks>Element is scrolled into view before text is harvested.  See <see cref="seleniumDriver.GetText(IWebElement,bool,bool)"/> for details.</remarks>
    /// <returns>Text from element</returns>
    public string GetText()
    {
        ThrowIfUnbound();
        return GetText(true);
    }

    public string GetText(bool IncludeDesendants)
    {
        ThrowIfUnbound();
        //
        // Only get the visible text - ensure what IS visible by scrolling into view (NOT if using IE)...
        //
        return seleniumDriver.GetText(WebElement, IncludeDesendants, false, false);
    }

    /// <summary>Gets visible text from element</summary>
    /// <param name="Text">Text from element</param>
    /// <returns>False if exception was thrown.</returns>
    /// <seealso cref="seleniumDriver.TryException"/>
    public bool TryGetText(out string Text)
    {
        ThrowIfUnbound();
        return TryGetText(true, out Text);
    }

    public bool TryGetText(bool IncludeDesendants, out string Text)
    {
        ThrowIfUnbound();
        try
        {
            Text = GetText();
            TryException = null;
            return true;
        }
        catch (Exception ex)
        {
            Text = string.Empty;
            TryException = ex;
            return false;
        }
    }
    /// <summary>Get all text from element (and descendant elements)
    /// </summary>
    /// <remarks>Element is scrolled into view before text is harvested.  See <see cref="seleniumDriver.GetText(IWebElement,bool,bool,bool)"/> for details.</remarks>
    /// <returns>Text from element (and descendants)</returns>
    public string GetAllText()
    {
        ThrowIfUnbound();

        // Just do the simple Text attribute thing.  Stuff if the user cant see the text!  However, still scroll into view as Selenium has an issue with that and
        // sometimes it would return a blank string if not in the viewport....
        return seleniumDriver.GetText(WebElement, true, true, false);
    }
    /// <summary>Get all text from element (and descendant elements)
    /// </summary>
    /// <param name="Text">Text from element</param>
    /// <returns>False if exception was thrown.</returns>
    /// <seealso cref="seleniumDriver.TryException"/>
    public bool TryGetAllText(out string Text)
    {
        ThrowIfUnbound();
        try
        {
            Text = GetAllText();
            TryException = null;
            return true;
        }
        catch (Exception ex)
        {
            Text = string.Empty;
            TryException = ex;
            return false;
        }
    }
    /// <summary>Get attribute text from current element
    /// </summary>
    /// <param name="Attribute">Attribute to get</param>
    /// <returns>Text in named attribute</returns>
    public string GetAttribute(string Attribute)
    {
        ThrowIfUnbound();

        string returnValue = WebElement.GetAttribute(Attribute);
        if ((returnValue == null)) throw new AttributeReturnedNull(MappingDetails, Attribute);
        return returnValue;
    }
    /// <summary>Get attribute test from current element.  Does not throw exception.
    /// </summary>
    /// <param name="Attribute">Attribute to get</param>
    /// <param name="AttributeText">Text in named attribute</param>
    /// <returns>True if attribute text got or false in an exception thrown</returns>
    public bool TryGetAttribute(string Attribute, out string AttributeText)
    {
        ThrowIfUnbound();
        try
        {
            AttributeText = GetAttribute(Attribute);
            TryException = null;
            return true;
        }
        catch (Exception ex)
        {
            AttributeText = string.Empty;
            TryException = ex;
            return false;
        }
    }
    /// <summary>
    /// Throws exception if Element is not bound to a Selenium IWebElement.
    /// </summary>
    private void ThrowIfUnbound() throws Exception
    {
        if (!HasAParent())
            throw new Exception("Cannot identify Selenium Driver as no parent set (SeleniumDriver or Element)!");
        if (!IsBoundToAWebElement())
            throw new Exception("Not bound to a Selenium Web Element");
    }
}

    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //region From ElementProperties.cs

    /// <summary>
    /// Indicates if Element if visible
    /// </summary>
    /// <seealso cref="seleniumDriver.IsElementVisible(IWebElement)"/>
    public boolean IsDisplayed() throws Exception { return Visible(this); }

    public boolean IsHeightStable() throws Exception { return !AttributeChanging("offsetHeight", Duration.ofMillis(200)); }
    public boolean IsWidthStable() throws Exception { return !AttributeChanging("offsetWidth", Duration.ofMillis(200)); }

    public boolean IsSizeStable() throws Exception { return !AttributeChanging(new String[] { "offsetWidth", "offsetHeight" }, Duration.ofMillis(200)); }

    public boolean IsPositionStable() throws Exception{
        String first = getSeleniumDriver().ExecuteJavaScriptReturningString("var rect = arguments[0].getBoundingClientRect(); return '' + rect.left + ',' + rect.top + ',' + rect.right + ',' + rect.bottom", webElement);
        Thread.sleep(200);
        String second = getSeleniumDriver().ExecuteJavaScriptReturningString("var rect = arguments[0].getBoundingClientRect(); return '' + rect.left + ',' + rect.top + ',' + rect.right + ',' + rect.bottom", webElement);
        return (first == second);  // If they have the same coordinates after 200mS we will consider the element stable
    }

    /// <summary>
    /// Indicates if Element if enabled
    /// </summary>
    /// <seealso cref="seleniumDriver.IsElementEnabled(IWebElement)"/>
    public boolean IsEnabled() throws Exception { return getSeleniumDriver().IsElementEnabled(this); }

    /// <summary>
    /// Returns the element size using the java clientHeight and clientWidth attributes
    /// </summary>
    public Size ElementSize() throws Exception {
        String Height = getSeleniumDriver().ExecuteJavaScriptReturningString("return arguments[0].clientHeight", webElement);
        String Width = getSeleniumDriver().ExecuteJavaScriptReturningString("return arguments[0].clientWidth", webElement);
        return new Size(Integer.parseInt(Width), Integer.parseInt(Height));
    }

    /// <summary>Tests if element is currently visible to the user.</summary>
    /// <param name="Element">Element to test</param>
    /// <returns>True if visible, false if not (or if element is null)</returns>
    /// <remarks>
    /// This uses the Selenium Displayed boolean property.
    /// </remarks>
    public static boolean Visible(Element element) throws Exception {
        return Visible(element,false);
    }

    public static boolean Visible(Element element, boolean CheckIfElementIsInViewport) throws Exception
    {
        return element.Visible(CheckIfElementIsInViewport);
    }

    /// <summary>Tests if element is currently visible to the user.</summary>
    /// <param name="Element">Element to test</param>
    /// <param name="CheckIfElementIsInViewport">If true, checks to see if element is in Viewport.  If false uses builtin Selenium element property</param>
    /// <returns>True if visible, false if not (or if element is null)</returns>
    /// <remarks>
    /// This uses the Selenium Displayed boolean property but ANDs it with a Javascript result that discovers if the element is in the viewport.  This
    /// fixes the Selenium issue whereby it returns a false TRUE (IE. Element is not actually in the Displayport....)
    /// </remarks>
    public boolean Visible() throws Exception {
        return Visible(false);
    }
    public boolean Visible(boolean CheckIfElementIsInViewport) throws Exception
    {
        if (this.webElement != null)
        {
            if (CheckIfElementIsInViewport)
            {
                boolean SeleniumDisplayed = false;
                boolean MyDisplayed = false;
                ByReference<String> sResult = new ByReference<String>();
                if (!getSeleniumDriver().TryExecuteJavaScript("var rect = arguments[0].getBoundingClientRect(); return ( rect.top >= 0 && rect.left >= 0 && rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && rect.right <= (window.innerWidth || document.documentElement.clientWidth));", sResult, this.webElement))
                {
                    throw new Exception(String.format("Visible javascript error: {0}", sResult));
                }
                SeleniumDisplayed = this.webElement.isDisplayed();
                MyDisplayed = (sResult.get().toLowerCase() == "true");
                Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element in Viewport = {0} ({1})", sResult, MyDisplayed ? "Yes" : "No");
                return SeleniumDisplayed && MyDisplayed;
            }
            else
                return this.webElement.isDisplayed();
        }
        else
        {
            Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Element displayed = [Web or element is NULL]");
            return false;
        }
    }

    private boolean AttributeChanging(String AttribName, Duration timeDelta) throws Exception
    {
        String First = getSeleniumDriver().ExecuteJavaScriptReturningString(String.format("return arguments[0].%s;", AttribName), webElement);
        Thread.sleep(timeDelta.toMillis());
        String Second = getSeleniumDriver().ExecuteJavaScriptReturningString(String.format("return arguments[0].%s;", AttribName), webElement);
        return (!First.equals(Second));
    }

    private boolean AttributeChanging(String[] AttribNames, Duration timeDelta) throws Exception
    {
        List<String[]> Measures = new ArrayList<String[]>();
        try
        {
            for (String attrib : AttribNames)
            {
                String[] measure = new String[2];
                measure[0] = attrib;
                measure[1] = getSeleniumDriver().ExecuteJavaScriptReturningString(String.format("return arguments[0].%s;", measure[0]), webElement);
                Measures.add(measure);
            }
            Thread.sleep(timeDelta.toMillis());

            for (String[] measure : Measures)
            {
                String newMeasure = getSeleniumDriver().ExecuteJavaScriptReturningString(String.format("return arguments[0].%s;", measure[0]), webElement);
                if (measure[1] != newMeasure)
                    return false;
            }
            return true;
        }
        catch (Exception ex)
        {
            throw new Exception("Error obtaining and comparing named attributes", ex);
        }
    }

    private boolean HasAParent() { return (getParentOfThisElement() != null); }
    private boolean IsBoundToAWebElement() { return (webElement != null); }
    private boolean HasMappingDetails() { return (getMappingDetails() != null); }

    //endregion

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //
    //region From ElementConstructors.cs

    /// <summary>
    /// Create an instance of an Element.
    /// </summary>
    /// <remarks>
    /// Element has no parent set, is not bound to a Selemenium WebElement and has no mapping details.
    /// </remarks>
    public Element()
    {
        setWebElement(null);
        try { setParentOfThisElement(null); } catch(Exception ex){}
        try { setMappingDetails(null); } catch(Exception ex){}
    }


    /// <summary>
    /// Create an instance of an Element.
    /// </summary>
    /// <remarks>
    /// Element has no parent set, is not bound to a Selemenium WebElement and has no mapping details.
    /// </remarks>
    public Element(ObjectMappingDetails mappingDetails)
    {
        setWebElement(null);
        try { setParentOfThisElement(null); } catch(Exception ex){}
        try { setMappingDetails(mappingDetails); } catch(Exception ex){}
    }


    /// <summary>
    /// Create an instance of an Element. Element will be child of the DOM, and will have no parent element.  It has not yet been bound to a Selenium WebElement
    /// </summary>
    /// <remarks>
    /// Element has no parent set, is not bound to a Selemenium WebElement and has no mapping details.
    /// </remarks>
    /// <param name="seleniumDriver">The Selenium Driver instance this element will be located in</param>
    public Element(SeleniumDriver seleniumDriver)
    {
        setWebElement(null);
        try { setParentOfThisElement(seleniumDriver); } catch(Exception ex){}
        try { setMappingDetails(mappingDetails); } catch(Exception ex){}
    }


    /// <summary>
    /// Create an instance of an Element. Element will be child of another element.  It has not yet been bound to a Selenium WebElement
    /// </summary>
    /// <remarks>
    /// Element will have a parent element, but is not yet bound to a Selemenium WebElement and has no mapping details.
    /// </remarks>
    /// <param name="seleniumDriver">The Selenium Driver instance this element will be located in</param>
    public Element(Element parentElement)
    {
        setWebElement(null);
        try { setParentOfThisElement(parentElement); } catch(Exception ex){}
        try { setMappingDetails(mappingDetails); } catch(Exception ex){}
    }

    /// <summary>
    /// Create an instance of an Element. Element will be child of the DOM, and will have no parent element.  It has not yet been bound to a Selenium WebElement
    /// </summary>
    /// <remarks>
    /// Element has no parent set, is not bound to a Selemenium WebElement but has been passed its Mapping Details (Find Logic).
    /// </remarks>
    /// <param name="seleniumDriver">The Selenium Driver instance this element will be located in</param>
    /// <param name="MappingDetails">Find Logic and friendly name of element when bound to a Selenium WebElement</param>
    public Element(SeleniumDriver seleniumDriver, ObjectMappingDetails MappingDetails)
    {
        setWebElement(null);
        try { setParentOfThisElement(seleniumDriver); } catch(Exception ex){}
        try { setMappingDetails(MappingDetails); } catch(Exception ex){}
    }


    /// <summary>
    /// Create an instance of an Element. Element will be child of another element.  It has not yet been bound to a Selenium WebElement
    /// </summary>
    /// <remarks>
    /// Element will have a parent element, but is not yet bound to a Selemenium WebElement but has been passed its Mapping Details (Find Logic).
    /// </remarks>
    /// <param name="seleniumDriver">The Selenium Driver instance this element will be located in</param>
    /// <param name="MappingDetails">Find Logic and friendly name of element when bound to a Selenium WebElement</param>
    public Element(Element parentElement, ObjectMappingDetails MappingDetails)
    {
        setWebElement(null);
        try { setParentOfThisElement(parentElement); } catch(Exception ex){}
        try { setMappingDetails(MappingDetails); } catch(Exception ex){}
    }


    /// <summary>
    /// Instatiates a new Element as the top-level element in the DOM. Usually called from within the SeleniumDriver <see cref="seleniumDriver.FindElement(FindBy,string)"/> method.
    /// </summary>
    /// <param name="SeleniumDriver">Instantiated and fully initialised Driver</param>
    /// <param name="iWebElement">Raw Selenium element for this element</param>
    /// <param name="FriendlyName">Meaningful text description of element used in error reporting and results</param>
    /// <param name="FindLogic">Logic used to locate this element</param>
    /// <remarks>
    /// All Elements in a test can trace their origin back to a root element.  This enables cacheing (to reduce IP wire traffic
    /// when executing remotely) while enabling the automatic Stale Element resolution, simplifying find logic, etc...
    /// <para/><para/>
    /// Therefore, when a new window has been instantiated the first call should be to the SeleniumDriver.FindElement to establish
    /// the root element.
    /// </remarks>
    public Element(SeleniumDriver SeleniumDriver, WebElement webElement, ObjectMappingDetails MappingDetails)
    {
        setWebElement(webElement);
        try { setParentOfThisElement(SeleniumDriver); } catch(Exception ex){}
        try { setMappingDetails(MappingDetails); } catch(Exception ex){}
    }

    /// <summary>
    /// Instatiates a new Element as a child of an existing Element.
    /// </summary>
    /// <param name="Parent">Parent of this elementr</param>
    /// <param name="iWebElement">Raw Selenium element for this element</param>
    /// <param name="FriendlyName">Meaningful text description of element used in error reporting and results</param>
    /// <param name="FindLogic">Logic used to locate this element</param>
    /// <remarks>
    /// All Elements in a test can trace their origin back to a root element.  This enables cacheing (to reduce IP wire traffic
    /// when executing remotely) while enabling the automatic Stale Element resolution, simplifying find logic, etc...
    /// <para/><para/>
    /// Therefore, when a new window has been instantiated the first call should be to the SeleniumDriver.FindElement to establish
    /// the root element.
    /// </remarks>
    public Element(Element Parent, WebElement webElement, ObjectMappingDetails MappingDetails)
    {
        setWebElement(webElement);
        try { setParentOfThisElement(Parent); } catch(Exception ex){}
        try { setMappingDetails(MappingDetails); } catch(Exception ex){}
    }

    //endregion



}
