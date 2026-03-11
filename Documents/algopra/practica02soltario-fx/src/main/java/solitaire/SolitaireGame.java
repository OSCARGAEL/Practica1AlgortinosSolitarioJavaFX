package solitaire;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Palo;

public class SolitaireGame {
    // Estructuras base del juego
    private final TableauDeck[] tableau = new TableauDeck[7];
    private final FoundationDeck[] foundation = new FoundationDeck[Palo.values().length];

    // Ultima foundation actualizada (para UI)
    private FoundationDeck lastFoundationUpdated;

    // Monton para robar y waste
    private final DrawPile drawPile;
    private final WastePile wastePile;

    // Historial de movimientos y contador visible
    private final Pila<Movimientos> historial = new Pila<Movimientos>(512);
    private int moveCount = 0;

    // Clona bloque bottom->top sin consumir el original
    private Pila<CartaInglesa> clonarBottomTop(Pila<CartaInglesa> original) {
        Pila<CartaInglesa> aux = new Pila<CartaInglesa>(512);
        for (CartaInglesa c = original.pop();
                c != null;
                c = original.pop())
            aux.push(c);
        Pila<CartaInglesa> copia = new Pila<CartaInglesa>(512);
        for (CartaInglesa c = aux.pop();
                c != null;
                c = aux.pop()) {
            original.push(c); 
            copia.push(c); }
        return copia;
    }

