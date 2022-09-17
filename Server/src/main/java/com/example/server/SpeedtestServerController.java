package com.example.server;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SpeedtestServerController {
    @FXML
    public TextField portField;
    @FXML
    public Button startStopButton;


    @FXML
    public TextField tcp_singleDataSize;
    @FXML
    public TextField tcp_totalSizeOfTransferredData;
    @FXML
    public TextField tcp_totalTransmissionTime;
    @FXML
    public TextField tcp_transmissionSpeed;


    @FXML
    public TextField udp_singleDataSize;
    @FXML
    public TextField udp_totalSizeOfTransferredData;
    @FXML
    public TextField udp_totalTransmissionTime;
    @FXML
    public TextField udp_transmissionSpeed;


    private TCPThread tcpThread;
    private UDPThread udpThread;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledUpdating;

    private void updateStats() {
        if (this.tcpThread != null) {
            updateTCPstats(
                    this.tcpThread.getRecvBufferSize(),
                    this.tcpThread.getRecvDataSize(),
                    this.tcpThread.getTransmissionTime(),
                    this.tcpThread.getTransmissionSpeed()
            );
        }
        if (this.udpThread != null) {
            updateUDPstats(
                    this.udpThread.getRecvBufferSize(),
                    this.udpThread.getRecvDataSize(),
                    this.udpThread.getTransmissionTime(),
                    this.udpThread.getTransmissionSpeed()
            );
        }
    }

    @FXML
    public void initialize() {
        startUpdatingStats();
    }

    private void startUpdatingStats() {
        scheduledUpdating = executorService.scheduleAtFixedRate(
                () -> Platform.runLater(this::updateStats),
                50, 50, TimeUnit.MILLISECONDS
        );
    }

    public void onStartStop(ActionEvent actionEvent) {
        if (this.startStopButton.getText().equalsIgnoreCase("Start listening")) {
            int port = Integer.parseInt(this.portField.getText());
            if (port != 0 && port <= 65535 && port >= -65535) {
                if (port < 0) {
                    port = -1 * port;
                }
            } else {
                port = 7777;
            }

            this.startStopButton.setText("Stop listening");
            this.portField.setDisable(true);

            this.portField.setText("" + port);

            this.tcpThread = new TCPThread(port);
            this.udpThread = new UDPThread(port);

            this.tcpThread.start();
            this.udpThread.start();

            startUpdatingStats();

        } else if (this.startStopButton.getText().equalsIgnoreCase("Stop listening")) {
            this.tcpThread.close();
            this.udpThread.close();
            this.startStopButton.setText("Start listening");
            this.portField.setDisable(false);
            scheduledUpdating.cancel(false);
        }
    }


    private void updateTCPstats(long singleDataSize,
                                double totalSizeOfTransferredData,
                                double totalTransmissionTime,
                                double transmissionSpeed
    ) {
        this.tcp_singleDataSize.setText(String.valueOf(singleDataSize));
        this.tcp_totalSizeOfTransferredData.setText(String.format("%.3f", totalSizeOfTransferredData / 1024));
        this.tcp_totalTransmissionTime.setText(String.format("%.1f", totalTransmissionTime / 1000));
        this.tcp_transmissionSpeed.setText(String.format("%.3f", transmissionSpeed / 1024));
    }

    private void updateUDPstats(long singleDataSize,
                                double totalSizeOfTransferredData,
                                double totalTransmissionTime,
                                double transmissionSpeed
    ) {
        this.udp_singleDataSize.setText(String.valueOf(singleDataSize));
        this.udp_totalSizeOfTransferredData.setText(String.format("%.3f", totalSizeOfTransferredData / 1024));
        this.udp_totalTransmissionTime.setText(String.format("%.1f", totalTransmissionTime / 1000));
        this.udp_transmissionSpeed.setText(String.format("%.3f", transmissionSpeed / 1024));
    }
}