# Medicine Reminder and Management System (Java Swing + MySQL + JDBC)

A beginner-friendly, modular Java desktop application designed for second-year engineering Java Project-Based Learning (PBL) demonstrations.

## 1. Prerequisites
- **JDK 17+** (or JDK 11+)
- **MySQL Server 8.0+** (or XAMPP/MariaDB)
- **MySQL Connector/J JAR** (`mysql-connector-j-8.x.x.jar`) placed in `lib/`
- **IDE**: VS Code (Extension Pack for Java) or IntelliJ IDEA Community Edition

## 2. Step-by-Step Setup & Execution

### Step 1: Create the MySQL Database
```bash
mysql -u root -p < database/schema.sql
```
*(Or open `database/schema.sql` in MySQL Workbench / phpMyAdmin and execute the script.)*

### Step 2: Configure Database Credentials
Edit `config/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/medicine_reminder_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD
```

### Step 3: Compile & Run (Command Line)
**Windows (PowerShell / CMD):**
```bat
mkdir out
javac -cp "lib/*" -d out src/com/medreminder/*.java src/com/medreminder/*/*.java
java -cp "out;lib/*" com.medreminder.Main
```

**macOS / Linux:**
```bash
mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" com.medreminder.Main
```

### Step 4: Demo Credentials
- Username: `arjun_med`
- Password: `MedTrack@123`
- Or click **"New User? Create an Account"** in the Login window to register a fresh user.
