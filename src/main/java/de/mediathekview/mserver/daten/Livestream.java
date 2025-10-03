package de.mediathekview.mserver.daten;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

public class Livestream extends AbstractMediaResource<URL> {
	private static final long serialVersionUID = 6510203888335220851L;

	/**
	 * DON'T USE! - ONLY FOR GSON!
	 */
	@SuppressWarnings("unused")
	private Livestream() {
		super();
	}
	
	public Livestream(final UUID aUuid, final Sender aSender, final String aTitel, final String aThema,
			final LocalDateTime aTime) {
		super(aUuid, aSender, aTitel, aThema, aTime);
	}
	
	public Livestream(Livestream copyObj) {
		super(copyObj);
	}

}
