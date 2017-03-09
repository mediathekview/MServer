/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package mServer.tool;

import java.util.concurrent.TimeUnit;

import mServer.MServer;

public class MserverTimer extends Thread {

    private final MServer mserver;

    public MserverTimer(MServer mserver) {
        this.mserver = mserver;
        setName("MServerTimer");
    }

    public void ping() {
        if (!mserver.isSuchen()) {
            // nicht beschäftigt
            mserver.laufen();
        }
    }

    @Override
    public synchronized void run() {
        while (true) {
            ping();
            // let's stop when there was an interrupt
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            schlafen();
        }
    }

    private void schlafen() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }
}
