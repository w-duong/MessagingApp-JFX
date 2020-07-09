package sample.controller;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import sample.model.Client;
import sample.model.Contact;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static sample.model.Contact.parser;

public class messageMainController implements Initializable
{
    Client client;

    @FXML
    ListView<Contact> messageNow;
    @FXML
    ListView<Contact> messageLater;
    @FXML
    TabPane tabHolder;

    private ObservableList<Contact> onlineNow = FXCollections.observableArrayList();
    private ObservableList<Contact> contacts = FXCollections.observableArrayList();

    public void setClient (Client client) { this.client = client; }

    public void menuNewConnection (ActionEvent event) throws IOException // REQUIRED: creating new Tab may cause exception
    {
        TextInputDialog getContactID = new TextInputDialog("example: FirstName LastName@ContactNumber");
        getContactID.setHeaderText("Enter Contact Info");

        getContactID.showAndWait();
        Contact newPerson = Contact.parser (getContactID.getEditor().getText());
        contacts.add(newPerson);

        refreshList();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../layout/messengerTab.fxml"));
        Tab newConnection = new Tab("Friend-XXX", loader.load());

        tabHolder.getTabs().add(newConnection);
    }

    public void menuClose (ActionEvent event)
    {
        Platform.exit();
        System.exit(0);
    }

    public void refreshList ()
    {
        for (Contact person : contacts)
            if (person.isOnline() && !onlineNow.contains(person))
                onlineNow.add(person);
            else if (!person.isOnline() && onlineNow.contains(person))
                onlineNow.remove(person);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        // sets policy for tabs (method accepts enum of 'TabPane.TabClosingPolicy.xx')
        tabHolder.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // EXAMPLES
        contacts.add(new Contact("William", "Duong", 5840559));
        contacts.add(new Contact ("Jackie", "Duong", 8132974));
        contacts.add(new Contact ("Jennifer", "Duong", 4662459));

        contacts.get(1).setOnline(true);

        messageLater.setItems(contacts);

        refreshList();

        messageNow.setItems(onlineNow);
    }
}
