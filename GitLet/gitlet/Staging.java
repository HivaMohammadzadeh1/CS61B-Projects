package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.HashMap;

/** This class represents the staging area that keeps track of staged
 * for addition and Staged for removal files.
 *  @author Hiva Mohammadzadeh
 */

public class Staging implements Serializable {

    /** Staging Constructor: creates a new commit with a new hash map
     * for the blobs of this commit.
     * */
    public Staging() {
        removedBlobs = new HashMap<>();
        stagedBlobs = new HashMap<>();
    }

    public void addBlobs(String blobName, Blob addedBlob) {
        stagedBlobs.put(blobName, addedBlob);
    }

    public void removeBlobs(String blobName, Blob removedBlob) {
        removedBlobs.put(blobName, removedBlob);
    }

    public void unStageBlobs(String blobName) {
        stagedBlobs.remove(blobName);
    }

    public void clearStagingArea() {
        stagedBlobs.clear();
        removedBlobs.clear();
    }

    public void removeBlobsReverse(String fileName) {
        removedBlobs.remove(fileName);
    }

    public Map<String, Blob> getRemovedBlobs() {
        return removedBlobs;
    }

    public Map<String, Blob> getStagedBlobs() {
        return stagedBlobs;
    }

    /** Function that creates a staging area inside the gitlet repository.
     * It saves the current state of the staging area after changes.
     * */
    public void saveCurrentStagingArea() {
        File currentStageFile = Utils.join(".gitlet", "stage");
        Utils.writeObject(currentStageFile, this);
    }

    /** Map representing all the staged blobs.*/
    private Map<String, Blob> stagedBlobs;
    /** Map representing all the removed blobs.*/
    private Map<String, Blob> removedBlobs;
}
