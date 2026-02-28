package com.example.commercant_gestion.model;

public class SaleLine {
    private int id;
    private int venteId;
    private Product product;
    private int quantite;
    private double prixUnitaire; // Prix au moment de la vente

    public SaleLine(int id, int venteId, Product product, int quantite, double prixUnitaire) {
        this.id = id;
        this.venteId = venteId;
        this.product = product;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public SaleLine(Product product, int quantite, double prixUnitaire) {
        this.product = product;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVenteId() {
        return venteId;
    }

    public void setVenteId(int venteId) {
        this.venteId = venteId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public double getTotal() {
        return quantite * prixUnitaire;
    }
}
