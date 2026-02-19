/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import DeckOfCards.CartaInglesa;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.*;
import solitaire.FoundationDeck;
import solitaire.SolitaireGame;
import solitaire.TableauDeck;

public class ControladorJuego {
    private final StackPane vistaMazo;
    private final Pane descarte;
    private final List<Pane> fundacion;
    private final List<Pane> columna;
    private final Runnable onVictoria;
    private final Map<StackPane, Carta> cartaPorNodo = new HashMap<>();
    private final double pasoSolape = 32;
    private final double margenSuperior = 12;
    private boolean victoriaMostrada = false;
    private SolitaireGame juego;
    private final AnimacionArrastre arrastre;
    private final Pane capaArrastre;


    public ControladorJuego(StackPane vistaMazo, Pane descarte,
                            List<Pane> fundacion,
                            List<Pane> columna, Runnable onVictoria,
                            Pane capaArrastre) {
        this.vistaMazo = vistaMazo;
        this.descarte = descarte;
        this.fundacion = fundacion;
        this.columna = columna;
        this.onVictoria = onVictoria;
        this.arrastre = new AnimacionArrastre(fundacion, columna, descarte,
                cartaPorNodo,
                this::reproducirSonidoDeMovimientoValido,
                this::reproducirSonidoDeMovimientoInvalido,
                this::intentarMovimiento,capaArrastre);
        this.capaArrastre= capaArrastre;
    }

    // Prepara el estado de una nueva partida
    public void prepararNuevaPartida() {
        victoriaMostrada = false;
        juego = new SolitaireGame();
        limpiarTablero();
        actualizarVistaDelJuego();
    }

    // Ejecuta la accion cuando se usa el mazo al robar
    public void manejarAccionDelMazoDeRobo() {
        if (juego.getDrawPile().hayCartas()) {
            juego.drawCards();
        } else {
            juego.reloadDrawPile();
        }
        refrescarWaste();
    }
    // Limpia los nodos visuales del tablero
    private void limpiarTablero() {
        for (Pane b : fundacion) {
            b.getChildren().clear();
        }
        for (Pane c : columna) {
            c.getChildren().clear();
        }
        descarte.getChildren().clear();
        cartaPorNodo.clear();
    }

    // Sincroniza la vista con el estado del juego
    private void actualizarVistaDelJuego() {
        List<List<CartaInglesa>> t = new ArrayList<>();
        for (TableauDeck td : juego.getTableau()) {
            t.add(td.getCards());
        }
        for (int i = 0; i < columna.size(); i++) {
            Pane cont = columna.get(i);
            cont.getChildren().clear();
            if (i < t.size()) {
                List<CartaInglesa> cartas = t.get(i);
                for (CartaInglesa ci : cartas) {
                    StackPane nodo = crearNodoVisualDeCarta(ci);
                    cont.getChildren().add(nodo);
                }
            }
            organizarCartas(cont);
        }
        for (Pane f : fundacion) {
            f.getChildren().clear();
        }
        refrescarWaste();
    }

    // Actualiza las cartas en waste
    private void refrescarWaste() {
        descarte.getChildren().clear();
        CartaInglesa w = juego.getWastePile().verCarta();
        if (w != null) {
            StackPane nodo = crearNodoVisualDeCarta(w);
            descarte.getChildren().add(nodo);
            organizarCartas(descarte);
        }
    }

    // Crea el StackPane visual para una carta
    private StackPane crearNodoVisualDeCarta(CartaInglesa ci) {
        StackPane n = new StackPane();
        n.setPrefSize(100, 140);
        String figura = ci.getPalo().getFigura();
        String figuraNormal = figura.replace("\uFE0E", "")
                .replace("\uFE0F", "")
                .replace("❤", "♥");
        String rango = convertirValorARangoDeCarta(ci.getValor());
        Carta c = new Carta(n, rango, figuraNormal, ci.getValor(), "rojo"
                .equals(ci.getPalo().getColor()), ci.isFaceup());
        cartaPorNodo.put(n, c);
        c.actualizarImagenSegunEstadoDeCarta();
        arrastre.registrarCartaComoArrastrable(n);
        return n;
    }

