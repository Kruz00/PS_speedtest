package com.example.client;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UDPThread extends Thread {
    private final String ipAddress;
    private final int port;
    private final int bufferSize;
    private final BooleanProperty isTCPConnected;
    private DatagramSocket clientSocket;

    private final ScheduledExecutorService executor;

    public UDPThread(String ipAddress, int port, int bufferSize, BooleanProperty isTCPConnected) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.bufferSize = bufferSize;
        this.isTCPConnected = isTCPConnected;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    private void errorClose() {
        Platform.runLater(() -> this.isTCPConnected.setValue(false));
        close();
    }


    public void close() {
        byte[] endMsg = "FINE".getBytes(StandardCharsets.UTF_8);
        try {
            DatagramPacket endPacket = new DatagramPacket(endMsg, endMsg.length, InetAddress.getByName(this.ipAddress), this.port);
            this.clientSocket.send(endPacket);
            System.out.println("end packet sent");
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MILLISECONDS);
            this.clientSocket.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("UDP closed");
    }

    public void run() {
        try {
            this.clientSocket = new DatagramSocket();
            Platform.runLater(() -> this.isTCPConnected.setValue(true));

            byte[] sizeMsg = ("SIZE:" + this.bufferSize).getBytes(StandardCharsets.UTF_8);
            DatagramPacket sizePacket = new DatagramPacket(sizeMsg, sizeMsg.length, InetAddress.getByName(this.ipAddress), this.port);
            this.clientSocket.send(sizePacket);

            byte[] testMsg = new byte[this.bufferSize];
            new Random().nextBytes(testMsg);
            DatagramPacket testPacket = new DatagramPacket(testMsg, testMsg.length, InetAddress.getByName(this.ipAddress), this.port);
            executor.scheduleWithFixedDelay(() -> {
                try {
                    this.clientSocket.send(testPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorClose();
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> this.isTCPConnected.setValue(false));
        }
    }

}
