import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import solitaire.SolitaireGame;

public class GUI extends Application {

    private Tablero tablero;
    private Pane capaArrastre;
    private Label lblMov = new Label("Movimientos: 0");
    private Timeline cron;

    // inicia la aplicacion grafica del juego
    @Override
    public void start(Stage v) {
        Button botonNuevo = new Button("Nuevo juego");
        Button botonDeshacer = new Button("Deshacer");

        HBox barra = new HBox(16, botonNuevo, botonDeshacer, lblMov);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(10, 10, 10, 10));

        capaArrastre = new Pane();
        capaArrastre.setPickOnBounds(false);

        tablero = new Tablero(this::mostrarPantallaDeVictoria, capaArrastre);

        BorderPane capa = new BorderPane();
        capa.setTop(barra);
        capa.setCenter(tablero.obtenerNodoRaizTablero());

        StackPane raiz = new StackPane();

        Node fondo = RecursosAudioMedia.crearFondoAnimadoInterfaz();
        if (fondo != null) {
            fondo.setMouseTransparent(true);
            raiz.getChildren().add(fondo);
        }

        raiz.getChildren().add(capa);
        raiz.getChildren().add(capaArrastre);

        Scene s = new Scene(raiz, 1100, 700);

        if (fondo instanceof ImageView) {
            ImageView iv = (ImageView) fondo;
            iv.fitWidthProperty().bind(s.widthProperty());
            iv.fitHeightProperty().bind(s.heightProperty());
        } else if (fondo instanceof MediaView) {
            MediaView mv = (MediaView) fondo;
            mv.fitWidthProperty().bind(s.widthProperty());
            mv.fitHeightProperty().bind(s.heightProperty());
        }

        v.setTitle("Solitario");
        v.setScene(s);
        v.show();

        RecursosAudioMedia.iniciarReproduccionMusicaFondo();

        tablero.reiniciarPartidaDesdeTablero();

        botonNuevo.setOnAction(e -> {
            tablero.reiniciarPartidaDesdeTablero();
            try {
                tablero.actualizarVistaDelTablero();
            } catch (Throwable ignored) {
            }
            actualizarEtiquetaDeMovimientos();
        });

        botonDeshacer.setOnAction(e -> {
            deshacerUltimoMovimientoDelJuego();
            actualizarEtiquetaDeMovimientos();
        });

        cron = new Timeline(new KeyFrame(Duration.millis(200), e -> actualizarEtiquetaDeMovimientos()));
        cron.setCycleCount(Timeline.INDEFINITE);
        cron.play();
    }

    // deshace el ultimo movimiento y repinta la interfaz
    private void deshacerUltimoMovimientoDelJuego() {
        SolitaireGame game = tablero.obtenerInstanciaJuego();
        if (game == null) {
            return;
        }

        boolean ok = false;
        try {
            ok = game.deshacerUltimoMovimiento();
        } catch (Throwable ignored) {
        }

        if (ok) {
            try {
                tablero.actualizarVistaDelTablero();
            } catch (Throwable ignored) {
            }
        }
    }

    // actualiza la etiqueta con la cantidad de movimientos
    private void actualizarEtiquetaDeMovimientos() {
        try {
            SolitaireGame g = tablero.obtenerInstanciaJuego();
            if (g != null) {
                lblMov.setText("Movimientos: " + g.getMoveCount());
            }
        } catch (Throwable ignored) {
        }
    }

    // detiene la aplicacion y libera recursos
    @Override
    public void stop() {
        try {
            if (cron != null) {
                cron.stop();
            }
        } catch (Throwable ignored) {
        }

        RecursosAudioMedia.detenerMusicaYLiberarRecursos();
        Platform.exit();
    }

    // muestra la pantalla o mensaje de victoria
    private void mostrarPantallaDeVictoria() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initModality(Modality.APPLICATION_MODAL);
        a.setTitle("Victoria");
        a.setHeaderText("¡Ganaste!");
        a.setContentText("Volver a jugar");
        ((Button) a.getDialogPane().lookupButton(a.getButtonTypes().get(0))).setText("Reiniciar");
        a.showAndWait();
        tablero.reiniciarPartidaDesdeTablero();
    }

    // punto de entrada de la aplicacion
    public static void main(String[] args) {
        launch();
    }
}