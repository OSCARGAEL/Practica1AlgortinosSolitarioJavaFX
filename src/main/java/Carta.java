/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Carta {
    public final StackPane nodo;
    public final String rango;
    public final String pinta;
    public final String paloNormalizado;
    public final int valor;
    public final boolean rojo;
    public boolean caraArriba;

    public Carta(StackPane n, String r, String p, int v, boolean ro, boolean up) {
        nodo = n;
        rango = r;
        pinta = p;
        valor = v;
        rojo = ro;
        caraArriba = up;
        paloNormalizado = switch (p.trim()) {
            case "♠" -> "spades";
            case "♣" -> "clubs";
            case "♥" -> "hearts";
            case "♦" -> "diamonds";
            default -> p.toLowerCase();
        };
    }
    // Actualiza la imagen segun el estado de la carta
    public void actualizarImagenSegunEstadoDeCarta() {
        Node cara;
        if (caraArriba) {
            cara = CargadorCarta.crearImagenDeCarta(pinta, rango, 100);
        } else {
            Image img = CargadorCarta.cargarImagenDeCartaPorNombre("back.png");
            if (img != null) {
                ImageView v = new ImageView(img);
                v.setFitWidth(100);
                v.setPreserveRatio(true);
                cara = v;
            } else {
                Rectangle r = new Rectangle(100, 140);
                r.setArcWidth(14);
                r.setArcHeight(14);
                r.setFill(Color.web("#2b5aa7"));
                cara = r;
            }
        }
        nodo.getChildren().setAll(cara);
    }
}
