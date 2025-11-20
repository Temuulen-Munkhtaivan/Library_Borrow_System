/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.serverapp;

import java.util.*;

/**
 *
 * @author tekuboii
 */
public class LibraryDataStore {
    
    //borrower -> List of borrowed boks (in insertion order)
    private final Map<String, List<BorrowRecord>> loansByBorrower = new HashMap<>();
    
    public synchronized List<String> borrowBook(String borrower, String date, String title){
        List<BorrowRecord> records = 
                loansByBorrower.computeIfAbsent(borrower, k -> new ArrayList<>());
        records.add(new BorrowRecord(date, title));
        return extractTitles(records);
    }
    
    public synchronized List<String> returnBook(String borrower, String date, String title)
            throws InvalidCommandException {
        
        List<BorrowRecord> records = loansByBorrower.get(borrower);
        if(records == null || records.isEmpty()){
            throw new InvalidCommandException(
                "borrower '" +borrower+"' does not currently hold '" +title+ "'" );
        }
        
        boolean removed = false;
        Iterator<BorrowRecord> it = records.iterator();
        while(it.hasNext()) {
            BorrowRecord r = it.next();
            if(r.getTitle().equalsIgnoreCase(title)
                    && r.getDate().equalsIgnoreCase(date)) {
                it.remove();
                removed = true;
                break;
            }
        }
        
        if(!removed){
            throw new InvalidCommandException(
                "borrower '" +borrower+ "' does not currently hold '" +title+ "'");
        }
        
        if(records.isEmpty()) {
            loansByBorrower.remove(borrower);
            return Collections.emptyList();
        }
        
        return extractTitles(records);
        
    }
    
    public synchronized List<String> listBooks(String borrower) {
        List<BorrowRecord> records = loansByBorrower.get(borrower);
        if(records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return extractTitles(records);
    }
    
    private List<String> extractTitles(List<BorrowRecord> records){
        List<String> titles = new ArrayList<>();
        for (BorrowRecord r : records) {
            titles.add(r.getTitle());
        }
        
        return titles;
    }
}
