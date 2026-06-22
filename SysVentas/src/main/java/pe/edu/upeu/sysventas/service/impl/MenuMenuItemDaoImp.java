package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.MenuMenuItenTO;
import pe.edu.upeu.sysventas.service.IMenuMenuItemDao;

import java.util.*;

public class MenuMenuItemDaoImp implements IMenuMenuItemDao {
    @Override
    public List<MenuMenuItenTO> listaAccesos(String perfil, Properties idioma) {

        List<MenuMenuItenTO> lista = new ArrayList<>();

        lista.add(new MenuMenuItenTO("miprincipal", "/view/login.fxml",
                idioma.getProperty("menu.nombre.principal"),
                idioma.getProperty("menuitem.nombre.salir"), "Salir", "S"));
        lista.add(new MenuMenuItenTO("miproducto", "/view/main_producto.fxml",
                idioma.getProperty("menu.nombre.producto"), idioma.getProperty("menuitem.nombre.producto"), "Gestión Productos", "T"));

        lista.add(new MenuMenuItenTO("micliente", "/view/main_producto.fxml", //cambiar por main_cliente
                "Venta", "Reg. Cliente", "Gestionar Cliente", "T"));
        lista.add(new MenuMenuItenTO("miventa", "/view/main_venta.fxml",
                "Venta", "Reg. Venta", "Gestionar Ventas", "T"));
        lista.add(new MenuMenuItenTO("mireporte", "/view/main_reporte.fxml",
                "Venta", "Reporte. Ventas", "Gestionar Reportes", "T"));

        List<MenuMenuItenTO> accesoReal = new ArrayList<>();
        accesoReal.add(lista.get(0));
        switch (perfil) {
            case "Administrador":
                accesoReal.add(lista.get(2));
                accesoReal.add(lista.get(3));
                break;
            case "Root":
                accesoReal = lista;
                break;
            case "Reporte":
                accesoReal.add(lista.get(1));
                accesoReal.add(lista.get(2));
                break;
            default:
                throw new AssertionError();
        }
        return accesoReal;

    }

    @Override
    public Map<String, String[]> accesosAutorizados(List<MenuMenuItenTO> accesos) {
        Map<String, String[]> menuConfig = new HashMap<>();
        for (MenuMenuItenTO menu : accesos) {
            menuConfig.put("mi" + menu.getIdNombreObj(),
                    new String[]{menu.getRutaFile(), menu.getNombreTab(),
                            menu.getTipoTab()});
        }
        return menuConfig;
    }
}
