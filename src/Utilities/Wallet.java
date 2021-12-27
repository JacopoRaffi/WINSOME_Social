package Utilities;

import java.util.LinkedList;
import java.util.List;

public class Wallet {
    private long totale;
    private List<> transazioni;

    public Wallet(){
        totale = 0;
        transazioni = new LinkedList<>();
    }

    public long getTotale() {
        return totale;
    }

    public List<> getTransazioni() {
        return transazioni;
    }

    public void setTotale(long totale) {
        this.totale = totale;
    }

    public boolean addTransazione(){
        return transazioni.add();
    }
}
