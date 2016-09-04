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
    private boolean interval;
    private int day;
    private int month;

    public Condition(int day, int month, String label) {
        this.day = day;
        this.month = month;
        this.interval = false;
        this.label = label;
    }

    public Condition(LocalDate start, LocalDate end, String label) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.interval = true;
    }

    public static Condition from(String chaine, String label) {
        boolean byInterval = false;
        if (chaine.contains("->")) {
            byInterval = true;
            String[] parts = chaine.split("->");
            if (parts.length != 2) {
                throw new RuntimeException("format du fichier legendes invalide a -> b : "+chaine);
            }
            LocalDate s = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate e = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            return new Condition(s, e, label);
        } else {
            String[] parts = chaine.split("/");

            return new Condition(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), label);
        }
    }

    public boolean oneDay() {
        if (interval) {
            return start.equals(end);
        } else {
            return true;
        }
    }

    public boolean accept(LocalDateTime date) {
        LocalDate d = date.toLocalDate();

        if (interval) {
            return (d.equals(start) || d.isAfter(start))
                    && (d.equals(end) || d.isBefore(end));
        } else {
            return (d.getDayOfMonth() == day)
                    && (d.getMonthValue() == month);
        }
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
