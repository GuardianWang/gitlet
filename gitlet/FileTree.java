package gitlet;


import java.io.File;

/**
 * Represents a gitlet stage object.
 * It's essentially a map from file to hash value.
 *
 *  @author Zichuan
 */
public class FileTree extends AddStage {

    public FileTree() {
        super();
    }

    /** Based on the file tree, add files in add staging area and removes files in remove staging area. */
    public void update(Stage stage) {
        for (File file : stage.getAddStage().keySet()) {
            put(file, stage.getHashAddStage(file));
        }

        for (File file : stage.getRemoveStage()) {
            remove(file);
        }
    }
}
