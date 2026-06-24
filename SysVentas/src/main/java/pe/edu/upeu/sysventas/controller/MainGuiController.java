package pe.edu.upeu.sysventas.controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.dto.MenuMenuItenTO;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.service.IMenuMenuItemDao;
import pe.edu.upeu.sysventas.utils.UtilsX;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class MainGuiController {
    private final IMenuMenuItemDao mmiDao;

    public MainGuiController(IMenuMenuItemDao mmiDao) {
        this.mmiDao = mmiDao;
    }


    Preferences userPrefs = Preferences.userRoot();
    UtilsX util = new UtilsX();
    Properties myresources = new Properties();
    @FXML
    private TabPane tabPaneFx;
    List<MenuMenuItenTO> lista;
    @FXML private BorderPane bp;
    @FXML private MenuBar menuBarFx;
    private Parent parent;
    Stage stage;

    @FXML private Menu menuEstilo = new Menu("Cambiar Estilo");
    ComboBox<String> comboBox = new ComboBox<>(
            javafx.collections.FXCollections.observableArrayList(
                    "Estilo por Defecto", "Estilo Oscuro",
                    "Estilo Azul", "Estilo Verde", "Estilo Rosado"));
    CustomMenuItem customItem = new CustomMenuItem(comboBox);

    @FXML private Menu menuIdioma = new Menu("Idioma");
    ComboBox<String> comboBoxIdioma = new ComboBox<>(
            javafx.collections.FXCollections.observableArrayList("Español",
                    "Ingles", "Frances"));
    CustomMenuItem customItemIdioma = new CustomMenuItem(comboBoxIdioma) ;

    @FXML
    public void initialize() {
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            if (tabPaneFx.getScene() != null && tabPaneFx.getScene().getWindow() !=
                    null) {
                stage = (Stage) tabPaneFx.getScene().getWindow();
            }
        });
        pause.play();
        graficarMenus();
        bp.setCenter(tabPaneFx);
        
        // Abrir dashboard por defecto
        abrirTabConFXML("/view/main_dashboard.fxml", "Dashboard Principal");
    }

    public void abrirTabConFXML(String fxmlPath, String tituloTab) {
        try {
            AppContext ctx = AppContext.getInstance();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(clazz -> ctx.getBean(clazz));
            Parent root = loader.load();
            ScrollPane scrollPane = new ScrollPane(root);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            Tab newTab = new Tab(tituloTab, scrollPane);
            tabPaneFx.getTabs().clear();
            tabPaneFx.getTabs().add(newTab);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar FXML: " + fxmlPath, e);
        }
    }


    private List<MenuMenuItenTO> listaAccesos() {
        myresources = util.detectLanguage(userPrefs.get("IDIOMAX", "es"));
        return mmiDao.listaAccesos(SessionManager.getInstance().getUserPerfil(),myresources);
    }

    public int[] contarMenuMunuItem(List<MenuMenuItenTO> data) {
        int menui = 0, menuitem = 0;
        String menuN = "";
        for (MenuMenuItenTO mmiItem : data) {
            if (!mmiItem.getMenunombre().equals(menuN)) {
                menuN = mmiItem.getMenunombre();
                menui++;
            }
            if (!mmiItem.getMenuitemnombre().equals("")) {
                menuitem++;
            }
        }
        return new int[]{menui, menuitem};
    }


    private void graficarMenus() {
        lista = listaAccesos();
        int[] mmi = contarMenuMunuItem(lista);
        Menu[] menu = new Menu[mmi[0]];
        MenuItem[] menuItem = new MenuItem[mmi[1]];
        menuBarFx = new MenuBar();
        MenuItemListener d = new MenuItemListener();
        MenuListener m = new MenuListener();
        String menuN = "";
        int menui = 0, menuitem = 0;
        char conti = 'N';
        for (MenuMenuItenTO mmix : lista) {
            if (!mmix.getMenunombre().equals(menuN)) {
                menu[menui] = new Menu(mmix.getMenunombre());
                menu[menui].setId("m" + mmix.getIdNombreObj());
                menu[menui].setOnShowing(m::menuSelected);
                if (!mmix.getMenuitemnombre().equals("")) {
                    menuItem[menuitem] = new
                            MenuItem(mmix.getMenuitemnombre());
                    menuItem[menuitem].setId("mi" + mmix.getIdNombreObj());
                    menuItem[menuitem].setOnAction(d::handle);
                    menu[menui].getItems().add(menuItem[menuitem]);
                    menuitem++;
                }
                menuBarFx.getMenus().add(menu[menui]);
                menuN = mmix.getMenunombre();
                conti = 'N';
                menui++;
            } else {
                conti = 'S';
            }
            if (!mmix.getMenuitemnombre().equals("") &&
                    mmix.getMenunombre().equals(menuN) && conti == 'S') {
                menuItem[menuitem] = new MenuItem(mmix.getMenuitemnombre());
                menuItem[menuitem].setId("mi" + mmix.getIdNombreObj());
                menuItem[menuitem].setOnAction(d::handle);
                menu[menui - 1].getItems().add(menuItem[menuitem]);
                menuitem++;
            }
        }
        comboBox.setOnAction(e -> cambiarEstilo());
        customItem.setHideOnClick(false);
        menuEstilo.getItems().clear();
        menuEstilo.getItems().add(customItem);
        menuBarFx.getMenus().addAll(menuEstilo);

        comboBoxIdioma.setOnAction(e -> cambiarIdioma());
        customItemIdioma.setHideOnClick(false);
        menuIdioma.getItems().clear();
        menuIdioma.getItems().add(customItemIdioma);
        menuBarFx.getMenus().addAll(menuIdioma);

        bp.setTop(menuBarFx);
    }

    private void cambiarIdioma() {
        String idiomaSeleccionado = comboBoxIdioma.getSelectionModel().getSelectedItem();
        switch (idiomaSeleccionado) {
            case "Español": userPrefs.put("IDIOMAX", "es"); break;
            case "Ingles": userPrefs.put("IDIOMAX", "en"); break;
            case "Frances": userPrefs.put("IDIOMAX", "fr"); break;
            default: userPrefs.put("IDIOMAX", "es"); break;
        }
        graficarMenus();
    }



    private void cambiarEstilo() {
        String estiloSeleccionado =
                comboBox.getSelectionModel().getSelectedItem();
        Scene escena = bp.getScene();
        escena.getStylesheets().clear();
        switch (estiloSeleccionado) {
            case "Estilo Oscuro":

                escena.getStylesheets().add(getClass().getResource("/css/estilo-oscuro.css").toExternalForm()); break;
            case "Estilo Azul":

                escena.getStylesheets().add(getClass().getResource("/css/estilo-azul.css").toExternalForm()); break;
            case "Estilo Verde":

                escena.getStylesheets().add(getClass().getResource("/css/estilo-verde.css").toExternalForm()); break;
            case "Estilo Rosado":

                escena.getStylesheets().add(getClass().getResource("/css/estilo-rosado.css").toExternalForm()); break;
            default: break;
        }
    }


    class MenuItemListener {
        Map<String, String[]> menuConfig;
        MenuItemListener() {
            menuConfig = mmiDao.accesosAutorizados(lista);
        }
        public void handle(ActionEvent e) {
            String id = ((MenuItem) e.getSource()).getId();
            if (menuConfig.containsKey(id)) {
                String[] cfg = menuConfig.get(id);
                if (cfg[2].equals("S")) {
                    redireccionar(cfg[0]);
                } else {
                    abrirTabConFXML(cfg[0], cfg[1]);
                }
            }
        }



        private void redireccionar(String fxmlPath) {
            tabPaneFx.getTabs().clear();
            try {
                AppContext ctx = AppContext.getInstance();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
                fxmlLoader.setControllerFactory(clazz -> ctx.getBean(clazz));
                parent = fxmlLoader.load();
                Scene scene = new Scene(parent);
                stage.sizeToScene();
                stage.setScene(scene);
                stage.centerOnScreen();
                stage.setTitle("NemaStreet - Tienda de Ropas");
                stage.setResizable(false);
                stage.show();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class MenuListener {
        public void menuSelected(Event e) {
            if (((Menu) e.getSource()).getId().equals("mmiver1")) {
                System.out.println("llego help");
            }
        }
    }


}
