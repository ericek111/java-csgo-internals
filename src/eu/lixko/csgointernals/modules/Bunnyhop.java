package eu.lixko.csgointernals.modules;

import eu.lixko.csgointernals.Client;
import eu.lixko.csgointernals.Engine;
import eu.lixko.csgointernals.Module;
import eu.lixko.csgoshared.offsets.Netvars;
import eu.lixko.csgoshared.offsets.Offsets;
import eu.lixko.csgoshared.sdk.Const;

public class Bunnyhop extends Module {

	Module thismodule = this;

	Thread bhopLoop = new Thread(new Runnable() {
		@Override
		public void run() {
			while (Client.theClient.isRunning) {
				try {
					Thread.sleep(5);
					if (Offsets.m_dwLocalPlayer == 0 || !thismodule.isToggled())
						continue;

					if (Engine.clientModule().readInt(Offsets.input.alt1) == 5) {
						long m_fFlags = Engine.clientModule().readLong(Offsets.m_dwLocalPlayer + Netvars.CBasePlayer.m_fFlags);
						// TODO: Add randomization settings.
						if (false && (m_fFlags & Const.FL_ONGROUND) > 0)
							Engine.clientModule().writeInt(Offsets.input.jump, 6);
						else if ((m_fFlags & Const.FL_ONGROUND) > 0) {
							Thread.sleep(5 + (int) (Math.random() * 15));
							Engine.clientModule().writeInt(Offsets.input.jump, 5);
							Thread.sleep(15 + (int) (Math.random() * 100));
							Engine.clientModule().writeInt(Offsets.input.jump, 4);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	});

	@Override
	public void onEngineLoaded() {
		bhopLoop.start();
	}

}
