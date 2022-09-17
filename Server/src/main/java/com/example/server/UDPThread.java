package com.example.server;

import javafx.application.Platform;

import java.io.FilterOutputStream;
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
//        Platform.runLater(() -> speedtestServerController.updateUDPstats(this.recvBufferSize, this.recvDataSize, this.transmissionTime, this.transmissionSpeed));

//        System.out.println(this.getClass().getName() + ": recvDataSize=" + this.recvDataSize + "; transmissionSpeed=" + this.transmissionSpeed);
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
            DatagramPacket packet;

            System.out.println("UDP Starting listening");
            this.serverSocket = new DatagramSocket(this.port);
            this.isListening = true;

            while (!this.isClosed.get()) {
                packet = new DatagramPacket(sizeMsg, sizeMsg.length);

                while (!this.isClosed.get()) {
                    System.out.println("UDP wait for first message");
                    this.serverSocket.receive(packet);
                    String[] firstMsg = (new String(packet.getData(), packet.getOffset(), packet.getLength())).split(":");
                    if (firstMsg[0].equalsIgnoreCase("SIZE")) {
                        System.out.println("UDP message: " + firstMsg[0] + firstMsg[1]);
                        this.resetStats();
                        this.recvBufferSize = Integer.parseInt(firstMsg[1]);
                        isSpeedtestRunning = true;
                        System.out.println("UDP Starting test");
                        this.startTime = System.currentTimeMillis();
                        break;
                    }
                }
                int bufSize = Math.max(this.recvBufferSize, 5);
                messageBuf = new byte[bufSize];
                packet = new DatagramPacket(messageBuf, messageBuf.length);
                String message;
                while (isSpeedtestRunning && !this.isClosed.get()) {
                    this.serverSocket.receive(packet);
                    message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    if (message.equalsIgnoreCase("FINE")) {
                        isSpeedtestRunning = false;
                        System.out.println("UDP End test");
                        break;
                    }
//                    executor.execute(this::calculateAndUpdateStats);
                    calculateAndUpdateStats();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("UDP Close thread");
            this.isListening = false;
        }
    }
}
