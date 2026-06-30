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
    @FXML private ComboBox<TipoDocumento> cbxFiltro;
    @FXML private TextField txtRepLegal;
    @FXML private TextField txtDireccion;
    @FXML private Label lbnMsg;
    @FXML private TableView<Cliente> tableView;
    @FXML private AnchorPane miContenedor;

    private List<Cliente> todosLosClientes = new java.util.ArrayList<>();

    private Cliente clienteEdit;
    TableViewHelper<Cliente> tableViewHelper;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @FXML
    public void initialize() {
        cbxTipoDocumento.getItems().addAll(TipoDocumento.values());
        cbxFiltro.getItems().addAll(TipoDocumento.values());

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
        todosLosClientes = clienteService.findAll();
        ObservableList<Cliente> list = FXCollections.observableArrayList(todosLosClientes);
        tableView.setItems(list);
    }

    @FXML
    public void filtrarTabla(ActionEvent event) {
        TipoDocumento filtro = cbxFiltro.getValue();
        if (filtro == null) {
            lbnMsg.setText("Seleccione DNI o RUC para filtrar");
            lbnMsg.setStyle("-fx-text-fill: red;");
            return;
        }
        List<Cliente> filtrados = todosLosClientes.stream()
                .filter(c -> filtro.equals(c.getTipoDocumento()))
                .collect(java.util.stream.Collectors.toList());
        tableView.setItems(FXCollections.observableArrayList(filtrados));
        lbnMsg.setText("Mostrando clientes con tipo: " + filtro.name());
        lbnMsg.setStyle("-fx-text-fill: green;");
    }

    @FXML
    public void mostrarTodos(ActionEvent event) {
        cbxFiltro.getSelectionModel().clearSelection();
        tableView.setItems(FXCollections.observableArrayList(todosLosClientes));
        lbnMsg.setText(" ");
    }

    @FXML
    public void validarFormulario(ActionEvent event) {
        if (txtDniRuc.getText().isEmpty() || txtNombres.getText().isEmpty()) {
            lbnMsg.setText("Ingrese DNI/RUC y nombres");
            lbnMsg.setStyle("-fx-text-fill: red;");
            return;
        }
        if (cbxTipoDocumento.getValue() == null) {
            lbnMsg.setText("Seleccione el Tipo de Documento");
            lbnMsg.setStyle("-fx-text-fill: red;");
            return;
        }

        Cliente c = new Cliente();
        c.setDniruc(txtDniRuc.getText());
        c.setNombres(txtNombres.getText());
        c.setTipoDocumento(cbxTipoDocumento.getValue());
        c.setRepLegal(txtRepLegal.getText());
        c.setDireccion(txtDireccion.getText());

        try {
            boolean existe = clienteEdit != null
                    || clienteService.findAll().stream()
                            .anyMatch(cl -> cl.getDniruc().equals(c.getDniruc()));

            if (existe) {
                clienteService.update(c.getDniruc(), c);
                lbnMsg.setText("Cliente actualizado correctamente");
            } else {
                clienteService.save(c);
                lbnMsg.setText("Cliente registrado correctamente");
            }
            lbnMsg.setStyle("-fx-text-fill: green;");
            clienteEdit = null;
            clearForm();
            listar();
        } catch (Exception e) {
            lbnMsg.setText("Error al guardar: " + e.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void cancelar(ActionEvent event) {
        clearForm();
        clienteEdit = null;
        lbnMsg.setText(" ");
    }

    @FXML
    public void buscarDni(ActionEvent event) {
        TipoDocumento tipo = cbxTipoDocumento.getValue();

        // Solo se puede buscar automáticamente si el tipo es DNI
        if (tipo == null) {
            lbnMsg.setText("Seleccione primero el Tipo de Documento");
            lbnMsg.setStyle("-fx-text-fill: red;");
            return;
        }

        if (tipo != TipoDocumento.DNI) {
            // Para RUC, CE, PASAPORTE: no se usa API, el usuario ingresa los datos manualmente
            lbnMsg.setText("Para tipo " + tipo.name() + ", ingrese los datos manualmente y presione Guardar");
            lbnMsg.setStyle("-fx-text-fill: #1a6fc4;");
            txtNombres.requestFocus();
            return;
        }

        // Solo para DNI: consultar API
        String dni = txtDniRuc.getText();
        if (dni != null && !dni.trim().isEmpty()) {
            pe.edu.upeu.sysventas.utils.ConsultaDNI consulta = new pe.edu.upeu.sysventas.utils.ConsultaDNI();
            pe.edu.upeu.sysventas.dto.PersonaDto persona = consulta.consultarDNI(dni);
            if (persona != null) {
                txtNombres.setText(persona.getNombre() + " " + persona.getApellidoPaterno() + " " + persona.getApellidoMaterno());
                lbnMsg.setText("DNI encontrado y llenado correctamente");
                lbnMsg.setStyle("-fx-text-fill: green;");
            } else {
                lbnMsg.setText("DNI no encontrado. Puede ingresar el nombre manualmente");
                lbnMsg.setStyle("-fx-text-fill: orange;");
                txtNombres.requestFocus();
            }
        } else {
            lbnMsg.setText("Ingrese el número de DNI para buscar");
            lbnMsg.setStyle("-fx-text-fill: red;");
        }
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
