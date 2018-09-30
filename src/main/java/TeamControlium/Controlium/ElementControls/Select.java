package TeamControlium.Controlium.ElementControls;

import TeamControlium.Controlium.ControlBase;
import TeamControlium.Controlium.ObjectMapping;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class Select extends ControlBase {

    private boolean delectAllOnSetting=false;
    public Select(ObjectMapping mapping) {
        setMapping(mapping);
    }
    public Select(ObjectMapping mapping,boolean startAllDeselected) {
        setMapping(mapping);
        delectAllOnSetting=startAllDeselected;
    }
    protected void controlBeingSet(boolean isFirstSetting) {
        if (delectAllOnSetting) {
            new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).deselectAll();
        }
    }

    public void selectItemByText(String text) {
        new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).selectByVisibleText(text);
    }

    public void selectItemByValueAttribute(String text) {
        new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).selectByValue(text);
    }

    public void selectItemByIndex(int index) {
        org.openqa.selenium.support.ui.Select selectElement = new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement());
        List<WebElement> allOptions = selectElement.getOptions();
        if (allOptions.size()<index+1) {
            throw new RuntimeException(String.format("Select element [%s] only has [%d] options but we wanted to select option [%d] (Zero based)",getMapping().getFriendlyName(),allOptions.size()));
        }
        selectElement.selectByIndex(index);
    }


    public String getSelectedItemText() {
        List<WebElement> elements = new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).getAllSelectedOptions();
        if (elements.size()==0) {
            throw new RuntimeException(String.format("Select element [%] has no selected items",getMapping().getFriendlyName()));
        }
        if (elements.size()>1) {
            throw new RuntimeException(String.format("Select element [%] has [%d] selected items.  Expected 1",elements.size(),getMapping().getFriendlyName()));
        }
        return elements.get(0).getText();
    }

    public int getSelectedItemTextsCount() {
        List<WebElement> elements = new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).getAllSelectedOptions();
        if (elements.size()==0) {
            throw new RuntimeException(String.format("Select element [%] has no selected items",getMapping().getFriendlyName()));
        }
        return elements.size();
    }

    public List<String> getAllSelectedItemTexts() {
        List<WebElement> elements = new org.openqa.selenium.support.ui.Select((WebElement)getRootElement().getUnderlyingWebElement()).getAllSelectedOptions();
        if (elements.size()==0) {
            throw new RuntimeException(String.format("Select element [%] has no selected items",getMapping().getFriendlyName()));
        }
        List<String> texts = elements.stream().map(element -> element.getText()).collect(Collectors.toList());
        return texts;
    }
}
