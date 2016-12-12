/*
 * Created on May 25, 2003
 */
package net.starmen.pkhack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Wrapper class for an Earthbound ROM that uses direct file i/o.
 * 
 * @author AnyoneEB
 */
public class RomFileIO extends AbstractRom
{
    public RandomAccessFile rom;
    public static RandomAccessFile newRom;

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#readFromRom(java.io.File)
     */
    protected void readFromRom(File rompath) throws FileNotFoundException,
        IOException
    {
        rom = new RandomAccessFile(rompath, "rwd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#read(int)
     */
    public int read(int offset)
    {
        try
        {
            if (offset >= this.length()) // don't write past the end of the
            // ROM
            {
                // System.out.println(
                // "Attempted read past end of rom, (0x"
                // + Integer.toHexString(offset)
                // + ")");
                return -1;
            }
            rom.seek(offset);
            return rom.readUnsignedByte();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public byte readByteSeek()
    {
        try
        {
            return rom.readByte();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public byte[] readByte(int offset, int length)
    {
        try
        {
            rom.seek(offset);
            byte[] out = new byte[length];
            rom.read(out);
            return out;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            /* AbstractROM's is slower, but will handle errors. */
            return super.readByte(offset, length);
        }
    }

    public byte[] readByteSeek(int length)
    {
        try
        {
            byte[] out = new byte[length];
            rom.read(out);
            return out;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            /* AbstractROM's is slower, but will handle errors. */
            return super.readByteSeek(length);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#write(int, int)
     */
    public void write(int offset, int arg)
    {
        try
        {
            if (offset >= rom.length())
                return;
            rom.seek(offset);
            rom.writeByte(arg);

            if (getRomType().equals("Earthbound") && length() == 0x600200
                && offset >= 0x008200 && offset < 0x009200)
                write(offset + 0x400000, arg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void write(int offset, byte[] arg, int len)
    {
        try
        {
            if (offset >= rom.length())
                return;
            rom.seek(offset);
            rom.write(arg, 0, len);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void writeSeek(byte[] arg, int len)
    {
        try
        {
            rom.write(arg, 0, len);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#readSeek()
     */
    public int readSeek()
    {
        try
        {
            return rom.readUnsignedByte();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#seek(int)
     */
    public void seek(int offset)
    {
        try
        {
            rom.seek(offset);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#writeSeek(int)
     */
    public void writeSeek(int arg)
    {
        try
        {
            rom.write(arg);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#saveRom(java.io.File)
     */
    public boolean saveRom(File rompath)
    {
        if (!this.isLoaded) // don't try to save if nothing is loaded
        {
            return false;
        }

        // ensure mirror for ExHiRom
        if (length() == 0x600200)
        {
            write(0x0101d5, 0x25);
            write(0x0101d7, 0x0d);
            write(0x408200, readByte(0x008200, 0x8000));
        }
        if (rompath != super.path)
        {
            try
            {
                byte[] b = new byte[(int) rom.length()];
                if (length() == 0x400200) {
				b[0xef6b] = b[0xeec6];
				b[0xef6c] = b[0xeec7];
				b[0xef6d] = b[0xeec8];
				b[0xef6e] = b[0xeec9];
				b[0xef6f] = b[0xeeca];
				b[0xef70] = b[0xeecb];
				b[0xef71] = b[0xeecc];
				b[0xef72] = b[0xeecd];
				b[0xef73] = b[0xeece];
				b[0xef74] = b[0xeecf];
				} else if (length() == 0x400000) {
				b[0xed6b] = b[0xecc6];
				b[0xed6c] = b[0xecc7];
				b[0xed6d] = b[0xecc8];
				b[0xed6e] = b[0xecc9];
				b[0xed6f] = b[0xecca];
				b[0xed70] = b[0xeccb];
				b[0xed71] = b[0xeccc];
				b[0xed72] = b[0xeccd];
				b[0xed73] = b[0xecce];
				b[0xed74] = b[0xeccf];
				}
                if (length() == 0x600200) {
				b[0xef6b] = b[0xeec6];
				b[0xef6c] = b[0xeec7];
				b[0xef6d] = b[0xeec8];
				b[0xef6e] = b[0xeec9];
				b[0xef6f] = b[0xeeca];
				b[0xef70] = b[0xeecb];
				b[0xef71] = b[0xeecc];
				b[0xef72] = b[0xeecd];
				b[0xef73] = b[0xeece];
				b[0xef74] = b[0xeecf];
				} else if (length() == 0x600000) {
				b[0xed6b] = b[0xecc6];
				b[0xed6c] = b[0xecc7];
				b[0xed6d] = b[0xecc8];
				b[0xed6e] = b[0xecc9];
				b[0xed6f] = b[0xecca];
				b[0xed70] = b[0xeccb];
				b[0xed71] = b[0xeccc];
				b[0xed72] = b[0xeccd];
				b[0xed73] = b[0xecce];
				b[0xed74] = b[0xeccf];
				}
				newRom = new RandomAccessFile(rompath, "rwd");
                rom.seek(0);
                newRom.seek(0);
				rom.read(b);
                newRom.write(b);
                rom = newRom;
                path = rompath;
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            setDefaultDir(rompath.getParent());
        }

        return true;
    }

    protected boolean _expand()
    {
        try
        {
            rom.seek(rom.length());
            byte[] b = new byte[1 << 20]; // 1 mebibyte
            for (int j = 0; j < 4096; j++)
                b[(j * 256) + 255] = 2;
            rom.write(b);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected boolean _expandEx()
    {
        try
        {
            rom.seek(rom.length());
            byte[] b = new byte[2 << 20]; // 2 mebibytes
            write(0x0101d5, 0x25);
            write(0x0101d7, 0x0d);
            System.arraycopy(readByte(0x008200, 0x8000), 0, b, 0x8000, 0x8000);
            rom.write(b);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean _truncate(int newLen)
    {
        try
        {
            rom.setLength(newLen);
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace(System.out);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#length()
     */
    public int length()
    {
        try
        {
            return (int) rom.length();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.starmen.pkhack.AbstractRom#isDirectFileIO()
     */
    public boolean isDirectFileIO()
    {
        return true;
    }
}