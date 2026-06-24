package pe.edu.upeu.sysventas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.service.IVentaService;

import java.util.List;

public class ReporteController {

    private final IVentaService ventaService;

    @FXML private TableView<Venta> tableView;

    public ReporteController(IVentaService ventaService) {
        this.ventaService = ventaService;
    }

    @FXML
    public void initialize() {
        try {
            TableColumn<Venta, Long> colId = new TableColumn<>("ID Venta");
            colId.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
            
            TableColumn<Venta, String> colSerie = new TableColumn<>("Serie");
            colSerie.setCellValueFactory(new PropertyValueFactory<>("serie"));
            
            TableColumn<Venta, String> colTipoDoc = new TableColumn<>("Tipo Doc.");
            colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDoc"));
            
            TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
            colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaGener"));
            
            TableColumn<Venta, String> colCliente = new TableColumn<>("DNI/RUC Cliente");
            colCliente.setCellValueFactory(cellData -> {
                if (cellData.getValue().getDniruc() != null) {
                    return new SimpleStringProperty(cellData.getValue().getDniruc().getDniruc());
                } else {
                    return new SimpleStringProperty("");
                }
            });
            
            TableColumn<Venta, Double> colPB = new TableColumn<>("Precio Base");
            colPB.setCellValueFactory(new PropertyValueFactory<>("preciobase"));
            
            TableColumn<Venta, Double> colIGV = new TableColumn<>("IGV");
            colIGV.setCellValueFactory(new PropertyValueFactory<>("igv"));
            
            TableColumn<Venta, Double> colTotal = new TableColumn<>("Total");
            colTotal.setCellValueFactory(new PropertyValueFactory<>("preciototal"));
            
            tableView.getColumns().addAll(colId, colSerie, colTipoDoc, colFecha, colCliente, colPB, colIGV, colTotal);

            listar();
        } catch (Exception e) {
            System.out.println("Error initialize reporte: " + e.getMessage());
        }
    }

    public void listar() {
        List<Venta> ventas = ventaService.findAll();
        ObservableList<Venta> list = FXCollections.observableArrayList(ventas);
        tableView.setItems(list);
    }
}
