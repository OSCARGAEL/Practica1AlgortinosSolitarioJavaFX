package solitaire;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Palo;

/**
 * Modela un moniculo donde se ponen las cartas
 * de un solo palo.
 *
 * @author Cecilia M. Curlango
 * @version 2025
 */
public class FoundationDeck {
    private final Palo palo;
    private final Pila<CartaInglesa> cartas = new Pila<>(16);

    public FoundationDeck(Palo palo) {
        this.palo = palo;
    }

    /**
     * Agrega una carta al monticulo. Solo la agrega si
     * la carta es del palo del monticulo y el la siguiente
     * carta en la secuencia.
     *
     * @param carta que se intenta almancenar
     * @return true si se pudo guardar la carta false si no
     */
    public boolean agregarCarta(CartaInglesa carta) {
        if (carta == null)
            return false;
        if (!carta.tieneElMismoPalo(palo))
            return false;

        CartaInglesa ultima = getUltimaCarta();
        if (ultima == null) {
            if (carta.getValorBajo() != 1)
                return false;
        } else {
            if (ultima.getValorBajo() + 1 != carta.getValorBajo())
                return false;
        }
        carta.makeFaceUp();
        cartas.push(carta);
        return true;
    }

    /**
     * Remover la ultima carta del monticulo.
     *
     * @return la carta que removio null si estaba vacio
     */
    public CartaInglesa removerUltimaCarta() {
        return cartas.pop();
    }

    /**
     * Determina si hay cartas en el Foundation.
     * @return true hay al menos una carta false no hay cartas
     */
    public boolean estaVacio() {
        return cartas.peek() == null;
    }

    /**
     * Obtiene la ultima carta del Foundation sin removerla.
     * @return ultima carta null si no hay cartas
     */
    public CartaInglesa getUltimaCarta() {
        return cartas.peek();
    }

    // Representacion lineal de la foundation
    @Override
    public String toString() {
        if (cartas.peek() == null)
            return "---";

        Pila<CartaInglesa> aux1 = new Pila<>(32);
        Pila<CartaInglesa> aux2 = new Pila<>(32);
        StringBuilder sb = new StringBuilder();

        for (CartaInglesa c = cartas.pop();
             c != null; c = cartas.pop()) {
            aux1.push(c);
        }

        for (CartaInglesa c = aux1.pop();
             c != null; c = aux1.pop()) {
            sb.append(c.toString());
            aux2.push(c);
        }

        for (CartaInglesa c = aux2.pop();
             c != null; c = aux2.pop()) {
            cartas.push(c);
        }
        return sb.toString();
    }
}