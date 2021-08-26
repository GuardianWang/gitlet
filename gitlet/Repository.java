package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;


/**
 * Represents a gitlet repository.
 * Supports init, add, commit, rm, log, global-log, find,
 * status, checkout, branch, rm-branch, reset, merge.
 * Also, supports sub-directory and can find the WORK_DIR recursively.
 * @author Zichuan Wang
 */
public class Repository {

    /** The current directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The current working directory. */
    public static final File WORK_DIR = getWorkRootDir();
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(WORK_DIR, ".gitlet");
    /** The .gitlet/objects directory. */
    public static final File GITLET_OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The .gitlet/refs directory. */
    public static final File GITLET_REFS_DIR = join(GITLET_DIR, "refs");
    /** The .gitlet/refs/heads directory. */
    public static final File GITLET_HEADS_DIR = join(GITLET_REFS_DIR, "heads");
    /** The .gitlet/refs/remotes directory. */
    public static final File GITLET_REMOTES_DIR = join(GITLET_REFS_DIR, "remotes");
    /** The .gitlet/refs/paths file. */
    public static final File GITLET_REMOTE_PATHS_DIR = join(GITLET_REFS_DIR, "paths");

    /** The .gitlet/index file. */
    public static final File GITLET_INDEX_FILE = join(GITLET_DIR, "index");
    /** The .gitlet/HEAD file. */
    public static final File GITLET_HEAD_FILE = join(GITLET_DIR, "HEAD");

    /**
     * Returns the path of working directory.
     * Find the path by recursively going back to parent directory and
     * check if .gitlet exists.
     * */
    static File getWorkRootDir() {
        File p = CWD;
        while (p != null) {
            if (join(p, ".gitlet").isDirectory()) {
                return p;
            }
            p = p.getParentFile();
        }
        exit("not a gitlet repository (or any of the parent directories): .gitlet");
        return null;
    }

    /**
     * Update the commit hash value of head after each commit.
     * @param commitID Hash value of last commit.
     * */
    protected static void updateHeadAfterCommit(String commitID) {
        File currentHead = getCurrentHeadFile();
        writeContents(currentHead, commitID);
    }

    /**
     * Get the file that stores head in
     * .gitlet/refs/heads/[branch]
     * @return The file that stores head.
     * */
    private static File getCurrentHeadFile() {
        return join(GITLET_DIR, readContentsAsString(GITLET_HEAD_FILE));
    }

    /** Get ID of the last commit. */
    public static String getCurrentHeadCommitID() {
        File currentHead = getCurrentHeadFile();
        return readContentsAsString(currentHead);
    }

    /** Read the commit object of last commit. */
    private static Commit readCurrentHeadCommitObject() {
        String lastCommitID = getCurrentHeadCommitID();
        return readCommitObjectByID(lastCommitID);
    }

    /** Returns the commit object by the specified commit hash value. */
    protected static Commit readCommitObjectByID(String commitID) {
        File lastCommitFile = getFileInObjectsByID(commitID);
        return readObject(lastCommitFile, Commit.class);
    }

    /** Returns the staging area object */
    private static Stage readStageObject() {
        return readObject(GITLET_INDEX_FILE, Stage.class);
    }

    /** Saves the staging area object */
    private static void writeStageObject(Stage stage) {
        writeObject(GITLET_INDEX_FILE, stage);
    }

    /** Get the relative File object in .gitlet/objects/ based on HASHVALUE. */
    protected static File getRelFileInObjectsByID(String hashValue) {
        return join(hashValue.substring(0, 2), hashValue.substring(2));
    }

    /** Get the File object in .gitlet/objects/ based on hashValue. */
    private static File getFileInObjectsByID(String hashValue) {
        return join(GITLET_OBJECTS_DIR, getRelFileInObjectsByID(hashValue).toString());
    }

    /** Returns the specified object by reading the content in .gitlet/objects based on hash value. */
    protected static <T extends Serializable> T readObjectFromObjectsByID(String hashValue,
                                                                          Class<T> expectedClass) {
        File commit = Repository.getFileInObjectsByID(hashValue);
        return Utils.readObject(commit, expectedClass);
    }

    /** Check if a File object is in .gitlet/objects/ based on HASHVALUE. */
    private static boolean isFileInObjects(String hashValue) {
        return getFileInObjectsByID(hashValue).isFile();
    }

