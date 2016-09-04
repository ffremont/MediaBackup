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
    private String hash;
    
    public MetaFile(Path path, long size, String hash) {
        this.path = path;
        this.size = size;
        this.hash = hash;
    }
    
    public Path path() {
        return path;
    }

    public void path(Path path) {
        this.path = path;
    }

    public long size() {
        return size;
    }

    public void size(long size) {
        this.size = size;
    }

    public LocalDateTime created() {
        return created;
    }

    public void created(LocalDateTime created) {
        this.created = created;
    }

    public String hash() {
        return hash;
    }

    public void hash(String hash) {
        this.hash = hash;
    }
}
