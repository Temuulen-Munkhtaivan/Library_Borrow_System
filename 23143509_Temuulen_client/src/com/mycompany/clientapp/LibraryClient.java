/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.clientapp;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
/**
 *
 * @author tekuboii
 */
public class LibraryClient {
    private static final String HOST = "localhost";
    private static final int PORT = 5000; // same as server

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader serverIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream()), true);
             BufferedReader keyboardIn = new BufferedReader(
                     new InputStreamReader(System.in))) {

            System.out.println("Connected to Library Server at " + HOST + ":" + PORT);
            System.out.println("Commands:");
            System.out.println("  borrow; Name; Date; Title");
            System.out.println("  return; Name; Date; Title");
            System.out.println("  list; Name; -; -");
            System.out.println("  example import - import; https://raw.githubusercontent.com/Temuulen-Munkhtaivan/Library_Borrow_System/refs/heads/main/loans.txt");
            System.out.println("  STOP\n");

            while (true) {
                System.out.print("Enter command: ");
                String command = keyboardIn.readLine();
                if (command == null) {
                    break; // EOF on stdin
                }
                command = command.trim();
                if (command.isEmpty()) {
                    continue;
                }

                // client-only import command
                if (command.toLowerCase().startsWith("import;")) {
                    handleImportCommand(command, serverOut, serverIn);
                    continue;
                }

                if (command.equalsIgnoreCase("STOP")) {
                    serverOut.println("STOP");
                    String reply = serverIn.readLine();
                    System.out.println("Server: " + reply);
                    break;
                }

                // normal send to server
                serverOut.println(command);
                String reply = serverIn.readLine();
                if (reply == null) {
                    System.out.println("Server closed connection.");
                    break;
                }
                System.out.println("Server: " + reply);
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // import; <URL>
    private static void handleImportCommand(String command, PrintWriter serverOut,
                                            BufferedReader serverIn) {
        String[] parts = command.split(";", 2);
        if (parts.length != 2) {
            System.out.println("Usage: import; <public-URL-to-loans.txt>");
            return;
        }
        String urlString = parts[1].trim();
        if (urlString.isEmpty()) {
            System.out.println("URL cannot be blank.");
            return;
        }

        int imported = 0;
        int skipped = 0;

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            try (BufferedReader httpIn = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {

                String firstLine = httpIn.readLine();
                if (firstLine == null || !firstLine.toLowerCase().startsWith("borrower=")) {
                    System.out.println("Invalid file format: first line must be borrower=<Name>");
                    return;
                }

                String borrowerName = firstLine.substring("borrower=".length()).trim();
                if (borrowerName.isEmpty()) {
                    System.out.println("Invalid file: borrower name is blank.");
                    return;
                }

                String line;
                int lineNumber = 1;

                while ((line = httpIn.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] loanParts = line.split(";", 2);
                    if (loanParts.length != 2) {
                        System.out.println("Skipped line " + lineNumber +
                                           ": wrong format (must be 'date; book title')");
                        skipped++;
                        continue;
                    }

                    String date = loanParts[0].trim();
                    String title = loanParts[1].trim();

                    if (date.isEmpty() || title.isEmpty()) {
                        System.out.println("Skipped line " + lineNumber +
                                           ": date or title is blank");
                        skipped++;
                        continue;
                    }

                    String borrowCommand = "borrow; " + borrowerName + "; " + date + "; " + title;
                    serverOut.println(borrowCommand);
                    String reply = serverIn.readLine();
                    if (reply == null) {
                        System.out.println("Server closed connection during import.");
                        break;
                    }
                    System.out.println("Server: " + reply);
                    imported++;
                }
            }

            System.out.println("Imported: " + imported + "; Skipped: " + skipped);

        } catch (IOException e) {
            System.out.println("HTTP import error: " + e.getMessage());
        }
    }
}
