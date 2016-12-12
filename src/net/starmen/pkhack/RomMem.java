/*
 * Created on Aug 19, 2004
 */
package net.starmen.pkhack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * TODO Write javadoc for this class
 * 
 * @author AnyoneEB
 */
public class RomMem extends AbstractRom
{
    /**
     * Contains the loaded ROM. It is perfered that you don't access this
     * directly.
     * 
     * @see #write(int, int)
     * @see #read(int)
     */
    protected byte[] rom;
    protected static TitletoCCS app = new TitletoCCS();

    protected void readFromRom(File rompath) throws FileNotFoundException,
        IOException
    {
        this.rom = new byte[(int) rompath.length()];
        FileInputStream in = new FileInputStream(rompath);
        in.read(rom);
        in.close();
    }

    public boolean saveRom(File rompath)
    {
        if (!this.isLoaded) // don't try to save if nothing is loaded
        {
            return false;
        }
        this.path = rompath;
        setDefaultDir(rompath.getParent());
        // ensure mirror for ExHiRom
        if (length() == 0x600200)
        {
            rom[0x0101d5] = 0x25;
            rom[0x0101d7] = 0x0d;
            System.arraycopy(rom, 0x008200, rom, 0x408200, 0x8000);
        }

        try
        {
            FileOutputStream out = new FileOutputStream(rompath);
            if (length() == 0x400200) {
            rom[0xef6b] = rom[0xeec6];
			rom[0xef6c] = rom[0xeec7];
			rom[0xef6d] = rom[0xeec8];
			rom[0xef6e] = rom[0xeec9];
			rom[0xef6f] = rom[0xeeca];
			rom[0xef70] = rom[0xeecb];
			rom[0xef71] = rom[0xeecc];
			rom[0xef72] = rom[0xeecd];
			rom[0xef73] = rom[0xeece];
			rom[0xef74] = rom[0xeecf];
		} else if (length() == 0x400000) {
			rom[0xed6b] = rom[0xecc6];
			rom[0xed6c] = rom[0xecc7];
			rom[0xed6d] = rom[0xecc8];
			rom[0xed6e] = rom[0xecc9];
			rom[0xed6f] = rom[0xecca];
			rom[0xed70] = rom[0xeccb];
			rom[0xed71] = rom[0xeccc];
			rom[0xed72] = rom[0xeccd];
			rom[0xed73] = rom[0xecce];
			rom[0xed74] = rom[0xeccf];
		}
            if (length() == 0x600200) {
            rom[0xef6b] = rom[0xeec6];
			rom[0xef6c] = rom[0xeec7];
			rom[0xef6d] = rom[0xeec8];
			rom[0xef6e] = rom[0xeec9];
			rom[0xef6f] = rom[0xeeca];
			rom[0xef70] = rom[0xeecb];
			rom[0xef71] = rom[0xeecc];
			rom[0xef72] = rom[0xeecd];
			rom[0xef73] = rom[0xeece];
			rom[0xef74] = rom[0xeecf];
		} else if (length() == 0x600000) {
			rom[0xed6b] = rom[0xecc6];
			rom[0xed6c] = rom[0xecc7];
			rom[0xed6d] = rom[0xecc8];
			rom[0xed6e] = rom[0xecc9];
			rom[0xed6f] = rom[0xecca];
			rom[0xed70] = rom[0xeccb];
			rom[0xed71] = rom[0xeccc];
			rom[0xed72] = rom[0xeccd];
			rom[0xed73] = rom[0xecce];
			rom[0xed74] = rom[0xeccf];
		}
            out.write(this.rom);
            out.close();
//          File fi = new File(rompath.getParent() + "/title_screen.ccs");
//          app.CCSFile(this.rom, fi);
		}
        catch (FileNotFoundException e)
        {
            System.err.println("Error: File not saved: File not found.");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("Error: File not saved: Could write file.");
            e.printStackTrace();
            return false;
        }
        System.out.println("Saved ROM: " + this.path.length() + " bytes");
        saveRomType();
        return true;
    }

    public void write(int offset, int arg)
    {
        if (offset > this.rom.length) // don't write past the end of the ROM
        {
            return;
        }

        this.rom[offset] = (byte) (arg & 255);

        if (getRomType().equals("Earthbound") && length() == 0x600200
            && offset >= 0x008200 && offset < 0x009200)
            write(offset + 0x400000, arg);
    }

