package net.starmen.pkhack;

import java.io.*;
import java.util.*;
import javax.xml.bind.*;

public class TitletoCCS {
	
	static String outbyte = "";
	static String ccsfile;
	
	public void CCSFile(byte[] b, File fil) {
		
		byte[] staticpal = Arrays.copyOfRange(b, 0x21B07C, 0x21B082);
		byte[] animpal = Arrays.copyOfRange(b, 0x21B083, 0x21B0FC);
		byte[] highpal = Arrays.copyOfRange(b, 0x21B0FD, 0x21B17C);
		byte[] staticlayarr = Arrays.copyOfRange(b, 0x21B17D, 0x21B410);
		byte[] staticlaygrap = Arrays.copyOfRange(b, 0x21B411, 0x21C8E4);
		byte[] textlaygrap = Arrays.copyOfRange(b, 0x21C8E5, 0x21CFE0);
		byte[] copy1 = Arrays.copyOfRange(b, 0x21CFE1, 0x21D007);
		byte[] copy2 = Arrays.copyOfRange(b, 0x21D008, 0x21D1AE);
		String bytes = DatatypeConverter.printHexBinary(staticpal);
		bytes += DatatypeConverter.printHexBinary(animpal);
		bytes += DatatypeConverter.printHexBinary(highpal);
		bytes += DatatypeConverter.printHexBinary(staticlayarr);
		bytes += DatatypeConverter.printHexBinary(staticlaygrap);
		bytes += DatatypeConverter.printHexBinary(textlaygrap);
		bytes += DatatypeConverter.printHexBinary(copy1);
		bytes += DatatypeConverter.printHexBinary(copy2);
		
		for (int i = 0; i < bytes.length(); i += 2) {
			outbyte += bytes.substring(i, i + 2) + " ";
		}
		
		ccsfile = "// This is the code for your title screen. :)" + "\n\nimport asm65816\n\n" + "ROM[0xC3F492] = ASMLoadAddress0E (staticpal)" + "\n\n" + "ROM[0xC0EC83] = ASMLoadAddress0E (animpal)" + "\n\n" + "ROM[0xC0EC9D] = ASMLoadAddress0E (highpal)" + "\n\n" + "ROM[0xC0EC1D] = ASMLoadAddress0E (staticlayarr)" + "\n\n" + "ROM[0xC0EBF2] = ASMLoadAddress0E (staticlaygrap)" + "\n\n" + "ROM[0xC0EC49] = ASMLoadAddress0E (textlaygrap)" + "\n\n" + "ROM[0xC0ECC6] = ASMLoadAddress0E (copy)" + "\n\n" + "ROM[0xC0ED6B] = ASMLoadAddress0E (copy)" + "\n\n" + "staticpal:\n" + "\"[" + outbyte.substring(0, 20) + "]\"\n\n" + "animpal:\n\"[" + outbyte.substring(21, 386) + "]\"\n\n" + "highpal:\n" + "\"[" + outbyte.substring(387, 770) + "]\"\n\n" + "staticlayarr:\n" + "\"[" + outbyte.substring(771, 2750) + "]\"\n\n" + "staticlaygrap:\n" + "\"[" + outbyte.substring(2751, 18746) + "]\"\n\n" + "textlaygrap:\n" + "\"[" + outbyte.substring(18747, 24110) + "]\"\n\n" + "copy:\n" + "\"[" + outbyte.substring(24111, 24227) + "]\"";
		
		try {
		FileWriter fw = new FileWriter(fil);
		BufferedWriter buff = new BufferedWriter(fw);
		buff.write(ccsfile);
		buff.close();
		} catch (Exception whatever) { System.err.println(whatever.toString()); }
	}
}