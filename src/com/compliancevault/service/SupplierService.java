package com.compliancevault.service;

import com.compliancevault.dao.SupplierDAO;
import com.compliancevault.model.Supplier;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SupplierService {
    private final SupplierDAO supplierDAO;

    // tolerance threshold — lower means stricter matching
    private static final int FUZZY_THRESHOLD = 3;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
    }

    public void createSupplier(Supplier supplier) throws SQLException {
        supplierDAO.insert(supplier);
    }

    public Supplier getSupplier(int supplierId) throws SQLException {
        return supplierDAO.findById(supplierId);
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        return supplierDAO.findAll();
    }

    public void updateSupplier(Supplier supplier) throws SQLException {
        supplierDAO.update(supplier);
    }

    public void deleteSupplier(int supplierId) throws SQLException {
        supplierDAO.delete(supplierId);
    }


    //Fuzzy search across company name, phone, and service type.
    //Tolerates typos and partial matches... important for receptionists
    //looking up a contractor at the door, often working from a verbal name
    // or a half-remembered company

    //Returns matches ranked by relevance (closest match first).
    public List<Supplier> searchSuppliers(String query) throws SQLException {
        if (query == null || query.isBlank()) {
            return getAllSuppliers();
        }

        String normalisedQuery = query.toLowerCase().trim();
        List<SupplierMatch> matches = new ArrayList<>();

        for (Supplier supplier : supplierDAO.findAll()) {
            int score = calculateMatchScore(supplier, normalisedQuery);
            if (score >= 0) {
                matches.add(new SupplierMatch(supplier, score));
            }
        }

        // sort by score ascending — lower = better match
        matches.sort(Comparator.comparingInt(SupplierMatch::score));

        List<Supplier> results = new ArrayList<>();
        for (SupplierMatch match : matches) {
            results.add(match.supplier());
        }
        return results;
    }


    //Returns -1 if no match. Otherwise returns a score where lower = better.
    //Direct substring matches score 0. Fuzzy matches score by edit distance
    private int calculateMatchScore(Supplier supplier, String query) {
        String[] fields = {
                safe(supplier.getCompanyName()),
                safe(supplier.getPhone()),
                safe(supplier.getServiceType())
        };

        int bestScore = Integer.MAX_VALUE;

        for (String field : fields) {
            // exact substring — best possible match
            if (field.contains(query)) {
                return 0;
            }
            // otherwise check edit distance against each word in the field
            for (String word : field.split("\\s+")) {
                int distance = levenshtein(word, query);
                if (distance <= FUZZY_THRESHOLD && distance < bestScore) {
                    bestScore = distance;
                }
            }
        }

        return bestScore == Integer.MAX_VALUE ? -1 : bestScore;
    }


    //Standard Levenshtein distance — counts insertions, deletions, and
    //substitutions needed to transform one string into another.
    //"meathco" vs "meatco" = 1 (one deletion)
    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,        // deletion
                                dp[i][j - 1] + 1),       // insertion
                        dp[i - 1][j - 1] + cost  // substitution
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    private String safe(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private record SupplierMatch(Supplier supplier, int score) {}

    //    second constructor that accepts the DAO for test
    public SupplierService(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }
}
