package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.model.Producto;

import java.util.List;

public interface ProductoIService extends ICrudGenericoService<Producto, Long> {
    List<ModeloDataAutocomplet> listAutoCompletProducto(String nombre);
    List<ModeloDataAutocomplet> listAutoCompletProducto();
}
