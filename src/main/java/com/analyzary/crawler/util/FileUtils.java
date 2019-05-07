package com.analyzary.crawler.util;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {


    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
           // TODO
        }
    }

    public static String readFile(String file)
            throws IOException {
        FileInputStream stream = new FileInputStream(file);
        try {
            Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8.name()));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } finally {
            stream.close();
        }
    }

    public static void writeFile(byte[] data, String filePath) throws IOException {
        if (!new File(filePath).exists()) {
            new File(filePath).createNewFile();
        }
        Files.write(new File(filePath).toPath(), data);
    }


    public static String filePathToHash(String path) {
        messageDigest.reset();
        messageDigest.update(path.getBytes(), 0, path.length());
        return new BigInteger(1, messageDigest.digest()).toString(16);
    }
}
