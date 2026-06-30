package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import pe.edu.upeu.sysventas.components.*;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Marca;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IMarcaService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;
import pe.edu.upeu.sysventas.service.ProductoIService;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Cambios respecto a Micronaut:
 * - Elimina @Singleton, @Inject (Micronaut/jakarta.inject).
 * - Servicios recibidos por constructor (inyección manual desde AppContext).
 */
public class ProductoController {

    @FXML TextField txtNombreProducto, txtPUnit,
            txtPUnitOld, txtUtilidad, txtStock, txtStockOld, txtFiltroDato, txtTalla, txtColor;
    @FXML ComboBox<ComboBoxOption> cbxMarca;
    @FXML ComboBox<ComboBoxOption> cbxCategoria;
    @FXML ComboBox<ComboBoxOption> cbxUnidMedida;

    @FXML private TableView<Producto> tableView;

    @FXML Label lbnMsg, idPrueba;
    @FXML private AnchorPane miContenedor;
    Stage stage;

    private final IMarcaService ms;
    private final ICategoriaService cs;
    private final ProductoIService ps;
    private final IUnidadMedidaService ums;

    public ProductoController(IMarcaService ms, ICategoriaService cs,
                              ProductoIService ps, IUnidadMedidaService ums) {
        this.ms = ms;
        this.cs = cs;
        this.ps = ps;
        this.ums = ums;
    }

    private Validator validator;
    ObservableList<Producto> listarProducto;
    Producto formulario;
    Long idProductoCE = 0L;

    private final ToltipCustom ttc=new ToltipCustom();



