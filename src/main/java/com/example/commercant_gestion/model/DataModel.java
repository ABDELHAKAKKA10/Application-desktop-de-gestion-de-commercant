package com.example.commercant_gestion.model;

import com.example.commercant_gestion.dao.ClientDAO;
import com.example.commercant_gestion.dao.ProductDAO;
import com.example.commercant_gestion.dao.SaleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class DataModel {
    private static DataModel instance;

    private ObservableList<Product> products;
    private ObservableList<Client> clients;
    private ObservableList<Sale> sales;

    private ProductDAO productDAO;
    private ClientDAO clientDAO;
    private SaleDAO saleDAO;

    private DataModel() {
        productDAO = new ProductDAO();
        clientDAO = new ClientDAO();
        saleDAO = new SaleDAO();

        products = FXCollections.observableArrayList();
        clients = FXCollections.observableArrayList();
        sales = FXCollections.observableArrayList();

        loadData();
    }

    public static DataModel getInstance() {
        if (instance == null) {
            instance = new DataModel();
        }
        return instance;
    }

    public void loadData() {
        products.setAll(productDAO.getAllProducts());
        clients.setAll(clientDAO.getAllClients());
        sales.setAll(saleDAO.getAllSales());
    }

    // --- Products ---
    public ObservableList<Product> getProducts() {
        return products;
    }

    public void addProduct(Product product) {
        productDAO.addProduct(product);
        products.setAll(productDAO.getAllProducts()); // Reload to get ID and ensure sync
    }

    public void updateProduct(Product product) {
        productDAO.updateProduct(product);
        // Trigger list update (clunky but ensures UI refresh)
        int index = products.indexOf(product);
        if (index >= 0) {
            products.set(index, product);
        }
    }

    public void deleteProduct(Product product) {
        productDAO.deleteProduct(product.getId());
        products.remove(product);
    }

    public void searchProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            products.setAll(productDAO.getAllProducts());
        } else {
            products.setAll(productDAO.searchProducts(keyword));
        }
    }

    // --- Clients ---
    public ObservableList<Client> getClients() {
        return clients;
    }

    public void addClient(Client client) {
        clientDAO.addClient(client);
        clients.setAll(clientDAO.getAllClients());
    }

    public void updateClient(Client client) {
        clientDAO.updateClient(client);
        int index = clients.indexOf(client);
        if (index >= 0) {
            clients.set(index, client);
        }
    }

    public void deleteClient(Client client) {
        clientDAO.deleteClient(client.getId());
        clients.remove(client);
    }

    public void searchClients(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            clients.setAll(clientDAO.getAllClients());
        } else {
            clients.setAll(clientDAO.searchClients(keyword));
        }
    }

    // --- Sales ---
    public ObservableList<Sale> getSales() {
        return sales;
    }

    public void createSale(Sale sale) throws SQLException {
        saleDAO.createSale(sale);
        sales.add(0, sale); // Add to top
        // Refresh products to update stocks
        products.setAll(productDAO.getAllProducts());
    }

    public void refreshSales() {
        sales.setAll(saleDAO.getAllSales());
    }

    public java.util.List<SaleLine> getSaleLines(int saleId) {
        return saleDAO.getSaleLines(saleId);
    }

    public void updateSale(Sale sale) throws SQLException {
        saleDAO.updateSale(sale);
        refreshSales();
        products.setAll(productDAO.getAllProducts()); // Sync stock
    }

    // --- Navigation / Coordination ---
    // Simple callback mechanism for editing
    private java.util.function.Consumer<Sale> onEditSaleRequest;

    public void setOnEditSaleRequest(java.util.function.Consumer<Sale> callback) {
        this.onEditSaleRequest = callback;
    }

    public void requestEditSale(Sale sale) {
        if (onEditSaleRequest != null) {
            onEditSaleRequest.accept(sale);
        }
    }
}