    public void write(int offset, byte[] arg, int len)
    {
        // OK to use this instead of write()?
        System.arraycopy(arg, 0, rom, offset, len);
    }

    public int read(int offset)
    {
        /* Kill the sign bit so the offset is positive. */
        offset &= 0x7fffffff;
        if (offset >= this.length()) // don't write past the end
        // of the ROM
        {
            // System.out.println(
            // "Attempted read past end of rom, (0x"
            // + Integer.toHexString(offset)
            // + ")");
            return -1;
        }
        return this.rom[offset] & 255;
    }

    public byte[] readByte(int offset, int length)
    {
        byte[] returnValue = new byte[length];
        try
        {
            // OK to not end up going to read function?
            System.arraycopy(rom, offset, returnValue, 0, length);
        }
        catch (IndexOutOfBoundsException e)
        {
            return super.readByte(offset, length);
        }
        return returnValue;
    }

    public void resetArea(int offset, int len, AbstractRom orgRom)
    {
        // only works if neither is direct file IO
        if (orgRom instanceof RomMem)
            System.arraycopy(((RomMem) orgRom).rom, offset, rom, offset, len);
        // otherwise, use normal methods to read/write
        else
            super.resetArea(offset, len, orgRom);
    }

    // TODO check _expand()
    protected boolean _expand()
    {
        int rl = length();
        byte[] out = new byte[rl + (4096 * 256)];
        // for (int i = 0; i < rl; i++)
        // {
        // out[i] = (byte) read(i);
        // }
        Arrays.fill(out, rl, out.length, (byte) 0);
        System.arraycopy(rom, 0, out, 0, rl);
        for (int j = 0; j < 4096; j++)
        {
            // for (int i = 0; i < 255; i++)
            // {
            // out[((j * 256) + i) + rl] = 0;
            // }
            // Arrays.fill(out, (j * 256) + rl, (j * 256) + rl + 255, (byte) 0);
            out[((j * 256) + 255) + rl] = 2;
        }

        rom = out;

        return true;
    }

    protected boolean _expandEx()
    {
        int rl = length();
        byte[] out = new byte[rl + (2 << 20)];
        Arrays.fill(out, rl, out.length, (byte) 0);
        System.arraycopy(rom, 0, out, 0, rl);
        out[0x0101d5] = 0x25;
        out[0x0101d7] = 0x0d;
        System.arraycopy(out, 0x008200, out, 0x408200, 0x8000);

        rom = out;

        return true;
    }

    public boolean _truncate(int newLen)
    {
        byte[] newRom = new byte[newLen];
        System.arraycopy(rom, 0, newRom, 0, newLen);
        rom = newRom;
        return true;
    }

    public int length()
    {
        return rom.length;
    }

    public IPSFile createIPS(AbstractRom orgRom, int start, int end)
    {
        if (orgRom instanceof RomMem)
        {
            return IPSFile.createIPS(this.rom, ((RomMem) orgRom).rom, start,
                end);
        }
        else
        {
            IPSFile out = new IPSFile();

            int cStart = -1;

            for (int i = start; i < end; i++)
            {
                if (rom[i] != orgRom.readByte(i))
                {
                    if (cStart == -1)
                        cStart = i;
                }
                else
                {
                    if (cStart != -1)
                    {
                        out.addRecord(cStart, ByteBlock.wrap(rom, cStart, i
                            - cStart));
                        cStart = -1;
                    }
                }
            }

            return out;
        }
    }

    public boolean apply(IPSFile ips)
    {
        try
        {
            ips.apply(this.rom);
            // ensure mirror for ExHiRom
            if (length() == 0x600200)
                System.arraycopy(rom, 0x008200, rom, 0x408200, 0x8000);
            return true;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
    }

    public boolean unapply(IPSFile ips, AbstractRom orgRom)
    {
        if (orgRom instanceof RomMem)
        {
            try
            {
                ips.unapply(this.rom, ((RomMem) orgRom).rom);
                return true;
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                return false;
            }
        }
        else
        {
            return super.unapply(ips, orgRom);
        }
    }

    public boolean check(IPSFile ips)
    {
        return ips.check(rom);
    }

    public boolean isDirectFileIO()
    {
        return false;
    }
}