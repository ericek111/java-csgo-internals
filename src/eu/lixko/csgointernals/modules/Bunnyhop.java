package eu.lixko.csgointernals.modules;

import com.sun.jna.Pointer;

import eu.lixko.csgointernals.Engine;
import eu.lixko.csgointernals.Module;
import eu.lixko.csgointernals.sdk.IClientMode;
import eu.lixko.csgoshared.offsets.Offsets;

public class Bunnyhop extends Module {

	Module thismodule = this;

	IClientMode.CreateMove hkCreateMove = new IClientMode.CreateMove() {
		@Override
		public boolean callback(Pointer thisptr, float flInputSampleTime, long cmd) {
			while(true) {
				if(cmd == 0) 
					break;
				
				int lpi = Offsets.IEngineClient.getOriginalFunction(12).invokeInt(new Object[] {Offsets.IClientEntityList.base()});
				long localplayer = Offsets.IClientEntityList.getOriginalFunction(3).invokeLong(new Object[] { Offsets.IClientEntityList.base(), lpi });
				
				if(localplayer == 0) 
					break;
				
				int cmdnum = Engine.unsafe.getInt(cmd + 8); // command_number
				if(cmdnum == 0)
					break;

				int buttons = Engine.unsafe.getInt(cmd + 52);
				if((buttons & (1 << 1)) == 0) // cmd->buttons & IN_JUMP
					break;

				int flags = Engine.unsafe.getInt(localplayer + 0x138);
				if((flags & 1) == 0) {
					buttons &= ~(1 << 1);
					Engine.unsafe.putInt(cmd + 52, buttons);
				}
				
				break;
			}
			Boolean origRet = (Boolean) origFunc.invoke(Boolean.class, new Object[] { thisptr, flInputSampleTime, cmd});
			return origRet.booleanValue();
		}
	};
	

	@Override
	public void onEngineLoaded() {
		hkCreateMove.hook();
	}

}
