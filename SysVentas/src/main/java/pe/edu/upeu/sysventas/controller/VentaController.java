package pe.edu.upeu.sysventas.controller;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import pe.edu.upeu.sysventas.components.*;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.dto.PersonaDto;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.VentCarrito;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.PrinterManager;

import javax.print.PrintService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;


public class VentaController {
    @FXML
    TextField autocompProducto;
    @FXML
    ComboBox<String> cbTalla;
    @FXML
    ComboBox<String> cbColor;
    @FXML
    TextField nombreProducto, codigoPro, stockPro, cantidadPro, punitPro, preTPro, txtBaseImp, txtIgv, txtDescuento, txtImporteT;
    @FXML
    Button btnRegVenta, btnRegCarrito, btnFormCliente, btnRegCliente;
    @FXML
    TextField autocompCliente, txtDireccion;
    @FXML
    TextField razonSocial;
    @FXML
    TextField dniRuc;
    @FXML
    TableView<VentCarrito> tableView;
    AutoCompleteTextField actf;
    ModeloDataAutocomplet lastProducto;
    AutoCompleteTextField actfC;
    ModeloDataAutocomplet lastCliente;

    ProductoIService ps;
    IClienteService cs;
    IVentaCarritoService daoC;
    IUsuarioService daoU;
    IVentaService daoV;
    IVentaDetalleService daoVD;
    Stage stage;
    @FXML
    private AnchorPane miContenedor;
    //private JasperPrint jasperPrint;
    private final SortedSet<ModeloDataAutocomplet> entries = new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) -> o1.toString().compareTo(o2.toString()));
    private final SortedSet<ModeloDataAutocomplet> entriesC = new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) -> o1.toString().compareTo(o2.toString()));
    private List<Producto> allProductosVenta = new ArrayList<>();

    ConsultaDNI cDni;


    public VentaController(ProductoIService ps, IClienteService cs, IVentaCarritoService daoC, IUsuarioService daoU, IVentaService daoV, IVentaDetalleService daoVD, ConsultaDNI cDni) {
        this.ps = ps;
        this.cs = cs;
        this.daoC = daoC;
        this.daoU = daoU;
        this.daoV = daoV;
        this.daoVD = daoVD;
        this.cDni = cDni;
    }

    @FXML
    public void initialize(){

        Platform.runLater(() -> {
            stage = (Stage) miContenedor.getScene().getWindow();
            System.out.println("El título del stage es: " + stage.getTitle());
        });

        listarCliente();
        autoCompletarCliente();

        listarProducto();
        actf = new AutoCompleteTextField<>(entries, autocompProducto);
        Runnable onProductoSelected = () -> {
            lastProducto = (ModeloDataAutocomplet) actf.getLastSelectedObject();
            if(lastProducto != null) {
                nombreProducto.setText(lastProducto.getNameDysplay());
                
                cbTalla.getItems().clear();
                cbColor.getItems().clear();
                cbTalla.setDisable(true);
                cbColor.setDisable(true);
                codigoPro.clear();
                stockPro.clear();
                punitPro.clear();
                cantidadPro.clear();
                preTPro.clear();
                btnRegCarrito.setDisable(true);

                Set<String> tallasDisponibles = new HashSet<>();
                for(Producto p : allProductosVenta) {
                    if(p.getNombre().equalsIgnoreCase(lastProducto.getNameDysplay()) && p.getStock() > 0) {
                        tallasDisponibles.add(p.getTalla());
                    }
                }
                cbTalla.setItems(FXCollections.observableArrayList(tallasDisponibles));
                if(!tallasDisponibles.isEmpty()) {
                    cbTalla.setDisable(false);
                }
            }
        };
        actf.setOnItemSelected(onProductoSelected);

        cbTalla.setOnAction(e -> {
            String selectedTalla = cbTalla.getSelectionModel().getSelectedItem();
            if (selectedTalla != null) {
                String selectedName = nombreProducto.getText();
                
                cbColor.getItems().clear();
                cbColor.setDisable(true);
                codigoPro.clear();
                stockPro.clear();
                punitPro.clear();
                cantidadPro.clear();
                preTPro.clear();
                btnRegCarrito.setDisable(true);

                Set<String> coloresDisponibles = new HashSet<>();
                for(Producto p : allProductosVenta) {
                    if(p.getNombre().equalsIgnoreCase(selectedName) && p.getTalla().equals(selectedTalla) && p.getStock() > 0) {
                        coloresDisponibles.add(p.getColor());
                    }
                }
                cbColor.setItems(FXCollections.observableArrayList(coloresDisponibles));
                if(!coloresDisponibles.isEmpty()) {
                    cbColor.setDisable(false);
                }
            }
        });

        cbColor.setOnAction(e -> {
            String selectedColor = cbColor.getSelectionModel().getSelectedItem();
            if (selectedColor != null) {
                String selectedName = nombreProducto.getText();
                String selectedTalla = cbTalla.getSelectionModel().getSelectedItem();
                
                for(Producto p : allProductosVenta) {
                    if(p.getNombre().equalsIgnoreCase(selectedName) && p.getTalla().equals(selectedTalla) && p.getColor().equals(selectedColor)) {
                        codigoPro.setText(String.valueOf(p.getIdProducto()));
                        punitPro.setText(String.valueOf(p.getPu()));
                        stockPro.setText(String.valueOf(p.getStock()));
                        cantidadPro.setText("1");
                        calcularPT();
                        cantidadPro.requestFocus();
                        break;
                    }
                }
            }
        });

        autocompProducto.setOnKeyReleased(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onProductoSelected.run();
            } else if (autocompProducto.getText().isEmpty()) {
                nombreProducto.clear();
                cbTalla.getItems().clear();
                cbColor.getItems().clear();
                cbTalla.setDisable(true);
                cbColor.setDisable(true);
                codigoPro.clear();
                punitPro.clear();
                stockPro.clear();
                cantidadPro.clear();
                preTPro.clear();
                btnRegCarrito.setDisable(true);
            }
        });

        cantidadPro.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (!btnRegCarrito.isDisabled()) {
                    registarPCarrito();
                }
            }
        });


        personalizarTabla();
        btnRegCarrito.setDisable(true);
        btnRegCliente.setDisable(true);
    }

    public void listarProducto(){
        entries.clear();
        allProductosVenta = ps.findAll();
        Set<String> nombresUnicos = new HashSet<>();
        for(Producto p : allProductosVenta) {
            if(p.getStock() > 0 && p.getNombre() != null) {
                nombresUnicos.add(p.getNombre());
            }
        }
        for(String nombre : nombresUnicos) {
            ModeloDataAutocomplet data = new ModeloDataAutocomplet();
            data.setIdx(nombre);
            data.setNameDysplay(nombre);
            data.setOtherData("");
            entries.add(data);
        }
    }
    public void listarCliente(){
        entriesC.clear();
        entriesC.addAll(cs.listAutoCompletCliente());
    }

    public void autoCompletarCliente(){ 
        actfC=new AutoCompleteTextField<>(entriesC, autocompCliente);
        Runnable onClienteSelected = () -> {
            lastCliente=(ModeloDataAutocomplet) actfC.getLastSelectedObject();
            if(lastCliente!=null){
                razonSocial.setText(lastCliente.getNameDysplay());
                dniRuc.setText(lastCliente.getIdx());
                String[] tempVal=lastCliente.getOtherData().split(":");
                txtDireccion.setText(tempVal[1]);
                listar();
            }
        };
        actfC.setOnItemSelected(onClienteSelected);

        autocompCliente.setOnKeyReleased(e->{
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                onClienteSelected.run();
            } else if (autocompCliente.getText().isEmpty()) {
                limpiarFormCliente();
                btnRegCliente.setDisable(true);
            }
        });
    }




    @FXML
    public void buscarClienteCdni(){
        limpiarFormCliente();
        Stage stage = StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        if(autocompCliente.getText().length()==8 || autocompCliente.getText().length()==11){
            try {
                if(cs.findById(autocompCliente.getText())!=null){
                    btnRegCliente.setDisable(true);
                    Toast.showToast(stage, "El cliente si existe", 2000, with, 50);
                    return;}
                consultarDNIReniec(with);
            } catch (ModelNotFoundException e) {
                btnRegCliente.setDisable(true);
                Toast.showToast(stage, "El cliente no existe", 2000, with, 50);
                consultarDNIReniec(with);
            }
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El valor buscado debe tener 8 o 11 digitos", 2000, with, 50);
        }
    }



    public void consultarDNIReniec(double with){
        PersonaDto p=cDni.consultarDNI(autocompCliente.getText());
        if(p!=null){
            razonSocial.setText(p.getNombre()+" "+p.getApellidoPaterno()+" "+p.getApellidoMaterno());
            dniRuc.setText(p.getDni());
            btnRegCliente.setDisable(false);
            Toast.showToast(stage, "El cliente se encontró en RENIEC para registrar debe hacer clik en Add", 2000, with, 50);
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El cliente no se encuentra en RENIEC y debe registrar a través del formulario de cliente", 2000, with, 50);
        }
    }

    public void limpiarFormCliente(){
        razonSocial.clear();
        dniRuc.clear();
        txtDireccion.clear();
    }


    @FXML
    public void guardarCliente(){
        Stage stage = StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        try {
            Cliente c= Cliente.builder().dniruc(dniRuc.getText())
                    .nombres(razonSocial.getText())
                    .repLegal(razonSocial.getText())
                    .tipoDocumento(TipoDocumento.DNI)
                    .build();
            cs.save(c);
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El cliente se guardo satisfactoriamente!", 2000, with, 50);
            listarCliente();
            listar();
        }catch (Exception e){
            Toast.showToast(stage, "Error al guardar cliente!", 2000, with, 50);
        }
    }

    public void personalizarTabla(){
        TableViewHelper<VentCarrito> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Prod", new ColumnInfo("idProducto.idProducto", 60.0)); 
        columns.put("Nombre Producto", new ColumnInfo("nombreProducto", 200.0));
        columns.put("Talla", new ColumnInfo("idProducto.talla", 60.0)); 
        columns.put("Color", new ColumnInfo("idProducto.color", 70.0)); 
        columns.put("Cantidad", new ColumnInfo("cantidad", 60.0)); // Columna visible "Columna 2" mapea al campo "campo2"
        columns.put("P.Unitario", new ColumnInfo("punitario", 100.0)); // Columna visible "Columna 2" mapea al campo "campo2"
        columns.put("P.Total", new ColumnInfo("ptotal", 100.0)); // Columna visible "Columna 2" mapea al campo "campo2"

        Consumer<VentCarrito> updateAction = (VentCarrito ventCarrito) -> { editVenCarrito(ventCarrito); };
        Consumer<VentCarrito> deleteAction = (VentCarrito ventCarrito) -> {deleteReg(ventCarrito); };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns,updateAction, deleteAction );

        tableView.setTableMenuButtonVisible(true);
    }

    public void listar(){
        tableView.getItems().clear();
        List<VentCarrito> lista=daoC.listaCarritoCliente(dniRuc.getText());
        double impoTotal = 0;
        for (VentCarrito dato: lista){
            impoTotal += Double.parseDouble(String.valueOf(dato.getPtotal()));
        }
        txtImporteT.setText(String.valueOf(impoTotal));
        double pv = impoTotal / 1.18;
        txtBaseImp.setText(String.valueOf(Math.round(pv * 100.0) / 100.0));
        txtIgv.setText(String.valueOf(Math.round((pv * 0.18) * 100.0) / 100.0));
        tableView.getItems().addAll(lista);
    }

    public void editVenCarrito(VentCarrito obj) {
        // Mover el item del carrito de vuelta al formulario
        Producto p = obj.getIdProducto();
        if(p != null && p.getNombre() != null) {
            autocompProducto.setText(p.getNombre());
            nombreProducto.setText(p.getNombre());
            
            // Llenar tallas
            Set<String> tallasDisponibles = new HashSet<>();
            for(Producto prod : allProductosVenta) {
                if(prod.getNombre().equalsIgnoreCase(p.getNombre()) && prod.getStock() > 0) {
                    tallasDisponibles.add(prod.getTalla());
                }
            }
            cbTalla.setItems(FXCollections.observableArrayList(tallasDisponibles));
            cbTalla.setDisable(false);
            cbTalla.getSelectionModel().select(p.getTalla());

            // Llenar colores
            Set<String> coloresDisponibles = new HashSet<>();
            for(Producto prod : allProductosVenta) {
                if(prod.getNombre().equalsIgnoreCase(p.getNombre()) && prod.getTalla().equals(p.getTalla()) && prod.getStock() > 0) {
                    coloresDisponibles.add(prod.getColor());
                }
            }
            cbColor.setItems(FXCollections.observableArrayList(coloresDisponibles));
            cbColor.setDisable(false);
            cbColor.getSelectionModel().select(p.getColor());

            codigoPro.setText(String.valueOf(p.getIdProducto()));
            punitPro.setText(String.valueOf(obj.getPunitario()));
            
            for(Producto prod : allProductosVenta) {
                if(prod.getIdProducto().equals(p.getIdProducto())) {
                    stockPro.setText(String.valueOf(prod.getStock()));
                    break;
                }
            }
            
            cantidadPro.setText(String.valueOf(obj.getCantidad()));
            calcularPT();
            btnRegCarrito.setDisable(false);
            
            // Eliminar del carrito para que el usuario pueda guardarlo de nuevo
            daoC.delete(obj.getIdCarrito());
            listar();
            
            Stage stage = StageManager.getPrimaryStage();
            double with=stage.getMaxWidth()/2;
            Toast.showToast(stage, "Producto devuelto al formulario para edición", 2000, with, 50);
        }
    }

    public void deleteReg(VentCarrito obj) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Confirmar acción");
        alert.setContentText("¿Estás seguro de que deseas eliminar este elemento?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            daoC.delete(obj.getIdCarrito());
            Stage stage = StageManager.getPrimaryStage();
            double with=stage.getMaxWidth()/2;
            Toast.showToast(stage, "¡Acción completada!", 2000, with, 50);
            listar();
        } else {
            System.out.println("Acción cancelada");
        }
    }

    @FXML
    private void calcularPT(){
        if(!cantidadPro.getText().equals("")){
            double dato=Double.parseDouble(punitPro.getText())*Double.parseDouble(cantidadPro.getText());
            preTPro.setText(String.valueOf(dato));
            if(Double.parseDouble(cantidadPro.getText())>0.0){
                btnRegCarrito.setDisable(false);
            }else{
                btnRegCarrito.setDisable(true);
            }
        }else{
            btnRegCarrito.setDisable(true);
        }
    }


    @FXML
    private void registarPCarrito(){
        System.out.println("ID:"+SessionManager.getInstance().getUserId());
        try {
            VentCarrito ss= VentCarrito.builder()
                    .dniruc(dniRuc.getText())
                    .idProducto(ps.findById(Long.parseLong(codigoPro.getText())))
                    .nombreProducto(nombreProducto.getText())
                    .cantidad(Double.parseDouble(cantidadPro.getText()))
                    .punitario(Double.parseDouble(punitPro.getText()))
                    .ptotal(Double.parseDouble(preTPro.getText()))
                    .estado(1)
                    .idUsuario(daoU.findById(SessionManager.getInstance().getUserId()))
                    .build();
            daoC.save(ss);
            listar();
            
            autocompProducto.clear();
            nombreProducto.clear();
            if(cbTalla != null) { cbTalla.getItems().clear(); cbTalla.setDisable(true); }
            if(cbColor != null) { cbColor.getItems().clear(); cbColor.setDisable(true); }
            codigoPro.clear();
            stockPro.clear();
            cantidadPro.clear();
            punitPro.clear();
            preTPro.clear();
            btnRegCarrito.setDisable(true);
            lastProducto = null;
            autocompProducto.requestFocus();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void registrarVenta(){
        Locale locale = new Locale("es", "es-PE");
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", locale);
        String fechaFormateada = localDate.format(formatter);

        List<VentCarrito> dd = daoC.listaCarritoCliente(dniRuc.getText());
        if(dd.size()>0){
            Venta to=Venta.builder()
                    .dniruc(cs.findById(dniRuc.getText()))
                    .preciobase(Double.parseDouble(txtBaseImp.getText()))
                    .igv(Double.parseDouble(txtIgv.getText()))
                    .preciototal(Double.parseDouble(txtImporteT.getText()))
                    .idUsuario(daoU.findById(SessionManager.getInstance().getUserId()))
                    .serie("V")
                    .tipoDoc("Factura")
                    .fechaGener(localDate.parse(fechaFormateada, formatter))
                    .numDoc("00" )
                    .build();
            Venta idX = daoV.save(to);

            List<VentaDetalle> vdList = new ArrayList<>();
            if (idX.getIdVenta() != 0) {
                for (VentCarrito car : dd) {
                    VentaDetalle vd = VentaDetalle.builder()
                            .idVenta(idX)
                            .idProducto(ps.findById(car.getIdProducto().getIdProducto()))
                            .cantidad(car.getCantidad())
                            .descuento(0.0)
                            .pu(car.getPunitario())
                            .subtotal(car.getPtotal())
                            .build();
                    VentaDetalle vdTemp=daoVD.save(vd);
                    vdList.add(vdTemp);
                }
            }
            daoC.deleteCarAll(dniRuc.getText());
            listar();
            idX.setDetalleVenta(vdList);
            Comprobante comprobante = daoV.generarComprobante(idX);
            try {
                if(comprobante!=null && dd.size()>0) {
                    mostrarVisorComprobante(comprobante);
                        //jasperPrint= daoV.runReport(Long.parseLong(String.valueOf(idX.getIdVenta())));
                        //Platform.runLater(() -> {
                          //  ReportAlert reportAlert=new ReportAlert(jasperPrint);
                           // reportAlert.show();
                        //});
                    limpiarForm();
                    print(Long.parseLong(String.valueOf(idX.getIdVenta())));

                }else{
                    Stage stage = StageManager.getPrimaryStage();
                    double with=stage.getMaxWidth()/2;
                    Toast.showToast(stage, "Error al generar comprobante", 2000, with, 50);
                }
            }catch (Exception e){
                System.out.println("VER:"+e.getMessage());
            }
        }else{
            Stage stage = StageManager.getPrimaryStage();
            double with=stage.getMaxWidth()/2;
            Toast.showToast(stage, "No hay productos en el carrito", 2000, with, 50);
        }
    }


    public  void mostrarVisorComprobante(Comprobante boleta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_comprobante.fxml"));
            Parent visorRoot = loader.load();

            MainComprobanteController controller = loader.getController();
            controller.pasarDatos(boleta);

            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Visualizar Comprobante");
            alert.setHeaderText(null);

            if (visorRoot instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) visorRoot).setPrefSize(800, 600);
            }

            StackPane stackPane = new StackPane(visorRoot);
            alert.getDialogPane().setContent(stackPane);

            ButtonType closeButton = new ButtonType("Cerrar");
            alert.getButtonTypes().add(closeButton);

            alert.setOnCloseRequest(event -> alert.close());
            alert.showAndWait().ifPresent(response -> {
                if (response == closeButton) {
                    alert.close();
                }
            });

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void print(Long idv){
        Venta vt=daoV.findById(idv);
        try {
            PrinterManager printerManager = PrinterManager.getInstance();
            PrintService printService = printerManager.getPrintService();
            PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
            EscPos escpos = new EscPos(printerOutputStream);

            Style titleStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true)
                    .setFontSize(Style.FontSize._2, Style.FontSize._2);
            Style normal = new Style()
                    .setJustification(EscPosConst.Justification.Left_Default);
            Style center = new Style()
                    .setJustification(EscPosConst.Justification.Center);
            escpos.writeLF(titleStyle, "BOLETA DE VENTA");
            escpos.writeLF(center, "Tienda Demo S.A.C.");
            escpos.writeLF(center, "RUC: 12345678901");
            escpos.writeLF(center, "Av. Principal 123 - Lima");
            escpos.writeLF(center, "--------------------------------");

            //Datos del cliente
            escpos.writeLF(normal, "Cliente: "+vt.getDniruc().getNombres());
            escpos.writeLF(normal, "DNI: "+vt.getDniruc().getDniruc());
            escpos.writeLF(normal, "Direccion: "+vt.getDniruc().getDireccion());
            escpos.writeLF(normal, "Fecha: "+vt.getFechaGener()+"");
            escpos.writeLF(normal, "--------------------------------");
            //Detalle
            escpos.writeLF(normal, "Cant  Descripción           Importe");
            int x=1;
            for(VentaDetalle vd:vt.getDetalleVenta()) {
                String punit=x+"      "+vd.getIdProducto().getNombre()+"          S/ "+vd.getSubtotal()+"";
                escpos.writeLF(normal, punit);
            }
            escpos.writeLF(normal, "--------------------------------");
            escpos.writeLF(normal, "TOTAL:                    S/"+vt.getPreciototal()+"");
            escpos.writeLF(normal, "--------------------------------");

            //Agregar QR con los datos principales
            String qrData = "Boleta N°001-000123 | Total: S/ "+vt.getPreciototal()+" | Fecha: "+vt.getFechaGener();
            QRCode qrCode = new QRCode()
                    .setJustification(EscPosConst.Justification.Center)
                    .setErrorCorrectionLevel(QRCode.QRErrorCorrectionLevel.QR_ECLEVEL_M_Default)
                    .setModel(QRCode.QRModel._1_Default);

            escpos.write(qrCode, qrData);
            escpos.feed(2);
            escpos.writeLF(center, "Gracias por su compra!");
            escpos.feed(6);

            //Corte total
            escpos.cut(EscPos.CutMode.FULL);
            escpos.close();
            System.out.println("Boleta impresa correctamente.");
            System.out.println("Impresión completada correctamente.");
        } catch (IOException e) {
            System.err.println("Error al inicializar la impresora: " + e.getMessage());
        }
    }


    public void limpiarForm(){
        autocompCliente.setText("");
        dniRuc.setText("");
        razonSocial.setText("");
        txtDireccion.setText("");

    }

}