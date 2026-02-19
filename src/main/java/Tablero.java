    /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
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

public class Tablero {
    private final GridPane raiz = new GridPane();
    private final StackPane vistaMazo = new StackPane();
    private final Pane descarte = new Pane();
    private final List<Pane> fundacion = new ArrayList<>();
    private final List<Pane> columna = new ArrayList<>();
    private final ControladorJuego controlador;

    public Tablero(Runnable onVictoria,Pane capaArrastre) {
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
            b.setStyle("-fx-background-color:#e9f5ea;-fx-border-color:darkgreen;" +
                    "-fx-border-width:1.5;-fx-background-radius:12;" +
                    "-fx-border-radius:12;");
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
        controlador = new ControladorJuego(vistaMazo, descarte, fundacion, columna,
                onVictoria,capaArrastre);
        vistaMazo.setOnMouseClicked(e ->
                controlador.manejarAccionDelMazoDeRobo());
    }
    // Devuelve el nodo raiz para mostrar el tablero
    public Parent obtenerNodoRaiz() {
        return raiz;
    }

    // Reinicia la partida
    public void iniciarNuevaPartida() {
        controlador.prepararNuevaPartida();
    }
}
