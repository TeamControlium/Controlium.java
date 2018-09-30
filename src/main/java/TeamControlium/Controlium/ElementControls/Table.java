package TeamControlium.Controlium.ElementControls;

import TeamControlium.Controlium.ControlBase;
import TeamControlium.Controlium.HTMLElement;
import TeamControlium.Controlium.ObjectMapping;

import java.util.List;

public class Table extends ControlBase {

    public Table(ObjectMapping mapping) {
        setMapping(mapping);
    }

    public Table(String property, String logic) {

        ObjectMapping mapping=null;

        switch (property.trim().toLowerCase()) {
            case "caption":
                String normalizedLogic = logic.trim();
                mapping = new ObjectMapping(String.format(".//table[./caption[.='%s']]",normalizedLogic),String.format("Table with caption [%s]",property));
                break;
            default:
                throw new RuntimeException(String.format("Unknown property [%s] to find table",property));
        }
        setMapping(mapping);
    }

    protected void controlBeingSet(boolean isFirstSetting) {
    }

    public Cell getCell(int row, int column) {
        List<HTMLElement> rows = findAllElements(new ObjectMapping("./*/tr","All table rows"));
        if (rows.size()<row+1) {
            throw new RuntimeException(String.format("Table [%s] has [%d] rows but row index is [%d] (Zero based)",getMapping().getFriendlyName(),rows.size(),row));
        }
        List<HTMLElement> columns = findAllElements(new ObjectMapping(String.format("(./*/tr)[%d]/td",row+1),String.format("All columns on row [%d] (Zero based)",row)));
        if (columns.size()<column+1) {
            throw new RuntimeException(String.format("Table [%s] has [%d] columns on row [%d] but column index is [%d] (All indexes zero based)",
                    getMapping().getFriendlyName(),
                    columns.size(),
                    row,
                    column));
        }
        HTMLElement element = columns.get(column);

        return new Cell(columns.get(column));
    }


    public class Cell extends ControlBase
    {
       HTMLElement cellElement;

        protected void controlBeingSet(boolean isFirstSetting) {
        }


        public Cell(HTMLElement element) {
           setMapping(element.getMappingDetails());
           setRootElement(element);
       }
    }
}
