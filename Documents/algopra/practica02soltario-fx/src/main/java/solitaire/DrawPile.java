package solitaire;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Mazo;

/**
 * Modela un mazo de cartas de solitario.
 * @author Cecilia Curlango
 * @version 2025
 */
public class DrawPile {
    private final Pila<CartaInglesa> cartas;
    private int cuantasCartasSeEntregan = 1;

    public DrawPile() {
        this.cartas = new Pila<CartaInglesa>(128);

        Mazo mazo = new Mazo();
        for (int i = mazo.getCartas().size() - 1; i >= 0; i--) {
            this.cartas.push(mazo.getCartas().get(i));
        }
        setCuantasCartasSeEntregan(1);
    }

    /**
     * Establece cuantas cartas se sacan cada vez.
     * Puede ser 1 o 3 normalmente.
     * @param cuantasCartasSeEntregan
     */
    public void setCuantasCartasSeEntregan(int n) {
        this.cuantasCartasSeEntregan = cuantasCartasSeEntregan;
    }

    /**
     * Regresa la cantidad de cartas que se sacan cada vez.
     * @return cantidad de cartas que se entregan
     */
    public int getCuantasCartasSeEntregan() {
        return cuantasCartasSeEntregan;
    }

    /**
     * Retirar una cantidad de cartas. Este método se utiliza al inicio
     * de una partida para cargar las cartas de los tableaus.
     * Si se tratan de remover más cartas de las que hay,
     * se provocará un error.
     * @param cantidad de cartas que se quieren a retirar
     * @return cartas retiradas
     */
    public Pila<CartaInglesa> getCartas(int cantidad) {
        Pila<CartaInglesa> aux = new Pila<>(64);
        Pila<CartaInglesa> bloque = new Pila<>(64);
        for (int i = 0; i < cantidad; i++) {
            CartaInglesa c = cartas.pop();
            if (c == null) break;
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }
        return bloque;
    }

    /**
     * Retira y entrega las cartas del monton. La cantidad que retira
     * depende de cuántas cartas quedan en el montón y serán hasta el máximo
     * que se configuró inicialmente.
     * @return Cartas retiradas.
     */
    public Pila<CartaInglesa> retirarCartas() {
        Pila<CartaInglesa> aux = new Pila<>(8);
        Pila<CartaInglesa> bloque = new Pila<>(8);
        int maximo = cuantasCartasSeEntregan;
        for (int i = 0; i < maximo; i++) {
            CartaInglesa c = cartas.pop();
            if (c == null) break;
            c.makeFaceUp();
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }
        return bloque;
    }

    /**
     * Indica si aún quedan cartas para entregar.
     * @return true si hay cartas, false si no.
     */
    public boolean hayCartas() {
        return cartas.peek() != null;
    }

    public CartaInglesa verCarta() {
        return cartas.peek();
    }

    /**
     * Agrega las cartas recibidas al monton y las voltea
     * para que no se vean las caras.
     * @param cartasAgregar cartas que se agregan
     */
    public void recargar(Pila<CartaInglesa> cartasAgregar) {
        while (cartas.pop() != null) { /* discard */ }
        if (cartasAgregar == null || cartasAgregar.peek() == null) return;

        Pila<CartaInglesa> aux = new Pila<>(128);
        for (CartaInglesa c = cartasAgregar.pop(); c != null; c = cartasAgregar.pop()) {
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceDown();
            cartas.push(c);
        }
    }

    // Apila un bloque encima del mazo sin reemplazarlo
    // Se usa en undo de DRAW
    // Las cartas quedan face-down
    public void pushBlockOnTop(Pila<CartaInglesa> bloque) {
        if (bloque == null || bloque.peek() == null) return;
        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) {
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceDown();
            cartas.push(c);
        }
    }

    // Empuja UNA carta arriba del mazo face-down preservando el orden correcto
    public void pushCardOnTop(CartaInglesa c) {
        if (c != null) {
            c.makeFaceDown();
            cartas.push(c);
        }
    }

    @Override
    public String toString() {
        return hayCartas() ? "@" : "-E-";
    }
}