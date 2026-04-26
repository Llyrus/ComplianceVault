package com.compliancevault.controller;

import com.compliancevault.ComplianceVaultApp;
import com.compliancevault.model.Supplier;
import com.compliancevault.model.User;
import com.compliancevault.service.AuthService;
import com.compliancevault.service.ComplianceService;
import com.compliancevault.service.SupplierService;
import com.compliancevault.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    // Header
    @FXML private Label userLabel;

    // Summary cards
    @FXML private Label totalCount;
    @FXML private Label compliantCount;
    @FXML private Label nonCompliantCount;

    // Search & table
    @FXML private TextField searchField;
    @FXML private TableView<SupplierRow> supplierTable;
    @FXML private TableColumn<SupplierRow, String> companyColumn;
    @FXML private TableColumn<SupplierRow, String> phoneColumn;
    @FXML private TableColumn<SupplierRow, String> serviceColumn;
    @FXML private TableColumn<SupplierRow, String> statusColumn;

    private final AuthService authService = ComplianceVaultApp.getAuthService();
    private final SupplierService supplierService = new SupplierService();
    private final ComplianceService complianceService = new ComplianceService();

    /**
     called automatically by FXMLLoader after @FXML fields are injected.
     it's the right place for setup that depends on the FXML being fully loaded
     */
    @FXML
    public void initialize() {
        setupHeader();
        setupTableColumns();
        setupSearchListener();
        loadSuppliers();
    }

    private void setupHeader() {
        User user = authService.getCurrentUser();
        if (user != null) {
            userLabel.setText("Signed in as " + user.getUsername() + " (" + user.getRole() + ")");
        }
    }

    /**
     Wire each column to a property on SupplierRow.
     PropertyValueFactory uses reflection to find getCompanyName(), getPhone(), etc.
     */
    // !!Post-uni note!! PropertyValueFactory uses reflection, which is slower and
    // not refactor-safe (renaming the property breaks the table silently).
    // The modern approach is `setCellValueFactory(cellData -> cellData.getValue().companyNameProperty())`
    // with explicit JavaFX properties. Fine for uni; need to refactor for production

    private void setupTableColumns() {
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusText"));

        // Colour the status cell based on compliance state
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Compliant")) {
                        setStyle("-fx-text-fill: #0f6e56; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #922b21; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Double-click a row to open supplier detail (next session — for now just print)
        supplierTable.setRowFactory(tv -> {
            TableRow<SupplierRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SupplierRow clicked = row.getItem();
                    System.out.println("TODO: open supplier detail for " + clicked.getCompanyName()
                            + " (id=" + clicked.getSupplierId() + ")");
                }
            });
            return row;
        });
    }

    private void setupSearchListener() {
        // filter the table whenever the search field changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                List<Supplier> matches = newVal == null || newVal.isBlank()
                        ? supplierService.getAllSuppliers()
                        : supplierService.searchSuppliers(newVal);
                populateTable(matches);
            } catch (SQLException e) {
                e.printStackTrace();
                // !!Post-uni note!! surface this error to the user via a status bar.
                // Currently silently fails the search — bad UX... fine for assignment.
            }
        });
    }

    private void loadSuppliers() {
        try {
            List<Supplier> suppliers = supplierService.getAllSuppliers();
            populateTable(suppliers);
            updateSummaryCards(suppliers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateTable(List<Supplier> suppliers) throws SQLException {
        ObservableList<SupplierRow> rows = FXCollections.observableArrayList();
        for (Supplier s : suppliers) {
            boolean compliant = complianceService.isCompliant(s.getSupplierId());
            rows.add(new SupplierRow(s, compliant));
        }
        supplierTable.setItems(rows);
    }

    private void updateSummaryCards(List<Supplier> suppliers) throws SQLException {
        int total = suppliers.size();
        int compliant = 0;
        for (Supplier s : suppliers) {
            if (complianceService.isCompliant(s.getSupplierId())) {
                compliant++;
            }
        }
        totalCount.setText(String.valueOf(total));
        compliantCount.setText(String.valueOf(compliant));
        nonCompliantCount.setText(String.valueOf(total - compliant));
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        try {
            SceneManager.switchScene(userLabel,
                    "/com/compliancevault/view/login.fxml",
                    "ComplianceVault", 400, 360);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     View model, bundles a Supplier with its computed compliance status.
     Lives here because it's only used by this controller
     */
    //Post-uni note: if more screens need supplier+status together,
    //promote this to a shared viewmodel package
    public static class SupplierRow {
        private final Supplier supplier;
        private final boolean compliant;

        public SupplierRow(Supplier supplier, boolean compliant) {
            this.supplier = supplier;
            this.compliant = compliant;
        }

        public int getSupplierId() { return supplier.getSupplierId(); }
        public String getCompanyName() { return supplier.getCompanyName(); }
        public String getPhone() { return supplier.getPhone(); }
        public String getServiceType() { return supplier.getServiceType(); }
        public String getStatusText() { return compliant ? "Compliant" : "Non-compliant"; }
        public boolean isCompliant() { return compliant; }
    }
}