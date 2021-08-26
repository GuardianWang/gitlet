package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 * Represents a gitlet remove staging object.
 * The remove staging area contains the files 
 * that are in the last commit, but are later removed.
 * 
 * Files that are not in the last commit but
 * are in the add staging area will not appear in the remove staging area.
 * Instead, they will be simply removed from the add staging area.
 * I.e., the add staging area and the remove staging area are mutually exclusive.
 *
 * The remove staging area is represented by a set object, whose each
 * element is a File object.
 *
 *  @author Zichuan
 */
public class RemoveStage implements Serializable, StageBasic, Dumpable {
    /** The remove staging object. */
    private final Set<File> stage;

    public RemoveStage() {
        stage = new TreeSet<>();
    }

    /** Get the remove staging object. */
    public Set<File> getStage() {
        return stage;
    }

    /** Add a new file into the remove staging object. */
    public void add(File K) {
        stage.add(K);
    }

    /**
     * Remove a file from the remove staging object.
     *
     * @param K file to be removed
     * */
    public void remove(File K) {
        stage.remove(K);
    }

    /**
     * Returns {@code true} if the remove staging area contains the specified file.
     *
     * @return {@code true} if the remove staging area contains the specified file
     * */
    public boolean contains(File K) {
        return stage.contains(K);
    }

    /**
     * Returns {@code true} if the remove staging area is empty.
     *
     * @return {@code true} if the remove staging area is empty
     * */
    public boolean isEmpty() {
        return stage.isEmpty();
    }

    @Override
    public void dump() throws IOException {
        printWithHead("=== Removed Files ===", stage);
    }

}
