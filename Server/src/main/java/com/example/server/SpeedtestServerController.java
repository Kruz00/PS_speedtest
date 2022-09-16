package com.example.server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SpeedtestServerController {
    @FXML
    public TextField portField;
    @FXML
    public Button startStopButton;


    @FXML
    public TextField tcp_sds;
    @FXML
    public TextField tcp_tsotd;
    @FXML
    public TextField tcp_ttt;
    @FXML
    public TextField tcp_sct;
    @FXML
    public TextField tcp_ts;
    @FXML
    public TextField tcp_ld;
    @FXML
    public TextField tcp_te;


    @FXML
    public TextField udp_sds;
    @FXML
    public TextField udp_tsotd;
    @FXML
    public TextField udp_ttt;
    @FXML
    public TextField udp_sct;
    @FXML
    public TextField udp_ts;
    @FXML
    public TextField udp_ld;
    @FXML
    public TextField udp_te;


    @FXML
    public void initialize() {

    }

    public void onStartStop(ActionEvent actionEvent) {
    }
}