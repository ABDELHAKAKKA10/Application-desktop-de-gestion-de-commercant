package com.example.commercant_gestion.dao;

import com.example.commercant_gestion.model.Client;
import com.example.commercant_gestion.model.Product;
import com.example.commercant_gestion.model.Sale;
import com.example.commercant_gestion.model.SaleLine;
import com.example.commercant_gestion.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public void createSale(Sale sale) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtSale = null;
        PreparedStatement pstmtLine = null;
        PreparedStatement pstmtUpdateStock = null;

        String sqlSale = "INSERT INTO vente (date_vente, montant_total, client_id) VALUES (?, ?, ?)";
        String sqlLine = "INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE produit SET quantite_stock = quantite_stock - ? WHERE id = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert Sale
            pstmtSale = conn.prepareStatement(sqlSale, Statement.RETURN_GENERATED_KEYS);
            pstmtSale.setTimestamp(1, sale.getDateVente());
            pstmtSale.setDouble(2, sale.getMontantTotal());
            if (sale.getClient() != null) {
                pstmtSale.setInt(3, sale.getClient().getId());
            } else {
                pstmtSale.setNull(3, Types.INTEGER);
            }
            pstmtSale.executeUpdate();

            ResultSet rsKeys = pstmtSale.getGeneratedKeys();
            int saleId = -1;
            if (rsKeys.next()) {
                saleId = rsKeys.getInt(1);
                sale.setId(saleId);
            }

            // 2. Insert Lines and Update Stock
            pstmtLine = conn.prepareStatement(sqlLine);
            pstmtUpdateStock = conn.prepareStatement(sqlUpdateStock);

            for (SaleLine line : sale.getLines()) {
                // Check stock (simplified logic here, ideally check before transaction or here
                // and rollback)
                // For now assuming controller checks or we let DB constraints/logic handle it
                // (though DB constraint for unsigned int will fail if negative)

                // Insert line
                pstmtLine.setInt(1, saleId);
                pstmtLine.setInt(2, line.getProduct().getId());
                pstmtLine.setInt(3, line.getQuantite());
                pstmtLine.setDouble(4, line.getPrixUnitaire());
                pstmtLine.addBatch();

                // Update stock
                pstmtUpdateStock.setInt(1, line.getQuantite());
                pstmtUpdateStock.setInt(2, line.getProduct().getId());
                pstmtUpdateStock.addBatch();
            }

            pstmtLine.executeBatch();
            pstmtUpdateStock.executeBatch();

            conn.commit(); // Commit transaction

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e; // Re-throw to inform controller
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT v.id, v.date_vente, v.montant_total, " +
                "c.id as client_id, c.nom, c.prenom, c.telephone, c.email " +
                "FROM vente v " +
                "LEFT JOIN client c ON v.client_id = c.id " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client client = null;
                int clientId = rs.getInt("client_id");
                if (!rs.wasNull()) {
                    client = new Client(
                            clientId,
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("telephone"),
                            rs.getString("email"));
                }

                Sale sale = new Sale(
                        rs.getInt("id"),
                        rs.getTimestamp("date_vente"),
                        rs.getDouble("montant_total"),
                        client);
                sales.add(sale);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    public List<SaleLine> getSaleLines(int saleId) {
        List<SaleLine> lines = new ArrayList<>();
        String sql = "SELECT lv.id, lv.quantite, lv.prix_unitaire, p.id as p_id, p.nom, p.categorie, p.quantite_stock, p.prix_unitaire as p_current_price "
                +
                "FROM ligne_vente lv " +
                "JOIN produit p ON lv.produit_id = p.id " +
                "WHERE lv.vente_id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("p_id"),
                        rs.getString("nom"),
                        rs.getString("categorie"),
                        rs.getDouble("p_current_price"),
                        rs.getInt("quantite_stock"));
                SaleLine line = new SaleLine(
                        rs.getInt("id"),
                        saleId,
                        product,
                        rs.getInt("quantite"),
                        rs.getDouble("prix_unitaire"));
                lines.add(line);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public void updateSale(Sale sale) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmtUpdateSale = null;
        PreparedStatement pstmtDeleteLines = null;
        PreparedStatement pstmtInsertLine = null;
        PreparedStatement pstmtRestoreStock = null;
        PreparedStatement pstmtDeductStock = null;

        String sqlUpdateSale = "UPDATE vente SET montant_total = ?, client_id = ? WHERE id = ?";
        String sqlDeleteLines = "DELETE FROM ligne_vente WHERE vente_id = ?";
        String sqlInsertLine = "INSERT INTO ligne_vente (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
        String sqlRestoreStock = "UPDATE produit SET quantite_stock = quantite_stock + ? WHERE id = ?";
        String sqlDeductStock = "UPDATE produit SET quantite_stock = quantite_stock - ? WHERE id = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Get old lines to restore stock
            List<SaleLine> oldLines = getSaleLines(sale.getId());
            pstmtRestoreStock = conn.prepareStatement(sqlRestoreStock);
            for (SaleLine line : oldLines) {
                pstmtRestoreStock.setInt(1, line.getQuantite());
                pstmtRestoreStock.setInt(2, line.getProduct().getId());
                pstmtRestoreStock.addBatch();
            }
            pstmtRestoreStock.executeBatch();

            // 2. Delete old lines
            pstmtDeleteLines = conn.prepareStatement(sqlDeleteLines);
            pstmtDeleteLines.setInt(1, sale.getId());
            pstmtDeleteLines.executeUpdate();

            // 3. Update Sale Header
            pstmtUpdateSale = conn.prepareStatement(sqlUpdateSale);
            pstmtUpdateSale.setDouble(1, sale.getMontantTotal());
            if (sale.getClient() != null) {
                pstmtUpdateSale.setInt(2, sale.getClient().getId());
            } else {
                pstmtUpdateSale.setNull(2, Types.INTEGER);
            }
            pstmtUpdateSale.setInt(3, sale.getId());
            pstmtUpdateSale.executeUpdate();

            // 4. Insert New Lines and Deduct Stock
            pstmtInsertLine = conn.prepareStatement(sqlInsertLine);
            pstmtDeductStock = conn.prepareStatement(sqlDeductStock);

            for (SaleLine line : sale.getLines()) {
                // Insert
                pstmtInsertLine.setInt(1, sale.getId());
                pstmtInsertLine.setInt(2, line.getProduct().getId());
                pstmtInsertLine.setInt(3, line.getQuantite());
                pstmtInsertLine.setDouble(4, line.getPrixUnitaire());
                pstmtInsertLine.addBatch();

                // Deduct
                pstmtDeductStock.setInt(1, line.getQuantite());
                pstmtDeductStock.setInt(2, line.getProduct().getId());
                pstmtDeductStock.addBatch();
            }
            pstmtInsertLine.executeBatch();
            pstmtDeductStock.executeBatch();

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
