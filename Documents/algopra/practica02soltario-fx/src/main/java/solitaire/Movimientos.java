package solitaire;

import DeckOfCards.CartaInglesa;

// registro de un movimiento para poder deshacerlo
// 1 = draw
// 2 = reload
// 3 = waste a tableau
// 4 = waste a foundation
// 5 = tableau a tableau
// 6 = tableau a foundation
public class Movimientos {

    public static final int DRAW = 1;
    public static final int RELOAD = 2;
    public static final int WASTE_A_TABLEAU = 3;
    public static final int WASTE_A_FOUNDATION = 4;
    public static final int TABLEAU_A_TABLEAU = 5;
    public static final int TABLEAU_A_FOUNDATION = 6;

    // datos del movimiento
    private final int tipo;
    private final int fromIndex;
    private final int toIndex;
    private final int cantidad;
    private final boolean volteoAutomatico;
    private final int foundationIndex;
    private final Pila<CartaInglesa> block;

    // crea un registro de movimiento para soporte de undo
    public Movimientos(
            int tipo,
            int fromIndex,
            int toIndex,
            int cantidad,
            boolean volteoAutomatico,
            int foundationIndex,
            Pila<CartaInglesa> block
    ) {
        this.tipo = tipo;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cantidad = cantidad;
        this.volteoAutomatico = volteoAutomatico;
        this.foundationIndex = foundationIndex;
        this.block = block;
    }

    // devuelve el tipo de movimiento registrado
    public int getTipo() {
        return tipo;
    }

    // devuelve el indice de la columna origen
    public int getFromIndex() {
        return fromIndex;
    }

    // devuelve el indice de la columna destino
    public int getToIndex() {
        return toIndex;
    }

    // devuelve la cantidad de cartas movidas
    public int getCantidad() {
        return cantidad;
    }

    // indica si hubo volteo automatico de carta
    public boolean isVolteoAutomatico() {
        return volteoAutomatico;
    }

    // devuelve el indice de la foundation afectada
    public int getFoundationIndex() {
        return foundationIndex;
    }

    // devuelve el bloque original de cartas bottom a top
    public Pila<CartaInglesa> getBlock() {
        return block;
    }
}