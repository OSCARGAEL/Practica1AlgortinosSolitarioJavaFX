import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;
import solitaire.SolitaireGame;

public class Tablero {

    // contenedor raiz
    GridPane raiz = new GridPane();

    // vista del mazo draw
    StackPane vistaMazo = new StackPane();

    // pila de descarte waste
    Pane descarte = new Pane();

    // cuatro foundations
    List<Pane> fundacion = new ArrayList<>();

    // siete columnas tableau
    List<Pane> columna = new ArrayList<>();

    // controlador de interaccion y pintado
    ControladorJuego controlador;

    // construye el tablero visual y conecta eventos basicos
    public Tablero(Runnable onVictoria, Pane capaArrastre) {
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setHgap(14);
        raiz.setVgap(14);
        raiz.setPadding(new Insets(16));

        vistaMazo.setPrefSize(100, 140);

        Image img = CargadorCarta.cargarImagenDeCartaPorNombre("back.png");
        if (img != null) {
            ImageView v = new ImageView(img);
            v.setFitWidth(100);
            v.setPreserveRatio(true);
            vistaMazo.getChildren().add(v);
        } else {
            Rectangle r = new Rectangle(100, 140);
            r.setArcWidth(14);
            r.setArcHeight(14);
            r.setFill(Color.web("#2b5aa7"));
            vistaMazo.getChildren().add(r);
        }

        descarte.setPrefSize(100, 140);

        HBox zonaMazoDescarte = new HBox(12, vistaMazo, descarte);
        zonaMazoDescarte.setAlignment(Pos.CENTER_LEFT);

        HBox filaFundacion = new HBox(12);
        for (int i = 0; i < 4; i++) {
            Pane b = new Pane();
            b.setPrefSize(100, 140);
            b.setStyle(
                    "-fx-background-color:#e9f5ea;"
                            + "-fx-border-color:darkgreen;"
                            + "-fx-border-width:1.5;"
                            + "-fx-background-radius:12;"
                            + "-fx-border-radius:12;"
            );
            fundacion.add(b);
            filaFundacion.getChildren().add(b);
        }

        HBox filaSuperior = new HBox(250, zonaMazoDescarte, filaFundacion);
        filaSuperior.setAlignment(Pos.CENTER);
        raiz.add(filaSuperior, 0, 0, 7, 1);

        for (int i = 0; i < 7; i++) {
            Pane p = new Pane();
            p.setPrefSize(120, 480);
            columna.add(p);
            raiz.add(new VBox(new Label(""), p), i, 1);
        }

        controlador = new ControladorJuego(
                vistaMazo,
                descarte,
                fundacion,
                columna,
                onVictoria,
                capaArrastre
        );

        vistaMazo.setOnMouseClicked(e -> controlador.manejarAccionDelMazo());
    }

    // devuelve el nodo raiz del tablero
    public Parent obtenerNodoRaizTablero() {
        return raiz;
    }

    // reinicia la partida desde el tablero
    public void reiniciarPartidaDesdeTablero() {
        controlador.inicializarNuevaPartida();
    }

    // devuelve la instancia del juego
    public SolitaireGame obtenerInstanciaJuego() {
        return controlador.obtenerInstanciaJuego();
    }

    // actualiza la vista del tablero en pantalla
    public void actualizarVistaDelTablero() {
        controlador.actualizarInterfazGrafica();
    }
}