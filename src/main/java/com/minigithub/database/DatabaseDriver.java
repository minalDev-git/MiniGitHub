package com.minigithub.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.bson.BsonValue;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.minigithub.model.AdminModel;
import com.minigithub.model.FileModel;
import com.minigithub.model.Model;
import com.minigithub.model.RepositoryModel;
import com.minigithub.model.UserModel;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;

public class DatabaseDriver {

    private static final String DATABASE_NAME = "Users";

    private static MongoCollection<UserModel> getMongoCollection() {
        MongoDBConnection con = Model.getInstance().getConnection();
        MongoDatabase database = con.getDatabase(DATABASE_NAME);
        if (database == null) {
            throw new RuntimeException("Failed to get database connection");
        }
        MongoCollection<UserModel> collection = database.getCollection("users", UserModel.class);
        return collection;
    }

    /*
     * User
     */
    public static void updateUserData(ObjectId userId, String username, String password, String description,
            String jobtitle) {
        MongoCollection<UserModel> collection = getMongoCollection();
        try {
            Bson filter = Filters.eq("_id", userId);
            Bson update = null;
            if (collection.find(filter).first() != null) {
                update = Updates.combine(Updates.set("username", username), Updates.set("password", password),
                        Updates.set("description", description), Updates.set("jobTitle", jobtitle));
                collection.updateOne(filter, update);
                System.out.println("User Updated successfully");
            }
        } catch (MongoException e) {
            System.out.println("User does not Exists!");
        }
    }

