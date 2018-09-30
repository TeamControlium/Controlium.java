package TeamControlium.Controlium.ElementControls;

import TeamControlium.Controlium.ControlBase;
import TeamControlium.Controlium.ObjectMapping;

public class Anchor extends ControlBase {

    public Anchor(ObjectMapping mapping) {
        setMapping(mapping);
    }

    public Anchor(String text) { setMapping(new ObjectMapping(String.format(".//a[.='%s']",text)));}

    protected void controlBeingSet(boolean isFirstSetting) {
    }


}
