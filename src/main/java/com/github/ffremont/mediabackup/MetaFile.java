/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.mediabackup;

import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 *
 * @author florent
 */
public class MetaFile {
    private LocalDateTime created;
    private Path path;
    private long size;

    public MetaFile(LocalDateTime created, Path path, long size) {
        this.path = path;
        this.size = size;
        this.created = created;
    }
    
    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
    
}
