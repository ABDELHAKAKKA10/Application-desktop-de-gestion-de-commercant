package com.example.commercant_gestion.controller;

import com.example.commercant_gestion.model.Client;
import com.example.commercant_gestion.model.DataModel;
import com.example.commercant_gestion.model.Product;
import com.example.commercant_gestion.model.Sale;
import com.example.commercant_gestion.model.SaleLine;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SaleController implements Initializable {

    @FXML
    private ComboBox<Client> cbClient;
    @FXML
    private ComboBox<Product> cbProduct;
    @FXML
    private TextField txtQuantity;
    @FXML
    private Label lblPrice;
    @FXML
    private TableView<SaleLine> tableSaleLines;
    @FXML
    private TableColumn<SaleLine, String> colProductName;
    @FXML
    private TableColumn<SaleLine, Integer> colQuantity;
    @FXML
    private TableColumn<SaleLine, Double> colUnitPrice;
    @FXML
    private TableColumn<SaleLine, Double> colTotalLine;
    @FXML
    private Label lblTotalSale;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnCancel;

    private ObservableList<SaleLine> lineItems;
    private Sale currentSale;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lineItems = FXCollections.observableArrayList();
        currentSale = new Sale(null);

        // Bind ComboBoxes
        cbClient.setItems(DataModel.getInstance().getClients());
        cbProduct.setItems(DataModel.getInstance().getProducts());

        // Product selection listener
        cbProduct.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                lblPrice.setText(String.valueOf(newVal.getPrixUnitaire()));
            } else {
                lblPrice.setText("0.0");
            }
        });

        // Setup Table
        colProductName
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getNom()));
        colQuantity.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantite()));
        colUnitPrice.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire()));
        colTotalLine.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotal()));

        tableSaleLines.setItems(lineItems);

        if (btnCancel != null)
            btnCancel.setVisible(false);
    }

    // API for Edit Mode
    public void initEditMode(Sale sale) {
        this.isEditMode = true;
        this.currentSale = sale;

        // Restore Client
        if (sale.getClient() != null) {
            for (Client c : cbClient.getItems()) {
                if (c.getId() == sale.getClient().getId()) {
                    cbClient.getSelectionModel().select(c);
                    break;
                }
            }
        }

        // Restore Lines
        lineItems.setAll(DataModel.getInstance().getSaleLines(sale.getId()));
        currentSale.setLines(lineItems);

        updateTotal();

        if (btnConfirm != null)
            btnConfirm.setText("Mettre à jour");
        if (btnCancel != null)
            btnCancel.setVisible(true);
    }

    public void resetMode() {
        isEditMode = false;
        currentSale = new Sale(null);
        lineItems.clear();
        cbClient.getSelectionModel().clearSelection();
        cbProduct.getSelectionModel().clearSelection();
        updateTotal();

        if (btnConfirm != null)
            btnConfirm.setText("Valider la Vente");
        if (btnCancel != null)
            btnCancel.setVisible(false);
    }

    @FXML
    private void handleCancelEdit() {
        resetMode();
    }

    @FXML
    private void handleRefreshClients() {
        DataModel.getInstance().loadData();
    }

    @FXML
    private void handleAddToSale() {
        Product selectedProduct = cbProduct.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit.");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(txtQuantity.getText());
            if (quantity <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Quantité invalide.");
            return;
        }

        // Logic for stock check during edit is complex because we hold some stock
        // ourselves
        // Simplified check:
        if (quantity > selectedProduct.getQuantiteStock()) {
            // Ideally check if we are returning stock from old version, but simple check
            // prevents selling more than current stock on shelves
            showAlert("Stock insuffisant", "Il ne reste que " + selectedProduct.getQuantiteStock() + " unités.");
            return;
        }

        SaleLine line = new SaleLine(selectedProduct, quantity, selectedProduct.getPrixUnitaire());
        lineItems.add(line);
        currentSale.addLine(line);

        updateTotal();
    }

    private void updateTotal() {
        lblTotalSale.setText(String.valueOf(currentSale.getMontantTotal()));
    }

    @FXML
    private void handleConfirmSale() {
        Client selectedClient = cbClient.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert("Erreur", "Veuillez sélectionner un client.");
            return;
        }

        if (lineItems.isEmpty()) {
            showAlert("Erreur", "Le panier est vide.");
            return;
        }

        currentSale.setClient(selectedClient);
        currentSale.setLines(lineItems);

        try {
            if (isEditMode) {
                DataModel.getInstance().updateSale(currentSale);
                showAlert("Succès", "Vente modifiée avec succès !");
            } else {
                DataModel.getInstance().createSale(currentSale);
                showAlert("Succès", "Vente enregistrée avec succès !");
            }
            resetMode();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SGBD", "Erreur: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
