package com.example.commercant_gestion.controller;

import com.example.commercant_gestion.model.DataModel;
import com.example.commercant_gestion.model.Product;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtCategorie;
    @FXML
    private TextField txtPrix;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Product> tableProducts;
    @FXML
    private TableColumn<Product, Integer> colId;
    @FXML
    private TableColumn<Product, String> colNom;
    @FXML
    private TableColumn<Product, String> colCategorie;
    @FXML
    private TableColumn<Product, Double> colPrix;
    @FXML
    private TableColumn<Product, Integer> colStock;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind table to shared data model
        tableProducts.setItems(DataModel.getInstance().getProducts());

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        // Selection listener
        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
            }
        });
    }

    private void fillForm(Product product) {
        txtNom.setText(product.getNom());
        txtCategorie.setText(product.getCategorie());
        txtPrix.setText(String.valueOf(product.getPrixUnitaire()));
        txtStock.setText(String.valueOf(product.getQuantiteStock()));
    }

    @FXML
    private void handleAdd() {
        if (isValidInput()) {
            String nom = txtNom.getText();
            String categorie = txtCategorie.getText();
            double prix = Double.parseDouble(txtPrix.getText());
            int stock = Integer.parseInt(txtStock.getText());

            Product product = new Product(nom, categorie, prix, stock);
            DataModel.getInstance().addProduct(product);
            handleClear();
        }
    }

    @FXML
    private void handleUpdate() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null && isValidInput()) {
            selected.setNom(txtNom.getText());
            selected.setCategorie(txtCategorie.getText());
            selected.setPrixUnitaire(Double.parseDouble(txtPrix.getText()));
            selected.setQuantiteStock(Integer.parseInt(txtStock.getText()));

            DataModel.getInstance().updateProduct(selected);
            handleClear();
            tableProducts.refresh();
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un produit à modifier.");
        }
    }

    @FXML
    private void handleDelete() {
        Product selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le produit ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DataModel.getInstance().deleteProduct(selected);
                handleClear();
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un produit à supprimer.");
        }
    }

    @FXML
    private void handleClear() {
        txtNom.clear();
        txtCategorie.clear();
        txtPrix.clear();
        txtStock.clear();
        tableProducts.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText();
        DataModel.getInstance().searchProducts(keyword);
    }

    private boolean isValidInput() {
        String errorMessage = "";

        if (txtNom.getText() == null || txtNom.getText().isEmpty()) {
            errorMessage += "Nom invalide!\n";
        }
        if (txtPrix.getText() == null || txtPrix.getText().isEmpty()) {
            errorMessage += "Prix invalide!\n";
        } else {
            try {
                double prix = Double.parseDouble(txtPrix.getText());
                if (prix < 0)
                    errorMessage += "Le prix doit être positif!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Prix invalide (doit être un nombre)!\n";
            }
        }
        if (txtStock.getText() == null || txtStock.getText().isEmpty()) {
            errorMessage += "Stock invalide!\n";
        } else {
            try {
                int stock = Integer.parseInt(txtStock.getText());
                if (stock < 0)
                    errorMessage += "Le stock doit être positif!\n";
            } catch (NumberFormatException e) {
                errorMessage += "Stock invalide (doit être un entier)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Champs Invalides", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
