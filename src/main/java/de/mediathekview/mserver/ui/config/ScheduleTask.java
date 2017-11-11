package de.mediathekview.mserver.ui.config;

public class ScheduleTask implements Runnable{
	private MServerConfigUI instance;
	public ScheduleTask(MServerConfigUI mServerConfigUIInstance) {
		instance=mServerConfigUIInstance;
	}
    @Override
    public void run() {
        instance.start();
    }
}
