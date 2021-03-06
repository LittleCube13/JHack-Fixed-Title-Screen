package net.starmen.pkhack;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A wrapper for a <code>JComboBox</code> that has a text field. 
 * Whenever the text field is changed, the JComboBox is changed to reflect it
 * and vice versa.
 * 
 * @author EBisumaru
 * @see #comboBox
 */
public class AutoSearchBox extends JComponent implements ActionListener, KeyListener
{
	/**
	 * The JComboBox this is a wrapper for.
	 */
	private JComboBox comboBox;
	private JTextField tf;
	private JLabel label;
	private boolean incTf,//include the text field
		corr = true, //correlate the text box and combo box
		findOnlyExact; //if true, 11 will not be returned for a search for 1
	private int size;
	private int numberIndex=0; //index of the number to be matched with in the strings
	//of the combobox
	/**
	 * Creates a new AutoSearchBox wrapper for the specified JComboBox.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchSize The size of the search text field
	 */
	public AutoSearchBox(JComboBox jcb, String text, int searchSize)
	{
		this(jcb, text, searchSize, true);
	}

	/**
	 * Creates a new AutoSearchBox wrapper for the specified JComboBox.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 */
	public AutoSearchBox(JComboBox jcb, String text)
	{
		this(jcb, text, true);
	}
	
	/**
	 * Creates a new AutoSearchBox wrapper for the specified JComboBox.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchLeft If true, search box is left of combo box
	 */
	public AutoSearchBox(JComboBox jcb, String text, boolean searchLeft)
	{
		this(jcb, text, 10, searchLeft);
	}
	
	/**
	 * Creates a new AutoSearchBox wrapper for the specified JComboBox.
	 * Will find 11 on a search for 1.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchSize The size of the search text field
	 * @param searchLeft If true, search box is left of combo box
	 */
	public AutoSearchBox(JComboBox jcb, String text, int searchSize, boolean searchLeft)
	{
		this(jcb,text,searchSize,searchLeft,false);
	}
	/**
	 * Creates a new AutoSearchBox wrapper for the specified JComboBox.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchSize The size of the search text field
	 * @param searchLeft If true, search box is left of combo box
	 * @param findOnlyExact If true, searches will only find exact numbers
	 *   rather than ones that start with the search
	 */
	public AutoSearchBox(JComboBox jcb, String text, int searchSize, boolean searchLeft,
			boolean findOnlyExact)
	{
		super();
		this.comboBox = jcb;
		this.findOnlyExact = findOnlyExact;
		comboBox.setActionCommand("effectSel");
		comboBox.addActionListener(this);
		size = searchSize;
		incTf = true;
		this.tf = new JTextField(size);
		tf.getDocument().addDocumentListener(new DocumentListener()
				{
			public void changedUpdate(DocumentEvent de)
			{
			}
			public void insertUpdate(DocumentEvent de)
			{
				tfChanged();
			}
			public void removeUpdate(DocumentEvent de)
			{
				tfChanged();
			}
		});
		this.label = new JLabel(text);
		this.initGraphics(searchLeft);
	}
	/**
	 * Makes the specified JTextField a new AutoSearchBox wrapper for the 
	 * specified JComboBox. Will find 11 on a search for 1.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchSize The size of the search text field
	 * @param searchLeft If true, search box is left of combo box
	 * @param incTf Whether to include text field in display
	 */
	public AutoSearchBox(JComboBox jcb, JTextField jtf, String text, 
		boolean searchLeft, boolean incTf)
	{
		this(jcb,jtf,text,searchLeft,incTf,false);
	}
	/**
	 * Makes the specified JTextField a new AutoSearchBox wrapper for the 
	 * specified JComboBox.
	 * 
	 * @param jcb The JComboBox this is a wrapper for.
	 * @param text Text to label the JComboBox with.
	 * @param searchSize The size of the search text field
	 * @param searchLeft If true, search box is left of combo box
	 * @param incTf Whether to include text field in display
	 * @param findOnlyExact If true, searches will only find exact numbers
	 *   rather than ones that start with the search
	 */
	public AutoSearchBox(JComboBox jcb, JTextField jtf, String text, 
		boolean searchLeft, boolean incTf, boolean findOnlyExact)
	{
		super();
		this.findOnlyExact = findOnlyExact;
		this.comboBox = jcb;
		comboBox.setActionCommand("effectSel");
		comboBox.addActionListener(this);
		this.tf = jtf;
		this.incTf = incTf;
		tf.getDocument().addDocumentListener(new DocumentListener()
				{
			public void changedUpdate(DocumentEvent de)
			{
			}
			public void insertUpdate(DocumentEvent de)
			{
				tfChanged();
			}
			public void removeUpdate(DocumentEvent de)
			{
				tfChanged();
			}
		});
		this.label = new JLabel(text);
		this.initGraphics(searchLeft);
	}

