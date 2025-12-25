package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EchoClient {
    private static final Logger log = LoggerFactory.getLogger(EchoClient.class);

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8989;

        try (Socket socket = new Socket(host, port);
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            log.info("Connected to EchoServer at {}:{}", host, port);
            log.info("Type messages to send. Type 'exit' to quit.");

            String msg;
            while ((msg = stdin.readLine()) != null) {
                if ("exit".equalsIgnoreCase(msg)) {
                    break;
                }

                out.println(msg);
                String response = in.readLine();
                log.info("Echo: {}", response);
            }

        } catch (IOException e) {
            log.error("IO error in EchoClient", e);
        }
    }
}
