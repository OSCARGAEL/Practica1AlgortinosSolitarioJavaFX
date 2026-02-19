/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.Locale;

public class CargadorCarta {
    public static String CLASSPATH_DIR = "/cartas/Playing Cards/" +
            "PNG-cards-1.3/";
    // Crea el nodo visual de una carta
    public static ImageView crearImagenDeCarta(String palo,
                                               String rango,
                                               double ancho) {
        Image img = cargarImagenDeCartaPorNombre(SeleccionarNombreDeCarta(
                palo, rango));
        
        ImageView v = new ImageView(img);
        v.setFitWidth(ancho);
        v.setPreserveRatio(true);
        return v;
    }
    // Carga una imagen de carta por nombre
    public static Image cargarImagenDeCartaPorNombre(String nombre) {
        Image i = cargarImagenDesdeRecursos(nombre);
        return i;
    }
    // Elegi por el nombre de la carta
    private static String SeleccionarNombreDeCarta(String palo,
                                                   String rango) {
        String p = normalizarNombrePaloParaRecursos(palo);
        String r = rango.toUpperCase(Locale.ROOT);
        String b = switch (r) {
            case "A" -> "ace_of_" + p;
            case "J" -> "jack_of_" + p + "2";
            case "Q" -> "queen_of_" + p + "2";
            case "K" -> "king_of_" + p + "2";
            default -> r.toLowerCase(Locale.ROOT) + "_of_" + p;
        };
        return b + ".png";
    }
    // Carga una imagen desde recursos de imagenes y sonidos del proyecto
    private static Image cargarImagenDesdeRecursos(String n) {
        try {
            InputStream in = CargadorCarta.class.
                    getResourceAsStream(CLASSPATH_DIR + n);
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {}
        return null;
    }
    // Elegi el palo de la carta para coincidir con las imagenes de cartas
    private static String normalizarNombrePaloParaRecursos(String p) {
        String s = p.replace("\uFE0E", "").
                replace("\uFE0F", "").
                replace("❤", "♥").
                trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "♠", "spade", "spades", "picas" -> "spades";
            case "♣", "club", "clubs", "treboles", "tréboles" -> "clubs";
            case "♥", "heart", "hearts", "corazones" -> "hearts";
            case "♦", "diamond", "diamonds", "diamantes" -> "diamonds";
            default -> s;
        };
    }
   
}
