import DeckOfCards.CartaInglesa;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.*;
import javafx.scene.Node;
import solitaire.FoundationDeck;
import solitaire.SolitaireGame;
import solitaire.TableauDeck;

// controla la creacion y los movimientos entre la interfaz y el juego
public class ControladorJuego {

    // referencias a contenedores visuales
    StackPane vistaMazo;
    Pane descarte;
    List<Pane> fundacion;
    List<Pane> columna;

    // callback cuando se gana
    private final Runnable onVictoria;

    // mapa ui a modelo visual de carta para arrastre
    private final Map<StackPane, Carta> cartaPorNodo = new HashMap<>();

    // parametros de layout
    private final double pasoSolape = 32;
    private final double margenSuperior = 12;

    // estado
    private boolean victoriaMostrada = false;
    private SolitaireGame juego;

    // soporte de arrastre y capa overlay
    private final AnimacionArrastre arrastre;
    private final Pane capaArrastre;

    // crea el controlador y registra callbacks de arrastre
    public ControladorJuego(StackPane vistaMazo, Pane descarte, List<Pane> fundacion, List<Pane> columna,
                            Runnable onVictoria, Pane capaArrastre) {
        this.vistaMazo = vistaMazo;
        this.descarte = descarte;
        this.fundacion = fundacion;
        this.columna = columna;
        this.onVictoria = onVictoria;
        this.capaArrastre = capaArrastre;

        this.arrastre = new AnimacionArrastre(
                fundacion,
                columna,
                descarte,
                cartaPorNodo,
                this::manejarMovimientoValido,
                this::manejarMovimientoInvalido,
                this::intentarMovimientoConMotorDeReglas,
                this.capaArrastre
        );
    }

    // prepara el estado inicial de una nueva partida
    public void inicializarNuevaPartida() {
        victoriaMostrada = false;
        juego = new SolitaireGame();
        limpiarTableroVisual();
        renderizarEstadoDesdeMotor();
    }

    // ejecuta la accion cuando se usa el mazo
    public void manejarAccionDelMazo() {
        if (juego.getDrawPile().hayCartas()) {
            juego.drawCards();
        } else {
            juego.reloadDrawPile();
        }
        actualizarWastePileVisual();
    }

    // limpia los nodos visuales del tablero
    private void limpiarTableroVisual() {
        for (Pane b : fundacion) {
            desregistrarCartasDeContenedor(b);
            b.getChildren().clear();
        }
        for (Pane c : columna) {
            desregistrarCartasDeContenedor(c);
            c.getChildren().clear();
        }
        desregistrarCartasDeContenedor(descarte);
        descarte.getChildren().clear();
        cartaPorNodo.clear();
    }

    // dibuja el estado actual usando el motor del juego
    private void renderizarEstadoDesdeMotor() {
        if (juego == null) {
            return;
        }

        cartaPorNodo.clear();

        TableauDeck[] tds = juego.getTableau();
        for (int i = 0; i < columna.size(); i++) {
            Pane cont = columna.get(i);
            desregistrarCartasDeContenedor(cont);
            cont.getChildren().clear();

            if (i < tds.length) {
                var cartas = tds[i].getCards();
                for (CartaInglesa ci : cartas) {
                    StackPane nodo = crearNodoVisualDeCarta(ci);
                    cont.getChildren().add(nodo);
                }
            }

            organizarCartasEnPilaVisual(cont);
        }

        actualizarFundacionesVisuales();
        actualizarWastePileVisual();
    }

    // actualiza la vista de las fundaciones
    private void actualizarFundacionesVisuales() {
        var fds = juego.getFoundations();

        for (int i = 0; i < fundacion.size() && i < fds.length; i++) {
            Pane base = fundacion.get(i);
            desregistrarCartasDeContenedor(base);
            base.getChildren().clear();

            var fd = fds[i];
            CartaInglesa top = (fd != null) ? fd.getUltimaCarta() : null;

            if (top != null) {
                StackPane nodo = crearNodoVisualDeCarta(top);
                base.getChildren().add(nodo);
            }

            organizarCartasEnPilaVisual(base);
        }
    }

    // actualiza la pila waste en la vista
    private void actualizarWastePileVisual() {
        desregistrarCartasDeContenedor(descarte);
        descarte.getChildren().clear();

        CartaInglesa w = juego.getWastePile().verCarta();
        if (w != null) {
            StackPane nodo = crearNodoVisualDeCarta(w);
            descarte.getChildren().add(nodo);
            organizarCartasEnPilaVisual(descarte);
        }
    }

    // crea el stackpane visual para una carta
    private StackPane crearNodoVisualDeCarta(CartaInglesa ci) {
        StackPane n = new StackPane();
        n.setPrefSize(100, 140);

        String figura = ci.getPalo().getFigura();
        String figuraNormal = figura.replace("\uFE0E", "").replace("\uFE0F", "").replace("❤", "♥");
        String rango = convertirValorARangoDeCarta(ci.getValor());

        boolean esRojo = "rojo".equals(ci.getPalo().getColor());

        Carta c = new Carta(n, rango, figuraNormal, ci.getValor(), esRojo, ci.isFaceup());
        cartaPorNodo.put(n, c);
        c.actualizarImagenSegunEstadoDeCarta();

        arrastre.registrarCartaComoArrastrable(n);
        return n;
    }

    // convierte valor numerico a rango de carta
    private String convertirValorARangoDeCarta(int v) {
        if (v == 14) {
            return "A";
        }
        if (v == 11) {
            return "J";
        }
        if (v == 12) {
            return "Q";
        }
        if (v == 13) {
            return "K";
        }
        return String.valueOf(v);
    }

