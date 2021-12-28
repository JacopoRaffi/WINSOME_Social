package Utilities;

import java.util.LinkedList;
import java.util.List;

public class Wallet {
    private double totale; //wincoin
    private final List<String> transazioni;

    public Wallet(){
        totale = 0;
        transazioni = new LinkedList<>();
    }

    public synchronized double getTotale() {
       return totale;
    }

    public synchronized List<String> getTransazioni() {
        return transazioni;
    }

    public synchronized void addIncremento(double incremento) {
        totale += incremento;
    }

    public synchronized void addTransazione(String transazione){
        //una stringa transazione è incremento, date
        transazioni.add(transazione);

    }

    @Override
    public synchronized String toString(){
        //la stringa sarà totale, [transazione1, transazione2,..., transazioneN]
        return  "" + totale + ", "  + transazioni;
    }
}
