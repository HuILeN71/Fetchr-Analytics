package nl.dacolina.fetchranalytics.managers;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import nl.dacolina.fetchranalytics.FetchrAnalytics;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatapackManagerTest {


    private static final String ADDTAGCOMMAND = "\n" + "$tag @s add fetchranalytics.has_slot.$(slot_id)";
    private static final String REMOVETAGCOMAND = "tag @s remove fetchranalytics.has_slot.";
    // Find any version of Fetchr, so if version changes it will still work
    private static final String TARGETFOLDERREGEX = "Fetchr-.*";
    private static final String PATHTODATAPACK = "datapacks";
    private static final String ZIPFILENAME = "Fetchr.zip";
    private static final String STATCHRFILE = "statchr.rdy";
    private static final String SKYBOXFILE = "data" + File.separator + "fetchr" + File.separator +
            "function" + File.separator + "game" + File.separator + "skybox" + File.separator + "join_game.mcfunction";
    private static final String BACKGROUNDFILE = "data" + File.separator + "fetchr" + File.separator +
            "function" + File.separator + "item_detection" + File.separator + "set_background.mcfunction";

    private boolean datapackIsInstalled;

    public DatapackManagerTest(Path rootFolder) {
        // Get the folder path, if null is returned that means the datapack is not installed
        String datapackFolderPath = findFetchrDatapackFolder(rootFolder);

        if(datapackFolderPath != null) {

            if (!new File(datapackFolderPath + File.separator + STATCHRFILE).exists()) {

                String zipFilePath = datapackFolderPath + File.separator + ZIPFILENAME;
                String extractDir = datapackFolderPath + File.separator + "extracted"; // Temporary directory for extraction

                try {

                    // Step 1: Extract files from the ZIP archive
                    ZipFile zipFile = new ZipFile(zipFilePath);
                    if (!zipFile.isEncrypted()) {
                        zipFile.extractAll(extractDir); // Extract the entire contents of the ZIP
                    }

                    // Step 2: Modify the files you need to edit
                    writeToEndOfFile(extractDir + File.separator + SKYBOXFILE, createTagRemoveCommands(REMOVETAGCOMAND)); // Modify the skybox file to clear all the created tags
                    writeToEndOfFile(extractDir + File.separator + BACKGROUNDFILE, ADDTAGCOMMAND); // Modify the background file to add the tag command

                    // Step 3: Replace the original files in the ZIP with the modified ones
                    zipFile.removeFile(SKYBOXFILE);
                    zipFile.removeFile(BACKGROUNDFILE);

                    ZipParameters zipParameters = new ZipParameters();
//                    zipFile.setCharset(null);
                    zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
                    zipParameters.setCompressionLevel(CompressionLevel.NORMAL);

                    // Set path inside zip to skyboxfile
                    zipParameters.setFileNameInZip(SKYBOXFILE);
                    zipFile.addFile(new File(extractDir + File.separator + SKYBOXFILE), zipParameters);

                    // Set path inside zip to background file;
                    zipParameters.setFileNameInZip(BACKGROUNDFILE);
                    zipFile.addFile(new File(extractDir + File.separator + BACKGROUNDFILE), zipParameters);

                    FetchrAnalytics.LOGGER.info("Datapack has been modified succesfully!");

                    // Clean - up Task: Delete extracted Folder
                    deleteExtractedFolder(extractDir);

                    createStatchrFile(datapackFolderPath);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                FetchrAnalytics.LOGGER.info("The datapack has already been modified. No more work to do!");
            }
        }

    }

    private static void createStatchrFile(String directory) throws IOException {
        File statchrFile = new File(directory + File.separator + STATCHRFILE);

        if(statchrFile.createNewFile()) {
            FetchrAnalytics.LOGGER.debug("File already statchr.rdy has been created!");
        } else {
            FetchrAnalytics.LOGGER.debug("File already statchr.rdy exists!");
        }

    }

    private static boolean deleteExtractedFolder(String directory) {
        Path path = Paths.get(directory);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);  // Delete file
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);  // Delete directory after contents are deleted
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("Directory and its contents deleted successfully.");
        } catch (IOException e) {
            System.err.println("Error deleting directory: " + e.getMessage());
        }


        return true;
    }

    private static boolean writeToEndOfFile(String pathToFile, String content) {
        try {
            writeLine(pathToFile, content);
            FetchrAnalytics.LOGGER.debug("Line added successfully!");
            return true;
        } catch (IOException e) {
            FetchrAnalytics.LOGGER.debug("An error occurred: " + e.getMessage());
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

    private static String createTagRemoveCommands(String baseCommand) {
        int boardSize = 25;
        StringBuilder allCommands = new StringBuilder();

        for (int i = 0; i < boardSize; i++) {
            allCommands.append("\n").append(baseCommand).append(i);
        }

        return allCommands.toString();

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

}
