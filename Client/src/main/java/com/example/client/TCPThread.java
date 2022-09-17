package com.example.client;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.*;

public class TCPThread extends Thread {
    private final String ipAddress;
    private final int port;
    private final boolean nagle;
    private final int bufferSize;
    private final BooleanProperty isTCPConnected;
    private Socket clientSocket;

    private final ScheduledExecutorService executor;

    public TCPThread(String ipAddress, int port, boolean nagle, int bufferSize, BooleanProperty isTCPConnected) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.nagle = nagle;
        this.bufferSize = bufferSize;
        this.isTCPConnected = isTCPConnected;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MILLISECONDS);
            if(clientSocket != null)
                this.clientSocket.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("TCP closed");
    }

    public void run() {
        try {
            this.clientSocket = new Socket(this.ipAddress, this.port);
            Platform.runLater(() -> this.isTCPConnected.setValue(true));
            this.clientSocket.setTcpNoDelay(!this.nagle);
            DataOutputStream outputStream = new DataOutputStream(this.clientSocket.getOutputStream());
            byte[] sizeMsg = ("SIZE:" + this.bufferSize).getBytes(StandardCharsets.UTF_8);
            outputStream.write(sizeMsg, 0, sizeMsg.length);

            byte[] testMsg = new byte[this.bufferSize];
            new Random().nextBytes(testMsg);

            ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(() -> {
                try {
                    outputStream.write(testMsg, 0, testMsg.length);
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }, 0, 1, TimeUnit.MILLISECONDS);
//            scheduledFuture.get();
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> this.isTCPConnected.setValue(false));
        }
    }

}
