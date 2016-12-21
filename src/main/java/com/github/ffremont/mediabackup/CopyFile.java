/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.mediabackup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author florent
 */
public class CopyFile implements Runnable {

    private Path to;
    private Path from;
    private final int numBloc;
    private final MetaFile metaFile;
    private boolean byDay;

    public CopyFile(Path from, Path to, int i, MetaFile metaFile, boolean byDay) {
        this.from = from;
        this.numBloc = i;
        this.byDay = byDay;
        this.to = to;
        this.metaFile = metaFile;
    }

    @Override
    public void run() {
        try {
            String filename = metaFile.path().getFileName().toString();
            Path destination = destDirMedia(Paths.get(to.toAbsolutePath().toString(), numBloc + ""), metaFile.created());
            Files.createDirectories(destination);

            Path destinationFile = Paths.get(
                    destination.toAbsolutePath().toString(),
                    filename
            );

            if (Files.exists(destinationFile)) {
                String hashOfFileDestinationFile = HashFile.sha1(destinationFile);
                if (hashOfFileDestinationFile.equals(metaFile.hash())) {
                    System.out.println(destinationFile.toAbsolutePath().toString() + " existe déjà");
                } else {
                    Files.copy(
                            metaFile.path(),
                            destinationFile, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println(destinationFile.toAbsolutePath().toString() + " copié");
                }
            } else {
                Files.copy(
                        metaFile.path(),
                        destinationFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println(destinationFile.toAbsolutePath().toString() + " copié");
            }

            Path linkPath = Paths.get(
                    this.metaFile.path().toAbsolutePath().toString().replace(
                            from.toAbsolutePath().toString(),
                            Paths.get(to.toAbsolutePath().toString(), numBloc + "", "links").toAbsolutePath().toString()
                    ).replace(filename, ""));
            Files.createDirectories(linkPath);
            if(!Files.exists(linkPath)){
                Files.createSymbolicLink(Paths.get(linkPath.toAbsolutePath().toString(), filename), destinationFile);
            }
        } catch (IOException ex) {
            Logger.getLogger(CopyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Path destDirMedia(Path to, LocalDateTime date) {
        if (date == null) {
            return Paths.get(to.toAbsolutePath().toString(), "non triés");
        }
        Optional<Condition> con = MediaBackupApp.conditions.stream().filter(c -> c.accept(date)).findFirst();

        String mois = date.format(DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH));
        String jr = "";
        if (byDay) {
            jr = date.getDayOfMonth() + "";
        }

        if (con.isPresent()) {
            if (con.get().oneDay() && byDay) {
                jr = jr + " - " + con.get().getLabel();
            } else {
                mois = mois + " - " + con.get().getLabel();
            }
        }
        return Paths.get(to.toAbsolutePath().toString(),
                "" + date.getYear(),
                "" + mois,
                jr);
    }

}
