/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mediathekServer.tool;

/**
 *
 * @author emil
 */
public class MS_Warten {

    public synchronized void warten(int sekunden) {
        MS_Log.systemMeldung("Warten: " + String.valueOf(sekunden) + " Sekunden");
        try {
            this.wait(sekunden * 1000 /* Sekunden*/);
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(347895642, MS_Funktionen.class.getName(), "Warten nach dem Suchen", ex);
        }
    }
}
