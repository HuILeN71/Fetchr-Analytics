package nl.dacolina.fetchranalytics.managers;

import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// This manager will run at the start of the game and will check if the datapack is edited, if it is not, it will
// add some custom modifications that are required for the game to work. It's only a few lines but mandatory.
public class DatapackManager {

    private static final String ADDTAGCOMMAND = "\n" + "$tag @s add fetchranalytics.has_slot.$(slot_id)";
    private static final String REMOVETAGCOMAND = "tag @s remove fetchranalytics.has_slot.";
    // Find any version of Fetchr, so if version changes it will still work
    private static final String TARGETFOLDERREGEX = "Fetchr-.*";
    private static final String PATHTODATAPACK = "datapacks";
    private static final String ZIPFILENAME = "Fetchr.zip";
    private static final String SKYBOXFILE = "data" + File.separator + "fetchr" + File.separator +
            "function" + File.separator + "game" + File.separator + "skybox" + File.separator + "join_game.mcfunction";
    private static final String BACKGROUNDFILE = "data" + File.separator + "fetchr" + File.separator +
            "function" + File.separator + "item_detection" + File.separator + "set_background.mcfunction";

    private boolean datapackIsInstalled;

    public DatapackManager(Path rootFolder) {
        // Get the folder path, if null is returned that means the datapack is not installed
        String datapackFolderPath = findFetchrDatapackFolder(rootFolder);

        if(datapackFolderPath != null) {

            String destinationPathExtractedFiles = datapackFolderPath + File.separator + "extracted";

            if(extractZipFile(datapackFolderPath, ZIPFILENAME, destinationPathExtractedFiles)) {
                if(writeToEndOfFile(constructPathHelper(destinationPathExtractedFiles, SKYBOXFILE), createTagRemoveCommands(REMOVETAGCOMAND)) &&
                        writeToEndOfFile(constructPathHelper(destinationPathExtractedFiles, BACKGROUNDFILE), ADDTAGCOMMAND)) {

                }
            } else {
                FetchrAnalytics.LOGGER.error("Something went wrong while extracting the datapack!");
                this.datapackIsInstalled = false;
            }
        } else {
            this.datapackIsInstalled = false;
        }

    }

    private static boolean writeToEndOfFile(String pathToFile, String content) {
        try {
            writeLine(pathToFile, content);
            System.out.println("Line added successfully!");
            return true;
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
            return false;
        }

    }

    private static void writeLine(String filePath, String lineToAdd) throws IOException {
        // Open the file in append mode using FileWriter
        try (FileWriter fw = new FileWriter(filePath, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(lineToAdd); // Write the new line
                bw.newLine(); // Add a newline character
        }
    }

    private static String constructPathHelper(String pathToExtractedFolder, String datapackFile) {
        return pathToExtractedFolder + File.separator + datapackFile;
    }

    private static String createTagRemoveCommands(String baseCommand) {
        int boardSize = 25;
        StringBuilder allCommands = new StringBuilder();

        for (int i = 0; i < boardSize; i++) {
            allCommands.append("\n").append(baseCommand).append(i);
        }

        return allCommands.toString();

    }

    private static boolean extractZipFile(String folderPath, String zipFileName, String destinationPath) {
        File zipFile = new File(folderPath, zipFileName);
        File destinationDirectory = new File(destinationPath);

        if (zipFile.exists() && zipFile.isFile()) {
            try {
                unzip(zipFile, destinationDirectory);
                System.out.println("ZIP file extracted successfully to: " + destinationPath);
                return true;
            } catch (IOException e) {
                System.err.println("Error while extracting ZIP file: " + e.getMessage());
            }
        } else {
            System.err.println("ZIP file not found: " + zipFile.getAbsolutePath());
        }
        return false;

    }

    // This function is checking for the Fetchr folder and adding the path after it to the location of the Fetchr,zip

    private static String findFetchrDatapackFolder(Path rootFolder) {

        //Check if directory has been passed to the function
        if (rootFolder.toFile().isDirectory()) {
            String pathToFolder = getFetchrFolder(rootFolder.toFile(), TARGETFOLDERREGEX);

            if(pathToFolder != null) {
                return pathToFolder + File.separator + PATHTODATAPACK;
            }
        }

        return null;

    }

    private static String getFetchrFolder(File targetDirectory, String regex) {
        File[] files = targetDirectory.listFiles(); // Get all files and folders
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().matches(regex)) {
                    return file.getPath(); // Folder matching regex found
                }
            }
        }
        return null; // No matching folder found
    }

    // Some kind of unzip function from the internet
    private static void unzip(File zipFile, File destinationDir) throws IOException {
        if (!destinationDir.exists()) {
            destinationDir.mkdirs(); // Create destination directory if it doesn't exist
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File file = new File(destinationDir, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }


}
