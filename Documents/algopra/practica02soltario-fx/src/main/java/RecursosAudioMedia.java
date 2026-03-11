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
    private static boolean muteado = false;
    private static double volumenAnterior = 1.0;

    // crea el fondo animado para la interfaz
    public static ImageView crearFondoAnimadoInterfaz() {
        Image i = cargarImageDesdeClasspath("/sonidos/" + "fondito.gif");
        if (i == null) {
            return null;
        }

        ImageView v = new ImageView(i);
        v.setPreserveRatio(false);
        return v;
    }

    // inicia la reproduccion de musica de fondo
    public static void iniciarReproduccionMusicaFondo() {
        Media m = cargarMediaDesdeClasspath("/sonidos/" + "musica.mp3");
        if (m == null) {
            return;
        }

        musica = new MediaPlayer(m);
        musica.setCycleCount(MediaPlayer.INDEFINITE);
        if (muteado) {
            musica.setVolume(0.0);
        } else {
            musica.setVolume(1.0);
        }
        musica.play();
    }

    // reproduce un efecto de sonido
    public static void reproducirEfectoDeSonido(String base) {
        Media m = cargarMediaDesdeClasspath("/sonidos/" + base + ".mp3");
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

    // detiene la musica y libera recursos de audio
    public static void detenerMusicaYLiberarRecursos() {
        if (musica != null) {
            try {
                musica.stop();
                musica.dispose();
            } catch (Exception ignored) {
            }
        }

        for (MediaPlayer mp : new ArrayList<>(activos)) {
            try {
                mp.stop();
                mp.dispose();
            } catch (Exception ignored) {
            }
        }

        activos.clear();
    }

    // carga un media desde recursos del proyecto
    private static Media cargarMediaDesdeClasspath(String cp) {
        try {
            URL u = RecursosAudioMedia.class.getResource(cp);
            if (u != null) {
                return new Media(u.toExternalForm());
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    // carga una imagen desde recursos del proyecto
    private static Image cargarImageDesdeClasspath(String cp) {
        try {
            URL u = RecursosAudioMedia.class.getResource(cp);
            if (u != null) {
                return new Image(u.toExternalForm());
            }
        } catch (Throwable ignored) {
        }

        return null;
    }
    public static void alternarMuteDeMusica() {
    muteado = !muteado;

    if (musica != null) {
        if (muteado) {
            volumenAnterior = musica.getVolume();
            musica.setVolume(0.0);
        } else {
            musica.setVolume(volumenAnterior <= 0 ? 1.0 : volumenAnterior);
        }
    }
    }

    public static boolean estaMuteadaLaMusica() {
        return muteado;
    }
}