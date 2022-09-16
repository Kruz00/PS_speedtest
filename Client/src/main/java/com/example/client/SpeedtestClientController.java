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

    private final BooleanProperty hasTCPConnected = new SimpleBooleanProperty(false);
    private final BooleanProperty hasUDPConnected = new SimpleBooleanProperty(false);

    @FXML
    public void initialize() {
        this.packetSizeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            this.bytesLabel.setText("Bytes: " + newValue.intValue());
        });

        this.hasTCPConnected.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.tcpLabel.setText("TCP Thread: connected");
            } else  {
                this.tcpLabel.setText("TCP Thread: error");
            }
        });
        this.hasUDPConnected.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.udpLabel.setText("UDP Thread: connected");
            } else {
                this.udpLabel.setText("UDP Thread: error");
            }
        });

    }

    public void onNagleAlgorithmCheckbox(ActionEvent actionEvent) {
        this.nagleAlgorithm = !this.nagleAlgorithm;
    }

    public void onStartStop() {
        this.isRunning = !this.isRunning;
        this.refreshWindow();
    }

    private void refreshWindow() {
        String tcpLabel, udpLabel, buttonText;
        if (this.isRunning) {
            tcpLabel = "TCP Thread: starting";
            udpLabel = "UDP Thread: starting";
            buttonText = "Stop transmission";


            this.serverAddressLabel.setText(ipAddressField.getText() + ":" + portField.getText());
        } else {
            tcpLabel = "TCP Thread: disconnected";
            udpLabel = "UDP Thread: stopped";
            buttonText = "Start transmission";

//            if (BenchmarkApplication.tcpSender != null) {
//                BenchmarkApplication.tcpSender.shutdown();
//                try {
//                    BenchmarkApplication.tcpSender.join();
//                } catch (InterruptedException ignored) {
//                }
//            }
//            if (BenchmarkApplication.udpSender != null) {
//                BenchmarkApplication.udpSender.shutdown();
//                try {
//                    BenchmarkApplication.udpSender.join();
//                } catch (InterruptedException ignored) {
//                }
//            }
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