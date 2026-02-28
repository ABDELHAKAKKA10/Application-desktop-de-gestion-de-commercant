package com.example.commercant_gestion.controller;

import com.example.commercant_gestion.model.DataModel;
import com.example.commercant_gestion.model.Sale;
import com.example.commercant_gestion.model.SaleLine;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML
    private TableView<Sale> tableSales;
    @FXML
    private TableColumn<Sale, Integer> colId;
    @FXML
    private TableColumn<Sale, String> colDate;
    @FXML
    private TableColumn<Sale, String> colClient;
    @FXML
    private TableColumn<Sale, Double> colTotal;

    @FXML
    private TableView<SaleLine> tableDetails;
    @FXML
    private TableColumn<SaleLine, String> colDetailProduct;
    @FXML
    private TableColumn<SaleLine, Integer> colDetailQuantity;
    @FXML
    private TableColumn<SaleLine, Double> colDetailPrice;
    @FXML
    private TableColumn<SaleLine, Double> colDetailTotal;

    private ObservableList<SaleLine> detailList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // --- Sales Table ---
        tableSales.setItems(DataModel.getInstance().getSales());

        colId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getId()));
        colDate.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDateVente().toString()));
        colClient.setCellValueFactory(cellData -> {
            if (cellData.getValue().getClient() != null) {
                return new SimpleStringProperty(cellData.getValue().getClient().toString());
            } else {
                return new SimpleStringProperty("Inconnu/Supprimé");
            }
        });
        colTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getMontantTotal()));

        // Listener for Detail View
        tableSales.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadDetails(newVal);
            } else {
                detailList.clear();
            }
        });

        // --- Details Table ---
        detailList = FXCollections.observableArrayList();
        tableDetails.setItems(detailList);

        colDetailProduct
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getNom()));
        colDetailQuantity
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getQuantite()));
        colDetailPrice
                .setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getPrixUnitaire()));
        colDetailTotal.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getTotal()));
    }

    private void loadDetails(Sale sale) {
        detailList.clear();
        detailList.addAll(DataModel.getInstance().getSaleLines(sale.getId()));
    }

    @FXML
    private void handleRefresh() {
        DataModel.getInstance().refreshSales();
        detailList.clear();
    }

    @FXML
    private void handleModify() {
        Sale selectedSale = tableSales.getSelectionModel().getSelectedItem();
        if (selectedSale != null) {
            DataModel.getInstance().requestEditSale(selectedSale);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setContentText("Veuillez sélectionner une vente à modifier.");
            alert.showAndWait();
        }
    }
}
