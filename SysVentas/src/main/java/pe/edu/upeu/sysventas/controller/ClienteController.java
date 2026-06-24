package pe.edu.upeu.sysventas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.service.IClienteService;

import java.util.LinkedHashMap;
import java.util.List;

public class ClienteController {

    private final IClienteService clienteService;

    @FXML private TextField txtDniRuc;
    @FXML private TextField txtNombres;
    @FXML private ComboBox<TipoDocumento> cbxTipoDocumento;
    @FXML private TextField txtRepLegal;
    @FXML private TextField txtDireccion;
    @FXML private Label lbnMsg;
    @FXML private TableView<Cliente> tableView;
    @FXML private AnchorPane miContenedor;

    private Cliente clienteEdit;
    TableViewHelper<Cliente> tableViewHelper;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        cbxTipoDocumento.getItems().addAll(TipoDocumento.values());
        
        tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("DNI/RUC", new ColumnInfo("dniruc", 100.0));
        columns.put("Nombres", new ColumnInfo("nombres", 200.0));
        columns.put("Tipo Doc.", new ColumnInfo("tipoDocumento", 100.0));
        columns.put("Rep. Legal", new ColumnInfo("repLegal", 150.0));
        columns.put("Dirección", new ColumnInfo("direccion", 200.0));
        
        try {
            tableViewHelper.addColumnsInOrderWithSize(tableView, columns, this::editCliente, this::deleteCliente);
            listar();
        } catch (Exception e) {
            System.out.println("Error initialize cliente: " + e.getMessage());
        }
    }

    public void listar() {
        List<Cliente> clientes = clienteService.findAll();
        ObservableList<Cliente> list = FXCollections.observableArrayList(clientes);
        tableView.setItems(list);
    }

    @FXML
    public void validarFormulario(ActionEvent event) {
        if (txtDniRuc.getText().isEmpty() || txtNombres.getText().isEmpty()) {
            lbnMsg.setText("Ingrese DNI/RUC y nombres");
            lbnMsg.setStyle("-fx-text-fill: red;");
            return;
        }
        
        Cliente c = new Cliente();
        c.setDniruc(txtDniRuc.getText());
        c.setNombres(txtNombres.getText());
        c.setTipoDocumento(cbxTipoDocumento.getValue());
        c.setRepLegal(txtRepLegal.getText());
        c.setDireccion(txtDireccion.getText());

        if (clienteEdit == null) {
            clienteService.save(c);
            lbnMsg.setText("Cliente registrado correctamente");
        } else {
            clienteService.update(c.getDniruc(), c);
            lbnMsg.setText("Cliente actualizado correctamente");
            clienteEdit = null;
        }
        lbnMsg.setStyle("-fx-text-fill: green;");
        clearForm();
        listar();
    }

    @FXML
    public void cancelar(ActionEvent event) {
        clearForm();
        clienteEdit = null;
        lbnMsg.setText(" ");
    }

    private void clearForm() {
        txtDniRuc.clear();
        txtDniRuc.setDisable(false);
        txtNombres.clear();
        cbxTipoDocumento.getSelectionModel().clearSelection();
        txtRepLegal.clear();
        txtDireccion.clear();
    }

    private void editCliente(Cliente c) {
        clienteEdit = c;
        txtDniRuc.setText(c.getDniruc());
        txtDniRuc.setDisable(true); // El DNI es PK, no se debe cambiar
        txtNombres.setText(c.getNombres());
        cbxTipoDocumento.setValue(c.getTipoDocumento());
        txtRepLegal.setText(c.getRepLegal());
        txtDireccion.setText(c.getDireccion());
    }

    private void deleteCliente(Cliente c) {
        clienteService.delete(c.getDniruc());
        listar();
    }
}
