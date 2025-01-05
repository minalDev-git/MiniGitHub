package com.minigithub.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Date;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;

public class GridFSUtility {

    private static final String DATABASE_NAME = "test"; // Default database name
    private static GridFSBucket getGridFSBucket(){
        MongoDBConnection con = Model.getInstance().getConnection();
        MongoDatabase database = con.getDatabase(DATABASE_NAME);
        if (database == null) {
            throw new RuntimeException("Failed to get database connection");
        }
        GridFSBucket gridFSBucket = GridFSBuckets.create(database);
        return gridFSBucket;
    }

    public static ObjectId uploadFile(String filePath,String fileName, String ext){
        try (FileInputStream fileStream = new FileInputStream(filePath)) {
            GridFSBucket gridFSBucket = getGridFSBucket();
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("type", ext));

            // Upload to GridFS and get the file ID
            return gridFSBucket.uploadFromStream(fileName, fileStream, options);
        } catch (Exception e) {
            System.out.println("File Not Added");
            return null;
        }
    }
    public static GridFSFile getFile(ObjectId fileId){
        try {
            GridFSBucket gridFSBucket = getGridFSBucket();
            Bson filter = Filters.eq("_id", fileId);
            GridFSFile file = gridFSBucket.find(filter).first();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static GridFSFile getFileByName(String filename){
        try {
            GridFSBucket gridFSBucket = getGridFSBucket();
            Bson filter = Filters.eq("filename", filename);
            GridFSFile file = gridFSBucket.find(filter).first();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Downloading the file to locally save it on the disk
    public static void downloadFile(ObjectId fileId, String destinationPath){
        GridFSBucket gridFSBucket = getGridFSBucket();
        Bson filter = Filters.eq("_id", fileId);
        GridFSFile file = gridFSBucket.find(filter).first();
        GridFSDownloadOptions downloadOptions = new GridFSDownloadOptions().revision(0);
        try (FileOutputStream streamToDownloadTo = new FileOutputStream(destinationPath +"/"+file.getFilename())) {
            gridFSBucket.downloadToStream(file.getFilename(), streamToDownloadTo,downloadOptions);
            streamToDownloadTo.flush();
        } catch (Exception e) {
            System.out.println("File not downloaded");
            e.printStackTrace();
        }
    }
    public static byte[] getFileContents(ObjectId fileId){
        try {
            GridFSBucket gridFSBucket = getGridFSBucket();
            try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(fileId)) {
            int fileLength = (int) downloadStream.getGridFSFile().getLength();
            byte[] fileDatabytes = new byte[fileLength];
            downloadStream.read(fileDatabytes);
            return fileDatabytes;
        }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static ObjectId updateFile(ObjectId fileId,String filename,String ext, InputStream inputStream){
        GridFSBucket gridFSBucket = getGridFSBucket();
        try {
            //delete the old file
            Bson filter = Filters.eq("_id", fileId);
            GridFSFile file = gridFSBucket.find(filter).first();
            deleteFile(file.getObjectId());
            ObjectId newFileId = gridFSBucket.uploadFromStream(filename, inputStream);
            System.out.println("Deleted old file with ID: " + fileId);
            return newFileId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static FileModel createREADME(ObjectId userId, String repoName) {
    FileModel file = new FileModel();
    File readmeFile = null;
    GridFSBucket gridFSBucket = getGridFSBucket();
    try {
        String filePath = "src/main/java/com/minigithub/READMEFiles/README.txt";
        String fileContent = "Welcome to " + repoName + "!\n\nThis is the README file for the repository.";

        //write content to the README file
        readmeFile = new File(filePath);
        try (FileWriter writer = new FileWriter(readmeFile)) {
            writer.write(fileContent);
        }
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            GridFSUploadOptions options = new GridFSUploadOptions()
                    .metadata(new org.bson.Document("type", "txt"));
            ObjectId fileId = gridFSBucket.uploadFromStream(readmeFile.getName(), inputStream, options);

            file.setFilename(readmeFile.getName());
            file.setType("txt");
            file.setDate(new Date());
            file.setRepoName(repoName);
            file.setId(fileId);
        }

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    } finally {
        //Clean up the local file
        if (readmeFile != null && readmeFile.exists()) {
            boolean deleted = readmeFile.delete();
            if (!deleted) {
                System.err.println("Failed to delete temporary README file");
            }
        }
    }
    return file;
}
    public static void deleteFile(ObjectId fileId){
        GridFSBucket gridFSBucket = getGridFSBucket();
        Bson filter = Filters.eq("_id", fileId);
        GridFSFile file = gridFSBucket.find(filter).first();
        if (file != null) {
            gridFSBucket.delete(fileId);
        }
    }
}
