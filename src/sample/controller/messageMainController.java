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
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import sample.model.Client;
import sample.model.Contact;
import sample.model.CustomTab;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

    public void menuNewConnection (ActionEvent event) throws IOException // REQUIRED: ...even when calling a function that throws
    {
        TextInputDialog getContactID = new TextInputDialog("example: FirstName LastName@ContactNumber");
        getContactID.setResizable(true);
        getContactID.setHeaderText("Enter Contact Info");

        getContactID.showAndWait();
        Contact newPerson = Contact.parser (getContactID.getEditor().getText());

        newConnection (newPerson);
    }

    public void menuClose (ActionEvent event)
    {
        client.sendMessage("EXIT");

        Platform.exit();
        System.exit(0);
    }

    public void newConnection (Contact person) throws IOException // REQUIRED: creating new Tab may cause exception
    {
        person.setOnline(true);
        if (!contacts.contains(person))
            contacts.add(person);

        refreshList();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("messengerTab.fxml"));
        CustomTab newConnection = new CustomTab(person.getFirstName(), loader.load(), tabHolder, client);
        newConnection.setId(person.getContactNumber());

        tabHolder.getTabs().add(newConnection);

        client.setTabPane(tabHolder);
        client.setContacts(contacts);
        client.setOnlineNow(onlineNow);
    }

    public void messageLaterDoubleClick (MouseEvent mouseEvent) throws IOException
    {
        if (mouseEvent.getClickCount() == 2)
            newConnection (messageLater.getSelectionModel().getSelectedItem());
    }

    public void messageNowDoubleClick (MouseEvent mouseEvent) throws IOException
    {
        if (mouseEvent.getClickCount() == 2)
            newConnection (messageNow.getSelectionModel().getSelectedItem());
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

        messageLater.setItems(contacts);
        messageLater.setStyle("-fx-text-fill: darkgray");
        messageLater.setStyle("-fx-font-style: italic");

        refreshList();

        messageNow.setItems(onlineNow);
        messageNow.setStyle("-fx-font-weight: bold");
    }
}
