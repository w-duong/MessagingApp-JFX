package sample.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import sample.model.Client;
import sample.model.CustomTab;

import java.util.HashMap;

public class messageTabController
{
    @FXML
    TextArea inputMessageField;
    @FXML
    TextFlow ChatLog;

    public void mouseSendButton (MouseEvent mouseEvent) { sendMessageSignal(); }

    public void keypressSendButton (KeyEvent keyEvent)
    {
        if (keyEvent.getCode().equals(KeyCode.ENTER) && keyEvent.isShiftDown())
            sendMessageSignal();
    }

    public void sendMessageSignal ()
    {
        String id = CustomTab.tabPane.getSelectionModel().getSelectedItem().getId();

        if (!inputMessageField.getText().equals("") && !inputMessageField.getText().equals(null))
        {
            String message = inputMessageField.getText();
            Text chatLine = new Text("You > " + message + "\n");
            chatLine.setFill(Paint.valueOf("Blue"));

            inputMessageField.clear();

            ChatLog.getChildren().add(chatLine);

            CustomTab.client.sendMessage (message + "@" + id);
        }
    }
}
