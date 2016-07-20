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
    private final int numBloc;
    private final MetaFile metaFile;
    private boolean byDay;

    public CopyFile(Path to, int i, MetaFile metaFile, boolean byDay) {
        this.numBloc = i;
        this.byDay = byDay;
        this.to = to;
        this.metaFile = metaFile;
    }

    @Override
    public void run() {
        try {
            Path destination = destDirMedia(Paths.get(to.toAbsolutePath().toString(), numBloc + ""), metaFile.getCreated());
            Files.createDirectories(destination);

            Files.copy(
                    metaFile.getPath(),
                    Paths.get(
                            destination.toAbsolutePath().toString(),
                            metaFile.getPath().getFileName().toString()
                    ), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Logger.getLogger(CopyFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Path destDirMedia(Path to, LocalDateTime date) {
        Optional<Condition> con = MediaBackupApp.conditions.stream().filter(c -> c.accept(date)).findFirst();

        String mois = date.format(DateTimeFormatter.ofPattern("MMMM", Locale.FRENCH));
        String jr = "";
        if (byDay) {
            jr = date.getDayOfMonth() + "";
        }

        if (con.isPresent()) {
            if (con.get().oneDay() && byDay) {
                jr = jr+ " - "+con.get().getLabel();
            } else {
                mois = mois+" - "+con.get().getLabel();
            }
        }
        return Paths.get(to.toAbsolutePath().toString(),
                "" + date.getYear(),
                "" + mois,
                jr);
    }

}
