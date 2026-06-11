# MiniGitHub

A lightweight desktop repository and file management client built with JavaFX and MongoDB. MiniGitHub mimics core GitHub-like features — user management, repository creation, and file storage — in a locally hosted environment.

---

## Features

- **Admin panel** — create and delete user accounts, view all users
- **User dashboard** — activity overview and recent repositories
- **Repository management** — create, upload, and download repositories with auto-generated READMEs
- **File management** — upload, view, edit, and download files stored in MongoDB GridFS
- **File viewer/editor** — view code, edit content, copy to clipboard, and save changes back to GridFS
- **Authentication modal** — protected operations require re-authentication

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | JavaFX |
| Database | MongoDB (Java Driver) |
| File Storage | MongoDB GridFS |
| Language | Java (modular, `module-info.java`) |
| Icons | de.jensd.fx.glyphs |

---

## Prerequisites

- Java 17+
- JavaFX SDK
- MongoDB running locally on `mongodb://localhost:27017`
- Maven (for dependency management)

---

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/minigithub.git
   cd minigithub
   ```

2. **Start MongoDB**
   ```bash
   mongod
   ```

3. **Build and run**
   ```bash
   mvn clean javafx:run
   ```

4. **Login**
   - A default admin account is created automatically on startup.
   - Use the admin account to create user accounts, then log in as a user.

---

## Project Structure

```
src/main/java/com/minigithub/
├── App.java                    # Entry point
├── model/                      # Data models (User, Repo, File, shared Model state)
├── database/                   # MongoDB connection, DB operations, GridFS utility
├── controllers/
│   ├── Admin/                  # Admin UI controllers
│   └── User/                   # User UI controllers
└── Views/                      # FXML loaders, scene management, cell factories
```

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin` |

> ⚠️ The admin account is ephemeral — it is created at startup and deleted after login. Users must be created by the admin before a user session begins.

---

## Known Limitations & Planned Improvements

- Passwords are stored in plaintext — hashing (BCrypt/Argon2) should be added before any real use
- MongoDB connection string is hardcoded; should be moved to a config file
- No file versioning or diff support
- No repository-level permissions or sharing
- No unit or integration tests yet

---

## License

MIT
