# ChestPlugin

**Description:**  
ChestPlugin is a Minecraft plugin designed to restrict access to chests and other storage blocks using security profiles managed in a MySQL database. Players can create multiple security profiles, add or remove members, and assign profiles to blocks to control who can access them.

---

## Commands

The plugin provides the following commands:

- `/chest help`  
  Show this help message.

- `/chest show`  
  Show your secure profiles.

- `/chest members <profileName>`  
  Show members of a secure profile.

- `/chest create <name>`  
  Add a new secure profile.

- `/chest delete <profileName>`  
  Delete a secure profile.

- `/chest add <profileName> <memberName>`  
  Add a member to a secure profile.

- `/chest remove <profileName> <memberName>`  
  Remove a member from a secure profile.

- `/chest default <profileName>`  
  Set a secure profile as default.

---

## Build Instructions

To compile the plugin, simply execute:

```bash
mvn clean package
```
The final .jar file will be located in the target directory.

---

## Configuration

You need to import the config_chest_database.yml file into your plugin's configuration folder. This file contains the database connection information required to link the plugin with your MySQL database.

Example config_chest_database.yml structure:

```yaml
database:
  host: "your-database-host"
  port: 3306
  database: "your-database-name"
  user: "your-database-username"
  password: "your-database-password"
```