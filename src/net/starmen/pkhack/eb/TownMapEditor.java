/*
 * Created on Mar 20, 2004
 */
package net.starmen.pkhack.eb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeSelectionModel;

import net.starmen.pkhack.AbstractRom;
import net.starmen.pkhack.CheckNode;
import net.starmen.pkhack.CheckRenderer;
import net.starmen.pkhack.HackModule;
import net.starmen.pkhack.IPSDatabase;
import net.starmen.pkhack.JHack;
import net.starmen.pkhack.NodeSelectionListener;
import net.starmen.pkhack.XMLPreferences;

/**
 * TODO Write javadoc for this class
 * 
 * @author AnyoneEB
 */
public class TownMapEditor extends FullScreenGraphicsEditor
{
    protected String getClassName()
    {
        return "eb.TownMapEditor";
    }

    public static final int NUM_TOWN_MAPS = 6;

    public int getNumScreens()
    {
        return NUM_TOWN_MAPS;
    }

    public TownMapEditor(AbstractRom rom, XMLPreferences prefs)
    {
        super(rom, prefs);

        try
        {
            Class[] c = new Class[]{byte[].class, TownMapEditor.class};
            IPSDatabase.registerExtension("tnm", TownMapEditor.class.getMethod(
                "importData", c), TownMapEditor.class.getMethod("restoreData",
                c), TownMapEditor.class.getMethod("checkData", c), this);
        }
        catch (SecurityException e)
        {
            // no security model, shouldn't have to worry about this
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            // spelling mistake, maybe? ^_^;
            e.printStackTrace();
        }
    }

    public String getVersion()
    {
        return "0.2";
    }

    public String getDescription()
    {
        return "Town Map Editor";
    }

    public String getCredits()
    {
        return "Written by AnyoneEB";
    }

    public static class TownMap extends FullScreenGraphics
    {
        private EbHackModule hm;
        private int num, oldPointer, oldLen;

        /** Number of palettes. */
        public static final int NUM_PALETTES = 2;
        /**
         * Number of arrangements. Note that this is more than fits on the
         * screen, so the last 128 are unused.
         */
        public static final int NUM_ARRANGEMENTS = 1024;
        /** Number of tiles. */
        public static final int NUM_TILES = 512;

        public TownMap(int i, EbHackModule hm)
        {
            this.hm = hm;
            this.num = i;

            oldPointer = toRegPointer(hm.rom.readMulti(0x202390 + (i * 4), 4));
        }

        public int getNumSubPalettes()
        {
            return NUM_PALETTES;
        }

        public int getSubPaletteSize()
        {
            return 16;
        }

        public int getNumArrangements()
        {
            return NUM_ARRANGEMENTS;
        }

        public int getNumTiles()
        {
            return NUM_TILES;
        }

        /**
         * Reads info from the orginal ROM and remembers specified parts.
         * 
         * @param toRead <code>boolean[]</code>:<code>[NODE_TILES]</code>=
         *            remember tiles, <code>[NODE_ARR]</code>= remember
         *            arrangement, <code>[NODE_PAL]</code>= remember palettes
         * @return true on success
         */
        private boolean readOrgInfo(boolean[] toRead)
        {
            AbstractRom r = JHack.main.getOrginalRomFile(hm.rom.getRomType());

            byte[] buffer = new byte[18496];
            System.out.println("About to attempt decompressing "
                + buffer.length + " bytes of town map #" + num + ".");
            int[] tmp = EbHackModule.decomp(oldPointer, buffer, r);
            if (tmp[0] < 0)
            {
                System.out.println("Error " + tmp[0]
                    + " decompressing town map #" + num + ".");
                return false;
            }
            oldLen = tmp[1];
            System.out.println("TownMap: Decompressed " + tmp[0]
                + " bytes from a " + tmp[1] + " byte compressed block.");

            int offset = 0;
            if (toRead[NODE_PAL])
            {
                palette = new Color[NUM_PALETTES][16];
                for (int i = 0; i < NUM_PALETTES; i++)
                {
                    Color[] target = palette[i];
                    HackModule.readPalette(buffer, offset, target);
                    offset += palette[i].length * 2;
                }
            }
            else
            {
                offset += NUM_PALETTES * 16 * 2;
            }
            if (toRead[NODE_ARR])
            {
                arrangementList = new short[NUM_ARRANGEMENTS];
                arrangement = new short[32][28];
                for (int i = 0; i < NUM_ARRANGEMENTS; i++)
                {
                    arrangementList[i] = (short) ((buffer[offset++] & 0xff) + ((buffer[offset++] & 0xff) << 8));
                }
                int j = 0;
                for (int y = 0; y < arrangement[0].length; y++)
                    for (int x = 0; x < arrangement.length; x++)
                        arrangement[x][y] = arrangementList[j++];
            }
            else
            {
                offset += NUM_ARRANGEMENTS * 2;
            }
            if (toRead[NODE_TILES])
            {
                tiles = new byte[NUM_TILES][8][8];
                for (int i = 0; i < NUM_TILES; i++)
                {
                    offset += HackModule.read4BPPArea(tiles[i], buffer, offset,
                        0, 0);
                }
            }

            return true;
        }

