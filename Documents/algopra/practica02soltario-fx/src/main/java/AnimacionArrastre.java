
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.Node;

public class AnimacionArrastre {

    public interface EjecutarAnimacion {

        // ejecuta la animacion de arrastre de una carta
        boolean ejecutar(Pane origen, List<StackPane> grupo, Pane destino);
    }

    private final List<Pane> fundacion;
    private final List<Pane> columna;
    private final Pane descarte;
    private final Map<StackPane, Carta> cartaPorNodo;
    private final Runnable onValido;
    private final Runnable onInvalido;
    private final EjecutarAnimacion ejecutor;
    private Pane origen;
    private List<StackPane> grupo;
    private List<Double> y0;
    private Point2D delta;
    private final Pane capaFlotante;
    private List<Double> offsetYOverlay;
    

    // inicializa los recursos necesarios para arrastrar cartas
    public AnimacionArrastre(List<Pane> fundacion, List<Pane> columna, Pane descarte,
                             Map<StackPane, Carta> cartaPorNodo, Runnable onValido,
                             Runnable onInvalido, EjecutarAnimacion ejecutor, Pane capaFlotante) {
        this.fundacion = fundacion;
        this.columna = columna;
        this.descarte = descarte;
        this.cartaPorNodo = cartaPorNodo;
        this.onValido = onValido;
        this.onInvalido = onInvalido;
        this.ejecutor = ejecutor;
        this.capaFlotante = capaFlotante;
    }

    // registra una carta para permitir arrastre
    public void registrarCartaComoArrastrable(StackPane carta) {
        carta.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> iniciarArrastreDeCarta(carta, e));
        carta.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::actualizarPosicionDuranteArrastre);
        carta.addEventFilter(MouseEvent.MOUSE_RELEASED, this::finalizarArrastreYSoltarCarta);
    }

    // inicia el arrastre de la carta seleccionada
    private void iniciarArrastreDeCarta(StackPane carta, MouseEvent e) {
        origen = (Pane) carta.getParent();
        if (origen == null) {
            grupo = List.of();
            return;
        }

        Carta cv = cartaPorNodo.get(carta);
        int idx = origen.getChildren().indexOf(carta);

        if (columna.contains(origen)) {
            if (!cv.caraArriba) {
                grupo = List.of();
                return;
            }
            grupo = new ArrayList<>();
            for (int i = idx; i < origen.getChildren().size(); i++) {
                StackPane n = (StackPane) origen.getChildren().get(i);
                Carta c = cartaPorNodo.get(n);
                if (!c.caraArriba) {
                    break;
                }
                grupo.add(n);
            }
        } else if (origen == descarte) {
            if (idx != origen.getChildren().size() - 1 || !cv.caraArriba) {
                grupo = List.of();
                return;
            }
            grupo = new ArrayList<>(List.of(carta));
        } else {
            grupo = List.of();
        }

        y0 = new ArrayList<>();
        for (StackPane n : grupo) {
            y0.add(n.getTranslateY());
        }

        Point2D p = carta.localToScene(0, 0);
        delta = new Point2D(e.getSceneX() - p.getX(), e.getSceneY() - p.getY());

        for (StackPane n : grupo) {
            n.toFront();
        }

        mostrarOverlayDeArrastre();
    }

    // actualiza la posicion mientras se arrastra
    private void actualizarPosicionDuranteArrastre(MouseEvent e) {
        if (origen == null || grupo == null || grupo.isEmpty()) {
            return;
        }

        Point2D p = capaFlotante.sceneToLocal(e.getSceneX() - delta.getX(), e.getSceneY() - delta.getY());
        for (int i = 0; i < grupo.size(); i++) {
            StackPane n = grupo.get(i);
            n.setLayoutX(p.getX());
            n.setLayoutY(p.getY() + offsetYOverlay.get(i));
        }
    }

    // finaliza el arrastre y suelta la carta
    private void finalizarArrastreYSoltarCarta(MouseEvent e) {
        if (origen == null || grupo == null || grupo.isEmpty()) {
            return;
        }

        Pane destino = encontrarDestinoPorCoordenadasPantalla(e.getSceneX(), e.getSceneY());
        boolean ok = ejecutor.ejecutar(origen, grupo, destino);

        if (!ok) {
            regresarCartaAPosicionOriginal();
            onInvalido.run();
        } else {
            limpiarOverlayDeArrastre();
            onValido.run();
        }

        origen = null;
        grupo = null;
        y0 = null;
        offsetYOverlay = null;
        delta = null;
    }

    // muestra el overlay visual para el arrastre
    private void mostrarOverlayDeArrastre() {
        List<Point2D> coords = new ArrayList<>();
        offsetYOverlay = new ArrayList<>();
        double baseY = 0;

        for (int i = 0; i < grupo.size(); i++) {
            StackPane n = grupo.get(i);
            Point2D s = n.localToScene(0, 0);
            Point2D o = capaFlotante.sceneToLocal(s);
            coords.add(o);
            if (i == 0) {
                baseY = o.getY();
            }
            offsetYOverlay.add(o.getY() - baseY);
        }

        for (StackPane n : grupo) {
            origen.getChildren().remove(n);
        }

        for (int i = 0; i < grupo.size(); i++) {
            StackPane n = grupo.get(i);
            Point2D o = coords.get(i);
            n.setTranslateX(0);
            n.setTranslateY(0);
            n.setLayoutX(o.getX());
            n.setLayoutY(o.getY());
            capaFlotante.getChildren().add(n);
            n.toFront();
        }
    }

    // regresa la carta a su posicion original
    private void regresarCartaAPosicionOriginal() {
        for (int i = 0; i < grupo.size(); i++) {
            StackPane n = grupo.get(i);
            capaFlotante.getChildren().remove(n);
            n.setLayoutX(0);
            n.setLayoutY(0);
            n.setTranslateX(0);
            n.setTranslateY(y0.get(i));
            origen.getChildren().add(n);
        }
    }

    // limpia el overlay usado durante el arrastre
    private void limpiarOverlayDeArrastre() {
        for (StackPane n : new ArrayList<>(grupo)) {
            capaFlotante.getChildren().remove(n);
        }
    }

    // encuentra el destino segun coordenadas en pantalla
    private Pane encontrarDestinoPorCoordenadasPantalla(double sx, double sy) {
        List<Pane> destinos = new ArrayList<>();
        destinos.addAll(fundacion);
        destinos.addAll(columna);
        destinos.add(descarte);

        for (Pane p : destinos) {
            Point2D a = p.localToScene(0, 0);
            boolean dentroX = sx >= a.getX() && sx <= a.getX() + p.getWidth();
            boolean dentroY = sy >= a.getY() && sy <= a.getY() + p.getHeight();
            if (dentroX && dentroY) {
                return p;
            }
        }
        return null;
    }
}