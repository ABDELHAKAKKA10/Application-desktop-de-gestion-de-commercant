package com.example.commercant_gestion.controller;

import com.example.commercant_gestion.model.Client;
import com.example.commercant_gestion.model.DataModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML
    private TextField txtPrenom;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtSearch;
    @FXML
    private TableView<Client> tableClients;
    @FXML
    private TableColumn<Client, Integer> colId;
    @FXML
    private TableColumn<Client, String> colNom;
    @FXML
    private TableColumn<Client, String> colPrenom;
    @FXML
    private TableColumn<Client, String> colTelephone;
    @FXML
    private TableColumn<Client, String> colEmail;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind to shared DataModel
        tableClients.setItems(DataModel.getInstance().getClients());

        // Setup columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Selection listener
        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillForm(newSelection);
            }
        });
    }

    private void fillForm(Client client) {
        txtNom.setText(client.getNom());
        txtPrenom.setText(client.getPrenom());
        txtTelephone.setText(client.getTelephone());
        txtEmail.setText(client.getEmail());
    }

    @FXML
    private void handleAdd() {
        if (isValidInput()) {
            Client client = new Client(
                    txtNom.getText(),
                    txtPrenom.getText(),
                    txtTelephone.getText(),
                    txtEmail.getText());
            DataModel.getInstance().addClient(client);
            handleClear();
        }
    }

    @FXML
    private void handleUpdate() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null && isValidInput()) {
            selected.setNom(txtNom.getText());
            selected.setPrenom(txtPrenom.getText());
            selected.setTelephone(txtTelephone.getText());
            selected.setEmail(txtEmail.getText());

            DataModel.getInstance().updateClient(selected);
            handleClear();
            tableClients.refresh();
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client à modifier.");
        }
    }

    @FXML
    private void handleDelete() {
        Client selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le client ?");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer ce client ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DataModel.getInstance().deleteClient(selected);
                handleClear();
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client à supprimer.");
        }
    }

    @FXML
    private void handleClear() {
        txtNom.clear();
        txtPrenom.clear();
        txtTelephone.clear();
        txtEmail.clear();
        tableClients.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText();
        DataModel.getInstance().searchClients(keyword);
    }

    private boolean isValidInput() {
        String errorMessage = "";

        if (txtNom.getText() == null || txtNom.getText().isEmpty()) {
            errorMessage += "Nom invalide!\n";
        }
        if (txtPrenom.getText() == null || txtPrenom.getText().isEmpty()) {
            errorMessage += "Prénom invalide!\n";
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
