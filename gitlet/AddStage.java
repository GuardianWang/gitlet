package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/** 
 * Represents a gitlet add staging object.
 * The add staging area contains the files
 * 1. that are in the last commit, but are modified before added, or
 * 2. that are not in the last commit.
 * 
 * The add staging area and the remove staging area are mutually exclusive.
 * 
 * The add staging area is represented by a map object, whose
 * key is a File object and value is the string of hash value of that file.
 * 
 *
 *  @author Zichuan
 */
public class AddStage implements Serializable, StageBasic, Dumpable {
    /** The add staging object. */
    private final Map<File, String> stage;

    public AddStage() {
        stage = new TreeMap<>();
    }

    /** Get the add staging object. */
    public Map<File, String> getStage() {
        return stage;
    }

    /** Returns set of files. */
    public Set<File> getFileSet() {
        return stage.keySet();
    }

    /** Add a new (file, hash value) pair into the add staging object. */
    public void put(File K, String V) {
        stage.put(K, V);
    }

    /** Remove a (file, hash value) pair from the add staging object. */
    public void remove(File K) {
        stage.remove(K);
    }

    /** Returns the hash value of a file in the add staging area. */
    public String getHashFromFile(File K) {
        return stage.get(K);
    }

    /** Returns the hash value of a file in the add staging area,
     * or defaultValue if the file doesn't exist. */
    public String getHashFromFileOrDefault(File K, String defaultValue) {
        return stage.getOrDefault(K, defaultValue);
    }

    /** Returns true if the add staging area contains the specified file. */
    public boolean contains(File K) {
        return stage.containsKey(K);
    }

    /** Returns true if the add staging area is empty. */
    public boolean isEmpty() {
        return stage.isEmpty();
    }

    @Override
    public void dump() throws IOException {
        printWithHead("=== Staged Files ===", getFileSet());
    }
}