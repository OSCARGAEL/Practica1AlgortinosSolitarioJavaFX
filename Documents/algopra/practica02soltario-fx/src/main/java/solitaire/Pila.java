package solitaire;

public class Pila<T> {

    // Arreglo interno y puntero al top (-1 cuando esta vacia)
    private final T[] pila;
    private int tope = -1;

    public Pila(int size) {
        pila = (T[]) new Object[size];
        tope = -1;
    }
    
     // Empuja un elemento al top.
     // Si esta llena, no inserta (evita ruido en consola).
     
    public void push(T dato) {
        if (isFull()) {
            // System.out.println("Desbordamiento");
            return;
        }
        pila[++tope] = dato;
    }

     // Saca y retorna el elemento del top.
     // Retorna null si esta vacia.
     
    public T pop() {
        if (isEmpty()) {
            // System.out.println("Subdesbordamiento");
            return null;
        }
        return pila[tope--];
    }

     // Retorna el elemento del top sin retirarlo.
     // Retorna null si esta vacia.
     
    public T peek() {
        if (isEmpty()) {
            // System.out.println("Pila vacia");
            return null;
        }
        return pila[tope];
    }

    // Retorna true si la pila esta llena. 
    public boolean isFull() {
        return tope == pila.length - 1;
    }

    // Retorna true si la pila esta vacia. 
    public boolean isEmpty() {
        return tope == -1;
    }
}
