package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EchoServer {
    private static final Logger log = LoggerFactory.getLogger(EchoServer.class);

    private final int port;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService executor;

    public EchoServer(int port) {
        this.port = port;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;

        executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));

        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;

            log.info("EchoServer started on port {}", port);

            while (running) {
                handleClientAccept(server);
            }

        } catch (IOException e) {
            if (running) {
                log.error("Failed to start server socket", e);
            }
        }
    }

    private void handleClientAccept(ServerSocket server) {
        try {
            Socket client = server.accept();
            log.info("Client connected: {}", client.getRemoteSocketAddress());
            executor.submit(new ClientHandler(client));
        } catch (IOException e) {
            if (running) {
                log.error("Error accepting client connection", e);
            } else {
                log.info("Server stopped accepting connections.");
            }
        }
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;

        log.info("Stopping server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            log.error("Error closing server socket", e);
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Server stopped.");
    }

    private record ClientHandler(Socket socket) implements Runnable {
        private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

        @Override
        public void run() {
            try (Socket s = socket;
                 BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                 PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    out.println(line);
                    log.info("Echoed to {}: {}", s.getRemoteSocketAddress(), line);
                }

                log.info("Client {} disconnected gracefully", s.getRemoteSocketAddress());

            } catch (SocketException e) {
                log.error("Client {} disconnected (socket closed or reset)", socket.getRemoteSocketAddress());
            } catch (IOException e) {
                log.error("Unexpected IO error with client {}", socket.getRemoteSocketAddress(), e);
            }
        }
    }
}