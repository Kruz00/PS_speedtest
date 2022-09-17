package com.example.server;

import javafx.application.Platform;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPThread extends Thread {
    private final int port;
    private final SpeedtestServerController speedtestServerController;
    private final ExecutorService executor;

    private boolean isListening = false;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private boolean isSpeedtestRunning = false; // set to true after receive first message, set to false after receive stop message

    private long startTime;
    private long currentTime;
    private long transmissionTime;
    private int recvBufferSize;
    private long recvDataSize;
    private double transmissionSpeed;

    private DatagramSocket serverSocket;


    public UDPThread(int port, SpeedtestServerController speedtestServerController) {
        this.port = port;
        this.speedtestServerController = speedtestServerController;
        executor = Executors.newSingleThreadExecutor();
    }

    public void close() {
        this.isClosed.set(true);
        this.serverSocket.close();
    }

    private void calculateAndUpdateStats() {
        this.currentTime = System.currentTimeMillis();
        this.transmissionTime = this.currentTime - this.startTime;
        this.recvDataSize += this.recvBufferSize;
        this.transmissionSpeed = (double) this.recvDataSize * 1000.0D / (double) (this.transmissionTime);
        Platform.runLater(() -> speedtestServerController.updateUDPstats(this.recvBufferSize, this.recvDataSize, this.transmissionTime, this.transmissionSpeed));

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

    public void run() {
        try {
            byte[] sizeMsg = new byte[12]; // in real max its 10, but for sure
            byte[] messageBuf;
            DatagramPacket pack;

            System.out.println("Starting UDP listening");
            this.serverSocket = new DatagramSocket(this.port);
            this.isListening = true;

            while (!this.isClosed.get()) {
                this.resetStats();
                pack = new DatagramPacket(sizeMsg, sizeMsg.length);

                while (!this.isClosed.get()) {
                    this.serverSocket.receive(pack);
                    String[] firstMsg = (new String(pack.getData(), pack.getOffset(), pack.getLength())).split(":");
                    if (firstMsg[0].equalsIgnoreCase("SIZE")) {
                        this.resetStats();
                        this.recvBufferSize = Integer.parseInt(firstMsg[1]);
                        isSpeedtestRunning = true;
                        System.out.println("Starting UDP test");
                        this.startTime = System.currentTimeMillis();
                        break;
                    }
                }

                messageBuf = new byte[this.recvBufferSize];
                pack = new DatagramPacket(messageBuf, messageBuf.length);
                String message;
                while (isSpeedtestRunning && !this.isClosed.get()) {
                    this.serverSocket.receive(pack);
                    message = new String(pack.getData(), pack.getOffset(), pack.getLength());
                    if (message.equalsIgnoreCase("FINE")) {
                        isSpeedtestRunning = false;
                        System.out.println("End UDP test");
                        break;
                    }
                    executor.execute(this::calculateAndUpdateStats);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Close UDP thread");
            this.isListening = false;
        }
    }
}
