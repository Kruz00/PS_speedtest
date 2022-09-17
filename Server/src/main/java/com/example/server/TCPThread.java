package com.example.server;

import javafx.application.Platform;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
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

    public long getTransmissionTime() {
        return transmissionTime;
    }

    public int getRecvBufferSize() {
        return recvBufferSize;
    }

    public long getRecvDataSize() {
        return recvDataSize;
    }

    public double getTransmissionSpeed() {
        return transmissionSpeed;
    }

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final Semaphore sem;


    public TCPThread(int port, SpeedtestServerController speedtestServerController) {
        this.port = port;
        this.speedtestServerController = speedtestServerController;
        acceptingExecutor = Executors.newSingleThreadExecutor();
        executor = Executors.newSingleThreadExecutor();
        this.sem = new Semaphore(0);
    }

    public void close() {
        acceptingExecutor.shutdown();
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
//        Platform.runLater(() -> speedtestServerController.updateTCPstats(this.recvBufferSize, this.recvDataSize, this.transmissionTime, this.transmissionSpeed));

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
                    System.out.println("TCP new client");
//                    resetStats();
                    this.sem.release();
                } else {
                    accepted.close();
                }
            }
        } catch (SocketException e) {
            this.sem.release();
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("TCP Error on new client");
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
                this.sem.acquire();
                System.out.println("TCP starting");
                DataInputStream inputStream;
                try {
                    inputStream = new DataInputStream(this.clientSocket.getInputStream());
                } catch (NullPointerException e) {
                    continue;
                }
                int bytesRead = inputStream.read(sizeMsg);
                if (bytesRead >= 0) {
                    String[] firstMsg = (new String(sizeMsg, 0, bytesRead, StandardCharsets.UTF_8)).split(":");
                    if (firstMsg[0].equalsIgnoreCase("SIZE")) {
                        resetStats();
                        this.recvBufferSize = Integer.parseInt(firstMsg[1]);
                        System.out.println("TCP Starting test: " + firstMsg[1]);
                        this.startTime = System.currentTimeMillis();
                    }
                } else {
                    this.clientSocket.close();
                    continue;
                }
                messageBuf = new byte[recvBufferSize];

                while (!this.isClosed.get()) { // till server ends
                    bytesRead = inputStream.read(messageBuf);
                    System.out.println("TCP get msg: " + Arrays.toString(messageBuf));
                    if (bytesRead >= 0) { // till client ends connection
//                        executor.execute(this::calculateAndUpdateStats);
                        calculateAndUpdateStats();
                    } else {
                        this.clientSocket.close();
                        break;
                    }
                }
                System.out.println("TCP End test");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.isListening = false;
        }
        System.out.println("TCP Close thread");
    }
}
