package no.nordicsemi.android.nrftoolbox.cgms;

import no.nordicsemi.android.ble.data.Data;

public class CGMSData {
	private static final byte OP_CODE_START_SESSION = 26;

	static Data startSession() {
		return new Data(OP_CODE_START_SESSION);
	}


}
