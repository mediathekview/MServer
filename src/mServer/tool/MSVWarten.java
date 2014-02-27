/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mServer.tool;

/**
 *
 * @author emil
 */
public class MSVWarten {

    public synchronized void sekundenWarten(int sekunden) {
        MSVLog.systemMeldung("Warten: " + String.valueOf(sekunden) + " Sekunden");
        try {
            while (sekunden > 0) {
                this.wait(1000 /* 1 Sekunde */);
                sekunden -= 1;
                System.out.print("\r");
                System.out.print(String.valueOf(sekunden));
            }
        } catch (Exception ex) {
            MSVLog.fehlerMeldung(347895642, MSVFunktionen.class.getName(), "Warten nach dem Suchen", ex);
        }
        System.out.println("");
    }
}
