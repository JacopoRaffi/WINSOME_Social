package Utilities;

import java.util.LinkedList;
import java.util.List;

public class Wallet {
    private double totale; //wincoin
    private List<String> transazioni;

    public Wallet(){
        totale = 0;
        transazioni = new LinkedList<>();
    }

    public double getTotale() {
        return totale;
    }

    public List<String> getTransazioni() {
        return transazioni;
    }

    public void addIncremento(double incremento) {
        totale += incremento;
    }

    public boolean addTransazione(String transazione){ //una stringa transazione è incremento, date
        return transazioni.add(transazione);
    }

    @Override
    public String toString(){ //la stringa sarà totale, [transazione1, transazione2,..., transazioneN]
        return "" + totale + ", "  + transazioni;
    }
}
