/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.mediabackup;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author florent
 */
public class Condition {
    private LocalDate start;
    private LocalDate end;
    private String label;

    public Condition(LocalDate start, LocalDate end, String label) {
        this.start = start;
        this.end = end;
        this.label = label;
    }

    public static Condition from(String start, String end, String label){
        LocalDate s = LocalDate.parse(start, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        LocalDate e = LocalDate.parse(end, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        return new Condition(s, e, label);
    }
    
    public boolean oneDay(){
        return start.equals(end);
    }
    
    public boolean accept(LocalDateTime date){
        LocalDate d = date.toLocalDate();
        
        return 
                (d.equals(start) || d.isAfter(start)) 
                && 
                (d.equals(end) || d.isBefore(end));
    }
    
    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    
}
