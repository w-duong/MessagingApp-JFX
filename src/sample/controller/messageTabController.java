package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import sample.model.Client;
import sample.model.CustomTab;

import java.util.HashMap;

public class messageTabController
{
    private Client client;
    private TabPane tabHolder;

    @FXML
    TextField inputMessageField;
    @FXML
    TextFlow ChatLog;

    public void setClient (Client client) { this.client = client; }
    public void setTabHolder (TabPane tabHolder) { this.tabHolder = tabHolder; }

    public void mouseSendButton (MouseEvent mouseEvent)
    {
        System.out.println (tabHolder.getTabs().size());
    }
}
