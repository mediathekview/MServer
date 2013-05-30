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
    
    public synchronized void sekundenWarten(int sekunden) {
        MS_Log.systemMeldung("Warten: " + String.valueOf(sekunden) + " Sekunden");
        try {
            while (sekunden > 0) {
                this.wait(1000 /* Sekunden*/);
                sekunden -= 1;
                System.out.print("\r");
                System.out.print(String.valueOf(sekunden));
            }
        } catch (Exception ex) {
            MS_Log.fehlerMeldung(347895642, MS_Funktionen.class.getName(), "Warten nach dem Suchen", ex);
        }
        System.out.println("");
    }
}
