package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An interface describing staging area objects.
 * @author Zichuan
 * */
public interface StageBasic {

    /** Remove a file. */
    void remove(File K);

    /** Check if a staging area contains the given file. */
    boolean contains(File K);

    /** Check if a staging area is empty. */
    boolean isEmpty();

    /** Returns the list of staged file names in lexicographic order. */
    static List<String> toSortedList(Set<File> fileSet) throws IOException {
        List<String> filePathList = new ArrayList<>();
        for (File file : fileSet) {
            file = Repository.getFileRelativeToCWD(file);
            filePathList.add(file.toString());
        }
        Collections.sort(filePathList);

        return filePathList;
    }

    /** Prints all staged files, each in a separate line. */
    default void print(List<String> list) {
        for (String s : list) {
            System.out.println(s);
        }
    }

    /**
     * Prints head at the front.
     * Prints all staged files, each in a separate line.
     * Ends with a blank line.
     * The list of staged file names are from the specified fileSet.
     * */
    default void printWithHead(String head, Set<File> fileSet) throws IOException {
        List<String> filePathList = toSortedList(fileSet);
        System.out.println(head);
        print(filePathList);
        System.out.println("");
    }
}
