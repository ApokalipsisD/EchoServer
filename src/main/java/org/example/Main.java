package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        EchoServer server = new EchoServer(8989);

        Thread serverThread = new Thread(server::start, "ServerThread");
        serverThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered. Stopping server...");
            server.stop();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.error("Main thread interrupted, stopping server...");
            Thread.currentThread().interrupt();
            server.stop();
        }
    }}