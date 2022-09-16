package com.example.client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SpeedtestClientController {
    public Label bytesLabel;
    public Label tcpLabel;
    public Label udpLabel;


    public Slider packetSizeSlider;
    public TextField ipAddressField;
    public TextField portField;
    public Button addAddressButton;
    public ComboBox<String> addressCombo;
    public CheckBox nagleCheckBox;
    public Button startStopButton;

    private boolean nagleAlgorithm = false;
    private boolean isRunning = false;

    private final BooleanProperty hasTCPConnected = new SimpleBooleanProperty(false);
    private final BooleanProperty hasUDPConnected = new SimpleBooleanProperty(false);

    public void nagleAlgorithmCheckboxHandler(ActionEvent actionEvent) {
        this.nagleAlgorithm = !this.nagleAlgorithm;
    }

    public void startStopHandler(ActionEvent actionEvent) {

    }
}