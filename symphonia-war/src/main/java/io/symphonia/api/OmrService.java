package io.symphonia.api;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import omr.WellKnowns;

/**
 * Service to provide a 'bridge' to the OMR core services.
 * 
 * @author sbunciak
 *
 */
@Singleton
public class OmrService {

    public OmrService() {
        // We need class WellKnowns to be elaborated before class Main
        WellKnowns.ensureLoaded();
    }

    /**
     * Methods performs OMR and returns MusicXML v3 String representation.
     * 
     * @param {@link java.io.File}
     * @return {@link java.io.File} xml
     * @throws IOException
     */
    public File getMusicXmlFromImage(File imageFile) throws IOException {
        File tmpOutput = File.createTempFile("musicXmlOutput", ".xml", WellKnowns.DATA_FOLDER);
        
        String[] args = {"-batch", "-input", imageFile.getAbsolutePath(), "-export", tmpOutput.getAbsolutePath()};

        // Then we call Main...
        omr.Main.doMain(args);

        return tmpOutput;
    }

    /**
     * Methods performs OMR and returns a PDF file.
     * 
     * @param {@link java.io.File}
     * @return {@link java.io.File} pdf
     * @throws IOException
     */
    public File getPdfFromImage(File imageFile) throws IOException {
        File tmpOutput = File.createTempFile("pdfOutput", ".pdf", WellKnowns.DATA_FOLDER);
        
        String[] args = {"-batch", "-input", imageFile.getAbsolutePath(), "-print", tmpOutput.getAbsolutePath()};

        // Then we call Main...
        omr.Main.doMain(args);

        return tmpOutput;
    }
}
