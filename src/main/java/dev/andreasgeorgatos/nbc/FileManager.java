package dev.andreasgeorgatos.nbc;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Scanner;

public class FileManager {
    private final Logger logger = LoggerFactory.getLogger(FileManager.class);

    private File trainingData;

    public FileManager() {
        trainingData = getFile("training");
        logger.info("The file {} has been read.", trainingData.getName());
    }


    @NotNull
    private File getFile(String fileName) {
        logger.info("What's the name of the {} file", fileName);

        Scanner scanner = new Scanner(System.in);

        File file = new File(scanner.nextLine());

        if (!file.exists()) {
            logger.info("File {} does not exist, please enter a valid file name.", fileName);
            return getFile(fileName);
        }

        if (!file.getName().endsWith(".csv")) {
            logger.info("File {} is not of type csv, please enter a valid file with the correct extension.", file.getName());
            return getFile(fileName);
        }

        return file;
    }

    public File getTrainingData() {
        return new File(trainingData.getAbsolutePath());
    }
}
