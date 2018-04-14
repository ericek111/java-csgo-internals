package eu.lixko.csgointernals.sdk;

import com.github.jonatino.process.Module;
import com.sun.jna.ptr.IntByReference;

import eu.lixko.csgoshared.offsets.GameInterface;

public class ISurface extends GameInterface {	
	public ISurface(Module module, long baseclass, String modulename, String version) {
		super(module, baseclass, modulename, version);
		INSTANCE = this;
	}

	public ISurface(Module module, long addr) {
		super(module, addr);
		INSTANCE = this;
	}

	public ISurface(Module module, String modulename, String version) {
		super(module, modulename, version);
		INSTANCE = this;
	}

	public static ISurface INSTANCE;
	
	public void DrawSetColor(int r, int g, int b, int a) {
		this.getOriginalFunction(14).invokeVoid(new Object[] { this.base(), r, g, b, a});
	}
	
	public void DrawFilledRect(int x0, int y0, int x1, int y1) {
		this.getOriginalFunction(16).invokeVoid(new Object[] { this.base(), x0, y0, x1, y1});
	}
	
	public void DrawOutlinedRect(int x0, int y0, int x1, int y1) {
		this.getOriginalFunction(18).invokeVoid(new Object[] { this.base(), x0, y0, x1, y1});
	}
	
	public void DrawLine(int x0, int y0, int x1, int y1) {
		this.getOriginalFunction(19).invokeVoid(new Object[] { this.base(), x0, y0, x1, y1});
	}
	
	public void DrawSetTextFont(long font) {
		this.getOriginalFunction(23).invokeVoid(new Object[] { this.base(), font});
	}
	
	public void DrawSetTextColor(int r, int g, int b, int a) {
		this.getOriginalFunction(24).invokeVoid(new Object[] { this.base(), r, g, b, a});
	}
	
	public void DrawSetTextPos(int x, int y) {
		this.getOriginalFunction(26).invokeVoid(new Object[] { this.base(), x, y});
	}
	
	public void DrawPrintText(String str) {
		this.getOriginalFunction(12).invokeVoid(new Object[] { this.base(), str, str.length(), 0});
	}
	
	public void DrawSetTextureRGBA(int textureID, byte[] colors, int w, int h) {
		this.getOriginalFunction(37).invokeVoid(new Object[] { this.base(), textureID, colors, w, h});
	}
	
	public void DrawSetTexture(int textureID) {
		this.getOriginalFunction(38).invokeVoid(new Object[] { this.base(), textureID});
	}
	
	public int CreateNewTextureID(boolean procedural) {
		return this.getOriginalFunction(43).invokeInt(new Object[] { this.base(), procedural});
	}
	
	public long CreateFont() {
		return this.getOriginalFunction(71).invokeLong(new Object[] { this.base() });
	}
	
	public void SetFontGlyphSet(long font, String fontName, int tall, int weight, int blur, int scanlines, int flags) {
		this.getOriginalFunction(72).invokeVoid(new Object[] { this.base(), font, fontName, tall, weight, blur, scanlines, flags, 0, 0});
	}
	
	public int[] GetTextSize(long font, String str) {
		IntByReference wide = new IntByReference();
		IntByReference tall = new IntByReference();
		this.getOriginalFunction(106).invokeVoid(new Object[] { this.base(), str, wide.getPointer(), tall.getPointer()});
		return new int[] { wide.getValue(), tall.getValue() };
	}
	
	public void DrawTexturedPolygon(int vtxCount, long vtx, boolean bClipVertices) {
		this.getOriginalFunction(106).invokeVoid(new Object[] { this.base(), vtxCount, vtx, bClipVertices});
	}
}
