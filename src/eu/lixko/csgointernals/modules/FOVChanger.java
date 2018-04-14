package eu.lixko.csgointernals.modules;

import com.sun.jna.Pointer;

import eu.lixko.csgointernals.Module;
import eu.lixko.csgointernals.sdk.IClientMode;

public class FOVChanger extends Module {
	IClientMode.OverrideView hkOverrideView = new IClientMode.OverrideView() {
		public void callback(Pointer thisptr, long pSetup) {
			//Engine.unsafe.putFloat(pSetup + 184, 20f);
			origFunc.invokeVoid(new Object[] {thisptr, pSetup});
		}
	};
	
	@Override
	public void onEngineLoaded() {
		hkOverrideView.hook();
	}
}