    @FXML
    public void initialize() {
        StringConverter<ComboBoxOption> converter = new StringConverter<>() {
            @Override
            public String toString(ComboBoxOption object) {
                return object == null ? "" : object.getValue();
            }
            @Override
            public ComboBoxOption fromString(String string) {
                return new ComboBoxOption("0", string);
            }
        };

        cbxMarca.getItems().addAll(ms.listarCombobox());
        cbxMarca.setEditable(true);
        cbxMarca.setConverter(converter);

        cbxCategoria.getItems().addAll(cs.listarCombobox());
        cbxCategoria.setEditable(true);
        cbxCategoria.setConverter(converter);

        cbxUnidMedida.getItems().addAll(ums.listarCombobox());
        new ComboBoxAutoComplete<>(cbxUnidMedida);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        TableViewHelper<Producto> tableViewHelper = new TableViewHelper<>();

        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Pro.", new ColumnInfo("idProducto", 60.0));
        columns.put("Nombre Producto", new ColumnInfo("nombre", 200.0));
        columns.put("Talla", new ColumnInfo("talla", 80.0));
        columns.put("Color", new ColumnInfo("color", 80.0));
        columns.put("P. Unitario", new ColumnInfo("pu", 100.0));
        columns.put("Marca", new ColumnInfo("idMarca.nombre", 150.0));
        columns.put("Categoria", new ColumnInfo("idCategoria.nombre", 150.0));

        Consumer<Producto> updateAction = producto -> editForm(producto);
        Consumer<Producto> deleteAction = producto -> {
            ps.delete(producto.getIdProducto());
            Stage currentStage = (Stage) miContenedor.getScene().getWindow();
            double w = currentStage.getWidth() / 1.5, h = currentStage.getHeight() / 2;
            Toast.showToast(currentStage, "Se eliminó correctamente!!", 2000, w, h);
            listar();
        };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);
        txtFiltroDato.textProperty().addListener((obs, o, n) -> filtrarProductos(n));
        listar();
    }

    public void listar() {
        try {
            tableView.getItems().clear();
            listarProducto = FXCollections.observableArrayList(ps.findAll());
            tableView.getItems().addAll(listarProducto);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void filtrarProductos(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            tableView.getItems().setAll(listarProducto);
        } else {
            String f = filtro.toLowerCase();
            List<Producto> filtrados = listarProducto.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(f)
                    || String.valueOf(p.getPu()).contains(f)
                    || String.valueOf(p.getUtilidad()).contains(f)
                    || p.getIdMarca().getNombre().toLowerCase().contains(f)
                    || p.getIdCategoria().getNombre().toLowerCase().contains(f))
                .collect(Collectors.toList());
            tableView.getItems().setAll(filtrados);
        }
    }

    @FXML
    public void validarFormulario() {
        formulario = new Producto();
        formulario.setNombre(txtNombreProducto.getText());
        formulario.setPu(parseDoubleSafe(txtPUnit.getText()));
        formulario.setPuold(parseDoubleSafe(txtPUnitOld.getText()));
        formulario.setUtilidad(parseDoubleSafe(txtUtilidad.getText()));
        formulario.setStock(parseDoubleSafe(txtStock.getText()));
        formulario.setStockold(parseDoubleSafe(txtStockOld.getText()));
        formulario.setTalla(txtTalla.getText());
        formulario.setColor(txtColor.getText());

        ComboBoxOption optM = cbxMarca.getValue();
        if (optM != null && optM.getKey().equals("0") && !optM.getValue().trim().isEmpty()) {
            String typedName = optM.getValue().trim();
            ComboBoxOption existing = cbxMarca.getItems().stream()
                .filter(m -> m.getValue().equalsIgnoreCase(typedName))
                .findFirst().orElse(null);
            if (existing != null) {
                optM = existing;
            } else {
                Marca newMarca = ms.save(pe.edu.upeu.sysventas.model.Marca.builder().nombre(typedName).build());
                optM = new ComboBoxOption(String.valueOf(newMarca.getIdMarca()), newMarca.getNombre());
                cbxMarca.getItems().add(optM);
                cbxMarca.getSelectionModel().select(optM);
            }
        }
        String idxM = optM == null ? "0" : optM.getKey();
        formulario.setIdMarca(idxM.equals("0") ? null : ms.findById(Long.parseLong(idxM)));

        ComboBoxOption optC = cbxCategoria.getValue();
        if (optC != null && optC.getKey().equals("0") && !optC.getValue().trim().isEmpty()) {
            String typedName = optC.getValue().trim();
            ComboBoxOption existing = cbxCategoria.getItems().stream()
                .filter(c -> c.getValue().equalsIgnoreCase(typedName))
                .findFirst().orElse(null);
            if (existing != null) {
                optC = existing;
            } else {
                pe.edu.upeu.sysventas.model.Categoria newCat = cs.save(pe.edu.upeu.sysventas.model.Categoria.builder().nombre(typedName).build());
                optC = new ComboBoxOption(String.valueOf(newCat.getIdCategoria()), newCat.getNombre());
                cbxCategoria.getItems().add(optC);
                cbxCategoria.getSelectionModel().select(optC);
            }
        }
        String idxC = optC == null ? "0" : optC.getKey();
        formulario.setIdCategoria(idxC.equals("0") ? null : cs.findById(Long.parseLong(idxC)));

        String idxUM = cbxUnidMedida.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxUnidMedida.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdUnidad(idxUM.equals("0") ? null : ums.findById(Long.parseLong(idxUM)));

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);
        List<ConstraintViolation<Producto>> violacionesOrdenadas = violaciones.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();

        } else {
            mostrarErroresValidacion(violacionesOrdenadas);
        }
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private void mostrarErroresValidacion(List<ConstraintViolation<Producto>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreProducto); campos.put("pu", txtPUnit);
        campos.put("puOld", txtPUnitOld);        campos.put("utilidad", txtUtilidad);
        campos.put("stock", txtStock);           campos.put("stockOld", txtStockOld);
        campos.put("talla", txtTalla);           campos.put("color", txtColor);
        campos.put("marca", cbxMarca);           campos.put("categoria", cbxCategoria);
        campos.put("unidadMedida", cbxUnidMedida);

        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerCtrl = {null};
        for (String campo : campos.keySet()) {
            violaciones.stream()
                .filter(v -> v.getPropertyPath().toString().equals(campo))
                .findFirst().ifPresent(v -> {

                    erroresOrdenados.put(campo, v.getMessage());

                    Control c = campos.get(campo);
                    if (c != null && !c.getStyleClass().contains("text-field-error")){
                        //c.getStyleClass().add("text-field-error");
                        if (c != null) ttc.marcarError(c, v.getMessage().trim());
                    }
                    if (primerCtrl[0] == null) primerCtrl[0] = c;
                });
        }
        if (!erroresOrdenados.isEmpty()) {
            lbnMsg.setText(erroresOrdenados.entrySet().iterator().next().getValue());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            if (primerCtrl[0] != null) Platform.runLater(primerCtrl[0]::requestFocus);
        }
    }

    private void procesarFormulario() {
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        limpiarError();
        try {
            Stage currentStage = (Stage) miContenedor.getScene().getWindow();
            double w = currentStage.getWidth() / 1.5, h = currentStage.getHeight() / 2;
            if (idProductoCE > 0L) {
                formulario.setIdProducto(idProductoCE);
                ps.update(idProductoCE, formulario);
                Toast.showToast(currentStage, "Se actualizó correctamente!!", 2000, w, h);
            } else {
                ps.save(formulario);
                Toast.showToast(currentStage, "Se guardó correctamente!!", 2000, w, h);
            }
            clearForm(); listar();
        } catch (Exception e) {
            e.printStackTrace();
            lbnMsg.setText("Error al guardar: " + e.getMessage());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        }
    }

    public void limpiarError() {
        List.of(txtNombreProducto, txtPUnit, txtPUnitOld, txtUtilidad,
                txtStock, txtStockOld, txtTalla, txtColor, cbxMarca, cbxCategoria, cbxUnidMedida)
            .forEach(c -> {c.getStyleClass().remove("text-field-error");
                ttc.limpiarCampo(c);
            });
    }

    public void clearForm() {
        txtNombreProducto.clear(); txtPUnit.clear(); txtPUnitOld.clear();
        txtUtilidad.clear(); txtStock.clear(); txtStockOld.clear();
        txtTalla.clear(); txtColor.clear();
        cbxMarca.getSelectionModel().clearSelection();
        cbxMarca.getEditor().clear();
        cbxCategoria.getSelectionModel().clearSelection();
        cbxCategoria.getEditor().clear();
        cbxUnidMedida.getSelectionModel().clearSelection();
        idProductoCE = 0L; limpiarError();
    }

    public void editForm(Producto producto) {
        txtNombreProducto.setText(producto.getNombre());
        txtPUnit.setText(producto.getPu().toString());
        txtPUnitOld.setText(producto.getPuold().toString());
        txtUtilidad.setText(producto.getUtilidad().toString());
        txtStock.setText(producto.getStock().toString());
        txtStockOld.setText(producto.getStockold().toString());
        txtTalla.setText(producto.getTalla());
        txtColor.setText(producto.getColor());
        cbxMarca.getSelectionModel().select(
            cbxMarca.getItems().stream()
                .filter(m -> Long.parseLong(m.getKey()) == producto.getIdMarca().getIdMarca())
                .findFirst().orElse(null));
        cbxCategoria.getSelectionModel().select(
            cbxCategoria.getItems().stream()
                .filter(c -> Long.parseLong(c.getKey()) == producto.getIdCategoria().getIdCategoria())
                .findFirst().orElse(null));
        cbxUnidMedida.getSelectionModel().select(
            cbxUnidMedida.getItems().stream()
                .filter(u -> Long.parseLong(u.getKey()) == producto.getIdUnidad().getIdUnidad())
                .findFirst().orElse(null));
        idProductoCE = producto.getIdProducto(); limpiarError();
    }
}
