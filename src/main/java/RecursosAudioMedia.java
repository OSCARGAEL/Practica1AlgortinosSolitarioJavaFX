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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RecursosAudioMedia {
    private static MediaPlayer musica;
    private static final List<MediaPlayer> activos = new ArrayList<>();

    // Crea el fondo animado para la interfaz
    public static ImageView crearFondoAnimado() {
        Image i = cargarImagen("/sonidos/" + "fondito.gif");
        if (i == null) {
            return null;
        }
        ImageView v = new ImageView(i);
        v.setPreserveRatio(false);
        return v;
    }

    // Inicia la musica de fondo en loop
    public static void iniciarMusicaDeFondo() {
        Media m = cargarMedio("/sonidos/" + "musica.mp3");
        if (m == null) {
            return;
        }
        musica = new MediaPlayer(m);
        musica.setCycleCount(MediaPlayer.INDEFINITE);
        musica.play();
    }

    // Reproduce un efecto de sonido de acierto o fallo al mover una carta
    public static void reproducirEfectoDeSonido(String base) {
        Media m = cargarMedio("/sonidos/" + base + ".mp3");
        if (m == null) {
            return;
        }
        MediaPlayer mp = new MediaPlayer(m);
        activos.add(mp);
        mp.setOnEndOfMedia(() -> {
            mp.dispose();
            activos.remove(mp);
        });
        mp.play();
    }

    // Detiene audio y libera recursos cargados
    public static void detenerYLiberar() {
        if (musica != null) {
            try {
                musica.stop();
                musica.dispose();
            } catch (Exception ignored) {}
        }
        for (MediaPlayer mp : new ArrayList<>(activos)) {
            try {
                mp.stop();
                mp.dispose();
            } catch (Exception ignored) {}
        }
        activos.clear();
    }

    // Carga un sonido desde recursos del proyecto
    private static Media cargarMedio(String cp) {
        try {
            URL u = RecursosAudioMedia.class.getResource(cp);
            if (u != null) {
                return new Media(u.toExternalForm());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    // Carga una imagen desde recursos del proyecto
    private static Image cargarImagen(String cp) {
        try {
            URL u = RecursosAudioMedia.class.getResource(cp);
            if (u != null) {
                return new Image(u.toExternalForm());
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
