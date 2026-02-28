package com.example.commercant_gestion.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private Timestamp dateVente;
    private double montantTotal;
    private Client client;
    private List<SaleLine> lines;

    public Sale(int id, Timestamp dateVente, double montantTotal, Client client) {
        this.id = id;
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.client = client;
        this.lines = new ArrayList<>();
    }

    public Sale(Client client) {
        this.client = client;
        this.dateVente = new Timestamp(System.currentTimeMillis());
        this.montantTotal = 0.0;
        this.lines = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getDateVente() {
        return dateVente;
    }

    public void setDateVente(Timestamp dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<SaleLine> getLines() {
        return lines;
    }

    public void setLines(List<SaleLine> lines) {
        this.lines = lines;
        calculateTotal();
    }

    public void addLine(SaleLine line) {
        this.lines.add(line);
        calculateTotal();
    }

    private void calculateTotal() {
        this.montantTotal = 0;
        for (SaleLine line : lines) {
            this.montantTotal += line.getTotal();
        }
    }
}
