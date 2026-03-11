package solitaire;

import DeckOfCards.CartaInglesa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modela un monticulo donde se ponen las cartas
 * de por valor, alternando el color.
 *
 * @author Cecilia M. Curlango
 * @version 2025
 */
public class TableauDeck {
    private final Pila<CartaInglesa> cartas = new Pila<>(256);

    /**
     * Carga las cartas iniciales y voltea la ultima.
     *
     * @param cartas iniciales
     */
    public void inicializar(Pila<CartaInglesa> bloque) {
        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) aux.push(c);
        CartaInglesa ultima = null;
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            ultima = c;
            cartas.push(c);
        }
        if (ultima != null) ultima.makeFaceUp();
    }

    // Itera bottom->top sin modificar la pila real
    public void forEachBottomTop(Consumer<CartaInglesa> consumer) {
        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) aux.push(c);
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            consumer.accept(c);
            cartas.push(c);
        }
    }

    public List<CartaInglesa> getCards() {
        ArrayList<CartaInglesa> list = new ArrayList<>();
        forEachBottomTop(list::add);
        return list;
    }

    public CartaInglesa viewCardStartingAt(int value) {
    Pila<CartaInglesa> aux = new Pila<>(256);
    CartaInglesa encontrada = null;
    for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
        aux.push(c);
        if (encontrada == null && c.isFaceup() && c.getValor() == value) {
            encontrada = c;
        }
    }
    for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
        cartas.push(c);
    }
    return encontrada;
    }

    /**
     * Remove cards starting from the one with a specified value.
     *
     * @param value of starting card to remove
     * @return removed cards or empty ArrayList if it is not possible to remove.
     */
    public Pila<CartaInglesa> removeStartingAt(int value) {
        Pila<CartaInglesa> auxTop = new Pila<>(256);
        Pila<CartaInglesa> bloque = new Pila<>(256);
        boolean found = false;

        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
            auxTop.push(c);
            if (c.isFaceup() && c.getValor() == value) {
                found = true;
                break;
            }
        }
        if (!found) {
            for (CartaInglesa c = auxTop.pop(); c != null; c = auxTop.pop()) cartas.push(c);
            return null;
        }
        for (CartaInglesa c = auxTop.pop(); c != null; c = auxTop.pop()) bloque.push(c);
        return bloque;
    }

    /**
     * Agrega una carta volteada al monticulo. Solo la agrega si:
     * A) es la siguiente carta en la secuencia
     * B) esta vacio y la carta es un Rey
     *
     * @param carta que se intenta almancenar
     * @return true si se pudo guardar la carta, false si no
     */
    public boolean agregarCarta(CartaInglesa carta) {
        if (carta == null) return false;
        if (!sePuedeAgregarCarta(carta)) return false;
        carta.makeFaceUp();
        cartas.push(carta);
        return true;
    }

    /**
     * Agrega un bloque de cartas al Tableau si la primera carta de las cartas recibidas
     * es de color alterno a la ultima carta del tableau y tambien es la siguiente.
     *
     * @param cartasRecibidas
     * @return true si se pudo agregar el bloque, false si no
     */
    public boolean agregarBloqueDeCartas(Pila<CartaInglesa> cartasRecibidas) {
        if (cartasRecibidas == null || cartasRecibidas.peek() == null) return false;

        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = cartasRecibidas.pop(); c != null; c = cartasRecibidas.pop()) aux.push(c);
        CartaInglesa primera = aux.peek();

        boolean puede = sePuedeAgregarCarta(primera);
        if (!puede) {
            for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) cartasRecibidas.push(c);
            return false;
        }

        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceUp();
            cartas.push(c);
        }
        return true;
    }

    // Reponer bloque bottom->top en este tableau sin invertir el orden
    // No consume el bloque lo restaura como llego
    public boolean reponerBloque(Pila<CartaInglesa> bloque) {
        if (bloque == null || bloque.peek() == null) return false;

        Pila<CartaInglesa> aux = new Pila<>(512);
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) aux.push(c);

        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
            this.cartas.push(c);
        }
        return true;
    }

    /**
     * Obtener la ultima carta del monticulo sin removerla
     *
     * @return la carta que esta al final, null si estaba vacio
     */
    public CartaInglesa verUltimaCarta() {
        return cartas.peek();
    }

    /**
     * Remover la ultima carta del monticulo.
     *
     * @return la carta que removio, null si estaba vacio
     */
    public CartaInglesa removerUltimaCarta() {
        return cartas.pop();
    }

    /**
     * Indica si esta vacio  el Tableau
     *
     * @return true si no tiene cartas restantes, false si tiene cartas.
     */
    public boolean isEmpty() {
        return cartas.peek() == null;
    }

    /**
     * Verifica si la carta que recibe puede ser la siguiente del tableau actual.
     *
     * @param cartaInicialDePrueba
     * @return true si puede ser la siguiente, false si no
     */
    public boolean sePuedeAgregarCarta(CartaInglesa cartaInicialDePrueba) {
        if (cartaInicialDePrueba == null) return false;
        CartaInglesa ultima = cartas.peek();
        if (ultima == null) {
            return cartaInicialDePrueba.getValor() == 13;
        }
        boolean alterno = !ultima.getColor().equals(cartaInicialDePrueba.getColor());
        boolean secuencia = ultima.getValor() == cartaInicialDePrueba.getValor() + 1;
        return alterno && secuencia;
    }

    /**
     * Obtiene la ultima carta del Tableau sin removerla.
     * @return ultima carta, null si no hay cartas
     */
    public CartaInglesa getUltimaCarta() {
        return cartas.peek();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<CartaInglesa> snap = getCards();
        if (snap.isEmpty()) sb.append("---");
        else for (CartaInglesa c : snap)
            sb.append(c);
        return sb.toString();
    }
}