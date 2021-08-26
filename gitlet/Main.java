package gitlet;

import java.io.IOException;
import java.io.File;

import static gitlet.Utils.exit;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zichuan Wang
 */
public class Main {

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            exit("Incorrect operands.");
        }
    }

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            exit("Please enter a command.");
        }

        String firstArg = args[0];

        switch (firstArg) {
            case "init" -> {
                validateNumArgs(args, 1);
                InitRepository.initializeRepository();
            }
            case "add" -> {
                validateNumArgs(args, 2);
                Repository.addFile(args[1]);
            }
            case "commit" -> {
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
            }
            case "rm" -> {
                validateNumArgs(args, 2);
                Repository.removeFile(args[1]);
            }
            case "log" -> {
                validateNumArgs(args, 1);
                Repository.printLog();
            }
            case "global-log" -> {
                validateNumArgs(args, 1);
                Repository.printGlobalLog();
            }
            case "find" -> {
                validateNumArgs(args, 2);
                Repository.findCommit(args[1]);
            }
            case "status" -> {
                validateNumArgs(args, 1);
                Repository.printStatus();
            }
            case "checkout" -> {
                if (args.length < 2 || args.length > 4) {
                    exit("Incorrect operands.");
                }
                if (args[1].equals("--") && args.length == 3) {
                    Repository.checkoutFileToHeadCommit(args[2]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutFileToCommit(args[3], args[1]);
                } else {
                    exit("Incorrect operands.");
                }
            }
            case "branch" -> {
                validateNumArgs(args, 2);
                Repository.addBranch(args[1]);
            }
            case "rm-branch" -> {
                validateNumArgs(args, 2);
                Repository.removeBranch(args[1]);
            }
            case "reset" -> {
                validateNumArgs(args, 2);
                Repository.resetToCommit(args[1]);
            }
            case "merge" -> {
                validateNumArgs(args, 2);
                Repository.mergeBranch(args[1]);
            }
            case "add-remote" -> {
                validateNumArgs(args, 3);
                Repository.addRemote(args[1], args[2]);
            }
            case "rm-remote" -> {
                validateNumArgs(args, 2);
                Repository.removeRemote(args[1]);
            }
            case "push" -> {
                validateNumArgs(args, 3);
                Repository.pushRemote(args[1], args[2]);
            }
            case "fetch" -> {
                validateNumArgs(args, 3);
                Repository.fetchRemote(args[1], args[2]);
            }
            case "pull" -> {
                validateNumArgs(args, 3);
                Repository.pullRemote(args[1], args[2]);
            }
            default -> exit("No command with that name exists.");
        }
    }
}
