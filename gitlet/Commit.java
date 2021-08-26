package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;

import static gitlet.Repository.getCurrentHeadCommitID;
import static gitlet.Utils.sha1;
import static gitlet.Utils.serialize;

/**
 * Represents a gitlet commit object.
 * Stores the hash values of parent, second parent (in merge), and file tree.
 * Stores message and date.
 *
 *  @author Zichuan
 */
public class Commit implements Serializable, Dumpable {

    /** The message of this Commit. */
    private final String message;
    private final Date date;
    private final String parent;
    private final String secondParent;
    private final String tree;

    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parent = "";
        secondParent = "";
        tree = "";
    }

    public Commit(String msg, String treeHashValue) {
        message = msg;
        date = new Date();
        parent = getCurrentHeadCommitID();
        tree = treeHashValue;
        secondParent = "";
    }

    public Commit(String msg, String treeHashValue, String secondParentHashValue) {
        message = msg;
        date = new Date();
        parent = getCurrentHeadCommitID();
        tree = treeHashValue;
        secondParent = secondParentHashValue;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getParentHashValue() {
        return parent;
    }

    public String getSecondParentHashValue() {
        return secondParent;
    }

    public boolean hasParent() {
        return parent.isEmpty();
    }

    public boolean hasSecondParent() {
        return secondParent.isEmpty();
    }

    public Commit getParentCommit() {
        return getCommitByID(parent);
    }

    public Commit getSecondParentCommit() {
        return getCommitByID(secondParent);
    }

    private Commit getCommitByID(String hashValue) {
        if (hashValue.isEmpty()) {
            return null;
        }
        return Repository.readCommitObjectByID(hashValue);
    }

    public String getTreeHashValue() {
        return tree;
    }

    public FileTree getFileTree() {
        if (tree.isEmpty()) {
            return new FileTree();
        }
        return Repository.readObjectFromObjectsByID(tree, FileTree.class);
    }

    public Map<File, String> getFileTreeMap() {
        return getFileTree().getStage();
    }

    /** Prints commit message and other data. */
    @Override
    public void dump() {
        StringBuilder strDate = new StringBuilder();
        Formatter formatter = new Formatter(strDate, Locale.US);
        formatter.format("Date: %1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
        System.out.println("===");
        System.out.printf("commit %s\n", sha1(serialize(this)));
        if (!secondParent.isEmpty()) {
            System.out.printf("Merge: %s %s\n", parent.substring(0, 7), secondParent.substring(0, 7));
        }
        System.out.println(strDate);
        System.out.println(message);
        System.out.println("");
    }

    public CommitTraversal traverseBack() {
        return new CommitTraversal(this);
    }

    public void printID() {
        System.out.println(sha1(serialize(this)));
    }

}
