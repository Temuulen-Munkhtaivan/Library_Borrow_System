/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverapp;

/**
 *
 * @author tekuboii
 */
public class BorrowRecord {
    private final String date;
    private final String title;

    public BorrowRecord(String date, String title) {
        this.date = date;
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }
}
