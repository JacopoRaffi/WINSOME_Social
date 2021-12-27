package Utilities;

import java.util.LinkedList;
import java.util.List;

public class Wallet {
    private int totale; //wincoin
    private List<String> transazioni;

    public Wallet(){
        totale = 0;
        transazioni = new LinkedList<>();
    }

    public long getTotale() {
        return totale;
    }

    public List<String> getTransazioni() {
        return transazioni;
    }

    public void addIncremento(int incremento) {
        totale += incremento;
    }

    public boolean addTransazione(String transazione){ //una stringa transazione è incremento, date
        return transazioni.add(transazione);
    }
}
