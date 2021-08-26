package gitlet;

import java.util.Iterator;

public class CommitTraversal implements Iterable<Commit> {

    private final Commit leafCommit;

    public CommitTraversal(Commit c) {
        leafCommit = c;
    }

    @Override
    public Iterator<Commit> iterator() {
        return new CommitTraversalIterator(leafCommit);
    }

    private static class CommitTraversalIterator implements Iterator<Commit> {

        private Commit commit;

        public CommitTraversalIterator(Commit c) {
            commit = c;
        }

        @Override
        public boolean hasNext() {
            return commit != null;
        }

        @Override
        public Commit next() {
            Commit currentCommit = commit;
            commit = commit.getParentCommit();

            return currentCommit;
        }
    }
}
