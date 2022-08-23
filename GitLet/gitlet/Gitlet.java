package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.TreeMap;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.List;


/** This class creates a new gitlet version control system
 * (.gitlet directory) when init is called.
 *  @author Hiva Mohammadzadeh
 */

public class Gitlet implements Serializable {

    /** Gitlet Constructor: creates a new gitlet version control system
     * Initializes the staging area, branches, commits, Commit IDs and
     * commit branches of a new git directory.
     */
    public Gitlet() {
        stagingArea = new Staging();
        branches = new HashMap<>();
        allBranches = new ArrayList<>();
        commits = new HashMap<>();
        commitIDs = new HashMap<>();
        branchCommits = new TreeMap<>();
    }

    public String getHeadPtr() {
        return headPointer;
    }

    public void setHeadPtr(String itsHead) {
        headPointer = itsHead;
    }

    /** INIT: Command that creates a new gitlet version control system.
     *  It also creates an initial commit.
     *  @param args String array to store the input from the user.
     */
    public void init(String[] args) {

        validateInput(args, 1);
        if (GITLET_DIRECTORY.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIRECTORY.mkdir();
        BLOBS_DIRECTORY.mkdir();
        COMMITS_DIRECTORY.mkdir();

        Commit tempCommit = new Commit();
        tempCommit.setTheMessage("initial commit");
        tempCommit.setTimeStamp("Thu Jan 01 00:00:00 1970 -0700");
        headPointer = tempCommit.saveTheCurrentCommit();
        commits.put(tempCommit, getHeadPtr());
        commitIDs.put(getHeadPtr(), tempCommit);
        idOfInitialCommit = getHeadPtr();
        allBranches.add("master");
        branches.put("master", tempCommit);
        nameOfCurrentBranch = "master";
        TreeSet<String> set = new TreeSet<>();
        set.add(getHeadPtr());
        branchCommits.put(nameOfCurrentBranch, set);
        saveTheCurrentGitlet();
    }

    /** ADD: Command that stages the file for addition and
     * adds a copy of the file from the staging area.
     * @param args String array to store the input from the user.
     */
    public void add(String[] args) {

        validateInput(args, 2);
        String nameOfFile = args[1];
        File stagingFile = Utils.join(CWD, nameOfFile);

        if (!stagingFile .exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        Commit current = commitIDs.get(getHeadPtr());
        String originalInfo = "";

        if (current.getBlobRef().containsKey(nameOfFile)) {
            originalInfo = current.getBlobRef()
                    .get(nameOfFile).getContent();
        }
        String newInfo = Utils.readContentsAsString(stagingFile);

        if (originalInfo.equals(newInfo)) {
            if (stagingArea.getStagedBlobs().containsKey(nameOfFile)) {
                stagingArea.unStageBlobs(nameOfFile);
            }
            if (stagingArea.getRemovedBlobs().containsKey(nameOfFile)) {
                stagingArea.removeBlobsReverse(nameOfFile);
            }
        } else {
            Blob addingBlob = new Blob(stagingFile);
            addingBlob.saveTheCurrentBlob();
            stagingArea.addBlobs(nameOfFile, addingBlob);
        }
        stagingArea.saveCurrentStagingArea();
        saveTheCurrentGitlet();
    }

    /** COMMIT: Command that saves a snapshot of the tracked files in the
     * current commit
     * and the staging area, so they can be restored at a later time.
     * Creates a new commit to tracks the saved files.
     * @param args String array to store the input from the user.
     */
    public void commit(String[] args) {

        validateInput(args, 2);
        if (args[1].isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        if (stagingArea.getStagedBlobs().isEmpty()
                && stagingArea.getRemovedBlobs().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit addingCommit = new Commit();
        addingCommit.setTheMessage(args[1]);
        addingCommit.setTimeStamp(new SimpleDateFormat(
                "EEE MMM d HH:mm:ss yyyy Z").format(new Date()));
        Map<String, Blob> blobsStartingFromParent = Commit
                .getCommit(getHeadPtr()).getBlobRef();

        if (blobsStartingFromParent == null) {
            addingCommit.setBlobRef(stagingArea.getStagedBlobs());
        } else {
            blobsStartingFromParent.putAll(stagingArea.getStagedBlobs());
            blobsStartingFromParent.keySet().removeAll(stagingArea
                    .getRemovedBlobs().keySet());
            Map<String, Blob> mergedBlobs = blobsStartingFromParent;
            addingCommit.setBlobRef(mergedBlobs);
        }

        addingCommit.setParentRef(getHeadPtr());
        setHeadPtr(addingCommit.saveTheCurrentCommit());
        commits.put(addingCommit, getHeadPtr());
        commitIDs.put(getHeadPtr(), addingCommit);
        if (!allBranches.contains(nameOfCurrentBranch)) {
            allBranches.add(nameOfCurrentBranch);
        }
        branches.put(nameOfCurrentBranch, addingCommit);
        TreeSet<String> newSet = branchCommits.get(nameOfCurrentBranch);
        newSet.add(getHeadPtr());
        branchCommits.put(nameOfCurrentBranch, newSet);
        stagingArea.clearStagingArea();
        stagingArea.saveCurrentStagingArea();
        saveTheCurrentGitlet();
    }

    /** CHECKOUT: Command that can do a few different things
     *  depending on what its arguments are.
     *  1. Takes the head commit version of the file and
     *      puts it in the working directory,
     *  2. Takes the given id version of the file and
     *      puts it in the working directory
     *  3. Takes the files the head of the given branch, and
     *      puts them in the working directory.
     *  @param args String array to store the input from the user.
     */
    public void checkout(String[] args) {

        Boolean case1 = args.length == 3 && args[1].equals("--");
        Boolean case2 = args.length == 4 && args[2].equals("--");
        Boolean case3 = args.length == 2;
        if ((!case1) && (!case2) && (!case3)) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (case1) {
            String nameOfFile = args[2];
            Commit checkingOutCommit = commitIDs.get(getHeadPtr());
            checkoutSpecificFile(checkingOutCommit, nameOfFile);
        }
        if (case2) {
            String commitID = convertCommitID(args[1]);
            if (commitID == null) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            String nameOfFile = args[3];
            Commit checkingOutCommit = commitIDs.get(commitID);
            checkoutSpecificFile(checkingOutCommit, nameOfFile);
        }
        if (case3) {
            String checkingOutBranch = args[1];
            if (!branches.containsKey(checkingOutBranch)) {
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            if (checkingOutBranch.equals(nameOfCurrentBranch)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            Commit currCommit = commitIDs.get(getHeadPtr());
            Commit checkingOutCommit = branches.get(checkingOutBranch);
            checkoutBranch(currCommit, checkingOutCommit);
            setHeadPtr(commits.get(checkingOutCommit));
            nameOfCurrentBranch = checkingOutBranch;
            saveTheCurrentGitlet();
        }
    }

    /** Checkout command Helper: Checks out a specific file.
     * Used in case 1 and 2 of the checkout.
     * @param checkingOutCommit The commit that we want to check out.
     * @param nameOfFile String represents the name of the file that we want
     *                 to checkout.
     */
    private void checkoutSpecificFile(Commit checkingOutCommit,
                                      String nameOfFile) {

        if (!checkingOutCommit.getBlobRef().containsKey(nameOfFile)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        Blob checkingOutBlob = checkingOutCommit.getBlobRef().get(nameOfFile);
        File replacingFile = Utils.join(CWD, nameOfFile);
        Utils.writeContents(replacingFile, checkingOutBlob.getContent());
    }

    /** Checkout command Helper: Checks out an arbitrary commit.
     * @param currCommit The current commit that the head is pointing to.
     * @param checkingOutCommit The commit that we want to check out.
     */
    public void checkoutBranch(Commit currCommit,
                                        Commit checkingOutCommit) {

        Set<String> currentCommitFiles = currCommit.getBlobRef().keySet();
        Set<String> checkOutFiles = checkingOutCommit
                .getBlobRef().keySet();

        List<String> untrackedFiles = findUntrackedFiles();
        if (untrackedFiles.size() != 0) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }


        for (String nameOfFile : checkOutFiles) {
            File fileToReplace = Utils.join(CWD, nameOfFile);
            Blob checkingOutBlob = checkingOutCommit.getBlobRef()
                    .get(nameOfFile);
            String content = checkingOutBlob.getContent();
            Utils.writeContents(fileToReplace, content);
        }
        for (String nameOfFile: currentCommitFiles) {
            if (!checkOutFiles.contains(nameOfFile)) {
                Utils.restrictedDelete(Utils.join(CWD, nameOfFile));
            }
        }
        stagingArea.clearStagingArea();
        stagingArea.saveCurrentStagingArea();
    }

    /** LOG: Command that Displays the information about each commit starting
     * from the current head commit backwards along the commit tree
     * until the initial commit.
     * @param args String array to store the input from the user.
     */
    public void log(String[] args) {

        validateInput(args, 1);
        String pointerToHead = getHeadPtr();
        String copyHead = pointerToHead;
        while (copyHead != null) {
            Commit currentCommit = Commit.getCommit(copyHead);
            System.out.println("===");
            System.out.println("commit " + copyHead);
            System.out.println("Date: " + currentCommit.getTimeStamp());
            System.out.println(currentCommit.getTheMessage() + "\n");
            copyHead = currentCommit.getParentRef();
        }
    }

    /** GLOBAL LOG: Command that is like log, except it displays information
     * about all commits ever made and Order doesn't matter.
     * @param args String array to store the input from the user.
     */
    public void globalLog(String[] args) {

        validateInput(args, 1);
        for (File currFile : COMMITS_DIRECTORY.listFiles()) {
            String pointerToFileName = currFile.getName();
            if (currFile.isFile()) {
                Commit currentCommit = Commit.getCommit(pointerToFileName);
                System.out.println("===");
                System.out.println("commit " + pointerToFileName);
                System.out.println("Date: " + currentCommit.getTimeStamp());
                System.out.println(currentCommit.getTheMessage() + "\n");
            }
        }
    }

    /** RM: Command that unstages the file if the file is tracked
     * in the current commit.
     * @param args String array to store the input from the user.
     */
    public void rm(String[] args) {
        validateInput(args, 2);
        String nameOfFile = args[1];
        Commit currentCommit = commitIDs.get(getHeadPtr());
        boolean checkStage = stagingArea.getStagedBlobs()
                .containsKey(nameOfFile);
        boolean checkCommit = currentCommit.getBlobRef()
                .containsKey(nameOfFile);
        if (!checkCommit && !checkStage) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else {
            if (checkStage) {
                stagingArea.unStageBlobs(nameOfFile);
            }
            if (checkCommit) {
                Blob removingBlob = currentCommit.getBlobRef().get(nameOfFile);
                stagingArea.removeBlobs(nameOfFile, removingBlob);
                File removingFile = Utils.join(CWD, nameOfFile);
                Utils.restrictedDelete(removingFile);
            }
        }
        stagingArea.saveCurrentStagingArea();
        saveTheCurrentGitlet();
    }

    /** FIND: Command that prints out the ids of all commits that
     * have been given commit messages, one per line and if there
     * are multiple such commits, it prints the ids out on separate lines.
     * @param args String array to store the input from the user.
     */
    public void find(String[] args) {
        validateInput(args, 2);
        int count = 0;
        for (File file : COMMITS_DIRECTORY.listFiles()) {
            String pointerToFileName = file.getName();
            Commit currCommit = commitIDs.get(pointerToFileName);
            if (file.isFile() && currCommit.getTheMessage().equals(args[1])) {
                count++;
                System.out.println(file.getName());
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** STATUS: Command that displays what branches currently exist,
     * marks the current branch with a *, and displays what files have
     * been staged for addition or removal.
     * Extra Credit?
     * @param args String array to store the input from the user.
     */
    public void status(String[] args) {
        validateInput(args, 1);
        System.out.println("=== Branches ===");
        for (String currentBranch : allBranches) {
            if (currentBranch.equals(nameOfCurrentBranch)) {
                System.out.println("*" + currentBranch);
            } else {
                System.out.println(currentBranch);
            }
        }
        System.out.println("\n=== Staged Files ===");
        Set<String> stagedFiles = stagingArea.getStagedBlobs().keySet();
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println("\n=== Removed Files ===");
        Set<String> removedFiles = stagingArea.getRemovedBlobs().keySet();
        for (String removedFile : removedFiles) {
            System.out.println(removedFile);
        }

        System.out.println("\n=== Modifications Not Staged For"
                + " Commit ===\n\n=== Untracked Files ===\n");

    }


    /** BRANCH: Command that creates a new branch with the given name,
     * and points it at the current head node.
     * A branch is a name for a reference (a SHA-1 identifier) to a
     * commit node.
     * Before calling branch, the code will run with a default branch
     * called "master".
     * It will give us a new pointer.
     * @param args String array to store the input from the user.
     */
    public void branch(String[] args) {
        validateInput(args, 2);
        String branch = args[1];
        if (branches.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
        }
        Commit currCommit = branches.get(nameOfCurrentBranch);
        branches.put(branch, currCommit);
        TreeSet<String> set = new TreeSet<>();
        set.add(getHeadPtr());
        branchCommits.put(branch, set);
        saveTheCurrentGitlet();
    }

    /** RM BRANCH: Command that deletes the branch with the given name.
     * It also deletes the pointer associated with that branch.
     * It does not delete all commits that were created under the branch.
     * @param args String array to store the input from the user.
     */
    public void rmBranch(String[] args) {
        validateInput(args, 2);
        String branchRemoved = args[1];
        if (!branches.containsKey(branchRemoved)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (nameOfCurrentBranch.equals(branchRemoved)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        branches.remove(branchRemoved);
        saveTheCurrentGitlet();
    }

    /** RESET: Command that checks out all the files tracked by the
     * given commit.
     * It removes tracked files that are not present in that commit.
     * It moves the current branch's head to that commit node.
     * The staging area is cleared.
     * This command is essentially checkout of an arbitrary commit that
     * also changes the current branch head.
     * @param args String array to store the input from the user.
     */
    public void reset(String[] args) {

        validateInput(args, 2);
        String id = convertCommitID(args[1]);
        if (id == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit currCommit = branches.get(nameOfCurrentBranch);
        Commit givenBranchCommit = commitIDs.get(args[1]);

        if (givenBranchCommit == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Set<String> filesInCurrentCommit = currCommit.getBlobRef().keySet();
        Set<String> filesInGivenCommit = givenBranchCommit
                .getBlobRef().keySet();

        for (String fileName : filesInGivenCommit) {
            if (!filesInCurrentCommit.contains(fileName)
                    && Utils.join(CWD, fileName).exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        Commit resettingCommit = commitIDs.get(id);
        for (String f : Utils.plainFilenamesIn(CWD)) {
            if (!resettingCommit.getBlobRef().containsKey(f)) {
                Utils.restrictedDelete(f);
            }
        }

        for (String blobID : resettingCommit.getBlobRef().keySet()) {
            checkoutSpecificFile(resettingCommit, blobID);
        }

        branches.put(nameOfCurrentBranch, resettingCommit);
        setHeadPtr(id);
        stagingArea.clearStagingArea();
        saveTheCurrentGitlet();
    }

    /** MERGE: Command that merges files from the given branch into
     * the current branch.
     * The set of commits has progressed from a simple sequence to a tree
     * and now, finally, to a full directed acyclic graph.
     * @param args String array to store the input from the user.
     */
    public void merge(String[] args) {
        validateInput(args, 2);
        String givenBranchName = args[1];
        if (!stagingArea.getRemovedBlobs().isEmpty()
                || !stagingArea.getStagedBlobs().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(givenBranchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (nameOfCurrentBranch.equals(givenBranchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        List<String> untrackedFiles = findUntrackedFiles();
        if (untrackedFiles.size() != 0) {
            System.out.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
            System.exit(0);
        }
    }

    /** Finds the commit id that matches the abbreviated id provided by user.
     * Used in checkout and reset.
     * @param abbrevID string representing the abbreviated id.
     * @return the commit id.
     */
    public String convertCommitID(String abbrevID) {
        for (String id : commits.values()) {
            if (id.startsWith(abbrevID)) {
                return id;
            }
        }
        return null;
    }

    /** Function that keeps track of all the untracked files.
     * @return fileNames a list of strings that include all
     * the untracked files.
     */
    List<String> findUntrackedFiles() {
        Commit commit = commitIDs.get(getHeadPtr());
        List<String> fileNames = new ArrayList<>();
        Map<String, Blob> blobs = commit.getBlobRef();
        Set<String> stagedFiles = stagingArea.getStagedBlobs().keySet();
        for (String fileName : Utils.plainFilenamesIn(CWD)) {
            if (!blobs.containsKey(fileName)
                    && !stagedFiles.contains(fileName)) {
                fileNames.add(fileName);
            }
        }
        return fileNames;
    }

    /** Function that creates a new .gitlet file, and
     * writes new gitlet object to .gitlet directory.
     * It saves the current state of the git after commands.
     */
    public void saveTheCurrentGitlet() {
        File newGitletFile = Utils.join(".gitlet", "gitlet");
        Utils.writeObject(newGitletFile, this);
    }

    /** Function that is used to get the current Gitlet object.
     * Used in main to access the current gitlet object.
     * @return The gitlet object that was read.
     */
    public static Gitlet getGitlet() {
        File gitlet = Utils.join(".gitlet", "gitlet");
        return Utils.readObject(gitlet, Gitlet.class);
    }

    /** Function that checks the number of arguments with the expected
     * number of arguments, and exits if they do not match.
     * @param args String array to store the input from the user.
     * @param expectedArgs integer representing the number of expected arguments
     */
    public static void validateInput(String[] args, int expectedArgs) {
        if (args.length != expectedArgs) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }


    /** Staging object representing the staging area.*/
    private Staging stagingArea;
    /** String representing the pointer to the head commit.*/
    private String headPointer;
    /** String representing SHA-1 reference to initial commit.*/
    private String idOfInitialCommit;
    /** String to track the current baranch.*/
    private String nameOfCurrentBranch;

    /** File directory which is the current directory.*/
    static final File CWD = new File(".");
    /** File directory where gitlet repository is.*/
    static final File GITLET_DIRECTORY = Utils.join(CWD, ".gitlet");
    /** File directory where the blobs are.*/
    static final File BLOBS_DIRECTORY = Utils.join(GITLET_DIRECTORY, "Blob");
    /** File directory where the commits are.*/
    static final File COMMITS_DIRECTORY
            = Utils.join(GITLET_DIRECTORY, "Commit");

    /** Map representing the branches.*/
    private Map<String, Commit> branches;
    /** ArrayList representing the branches.*/
    private ArrayList<String> allBranches;
    /** Map representing the branch of all the commits.*/
    private Map<String, TreeSet<String>> branchCommits;
    /** Map representing the commits.*/
    private Map<Commit, String> commits;
    /** Map representing the commit IDs.*/
    private Map<String, Commit> commitIDs;
}
