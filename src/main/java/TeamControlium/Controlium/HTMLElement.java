package TeamControlium.Controlium;

import TeamControlium.Utilities.Logger;
import org.openqa.selenium.WebElement;

public class HTMLElement {

      private ObjectMapping _mappingDetails;
      private WebElement _webElement;
      private Object _parentElementOrDriver;


      // PROPERTIES
      public WebElement getWebElement() {return _webElement;}
      public WebElement setWebElement(WebElement webElement) {_webElement = webElement; _mappingDetails = new ObjectMapping(null,String.format("Wired directly to Selenium WebElement [%s]",webElement.toString())); return _webElement;} // Manually wiring to WebElement so we have no mapping details!

      public ObjectMapping getMappingDetails() {return _mappingDetails;}
      public ObjectMapping setMappingDetails(ObjectMapping mappingDetails) {
            if ((mappingDetails != null) && (_webElement != null)) {
                  Logger.WriteLine(Logger.LogLevels.Error,"Trying to set Mapping Details after Element ([%s]) has already been bound (found by Selenium)",getFriendlyName());
                  throw new RuntimeException("Cannot set mapping logic after element has been bound.  See log!");
            }
            _mappingDetails = mappingDetails;
            return _mappingDetails;
      }


      // So, carry on here. from c# Element.cs at the mo....

}
