package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static gitlet.Utils.*;

public class InitRepository {

    /** The current directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The .gitlet/objects directory. */
    public static final File GITLET_OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The .gitlet/refs directory. */
    public static final File GITLET_REFS_DIR = join(GITLET_DIR, "refs");
    /** The .gitlet/refs/heads directory. */
    public static final File GITLET_HEADS_DIR = join(GITLET_REFS_DIR, "heads");

    /** The .gitlet/index file. */
    public static final File GITLET_INDEX_FILE = join(GITLET_DIR, "index");
    /** The .gitlet/HEAD file. */
    public static final File GITLET_HEAD_FILE = join(GITLET_DIR, "HEAD");

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit:
     * a commit that contains no files and has the commit message "initial commit".
     * It will have a single branch: master, which initially points to this initial commit,
     * and master will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
     * */
    public static void initializeRepository() throws IOException {
        prepareRepository();
        String hashValue = commit();
        Repository.updateHeadAfterCommit(hashValue);
    }

    /**
     * Create necessary folders and files that do not exist.
     * Including folders that store blobs, trees, and commits,
     * and folders that store heads.
     * */
    private static void prepareRepository() throws IOException {
        if (GITLET_DIR.isDirectory()) {
            exit("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdirs();
        GITLET_OBJECTS_DIR.mkdirs();
        GITLET_REFS_DIR.mkdirs();
        GITLET_HEADS_DIR.mkdirs();

        GITLET_HEAD_FILE.createNewFile();

        // empty staging area
        Repository.resetStage();

        // path to master head
        File masterHeadRelativePath = Repository.relativeSimplePath(GITLET_DIR,
                join(GITLET_HEADS_DIR, "master"));
        writeContents(GITLET_HEAD_FILE, masterHeadRelativePath.toString());
    }

    /** Initial commit. */
    private static String commit() {
        Commit initCommit = new Commit();

        return Repository.writeObjectInDir(initCommit);
    }
}