    // reacomoda visualmente cartas dentro de una pila
    private void organizarCartasEnPilaVisual(Pane pila) {
        boolean base = fundacion.contains(pila) || pila == descarte;
        double y = base ? 0 : margenSuperior;
        double paso = base ? 0 : pasoSolape;

        for (var n : new ArrayList<>(pila.getChildren())) {
            n.setTranslateX(0);
            n.setTranslateY(y);
            y += paso;
        }
    }

    // intenta un movimiento validandolo con el motor
    private boolean intentarMovimientoConMotorDeReglas(Pane origen, List<StackPane> grupo, Pane destino) {
        if (destino == null) {
            return false;
        }

        if (origen == descarte) {
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveWasteToTableau(d + 1);
                if (ok) {
                    renderizarMovimientoEntreColumnas(origen, destino);
                    actualizarWastePileVisual();
                    verificarCondicionDeVictoria();
                } else {
                    manejarMovimientoInvalido();
                }
                return ok;
            }

            if (fundacion.contains(destino)) {
                boolean ok = juego.moveWasteToFoundation();
                if (ok) {
                    actualizarFundacionVisualActualizada();
                    actualizarWastePileVisual();
                    verificarCondicionDeVictoria();
                } else {
                    manejarMovimientoInvalido();
                }
                return ok;
            }

            return false;
        }

        if (columna.contains(origen)) {
            int o = columna.indexOf(origen);

            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveTableauToTableau(o + 1, d + 1);
                if (ok) {
                    renderizarMovimientoEntreColumnas(origen, destino);
                    verificarCondicionDeVictoria();
                } else {
                    manejarMovimientoInvalido();
                }
                return ok;
            }

            if (fundacion.contains(destino)) {
                if (grupo.size() != 1) {
                    manejarMovimientoInvalido();
                    return false;
                }

                boolean ok = juego.moveTableauToFoundation(o + 1);
                if (ok) {
                    renderizarColumnaTableauPorIndice(o);
                    actualizarFundacionVisualActualizada();
                    verificarCondicionDeVictoria();
                } else {
                    manejarMovimientoInvalido();
                }
                return ok;
            }

            return false;
        }

        return false;
    }

    // renderiza el cambio entre columnas
    private void renderizarMovimientoEntreColumnas(Pane origen, Pane destino) {
        if (columna.contains(origen)) {
            int i = columna.indexOf(origen);
            renderizarColumnaTableauPorIndice(i);
        }

        if (columna.contains(destino)) {
            int j = columna.indexOf(destino);
            renderizarColumnaTableauPorIndice(j);
        }
    }

    // renderiza solo una columna segun su indice
    private void renderizarColumnaTableauPorIndice(int i) {
        if (i < 0 || i >= columna.size()) {
            return;
        }

        Pane cont = columna.get(i);
        desregistrarCartasDeContenedor(cont);
        cont.getChildren().clear();

        TableauDeck[] tds = juego.getTableau();
        if (i >= 0 && i < tds.length) {
            List<CartaInglesa> cartas = tds[i].getCards();
            for (CartaInglesa ci : cartas) {
                StackPane nodo = crearNodoVisualDeCarta(ci);
                cont.getChildren().add(nodo);
            }
        }

        organizarCartasEnPilaVisual(cont);
    }

    // actualiza la fundacion cuyo top cambio en el motor
    private void actualizarFundacionVisualActualizada() {
        FoundationDeck fd = juego.getLastFoundationUpdated();
        if (fd == null) {
            return;
        }

        CartaInglesa top = fd.getUltimaCarta();
        if (top == null) {
            return;
        }

        int idx = top.getPalo().ordinal();
        if (idx < 0 || idx >= fundacion.size()) {
            return;
        }

        Pane base = fundacion.get(idx);
        desregistrarCartasDeContenedor(base);
        base.getChildren().clear();

        StackPane nodo = crearNodoVisualDeCarta(top);
        base.getChildren().add(nodo);
        organizarCartasEnPilaVisual(base);
    }

    // aplica efectos cuando el movimiento es valido
    private void manejarMovimientoValido() {
        try {
            RecursosAudioMedia.reproducirEfectoDeSonido("sonido_de_aprobacion");
        } catch (Throwable ignored) {
        }
    }

    // aplica efectos cuando el movimiento es invalido
    private void manejarMovimientoInvalido() {
        try {
            RecursosAudioMedia.reproducirEfectoDeSonido("sonido_de_error");
        } catch (Throwable ignored) {
        }
        renderizarEstadoDesdeMotor();
    }

    // verifica si ya se cumplio la victoria
    private void verificarCondicionDeVictoria() {
        if (victoriaMostrada) {
            return;
        }

        boolean ok = juego.isGameOver();
        if (ok) {
            victoriaMostrada = true;
            onVictoria.run();
        }
    }

    // devuelve la instancia actual del juego
    public SolitaireGame obtenerInstanciaJuego() {
        return juego;
    }

    // actualiza la interfaz grafica segun el estado del juego
    public void actualizarInterfazGrafica() {
        renderizarEstadoDesdeMotor();
    }

    // saca del mapa las cartas asociadas a un contenedor
    private void desregistrarCartasDeContenedor(Pane cont) {
        List<Node> hijos = new ArrayList<>(cont.getChildren());
        for (Node node : hijos) {
            if (node instanceof StackPane) {
                StackPane sp = (StackPane) node;
                cartaPorNodo.remove(sp);
            }
        }
    }
}