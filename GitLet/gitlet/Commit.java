package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/** This class handles the commits of the program,
 * and implements methods to set up commits.
 *  @author Hiva Mohammadzadeh
 */

public class Commit implements Serializable {

    /** Commit Constructor: creates a new commit with a new hash map
     * for the blobs of this commit.
     */
    public Commit() {
        blobMapRef = new HashMap<>();
    }

    public Map<String, Blob> getBlobRef() {
        return blobMapRef;
    }

    public void setBlobRef(Map<String, Blob> itsBlobMapRef) {
        blobMapRef = itsBlobMapRef;
    }

    public String getTheMessage() {
        return message;
    }

    public void setTheMessage(String itsMessage) {
        message = itsMessage;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String itsTimeStamp) {
        timeStamp = itsTimeStamp;
    }

    public String getParentRef() {
        return parentRef;
    }

    public void setParentRef(String itsParentRef) {
        parentRef = itsParentRef;
    }

    /** Function that is used to get the commit object with the given commitID.
     * Used in log and commit.
     * @param commitID String representing the Id of the commit.
     * @return The object that was read.
     */
    public static Commit getCommit(String commitID) {
        File file = Utils.join(Gitlet.COMMITS_DIRECTORY, commitID);
        return Utils.readObject(file, Commit.class);
    }

    /** Function that creates a commit Id for the new commit,
     * and writes new commit object to Commit directory.
     * @return commitID The id of the created blob object.
     */
    public String saveTheCurrentCommit() {
        String commitID = Utils.sha1(Utils.serialize(this));
        File newCommit = Utils.join(Gitlet.COMMITS_DIRECTORY, commitID);
        Utils.writeObject(newCommit, this);
        return commitID;
    }

    /** FIELDS:
     * String to track the Commit time.*/
    private String timeStamp;
    /** String to track the commit message.*/
    private String message;
    /** String holding the SHA-1 reference to parent commit.*/
    private String parentRef;
    /** Map holding the file (blob) reference of the files in the
     * commit where key: user file name, value: blob object.*/
    private Map<String, Blob> blobMapRef;


}