    // Convierte valor numerico a rango de carta
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
    // Reacomoda visualmente cartas de fundacion
    private void organizarCartas(Pane pila) {
        boolean base = fundacion.contains(pila) || pila == descarte;
        double y = base ? 0 : margenSuperior;
        double paso = base ? 0 : pasoSolape;
        for (var n : pila.getChildren()) {
            n.setTranslateX(0);
            n.setTranslateY(y);
            y += paso;
        }
    }
    // Intenta un movimiento validandolo
    private boolean intentarMovimiento(Pane origen, List<StackPane> grupo,
                                       Pane destino) {
        if (destino == null) {
            return false;
        }
        if (origen == descarte) {
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveWasteToTableau(d + 1);
                if (ok) {
                    mostrarMovimientoEntreColumnas(origen, destino);
                    refrescarWaste();
                    verificarCondicionDeVictoria();
                }
                return ok;
            }
            if (fundacion.contains(destino)) {
                boolean ok = juego.moveWasteToFoundation();
                if (ok) {
                    actualizarFundacion();
                    refrescarWaste();
                    verificarCondicionDeVictoria();
                }
                return ok;
            }
            return false;
        }
        if (columna.contains(origen)) {
            int o = columna.indexOf(origen);
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveTableauToTableau(
                        o + 1, d + 1);
                if (ok) {
                    mostrarMovimientoEntreColumnas(origen, destino);
                    verificarCondicionDeVictoria();
                }
                return ok;
            }
            if (fundacion.contains(destino)) {
                if (grupo.size() != 1) {
                    return false;
                }
                boolean ok = juego.moveTableauToFoundation(o + 1);
                if (ok) {
                    mostarColumnaTableauPorIndice(o);
                    actualizarFundacion();
                    verificarCondicionDeVictoria();
                }
                return ok;
            }
            return false;
        }
        return false;
    }
    // Muestra el cambio entre columnas tableau
    private void mostrarMovimientoEntreColumnas(Pane origen, Pane destino) {
        if (columna.contains(origen)) {
            int i = columna.indexOf(origen);
            mostarColumnaTableauPorIndice(i);
        }
        if (columna.contains(destino)) {
            int j = columna.indexOf(destino);
            mostarColumnaTableauPorIndice(j);
        }
    }
    // Muestra solo una columna segun su indice
    private void mostarColumnaTableauPorIndice(int i) {
        Pane cont = columna.get(i);
        cont.getChildren().clear();
        List<CartaInglesa> cartas = juego.getTableau().get(i).getCards();
        for (CartaInglesa ci : cartas) {
            StackPane nodo = crearNodoVisualDeCarta(ci);
            cont.getChildren().add(nodo);
        }
        organizarCartas(cont);
    }
    // Actualiza la vista de las fundaciones
    private void actualizarFundacion() {
        FoundationDeck fd = juego.getLastFoundationUpdated();
        if (fd == null) {
            return;
        }
        CartaInglesa top = fd.getUltimaCarta();
        if (top == null) {
            return;
        }
        int idx = top.getPalo().ordinal();
        Pane base = fundacion.get(idx);
        base.getChildren().clear();
        StackPane nodo = crearNodoVisualDeCarta(top);
        base.getChildren().add(nodo);
        organizarCartas(base);
    }
    // Aplica un efecto de sonido cuando el movimiento es valido
    private void reproducirSonidoDeMovimientoValido() {
        RecursosAudioMedia.reproducirEfectoDeSonido("sonido_de_aprobacion");
    }
    // Aplica un efecto de sonido cuando el movimiento es invalido
    private void reproducirSonidoDeMovimientoInvalido() {
        RecursosAudioMedia.reproducirEfectoDeSonido("sonido_de_error");
    }

    // Verifica si ya se cumplio la victoria
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
}
