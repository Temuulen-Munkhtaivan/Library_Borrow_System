/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
/**
 *
 * @author tekuboii
 */
public class LibraryServer {
    private static final int PORT = 5000; // must match client

    public static void main(String[] args) {
        LibraryDataStore dataStore = new LibraryDataStore();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Library Server listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, dataStore);
                Thread t = new Thread(handler);
                t.start(); // one thread per client
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
