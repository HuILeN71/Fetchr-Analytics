package nl.dacolina.fetchranalytics.util;

import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {


    public static void zipFolder(String folderToZip, String zipFilePath) {
        Path sourceFolder = Paths.get(folderToZip);
        File zipFile = new File(zipFilePath);

        // Delete the existing zip file if it exists
        if (zipFile.exists()) {
            boolean deleted = zipFile.delete();
            if (deleted) {
                System.out.println("Existing zip file deleted: " + zipFilePath);
            } else {
                System.out.println("Failed to delete the existing zip file: " + zipFilePath);
            }
        }


        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Files.walk(sourceFolder)
                    .filter(path -> !Files.isDirectory(path))  // Exclude directories
                    .forEach(path -> {
                        try {
                            // Create a relative path for the file inside the zip
                            Path relativePath = sourceFolder.relativize(path);

                            // Create a new ZipEntry for each file
                            zipOutputStream.putNextEntry(new ZipEntry(relativePath.toString()));

                            // Write the file content to the zip output stream
                            Files.copy(path, zipOutputStream);

                            // Close the current zip entry
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
