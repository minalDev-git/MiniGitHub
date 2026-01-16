# MiniGitHub — Project Documentation

---

## Project objective
MiniGitHub is a lightweight desktop client that mimics essential repository and file management features: user administration, repository creation, file upload/download/storage (via MongoDB GridFS), and a JavaFX-based GUI for browsing, viewing and editing file contents. The project focuses on an educational/simple local-hosted alternative to full GitHub, demonstrating UI flow, data modeling and GridFS integration.

---

## Tech stack & dependencies
- Java (modular application using `module-info.java`)
- JavaFX (UI)
- MongoDB (Java drivers) with GridFS for file storage
  - org.mongodb.driver (sync / reactives, BSON)
- GridFS utility for uploading/downloading file contents
- de.jensd.fx.glyphs libraries (icon packs for JavaFX)
- Project structure: MVC-ish separation (controllers, model, views) with a singleton `Model` for shared state

Files of interest:
- `src/main/java/com/minigithub/*` — application code
- `module-info.java` — module declarations
- `src/main/resources` / `target/classes` — styling (CSS)
- `READMEFiles/README.txt` — template README created for new repositories

---

## High-level features & functionality
- Login screen with account-type choice (User / Admin)
- Admin flow:
  - Create users
  - List and delete existing users
- User flow:
  - Create repositories
  - Upload files into repositories (stored in MongoDB GridFS)
  - Download files and repositories to local disk
  - View file contents (with copy-to-clipboard)
  - Edit and save file contents (where allowed)
  - Automatic README generation for new repos
  - UI navigation: Dashboard, Repositories, Profile, Files, File View
- GridFS-backed file storage + metadata via model objects
- Modal authentication box for sensitive operations
- Light UX helpers (auto-dismiss alerts, cell factories for list views)
- Streak / activity tracking (basic implementation present in the model)

---

## Main classes (one-line purpose per class)

### Application entry & module
- `com.minigithub.App` — Application bootstrap; creates default admin and shows login window.
- `module-info.java` — Module declaration / runtime module requirements.

### Models
- `com.minigithub.model.Person` — Abstract base class for account types (id, username, password).
- `com.minigithub.model.AdminModel` — Admin account model (extends `Person`, default admin creds).
- `com.minigithub.model.UserModel` — User account model (extends `Person`, holds email, passkey, repositories, activity).
- `com.minigithub.model.RepositoryModel` — Repository metadata (owner, name, description, date, file ids).
- `com.minigithub.model.FileModel` — File metadata (filename, type, date, repo name).
- `com.minigithub.model.Model` — Global singleton application state and helpers (current user, selected repo/file, flags, cached lists).

### Database & storage
- `com.minigithub.database.MongoDBConnection` — Manages connection to MongoDB and provides a `MongoDatabase`.
- `com.minigithub.database.DatabaseDriver` — Higher-level DB operations: user insertion, retrieval, delete; repository/file metadata updates; admin creation/validation; many DB interactions.
- `com.minigithub.database.GridFSUtility` — GridFS helper for uploading, downloading, updating, deleting files and creating README.

### Views & UI helpers
- `com.minigithub.Views.ViewScenes` — Centralized loader for FXML scenes and utilities to show login/user/admin windows, modals and to expose selection properties used for navigation.
- `com.minigithub.Views.AccountType` (enum) — Account types: ADMIN, USER.
- `com.minigithub.Views.AdminMenuOptions` (enum) — Admin menu navigation options.
- `com.minigithub.Views.UserMenuOptions` (enum) — User menu options.
- Cell factories:
  - `com.minigithub.Views.DashboardCellFactory` — ListCell factory for Dashboard repo items.
  - `com.minigithub.Views.RepositoryCellFactory` — ListCell factory for repo list.
  - `com.minigithub.Views.FileCellFactory` — ListCell factory for file list.
- CSS and resource styles under `src/main/resources` / `target/classes`.

### Controllers (UI)
- `com.minigithub.controllers.LoginController` — Login UI: choose account type and authenticate users/admins.
- Admin controllers:
  - `com.minigithub.controllers.Admin.AdminController` — Admin window controller: reacts to admin menu selection and performs logout.
  - `com.minigithub.controllers.Admin.AdminMenuController` — Admin menu (buttons) handling.
  - `com.minigithub.controllers.Admin.CreateUserController` — Admin interface to create new users (validation, DB insert).
  - `com.minigithub.controllers.Admin.UsersController` — Displays all users in a list.
  - `com.minigithub.controllers.Admin.UsersCellController` — Per-user cell controller (actions such as delete/download).
