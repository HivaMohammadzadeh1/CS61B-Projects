package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Hiva Mohammadzadeh
 */

public class Main {
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     * */
    public static void main(String... args) {
        checkArgs(args);
        if (args[0].equals("init") && args.length == 1) {
            Gitlet gitlet = new Gitlet();
            gitlet.init(args);
        } else if (args[0].equals("add")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.add(args);
        } else if (args[0].equals("commit")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.commit(args);
        } else if (args[0].equals("log")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.log(args);
        } else if (args[0].equals("checkout")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.checkout(args);
        } else if (args[0].equals("global-log")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.globalLog(args);
        } else if (args[0].equals("rm")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.rm(args);
        } else if (args[0].equals("find")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.find(args);
        } else if (args[0].equals("status")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.status(args);
        } else if (args[0].equals("branch")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.branch(args);
        } else if (args[0].equals("rm-branch")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.rmBranch(args);
        } else if (args[0].equals("reset")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.reset(args);
        } else if (args[0].equals("merge")) {
            checkGitRepository();
            Gitlet gitlet = Gitlet.getGitlet();
            gitlet.merge(args);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /** Function that checks if the user entered a command or not.
     * @param args the string array holding the user's input.
     */
    public static void checkArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
    }

    /** Function that checks if there is already a gitlet
     * repository created or not.
     * */
    public static void checkGitRepository() {
        if (!Utils.join(Gitlet.CWD, ".gitlet").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
