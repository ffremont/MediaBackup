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
    private static List<String> sha1knownCreationDate = new ArrayList<>();
    private static List<MetaFile> unknown = new ArrayList<>();

    public static List<Condition> conditions = new ArrayList<>();

    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private static void clear(Path directoryToDelete) {
        try {
            if (Files.exists(directoryToDelete)) {
                Files.walk(directoryToDelete).
                        sorted((a, b) -> b.compareTo(a)). // reverse; files before dirs
                        forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException e) { /* ... */ }
                        });
            }
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
        if (Files.exists(legPath)) {
            Properties legend = new Properties();
            legend.load(Files.newInputStream(legPath));
            for (String prop : legend.stringPropertyNames()) {
                conditions.add(Condition.from(prop, legend.getProperty(prop)));
            }
        }

        Files.walk(from).forEach((Path filePath) -> {
            if (Files.isRegularFile(filePath)) {
                System.out.println(filePath);
                String hashOfFile = HashFile.sha1(filePath);
                MetaFile meta = null;
                try {
                    meta = new MetaFile(filePath, Files.size(filePath), hashOfFile);
                } catch (IOException ex) {
                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(filePath.toAbsolutePath().toFile());
                    Iterable<Directory> it = metadata.getDirectories();
                    for (Directory dir : it) {
                        for (Tag tag : dir.getTags()) {
                            if (tag.getTagType() == DATE_TIME_ORIGINAL) {
                                sha1knownCreationDate.add(hashOfFile);
                                System.out.println(filePath);
                                LocalDateTime created = LocalDateTime.ofInstant(dir.getDate(tag.getTagType()).toInstant(), ZoneId.systemDefault());
                                meta.created(created);

                                // on ne prend pas les doublons
                                if (!dates.values().stream().anyMatch(metaFile -> hashOfFile.equals(metaFile.hash()))) {
                                    dates.put(created, meta);
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ImageProcessingException ex) {
                    Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (!sha1knownCreationDate.contains(hashOfFile) && (meta != null)) {
                    unknown.add(meta);
                }

            }
        });

        int i = 0;
        long sum = 0;
        List<Future> calls = new ArrayList<>();
        for (Map.Entry<LocalDateTime, MetaFile> entry : dates.entrySet()) {
            if ((blocsize != -1) && (sum + entry.getValue().size() > blocsize)) {
                i++;
            }

            calls.add(executor.submit(new CopyFile(from, to, i, entry.getValue(), byDay)));
            sum += entry.getValue().size();
        }
        // toutes les dates inconnues
        for (MetaFile meta : unknown) {
            if ((blocsize != -1) && (sum + meta.size() > blocsize)) {
                i++;
            }

            calls.add(executor.submit(new CopyFile(from, to, i, meta, byDay)));
            sum += meta.size();
        }

        for (Future f : calls) {
            try {
                f.get();
            } catch (ExecutionException ex) {
                Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();

        System.out.println("Copie déterminée des " + dates.size() + " images");
    }

}
