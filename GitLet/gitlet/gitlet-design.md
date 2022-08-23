# Gitlet Design Document
author: Hiva Mohammadzadeh

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures
##Classes
### Main.java
1. This class is the entry point of the program. 
2. It implements methods to set up persistance and support each command of the program.
3. Handles Input. 
4. Used to run all gitlet commands, such as init, add, and so on.

### Gitlet.java
1. creates .gitlet folder when call init.
2. Manages all the commit history.
3. Has a staging object 
4. Has a commit object
5. Has a blob object
6. Head pointer
#### Fields
1. Map<String, Commit> commitMap; // all existing commits (values) with SHA-1s (keys)
2. String head; // HEAD pointer 
3. String initialId; // SHA-1 reference to initial commit 
4. File CWD = new File("."); // current directory
5. File GITLET_FOLDER = Utils.join(CWD, ".gitlet"); // directory where gitlet resides

### Staging.java
1. staged for addition
2. Staged for removal
#### Fields
1. List<Blob> stagedBlobs; // files/blobs added to staging area

### Commit.java
1. This class is the class that handles the commits of the program. It implements methods to set up commits. 
2. Has access to blobs
3. Assigns pointers to blobs 
4. Sets and contains information of each commit object.
#### Fields
1. private String message: A string that holds the string log message that we're trying to save the commit as. 
2. private String timestamp: date and time the commit was made. 
3. private String SHA-1 id- call the function in utils 
4. String parentRef; // SHA-1 reference to parent commit
5. List<String> blobRef; // file (blob) reference of the files in the commit
### Blob.java
1. Stores all the file versions in gitlet.
#### Fields
1. String id; // SHA-1 reference to file 
2. File file; // file


Include here any class definitions. For each class list the instance
variables and static variables (if any). Include a ***brief description***
of each variable and its purpose in the class. Your explanations in
this section should be as concise as possible. Leave the full
explanation to the following sections. You may cut this section short
if you find your document is too wordy.

##Commands 
### init 
1. Creates a new gitlet version control system 
2. Creates an initial commit
### add
1. Stages the file for addition 
2. Adds a copy of the file from the staging area 
### commit 
1. Saves a snapshot of the tracked files in the current commit and the staging area so they can be restored at a later time, creating a new commit. 
2. Tracks the saved files.
### rm 
1. Unstages the file if the file is tracked in the current commit.
### log
1. Displays the information about each commit starting from the current head commit backwards along the commit tree until the initial commit.
2. Starts the first parent commit links and ignores any second parents found in merge commits. These commits are stored in the commit's history.
3. For each commit, it will display its commit id, the timestamp in Pacific Standard Time of the commit and the commit message.
4. There's a === before each commit and an empty line after it.
### global-log
1. Like log, except it displays information about all commits ever made. 
2. Order doesn't matter.
### find
1. Prints out the ids of all commits that have been given commit messages, one per line, 
2. If multiple such commits, it prints the ids out on separate lines. 
3. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command above.
4. hashMap - mapping commit messages to commits 
### status
1. Displays what branches currently exist
2. Marks the current branch with a *. 
3. Displays what files have been staged for addition or removal.
4. EXTRA CREDIT:
   1. A file in the working directory is "modified but not staged" if it is: 
      1. Tracked in the current commit, changed in the working directory, but not staged; 
      2. Staged for addition, but with different contents than in the working directory;
      3. Staged for addition, but deleted in the working directory; 
      4. Not staged for removal, but tracked in the current commit and deleted from the working directory.
   2. ("Untracked Files") is for files present in the working directory but neither staged for addition nor tracked. 
      1. This includes files that have been staged for removal, but then re-created without Gitlet's knowledge.
      
### checkout 
1. Checkout is a general command that can do a few different things depending on what its arguments are. 
2. There are 3 possible use cases:
   1. Takes the version of the file as it exists in the head commit, and puts it in the working directory, 
      1. overwrites the version of the file that's already there if there is one. 
      2. The new version of the file is not staged.
   2. Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory
      1. overwrites the version of the file that's already there if there is one. 
      2. The new version of the file is not staged.
   3. Takes all files in the commit at the head of the given branch, and puts them in the working directory
      1. overwrites the versions of the files that are already there if they exist. 
      2. at the end of this command, the given branch will now be considered the current branch (HEAD). 
      3. Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. 
      4. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
