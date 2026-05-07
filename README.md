# Application de Localisation Android avec OpenStreetMap

## Description

Cette application Android permet :

- D’obtenir la position GPS de l’utilisateur
- D’afficher la position sur une carte OpenStreetMap
- D’enregistrer les coordonnées dans une base de données MySQL via PHP
- De récupérer et afficher les positions enregistrées

Technologies utilisées :

- Java Android
- OpenStreetMap
- PHP
- MySQL
- XAMPP / WAMP
- API REST

---

## Prérequis

Installer les outils suivants :

- Android Studio
- XAMPP ou WAMP
- JDK
- SDK Android

---

## Structure du projet

```bash
project/
│
├── android_app/
│
└── backend/
    ├── createPosition.php
    └── getPosition.php
```

---

## Configuration Backend PHP/MySQL

### 1. Démarrer XAMPP

Lancer :

- Apache
- MySQL

---

### 2. Créer la base de données

Ouvrir phpMyAdmin :

```bash
http://localhost/phpmyadmin
```

Créer la base de données :

```sql
CREATE DATABASE map_project;
```

---

### 3. Créer la table positions

```sql
CREATE TABLE `positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date` datetime NOT NULL,
  `imei` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

---

## Backend PHP

Créer le dossier :

```bash
C:\xampp\htdocs\map_project\
```

---

## createPosition.php

```php
<?php
header("Content-Type: application/json");

$host = "localhost";
$db_name = "map_project";
$username = "root";
$password = "";

try {

    $conn = new PDO(
        "mysql:host=$host;dbname=$db_name;charset=utf8",
        $username,
        $password
    );

    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch(PDOException $e){

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);

    exit();
}

$latitude  = $_POST['latitude'] ?? null;
$longitude = $_POST['longitude'] ?? null;
$date      = $_POST['date'] ?? null;
$imei      = $_POST['imei'] ?? null;

if(!$latitude || !$longitude || !$date || !$imei){

    echo json_encode([
        "success" => false,
        "message" => "Missing data"
    ]);

    exit();
}

try {

    $sql = "INSERT INTO positions(latitude, longitude, date, imei)
            VALUES(:latitude, :longitude, :date, :imei)";

    $stmt = $conn->prepare($sql);

    $stmt->bindParam(':latitude', $latitude);
    $stmt->bindParam(':longitude', $longitude);
    $stmt->bindParam(':date', $date);
    $stmt->bindParam(':imei', $imei);

    $stmt->execute();

    echo json_encode([
        "success" => true,
        "message" => "Position saved"
    ]);

} catch(PDOException $e){

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}
?>
```

---

## getPosition.php

```php
<?php
header("Content-Type: application/json");

$host = "localhost";
$db_name = "map_project";
$username = "root";
$password = "";

try {

    $conn = new PDO(
        "mysql:host=$host;dbname=$db_name;charset=utf8",
        $username,
        $password
    );

    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

} catch(PDOException $e){

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);

    exit();
}

try {

    $stmt = $conn->prepare(
        "SELECT * FROM positions ORDER BY date DESC"
    );

    $stmt->execute();

    $positions = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        "success" => true,
        "positions" => $positions
    ]);

} catch(PDOException $e){

    echo json_encode([
        "success" => false,
        "message" => $e->getMessage()
    ]);
}
?>
```

---

## API URLs

### Android Emulator

```bash
http://10.0.2.2/map_project/createPosition.php
```

```bash
http://10.0.2.2/map_project/getPosition.php
```

---
<img width="344" height="658" alt="Screenshot 2026-05-07 211401" src="https://github.com/user-attachments/assets/cff73e42-5273-4123-a0e4-2766af9ed9e0" />


<img width="414" height="670" alt="Screenshot 2026-05-07 211421" src="https://github.com/user-attachments/assets/a6e7d831-0c08-446f-ac29-a47a9239dc02" />

### Real Android Device

Replace with your computer IP address :

```bash
http://192.168.1.X/map_project/createPosition.php
```

---

## Android Permissions

Add inside `AndroidManifest.xml` :

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

Inside `<application>` :

```xml
android:usesCleartextTraffic="true"
```

---

## Testing

Open browser :

```bash
http://localhost/map_project/getPosition.php
```

Expected result :

```json
{
  "success": true,
  "positions": []
}
```

---

## Security Improvements

Recommended improvements :

- Validate latitude/longitude
- Add authentication
- Use HTTPS
- Store DB credentials in environment variables
- Limit API requests

---

## Features

- GPS localization
- OpenStreetMap integration
- Save locations in MySQL
- REST API with PHP
- Retrieve saved positions
- JSON communication

---

## Author

LAHDIRI Youness

Cybersecurity & Software Engineering Student
