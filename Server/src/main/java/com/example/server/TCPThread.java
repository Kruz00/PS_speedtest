package com.example.server;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPThread extends Thread {
    private final int port;
    private final SpeedtestServerController speedtestServerController;
    private final ExecutorService acceptingExecutor;
    private final ExecutorService executor;

    private boolean isListening = false;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private long startTime;
    private long currentTime;
    private long transmissionTime;
    private int recvBufferSize;
    private long recvDataSize;
    private double transmissionSpeed;

    private ServerSocket serverSocket;
    private Socket clientSocket;


    public TCPThread(int port, SpeedtestServerController speedtestServerController) {
        this.port = port;
        this.speedtestServerController = speedtestServerController;
        acceptingExecutor = Executors.newSingleThreadExecutor();
        executor = Executors.newSingleThreadExecutor();

    }

    public void close() {
        this.isClosed.set(true);

        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calculateAndUpdateStats() {
        this.currentTime = System.currentTimeMillis();
        this.transmissionTime = this.currentTime - this.startTime;
        this.recvDataSize += this.recvBufferSize;
        this.transmissionSpeed = (double) this.recvDataSize * 1000.0D / (double) (this.transmissionTime);
        Platform.runLater(() -> speedtestServerController.updateTCPstats(this.recvBufferSize, this.recvDataSize, this.transmissionTime, this.transmissionSpeed));

        System.out.println(this.getClass().getName() + ": recvDataSize=" + this.recvDataSize + "; transmissionSpeed=" + this.transmissionSpeed);
    }

    private void resetStats() {
        this.startTime = 0L;
        this.currentTime = 0L;
        this.transmissionTime = 0L;
        this.recvBufferSize = 0;
        this.recvDataSize = 0L;
        this.transmissionSpeed = 0.0D;
    }

    private void acceptingConnection() {
        try {
            while (!isClosed.get()) {
                Socket accepted = serverSocket.accept();
                if (clientSocket == null || clientSocket.isClosed()) {
                    clientSocket = accepted;
                    resetStats();
                    this.notify();
                } else {
                    accepted.close();
                }
            }
        } catch (IOException e) {
            System.out.println("Error on new client");
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port, 1); // minimum number
            this.isListening = true;

            System.out.println("Starting TCP listening");
            acceptingExecutor.submit(this::acceptingConnection);

            byte[] sizeMsg = new byte[12]; // in real max its 10, but for sure
            byte[] messageBuf;

            while (!this.isClosed.get()) {
                this.wait();
                DataInputStream in = new DataInputStream(this.clientSocket.getInputStream());
                int bytesRead = in.read(sizeMsg);
                if (bytesRead >= 0) {
                    String[] firstMsg = (new String(sizeMsg, 0, bytesRead)).split(":");
                    this.recvBufferSize = Integer.parseInt(firstMsg[1]);
                    System.out.println("Starting TCP test");
                    this.startTime = System.currentTimeMillis();
                } else {
                    this.clientSocket.close();
                    continue;
                }
                messageBuf = new byte[recvBufferSize];

                while (!this.isClosed.get()) { // till server ends
                    bytesRead = in.read(messageBuf);
                    if (bytesRead >= 0) { // till client ends connection
                        executor.execute(this::calculateAndUpdateStats);
                    } else {
                        this.clientSocket.close();
                        break;
                    }
                }
                System.out.println("End UDP test");
                acceptingExecutor.shutdown();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Close TCP thread");
            this.isListening = false;
        }
    }
}
