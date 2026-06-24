package pe.edu.upeu.sysventas.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.service.IClienteService;
import pe.edu.upeu.sysventas.service.IVentaService;
import pe.edu.upeu.sysventas.service.ProductoIService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Label lblTotalVentas;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalProductos;
    @FXML private BarChart<String, Number> barChartVentas;
    @FXML private PieChart pieChartStock;

    private final IClienteService clienteService;
    private final IVentaService ventaService;
    private final ProductoIService productoService;

    public DashboardController(IClienteService clienteService, IVentaService ventaService, ProductoIService productoService) {
        this.clienteService = clienteService;
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    @FXML
    public void initialize() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        try {
            // Cargar clientes
            List<Cliente> clientes = clienteService.findAll();
            lblTotalClientes.setText(String.valueOf(clientes.size()));

            // Cargar ventas
            List<Venta> ventas = ventaService.findAll();
            double totalVentas = 0;
            for(Venta v : ventas) {
                totalVentas += v.getPreciototal();
            }
            lblTotalVentas.setText(String.format("S/ %.2f", totalVentas));

            // Llenar BarChart de Ventas
            XYChart.Series<String, Number> seriesVentas = new XYChart.Series<>();
            seriesVentas.setName("Monto de Venta");
            int i = 1;
            for(Venta v : ventas) {
                seriesVentas.getData().add(new XYChart.Data<>("V-" + i++, v.getPreciototal()));
                if(i > 10) break; // solo mostrar ultimas 10
            }
            barChartVentas.getData().add(seriesVentas);

            // Cargar productos
            List<Producto> productos = productoService.findAll();
            double totalStock = 0;
            for(Producto p : productos) {
                totalStock += p.getStock();
            }
            lblTotalProductos.setText(String.valueOf((int)totalStock));

            // Llenar PieChart de Stock por Producto
            Map<String, Double> stockPorProducto = productos.stream()
                    .collect(Collectors.groupingBy(Producto::getNombre, Collectors.summingDouble(Producto::getStock)));

            for (Map.Entry<String, Double> entry : stockPorProducto.entrySet()) {
                if(entry.getValue() > 0) {
                    pieChartStock.getData().add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