### branch 
1. Creates a new branch with the given name, and points it at the current head node. 
2. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. 
3. This command does NOT immediately switch to the newly created branch (just as in real Git). 
4. Before you ever call branch, your code should be running with a default branch called "master".
5. All that creating a branch does is to give us a new pointer
### rm-branch
1. Deletes the branch with the given name. 
2. Deletes the pointer associated with the branch; 
3. It does not delete all commits that were created under the branch.
### reset
1. Checks out all the files tracked by the given commit. 
2. Removes tracked files that are not present in that commit. 
3. Moves the current branch's head to that commit node. See the intro for an example of what happens to the head pointer after using reset. 
4. The [commit id] may be abbreviated as for checkout. 
5. The staging area is cleared. 
6. The command is essentially checkout of an arbitrary commit that also changes the current branch head.
### merge
1. Merges files from the given branch into the current branch. 
2. By the way, we hope you've noticed that the set of commits has progressed from a simple sequence to a tree and now, finally, to a full directed acyclic graph.


## 2. Algorithms

This is where you tell us how your code works. For each class, include
a high-level description of the methods in that class. That is, do not
include a line-by-line breakdown of your code, but something you would
write in a javadoc comment above a method, ***including any edge cases
you are accounting for***. We have read the project spec too, so make
sure you do not repeat or rephrase what is stated there.  This should
be a description of how your code accomplishes what is stated in the
spec.


The length of this section depends on the complexity of the task and
the complexity of your design. However, simple explanations are
preferred. Here are some formatting tips:

* For complex tasks, like determining merge conflicts, we recommend
  that you split the task into parts. Describe your algorithm for each
  part in a separate section. Start with the simplest component and
  build up your design, one piece at a time. For example, your
  algorithms section for Merge Conflicts could have sections for:

   * Checking if a merge is necessary.
   * Determining which files (if any) have a conflict.
   * Representing the conflict in the file.
  
* Try to clearly mark titles or names of classes with white space or
  some other symbols.
## Main
  public static void main(String... args); 

## Static Gitlet Class 
public Gitlet(); // no args constructor to initialize Gitlet object
public void init(); // creates a new Gitlet version-control system in the current directory
public void add(String fileName); // adds a file to staging area
public void commit(String msg); // creates a new commit
public void checkout(); // checks out a file or branch
public void log(); // displays information about each commit backwards until the initial commit
public void global-log(); // displays information about all commits in history
public void rm(); // removes the file from staging area
public void find();
public void status();
public void branch();
public void rm-branch();
public void reset();
public void merge();
public void saveGitlet(); // saves the Gitlet instance object
public static Gitlet fromFile(); // reads the Gitlet object from file

## Stage
public Stage(); // no args constructor to initialize empty blob list
public void add(Blob blob); // adds a blob to the blob list
public void remove(Blob blob); // removes a blob from the blob list

## Commit
public String getId(); // gets SHA-1 reference to the commit instance
public void setId(String id); // sets SHA-1 reference to the commit instance
public String getMessage(); // gets commit message
public void setMessage(String message); // sets commit message
public String getParentRef(); // gets SHA-1 reference to parent commit
public void setParentRef(String refToParent); // sets SHA-1 reference to parent commit
public String getBlobRef(); // gets SHA-1 references to blobs in the commit
public void setBlobRef(String refToBlob); // sets SHA-1 references to blobs in the commit
public Timestamp getTimestamp(); // gets time of the commit
public void setTimestamp(Timestamp commitTime); // sets time of the commit
public void saveCommit(); // gives the commit a SHA-1 and saves it to file whose name is the SHA-1
public static Commit fromFile(String name); // reads a commit file with its name

## Blob
public Blob(File file); // constructor to initialize blob object with a file
public String getId(); // gets SHA-1 reference to the blob
public void setId(String id); // sets SHA-1 reference to the blob
public File getFile();
public void setFile(File file);
public void saveBlob(); // gives the blob a SHA-1 and saves it to file whose name is the SHA-1
public static Blob fromFile(String name); // reads a blob file with its name


## 3. Persistence

Describe your strategy for ensuring that you don’t lose the state of your program
across multiple runs. Here are some tips for writing this section:

* This section should be structured as a list of all the times you
  will need to record the state of the program or files. For each
  case, you must prove that your design ensures correct behavior. For
  example, explain how you intend to make sure that after we call
       `java gitlet.Main add wug.txt`,
  on the next execution of
       `java gitlet.Main commit -m “modify wug.txt”`, 
  the correct commit will be made.
  
* A good strategy for reasoning about persistence is to identify which
  pieces of data are needed across multiple calls to Gitlet. Then,
  prove that the data remains consistent for all future calls.
  
* This section should also include a description of your .gitlet
  directory and any files or subdirectories you intend on including
  there.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

