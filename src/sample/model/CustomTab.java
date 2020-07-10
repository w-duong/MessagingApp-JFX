package sample.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.TextFlow;

/* Custom Class only needed as wrapper to allow access to Client (for communications over Socket)
and TabPane (for access to identifiers within Tab group) */
public class CustomTab extends Tab
{
    public static Client client;
    public static TabPane tabPane;

    public CustomTab (String title, Node node, TabPane tabPane, Client client)
    {
        super(title, node);
        this.tabPane = tabPane;
        this.client = client;
    }
}
