package gitlet;

import java.io.Serializable;
import java.io.File;

/** This class stores all the file versions in gitlet in a Blob directory.
 *  @author Hiva Mohammadzadeh
 */

public class Blob implements Serializable {

    /** Blob Constructor.
     * @param givenFile The file that we're creating a blob from.
     */
    public Blob(File givenFile) {
        file = givenFile;
        info = "";
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String userFileName) {
        fileName = userFileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File givenFile) {
        file = givenFile;
    }

    public String getContent() {
        return info;

    }
    public void setContent(String givenContent) {
        info = givenContent;
    }

    /** Function that reads the file and creates a blob from it:
     * It reads the content of the file and stores it into info,
     * gets the name and sets it, creates a blob object out of
     * the content that it read and writes the new blob object
     * to the Blob directory.
     * @return blobID The id of the created blob object
     */
    public String saveTheCurrentBlob() {

        info = Utils.readContentsAsString(file);
        setFileName(file.getName());
        String blobID = Utils.sha1(Utils.serialize(this));
        File newBlob = Utils.join(Gitlet.BLOBS_DIRECTORY, blobID);
        Utils.writeObject(newBlob, this);
        return blobID;
    }

    /** The file that represents the blob.*/
    private File file;
    /** Name of the blob file.*/
    private String fileName;
    /** Content of the blob file.*/
    private String info;
}
