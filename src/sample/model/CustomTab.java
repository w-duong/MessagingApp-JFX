package sample.model;

import javafx.scene.Node;
import javafx.scene.control.Tab;

public class CustomTab extends Tab
{
    public static String tabIdentifier;

    public CustomTab (String title, Node node, int identifier)
    {
        super(title, node);
        setTabIdentifier(identifier);
    }

    public void setTabIdentifier (String identifier) { this.tabIdentifier = identifier; }
    public void setTabIdentifier (int identifier) { this.tabIdentifier = Integer.toString(identifier); }
    public String getTabIdentifier () { return this.tabIdentifier; }
}
