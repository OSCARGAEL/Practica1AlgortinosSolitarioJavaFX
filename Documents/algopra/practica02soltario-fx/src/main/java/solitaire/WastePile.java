package solitaire;

import DeckOfCards.CartaInglesa;

/**
 * Modela el monticulo donde se colocan las cartas
 * que se extraen de Draw pile.
 *
 * @author Cecilia Curlango Rosas
 * @version 2025
 */
public class WastePile {

    // estructura LIFO interna
    private final Pila<CartaInglesa> cartas = new Pila<>(64);

    public void addCartas(Pila<CartaInglesa> nuevas) {
        if (nuevas == null || nuevas.peek() == null) return;

        Pila<CartaInglesa> aux = new Pila<>(128);
        for (CartaInglesa c = nuevas.pop(); c != null; c = nuevas.pop()) {
            aux.push(c);
        }

        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceUp();
            cartas.push(c);
        }
    }

    public Pila<CartaInglesa> emptyPile() {
        Pila<CartaInglesa> aux = new Pila<>(128);
        Pila<CartaInglesa> bloque = new Pila<>(128);

        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
            aux.push(c);
        }

        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }

        return bloque;
    }

    /**
     * Obtener la ultima carta sin removerla.
     * @return Carta que esta encima. Si esta vacia es null
     */
    public CartaInglesa verCarta() {
        return cartas.peek();
    }

    public CartaInglesa getCarta() {
        return cartas.pop();
    }

    public boolean hayCartas() {
        return cartas.peek() != null;
    }

    @Override
    public String toString() {
        CartaInglesa top = cartas.peek();
        return (top == null) ? "---" : top.toString();
    }
}