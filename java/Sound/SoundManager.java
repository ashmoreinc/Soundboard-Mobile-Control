package Sound;

import java.io.*;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/*
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
*/
public class SoundManager {
    private final String fileRoot;
    private final String manifestFolder = "Manifest";
    private final String manifestFile = "manifest.csv";
    private final String soundFolder = "Audio";

    private final Map<String, String> sounds;

    public SoundManager() throws Exception {
        sounds = new HashMap<>();
        this.fileRoot = "Files";

        initialise();
    }

    public SoundManager(String fileRoot) throws Exception {
        sounds = new HashMap<>();
        this.fileRoot = fileRoot;

        initialise();
    }

    // Handle File names and storage
    private void initialise () throws Exception {
        if(checkManifest()){
            parseFilenames();
        } else {
            String manifestLoc = fileRoot + "/" + manifestFolder  + "/" + manifestFile;
            throw new Exception("Could not create/find the manifest at '" + manifestLoc + "'");
        }
    }

    private boolean checkManifest () {
        // Checks if the manifest exists. If it doesn't create one.
        String manifestLoc = fileRoot + "/" + manifestFolder  + "/" + manifestFile;

        File manifest = new File(manifestLoc);

        if(!manifest.exists()){
            try {
                return manifest.createNewFile();
            } catch (IOException e) {
                System.err.println("Exception creating the manifest.");
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private void parseFilenames () throws FileNotFoundException {
        // Get the files and then store them in the soundFiles list
        String manifestLoc = fileRoot + "/" + manifestFolder  + "/" + manifestFile;

        File manifest = new File(manifestLoc);

        Scanner myReader = new Scanner(manifest);
        while(myReader.hasNextLine()) {
            String line = myReader.nextLine();
            String[] splitLine = line.split(",");

            if (splitLine.length != 2) {
                System.out.println("Disregarding line: " + line);
                System.out.println("Reason: Too many delimiters.");
            } else {
                sounds.put(splitLine[0], splitLine[1]);
            }

        }
    }

    public Map<String, String> getSounds () {
        return sounds;
    }

    public boolean manifestAddEntry(String name, String filename) throws FileNotFoundException{
        // Add a entry to the CSV File
        if (sounds.containsKey(name) || sounds.containsValue(filename)) {
            return false;
        } else {
            String manifestLoc = fileRoot + "/" + manifestFolder  + "/" + manifestFile;

            File manifest = new File(manifestLoc);
            if (!manifest.exists()){
                throw new FileNotFoundException();
            }
            try {
                FileWriter writer = new FileWriter(manifestLoc, true);
                writer.write(name + "," + filename  + "\n");
                writer.close();

                sounds.put(name, filename);
                return true;
            } catch (IOException e) {
                System.err.println("Could not open file " + manifestLoc);
                return false;
            }
        }
    }

    public boolean manifestRemoveEntry(String name) {
        sounds.remove(name);

        try {
            manifestRewriteAll();
            return true;
        } catch (FileNotFoundException e) {
            System.err.println("Could not open the manifest.");
            return false;
        }
    }

    private void manifestRewriteAll() throws FileNotFoundException {
        String manifestLoc = fileRoot + "/" + manifestFolder + "/" + manifestFile;

        File manifest = new File(manifestLoc);

        // Check the file currently exists
        if (!manifest.exists()) {
            throw new FileNotFoundException();
        }

        try {
            FileWriter writer = new FileWriter(manifest);

            sounds.forEach((name, filename) -> {
                try {
                    writer.write(name + "," + filename + "\n");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            writer.close();

        } catch (IOException e) {
            System.err.println("Could not open '" + manifest.getPath() + "'.");
            e.printStackTrace();
        }
    }

    public boolean addNewFile(String originalLocation, String name) throws IOException {
        // Check if we already have that name
        if(sounds.containsKey(name)) {
            return false;
        }

        // Check if the given file exists
        File origin = new File(originalLocation);
        if(!origin.exists()){
            throw new FileNotFoundException();
        }

        // Create the new file name, add the time to the filename if the filename is already found
        File newFile = new File(fileRoot + "/" + soundFolder  + "/" + origin.getName());
        while(newFile.exists()){
            newFile = new File(fileRoot + "/" + soundFolder  + "/" + System.currentTimeMillis() + "_" + origin.getName());
        }

        // Copy the file to our new location
        Files.copy(Path.of(origin.getPath()), new FileOutputStream(newFile));

        // Add the entry to the manifest
        return manifestAddEntry(name, newFile.getName());
    }

    public boolean removeFile(String filename){
        // TODO: Create the remove file function
        System.out.println("Does nothing yet.");
        return true;
    }

    // Control sounds

    public synchronized void playSound (String soundName) throws Exception {
        if(!sounds.containsKey(soundName)){
            throw new Exception("Sound with name " + soundName + " does not exist.");
        }
        String soundLoc = fileRoot + "/" + soundFolder + "/" + sounds.get(soundName);

        System.out.println("Playing: " + soundLoc);
        /*
        Platform.startup(() -> {
            Media sound = new Media(new File(soundLoc).toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        });
        */
    }

}
