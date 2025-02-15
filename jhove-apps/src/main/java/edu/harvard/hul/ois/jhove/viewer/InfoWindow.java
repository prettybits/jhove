/**********************************************************************
 * Jhove - JSTOR/Harvard Object Validation Environment
 * Copyright 2003 by JSTOR and the President and Fellows of Harvard College
 **********************************************************************/

package edu.harvard.hul.ois.jhove.viewer;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.OutputHandler;

/**
 * An abstract superclass for windows that display information
 * and can be saved to a file.
 * 
 * @author Gary McGath
 */
public abstract class InfoWindow extends JFrame 
{
    protected App _app;
    private JhoveBase _base;
    private JMenuItem _saveItem;
    private JMenuItem _closeItem;
    private JComboBox<String> _handlerBox;
    private JComboBox<String> _encodingBox;
    private static String _lastEncoding;
    private static String _lastHandler;
    
    protected static final String eol = System.getProperty("line.separator");
    private static final String[] encodings =
        { "UTF-8", "ISO-8859-1", "Cp1252", "MacRoman"};
    protected SimpleDateFormat _dateFmt;

    private static final String FILE_NOT_SAVED = "File not saved";
    
    /**
     * 
     * Constructor. 
     * The window is created with a File menu that has
     * "Save as" and "Close" items.
     * 
     * @param title   Window title.  Will be truncated to 32 characters.
     * @param app     The associated App object.
     * @param base    The associated JhoveBase object.
     * 
     * @throws HeadlessException
     */
    public InfoWindow(String title, App app, JhoveBase base) 
                throws HeadlessException {
        super(title);

        // Avoid silly window titles from excessively long URI's
        if (title.length () > 32) {
            setTitle(title.substring(0,29) + "...");
        }
        _app = app;
        _base = base;
        JMenuBar menuBar = new JMenuBar ();
        JMenu fileMenu = new JMenu ("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add (fileMenu);
        _saveItem = new JMenuItem ("Save as...");
        _saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add (_saveItem);
        
        _closeItem = new JMenuItem ("Close");
        fileMenu.add (_closeItem);
        // Make mnemonic control-W, command-W, or whatever-W
        _closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        _closeItem.addActionListener (e -> closeFromMenu ());
        
        setDefaultCloseOperation (WindowConstants.HIDE_ON_CLOSE);
        setJMenuBar (menuBar);
        _dateFmt = new SimpleDateFormat ("yyyy-MM-dd");
    }

    /** Sets the ActionListener for the "Save as" menu item.
     *  Subclasses need to call this with an appropriate
     *  ActionListener in order to make the menu item
     *  functional.
     */
    protected void setSaveActionListener (ActionListener listener)
    {
        _saveItem.addActionListener (listener);
    }
    
    /** Puts up a dialog to save the file.
     *  If the user requests a file, deletes any old file
     *  with the same name, creates the new file, and
     *  returns a PrintWriter to the file.
     *  The save dialog is customized with two
     *  JComboBoxes. One lets the user
     *  select a character encoding, which is used by
     *  the PrintWriter; the other lets the user
     *  choose an OutputHandler to control the output
     *  format (e.g., text or HTML).
     *  The encodings shown in a JComboBox are
     *  UTF-8, ISO-8859-1, Cp1252, MacRoman, and
     *  the default encoding for the locale (if different
     *  from the above), but the user can type in other
     *  encodings.  If an unknown encoding is selected,
     *  an error dialog will be displayed.
     *  OutputHandlers other than the ones known
     *  to the application can't be specified (what would
     *  the application do with them?)
     */
    protected PrintWriter doSaveDialog ()
    {
        JFileChooser saver = new JFileChooser ();
        // On Mac OS, make packages and .apps opaque.
        JhoveWindow.makeChooserOpaque (saver);
        
        File lastDir = _base.getSaveDirectory ();
        if (lastDir != null) {
            saver.setCurrentDirectory(lastDir);
        }
        
        // Create a custom panel so we can set options.
        JPanel accessory = new JPanel ();
        accessory.setPreferredSize(new Dimension (180, 120));
        
        // Build list of handlers into a popup menu
        Vector<String> handlerItems = new Vector<> (10);
        for (OutputHandler han : _base.getHandlerList()) {
            handlerItems.add (han.getName ());
        }
        _handlerBox = new JComboBox<>(handlerItems);
        _handlerBox.setSize (120, 20);
        accessory.add (new JLabel ("Choose output handler"));
        if (_lastHandler != null) {
            _handlerBox.setSelectedItem (_lastHandler);
        }
        accessory.add (_handlerBox);
        
        // Build a list of encodings into a popup menu.
        // The default encoding must be the first.
        Vector<String> encItems = new Vector<>(encodings.length + 1);
        String defaultEncoding = _base.getEncoding();
        List<String> otherEncodings = new ArrayList<>(encodings.length);
        for (String encoding : encodings) {
            if (encoding.equalsIgnoreCase(defaultEncoding)) {
                encItems.add(encoding);
            } else {
                otherEncodings.add(encoding);
            }
        }
        encItems.addAll(otherEncodings);
        _encodingBox = new JComboBox<> (encItems);
        if (_lastEncoding != null) {
            _encodingBox.setSelectedItem (_lastEncoding);
        }
        _encodingBox.setSize (120, 20);
        _encodingBox.setEditable (true);   // Let user type in any encoding
        accessory.add (new JLabel ("Select encoding"));
        accessory.add (_encodingBox);

        saver.setAccessory(accessory);
        saver.setDialogTitle("Save information to file");
        int retval = saver.showSaveDialog (this);
        if (retval == JFileChooser.APPROVE_OPTION) {
            FileOutputStream  os = null;
            File file = null;
            try {
                _base.setSaveDirectory (saver.getCurrentDirectory ());
                file = saver.getSelectedFile();
                if (file.exists ()) {
                    int opt = JOptionPane.showConfirmDialog
                        (this, "That file already exists. Replace?", 
                        "Replace",
                        JOptionPane.OK_CANCEL_OPTION);
                    if (opt != JOptionPane.OK_OPTION) {
                        return null;
                    }
                    // User requested replacement.  Delete the old file.
                    if (!file.delete ()) {
                        JOptionPane.showMessageDialog(this,
                               "Could not delete file",
                               "File not deleted",
                               JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                }
                file.createNewFile();
                String encoding = (String) _encodingBox.getSelectedItem();
                os = new FileOutputStream (file);
                OutputStreamWriter writer = new OutputStreamWriter (os, encoding);
                return new PrintWriter (writer);
            }
            catch (UnsupportedEncodingException uee) {
                JOptionPane.showMessageDialog(this,
                    "Unknown encoding ",
                    FILE_NOT_SAVED,
                    JOptionPane.ERROR_MESSAGE);
                // Get rid of the file
                try {
                    if (os != null) {
                        os.close ();
                    }
                    file.delete ();
                }
                catch (Exception e) {
                }
                return null;
            }
            catch (IOException ioe) {
                JOptionPane.showMessageDialog(this,
                    ioe.getMessage(),
                    FILE_NOT_SAVED,
                    JOptionPane.ERROR_MESSAGE);
                // Get rid of the file
                try {
                    if (os != null) {
                        os.close ();
                    }
                    file.delete ();
                }
                catch (Exception e) {
                }
                return null;
            }
        }
        return null;   
    }
    
    /** Sets up the OutputHandler from the JComboBox
     *  and returns it.  Subclasses should call
     *  <code>selectHandler</code> to obtain an
     *  OutputHandler with which to produce data.
     */
    protected OutputHandler selectHandler ()
    {
        _lastHandler = (String) _handlerBox.getSelectedItem ();
        OutputHandler handler = 
            _base.getHandlerMap().get (_lastHandler.toLowerCase ());
        _lastEncoding = (String) _encodingBox.getSelectedItem ();
        handler.setEncoding (_lastEncoding);
        handler.setApp (_app);
        handler.setBase (_base);
        return handler;
    } 
    
    /**
     *  Handler for the "Close" menu item.
     *  Simply hides the window without deleting it.
     */
    protected void closeFromMenu ()
    {
        setVisible (false);
    }
}
