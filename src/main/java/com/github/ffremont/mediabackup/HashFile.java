/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ffremont.mediabackup;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author florent
 */
public class HashFile {

    private HashFile() {
    }

    public static String sha1(Path file) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            try (DigestInputStream digestIs = new DigestInputStream(new FileInputStream(file.toFile()), md)) {
                byte[] buffer = new byte[10240]; // 10ko
                while (0 < digestIs.read(buffer)) {
                }

                digestIs.close();
            }

            return String.format("%032X", new BigInteger(1, md.digest()));
        } catch (NoSuchAlgorithmException | IOException ex) {
            Logger.getLogger(HashFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
}