    /** Returns the hash code of a file. */
    private static String sha1OfFile(File f) {
        byte[] fileContent = readContents(f);

        return sha1(fileContent);
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * If the current working version of the file is identical to the version
     * in the current commit, do not stage it to be added,
     * and remove it from the staging area if it is already there
     * (as can happen when a file is changed, added, and then changed back to it’s original version).
     * */
    public static void addFile(String filename) throws IOException {
        File fileToAdd = new File(filename);
        if (!fileToAdd.isFile()) {
            exit("File does not exist.");
        }
        File relativePathFileToAdd = relativeSimplePath(fileToAdd.getAbsoluteFile());
        addFile(relativePathFileToAdd);
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * If the current working version of the file is identical to the version
     * in the current commit, do not stage it to be added,
     * and remove it from the staging area if it is already there
     * (as can happen when a file is changed, added, and then changed back to it’s original version).
     *
     * @param file Relative path to WORK_DIR.
     * */
    private static void addFile(File file) {
        Stage stage = readStageObject();

        // hash value of file content
        String hashValue = sha1OfFile(getAbsoluteFileFromWorkDir(file));

        // update staging area
        stage.removeFromRemoveStage(file);
//        System.out.printf("Remove from remove staging %s\n", relativePathFileToAdd);  // debug
        if (inLastCommit(file, hashValue)) {
            stage.removeFromAddStage(file);
//            System.out.printf("Remove from add staging %s: %s\n", relativePathFileToAdd, hashValue);  // debug
        } else {
            stage.addToAddStage(file, hashValue);
//            System.out.printf("Add to add staging %s: %s\n", relativePathFileToAdd, hashValue);  // debug
        }
        writeStageObject(stage);

        // save blob
        if (!isFileInObjects(hashValue)) {
            writeContentsInBytes(getAbsoluteFileFromWorkDir(file), getFileInObjectsByID(hashValue));
//            System.out.printf("Add to objects %s: %s\n", relativePathFileToAdd, hashValue);  // debug
        }
    }

    /**
     * Returns the simplified relative path.
     * @param src Base path.
     * @param dest Target path.
     * @return Src/return is target.
     * */
    protected static File relativeSimplePath(File src, File dest) throws IOException {
        return src.getCanonicalFile().toPath().relativize(
                dest.getCanonicalFile().toPath()).toFile();
    }

    /**
     * Returns the simplified path relative to WORK_DIR.
     * @param dest Target path.
     * @return Src/return is target.
     * */
    protected static File relativeSimplePath(File dest) throws IOException {
        return WORK_DIR.getCanonicalFile().toPath().relativize(
                dest.getCanonicalFile().toPath()).toFile();
    }

    /** Check if the file to add is among the most recent commit.
     * Among means
     * 1. the contents, i.e., the hash values of content are the same, and
     * 2. the file paths are the same.
     * @param file The file to add.
     * @param hashValue The hash value of file content to add.
     * @return Returns true if the file to add is among the most recent commit,
     * returns false otherwise.
     * */
    private static boolean inLastCommit(File file, String hashValue) {
        FileTree fileTree = readCurrentHeadCommitObject().getFileTree();

        return isFileInTree(file, hashValue, fileTree);
    }

    private static boolean inLastCommit(File file) {
        String hashValue = sha1OfFile(getAbsoluteFileFromWorkDir(file));

        return inLastCommit(file, hashValue);
    }

    /** Returns {@code true} if the file is in the recent commit. */
    private static boolean nameInLastCommit(File file) {
        FileTree fileTree = readCurrentHeadCommitObject().getFileTree();

        return fileTree.contains(file);
    }

    /**
     * Write serializable OBJ in GITLET_OBJECTS_DIR
     * and return the sha1 hash value of OBJ.
     * @param obj Serializable object.
     * @return The sha1 hash value of OBJ.
     * */
    protected static String writeObjectInDir(Serializable obj) {
        String hashValue = sha1(serialize(obj));
        writeObject(GITLET_OBJECTS_DIR, hashValue, obj);

        return hashValue;
    }

    /**
     * Saves a snapshot of tracked files in the current commit
     * and staging area so they can be restored at a later time,
     * creating a new commit.
     * */
    public static void commit(String msg) {
        String commitTreeHashValue = getCommitTreeHashValue(msg);
        Commit newCommit = new Commit(msg, commitTreeHashValue);
        finishCommit(newCommit);
    }

    /**
     * Saves a snapshot of tracked files in the current commit
     * and staging area so they can be restored at a later time,
     * creating a new commit.
     * */
    public static void commit(String msg, String secondParentHashValue) {
        String commitTreeHashValue = getCommitTreeHashValue(msg);
        Commit newCommit = new Commit(msg, commitTreeHashValue, secondParentHashValue);
        finishCommit(newCommit);
    }

    /** Returns the hash value of commit tree. */
    private static String getCommitTreeHashValue(String msg) {
        // commit tree
        Stage stage = readStageObject();
        if (stage.isEmpty()) {
            exit("No changes added to the commit.");
        }
        if (msg.isEmpty()) {
            exit("Please enter a commit message.");
        }
        FileTree commitTree = readCurrentHeadCommitObject().getFileTree();

        commitTree.update(stage);

        return writeObjectInDir(commitTree);
    }

    /** Writes the new commit object, updates HEAD, and cleans staging area. */
    private static void finishCommit(Commit newCommit) {
        String newCommitHashValue = writeObjectInDir(newCommit);
//        System.out.printf("New commit %s\n", newCommitHashValue);  // debug

        // head
        updateHeadAfterCommit(newCommitHashValue);

        // staging area
        resetStage();
    }

    /** Clean staging area. */
    protected static void resetStage() {
        Stage emptyStage = new Stage();
        writeObject(GITLET_INDEX_FILE, emptyStage);
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory
     * if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * */
    public static void removeFile(String filename) throws IOException {
        File fileToRemove = new File(filename);
        File relativePathFileToRemove = relativeSimplePath(fileToRemove.getAbsoluteFile());
        removeFile(relativePathFileToRemove);
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory
     * if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     *
     * @param file Relative path to WORK_DIR.
     * */
    private static void removeFile(File file) {
        Stage stage = readStageObject();
        boolean containsFileInAddStage = stage.containsFileInAddStage(file);
        boolean containsFileInLastCommit = nameInLastCommit(file);

        if (!containsFileInAddStage && !containsFileInLastCommit) {
            exit("No reason to remove the file.");
        }

        // delete file from directory
        restrictedDelete(getAbsoluteFileFromWorkDir(file));
//        System.out.printf("Delete file from directory %s\n", relativePathFileToRemove);  // debug

        // update staging area
        if (containsFileInLastCommit) {
            stage.addToRemoveStage(file);
//            System.out.printf("Add to remove staging %s\n", relativePathFileToRemove);  // debug
        }
        if (containsFileInAddStage){
            stage.removeFromAddStage(file);
//            System.out.printf("Remove from add staging %s\n", relativePathFileToRemove);  // debug
        }
        writeStageObject(stage);
    }

    /**
     * Starting at the current head commit, display information about each commit
     * backwards along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents found in merge commits.
     *
     * For every node in this history, the information it should display is
     * the commit id, the time the commit was made, and the commit message.
     * */
    public static void printLog() {
        Commit commit = readCurrentHeadCommitObject();
        for (Commit c : commit.traverseBack()) {
            c.dump();
        }
    }

    /** Like log, except displays information about all commits ever made. */
    public static void printGlobalLog() {
        for (Commit c : getAllCommits()) {
            c.dump();
        }
    }

    /** Returns a list of the names of all branches in lexicographic order*/
    private static List<String> getAllBranches() {
        return plainFilenamesIn(GITLET_HEADS_DIR);
    }

    /** Returns list of head commits of all branches. */
    private static Set<String> getAllBranchIDs() {
        List<String> headList = getAllBranches();
        Set<String> headIDs = new TreeSet<>();

        assert headList != null;
        for (String h : headList) {
            File headFile = getBranchHeadFile(h);
            String headID = readContentsAsString(headFile);
            headIDs.add(headID);
        }

        return headIDs;
    }

    /** Returns a set of all distinct commits. */
    private static CommitGlobalTraversal getAllCommits() {
        // get all branch heads
        Set<String> branchIDs = getAllBranchIDs();

        // get all distinct commits
        return new CommitGlobalTraversal(branchIDs);
    }


    /** Prints out the ids of all commits that have the given commit message, one per line.
     * If there are multiple such commits, it prints the ids out on separate lines. */
    public static void findCommit(String msg) {
        int cnt = 0;
        for (Commit c : getAllCommits()) {
            if (c.getMessage().contains(msg)) {
                c.printID();
                cnt += 1;
            }
        }
        if (cnt == 0) {
            exit("Found no commit with that message.");
        }
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     *
     * A file in the working directory is "modified but not staged" if it is
     * - Tracked in the current commit, changed in the working directory, but not staged; or
     * - Staged for addition, but with different contents than in the working directory; or
     * - Staged for addition, but deleted in the working directory; or
     * - Not staged for removal, but tracked in the current commit and deleted from the working directory.
     *
     * The final category (Untracked Files) is for files present in the working directory
     * but neither staged for addition nor tracked.
     * This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge.
     * */
    public static void printStatus() throws IOException {
        // branches
        printBranches();
        // staged and removed files
        readStageObject().dump();
        // Modifications Not Staged For Commit
        printUnStagedChanges();
        // untracked files
        printUntrackedFiles();
    }

    /** Displays what branches currently exist, and marks the current branch with a *. */
    private static void printBranches() {
        List<String> branches = getAllBranches();
        String currentBranch = getCurrentBranch();
        boolean foundCurrentBranch = false;
        System.out.println("=== Branches ===");
        for (String b : branches) {
            if (!foundCurrentBranch && b.equals(currentBranch)) {
                System.out.println("*" + b);
                foundCurrentBranch = true;
            } else {
                System.out.println(b);
            }
        }
        System.out.println();
    }

    /**
     * A file in the working directory is "modified but not staged" if it is
     * - Tracked in the current commit, changed in the working directory, but not staged; or
     * - Staged for addition, but with different contents than in the working directory; or
     * - Staged for addition, but deleted in the working directory; or
     * - Not staged for removal, but tracked in the current commit and deleted from the working directory.
     * */
    private static void printUnStagedChanges() throws IOException {
        System.out.println("=== Modifications Not Staged For Commit ===");
        FileTree currentCommitFileTree = readCurrentHeadCommitObject().getFileTree();
        Stage stage = readStageObject();

        Set<String> unstaged = new TreeSet<>();

        // iterate add stage
        for (File f : stage.getAddStageFileSet()) {
            File rel = getFileRelativeToCWD(f);
            File abs = getAbsoluteFileFromWorkDir(f);
            if (!abs.exists()) {
                // does not exist
                unstaged.add(rel + " (deleted)");
            } else if (!sha1OfFile(abs).equals(stage.getHashAddStage(f))) {
                // hash value is not the same
                unstaged.add(rel + " (modified)");
            }
        }

        // iterate commit file tree
        for (File f : currentCommitFileTree.getFileSet()) {
            File rel = getFileRelativeToCWD(f);
            File abs = getAbsoluteFileFromWorkDir(f);
            if (!abs.exists() && !stage.containsFileInRemoveStage(f)) {
                // does not exist, not in remove staging area
                unstaged.add(rel + " (deleted)");
            } else if (abs.exists()) {
                String hashValue = sha1OfFile(abs);
                if (!hashValue.equals(currentCommitFileTree.getHashFromFile(f)) && !stage.containsFileInAddStage(f)) {
                    // hash value is not the same, not in add staging area
                    // if file is in the add staging area, then the logic is in the above iteration
                    unstaged.add(rel + " (modified)");
                }
            }
        }

        List<String> unstagedFileName = new ArrayList<>();
        unstagedFileName.addAll(unstaged);
        Collections.sort(unstagedFileName);
        for (String s : unstagedFileName) {
            System.out.println(s);
        }
        System.out.println();

    }

    /**
     * Returns the file path relative to CWD.
     * @param f Path relative to WORK_DIR.
     * */
    protected static File getFileRelativeToCWD(File f) throws IOException {
        return relativeSimplePath(CWD, getAbsoluteFileFromWorkDir(f));
    }

    /**
     * Returns the absolute path file.
     * @param f Relative path file from WORK_DIR.
     * */
    protected static File getAbsoluteFileFromWorkDir(File f) {
        return getAbsoluteFileFromWorkDir(f.toString());
    }

    /**
     * Returns the absolute path file.
     * @param f Relative path file from WORK_DIR.
     * */
    private static File getAbsoluteFileFromWorkDir(String f) {
        assert WORK_DIR != null;
        return join(WORK_DIR, f);
    }

    /**
     * The final category (Untracked Files) is for files present in the working directory
     * but neither staged for addition nor tracked.
     * This includes files that have been staged for removal,
     * but then re-created without Gitlet’s knowledge.
     * */
    private static void printUntrackedFiles() throws IOException {
        System.out.println("=== Untracked Files ===");
        FileTree currentCommitFileTree = readCurrentHeadCommitObject().getFileTree();
        Stage stage = readStageObject();
        List<File> allFiles = listFiles(WORK_DIR.getAbsolutePath());

        // add all files to set
        Set<File> untracked = new TreeSet<>(allFiles);

        // all files - commit file tree - add staging area
        untracked.removeAll(currentCommitFileTree.getFileSet());
        untracked.removeAll(stage.getAddStageFileSet());

        // in the remove staging area but file exists
        for (File f : stage.getRemoveStage()) {
            if (getAbsoluteFileFromWorkDir(f).exists()) {
                untracked.add(f);
            }
        }

        // file path relative to CWD
        List<File> untrackedFile = new ArrayList<>();
        for (File f : untracked) {
            untrackedFile.add(getFileRelativeToCWD(f));
        }

        Collections.sort(untrackedFile);
        for (File f : untrackedFile) {
            System.out.println(f);
        }
        System.out.println();
    }

    /** Returns the name of current branch */
    private static String getCurrentBranch() {
        File head = new File(readContentsAsString(GITLET_HEAD_FILE));
        return head.getName();
    }

    /**
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * */
    public static void checkoutFileToHeadCommit(String filename) throws IOException {
        String commitID = getCurrentHeadCommitID();
        checkoutFileToCommit(filename, commitID);
    }

    /**
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file
     * that’s already there if there is one.
     * The new version of the file is not staged.
     * */
    public static void checkoutFileToCommit(String filename, String commitID) throws IOException {
        // possibly need to get the full ID
        if (commitID.length() < 40) {
            commitID = getFullID(commitID);
        }
        // read commit object
        if (!isFileInObjects(commitID)) {
            exit("No commit with that id exists.");
        }

        // from commit tree get the file
        File file = relativeSimplePath(join(CWD, filename));
        FileTree fileTree = readCommitObjectByID(commitID).getFileTree();
        checkoutFileFromFileTree(file, fileTree);
    }

    /**
     * Writes the file content in fileTree to the specified file.
     * @param file Relative path to WORK_DIR.
     * @param fileTree FileTree object.
     * */
    private static void checkoutFileFromFileTree(File file, FileTree fileTree) {
        if (!fileTree.contains(file)) {
            exit("File does not exist in that commit.");
        }
        String FileID = fileTree.getHashFromFile(file);
        File commitedFile = getFileInObjectsByID(FileID);
        // write the file
        writeContentsInBytes(commitedFile, getAbsoluteFileFromWorkDir(file));
    }

    /** Returns the full hash value based on the first few characters. */
    private static String getFullID(String shortID) {
        File folder = join(GITLET_OBJECTS_DIR, shortID.substring(0, 2));
        if (!folder.isDirectory()) {
            exit("No commit with that id exists.");
        }
        String target = shortID.substring(2);
        String full = "";
        int cnt = 0;
        List<String> ids = plainFilenamesIn(folder);
        assert ids != null;
        for (String id : ids) {
            if (id.startsWith(target)) {
                cnt += 1;
                full = id;
            }
        }
        if (cnt == 1) {
            return shortID.substring(0, 2) + full;
        } else if (cnt > 1) {
            exit("Find multiple commits with the given ID.");
        } else {
            exit("No commit with that id exists.");
        }

        return null;
    }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files
     * that are already there if they exist.
     * Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present
     * in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch.
     * */
    public static void checkoutBranch(String branchName) throws IOException {
        // check if branchName exists
        File newHeadFile = getBranchHeadFile(branchName);
        if (!newHeadFile.isFile()) {
            exit("No such branch exists.");
        }
        String currentBranch = getCurrentBranch();
        if (currentBranch.equals(branchName)) {
            exit("No need to checkout the current branch.");
        }
        String newHeadCommitID = readContentsAsString(newHeadFile);

        // modify files
        // reset staging area because the checked-out branch is not the current branch
        resetToCommitBasic(newHeadCommitID, true);

        // switch branch
        writeHEADFile(branchName);
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * */
    private static void resetToCommitBasic(String commitID) {
        FileTree treeCurrent = readCurrentHeadCommitObject().getFileTree();
        FileTree treeNew = readCommitObjectByID(commitID).getFileTree();

        // check if there is untracked files that will be overwritten or deleted
        for (File file : treeNew.getFileSet()) {
            if (isFileInTheWay(file, treeCurrent)) {
                exit("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }
        for (File file : treeCurrent.getFileSet()) {
            if (isFileInTheWay(file, treeCurrent)) {
                exit("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        // delete tracked files in current branch head
        for (File file : treeCurrent.getFileSet()) {
            getAbsoluteFileFromWorkDir(file).delete();
        }
        // write tracked files in new branch head
        for (File file : treeNew.getFileSet()) {
            File fileToAdd = getAbsoluteFileFromWorkDir(file);
            String FileID = treeNew.getHashFromFile(file);
            File trackedFile = getFileInObjectsByID(FileID);
            // write the file
            writeContentsInBytes(trackedFile, fileToAdd);
        }
    }

    private static void resetToCommitBasic(String commitID, boolean resetStage) {
        resetToCommitBasic(commitID);
        if (resetStage) {
            resetStage();
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch (just as in real Git).
     * Before you ever call branch, your code should be running with a default branch called "master".
     * */
    public static void addBranch(String branchName) {
        // check if branchName exists
        File newBranchHead = getBranchHeadFile(branchName);
        if (newBranchHead.isFile()) {
            exit("A branch with that name already exists.");
        }

        // the new branch shares the same hash code with the last commit
        File currentHead = getCurrentHeadFile();
        writeContents(newBranchHead, readContentsAsString(currentHead));
//        System.out.printf("Add new branch head: %s\n", readContentsAsString(newBranchHead));  // debug
    }

    /** Writes into .gitlet/HEAD the path to a branch head according to the specified branchName. */
    private static void writeHEADFile(String branchName) throws IOException {
        File branchHeadRelativePath = relativeSimplePath(GITLET_DIR,
                getBranchHeadFile(branchName));
        writeContents(GITLET_HEAD_FILE, branchHeadRelativePath.toString());
    }

    /**
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     * */
    public static void removeBranch(String branchName) {
        // check if on that branch
        if (getCurrentBranch().equals(branchName)) {
            exit("Cannot remove the current branch.");
        }
        // check if branchName exists
        File branchHead = getBranchHeadFile(branchName);
        if (!branchHead.isFile()) {
            exit("A branch with that name does not exist.");
        }

        // delete the branch head
        branchHead.delete();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * */
    public static void resetToCommit(String commitID) {
        // possibly need to get the full ID
        if (commitID.length() < 40) {
            commitID = getFullID(commitID);
        }
        resetToCommitBasic(commitID);
        resetStage();
        // update head commit ID
        writeContents(getCurrentHeadFile(), commitID);
    }

    /** Merges files from the given branch into the current branch. */
    public static void mergeBranch(String branchName) {
        String currentBranchName = getCurrentBranch();

        // failure cases
        if (!readStageObject().isEmpty()) {
            exit("You have uncommitted changes.");
        }
        if (!getBranchHeadFile(branchName).isFile()) {
            exit("A branch with that name does not exist.");
        }
        if (currentBranchName.equals(branchName)) {
            exit("Cannot merge a branch with itself.");
        }

        // split point
        String headID = getCurrentHeadCommitID();
        String otherID = getBranchHeadHashValue(branchName);
        String splitPointID = getSplitPointID(headID, otherID);
        if (splitPointID.equals(otherID)) {
            exit("Given branch is an ancestor of the current branch.");
        }
        if (splitPointID.equals(headID)) {
            // checkout, branch remains the same
            resetToCommitBasic(otherID, true);
            // HEAD points at other
            writeContents(getCurrentHeadFile(), otherID);
            exit("Current branch fast-forwarded.");
        }

        // file set
        FileTree splitPoint = readCommitObjectByID(splitPointID).getFileTree();
        FileTree head = readCommitObjectByID(headID).getFileTree();
        FileTree other = readCommitObjectByID(otherID).getFileTree();

        Set<File> files = new TreeSet<>();
        files.addAll(splitPoint.getFileSet());
        files.addAll(head.getFileSet());
        files.addAll(other.getFileSet());

        // merge condition
        Map<File, Integer> fileToMergeID = mergeCondition(files, splitPoint, head, other);

        // check if there is an untracked file in the way
        boolean fileInTheWay = isFileInTheWay(fileToMergeID);
        if (fileInTheWay) {
            exit("There is an untracked file in the way; delete it, or add and commit it first.");
        }

        // merge
        boolean existConflict = mergeFile(fileToMergeID, head, other);

        // commit
        commit(String.format("Merged %s into %s.", branchName, currentBranchName), otherID);
        if (existConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Returns {@code true} if at least one file is in the way. */
    private static boolean isFileInTheWay(Map<File, Integer> fileToMergeID) {
        FileTree fileTree = readCurrentHeadCommitObject().getFileTree();
        for (File f : fileToMergeID.keySet()) {
            int mergeID = fileToMergeID.get(f);
            switch (mergeID) {
                case 1, 5, 6, 8 -> {
                    if (isFileInTheWay(f, fileTree)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code file} exists in working directory, and
     * {@code file} is not in {@code fileTree} or
     * {@code file} is in {@code fileTree} but the hash value doesn't match.
     * */
    private static boolean isFileInTheWay(File file, FileTree fileTree) {
        File abs = getAbsoluteFileFromWorkDir(file);
        if (!abs.exists()) {
            // if file doesn't even exist, the rewrite or delete won't matter
            return false;
        }
        // file exists in the directory
        return !fileTree.contains(file) || !isFileInTree(file, fileTree);
    }

    /** Returns {@code true} if {@code file} is in {@code fileTree} and the hash value matches. */
    private static boolean isFileInTree(File file, FileTree fileTree) {
        String hashValue = sha1OfFile(getAbsoluteFileFromWorkDir(file));
        return isFileInTree(file, hashValue, fileTree);
    }

    /** Returns {@code true} if {@code file} is in {@code fileTree} and the {@code hashValue} matches. */
    private static boolean isFileInTree(File file, String hashValue, FileTree fileTree) {
        return fileTree.contains(file) && fileTree.getHashFromFile(file).equals(hashValue);
    }

    /** Returns the commit object that is the head of the specified branch. */
    private static Commit readBranchHeadCommit(String branchName) {
        String hashValue = getBranchHeadHashValue(branchName);
        return readCommitObjectByID(hashValue);
    }

    /** Returns the hash value of the commit object that is the head of the specified branch. */
    private static String getBranchHeadHashValue(String branchName) {
        return readContentsAsString(getBranchHeadFile(branchName));
    }

    /** Returns the file in .gitlet/refs/heads of the specified branchName. */
    private static File getBranchHeadFile(String branchName) {
        return join(GITLET_HEADS_DIR, branchName);
    }

    /** Returns the split point hash value of HEAD and other branch. */
    private static String getSplitPointID(String headID, String otherID) {
        Set<String> idSet = new TreeSet<>();
        String splitPointID = "";
        Commit head = readCommitObjectByID(headID);
        Commit other = readCommitObjectByID(otherID);
        while (head != null || other != null) {
            if (head != null) {
                if (!idSet.add(headID)) {
                    splitPointID = headID;
                    break;
                }
                headID = head.getParentHashValue();
                head = head.getParentCommit();
            }

            if (other != null) {
                if (!idSet.add(otherID)) {
                    splitPointID = otherID;
                    break;
                }
                otherID = other.getParentHashValue();
                other = other.getParentCommit();
            }
        }

        return splitPointID;
    }

    /** Merges files. */
    private static boolean mergeFile(Map<File, Integer> fileToMergeID, FileTree head, FileTree other) {
        boolean existConflict = false;
        for (File f : fileToMergeID.keySet()) {
            int mergeID = fileToMergeID.get(f);
            if (mergeID == 8) {
                existConflict = true;
            }
            mergeFile(f, mergeID, head, other);
        }

        return existConflict;
    }

    /** Merges a single file based on {@code mergeID} */
    private static void mergeFile(File file, int mergeID, FileTree head, FileTree other) {
        switch (mergeID) {
            case 1, 5 -> {
                // Should be checked out and staged.
                checkoutFileFromFileTree(file, other);
                addFile(file);
            }
            case 6 -> {
                // Should be removed and untracked.
                removeFile(file);
            }
            case 8 -> {
                // Replace the contents of the conflicted file and stage the result.
                // Treat a deleted file in a branch as an empty file.
                String content = getConflictConcatenation(file, head, other);
                writeContents(getAbsoluteFileFromWorkDir(file), content);
                addFile(file);
            }
        }
    }

    /** Returns the concatenated content of two conflict files */
    private static String getConflictConcatenation(File file, FileTree head, FileTree other) {
        StringBuilder sb = new StringBuilder();
        String headContent = "";
        String otherContent = "";
        if (head.contains(file)) {
            headContent = readContentsAsString(getFileInObjectsByID(head.getHashFromFile(file)));
        }
        if (other.contains(file)) {
            otherContent = readContentsAsString(getFileInObjectsByID(other.getHashFromFile(file)));
        }
        sb.append("<<<<<<< HEAD\n");
        sb.append(headContent);
        sb.append("=======\n");
        sb.append(otherContent);
        sb.append(">>>>>>>");

        return sb.toString();
    }

    /** Returns file and mergeID pair for each file. */
    private static Map<File, Integer> mergeCondition(Set<File> files, FileTree splitPoint,
                                                     FileTree head, FileTree other) {
        Map<File, Integer> fileToMergeID = new TreeMap<>();
        for (File file : files) {
            int mergeID = mergeCondition(file, splitPoint, head, other);
            fileToMergeID.put(file, mergeID);
        }
        return fileToMergeID;
    }

    /** Returns mergeID pair for each file. */
    private static int mergeCondition(File file, FileTree splitPoint, FileTree head, FileTree other) {
        // hash value of file in different commits
        // hash value is "" if the file doesn't exist in that commit.
        String empty = "";
        String hashSplitPoint = splitPoint.getHashFromFileOrDefault(file, empty);
        String hashHead = head.getHashFromFileOrDefault(file, empty);
        String hashOther = other.getHashFromFileOrDefault(file, empty);

        int cond = 0;
        // file exists in at least one commit
        if (!hashSplitPoint.equals(empty) && !hashHead.equals(empty) && !hashOther.equals(empty) &&
                hashSplitPoint.equals(hashHead) && !hashSplitPoint.equals(hashOther)) {
            // case 1:
            // Any files that have been modified in the given branch since the split point,
            // but not modified in the current branch since the split point.
            cond = 1;
        } else if (!hashSplitPoint.equals(empty) && !hashHead.equals(empty) && !hashOther.equals(empty) &&
                !hashSplitPoint.equals(hashHead) && hashSplitPoint.equals(hashOther)) {
            // case 2:
            // Any files that have been modified in the current branch
            // but not in the given branch since the split point.
            cond = 2;
        } else if (hashHead.equals(hashOther)) {
            // case 3:
            // Any files that have been modified in both the current and given branch in the same way
            // (i.e., both files now have the same content or were both removed)
            cond = 3;
        } else if (hashSplitPoint.equals(empty) && !hashHead.equals(empty) && hashOther.equals(empty)) {
            // case 4:
            // Any files that were not present at the split point and
            // are present only in the current branch
            cond = 4;
        } else if (hashSplitPoint.equals(empty) && hashHead.equals(empty) && !hashOther.equals(empty)) {
            // case 5:
            // Any files that were not present at the split point and
            // are present only in the given branch
            cond = 5;
        } else if (!hashSplitPoint.equals(empty) && !hashHead.equals(empty) && hashOther.equals(empty) &&
                hashSplitPoint.equals(hashHead)) {
            // case 6:
            // Any files present at the split point,
            // unmodified in the current branch,
            // and absent in the given branch
            cond = 6;
        } else if (!hashSplitPoint.equals(empty) && hashHead.equals(empty) && !hashOther.equals(empty) &&
                hashSplitPoint.equals(hashOther)) {
            // case 7:
            // Any files present at the split point,
            // unmodified in the current branch,
            // and absent in the current branch
            cond = 7;
        } else {
            // case 8:
            // Any files modified in different ways in the current and given branches are in conflict.
            // Can mean that the contents of both are changed and different from other,
            // or the contents of one are changed and the other file is deleted,
            // or the file was absent at the split point
            // and has different contents in the given and current branches.
            cond = 8;
        }

        return cond;
    }

    /**
     * Saves the given login information under the given remote name.
     * <ul>
     * <li>GITLET_REMOTES_DIR</li>
     *      <ul><li>name1</li>
     *          <ul><li>master</li></ul></ul>
     *      <ul><li>name2</li>
     *          <ul><li>master</li></ul></ul>
     * <ul/>
     * @param name Remote name.
     * @param path Path to .gitlet.
     * */
    public static void addRemote(String name, String path) throws IOException {
        File remoteFolder = join(GITLET_REMOTES_DIR, name);
        if (remoteFolder.exists()) {
            exit("A remote with that name already exists.");
        } else {
            remoteFolder.mkdirs();
        }
        File pathFile = join(GITLET_REMOTE_PATHS_DIR, name);
        File absPath = join(CWD, path).getCanonicalFile();
        writeContents(pathFile, absPath.toString());
    }

    /**
     * Remove information associated with the given remote name.
     * If you ever wanted to change a remote that you added, you would have to first remove it and then re-add it.
     *
     * @param name Remote name to delete.
     * */
    public static void removeRemote(String name) throws IOException {
        File remoteFolder = join(GITLET_REMOTES_DIR, name);
        File pathFile = join(GITLET_REMOTE_PATHS_DIR, name);
        if (remoteFolder.isDirectory()) {
            Utils.deleteDir(remoteFolder);
            pathFile.delete();
        } else {
            exit("A remote with that name does not exist.");
        }
    }

    /**
     * Attempts to append the current branch’s commits to the end of the given branch at the given remote.
     * This command only works if the remote branch’s head is in the history of the current local head.
     * Then, the remote should reset to the front of the appended commits.
     *
     * @param remote Remote name.
     * @param remoteBranch Remote branch name.
     * */
    public static void pushRemote(String remote, String remoteBranch) throws IOException {
        // remote branch
        File remoteGit = checkRemoteExist(remote);

        File remoteBranchFile = join(remoteGit, relativeSimplePath(GITLET_DIR, GITLET_HEADS_DIR).toString(),
                remoteBranch);
        File currentHead = getCurrentHeadFile();

        // check if ahead
        String headID = readContentsAsString(currentHead);
        String remoteHeadID = "";
        if (remoteBranchFile.exists()) {
            remoteHeadID = readContentsAsString(remoteBranchFile);
            if (!isFileInObjects(remoteHeadID) || !getSplitPointID(headID, remoteHeadID).equals(remoteHeadID)) {
                exit("Please pull down remote changes before pushing.");
            }
        }

        // copy blobs
        File remoteObjectDir = join(remoteGit, relativeSimplePath(GITLET_DIR, GITLET_OBJECTS_DIR));
        Commit commit = readCurrentHeadCommitObject();
        copyCommits(commit, headID, GITLET_OBJECTS_DIR, remoteObjectDir);

        // update remote file?

        // update hash
        writeContents(remoteBranchFile, headID);
        writeContents(join(GITLET_REMOTES_DIR, remote, remoteBranch), headID);
    }

    /**
     * Copies to specified directory the commit history including corresponding file tree and file blobs backwards
     * since a commit object .
     *
     * @param commit The commit object to start with.
     * @param hashID Hash value of the commit.
     * @param src Source objects directory.
     * @param target Target objects directory.
     * */
    private static void copyCommits(Commit commit, String hashID, File src, File target) throws IOException {
        for (Commit c : commit.traverseBack()) {
            if (!copyCommit(c, hashID, src, target)) {
                break;
            }
            hashID = c.getParentHashValue();
        }
    }

    /**
     * Copies a commit object and its corresponding file tree and file blobs to specified directory.
     * Returns {@code true} if the commit does not exist in the specified directory.
     *
     * @param c The commit object.
     * @param hashID Hash value of the commit.
     * @param src Source objects directory.
     * @param target Target objects directory.
     * @return Returns {@code true} if the commit does not exist in the specified directory.
     * */
    private static boolean copyCommit(Commit c, String hashID, File src, File target) throws IOException {
        // copy c
        if (!copyFileByHashID(hashID, src, target)) {
            return false;
        }
        // copy file tree
        copyFileByHashID(c.getTreeHashValue(), src, target);
        // copy files
        for (String hash : c.getFileTreeMap().values()) {
            copyFileByHashID(hash, src, target);
        }
        return true;
    }

    /**
     * Copies a single blob to specified directory.
     * Returns {@code true} if either {@code hashID} is empty or the file does not exist in the specified directory.
     * Returns {@code false} if the file exists in the specified directory.
     *
     * @param hashID Hash value of an object.
     * @param src Source objects directory.
     * @param target Target objects directory.
     * @return Returns {@code true} if either {@code hashID} is empty or the file does not exist in the specified directory.
     * Returns {@code false} if the file exists in the specified directory.
     * */
    private static boolean copyFileByHashID(String hashID, File src, File target) throws IOException {
        if (hashID.isEmpty()) {
            return true;
        }
        File relFile = getRelFileInObjectsByID(hashID);
        File targetFile = join(target, relFile);
        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
            Files.copy(join(src, relFile).toPath(), targetFile.toPath());
            return true;
        }
        return false;
    }

    /**
     * Exits if the remote repository does not exist.
     *
     * @param remote Remote name
     * @return Returns path to remote .gitlet.
     * */
    private static File checkRemoteExist(String remote) {
        File remotePath = join(GITLET_REMOTE_PATHS_DIR, remote);
        if (!remotePath.exists()) {
            exit("Remote directory not found.");
        }
        File remoteGit = new File(readContentsAsString(remotePath)); // .gitlet
        if (!remoteGit.exists()) {
            exit("Remote directory not found.");
        }
        return remoteGit;
    }

    /**
     * Brings down commits from the remote Gitlet repository into the local Gitlet repository.
     * This branch is created in the local repository if it did not previously exist.
     *
     * @param remote Remote name.
     * @param remoteBranch Remote branch name.
     * */
    public static void fetchRemote(String remote, String remoteBranch) throws IOException {
        // remote branch
        File remoteGit = checkRemoteExist(remote);

        File remoteBranchFile = join(remoteGit, relativeSimplePath(GITLET_DIR, GITLET_HEADS_DIR).toString(),
                remoteBranch);
        if (!remoteBranchFile.exists()) {
            exit("That remote does not have that branch.");
        }

        // copy blobs
        String headID = readContentsAsString(remoteBranchFile);
        File remoteObjectDir = join(remoteGit, relativeSimplePath(GITLET_DIR, GITLET_OBJECTS_DIR));
        File remoteCommitFile = join(remoteObjectDir, getRelFileInObjectsByID(headID));
        Commit remoteCommit = readObject(remoteCommitFile, Commit.class);
        copyCommits(remoteCommit, headID, remoteObjectDir, GITLET_OBJECTS_DIR);

        // update hash
        writeContents(join(GITLET_REMOTES_DIR, remote, remoteBranch), headID);
        writeContents(join(GITLET_HEADS_DIR, remote, remoteBranch), headID);
    }

    /**
     * Fetches remote branch and then merges that fetch into the current branch.
     *
     * @param remote Remote name.
     * @param remoteBranch Remote branch name.
     * */
    public static void pullRemote(String remote, String remoteBranch) throws IOException {
        fetchRemote(remote, remoteBranch);
        mergeBranch(remote + File.separatorChar + remoteBranch);
    }
}
