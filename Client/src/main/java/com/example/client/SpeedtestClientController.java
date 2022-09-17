package com.example.client;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SpeedtestClientController {
    @FXML
    public Label bytesLabel;
    @FXML
    public Label tcpLabel;
    @FXML
    public Label udpLabel;
    @FXML
    public Label serverAddressLabel;

    @FXML
    public Slider packetSizeSlider;
    @FXML
    public TextField ipAddressField;
    @FXML
    public TextField portField;
    @FXML
    public CheckBox nagleCheckBox;
    @FXML
    public Button startStopButton;

    private boolean nagleAlgorithm = false;
    private boolean isRunning = false;

    private final BooleanProperty isTCPConnected = new SimpleBooleanProperty(false);
    private final BooleanProperty isUDPConnected = new SimpleBooleanProperty(false);

    private TCPThread tcpThread;
    private UDPThread udpThread;

    @FXML
    public void initialize() {
//        this.ipAddressField.setText("127.0.0.1"); // localhost
        this.ipAddressField.setText("10.11.215.129"); // dell

        this.packetSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.bytesLabel.setText("Bytes: " + newValue.intValue());
        });

        this.isTCPConnected.addListener((observable, oldValue, newValue) -> {
            System.out.println("tcp newvalue " + newValue);
            if (newValue) {
                this.tcpLabel.setText("TCP Thread: connected");
            } else  {
                this.tcpLabel.setText("TCP Thread: error");
            }
        });
        this.isUDPConnected.addListener((observable, oldValue, newValue) -> {
            System.out.println("udp newvalue " + newValue);
            if (newValue) {
                this.udpLabel.setText("UDP Thread: connected");
            } else {
                this.udpLabel.setText("UDP Thread: error");
            }
        });

    }

    public void onNagleAlgorithmCheckbox(ActionEvent actionEvent) {
        this.nagleAlgorithm = this.nagleCheckBox.isSelected();
    }

    public void onStartStop() {
        this.isRunning = !this.isRunning;

        String tcpLabel, udpLabel, buttonText;
        this.nagleAlgorithm = this.nagleCheckBox.isSelected();

        if (this.isRunning) {
            tcpLabel = "TCP Thread: starting";
            udpLabel = "UDP Thread: starting";
            buttonText = "Stop transmission";

            tcpThread = new TCPThread(this.ipAddressField.getText(), Integer.parseInt(this.portField.getText()),
                    this.nagleAlgorithm, (int) this.packetSizeSlider.getValue(), isTCPConnected);
            udpThread = new UDPThread(this.ipAddressField.getText(), Integer.parseInt(this.portField.getText()),
                    (int) this.packetSizeSlider.getValue(), isUDPConnected);

            tcpThread.start();
            udpThread.start();

            this.serverAddressLabel.setText(ipAddressField.getText() + ":" + portField.getText());
        } else {
            tcpLabel = "TCP Thread: disconnected";
            udpLabel = "UDP Thread: stopped";
            buttonText = "Start transmission";

            tcpThread.close();
            udpThread.close();

            this.serverAddressLabel.setText("None");
        }

        this.tcpLabel.setText(tcpLabel);
        this.udpLabel.setText(udpLabel);
        this.startStopButton.setText(buttonText);

        this.packetSizeSlider.setDisable(this.isRunning);
        this.ipAddressField.setDisable(this.isRunning);
        this.portField.setDisable(this.isRunning);
        this.nagleCheckBox.setDisable(this.isRunning);
    }

}