        public boolean readInfo(boolean allowFailure)
        {
            if (isInited)
                return true;

            byte[] buffer = new byte[18496];
            System.out.println("About to attempt decompressing "
                + buffer.length + " bytes of town map #" + num + ".");
            int[] tmp = hm.decomp(oldPointer, buffer);
            if (tmp[0] < 0)
            {
                String err = "Error " + tmp[0] + " decompressing town map #"
                    + num + ".";
                System.out.println(err);
                if (!allowFailure)
                {
                    return false;
                }
            }
            oldLen = tmp[1];
            System.out.println("TownMap: Decompressed " + tmp[0]
                + " bytes from a " + tmp[1] + " byte compressed block.");

            int offset = 0;
            palette = new Color[NUM_PALETTES][16];
            for (int i = 0; i < NUM_PALETTES; i++)
            {
                HackModule.readPalette(buffer, offset, palette[i]);
                offset += palette[i].length * 2;
            }
            arrangementList = new short[NUM_ARRANGEMENTS];
            arrangement = new short[32][28];
            for (int i = 0; i < NUM_ARRANGEMENTS; i++)
            {
                arrangementList[i] = (short) ((buffer[offset++] & 0xff) + ((buffer[offset++] & 0xff) << 8));
            }
            int j = 0;
            for (int y = 0; y < arrangement[0].length; y++)
                for (int x = 0; x < arrangement.length; x++)
                    arrangement[x][y] = arrangementList[j++];
            tiles = new byte[NUM_TILES][8][8];
            for (int i = 0; i < NUM_TILES; i++)
            {
                offset += HackModule.read4BPPArea(tiles[i], buffer, offset, 0,
                    0);
            }

            isInited = true;
            return tmp[0] >= 0;
        }

        public boolean readInfo()
        {
            return readInfo(true); // XXX should this be false?
        }

        /**
         * Inits all values to zero. Will have no effect if {@link #readInfo()}
         * or this has already been run successfully. Use this if
         * <code>readInfo()</code> always fails.
         */
        public void initToNull()
        {
            if (isInited)
                return;

            // EMPTY PALETTES
            for (int i = 0; i < palette.length; i++)
                Arrays.fill(palette[i], Color.BLACK);

            // EMPTY ARRANGEMENTS
            Arrays.fill(arrangementList, (short) 0);
            for (int x = 0; x < arrangement.length; x++)
                Arrays.fill(arrangement[x], (short) 0);

            // EMPTY TILES
            for (int i = 0; i < tiles.length; i++)
                for (int x = 0; x < tiles[i].length; x++)
                    Arrays.fill(tiles[i][x], (byte) 0);

            // mark length as zero to prevent problems
            oldLen = 0;

            isInited = true;
        }

