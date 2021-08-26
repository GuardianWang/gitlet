package gitlet;

import java.util.*;

/**
 * Iterates all commits.
 * Instead of save all commits in a set, save commit IDs in a set to save memory.
 * Each time, read only one commit and find the next commit that has not been traversed yet.
 * */
public class CommitGlobalTraversal implements Iterable<Commit> {

    private final Set<String> headIDs;

    public CommitGlobalTraversal(Set<String> h) {
        headIDs = h;
    }

    @Override
    public Iterator<Commit> iterator() {
        return new CommitGlobalTraversalIterator(headIDs);
    }

    private static class CommitGlobalTraversalIterator implements Iterator<Commit> {

        private final Deque<String> heads;
        private Commit c;
        private final Set<String> commitIDs;

        public CommitGlobalTraversalIterator(Set<String> h) {
            commitIDs = new TreeSet<>();
            heads = new LinkedList<>();
            heads.addAll(h);
            c = null;

            // initialize c
            if (!heads.isEmpty()) {
                startFromNewBranch();
            }
        }

        /** Assigns {@code c} with the Commit object of new branch head. */
        private boolean startFromNewBranch() {
            assert !heads.isEmpty();
            String cID = heads.pop();
            c = Repository.readCommitObjectByID(cID);

            return commitIDs.add(cID);
        }

        /** Assigns {@code c} with the Commit object that has not been traversed. */
        private void updateCommit() {
            assert c != null;
            boolean findNext = false;
            while (!findNext) {
                // iterate the current branch
                for (Commit p : c.traverseBack()) {
                    if (commitIDs.add(p.getParentHashValue())) {
                        // parent of c is not traversed yet
                        c = p.getParentCommit();  // may be null
                        if (c != null) {
                            findNext = true;
                            break;
                        }
                    }
                }

                if (findNext) {
                    break;
                }

                // switch a branch
                if (heads.isEmpty()) {
                    // no more branches
                    break;
                } else if (startFromNewBranch()) {
                    // the new branch head is not traversed yet, c is new head
                    findNext = true;
                }  // else, the new branch head is traversed
            }

            if (!findNext) {
                c = null;
            }
        }

        @Override
        public boolean hasNext() {
            return c != null;
        }

        @Override
        public Commit next() {
            Commit p = c;
            updateCommit();

            return p;
        }
    }
}
