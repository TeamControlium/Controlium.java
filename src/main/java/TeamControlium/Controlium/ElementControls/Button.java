package TeamControlium.Controlium.ElementControls;

import TeamControlium.Controlium.ControlBase;
import TeamControlium.Controlium.ObjectMapping;

public class Button extends ControlBase {

    public Button(ObjectMapping mapping) {
        setMapping(mapping);
    }

    public Button(String text) { setMapping(new ObjectMapping(String.format(".//button[.='%s']",text)));}

    protected void controlBeingSet(boolean isFirstSetting) {
    }


}