        public boolean writeInfo()
        {
            if (!isInited)
                return false;

            byte[] udata = new byte[18496];
            int offset = 0;
            for (int i = 0; i < NUM_PALETTES; i++)
            {
                HackModule.writePalette(udata, offset, palette[i]);
                offset += palette[i].length * 2;
            }
            int j = 0;
            for (int y = 0; y < arrangement[0].length; y++)
                for (int x = 0; x < arrangement.length; x++)
                    arrangementList[j++] = arrangement[x][y];
            for (int i = 0; i < NUM_ARRANGEMENTS; i++)
            {
                udata[offset++] = (byte) (arrangementList[i] & 0xff);
                udata[offset++] = (byte) ((arrangementList[i] >> 8) & 0xff);
            }

            for (int i = 0; i < NUM_TILES; i++)
            {
                offset += HackModule.write4BPPArea(tiles[i], udata, offset, 0,
                    0);
            }

            byte[] compMap; // , compTilesetTv;
            int compLen = comp(udata, compMap = new byte[20000]);

            if (!hm.writeToFree(compMap, 0x202390 + (num * 4), oldLen, compLen))
                return false;
            System.out.println("Wrote "
                + (oldLen = compLen)
                + " bytes of tileset #"
                + num
                + " tiles at "
                + Integer.toHexString(oldPointer = toRegPointer(hm.rom
                    .readMulti(0x202390 + (num * 4), 4))) + " to "
                + Integer.toHexString(oldPointer + compLen - 1) + ".");

            return true;
        }

    }

    public static final TownMap[] townMaps = new TownMap[NUM_TOWN_MAPS];

    public static void readFromRom(EbHackModule hm)
    {
        for (int i = 0; i < townMaps.length; i++)
        {
            townMaps[i] = new TownMap(i, hm);
        }
    }

    protected void readFromRom()
    {
        readFromRom(this);
    }

    /**
     * Reads in {@link EbHackModule#townMapNames}if it hasn't already been read
     * in. Reads from net/starmen/pkhack/townMapNames.txt.
     */
    public static void initTownMapNames(String romPath)
    {
        readArray(TownMapEditor.class.getClassLoader(), DEFAULT_BASE_DIR,
            "townMapNames.txt", romPath, false, townMapNames);
    }

    public FullScreenGraphics getScreen(int num)
    {
        return townMaps[num];
    }

    public String getScreenName(int num)
    {
        return townMapNames[num];
    }

    public String[] getScreenNames()
    {
        return townMapNames;
    }

    public void setScreenName(int num, String newName)
    {
        townMapNames[num] = newName;
    }

    public void reset()
    {
        initTownMapNames(rom.getPath());
        readFromRom();
    }

    protected int getTileSelectorWidth()
    {
        return 32;
    }

    protected int getTileSelectorHeight()
    {
        return 16;
    }

    protected int focusDaDir()
    {
        return SwingConstants.TOP;
    }

    protected int focusArrDir()
    {
        return SwingConstants.LEFT;
    }

    protected JComponent layoutComponents()
    {
        Box center = new Box(BoxLayout.Y_AXIS);
        center.add(getLabeledComponent("Map: ", screenSelector));
        center.add(getLabeledComponent("Map Name: ", name));
        center.add(getLabeledComponent("SubPalette: ", subPalSelector));
        center.add(Box.createVerticalStrut(20));
        center.add(createFlowLayout(da));
        center.add(Box.createVerticalStrut(5));
        center.add(createFlowLayout(pal));
        center.add(Box.createVerticalStrut(100));
        center.add(createFlowLayout(fi));
        center.add(Box.createVerticalGlue());

        JPanel display = new JPanel(new BorderLayout());
        display.add(pairComponents(center, null, false), BorderLayout.CENTER);
        display.add(pairComponents(dt, null, false), BorderLayout.EAST);
        display.add(pairComponents(tileSelector, arrangementEditor, false),
            BorderLayout.WEST);

        return display;
    }

    public static final int NODE_BASE = 0;
    public static final int NODE_TILES = 1;
    public static final int NODE_ARR = 2;
    public static final int NODE_PAL = 3;

    public static class TownMapImportData
    {
        public byte[][][] tiles;
        public short[] arrangement;
        public Color[][] palette;
    }

    public static final byte TNM_VERSION = 1;