    public static void UpdateStreaks(ObjectId userId, Integer streaks) {
        MongoCollection<UserModel> collection = getMongoCollection();
        try {
            Bson filter = Filters.eq("_id", userId);
            if (collection.find(filter).first() != null) {
                Bson update = Updates.set("streaks", streaks);
                collection.updateOne(filter, update);
                System.out.println("Streaks updated");
            }
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static void incrementCommits(ObjectId userId, Integer number) {
        MongoCollection<UserModel> collection = getMongoCollection();
        try {
            Bson filter = Filters.eq("_id", userId);
            if (collection.find(filter).first() != null) {
                Bson update = Updates.inc("commits", number);
                collection.updateOne(filter, update);
                System.out.println("Commits Incremented");
            }
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static void updateLastCommitYear(ObjectId userId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        try {
            Bson filter = Filters.eq("_id", userId);
            UserModel user = collection.find(filter).first();
            if (user != null) {
                String dateString = new Date().toString(); 
                // Split the string by spaces
                String[] stringParts = dateString.split(" "); 
                // The year is the last part
                String year = stringParts[stringParts.length - 1];
                if (user.getLastCommitYear().compareTo(year) > 0) {
                    Integer newYear = Integer.parseInt(year) + 1;
                    Bson update = Updates.set("lastCommitYear", Integer.toString(newYear));
                    incrementCommits(userId, 0);
                    collection.updateOne(filter, update);
                } else {
                    Bson update = Updates.set("lastCommitYear", year);
                    collection.updateOne(filter, update);
                }
                System.out.println("lastCommitYear Set");
            }
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<RepositoryModel> getAllRepos(ObjectId userId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson filter = Filters.eq("_id", userId);
        UserModel user = collection.find(filter).first();
        return user.getRepositories();
    }

    public static RepositoryModel getRepo(ObjectId userId, ObjectId repoId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson userFilter = Filters.eq("_id", userId);
        UserModel user = collection.find(userFilter).first();
        ArrayList<RepositoryModel> repositories = user.getRepositories();
        for (RepositoryModel repo : repositories) {
            if (repo.getId().equals(repoId)) {
                return repo;
            }
        }
        return null;
    }

    public static RepositoryModel getRepoByName(ObjectId userId, String repoName) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson userFilter = Filters.eq("_id", userId);
        UserModel user = collection.find(userFilter).first();
        ArrayList<RepositoryModel> repositories = user.getRepositories();
        for (RepositoryModel repo : repositories) {
            if (repo.getRepositoryName().equals(repoName)) {
                return repo;
            }
        }
        return null;
    }

    public static void addRepository(ObjectId userId, RepositoryModel repository) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson userFilter = Filters.eq("_id", userId);
        // MongoDB does not automatically generate _id for sub-documents (like
        // repositories inside repositories).
        repository.setId(new ObjectId());
        Bson update = Updates.push("repositories", repository);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        collection.findOneAndUpdate(userFilter, update, options);
    }

    public static void deleteRepository(ObjectId userId, ObjectId repoId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson userFilter = Filters.eq("_id", userId);
        Bson repoFilter = Filters.eq("_id", repoId);
        Bson update = Updates.pull("repositories", repoFilter);
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        collection.findOneAndUpdate(userFilter, update, options);
    }
    public static ArrayList<RepositoryModel> getRecentRepos(ObjectId userId){
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson userFilter = Filters.eq("_id", userId);
        UserModel user = collection.find(userFilter).first();
        ArrayList<RepositoryModel> repositories = user.getRepositories();
        ArrayList<RepositoryModel> recentRepos = new ArrayList<>();
        if (!repositories.isEmpty()) {
            for (int i = repositories.size()-1; i >= 0; i--) {
                recentRepos.add(repositories.get(i));
            }
            return recentRepos;
        }
        else{
            return null;
        }
    }
    // When a File is added, basically we are updating the repository
    public static boolean addFileToRepository(ObjectId userId, ObjectId repoId,ObjectId fileId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        // Use the _id field of the user to find the document.
        Bson userFilter = Filters.eq("_id", userId);
        // Use the $[<identifier>] array filter to identify the repository to modify
        // based on its id.
        // The "repo" in "repo.id" is the identifier(we created) to refer to each
        // element in the repositories array.
        // repo.id accesses the id field of the array element.
        Bson repoFilter = Filters.eq("repo._id", repoId);
        // Use the $push operator to append the new file ObjectId to the files array of
        // the targeted repository.
        Bson update = Updates.push("repositories.$[repo].files", fileId);

        // the method ReturnDocument.AFTER ensures that the method returns the document
        // after the update is applied.
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        // the update operation will know how to match the specific repository using
        // this filter.
        options.arrayFilters(Arrays.asList(repoFilter));

        // This operation will return the updated document if the update succeeds.
        try {
            // Perform the update
            UserModel updatedUser = collection.findOneAndUpdate(userFilter, update, options);

            // Check if the operation was successful
            if (updatedUser != null) {
                System.out.println("File added to repository successfully.");
                return true;
            } else {
                System.out.println("Failed to add file to repository.");
                return false;
            }
        } catch (Exception e) {
            // Log any exceptions
            e.printStackTrace();
            return false;
        }
    }

    // When a File is deleted, basically we are updating the repository
    public static boolean deleteFileFromRepository(ObjectId userId, ObjectId repoId, ObjectId fileId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        // Use the _id field of the user to find the document.
        Bson userFilter = Filters.eq("_id", userId);
        // Use the $[<identifier>] array filter to identify the repository to modify
        // based on its id.
        // The "repo" in "repo.id" is the identifier(we created) to refer to each
        // element in the repositories array.
        // repo.id accesses the id field of the array element.
        Bson repoFilter = Filters.eq("repo._id", repoId);
        // Use the $pull operator to remove the file ObjectId from the files array of
        // the targeted repository.
        Bson update = Updates.pull("repositories.$[repo].files", fileId);

        // the method ReturnDocument.AFTER ensures that the method returns the document
        // after the update is applied.
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .returnDocument(ReturnDocument.AFTER)
                .arrayFilters(Arrays.asList(repoFilter));
        // the update operation will know how to match the specific repository using
        // this filter.

        // This operation will return the updated document if the update succeeds.
        try {
            // Attempt the update
            UserModel updatedDocument = collection.findOneAndUpdate(userFilter, update, options);

            // Check if the operation modified the document
            if (updatedDocument != null) {
                return true; // File successfully deleted
            } else {
                return false; // No matching userId or repoId found
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Handle exceptions gracefully
        }
    }

    public static ArrayList<FileModel> getAllFilesFromRepo(ObjectId userId, ObjectId repoId) {
        RepositoryModel repo = getRepo(userId, repoId);
        ArrayList<ObjectId> fileIds = repo.getFiles();
        System.out.println(fileIds);
        ArrayList<FileModel> files = new ArrayList<>();
        try {
            for (ObjectId objectId : fileIds) {
                FileModel file = new FileModel();
                GridFSFile dbfile = GridFSUtility.getFile(objectId);
                file.setDate(dbfile.getUploadDate());
                file.setFilename(dbfile.getFilename());
                file.setId(dbfile.getObjectId());
                int index = dbfile.getFilename().lastIndexOf(".");
                String fileType = dbfile.getFilename().substring(index+1);
                file.setType(fileType);
                file.setRepoName(repo.getRepositoryName());
                files.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No files exist");
        }
        return files;
    }

    /*
     * Admin
     */
    public static ObjectId insertUserData(UserModel user) {
        MongoCollection<UserModel> collection = getMongoCollection();
        BsonValue insertedId = collection.insertOne(user).getInsertedId();
        ObjectId objectId = insertedId.asObjectId().getValue();
        System.out.println("Inserted ObjectId: " + objectId);
        return objectId;
    }

    public static ArrayList<UserModel> getAllUsers() {
        MongoCollection<UserModel> collection = getMongoCollection();
        ArrayList<UserModel> users = new ArrayList<>();
        collection.find().into(users);
        return users;
    }

    public static void deleteUser(ObjectId userId) {
        MongoCollection<UserModel> collection = getMongoCollection();
        try {
            Bson filter = Filters.eq("_id", userId);
            if (collection.find(filter).first() != null) {
                collection.deleteOne(filter);
                System.out.println("User Deleted");
            }
        } catch (MongoException e) {
            System.out.println("No such user Exists!");
        }
    }
    /*
     * Both
     */
    // Validates the User on Login
    public static UserModel getUserData(String username, String password) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson filter = Filters.and(Filters.eq("username", username), Filters.eq("password", password));
        UserModel user = collection.find(filter).first();
        return user;
    }

    // User with same email and password must not be created
    public static boolean isUserExists(String email, String password, String passkey) {
        MongoCollection<UserModel> collection = getMongoCollection();
        Bson filter = Filters.or(Filters.eq("email", email), Filters.eq("password", password),
                Filters.eq("passkey", passkey));
        ArrayList<UserModel> user = new ArrayList<>();
        collection.find(filter).into(user);
        return user.isEmpty();
    }

    // public static UserModel getUserData(ObjectId userId) {
    //     MongoCollection<UserModel> collection = getMongoCollection();
    //     try {
    //         Bson filter = Filters.eq("_id", userId);
    //         return collection.find(filter).first();
    //     } catch (MongoException e) {
    //         System.out.println("User not Found");
    //         return null;
    //     }
    // }

    public static ObjectId createAdmin(){
        MongoDBConnection con = Model.getInstance().getConnection();
        MongoDatabase database = con.getDatabase(DATABASE_NAME);
        System.out.println(database);
        if (database == null) {
            throw new RuntimeException("Failed to get database connection");
        }
        MongoCollection<AdminModel> collection = database.getCollection("admin", AdminModel.class);
        AdminModel admin = new AdminModel();
        BsonValue insertedId = collection.insertOne(admin).getInsertedId();
        ObjectId objectId = insertedId.asObjectId().getValue();
        return objectId;
    }
    public static AdminModel getAdmin(String username, String password){
        MongoDBConnection con = Model.getInstance().getConnection();
        MongoDatabase database = con.getDatabase(DATABASE_NAME);
        System.out.println(database);
        if (database == null) {
            throw new RuntimeException("Failed to get database connection");
        }
        MongoCollection<AdminModel> collection = database.getCollection("admin", AdminModel.class);
        try {
            Bson filter = Filters.and(Filters.eq("username", username), Filters.eq("password", password));
            AdminModel admin = collection.find(filter).first();
            return admin;
        } catch (MongoException e) {
            System.out.println("User not Found");
            return null;
        }
    }
    public static void deleteAdmin(ObjectId adminId){
        MongoDBConnection con = Model.getInstance().getConnection();
        MongoDatabase database = con.getDatabase(DATABASE_NAME);
        System.out.println(database);
        if (database == null) {
            throw new RuntimeException("Failed to get database connection");
        }
        MongoCollection<AdminModel> collection = database.getCollection("admin", AdminModel.class);
        try {
            Bson filter = Filters.eq("_id", adminId);
            if (collection.find(filter).first() != null) {
                collection.deleteOne(filter);
                System.out.println("User Deleted");
            }
        } catch (MongoException e) {
            System.out.println("No such user Exists!");
        }
    }
}
