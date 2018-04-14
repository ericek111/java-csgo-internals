package eu.lixko.csgointernals.modules;

import com.sun.jna.Pointer;

import eu.lixko.csgointernals.Module;
import eu.lixko.csgointernals.sdk.IVPanel;
import eu.lixko.csgoshared.offsets.Offsets;

public class PaintTraverse extends Module {
	IVPanel.PaintTraverse hkPaintTraverse = new IVPanel.PaintTraverse() {
		public void callback(Pointer thisptr, long vgui_panel, boolean force_repaint, boolean allow_force) {
			//Engine.unsafe.putFloat(pSetup + 184, 20f);
			while(true) {
				String vguiPanelName = Offsets.IVPanel.GetName(vgui_panel);
				if(!vguiPanelName.equals("MatSystemTopPanel"))
					break;
				Offsets.ISurface.DrawSetColor(50, 50, 0, 255);
				Offsets.ISurface.DrawFilledRect(50, 50, 150, 150);
				/*Offsets.ISurface.DrawSetTextPos(50, 50);
				Offsets.ISurface.DrawSetTextColor(255, 255, 0, 255);
				Offsets.ISurface.DrawPrintText("LOL!");*/

				break;
			}
			origFunc.invokeVoid(new Object[] {thisptr, vgui_panel, force_repaint, allow_force});
		}
	};
	
	@Override
	public void onEngineLoaded() {
		hkPaintTraverse.hook();
	}
}
