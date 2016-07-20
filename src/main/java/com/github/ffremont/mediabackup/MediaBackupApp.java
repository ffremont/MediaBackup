/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.mediabackup;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author florent
 */
public class MediaBackupApp {

    private static final int DATE_TIME_ORIGINAL = 36867;
    private static Map<LocalDateTime, MetaFile> dates = new TreeMap<>();
    
    public static List<Condition> conditions = new ArrayList<>();

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private static void clear(Path directoryToDelete) {
        try {
            Files.walk(directoryToDelete).
                    sorted((a, b) -> b.compareTo(a)). // reverse; files before dirs
                    forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) { /* ... */ }
                    });
        } catch (IOException ex) {
            Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        long blocsize = System.getProperty("blocsize") != null ? Long.valueOf(System.getProperty("blocsize")) : -1;
        Path from = System.getProperty("from") != null ? Paths.get(System.getProperty("from")) : Paths.get("from");
        Path to = System.getProperty("to") != null ? Paths.get(System.getProperty("to")) : Paths.get("to");
        boolean byDay = System.getProperty("byDay") != null;
        clear(to);
        
        Path legPath = Paths.get("legendes.properties");
        if(Files.exists(legPath)){
            Properties legend = new Properties();
            legend.load(Files.newInputStream(legPath));
            for(String prop : legend.stringPropertyNames()){
                String[] parts = prop.split("->");
                if(parts.length != 2){
                    throw new RuntimeException("format du fichier legendes invalide a -> b");
                }
                
                conditions.add(Condition.from(parts[0].trim(), parts[1].trim(), legend.getProperty(prop)));
            }
            
        }

        Files.walk(from).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                System.out.println(filePath);
                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(filePath.toAbsolutePath().toFile());
                    Iterable<Directory> it = metadata.getDirectories();
                    it.forEach((Directory dir) -> {
                        dir.getTags().forEach((Tag tag) -> {
                            if (tag.getTagType() == DATE_TIME_ORIGINAL) {
                                System.out.println(filePath);
                                try {
                                    LocalDateTime created = LocalDateTime.ofInstant(dir.getDate(tag.getTagType()).toInstant(), ZoneId.systemDefault());
                                    dates.put(created, new MetaFile(created, filePath, Files.size(filePath)));
                                } catch (IOException ex) {
                                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                    });
                } catch (IOException ex) {
                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ImageProcessingException ex) {
                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        int i = 0;
        long sum = 0;
        List<Future> calls = new ArrayList<>();
        for (Map.Entry<LocalDateTime, MetaFile> entry : dates.entrySet()) {
            if ((blocsize != -1) && (sum + entry.getValue().getSize() > blocsize)) {
                i++;
            }

            calls.add(executor.submit(new CopyFile(to, i, entry.getValue(), byDay)));
            sum += entry.getValue().getSize();
        }

        for(Future f : calls){
            try {
                f.get();
            } catch (ExecutionException ex) {
                Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        
        System.out.println("Copie déterminée des "+dates.size()+" images");
    }

}