- User controllers:
  - `com.minigithub.controllers.User.UserController` — User window main controller: reacts to user menu selections and manages center content.
  - `com.minigithub.controllers.User.DashboardController` — Dashboard: shows recent repositories, stats.
  - `com.minigithub.controllers.User.DashboardCellController` — Repo cell in dashboard.
  - `com.minigithub.controllers.User.RepositoriesController` — Manage repositories: list, upload repo, download repo, create new repo.
  - `com.minigithub.controllers.User.RepositoryCellController` — Per-repo cell controller (delete, download, view).
  - `com.minigithub.controllers.User.CreateRepoController` — UI to create a new repository (creates README, inserts metadata).
  - `com.minigithub.controllers.User.FileController` — Manage files within a repository: upload, download, list.
  - `com.minigithub.controllers.User.FileCellController` — Per-file list cell: delete (with auth), open/view file.
  - `com.minigithub.controllers.User.FileViewController` — File viewer/editor: view code, edit, save, copy, download.
  - `com.minigithub.controllers.User.ProfileController` — Profile view/edit for logged-in user.

---

## Important classes — methods & responsibilities (detailed)

Note: method lists below reflect the public/visible methods and responsibilities evident from the codebase.

### com.minigithub.model.Model (singleton application state)
- Purpose: central shared state & helpers for the application (selected user/repo/file, lists, flags).
- Key fields:
  - `ObjectProperty<UserModel> user` — currently selected / logged-in user.
  - `ObjectProperty<RepositoryModel> repository` — selected repository.
  - `ObjectProperty<FileModel> file` — selected file.
  - ObservableLists for `files`, `repositories`, and `users`.
  - flags: `userLoginFlag`, `adminLoginFlag`, `authflag` (used for authorization checks), `streakFlag`.
  - `MongoDBConnection connection` and `ViewScenes viewScene` singletons.
- Key methods:
  - `public static synchronized Model getInstance()` — returns the singleton instance.
  - `public void reset()` — clears user, repository and file selections and resets flags / lists.
  - `public ViewScenes getViewScene()` — returns the `ViewScenes` helper for loading UI.
  - `public MongoDBConnection getConnection()` — returns DB connection helper.
  - `public AccountType getLoginAccountType()` / `setLoginAccountType(...)` — get/set whether login is ADMIN/USER.
  - Getters/setters for `user`, `repository`, `file` (and their ObjectProperty wrappers).
  - `public void setRepositories(ArrayList<RepositoryModel> repositories)` — sets repo list in the observable list.
  - Admin-related helpers:
    - `createAdmin()` — calls DB driver to ensure admin exists.
    - `deleteAdmin()` — delete admin record after use (project uses ephemeral admin).
    - `getAdminLoginFlag()` / `setAdminLoginFlag(boolean)`.
  - Authorization flags and streak getters/setters.

Why important: Model provides the shared state and navigation hooks used across controllers, making it the de-facto application context.

---

### com.minigithub.database.DatabaseDriver
- Purpose: wrapper for MongoDB collection access and database operations across Users, Repositories, Files and Admin account.
- Notable responsibilities / methods (representative):
  - `public static ObjectId createAdmin()` — inserts a default admin record and returns ObjectId.
  - `public static AdminModel getAdmin(String username, String password)` — fetch admin doc matching username/password.
  - `public static void deleteAdmin(ObjectId adminId)` — remove admin doc (used after admin login flow).
  - `public static ObjectId insertUserData(UserModel user)` — insert a new user document and return id.
  - `public static ArrayList<UserModel> getAllUsers()` — return all users.
  - `public static void deleteUser(ObjectId userId)` — delete a user by id.
  - `public static UserModel getUserData(String username, String password)` — validate a user on login.
  - `public static boolean isUserExists(String email, String password, String passkey)` — check duplicates before creation.
  - Repository and file metadata updates:
    - `deleteRepository(ObjectId userId, ObjectId repoId)`
    - `getRecentRepos(ObjectId userId)`
    - `addFileToRepository(userId, repoId, fileId)` — update repo's file list
    - `deleteFileFromRepository(userId, repoId, fileId)` — remove file id from repo
    - `getAllFilesFromRepo(userId, repoId)` — list all files (metadata) for repo
- Why important: DatabaseDriver centralizes database queries and updates (collections, Filters, Updates), used by UI controllers and model to persist and fetch app data.

---

