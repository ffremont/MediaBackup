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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author florent
 */
public class MediaBackupApp {

    private static final int DATE_TIME_ORIGINAL = 36867;
    private static Map<Path, LocalDateTime> dates = new HashMap<>();
    private static Long blockCount;

    public static void main(String[] args) throws IOException {
        long blockSize = args.length > 0 ? Long.valueOf(args[0]) : -1;

        Path from = Paths.get("from");
        Path to = Paths.get("to");
        Path blocs = Paths.get("blocs");

        Files.walk(from).forEach(filePath -> {
            if (Files.isRegularFile(filePath)) {
                System.out.println(filePath);
                try {
                    Metadata metadata = ImageMetadataReader.readMetadata(filePath.toAbsolutePath().toFile());
                    Iterable<Directory> it = metadata.getDirectories();
                    it.forEach((Directory dir) -> {
                        dir.getTags().forEach((Tag tag) -> {
                            if (tag.getTagType() == DATE_TIME_ORIGINAL) {
                                /*System.out.println("--------------");
                                 System.out.println(tag.getTagName());
                                 System.out.println(tag.getTagType());
                                 System.out.println(tag.getTagTypeHex());
                                 System.out.println(dir.getDate(tag.getTagType()));
                                 System.out.println("--------------");*/
                                System.out.println(filePath);
                                dates.put(filePath, LocalDateTime.ofInstant(dir.getDate(tag.getTagType()).toInstant(), ZoneId.systemDefault()));
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

        // chaque fichiers
        dates.entrySet().stream().forEach((Map.Entry<Path, LocalDateTime> entry) -> {
            try {
                Path destination = Paths.get(to.toAbsolutePath().toString(),
                        "" + entry.getValue().getYear(),
                        "" + entry.getValue().format(DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH)),
                        entry.getValue().getDayOfMonth() + "");
                Files.createDirectories(destination);
                //Metadata metadata = ImageMetadataReader.readMetadata(imagePath);
                Files.copy(
                        entry.getKey(),
                        Paths.get(
                                destination.toAbsolutePath().toString(),
                                entry.getKey().getFileName().toString()
                        ), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        if (blockSize != -1) {
            int blocNb = 1;
            blockCount = new Long(0);
            Files.walk(from).forEach((Path filePath) -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        blockCount += Files.size(filePath);
                    } catch (IOException ex) {
                        Logger.getLogger(MediaBackupApp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            });
        }
    }
}
