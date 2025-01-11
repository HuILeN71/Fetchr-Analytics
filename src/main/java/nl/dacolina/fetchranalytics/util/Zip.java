package nl.dacolina.fetchranalytics.util;

import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    private String destinationDirectory;
    private String targetDirectory;
    private String zipFileName;

    public Zip(String destinationDirectory, String targetDirectory, String zipFileName) {

        this.zipFileName = zipFileName;
        this.destinationDirectory = destinationDirectory;
        this.targetDirectory = targetDirectory + File.separator + File.separator + this.zipFileName;

        try {
            File folderToZip = new File(this.destinationDirectory);

            if (!folderToZip.exists()) {
                throw new FileNotFoundException("The estracted folder was not found!");
            }

            try {

                FetchrAnalytics.LOGGER.info("poep");

                File zipFileObj = new File(this.targetDirectory);
                if (zipFileObj.exists() && !zipFileObj.delete()) {
                    throw new IOException("Failed to delete existing ZIP file: " + this.targetDirectory);
                }

                FileOutputStream fos = new FileOutputStream(this.targetDirectory);
                ZipOutputStream zos = new ZipOutputStream(fos);


                File[] files = folderToZip.listFiles();
                if (files != null) {
                    for (File file : files) {
                        zipFolder(file, "", zos); // Pass an empty string for the base path
                    }
                }

                fos.close();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        } catch (IOException e) {

        }


    }

    private static void zipFolder(File file, String basePath, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            // Recursively zip subfolders
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    zipFolder(child, basePath + file.getName() + "/", zos);
                }
            }
        } else {
            // Add file to ZIP
            try (FileInputStream fis = new FileInputStream(file)) {
                String zipEntryName = basePath + file.getName();
                System.out.println("Adding file to zip: " + zipEntryName);
                zos.putNextEntry(new ZipEntry(zipEntryName));

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

}
