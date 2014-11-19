/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mvServer.tool;

/**
 *
 * @author emil
 */
public class MvSWarten {

    public synchronized void sekundenWarten(int sekunden) {
        MvSLog.systemMeldung("Warten: " + String.valueOf(sekunden) + " Sekunden");
        try {
            while (sekunden > 0) {
                this.wait(1000 /* 1 Sekunde */);
                sekunden -= 1;
                System.out.print("\r");
                System.out.print(String.valueOf(sekunden));
            }
        } catch (Exception ex) {
            MvSLog.fehlerMeldung(347895642, MvSFunktionen.class.getName(), "Warten nach dem Suchen", ex);
        }
        System.out.println("");
    }
}
