/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverapp;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 *
 * @author tekuboii
 */
public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private final LibraryDataStore dataStore;

    public ClientHandler(Socket clientSocket, LibraryDataStore dataStore) {
        this.clientSocket = clientSocket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + clientSocket);

        try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.equalsIgnoreCase("STOP")) {
                    out.println("TERMINATE");
                    break;
                }

                try {
                    String response = processCommand(line);
                    out.println(response);
                } catch (InvalidCommandException e) {
                    out.println("InvalidCommandException: " + e.getMessage());
                } catch (Exception e) {
                    out.println("Server error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            System.out.println("I/O error with client " + clientSocket + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected: " + clientSocket);
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private String processCommand(String message) throws InvalidCommandException {
        // Expected: action; borrower name; date; book title
        String[] parts = message.split(";");
        if (parts.length != 4) {
            throw new InvalidCommandException(
                    "command must have 4 fields: action; borrower; date; title");
        }

        String action   = parts[0].trim().toLowerCase();
        String borrower = parts[1].trim();
        String date     = parts[2].trim();
        String title    = parts[3].trim();

        if (action.isEmpty()) {
            throw new InvalidCommandException("action must be borrow/return/list");
        }

        switch (action) {
            case "borrow":
                if (borrower.isEmpty() || title.isEmpty() || date.isEmpty()) {
                    throw new InvalidCommandException(
                            "borrow requires non-blank borrower, date, and title");
                }
                List<String> borrowed = dataStore.borrowBook(borrower, date, title);
                return makeBorrowerReply(borrower, borrowed);

            case "return":
                if (borrower.isEmpty() || title.isEmpty() || date.isEmpty()) {
                    throw new InvalidCommandException(
                            "return requires non-blank borrower, date, and title");
                }
                List<String> remaining = dataStore.returnBook(borrower, date, title);
                return makeBorrowerReply(borrower, remaining);

            case "list":
                if (borrower.isEmpty()) {
                    throw new InvalidCommandException("list requires a borrower name");
                }
                List<String> listBooks = dataStore.listBooks(borrower);
                return makeBorrowerReply(borrower, listBooks);

            default:
                throw new InvalidCommandException("action must be borrow/return/list");
        }
    }

    private String makeBorrowerReply(String borrower, List<String> titles) {
        if (titles.isEmpty()) {
            return borrower + " currently has no books on loan.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(borrower).append(" currently has: ");
        for (int i = 0; i < titles.size(); i++) {
            sb.append(titles.get(i));
            if (i < titles.size() - 1) {
                sb.append("; ");
            }
        }
        return sb.toString();
    }
}