    // Cuenta elementos de una pila sin modificarla
    private int tamanoPila(Pila<CartaInglesa> pila) {
        Pila<CartaInglesa> aux = clonarBottomTop(pila);
        int k = 0;
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) k++;
        return k;
    }


    // Valida que el numero de tableau este entre 1 y 7
    private boolean esIndiceTableauValido(int numero) {
        return numero >= 1 && numero <= tableau.length;
    }

    // Crea una partida nueva, reparte y tira primeras cartas al waste. 
    public SolitaireGame() {
        drawPile = new DrawPile();
        wastePile = new WastePile();
        createTableaux();
        createFoundations();
        // primer pase al waste
        wastePile.addCartas(drawPile.retirarCartas());
    }

    // Getters usados por la UI
    public DrawPile getDrawPile() {
        return drawPile; 
    }
    public TableauDeck[] getTableau() {
        return tableau; 
    }
    public WastePile getWastePile() {
        return wastePile; 
    }
    public FoundationDeck getLastFoundationUpdated() {
        return lastFoundationUpdated; 
    }
    public FoundationDeck[] getFoundations() {
        return foundation; 
    }
    public int getMoveCount() {
        return moveCount; 
    }

    // Recarga el mazo con todas las cartas del waste.
    public void reloadDrawPile() {
        int k = 0;
        while (true) {
            CartaInglesa c = wastePile.getCarta();
            if (c == null) break;
            drawPile.pushCardOnTop(c);
            k++;
        }
        if (k == 0) return;

        historial.push(new Movimientos(Movimientos.RELOAD, -1, -1, k, false, -1, null));
        moveCount++;
    }



    // Roba 1 o 3 cartas del mazo al waste (undoable).
    public void drawCards() {
        Pila<CartaInglesa> bloque = drawPile.retirarCartas();     // bottom->top
        Pila<CartaInglesa> copia = clonarBottomTop(bloque);
        int k = tamanoPila(copia);                                 // medir antes
        wastePile.addCartas(bloque);                               // a waste face-up
        if (k == 0) return;

        historial.push(new Movimientos(Movimientos.DRAW, -1, -1, k, false, -1, null));
        moveCount++;
    }

    // Mueve top del waste a un tableau si cumple reglas.
    public boolean moveWasteToTableau(int dest) {
        if (!esIndiceTableauValido(dest)) return false;
        TableauDeck d = tableau[dest - 1];
        CartaInglesa carta = wastePile.verCarta();
        if (carta != null && d.sePuedeAgregarCarta(carta)) {
            wastePile.getCarta();
            d.agregarCarta(carta);
            historial.push(new Movimientos(Movimientos.WASTE_A_TABLEAU, -1, dest - 1, 1, false, -1, null));

            moveCount++;
            return true;
        }
        return false;
    }

    // Mueve top del waste a su foundation si procede.
    public boolean moveWasteToFoundation() {
        CartaInglesa carta = wastePile.verCarta();
        if (carta == null) 
            return false;
        if (!puedeIrAFundacion(carta)) 
            return false;
        wastePile.getCarta();
        moveCartaToFoundation(carta);
        int fidx = carta.getPalo().ordinal();
        historial.push(new Movimientos(Movimientos.WASTE_A_FOUNDATION, -1, -1, 1, false, fidx, null));

        moveCount++;
        return true;
    }

    // Mueve un bloque desde un tableau a otro si cumple reglas.
    public boolean moveTableauToTableau(int origen, int destino) {
        if (!esIndiceTableauValido(origen) || !esIndiceTableauValido(destino) || origen == destino) return false;
        TableauDeck src = tableau[origen - 1];
        if (src.isEmpty()) 
            return false;
        TableauDeck dst = tableau[destino - 1];

        // valor que debe tener la carta inferior del bloque
        int needed = dst.isEmpty() ? 13 : (dst.verUltimaCarta().getValor() - 1);

        CartaInglesa start = src.viewCardStartingAt(needed);
        if (start == null || !dst.sePuedeAgregarCarta(start)) 
            return false;

        Pila<CartaInglesa> bloque = src.removeStartingAt(needed);
        if (bloque == null || bloque.peek() == null) 
            return false;

        // clonar bloque para undo
        Pila<CartaInglesa> copiaParaUndo = clonarBottomTop(bloque);
        int k = tamanoPila(copiaParaUndo);

        // ver si se voltea la nueva top del origen.
        boolean flipped = false;
        CartaInglesa nuevaTop = src.verUltimaCarta();
        if (nuevaTop != null && !nuevaTop.isFaceup()) 
            flipped = true;

        boolean agregado = dst.agregarBloqueDeCartas(bloque);
        if (!agregado) {
            // si no encaja, reponemos el bloque original.
            src.reponerBloque(copiaParaUndo);
            return false;
        }
        // si tocaba voltear, hacerlo ahora.
        if (flipped) {
            CartaInglesa t = src.verUltimaCarta();
            if (t != null && !t.isFaceup()) t.makeFaceUp();
        }

        historial.push(new Movimientos(Movimientos.TABLEAU_A_TABLEAU, origen - 1, destino - 1, k, flipped, -1, copiaParaUndo));
        moveCount++;
        return true;
    }

    // Mueve top de un tableau a su foundation si procede.
    public boolean moveTableauToFoundation(int numero) {
        if (!esIndiceTableauValido(numero)) return false;

        TableauDeck fuente = tableau[numero - 1];

        CartaInglesa vista = fuente.verUltimaCarta();
        if (vista == null) 
            return false;
        if (!puedeIrAFundacion(vista)) 
            return false;

        CartaInglesa carta = fuente.removerUltimaCarta();
        boolean flipped = false;
        CartaInglesa nuevaTop = fuente.verUltimaCarta();
        if (nuevaTop != null && !nuevaTop.isFaceup()) 
            flipped = true;

        if (!moveCartaToFoundation(carta)) {
            // restaurar si fallara (raro por la validacion previa)
            Pila<CartaInglesa> uno = new Pila<CartaInglesa>(4);
            uno.push(carta);
            fuente.reponerBloque(uno);
            return false;
        }
        if (flipped) {
            CartaInglesa t = fuente.verUltimaCarta();
            if (t != null && !t.isFaceup()) 
                t.makeFaceUp();
        }
        int fidx = carta.getPalo().ordinal();
        historial.push(new Movimientos(Movimientos.TABLEAU_A_FOUNDATION, numero - 1, -1, 1, flipped, fidx, null));

        moveCount++;
        return true;
    }

    // Agrega carta a su foundation y actualiza referencia para UI
    private boolean moveCartaToFoundation(CartaInglesa carta) {
        int idx = carta.getPalo().ordinal();
        FoundationDeck dest = foundation[idx];
        lastFoundationUpdated = dest;
        return dest.agregarCarta(carta);
    }

    // Valida si una carta puede ir a su foundation
    private boolean puedeIrAFundacion(CartaInglesa carta) {
        FoundationDeck fd = foundation[carta.getPalo().ordinal()];
        CartaInglesa top = fd.getUltimaCarta();
        if (top == null) return carta.getValorBajo() == 1;
        return top.getValorBajo() + 1 == carta.getValorBajo();
    }

    // Verifica fin de juego: todas las foundations en K.
    public boolean isGameOver() {
        for (FoundationDeck fd : foundation) {
            if (fd.estaVacio()) return false;
            CartaInglesa top = fd.getUltimaCarta();
            if (top == null || top.getValor() != 13) 
                return false;
        }
        return true;
    }

    // Crea las 4 foundations
    private void createFoundations() {
        for (Palo p : Palo.values()) 
            foundation[p.ordinal()] = new FoundationDeck(p);
    }

    // Reparte cartas a los 7 tableaux
    private void createTableaux() {
        for (int i = 0; i < tableau.length; i++) {
            TableauDeck t = new TableauDeck();
            t.inicializar(drawPile.getCartas(i + 1));
            tableau[i] = t;
        }
    }

    // Deshace el ultimo movimiento de forma robusta.
    public boolean deshacerUltimoMovimiento() {
        Movimientos m = historial.pop();
        if (m == null) 
            return false;

        boolean ok = false;
        switch (m.getTipo()) {
            case Movimientos.DRAW: {
                // tomar k del waste y ponerlas encima del draw (face-down)
                Pila<CartaInglesa> aux = new Pila<CartaInglesa>(128);
                for (int i = 0; i < m.getCantidad(); i++) {
                    CartaInglesa c = wastePile.getCarta();
                    if (c == null) 
                        break;
                    aux.push(c);
                }
                Pila<CartaInglesa> bloque = new Pila<CartaInglesa>(128);
                for (CartaInglesa c = aux.pop(); 
                        c != null; 
                        c = aux.pop()) 
                    bloque.push(c);
                drawPile.pushBlockOnTop(bloque);
                ok = true;
                break;
            }
            case Movimientos.RELOAD: {
                // tomar k del draw y regresarlas al waste (face-up)
                Pila<CartaInglesa> bloque = drawPile.getCartas(m.getCantidad());
                Pila<CartaInglesa> aux = new Pila<CartaInglesa>(64);
                for (CartaInglesa c = bloque.pop();
                        c != null; 
                        c = bloque.pop()) {
                    c.makeFaceUp(); 
                    aux.push(c); 
                }
                Pila<CartaInglesa> paraWaste = new Pila<CartaInglesa>(64);
                for (CartaInglesa c = aux.pop();
                        c != null; 
                        c = aux.pop()) 
                    paraWaste.push(c);
                wastePile.addCartas(paraWaste);
                ok = true;
                break;
            }
            case Movimientos.WASTE_A_TABLEAU: {
                // quitar 1 del tableau destino y regresar al waste
                TableauDeck dest = tableau[m.getToIndex()];
                CartaInglesa c = dest.removerUltimaCarta();
                if (c == null) {
                    ok = false; 
                    break; 
                }
                Pila<CartaInglesa> b = new Pila<CartaInglesa>(4);
                c.makeFaceUp();
                b.push(c);
                wastePile.addCartas(b);
                ok = true;
                break;
            }
            case Movimientos.WASTE_A_FOUNDATION: {
                // quitar 1 de la foundation y regresar al waste
                FoundationDeck fd = foundation[m.getFoundationIndex()];
                CartaInglesa c = fd.removerUltimaCarta();
                if (c == null) {
                    ok = false; 
                    break; 
                }
                Pila<CartaInglesa> b = new Pila<CartaInglesa>(4);
                c.makeFaceUp();
                b.push(c);
                wastePile.addCartas(b);
                ok = true;
                break;
            }
            case Movimientos.TABLEAU_A_TABLEAU: {
                TableauDeck src = tableau[m.getFromIndex()];
                TableauDeck dst = tableau[m.getToIndex()];

                int k = (m.getBlock() != null) ? tamanoPila(m.getBlock()) : m.getCantidad();
                if (k <= 0) {
                    ok = false; 
                    break; 
                }

                // quitar exactamente k del destino y guardarlas por si rollback
                Pila<CartaInglesa> quitadasTopDown = new Pila<CartaInglesa>(512);
                int quitadas = 0;
                for (int i = 0; i < k; i++) {
                    CartaInglesa c = dst.removerUltimaCarta();
                    if (c == null) break;
                    quitadasTopDown.push(c);
                    quitadas++;
                }
                if (quitadas != k) {
                    // devolver al destino si no pudimos quitar k
                    Pila<CartaInglesa> devolver = new Pila<CartaInglesa>(512);
                    for (CartaInglesa c = quitadasTopDown.pop(); c != null; c = quitadasTopDown.pop()) {
                        devolver.push(c);
                    }
                    dst.reponerBloque(devolver);
                    ok = false;
                    break;
                }

                // clonar bloque original para reponer en el origen
                if (m.getBlock() == null || m.getBlock().peek() == null) {
                    // no hay bloque original: rollback
                    Pila<CartaInglesa> devolver = new Pila<CartaInglesa>(512);
                    for (CartaInglesa c = quitadasTopDown.pop(); c != null; c = quitadasTopDown.pop()) {
                        devolver.push(c);
                    }
                    dst.reponerBloque(devolver);
                    ok = false;
                    break;
                }
                Pila<CartaInglesa> bloque = clonarBottomTop(m.getBlock()); // bottom->top

                // revertir flip automatico si lo hubo
                if (m.isVolteoAutomatico()) {
                    CartaInglesa expuesta = src.verUltimaCarta();
                    if (expuesta != null && expuesta.isFaceup()) expuesta.makeFaceDown();
                }

                // reponer en el origen; si falla, rollback al destino
                boolean regreso = src.reponerBloque(bloque);
                if (!regreso) {
                    Pila<CartaInglesa> devolver = new Pila<CartaInglesa>(512);
                    for (CartaInglesa c = quitadasTopDown.pop();
                            c != null; c = quitadasTopDown.pop()) {
                        devolver.push(c);
                    }
                    dst.reponerBloque(devolver);
                    ok = false;
                    break;
                }
                ok = true;
                break;
            }
            case Movimientos.TABLEAU_A_FOUNDATION: {
                // quitar 1 de foundation y reponer en el origen
                FoundationDeck fd = foundation[m.getFoundationIndex()];
                CartaInglesa c = fd.removerUltimaCarta();
                if (c == null) { ok = false; break; }

                // revertir flip automatico antes de reponer
                if (m.isVolteoAutomatico()) {
                    CartaInglesa expuesta = tableau[m.getFromIndex()].verUltimaCarta();
                    if (expuesta != null && expuesta.isFaceup()) expuesta.makeFaceDown();
                }

                Pila<CartaInglesa> uno = new Pila<CartaInglesa>(4);
                uno.push(c);
                boolean back = tableau[m.getFromIndex()].reponerBloque(uno);
                if (!back) {
                    // si no reponemos, devolvemos a foundation
                    fd.agregarCarta(c);
                    ok = false;
                    break;
                }
                ok = true;
                break;
            }
            default:
                ok = false;
        }

        if (ok && moveCount > 0) moveCount--;
        return ok;
    }
}