	private void initGraphics(boolean searchLeft)
	{
		setLayout(new BorderLayout());

		tf.addKeyListener(this);

		if(incTf)
			add(tf,	(searchLeft ? BorderLayout.WEST : BorderLayout.EAST));
		add(HackModule.pairComponents(label, comboBox, true),
				(searchLeft ? BorderLayout.EAST : BorderLayout.WEST));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae)
	{
		Object a = comboBox.getSelectedItem();
		if (corr && (a != null))
		{
			String _ = a.toString();
			tf.setText(_.substring(0,_.indexOf(" ")));	
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent ke)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent ke)
	{}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent ke)
	{}
	public void setEnabled(boolean enable)
	{
		super.setEnabled(enable);
		if(enable)
		{
			tf.setEnabled(true);
			comboBox.setEnabled(true);
			label.setEnabled(true);
		}
		else
		{
			tf.setEnabled(false);
			comboBox.setEnabled(false);
			label.setEnabled(false);
		}
	}
	
	public void tfChanged()
	{
		if(corr)
		{
			comboBox.removeActionListener(this);
			if (!search(tf.getText(), comboBox, true, false, numberIndex, 
					findOnlyExact))
			{	
				comboBox.addItem(tf.getText() + " ???");
				comboBox.setSelectedItem(tf.getText() + " ???");
			}
			comboBox.addActionListener(this);
		}
	}
	
	public String getText()
	{
		return tf.getText();
	}
	
	public void setText(String text)
	{
		tf.setText(text);
	}
	
	public JTextField getTF()
	{
		return tf;
	}
	
	public JComboBox getCB()
	{
		return comboBox;
	}
	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If
	 * the <code>String</code> is found inside an item in the
	 * <code>JComboBox</code> then the item is selected on the JComboBox and
	 * true is returned. The search starts at the item after the currently
	 * selected item or the first item if the last item is currently selected.
	 * If the search <code>String</code> is not found, then an error dialog
	 * pops up and the function returns false.
	 * 
	 * @param text <code>String</code> to seach for. Can be anywhere inside
	 *            any item.
	 * @param selector <code>JComboBox</code> to get list from, and set if
	 *            search is found.
	 * @param beginFromStart If true, search starts at first item.
	 * @param displayError If false, the error dialog will never be displayed
	 * @param findOnlyExact Find only values of exact size
	 * @return Whether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector,
			boolean beginFromStart, boolean displayError, int numberIndex, 
			boolean findOnlyExact)
	{
		text = text.toLowerCase();
		for (int i = (selector.getSelectedIndex() + 1 != selector
				.getItemCount()
				&& !beginFromStart ? selector.getSelectedIndex() + 1 : 0); i < selector
				.getItemCount(); i++)
		{
			String field = selector.getItemAt(i).toString().toLowerCase();
			if (field.indexOf(text) == numberIndex)
			{
/*				if (selector.getSelectedIndex() == -1 && selector.isEditable())
				{
					selector.setEditable(false);
					selector.setSelectedIndex(i == 0 ? 1 : 0);
					selector.setSelectedIndex(i);
					selector.setEditable(true);
				}
*/				if(findOnlyExact)
				{
					if(field.indexOf(" ", numberIndex) == numberIndex + text.length())
					{
						selector.setSelectedIndex(i);
						selector.repaint();
						return true;
					}
					else;
				}
				else
				{
					selector.setSelectedIndex(i);
					selector.repaint();
					return true;
				}
			}
		}

		if (beginFromStart)
		{
				System.out.println("Search not found.");
			return false;
		}
		else
		{
			return search(text, selector, true, displayError, numberIndex,
					findOnlyExact);
		}
	}

	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If
	 * the <code>String</code> is found inside an item in the
	 * <code>JComboBox</code> then the item is selected on the JComboBox and
	 * true is returned. The search starts at the item after the currently
	 * selected item or the first item if the last item is currently selected.
	 * If the search <code>String</code> is not found, then an error dialog
	 * pops-up and the function returns false.
	 * 
	 * @param text <code>String</code> to seach for. Can be anywhere inside
	 *            any item.
	 * @param selector <code>JComboBox</code> to get list from, and set if
	 *            search is found.
	 * @param beginFromStart If true, search starts at first item.
	 * @return Wether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector,
			boolean beginFromStart, int numberIndex, boolean findOnlyExact)
	{
		return search(text, selector, beginFromStart, true, numberIndex, findOnlyExact);
	}

	/**
	 * Searches for a <code>String</code> in a <code>JComboBox</code>. If
	 * the <code>String</code> is found inside an item in the
	 * <code>JComboBox</code> then the item is selected on the JComboBox and
	 * true is returned. The search starts at the item after the currently
	 * selected item or the first item if the last item is currently selected.
	 * If the search <code>String</code> is not found, then an error dialog
	 * pops-up and the function returns false.
	 * 
	 * @param text <code>String</code> to seach for. Can be anywhere inside
	 *            any item.
	 * @param selector <code>JComboBox</code> to get list from, and set if
	 *            search is found.
	 * @return Wether the search <code>String</code> is found.
	 */
	public static boolean search(String text, JComboBox selector, int numberIndex,
			boolean findOnlyExact)
	{
		return search(text, selector, false, numberIndex, findOnlyExact);
	}
	
	public void setCorr(boolean _) {corr = _;}
	
	public boolean getCorr() {return corr;}
	
	public void setNumberIndex(int _) {numberIndex = _;}
	
	public int getNumberIndex() {return numberIndex;}
}