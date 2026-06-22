package pe.edu.upeu.sysventas.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.dto.comprobante.ItemComprobante;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.VentaRepository;
import pe.edu.upeu.sysventas.service.IVentaService;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VentaServiceImp extends CrudGenericoServiceImp<Venta, Long>
        implements IVentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaServiceImp.class);

    private final VentaRepository ventaRepository;
    private final DataSource dataSource;

    public VentaServiceImp(VentaRepository ventaRepository, DataSource dataSource) {
        this.ventaRepository = ventaRepository;
        this.dataSource = dataSource;
    }

    @Override
    protected ICrudGenericoRepository<Venta, Long> getRepo() {
        return ventaRepository;
    }

    @Override
    public File getFile(String filex) {
        File newFolder = new File("jasper");
        Path camino = Paths.get(newFolder.getAbsolutePath() + "/" + filex);
        logger.debug("Ruta Jasper: {}", camino.toAbsolutePath());
        return camino.toFile();
    }

    @Override
    public Comprobante generarComprobante(Venta venta){
        Comprobante b = new Comprobante();
        b.setRucEmisor("10436319172");
        b.setRazonSocial("SysCenterLife S.A.C.");
        b.setDireccion("Av. La Torre  - Juliaca");
        b.setUbigeo("Puno, Juliaca, Cercado");
        b.setTelefono("951782520");

        b.setTipoDocumento("BOLETA DE VENTA ELECTRÓNICA");
        b.setSerie("B001");
        b.setNumero("00000042");
        b.setFechaEmision(venta.getFechaGener().toString());
        b.setMoneda("PEN");

        b.setTipoDocCliente(venta.getDniruc().getTipoDocumento().name());
        b.setNroDocCliente(venta.getDniruc().getDniruc());
        b.setNombreCliente(venta.getDniruc().getNombres());
        b.setDireccionCliente(venta.getDniruc().getDireccion());

        List<ItemComprobante> items = new ArrayList<>();

        for (VentaDetalle vd : venta.getDetalleVenta()) {
            items.add(new ItemComprobante(vd.getIdProducto().getNombre(), vd.getCantidad(), new BigDecimal(vd.getSubtotal())));
        }
        b.setItems(items);
        // Calcular totales
        BigDecimal total = items.stream()
                .map(ItemComprobante::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        b.setOpGravadas(total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP));
        b.setIgv(total.subtract(b.getOpGravadas()).setScale(2, RoundingMode.HALF_UP));
        b.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        return b;
    }


   /* @Override
    public JasperPrint runReport(Long idv) throws JRException, SQLException {
        if (!ventaRepository.existsById(idv)) {
            throw new IllegalArgumentException("La venta con id " + idv + " no existe");
        }
        HashMap<String, Object> param = new HashMap<>();
        param.put("idventa", idv);
        param.put("imagenurl", getFile("logoupeu.png").getAbsolutePath());
        param.put("urljasper", getFile("detallev.jasper").getAbsolutePath());

        JasperDesign jdesign = JRXmlLoader.load(getFile("comprobante.jrxml"));
        JasperReport jreport = JasperCompileManager.compileReport(jdesign);

        try (Connection conn = dataSource.getConnection()) {
            return JasperFillManager.fillReport(jreport, param, conn);
        }
    }

    @Override
    public JasperPrint runReportVentas(String fInicio, String ffinal) throws JRException, SQLException {
        HashMap<String, Object> param = new HashMap<>();
        param.put("fechaI", fInicio);
        param.put("imagenurl", getFile("logoupeu.png").getAbsolutePath());
        param.put("fechaF", ffinal);

        JasperDesign jdesign = JRXmlLoader.load(getFile("reporte_venta_resumen.jrxml"));
        JasperReport jreport = JasperCompileManager.compileReport(jdesign);
        try (Connection conn = dataSource.getConnection()) {
            return JasperFillManager.fillReport(jreport, param, conn);
        }
    }*/
}
