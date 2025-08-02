---

# SimpleHomes

**SimpleHomes** is a lightweight and high-performance **home management plugin** for **Spigot 1.21**.
It supports data storage via **MySQL** or **SQLite** and requires **no external dependencies**.

---

## 🔧 Features

* Fast and minimal home system
* Configurable data storage (MySQL or SQLite)
* Simple, user-friendly commands
* Per-player home limits via permissions
* No external dependencies

---

## 📦 Installation

1. Download the plugin `.jar` and place it in your server’s `/plugins` folder.
2. Start or reload the server to generate configuration files.
3. Choose your preferred storage type (`mysql` or `sqlite`) in `config.yml`.
4. (Optional) Use a permissions plugin like **LuckPerms** to manage home limits.

---

## 💬 Commands

### `/home <homeName>`

Teleport to your saved home location.

### `/homeset <homeName>`

Set your current location as your home.
*(No permission required)*

### `/delhome <homeName>`

Delete your current home.
*(No permission required)*

---

## 🔐 Permissions

| Permission            | Description                                                           |
| --------------------- | --------------------------------------------------------------------- |
| `simplehomes.limit.X` | Sets the number of homes a player can set (replace `X` with a number) |

> Example: `simplehomes.limit.3` allows a player to set up to 3 homes.

---

## 📁 Configuration

Configure the plugin via `config.yml`.
Example:

```yaml
storage-type: sqlite # Options: sqlite, mysql

database:
  host: localhost
  port: 3306
  database: simplehomes
  username: root
  password: yourpassword
```

---

## ✅ Recommended Plugins

* [LuckPerms](https://luckperms.net/) – for managing player permissions and home limits.

---

## 📄 License

GNU GPL v3 License

---

Made with ❤️ by [Alzy](https://github.com/Alzyy)

---
