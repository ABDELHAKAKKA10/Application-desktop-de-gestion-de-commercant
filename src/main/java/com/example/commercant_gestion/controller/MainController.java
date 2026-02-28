package com.example.commercant_gestion.controller;

import com.example.commercant_gestion.model.DataModel;
import com.example.commercant_gestion.model.Sale;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Center Views
    @FXML
    private StackPane contentArea;
    @FXML
    private Node productView;
    @FXML
    private Node clientView;
    @FXML
    private Node saleView;
    @FXML
    private Node historyView;


    @FXML
    private Button btnProducts;
    @FXML
    private Button btnClients;
    @FXML
    private Button btnNewSale;
    @FXML
    private Button btnHistory;


    @FXML
    private SaleController saleViewController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        DataModel.getInstance().setOnEditSaleRequest(this::editSale);


        showView(productView, btnProducts);
    }

    @FXML
    private void handleShowProducts() {
        showView(productView, btnProducts);
    }

    @FXML
    private void handleShowClients() {
        showView(clientView, btnClients);
    }

    @FXML
    private void handleShowNewSale() {
        showView(saleView, btnNewSale);
    }

    @FXML
    private void handleShowHistory() {
        showView(historyView, btnHistory);
    }

    private void showView(Node view, Button activeButton) {
        // Hide all
        if (productView != null)
            productView.setVisible(false);
        if (clientView != null)
            clientView.setVisible(false);
        if (saleView != null)
            saleView.setVisible(false);
        if (historyView != null)
            historyView.setVisible(false);

        // Show target
        if (view != null)
            view.setVisible(true);

        // Update Button Styles
        resetButtonStyles();
        activeButton.getStyleClass().add("nav-button-active");
    }

    private void resetButtonStyles() {
        btnProducts.getStyleClass().removeAll("nav-button-active");
        btnClients.getStyleClass().removeAll("nav-button-active");
        btnNewSale.getStyleClass().removeAll("nav-button-active");
        btnHistory.getStyleClass().removeAll("nav-button-active");
    }

    private void editSale(Sale sale) {
        if (saleViewController != null) {
            saleViewController.initEditMode(sale);
            handleShowNewSale(); // Switch to sale view
        } else {
            System.err.println("SaleController reference is null!");
        }
    }
}