### com.minigithub.database.GridFSUtility
- Purpose: handle GridFS file storage (upload, download, read, update, delete), and create README files for new repos.
- Key methods:
  - `private static GridFSBucket getGridFSBucket()` — obtains GridFSBucket from `MongoDBConnection` for the configured database.
  - `public static ObjectId uploadFile(String filePath, String fileName, String ext)` — upload local file to GridFS; returns file id.
  - `public static GridFSFile getFile(ObjectId fileId)` — get GridFS metadata by id.
  - `public static GridFSFile getFileByName(String filename)` — find a GridFS file by filename.
  - `public static void downloadFile(ObjectId fileId, String destinationPath)` — save GridFS file to destination path.
  - `public static byte[] getFileContents(ObjectId fileId)` — get bytes of file stored in GridFS.
  - `public static ObjectId updateFile(ObjectId fileId, String filename, String ext, InputStream inputStream)` — replace/update file contents in GridFS.
  - `public static FileModel createREADME(ObjectId userId, String repoName)` — generate a README text file, upload to GridFS and return a `FileModel` describing it.
  - `public static void deleteFile(ObjectId fileId)` — delete file from GridFS bucket.
- Why important: GridFSUtility is the layer that persists raw file content; all file upload/download functions in the UI call into this utility.

---

### com.minigithub.database.MongoDBConnection
- Purpose: manage MongoDB client and provide `MongoDatabase` with POJO codec registry.
- Key aspects:
  - `CONNECTION_STRING = "mongodb://localhost:27017"` (configurable in code).
  - Builds a `CodecRegistry` with POJO support to map Java model classes to BSON.
  - `public MongoDatabase getDatabase(String databaseName)` — returns the requested database instance.
  - `public void closeConnection()` — closes the MongoClient connection when needed.
- Why important: underlying DB connection provider used by DatabaseDriver and GridFSUtility.

---

### com.minigithub.Views.ViewScenes
- Purpose: centralized FXML loading and window/stage management; exposes ObjectProperty selection hooks used for navigation between views.
- Key responsibilities:
  - Lazily load AnchorPane views for Dashboard, Repositories, Profile, Files, File view, Create Repo, Create User, Users list, etc.
  - `public void showLoginWindow()` / `showUserWindow()` / `showAdminWindow()` — instantiate stages for primary areas.
  - `public void showAuthenticationModal()` — show blocking authentication modal for protected operations.
  - `getUserMenuSelectedItem()` / `getAdminMenuSelectedItem()` — expose selection properties that controllers bind to in order to drive view changes.
  - `closeStage(Stage)` — close a stage.

Why important: decouples controllers from direct FXML loading and stage creation; standardizes navigation.

---

### Selected controllers — responsibilities & methods

Note: below are controllers that coordinate UI and call model / DB / GridFS utilities.

- `LoginController`
  - Responsibilities: login screen handling. Provide account type selection (User/Admin). Authenticate user credentials by calling `DatabaseDriver.getUserData` or `getAdmin`.
  - Key method (observed):
    - `evaluateAdminCred(String username, String password)` — attempts admin validation via DatabaseDriver, sets `Model.getInstance().setAdminLoginFlag(true)` upon success and triggers deletion of the admin record in DB (project uses a temporary admin), then shows admin UI.

- `CreateUserController`
  - Responsibilities: Admin UI to create a user.
  - Key methods:
    - `initialize(...)` — set up UI listeners.
    - `createUser()` — validate inputs, build a `UserModel`, call `DatabaseDriver.insertUserData(...)`, and update UI.
    - Validation helpers: `isValidEmail(String)`, `isValidPassword(String)`, `isValidPassKey(String)`, `isUserExist(...)`, `isDataEmpty()`.
    - `clearCredentials()` — reset form fields.

- `CreateRepoController`
  - Responsibilities: create repository for a user. Calls `DatabaseDriver` to persist repository metadata and `GridFSUtility.createREADME` to attach initial README.
  - Typical methods: `initialize(...)`, repo creation/validation logic; updates `Model` repositories.

- `RepositoriesController`
  - Responsibilities: list repositories; create/upload/download whole repositories (download likely packages all files to local location); uses `RepositoryCellFactory` to render items.
  - Key methods: `uploadRepository()`, `downloadRepository()`, `onNewRepo()`, `addListeners()`, `validateRepoName(String)`, `updateStreak(RepositoryModel)`.

- `FileController`
  - Responsibilities: list files in a repository, file upload/download, and wiring file list cell behavior.
  - Key methods:
    - `initialize(...)` — wire UI and load files via `DatabaseDriver.getAllFilesFromRepo`.
    - `uploadFile()` — open file chooser, call `GridFSUtility.uploadFile` and `DatabaseDriver.addFileToRepository`, add to UI list.
    - `downloadFile()` — `GridFSUtility.downloadFile(...)` to user's Downloads folder.
    - `getUniqueFileName(...)` — ensure unique names within repository.
    - `updateStreak(FileModel)` — small activity-tracking helper.