    public static boolean exportData(File f, boolean[][] a)
    {
        // make a byte whichMaps. for each map if it is used set the bit at the
        // place equal to the map number to 1
        byte whichMaps = 0;
        for (int i = 0; i < a.length; i++)
            whichMaps |= (a[i][NODE_BASE] ? 1 : 0) << i;

        try
        {
            FileOutputStream out = new FileOutputStream(f);

            out.write(TNM_VERSION);
            out.write(whichMaps);
            for (int m = 0; m < a.length; m++)
            {
                if (a[m][NODE_BASE])
                {
                    // if writing this map...
                    // say what parts we will write, once again as a bit mask
                    byte whichParts = 0;
                    for (int i = 1; i < a[m].length; i++)
                        whichParts |= (a[m][i] ? 1 : 0) << (i - 1);
                    out.write(whichParts);
                    // write tiles?
                    if (a[m][NODE_TILES])
                    {
                        byte[] b = new byte[TownMap.NUM_TILES * 32];
                        int offset = 0;
                        for (int i = 0; i < TownMap.NUM_TILES; i++)
                            offset += write4BPPArea(townMaps[m].getTile(i), b,
                                offset, 0, 0);
                        out.write(b);
                    }
                    // write arrangements?
                    if (a[m][NODE_ARR])
                    {
                        short[] arr = townMaps[m].getArrangementArr();
                        byte[] barr = new byte[arr.length * 2];
                        int off = 0;
                        for (int i = 0; i < arr.length; i++)
                        {
                            barr[off++] = (byte) (arr[i] & 0xff);
                            barr[off++] = (byte) ((arr[i] >> 8) & 0xff);
                        }
                        out.write(barr);
                    }
                    // write palettes?
                    if (a[m][NODE_PAL])
                    {
                        byte[] pal = new byte[64];
                        writePalette(pal, 0, townMaps[m].getSubPal(0));
                        writePalette(pal, 32, townMaps[m].getSubPal(1));
                        out.write(pal);
                    }
                }
            }

            out.close();
            return true;
        }
        catch (FileNotFoundException e)
        {
            System.err
                .println("File not found error exporting town map data to "
                    + f.getAbsolutePath() + ".");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("IO error exporting town map data to "
                + f.getAbsolutePath() + ".");
            e.printStackTrace();
            return false;
        }
    }

