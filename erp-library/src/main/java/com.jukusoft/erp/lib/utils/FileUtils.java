package com.jukusoft.erp.lib.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Utils for file operations
 *
 * Created by Justin on 24.08.2016.
 */
public class FileUtils {

    /**
     * read content from file
     *
     * @param path
     *            path to file
     * @param encoding
     *            file encoding
     *
     * @throws IOException
     *
     * @return content of file as string
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        if (!new File(path).exists()) {
            throw new IOException("File doesnt exists: " + path);
        }

        if (!new File(path).canRead()) {
            throw new IOException("File isnt readable, please correct permissions: " + path);
        }

        // read bytes from file
        byte[] encoded = Files.readAllBytes(Paths.get(path));

        // convert bytes to string with specific encoding and return string
        return new String(encoded, encoding);
    }

    /**
     * read lines from file
     *
     * @param path
     *            path to file
     * @param charset
     *            encoding of file
     *
     * @throws IOException
     *
     * @return list of lines from file
     */
    public static List<String> readLines(String path, Charset charset) throws IOException {
        if (!(new File(path)).exists()) {
            throw new FileNotFoundException("Couldnt find file: " + path);
        }

        return Files.readAllLines(Paths.get(path), charset);
    }

    /**
     * write text to file
     *
     * @param path
     *            path to file
     * @param content
     *            content of file
     * @param encoding
     *            file encoding
     *
     * @throws IOException
     */
    public static void writeFile(String path, String content, Charset encoding) throws IOException {
        Files.write(Paths.get(path), content.getBytes(encoding), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
    * get path to user.home directory
     *
     * @return path to user.home directory
    */
    public static String getUserHomeDir () {
        return System.getProperty("user.home");
    }

    public static String getAppHomeDir (String appName) {
        return getUserHomeDir() + "/." + appName + "/";
    }

}