- `FileViewController`
  - Responsibilities: view and edit file contents (text/code); copy to clipboard, save edits back to GridFS, download file.
  - Key methods:
    - `initialize(...)` & `addListeners()` — attach buttons to handlers (edit, copy, save, download, back).
    - `onEdit()` — toggle editing mode.
    - `OnSave()` — gather text, call `GridFSUtility.updateFile(...)` to update contents and update metadata via database.
    - `onDownload()` — call `GridFSUtility.downloadFile`.
    - `onCopy()` — copy contents to system clipboard.
    - `onBackPressed()` — navigate back to previous view.
    - `showAutoDismissAlert(String,int)` — show temporary alerts.

- `AdminController`, `AdminMenuController`, `UsersController`, and `UsersCellController`
  - Responsibilities: admin navigation and user management.
  - AdminMenuController wires menu buttons to set the `AdminMenuOptions` selection property.
  - AdminController listens to `Model.getInstance().getViewScene().getAdminMenuSelectedItem()` and updates the center pane; handles logout which resets flags and shows login window.
  - UsersController loads user list using `DatabaseDriver.getAllUsers()` and injects a `UsersCellFactory` for per-row interactions.
  - UsersCellController handles per-user actions (delete user, potentially download user repos etc.).

---

## Admin role — capabilities
- Login via admin credentials (default admin is created at app start).
- Create new user accounts (via Create User UI).
- View all existing users (Users list).
- Delete users (removes user document from MongoDB).
- Navigate admin-specific menu items (Create User, Users, Logout).
- Admin flow in the code uses an ephemeral admin record (created at app start and often deleted after use) — take care if relying on long-lived admin accounts.

---

## User role — capabilities
- Register (users are created by admin in this implementation).
- Login as a normal user (via LoginController).
- View Dashboard: recent repositories and activity.
- Create new repositories (CreateRepoController); a README is auto-created and stored via GridFS.
- Upload files to repositories (uploadFile), where files are stored inside MongoDB GridFS and referenced via ObjectId in repository metadata.
- View files in a repository (FileController).
- Open a file to view/edit (FileViewController): copy to clipboard, download, edit then save back to GridFS.
- Download individual files or whole repositories to the local Downloads folder.
- Manage their profile (ProfileController).
- Trigger authentication modal for certain protected operations (app shows an AuthBox view when Model.authflag is false).

---

## Notable UX / behavior decisions (observed)
- Model singleton centralizes state and is used heavily for passing selections and flags between controllers.
- GridFS is used as the single source of truth for file content; metadata is stored in normal collections (users, repositories).
- Some flows use ephemeral admin creation/deletion (createAdmin/deleteAdmin).
- Several UI elements rely on `getViewScene().getXxxSelectedItem()` properties and listeners for navigation.

---

## Short conclusion
MiniGitHub demonstrates a compact desktop repo/file management app built with JavaFX and MongoDB (GridFS). It has a clear separation between UI controllers, a central Model for state, and database helpers for persistence and GridFS operations. The project is well-suited as a learning project to understand JavaFX + MongoDB integration and building a simple "GitHub-like" experience without a distributed VCS backend.

---

## Suggested future enhancements
1. Authentication & Security
   - Hash & salt passwords (do not store raw passwords). Use PBKDF2/BCrypt/Argon2.
   - Replace ephemeral admin flow with proper admin credential management, and protect admin endpoints.
   - Use environment/config files (or system properties) for MongoDB connection string rather than a hard-coded "mongodb://localhost:27017".

2. Data integrity & transactions
   - Use transactions where multiple DB updates must be atomic (e.g., upload a file + add its id to repo).
   - Add defensive checks and error handling for DB and GridFS failures.

3. Collaboration & permissions
   - Add repository-level permissions (owner, collaborators, read-only shares).
   - Implement links/sharing or make public/private repositories.

4. Versioning & diffs
   - Provide file versioning (either by storing revisions in GridFS or tracking changes).
   - Provide diff view between versions and simple commit messages.

5. UX & performance
   - Pagination or lazy-loading for long lists (users, repositories, files).
   - Add search (by repo name, filename, content inside files).
   - Syntax highlighting in FileView for common languages (use a code editor component).

6. Testing & CI
   - Add unit tests for DatabaseDriver and GridFSUtility (with embedded Mongo or test containers).
   - Add integration tests for core flows.
   - Add a CI pipeline that runs checks on push.

7. Packaging & distribution
   - Provide a native-packaged runtime (jlink / jpackage) for end users.
   - Add configuration UI or file for DB connection and app settings.

8. Data model & schema improvements
   - Normalize or document schema for users/repositories/files in repo docs.
   - Index commonly queried fields (username, repositoryName) for performance.

9. Documentation & onboarding
   - Expand README with setup instructions (MongoDB requirement, Java/JavaFX version, running the app).
   - Provide sample data and quickstart steps.

---
