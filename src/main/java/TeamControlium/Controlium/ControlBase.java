package TeamControlium.Controlium;


import TeamControlium.Utilities.Logger;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ControlBase {
    // DELEGATES
//    private delegate Element ControlFindElement(ObjectMappingDetails findLogic);
    /// <summary>
    /// System delegate to do FindElement at a Control level
    /// </summary>
    private Function<Element,ObjectMappingDetails> _ControlFindElement;
    public Function<Element,ObjectMappingDetails> getControlFindElement() { return _ControlFindElement;}
    public void setTestToolLog(Function<Element,ObjectMappingDetails> value) { _ControlFindElement=value;}


    /// <summary>Custom Parent element of this control</summary>
    private Element _RootElement;
    protected Element setRootElement(Element value) { _RootElement = value; return _RootElement;}
    protected Element getRootElement() { return _RootElement;}

    /// <summary>Find logic used to locate this control from the root or Parent control</summary>
    public ObjectMappingDetails getMapping() { return _RootElement.getMappingDetails(); }


    /// <summary>
    /// Tests is controls root element is stale
    /// </summary>
    public boolean IsStale() {
        try {
            //
            // We do a dummy action to force Selenium to report any stagnantation....
            //
            boolean dummy = RootElement.IsEnabled();
            return false;
        }
        catch (Exception ex) {
            Logger.LogException(ex);
            return true;
        }
    }

    // PROPERTIES
    /// <summary>This controls Parent.  If a root control this is null</summary>
    public ControlBase ParentControl { get; private set; }

    /// <summary>Root element of this control</summary>
    public Element RootElement { get { if (_RootElement == null) throw new Exception($"Control [{Mapping?.FriendlyName ?? "Not Set!!?"}] root element NULL.  Has control been located on the page (IE. SetControl(...)?"); else { return _RootElement; } } protected set { _RootElement = value; } }

    /// <summary>Reference to the SeleniumDriver</summary>
    public SeleniumDriver SeleniumDriver { get; private set; }

    /// <summary>
    /// Clear the control cache
    /// </summary>
    public static void ClearCache()
    {
        ControlCache.ClearCache();
    }

    protected void SetRootElement(ObjectMappingDetails mappingDetails)
    {
        RootElement = new Element(mappingDetails);
    }



    /// <summary>Sets on a child control with the childs find logic being applied this controls root element.  Method can be overriden by control implementations.
    /// </summary>
    /// <param name="NewControl">Control to be located</param>
    /// <returns>Reference to located control (or cached reference.)</returns>
    public virtual T SetControl<T>(T NewControl) where T : ControlBase
    {
        return SetControl(this, NewControl);
    }

    /// <summary>Instantiates a new TeamControlium Control as a child of the passed ParentControl. If no parent, Control is top-level.
    /// </summary>
    /// <param name="SeleniumDriver">Reference to TeamControlium Driver</param>
    /// <param name="ParentControl">Control the instantiated control is a child of. Can be null if new control is a top-level Control (IE. A function/pattern)</param>
    /// <param name="CustomRootElement">A dynamic Element the child control may be referenced from.  Can be null if child's find logic is from the parents root</param>
    /// <param name="NewControl">New control instance with find logic and friendly name set</param>
    /// <remarks>If control was sucessfully located, caching is not disabled and the control is cached, the cached reference is returned instead of the located.  If a cache miss the reference is
    /// added.</remarks>
    /// <returns>Reference to found control (or cached - see Remarks)</returns>
    public static T SetControl<T>(SeleniumDriver SeleniumDriver, ControlBase ParentControl, T NewControl) where T : ControlBase
    {
        Stopwatch stopWatch = Stopwatch.StartNew();
        try
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, "Setting on control [{0}] from parent [{1}]", NewControl == null ? "<No control>" : NewControl.Mapping?.FriendlyName?? NewControl.Mapping?.FindLogic??"No find logic!!", (ParentControl == null) ? "<No parent - so top level>" : ParentControl.Mapping?.FriendlyName?? ParentControl.Mapping?.FindLogic??"Paraent has no find logic!!");
            // Find the element - we can assume it will be good as any issue will have chucked an exception
            if ((ParentControl != null) && ParentControl.IsStale)
            {
                Logger.WriteLine(Logger.LogLevels.TestInformation, "Parent control is stale. Refreshing");
                ParentControl.RootElement = null;
                ControlBase RefreshedParentControl = ControlBase.SetControl(ParentControl.SeleniumDriver, ParentControl.ParentControl, ParentControl);
                ParentControl = RefreshedParentControl;
            }

            //
            // We may just be wrapping an Element in a Control that has already been found.  In which case, dont bother
            // to do a find for it....
            //
            if (NewControl?._RootElement.WebElement == null)
            {
                Logger.WriteLine(Logger.LogLevels.TestDebug, $"New control Root is null (It has not yet been found), so finding (Find logic: [{NewControl?.Mapping?.FindLogic ?? "Null!!!"}]");
                ControlFindElement finder = FindToUse(SeleniumDriver, ParentControl);

                Element Element = FindControlRootElement(finder, NewControl.Mapping);

                NewControl.RootElement = Element;
            }

            //
            // Populate new Control object.
            //
            NewControl.SeleniumDriver = SeleniumDriver;
            NewControl.ParentControl = ParentControl; // This may be null.  So, new control is top level....

            // Put the control through the cache - either adds it to the cache (miss) or references the cache version (hit)
            // Inform the control we have performed a 'set' on it letting it know whether this is the first or subsequent times.  Note
            // that caching has been DISABLED for development of the framework purposes. Check will return false if the control EXISTS OR NOT!
            switch (ControlCache.Check(ref NewControl))
            {
                case ControlCache.ControlCacheStates.CacheHit:
                {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, "Using cached control");
                    NewControl.ControlBeingSet(false);
                    break;
                }
                case ControlCache.ControlCacheStates.CacheMiss:
                {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, "Control not cached - new control used");
                    NewControl.ControlBeingSet(true);
                    break;
                }
                case ControlCache.ControlCacheStates.CachedControlWasStale:
                {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, "Control stale - all cached controls invalidated and new control used");
                    NewControl.ControlBeingSet(true);
                    break;
                }
                default:
                {
                    Logger.WriteLine(Logger.LogLevels.TestInformation, "Control caching disabled");
                    NewControl.ControlBeingSet(true);
                    break;
                }
            }
            return NewControl;
        }
        catch (Exception ex)
        {
            // Be worth sticking a log in here
            throw ex;
        }
    }

    /// <summary>Instantiates a new TeamControlium Control as a child of the passed ParentControl. If no parent, Control is top-level.
    /// </summary>
    /// <param name="SeleniumDriver">Reference to TeamControlium Driver</param>
    /// <param name="ParentControl">Control the instantiated control is a child of. Can be null if new control is a top-level Control (IE. A function/pattern)</param>
    /// <param name="NewControl">New control instance with find logic and friendly name set</param>
    /// <remarks>If control was sucessfully located, caching is not disabled and the control is cached, the cached reference is returned instead of the located.  If a cache miss the reference is
    /// added.</remarks>
    /// <returns>Reference to found control (or cached - see Remarks)</returns>
    public static T SetControl<T>(ControlBase ParentControl, T NewControl)
    where T : ControlBase
    {
        return SetControl(ParentControl.SeleniumDriver, ParentControl, NewControl);
    }


    /// <summary>Instantiates a new root TeamControlium Control
    /// </summary>
    /// <param name="SeleniumDriver">SeleniumDriver reference</param>
    /// <remarks>If control was sucessfully located, caching is not disabled and the control is cached, the cached reference is returned instead of the located.  If a cache miss the reference is
    /// added.</remarks>
    /// <returns>Reference to found control (or cached - see Remarks)</returns>
    public static T SetControl<T>(SeleniumDriver SeleniumDriver, T NewControl) where T : ControlBase
    {
        return SetControl(SeleniumDriver, null, NewControl);
    }

    /// <summary>Sends a clear event to an element within the control.  Find logic must work from the top-element of the control.
    /// </summary>
    /// <param name="findLogic">Find logic to element that is to be cleared</param>
    /// <param name="friendlyName">Name of element</param>
    public void ClearElement(ObjectMappingDetails findLogic, string friendlyName)
    {
        FindElement(findLogic).Clear();
    }


    /// <summary>Send a click (or Tap) event to the root element of the control.
    /// <para/><para/>
    /// Implementors may override to route click event to the appropriate element in the control. If not overridden,
    /// click event is targeted to the controls root element.
    /// </summary>
    public virtual void Click()
    {
        Logger.WriteLine(Logger.LogLevels.TestDebug, "Clicking element [{0}]",RootElement?.MappingDetails?.FriendlyName?? RootElement?.MappingDetails?.FindLogic??"Dunno!");
        RootElement.Click();
    }

    /// <summary>Sends a click event to an element within the control.  Find logic must work from the top-element of the control.
    /// </summary>
    /// <param name="findLogic">Find logic for element to be clicked</param>
    /// <param name="friendlyName">Name of element to clicked</param>
    public void ClickElement(ObjectMappingDetails findLogic)
    {
        FindElement(findLogic).Click();
    }

    /// <summary>Sends a click event to an element within the control if that element is exists.  Find logic must work from the top-element of the control.
    /// Does not throw an error if the element does not exist or is not visible.  If element does not exist, nothing is done and no error thrown.
    /// </summary>
    /// <param name="findLogic">Find logic to element</param>
    /// <param name="friendlyName">Name of element</param>
    public void ClickElementIfExists(ObjectMappingDetails findLogic)
    {
        Element element;
        if (RootElement.TryFindElement(findLogic, out element))
        {
            element.Click();
        }
        else
        {
            Logger.WriteLine(Logger.LogLevels.TestInformation, $"[{findLogic.FindLogic}] didnt find any match.  No click.");
        }
    }

    // METHODS
    /// <summary>A virtual method that Controls inheriting this CoreLib_Control can override.  The method is called
    /// when SetControl has identified the new control's root element and has populated the control's metadata.
    /// </summary>
    /// <param name="IsFirstSetting">
    /// Indicates if this is the first time the control has been located and instantiated (true)
    /// Or if the control has been used before in this test and has just had the IWebElement refreshed.
    /// </param>
    public virtual void ControlBeingSet(bool IsFirstSetting)
    {
    }


    /// <summary>Gets the attribute from an element within the control.  If element does not exist a NotFound exception is thrown. If attribute does not exist an empty string is returned
    /// </summary>
    /// <param name="findLogic">Find logic reletive to the root element of the control</param>
    /// <param name="friendlyName">Friendly name of the element the text is coming from</param>
    /// <param name="attributeName">Attribute to get contents of</param>
    /// <returns>Contenst of attribute</returns>
    public string GetAttribute(ObjectMappingDetails findLogic, string attributeName)
    {
        Element element = FindElement(findLogic);
        try
        {
            return element.GetAttribute(attributeName);
        }
        catch
        {
            return string.Empty;
        }
    }

    public string GetAttribute(string attributeName)
    {
        try
        {
            return RootElement.GetAttribute(attributeName);
        }
        catch
        {
            return string.Empty;
        }
    }


    /// <summary>Gets the text from an element within the control.  If element does not exist, exception is thrown.
    /// </summary>
    /// <param name="findLogic">Find logic reletive to the root element of the control</param>
    /// <param name="friendlyName">Friendly name of the element the text is coming from</param>
    /// <returns>text in identified element (uses Element.GetText)</returns>
    public string GetText(ObjectMappingDetails findLogic, string friendlyName)
    {
        Element element = FindElement(findLogic);
        return element.GetText();
    }

    /// <summary>Gets the text from root element of control.
    /// </summary>
    /// <returns>text in control root element (uses Element.GetText)</returns>
    public string GetText()
    {
        return RootElement.GetText();
    }


    /// <summary>Returns true if element has defined attribute and false if not.  If element does not exist an exception is thrown
    /// </summary>
    /// <param name="findLogic">Find logic of the element, reletive to the root element of the control</param>
    /// <param name="friendlyName">Friendly name of the element to be checked</param>
    /// <param name="attributeName">Attribute to see if exists</param>
    /// <returns>true if attributeName exists, false if not</returns>
    public bool HasAttribute(ObjectMappingDetails findLogic, string friendlyName, string attributeName)
    {
        Element element = FindElement(findLogic);
        try
        {
            element.FindElement(new ObjectMappingDetails(string.Format(".[@{0}]", $"Attribute [{attributeName}] of {findLogic.FriendlyName ?? findLogic.FindLogic}")));
            return true;
        }
        catch
        {
            return false;
        }
    }

    public bool HasAttribute(string attributeName)
    {
        try
        {
            RootElement.FindElement(new ObjectMappingDetails(string.Format(".[@{0}]", $"Attribute [{attributeName}] of {RootElement.MappingDetails.FriendlyName ?? RootElement.MappingDetails.FindLogic}")));
            return true;
        }
        catch
        {
            return false;
        }
    }




    /// <summary>Set the text from an element within the control
    /// </summary>
    /// <param name="findLogic">Find logic reletive to the root element of the control</param>
    /// <param name="friendlyName">Friendly name of the element the text is coming from</param>
    /// <param name="text">text to set</param>
    public virtual void SetText(ObjectMappingDetails mapping, string friendlyName, string text)
    {
        FindElement(mapping).SetText(text);
    }

    /// <summary>
    /// Set the text in the root element of the control
    /// </summary>
    /// <param name="text">text to set</param>
    public virtual void SetText(string Text)
    {
        RootElement.SetText(Text);
    }


    private static Element FindControlRootElement(ControlFindElement FindMechanism, ObjectMappingDetails FindLogic)
    {
        Element returnValue;
        returnValue = FindMechanism(FindLogic);
        if (returnValue == null)
        {
            throw new Exception("Unexected error!  Find element returned null element.  See Debug log.");
        }
        return returnValue;
    }

    private static ControlFindElement FindToUse(SeleniumDriver Driver, ControlBase ParentControl)
    {
        string DebugText = string.Empty;
        ControlFindElement findToUse;
        if (ParentControl == null)
        {
            // We have no parent.  So we call the SeleniumDriver FindElement to search the whole DOM.
            DebugText = "Top Level (IE. DOM) so using driver find";
            findToUse = Driver.FindElement;
        }
        else
        {
            // Parent control is good and not using Custom root element.  So FindElement from the parent's root
            DebugText = "Has parent control so Root element of Parent Control";
            findToUse = ParentControl.RootElement.FindElement;
        }
        Logger.WriteLine(Logger.LogLevels.FrameworkDebug, "Find to use: " + DebugText);
        return findToUse;
    }

    public Element FindElement(ObjectMappingDetails findLogic)
    {
        return RootElement.FindElement(findLogic);
    }

    public bool WaitUntilStable(int timeoutMilliseconds=5000)
    {
        return RootElement.WaitForElementPositionStable(TimeSpan.FromMilliseconds(timeoutMilliseconds));
    }

    /// <summary>
    /// Indicates if control's root element is visible.  Will return true even if control is scrolled out
    /// of view or visually obscured by another element
    /// </summary>
    public bool IsVisible
    {
        get
        {
            if (RootElement!=null && RootElement.WebElement!=null)
            {
                return RootElement.Visible();
            }
            else
            {
                throw new Exception(string.Format("Cannot get visibility status of [{0}]as Root Element has not be found!", Mapping.FriendlyName ?? Mapping.FindLogic ?? "find logic null!"));
            }
        }
    }

    private bool IsToken(string PossibleToken)
    {
        // Tokens start and end with & - this is the usual with tools as well....
        return (PossibleToken.StartsWith("&") && PossibleToken.EndsWith("&"));
    }

}

}
