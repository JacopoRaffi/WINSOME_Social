package Utilities;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Wallet {
    private double totale; //wincoin
    private final List<String> transazioni;
    private final Lock readLock;
    private final Lock writeLock;

    public Wallet(){
        totale = 0;
        ReentrantReadWriteLock auxLock = new ReentrantReadWriteLock();
        readLock = auxLock.readLock();
        writeLock = auxLock.writeLock();
        transazioni = new LinkedList<>();
    }

    public double getTotale() {
        double varTotale;
        try{
            readLock.lock();
            varTotale = totale;
        }finally{
            readLock.unlock();
        }
        return varTotale;
    }

    public List<String> getTransazioni() {
        return transazioni;
    }

    public void addIncremento(double incremento) {
        try {
            writeLock.lock();
            totale += incremento;
        }finally{
            writeLock.unlock();
        }
    }

    public void addTransazione(String transazione){
        try {//una stringa transazione è incremento, date
            writeLock.lock();
            transazioni.add(transazione);
        }finally{
            writeLock.unlock();
        }
    }

    @Override
    public String toString(){
        //la stringa sarà totale, [transazione1, transazione2,..., transazioneN]
        String ret;
        try{
            readLock.lock();
            ret = "" + totale + ", "  + transazioni;
        }finally{
            readLock.unlock();
        }
        return ret;
    }
}