    public static TownMapImportData[] importData(InputStream in)
        throws IOException
    {
        TownMapImportData[] out = new TownMapImportData[townMaps.length];

        byte version = (byte) in.read();
        if (version > TNM_VERSION)
        {
            if (JOptionPane.showConfirmDialog(null,
                "TNM file version not supported." + "Try to load anyway?",
                "TMN Version " + version + " Not Supported",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                return null;
        }
        byte whichMaps = (byte) in.read();
        for (int m = 0; m < townMaps.length; m++)
        {
            // if bit for this map set...
            if (((whichMaps >> m) & 1) != 0)
            {
                out[m] = new TownMapImportData();
                byte whichParts = (byte) in.read();
                // if tile bit set...
                if ((whichParts & 1) != 0)
                {
                    byte[] b = new byte[TownMap.NUM_TILES * 32];
                    in.read(b);

                    int offset = 0;
                    out[m].tiles = new byte[TownMap.NUM_TILES][8][8];
                    for (int i = 0; i < TownMap.NUM_TILES; i++)
                        offset += read4BPPArea(out[m].tiles[i], b, offset, 0, 0);
                }
                // if arr bit set...
                if (((whichParts >> 1) & 1) != 0)
                {
                    out[m].arrangement = new short[TownMap.NUM_ARRANGEMENTS];
                    byte[] barr = new byte[out[m].arrangement.length * 2];
                    in.read(barr);

                    int off = 0;
                    for (int i = 0; i < out[m].arrangement.length; i++)
                    {
                        out[m].arrangement[i] = (short) ((barr[off++] & 0xff) + ((barr[off++] & 0xff) << 8));
                    }
                }
                // if pal bit set...
                if (((whichParts >> 2) & 1) != 0)
                {
                    byte[] pal = new byte[64];
                    in.read(pal);

                    out[m].palette = new Color[2][16];
                    readPalette(pal, 0, out[m].palette[0]);
                    readPalette(pal, 32, out[m].palette[1]);
                }
            }
        }
        in.close();

        return out;
    }

    public static TownMapImportData[] importData(File f)
    {
        try
        {
            return importData(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            System.err
                .println("File not found error importing town map data from "
                    + f.getAbsolutePath() + ".");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.err.println("IO error importing town map data from "
                + f.getAbsolutePath() + ".");
            e.printStackTrace();
        }
        return null;
    }

    public static TownMapImportData[] importData(byte[] b)
    {
        try
        {
            return importData(new ByteArrayInputStream(b));
        }
        catch (IOException e)
        {
            System.err.println("IO error importing Town Map data from "
                + "byte array.");
            e.printStackTrace();
        }
        return null;
    }

    protected boolean exportData()
    {
        CheckNode topNode = new CheckNode("Town Maps", true, true);
        topNode.setSelectionMode(CheckNode.DIG_IN_SELECTION);
        CheckNode[][] mapNodes = new CheckNode[NUM_TOWN_MAPS][4];
        for (int i = 0; i < mapNodes.length; i++)
        {
            mapNodes[i][NODE_BASE] = new CheckNode(townMapNames[i], true, true);
            mapNodes[i][NODE_BASE].setSelectionMode(CheckNode.DIG_IN_SELECTION);
            mapNodes[i][NODE_BASE].add(mapNodes[i][NODE_TILES] = new CheckNode(
                "Tiles", false, true));
            mapNodes[i][NODE_BASE].add(mapNodes[i][NODE_ARR] = new CheckNode(
                "Arrangement", false, true));
            mapNodes[i][NODE_BASE].add(mapNodes[i][NODE_PAL] = new CheckNode(
                "Palettes", false, true));

            topNode.add(mapNodes[i][NODE_BASE]);
        }
        JTree checkTree = new JTree(topNode);
        checkTree.setCellRenderer(new CheckRenderer());
        checkTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        checkTree.putClientProperty("JTree.lineStyle", "Angled");
        checkTree.addMouseListener(new NodeSelectionListener(checkTree));

        if (JOptionPane.showConfirmDialog(mainWindow, pairComponents(
            new JLabel("<html>" + "Select which items you wish to export."
                + "</html>"), new JScrollPane(checkTree), false),
            "Export What?", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION)
            return false;

        boolean[][] a = new boolean[NUM_TOWN_MAPS][4];
        for (int m = 0; m < NUM_TOWN_MAPS; m++)
            for (int i = 0; i < 4; i++)
                a[m][i] = mapNodes[m][i].isSelected();

        File f = getFile(true, "tnm", "TowN Map");
        if (f != null)
            return exportData(f, a);
        else
            return false;
    }

    private static boolean[][] showCheckList(boolean[][] in, String text,
        String title)
    {
        CheckNode topNode = new CheckNode("Town Maps", true, true);
        topNode.setSelectionMode(CheckNode.DIG_IN_SELECTION);
        CheckNode[][] mapNodes = new CheckNode[NUM_TOWN_MAPS][4];
        for (int i = 0; i < mapNodes.length; i++)
        {
            if (in == null || in[i][NODE_BASE])
            {
                mapNodes[i][NODE_BASE] = new CheckNode(townMapNames[i], true,
                    true);
                mapNodes[i][NODE_BASE]
                    .setSelectionMode(CheckNode.DIG_IN_SELECTION);
                if (in == null || in[i] == null || in[i][NODE_TILES])
                    mapNodes[i][NODE_BASE]
                        .add(mapNodes[i][NODE_TILES] = new CheckNode("Tiles",
                            false, true));
                if (in == null || in[i] == null || in[i][NODE_ARR])
                    mapNodes[i][NODE_BASE]
                        .add(mapNodes[i][NODE_ARR] = new CheckNode(
                            "Arrangement", false, true));
                if (in == null || in[i] == null || in[i][NODE_PAL])
                    mapNodes[i][NODE_BASE]
                        .add(mapNodes[i][NODE_PAL] = new CheckNode("Palettes",
                            false, true));

                topNode.add(mapNodes[i][NODE_BASE]);
            }
        }
        JTree checkTree = new JTree(topNode);
        checkTree.setCellRenderer(new CheckRenderer());
        checkTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        checkTree.putClientProperty("JTree.lineStyle", "Angled");
        checkTree.addMouseListener(new NodeSelectionListener(checkTree));

        // if user clicked cancel, don't take action
        if (JOptionPane.showConfirmDialog(null, pairComponents(
            new JLabel(text), new JScrollPane(checkTree), false), title,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION)
            return null;

        final boolean[][] a = new boolean[NUM_TOWN_MAPS][4];
        for (int m = 0; m < NUM_TOWN_MAPS; m++)
            for (int i = 0; i < 4; i++)
                a[m][i] = mapNodes[m][i] == null ? false : mapNodes[m][i]
                    .isSelected();

        return a;
    }

    protected boolean importData()
    {
        File f = getFile(false, "tnm", "TowN Map");
        TownMapImportData[] tmid;
        if (f == null || (tmid = importData(f)) == null)
            return false;
        return importData(tmid);
    }

    private boolean importData(TownMapImportData[] tmid)
    {
        boolean[][] in = new boolean[NUM_TOWN_MAPS][4];
        for (int i = 0; i < in.length; i++)
        {
            if (tmid[i] != null)
            {
                in[i][NODE_BASE] = true;
                in[i][NODE_TILES] = tmid[i].tiles != null;
                in[i][NODE_ARR] = tmid[i].arrangement != null;
                in[i][NODE_PAL] = tmid[i].palette != null;
            }
        }

        final boolean[][] a = showCheckList(in, "<html>"
            + "Select which items you wish to<br>"
            + "import. You will have a chance<br>"
            + "to select which map you want to<br>"
            + "actually put the imported data." + "</html>", "Import What?");
        if (a == null)
            return false;

        Box targetMap = new Box(BoxLayout.Y_AXIS);
        final JComboBox[] targets = new JComboBox[NUM_TOWN_MAPS];
        for (int m = 0; m < targets.length; m++)
        {
            if (a[m][NODE_BASE])
            {
                targets[m] = createComboBox(townMapNames);
                targets[m].setSelectedIndex(m);
                targetMap.add(getLabeledComponent(townMapNames[m] + " ("
                    + (a[m][NODE_TILES] ? "T" : "")
                    + (a[m][NODE_ARR] ? "A" : "") + (a[m][NODE_PAL] ? "P" : "")
                    + "): ", targets[m]));
            }
        }

        final JDialog targetDialog = new JDialog(mainWindow,
            "Select Import Targets", true);
        targetDialog.getContentPane().setLayout(new BorderLayout());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                targetDialog.setTitle("Canceled");
                targetDialog.setVisible(false);
            }
        });
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                // t = array of used targets
                boolean[][] t = new boolean[NUM_TOWN_MAPS][4];
                // m = source map
                for (int m = 0; m < NUM_TOWN_MAPS; m++)
                {
                    if (targets[m] != null)
                    {
                        // n = target map
                        int n = targets[m].getSelectedIndex();
                        for (int i = 1; i < 4; i++)
                        {
                            if (a[m][i])
                            {
                                // if part already used...
                                if (t[n][i])
                                {
                                    // fail
                                    JOptionPane.showMessageDialog(targetDialog,
                                        "Imported data must not overlap,\n"
                                            + "check your targets.",
                                        "Invalid Selection Error",
                                        JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                                else
                                {
                                    // set target part as used
                                    t[n][i] = true;
                                }
                            }
                        }
                    }
                }
                targetDialog.setVisible(false);
            }
        });
        targetDialog.getContentPane().add(
            createFlowLayout(new Component[]{ok, cancel}), BorderLayout.SOUTH);
        targetDialog.getContentPane()
            .add(
                pairComponents(new JLabel("<html>"
                    + "Select which map you would like<br>"
                    + "the data to be imported into.<br>"
                    + "For example, if you wish to import<br>" + "the "
                    + townMapNames[0] + " map into the<br>" + townMapNames[1]
                    + " map, then change the pull-down menu<br>" + "labeled "
                    + townMapNames[0] + " to " + townMapNames[1]
                    + ". If you do not<br>"
                    + "wish to make any changes, just click ok.<br>" + "<br>"
                    + "The T, A, and P indictate that you will be<br>"
                    + "importing tiles, arrangements, and palettes<br>"
                    + "respectively from that map." + "</html>"), targetMap,
                    false), BorderLayout.CENTER);
        targetDialog.pack();

        targetDialog.setVisible(true);
        if (targetDialog.getTitle().equals("Canceled"))
            return false;

        for (int m = 0; m < NUM_TOWN_MAPS; m++)
        {
            if (a[m][NODE_BASE])
            {
                int n = targets[m].getSelectedIndex();
                if (a[m][NODE_TILES])
                    for (int i = 0; i < TownMap.NUM_TILES; i++)
                        townMaps[n].setTile(i, tmid[m].tiles[i]);
                if (a[m][NODE_ARR])
                    townMaps[n].setArrangementArr(tmid[m].arrangement);
                if (a[m][NODE_PAL])
                    for (int p = 0; p < TownMap.NUM_PALETTES; p++)
                        for (int c = 0; c < 16; c++)
                            townMaps[n].setPaletteColor(c, p,
                                tmid[m].palette[p][c]);
            }
        }

        return true;
    }

    /**
     * Imports data from the given <code>byte[]</code> based on user input.
     * User input will always be expected by this method. This method exists to
     * be called by <code>IPSDatabase</code> for "applying" files with .tnm
     * extensions.
     * 
     * @param b <code>byte[]</code> containing exported data
     * @param tme instance of <code>LogoScreenEditor</code> to call
     *            <code>importData()</code> on
     */
    public static boolean importData(byte[] b, TownMapEditor tme)
    {
        boolean out = tme.importData(importData(b));
        if (out)
        {
            if (tme.mainWindow != null)
            {
                tme.mainWindow.repaint();
                tme.updatePaletteDisplay();
                tme.tileSelector.repaint();
                tme.arrangementEditor.clearSelection();
                tme.arrangementEditor.repaint();
                tme.updateTileEditor();
            }
            for (int i = 0; i < townMaps.length; i++)
                townMaps[i].writeInfo();
        }
        return out;
    }

    private static boolean checkMap(TownMapImportData tmid, int i)
    {
        if (!townMaps[i].readInfo(false))
            return false;
        if (tmid.tiles != null)
        {
            // check tiles
            for (int t = 0; t < tmid.tiles.length; t++)
                for (int x = 0; x < tmid.tiles[t].length; x++)
                    if (!Arrays.equals(tmid.tiles[t][x],
                        townMaps[i].tiles[t][x]))
                        return false;
        }
        if (tmid.arrangement != null)
        {
            // check arrangement
            if (!Arrays.equals(tmid.arrangement, townMaps[i]
                .getArrangementArr()))
                return false;
        }
        if (tmid.palette != null)
        {
            // check palette
            for (int p = 0; p < tmid.palette.length; p++)
                for (int c = 0; c < tmid.palette[p].length; c++)
                    if (!tmid.palette[p][c].equals(townMaps[i].palette[p][c]))
                        return false;
        }

        // nothing found wrong
        return true;
    }

    private static boolean checkMap(TownMapImportData gid)
    {
        for (int i = 0; i < NUM_TOWN_MAPS; i++)
            if (checkMap(gid, i))
                return true;
        return false;
    }

    /**
     * Checks if data from the given <code>byte[]</code> has been imported.
     * This method exists to be called by <code>IPSDatabase</code> for
     * "checking" files with .tnm extensions.
     * 
     * @param b <code>byte[]</code> containing exported data
     * @param tme instance of <code>TownMapEditor</code>
     */
    public static boolean checkData(byte[] b, TownMapEditor tme)
    {
        TownMapImportData[] tmid = importData(b);

        for (int i = 0; i < tmid.length; i++)
            if (tmid[i] != null)
                if (!checkMap(tmid[i]))
                    return false;

        return true;
    }

    /**
     * Restore data from the given <code>byte[]</code> based on user input.
     * User input will always be expected by this method. This method exists to
     * be called by <code>IPSDatabase</code> for "unapplying" files with .tnm
     * extensions.
     * 
     * @param b <code>byte[]</code> containing exported data
     * @param tme instance of <code>TownMapEditor</code>
     */
    public static boolean restoreData(byte[] b, TownMapEditor tme)
    {
        boolean[][] a = showCheckList(null, "<html>Select which items you wish"
            + "to restore to the orginal EarthBound verions.</html>",
            "Restore what?");
        if (a == null)
            return false;

        for (int i = 0; i < a.length; i++)
        {
            if (a[i][NODE_BASE])
            {
                townMaps[i].readOrgInfo(a[i]);
                townMaps[i].writeInfo();
            }
        }

        if (tme.mainWindow != null)
        {
            tme.mainWindow.repaint();
            tme.updatePaletteDisplay();
            tme.tileSelector.repaint();
            tme.arrangementEditor.clearSelection();
            tme.arrangementEditor.repaint();
            tme.updateTileEditor();
        }

        return true;
    }
}