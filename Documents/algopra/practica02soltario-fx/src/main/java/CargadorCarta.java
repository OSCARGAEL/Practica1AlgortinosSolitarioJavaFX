import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.io.InputStream;
import java.util.Locale;

public class CargadorCarta {

    public static String CLASSPATH_DIR = "/cartas/Playing Cards/PNG-cards-1.3/";

    // crea el nodo visual de una carta
    public static ImageView crearImageViewDeCarta(String palo, String rango, double ancho) {
        Image img = cargarImagenDeCartaPorNombre(construirNombreArchivoDeCarta(palo, rango));

        ImageView v = new ImageView(img);
        v.setFitWidth(ancho);
        v.setPreserveRatio(true);
        return v;
    }

    // carga la imagen de una carta desde recursos
    public static Image cargarImagenDeCartaPorNombre(String nombre) {
        Image i = cargarImagenDesdeRecursosClasspath(nombre);
        return i;
    }

    // construye el nombre del archivo de la carta
    private static String construirNombreArchivoDeCarta(String palo, String rango) {
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

    // carga una imagen desde recursos del proyecto
    private static Image cargarImagenDesdeRecursosClasspath(String n) {
        try {
            InputStream in = CargadorCarta.class.getResourceAsStream(CLASSPATH_DIR + n);
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // normaliza el palo para coincidir con recursos
    private static String normalizarNombrePaloParaRecursos(String p) {
        String s = p.replace("\uFE0E", "")
                .replace("\uFE0F", "")
                .replace("❤", "♥")
                .trim()
                .toLowerCase(Locale.ROOT);

        return switch (s) {
            case "♠", "spade", "spades", "picas" -> "spades";
            case "♣", "club", "clubs", "treboles", "tréboles" -> "clubs";
            case "♥", "heart", "hearts", "corazones" -> "hearts";
            case "♦", "diamond", "diamonds", "diamantes" -> "diamonds";
            default -> s;
        };
    }
}