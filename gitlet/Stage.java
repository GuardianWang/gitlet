package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Represents a gitlet stage object.
 * A stage object consists of an {@link AddStage} object
 * and a {@link RemoveStage} object.
 * The staging area consists of change of files to be committed.
 *
 * @author Zichuan
 */
public class Stage implements Serializable, Dumpable {

    /** Add staging object. */
    private final AddStage addStage;
    /** Remove staging object. */
    private final RemoveStage removeStage;

    public Stage() {
        addStage = new AddStage();
        removeStage = new RemoveStage();
    }

    /** Returns the add staging area object. */
    public Map<File, String> getAddStage() {
        return addStage.getStage();
    }

    /** Returns all files in add staging area. */
    public Set<File> getAddStageFileSet() {
        return addStage.getStage().keySet();
    }

    /** Returns the remove staging area object. */
    public Set<File> getRemoveStage() {
        return removeStage.getStage();
    }

    /** Add a new (file, hash value) pair into the add staging object. */
    public void addToAddStage(File K, String V) {
        addStage.put(K, V);
    }

    /** Add a new file into the remove staging object. */
    public void addToRemoveStage(File K) {
        removeStage.add(K);
    }

    /** Remove a (file, hash value) pair from the add staging object. */
    public void removeFromAddStage(File K) {
        addStage.remove(K);
    }

    /**
     * Remove a file from the remove staging object.
     *
     * @param K file to be removed
     * */
    public void removeFromRemoveStage(File K) {
        removeStage.remove(K);
    }

    /** Returns the hash value of a file in the add staging area. */
    public String getHashAddStage(File K) {
        return addStage.getHashFromFile(K);
    }

    /** Returns true if the add staging area contains the specified file. */
    public boolean containsFileInAddStage(File K) {
        return addStage.contains(K);
    }

    /**
     * Returns {@code true} if the remove staging area contains the specified file.
     *
     * @return {@code true} if the remove staging area contains the specified file
     * */
    public boolean containsFileInRemoveStage(File K) {
        return removeStage.contains(K);
    }

    /** Returns true if the add staging area is empty. */
    public boolean isAddStageEmpty() {
        return addStage.isEmpty();
    }

    /**
     * Returns {@code true} if the remove staging area is empty.
     *
     * @return {@code true} if the remove staging area is empty
     * */
    public boolean isRemoveStageEmpty() {
        return removeStage.isEmpty();
    }

    /**
     * Returns {@code true} if the staging area is empty.
     *
     * @return {@code true} if the staging area is empty
     * */
    public boolean isEmpty() {
        return isAddStageEmpty() && isRemoveStageEmpty();
    }

    @Override
    public void dump() throws IOException {
        addStage.dump();
        removeStage.dump();
    }
}
