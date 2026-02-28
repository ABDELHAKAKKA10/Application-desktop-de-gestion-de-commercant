CREATE DATABASE IF NOT EXISTS gestion_commercant;
USE gestion_commercant;

CREATE TABLE IF NOT EXISTS client (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS produit (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    categorie VARCHAR(100),
    prix_unitaire DOUBLE NOT NULL,
    quantite_stock INT NOT NULL
);

CREATE TABLE IF NOT EXISTS vente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    montant_total DOUBLE NOT NULL,
    client_id INT,
    FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ligne_vente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vente_id INT NOT NULL,
    produit_id INT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DOUBLE NOT NULL,
    FOREIGN KEY (vente_id) REFERENCES vente(id) ON DELETE CASCADE,
    FOREIGN KEY (produit_id) REFERENCES produit(id)
);
