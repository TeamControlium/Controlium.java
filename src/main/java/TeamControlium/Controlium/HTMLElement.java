package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HTMLElement {

      private ObjectMapping _mappingDetails;
      private WebElement _webElement;
      private Object _parentElementOrDriver;

      private long elementChangeDeltaTimeMs = 200; // Time to wait between samples when checking if an element is changing (IE. moving on screen)


      // PROPERTIES
      public WebElement getWebElement() {return _webElement;}
      public WebElement setWebElement(WebElement webElement) {_webElement = webElement; _mappingDetails = new ObjectMapping(null,String.format("Wired directly to Selenium WebElement [%s]",webElement.toString())); return _webElement;} // Manually wiring to WebElement so we have no mapping details!

      public ObjectMapping getMappingDetails() {return _mappingDetails;}
      public ObjectMapping setMappingDetails(ObjectMapping mappingDetails) {
            if ((mappingDetails != null) && (_webElement != null)) {
                  Logger.WriteLine(Logger.LogLevels.Error,"Trying to set Mapping Details after Element ([%s]) has already been bound (found by Selenium).  So cannot change mapping details.",getFriendlyName());
                  throw new RuntimeException("Cannot set mapping logic after element has been bound.  See log!");
            }
            _mappingDetails = mappingDetails;
            return _mappingDetails;
      }

      // Returns the instance of SeleniumDriver this Element is part of
      public SeleniumDriver getSeleniumDriver() {
            if (getParentOfThisElement() == null) {
                  Logger.WriteLine(Logger.LogLevels.Error,"Trying to get parent Element of [%s]. However, element has no Parent (it has not yet be found!)",getFriendlyName());
                  throw new RuntimeException("Cannot get an instance of the Selenium Driver as this element (or a Parent of) does not have a Parent!");
            }

            // Itterate to the top level element - which is SeleniumDriver type
            if (getParentOfThisElement().getClass() == SeleniumDriver.class) {
                  return (SeleniumDriver)getParentOfThisElement();
            } else {
                  return ((HTMLElement)getParentOfThisElement()).getSeleniumDriver();
            }
      }

      public Object getParentOfThisElement() {
          return _parentElementOrDriver;
      }
      public Object setParentOfThisElement(Object parentOfThisElement) {
          //
          // Make sure that we are only setting the parent to SeleniumDriver or another element.  We assume this within the framework, so lets be sure....
          //
          if (parentOfThisElement == null)
          {
              _parentElementOrDriver = (HTMLElement)null;  //  If it is null, force the issue....
          }
          else
          {
              if ((parentOfThisElement.getClass() != SeleniumDriver.class) &&
                      (parentOfThisElement.getClass() != HTMLElement.class))
                  throw new RuntimeException(String.format("An element can only have another Element or the SeleniumDriver as a parent. Type [%s] is invalid!", parentOfThisElement.getClass().getName()));
              _parentElementOrDriver = parentOfThisElement;
          }
          return getParentOfThisElement();
      }

      public boolean hasAParent() { return (_parentElementOrDriver!=null);}
      public boolean isBoundToAWebElement() { return (_webElement!=null);}

      public boolean isHeightStable(Duration deltaTime) { return !isAttributeChanging("offsetHeight",deltaTime);}
      public boolean isHeightStable() { return isHeightStable(Duration.ofMillis(elementChangeDeltaTimeMs));}

    public boolean isWidthStable(Duration deltaTime) { return !isAttributeChanging("offsetWidth",deltaTime);}
    public boolean isWidthStable() { return isWidthStable(Duration.ofMillis(elementChangeDeltaTimeMs));}

    public boolean isSizeStable(Duration deltaTime) { return !isAttributeChanging(new String[]{"offsetWidth", "offsetHeight"},deltaTime);}
    public boolean isSizeStable() { return isSizeStable(Duration.ofMillis(elementChangeDeltaTimeMs));}

    MAT CARRY ON HERE

    private boolean isAttributeChanging(String attributeName,Duration timeDelta) {
          boolean isChanging;
          try {
              String first = getSeleniumDriver().ExecuteJavaScript(String.class, String.format("return argumaents[0].%s;", attributeName), _webElement);
              try {
                  Thread.sleep(timeDelta.toMillis());
              } catch (Exception e) {
                  Logger.WriteLine(Logger.LogLevels.Error, "Exception sleeping during change monitoring of attribute.");
                  throw new RuntimeException(String.format("Exception sleeping during time stop-start delta", e));
              }
              String second = getSeleniumDriver().ExecuteJavaScript(String.class, String.format("return arguments[0].%s;", attributeName), _webElement);
              isChanging = first.equals(second);
              Logger.WriteLine(Logger.LogLevels.TestDebug, "Element [%s], Attribute [%s], Time Delta [%dmS], First State [%s], Second State [%s] - %s",
                                                                      getFriendlyName(),
                                                                      attributeName,
                                                                      timeDelta.toMillis(),
                                                                      first,
                                                                      second,
                                                                      (isChanging)?"Is changing":"Is not changing");
              return (first.equals(second));
          }
          catch (Exception e) {
              Logger.WriteLine(Logger.LogLevels.Error, "Exception monitoring attribute [%s] of element [%s]: %s", attributeName, this.getFriendlyName(), e.toString());
              throw new RuntimeException(String.format("Exception monitoring element attribute. See log.", e));
          }
      }


    private boolean isAttributeChanging(String[] attributeNames,Duration timeDelta) {
        HashMap<String,String> attributeFirstStates = new HashMap<String,String>();
        boolean isChanging;

        for(String attributeName : attributeNames) {
            attributeFirstStates.put(attributeName,getSeleniumDriver().ExecuteJavaScript(String.class,String.format("return arguments[0].%s;",attributeName),_webElement));
        }

        try {
            Thread.sleep(timeDelta.toMillis());
        } catch (Exception e) {
            Logger.WriteLine(Logger.LogLevels.Error, "Exception sleeping during change monitoring of attribute.");
            throw new RuntimeException(String.format("Exception sleeping during time stop-start delta", e));
        }

        for(Map.Entry<String,String> attributeFirstState : attributeFirstStates.entrySet()) {
            String second = getSeleniumDriver().ExecuteJavaScript(String.class,String.format("return argumaents[0].%s;",attributeFirstState.getKey()),_webElement);
            isChanging = attributeFirstState.getValue().equals(second);

            if(isChanging) {
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
                String.join(", ",attributeNames),
                timeDelta.toMillis());
        return false;
    }


    // METHODS
    public boolean isVisible() {
          return isVisible(false);
    }

    public boolean isVisible(boolean checkIfElementIsInViewport ) {
        ThrowIfUnbound(); // We need this to be bound to an element!
        Logger.WriteLine(Logger.LogLevels.FrameworkInformation,"Verifying if element is visible");
        boolean seleniumStatesElementDisplayed = _webElement.isDisplayed();

        if (checkIfElementIsInViewport && seleniumStatesElementDisplayed) {
            boolean elementWithinViewport = false;
            String sResult = null;
            try {
                sResult = getSeleniumDriver().ExecuteJavaScript(String.class, "var rect = arguments[0].getBoundingClientRect(); return ( rect.top >= 0 && rect.left >= 0 && rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) && rect.right <= (window.innerWidth || document.documentElement.clientWidth));", _webElement);
                return (sResult.trim().toLowerCase()=="true");
            } catch (Exception e) {
                throw new RuntimeException(String.format("Exception executing Javascript to find status of element [%s]", getFriendlyName()));
            }
        } else {
            return checkIfElementIsInViewport;
        }
    }


    public boolean waitForElementHeightStable() {
        return waitForElementHeightStable(null);
    }
    public boolean waitForElementHeightStable(Duration timeout)
    {
        ThrowIfUnbound();
        boolean didStabilzeBeforeTimeout = false;
        int Itterations = 0;
        Duration actualTimeout = (timeout==null) ? getSeleniumDriver().getElementFindTimeout() : timeout;
        long actualTimeoutMillis = actualTimeout.toMillis();

        Logger.WriteLine(Logger.LogLevels.FrameworkInformation, "Wait %dms for element [%s] to become height stable", actualTimeoutMillis, getFriendlyName());

        StopWatch timeWaited = StopWatch.createStarted();
        while (timeWaited.getTime(TimeUnit.MILLISECONDS) < actualTimeoutMillis)
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



    /// <summary>
    /// Throws exception if Element is not bound to a Selenium IWebElement.
    /// </summary>
    private void ThrowIfUnbound()
    {
        if (!hasAParent())
            throw new RuntimeException("Cannot identify Selenium Driver as no parent set (SeleniumDriver or Element)!");
        if (!isBoundToAWebElement())
            throw new RuntimeException("Not bound to a Selenium Web Element");
    }

    public String getFriendlyName() {
          ObjectMapping objectMapping = getMappingDetails();

          if (objectMapping==null) {
              return "No mapping details for element!";
          } else {
              return objectMapping.getFriendlyName();
          }
    }

    //////////////////// STATICS
    /// <summary>Tests if element is currently visible to the user.</summary>
    /// <param name="Element">Element to test</param>
    /// <returns>True if visible, false if not (or if element is null)</returns>
    /// <remarks>
    /// This uses the Selenium Displayed boolean property.
    /// </remarks>
    public static boolean isVisible(HTMLElement element) { return isVisible(element,false);}
    public static boolean isVisible(HTMLElement element, boolean CheckIfElementIsInViewport)
    {
        return element.isVisible(CheckIfElementIsInViewport);
    }


}
