package com.historicconquest.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("🌐 Starting Historic Conquest server...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();

            }

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}