package pe.edu.upeu.sysventas.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.components.StageManager;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.config.AppContext;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.model.Usuario;
import pe.edu.upeu.sysventas.service.IUsuarioService;

import java.io.IOException;

public class LoginController {

    private final IUsuarioService us;

    public LoginController(IUsuarioService us) {
        this.us = us;
    }

    @FXML
    TextField txtUsuario;
    @FXML
    PasswordField txtClave;
    @FXML
    Button btnIngresar;

    @FXML
    public void login(ActionEvent event) throws IOException {
        try {
            us.loginUsuario(txtUsuario.getText(), txtClave.getText())
                    .ifPresentOrElse(
                            usu -> abrirMain(event, usu),
                            () -> mostrarError(event)
                    );
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
        }
    }

    private void abrirMain(ActionEvent event, Usuario usu) {
        try {
            SessionManager.getInstance().setUserId(usu.getIdUsuario());
            SessionManager.getInstance().setUserName(usu.getUsuario());
            SessionManager.getInstance().setUserPerfil(usu.getIdPerfil().getNombre());

            AppContext ctx = AppContext.getInstance();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/maingui.fxml"));
            loader.setControllerFactory(clazz -> ctx.getBean(clazz));
            Parent mainRoot = loader.load();
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getBounds();
            Scene mainScene = new Scene(mainRoot, bounds.getWidth(), bounds.getHeight() - 30);

            mainScene.getStylesheets().add( getClass().getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/img/store.png").toExternalForm()));
            stage.setScene(mainScene);
            stage.setTitle("NemaStreet - Tienda de Ropas");
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setResizable(true);

            StageManager.setPrimaryStage(stage);
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.show();
        } catch (IOException e) {
            System.err.println("Error abriendo ventana principal: " +
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarError(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double w = stage.getWidth() * 2;
        double h = stage.getHeight() / 2;
        Toast.showToast(stage, "Credencial inválido!! intente nuevamente", 2000, w, h);
    }

    @FXML
    public void cerrar(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        Platform.exit();
        System.exit(0);
    }

}
