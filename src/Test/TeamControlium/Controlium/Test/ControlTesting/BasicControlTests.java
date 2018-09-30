package TeamControlium.Controlium.Test.ControlTesting;

import TeamControlium.Controlium.ControlBase;
import TeamControlium.Controlium.ElementControls.*;
import TeamControlium.Controlium.HTMLElement;
import TeamControlium.Controlium.ObjectMapping;
import TeamControlium.Controlium.SeleniumDriver;
import TeamControlium.Utilities.Logger;
import TeamControlium.Utilities.TestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BasicControlTests {

    private SeleniumDriver seleniumDriver=null;
    @BeforeEach
    void setUp() {
        TestData.setItem("Debug", "TakeScreenshot", false);
        TestData.setItem("Selenium", "SeleniumServerFolder", "./TestServer");
        if (seleniumDriver!=null) seleniumDriver.CloseDriver();
        seleniumDriver = new SeleniumDriver(true);
    }

    @AfterEach
    void tearDown() {
        if (seleniumDriver!=null) seleniumDriver.CloseDriver();
    }


    // Verify we can Set on an Input control
    @org.junit.jupiter.api.Test
    void VerifyBasicFindElement() {
        seleniumDriver.gotoURL("https://www.thecodingguys.net/tutorials/html/html-input-element");
        Input inputControl=null;
        try {
            inputControl = new Input(new ObjectMapping("//input[@name='s']","Search Input textbox"));
            inputControl = ControlBase.setControl(seleniumDriver,inputControl);
            //inputControl.setText("Hello");
            //String readBack = inputControl.getText();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Input control: ",e.getMessage());
        }
        assertNotNull(inputControl,"Input control successfully Set on");
    }

    // Verify we can select an item in a Select control using visible text
    @org.junit.jupiter.api.Test
    void VerifySelectingAnItemByTextInSelectControl() {
        seleniumDriver.gotoURL("https://html.com/attributes/option-selected/");
        Select selectControl=null;
        String itemToSelect="Lesser flamingo";
        String selectedItemText=null;
        try {
            selectControl = new Select(new ObjectMapping("//select[@name='favorites']","Example Favorites dropdown"));
            selectControl = ControlBase.setControl(seleniumDriver,selectControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control: ",e.getMessage());
        }
        try {
            selectControl.selectItemByText(itemToSelect);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        try {
            selectedItemText = selectControl.getSelectedItemText();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(itemToSelect,selectedItemText,"Select item and verify item text is as selected");
    }


    // Verify we can select an item in a Select control using the Value attribute
    @org.junit.jupiter.api.Test
    void VerifySelectingAnItemByValueInSelectControl() {
        seleniumDriver.gotoURL("https://html.com/attributes/option-selected/");
        Select selectControl=null;
        String itemToSelectText="Lesser flamingo";
        String itemToSelectValue="Lesser";
        String selectedItemText=null;
        try {
            selectControl = new Select(new ObjectMapping("//select[@name='favorites']","Example Favorites dropdown"));
            selectControl = ControlBase.setControl(seleniumDriver,selectControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control using text: ",e.getMessage());
        }
        try {
            selectControl.selectItemByValueAttribute(itemToSelectValue);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control using value attribute: ",e.getMessage());
        }
        try {
            selectedItemText = selectControl.getSelectedItemText();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(itemToSelectText,selectedItemText,"Select item and verify item text is as selected");
    }

    // Verify we can select an item in a Select control using the Value attribute
    @org.junit.jupiter.api.Test
    void VerifySelectingAnItemByIndexInSelectControl() {
        seleniumDriver.gotoURL("https://html.com/attributes/option-selected/");
        Select selectControl=null;
        String itemToSelectText="Lesser flamingo";
        int itemToSelectIndex=2;
        String selectedItemText=null;
        try {
            selectControl = new Select(new ObjectMapping("//select[@name='favorites']","Example Favorites dropdown"));
            selectControl = ControlBase.setControl(seleniumDriver,selectControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control using text: ",e.getMessage());
        }
        try {
            selectControl.selectItemByIndex(itemToSelectIndex);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control using index: ",e.getMessage());
        }
        try {
            selectedItemText = selectControl.getSelectedItemText();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(itemToSelectText,selectedItemText,"Select item and verify item text is as selected");
    }

    // Verify we can select multiple items in a Mult-Select control using visible texts
    @org.junit.jupiter.api.Test
    void VerifySelectingMultipleItemsByTextInSelectControlHasCorrectCount() {
        seleniumDriver.gotoURL("https://html.com/attributes/select-multiple/");
        Select selectControl=null;
        String item1ToSelect="Lesser flamingo";
        String item2ToSelect="Greater flamingo";
        String item3ToSelect="Andean flamingo";
        int numberOfSelectedItems=3;
        int selectedItemsCount=-1;
        try {
            selectControl = new Select(new ObjectMapping("//select[@multiple]","Example Favorites dropdown"));
            selectControl = ControlBase.setControl(seleniumDriver,selectControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control: ",e.getMessage());
        }
        try {
            selectControl.selectItemByText(item1ToSelect);
            selectControl.selectItemByText(item2ToSelect);
            selectControl.selectItemByText(item3ToSelect);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        try {
            selectedItemsCount = selectControl.getSelectedItemTextsCount();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(numberOfSelectedItems,selectedItemsCount,"Select multiple items and verify selected item count is correct");
    }


    // Verify we can select multiple items in a Mult-Select control using visible texts
    @org.junit.jupiter.api.Test
    void VerifySelectingMultipleItemsByTextInSelectControlHasCorrectTexts() {
        seleniumDriver.gotoURL("https://html.com/attributes/select-multiple/");
        Select selectControl=null;
        String item1ToSelect="Lesser flamingo";
        String item2ToSelect="Greater flamingo";
        String item3ToSelect="Andean flamingo";
        String item1NotSelected="American flamingo";
        String item2NotSelected="Chilean flamingo";
        String item3NotSelected="James's flamingo";
        int numberOfSelectedItems=3;
        List<String> texts = new ArrayList<>();
        try {
            selectControl = new Select(new ObjectMapping("//select[@multiple]","Example Favorites dropdown"));
            selectControl = ControlBase.setControl(seleniumDriver,selectControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control: ",e.getMessage());
        }
        try {
            selectControl.selectItemByText(item1ToSelect);
            selectControl.selectItemByText(item2ToSelect);
            selectControl.selectItemByText(item3ToSelect);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        try {
            texts = selectControl.getAllSelectedItemTexts();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(numberOfSelectedItems,texts.size(),"Select multiple items and verify selected item count is correct");
        assertEquals(1,texts.stream().filter(text -> (item1ToSelect.equals(text))).count(),"Select multiple items has item 1 selected ok");
        assertEquals(1,texts.stream().filter(text -> (item2ToSelect.equals(text))).count(),"Select multiple items has item 2 selected ok");
        assertEquals(1,texts.stream().filter(text -> (item3ToSelect.equals(text))).count(),"Select multiple items has item 3 selected ok");
        assertEquals(0,texts.stream().filter(text -> (item1NotSelected.equals(text))).count(),"Select multiple items has non-selected item 1 not selected ok");
        assertEquals(0,texts.stream().filter(text -> (item2NotSelected.equals(text))).count(),"Select multiple items has non-selected item 2 not selected ok");
        assertEquals(0,texts.stream().filter(text -> (item3NotSelected.equals(text))).count(),"Select multiple items has non-selected item 3 not selected ok");
    }


    // Verify controlBeingSet on SetControl
    @org.junit.jupiter.api.Test
    void VerifySelectResetControl() {
        seleniumDriver.gotoURL("https://html.com/attributes/select-multiple/");
        Select selectControl1=null;
        Select selectControl2=null;
        String item1ToSelect="Lesser flamingo";
        String item2ToSelect="Greater flamingo";
        String item3ToSelect="Andean flamingo";
        String item1SecondSelect="American flamingo";
        String item2SecondSelect="Andean flamingo";
        String item1NotSelected="Chilean flamingo";
        int numberOfSelectedItems=2;
        List<String> texts = new ArrayList<>();
        try {
            selectControl1 = new Select(new ObjectMapping("//select[@multiple]","Example Favorites dropdown"));
            selectControl1 = ControlBase.setControl(seleniumDriver,selectControl1);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Select control: ",e.getMessage());
        }
        try {
            selectControl1.selectItemByText(item1ToSelect);
            selectControl1.selectItemByText(item2ToSelect);
            selectControl1.selectItemByText(item3ToSelect);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        try {
            selectControl2 = new Select(new ObjectMapping("//select[@multiple]","Example Favorites dropdown"),true);
            selectControl2 = ControlBase.setControl(seleniumDriver,selectControl2);

        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        try {
            selectControl1.selectItemByText(item1SecondSelect);
            selectControl1.selectItemByText(item2SecondSelect);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown selecting item from Select control: ",e.getMessage());
        }
        assertEquals(numberOfSelectedItems,texts.size(),"Select multiple items and verify selected item count is correct");
        assertEquals(1,texts.stream().filter(text -> (item1SecondSelect.equals(text))).count(),"Select multiple items has item 1 selected ok");
        assertEquals(1,texts.stream().filter(text -> (item2SecondSelect.equals(text))).count(),"Select multiple items has item 2 selected ok");
        assertEquals(0,texts.stream().filter(text -> (item1ToSelect.equals(text))).count(),"Select multiple items has non-selected item 1 not selected ok");
        assertEquals(0,texts.stream().filter(text -> (item2ToSelect.equals(text))).count(),"Select multiple items has non-selected item 2 not selected ok");
        assertEquals(0,texts.stream().filter(text -> (item1NotSelected.equals(text))).count(),"Select multiple items has non-selected item 2 not selected ok");
    }

    // Verify we can select a basic table cell
    @org.junit.jupiter.api.Test
    void VerifyBasicFindTableCell() {
        seleniumDriver.gotoURL("https://html.com/tables/");
        int row = 3;
        int column = 2;
        Table tableControl=null;
        String cellContents=null;
        try {
            tableControl = new Table("Caption","A complex table");
            tableControl = ControlBase.setControl(seleniumDriver,tableControl);
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown setting on Table control: %s ",e.getMessage());
        }
        try {
            Table.Cell cell = tableControl.getCell(row,column);
            cellContents = cell.getText();
        }
        catch( Exception e) {
            Logger.WriteLine(Logger.LogLevels.TestInformation,"Error thrown getting [%s] table cell (Row [%d], Column [%d]): %s",
                    tableControl.getMapping().getFriendlyName(),
                    row,
                    column,
                    e.getMessage());
        }
        assertEquals("0.01",cellContents,"Table cell contents correctly read");
    }

}

