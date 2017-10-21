import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Notatnik extends JFrame implements ActionListener, WindowListener {

    private JScrollPane panel;
    private JTextArea textArea = new JTextArea("", 5, 20);
    private JMenuBar menu = new JMenuBar();
    private JPanel extraOptionsPanel = new JPanel();
    private JPanel tabs = new JPanel();
    private JLabel cursorPositionLabel = new JLabel();
    private int cursorPosition;
    private int numberOfLine = 1;
    private int startLine = 1;
    private JPanel lineAndPositionInformationPanel = new JPanel();
    private static Object[] yesNoCancel = {"Yes", "No", "Cancel"};
    private LinkedList<String> nonModifiedTextTab = new LinkedList<String>();
    private LinkedList<JTextArea> listOfDocuments = new LinkedList<JTextArea>();
    private int currentWindow = 0;
    private LinkedList<JButton> listOfButtons = new LinkedList<JButton>();
    private LinkedList<Boolean> isButton = new LinkedList<Boolean>();
    private JButton jButton1;
    private int numberOfDocuments = 1;
    private ImageIcon italicIcon;
    private ImageIcon boldIcon;
    private JButton italicButton;
    private JButton boldButton;

    public Notatnik() {
        super("Notatnik - Kacper Szalwa");
        pack();
        listOfDocuments.add(textArea);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(dimension.width - 100, dimension.height - 100);
        setResizable(true);
        panel = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(tabs, BorderLayout.NORTH);
        add(lineAndPositionInformationPanel, BorderLayout.SOUTH);
        lineAndPositionInformationPanel.add(cursorPositionLabel);
        nonModifiedTextTab.add("");

        // Create menu -----------------------------------
        JMenu mFile = new JMenu("File");

        JMenuItem mFileNew = new JMenuItem("New");
        mFileNew.addActionListener(this);
        mFileNew.setActionCommand("fileNew");
        mFile.add(mFileNew);

        JMenuItem mFileOpen = new JMenuItem("Open");
        mFileOpen.addActionListener(this);
        mFileOpen.setActionCommand("fileOpen");
        mFile.add(mFileOpen);

        JMenuItem mFileSave = new JMenuItem("Save");
        mFileSave.addActionListener(this);
        mFileSave.setActionCommand("fileSave");
        mFile.add(mFileSave);

        mFile.addSeparator();

        JMenuItem mFileExit = new JMenuItem("Exit");
        mFileExit.addActionListener(this);
        mFileExit.setActionCommand("fileExit");
        mFile.add(mFileExit);

        menu.add(mFile);

        JMenu mEdit = new JMenu("Edit");
        JMenuItem mEditClear = new JMenuItem("Clear");
        mEditClear.addActionListener(this);
        mEditClear.setActionCommand("editClear");
        mEdit.add(mEditClear);

        menu.add(mEdit);

        setJMenuBar(menu);
        // End of creating menu ---------------------------


        JButton jButton = new JButton("Untitled 0");
        tabs.add(jButton, BorderLayout.WEST);
        jButton.addActionListener(this);
        listOfButtons.add(jButton);
        isButton.add(0, true);
        jButton.setBackground(Color.red);

        jButton1 = new JButton("Close current tab");
        //tabs.add(jButton1, BorderLayout.WEST);
        jButton1.addActionListener(this);

        addWindowListener(this);
        cursorPositionLabel.setText("Line: " + 1 + ",  position: " + 1);
        // zawijanie wierszy
        textArea.setLineWrap(true);
        // zawijanie wierszy według wyrazów
        textArea.setWrapStyleWord(true);

        textArea = listOfDocuments.get(currentWindow);

        textArea.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                cursorPosition = e.getDot();
                new Thread(new CursorPlacement(textArea, numberOfLine, cursorPosition, startLine, cursorPositionLabel)).start();
                //SwingUtilities.invokeLater(new CursorPlacement(textArea, numberOfLine, cursorPosition, startLine, cursorPositionLabel));
            }
        });

        italicIcon = new ImageIcon("italic.jpg");
        boldIcon = new ImageIcon("bold.jpg");
        italicButton = new JButton(italicIcon);
        boldButton = new JButton(boldIcon);
        italicButton.setMargin(new Insets(0, 0, 0, 0));
        boldButton.setMargin(new Insets(0, 0, 0, 0));
        //italicButton.setBorder(null);
        //boldButton.setBorder(null);
        menu.add(italicButton);
        menu.add(boldButton);
        menu.add(jButton1);
        italicButton.addActionListener(this);
        boldButton.addActionListener(this);
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        if(!nonModifiedTextTab.get(currentWindow).equals(textArea.getText())) {
            int info = JOptionPane.showOptionDialog(textArea, "Do you want to save your document before exit?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, yesNoCancel, yesNoCancel[0]);
            if (info == JOptionPane.CLOSED_OPTION || info == JOptionPane.CANCEL_OPTION)
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            else if (info == JOptionPane.YES_OPTION) {
                if (save())
                    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                else
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            } else {
                setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            }
        }
        else
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {}

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {}

    @Override
    public void windowIconified(WindowEvent windowEvent) {}

    @Override
    public void windowOpened(WindowEvent windowEvent) {}

    @Override
    public void windowActivated(WindowEvent windowEvent) {}

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {}

    private boolean save() {
        boolean isSaved = false;
        FileChooserSave fcp = new FileChooserSave();
        if(fcp.getFile() != null) {
            BufferedWriter bw = null;
            try {
                listOfButtons.get(currentWindow).setText(fcp.getFile().getName());
                bw = new BufferedWriter(new FileWriter(fcp.getFile().getAbsolutePath()));
                bw.write(textArea.getText());
            } catch(IOException ee) {
                ee.printStackTrace();
            } finally {
                try {
                    if(bw != null)
                        bw.close();
                } catch(IOException ee) {
                    ee.printStackTrace();
                }
            }
            isSaved = true;
        }
        fcp.setVisible(true);
        return isSaved;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String sCommand = e.getActionCommand();

        if(sCommand == "fileNew") {
            nonModifiedTextTab.add("");
            JTextArea textAreaTmp = new JTextArea();
            textAreaTmp.setText("");
            listOfDocuments.add(textAreaTmp);
            currentWindow = listOfDocuments.size() - 1;
            ++numberOfDocuments;
            isButton.add(currentWindow, false);
            new Thread(new AddButtons(listOfDocuments, listOfButtons, tabs, this, isButton, currentWindow, "")).start();
            //SwingUtilities.invokeLater(new AddButtons(listOfDocuments, listOfButtons, tabs, this, isButton, currentWindow));
        }

        if(sCommand == "fileOpen") {
            FileChooserOpen fco = new FileChooserOpen();
            if(fco.getFile() != null) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(fco.getFile().getAbsolutePath()));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    JTextArea textAreaTmp = new JTextArea();
                    textAreaTmp.setText(stringBuilder.toString());
                    if((listOfDocuments.size() == 1 || currentWindow == 0) && listOfDocuments.get(currentWindow).getText().equals("")) {
                        nonModifiedTextTab.set(currentWindow, stringBuilder.toString());
                        listOfDocuments.set(currentWindow, textAreaTmp);
                        listOfButtons.get(currentWindow).setText(fco.getFile().getName());
                    }
                    else {
                        int info = JOptionPane.showOptionDialog(textArea, "Do you want to open the file in a new window?", "Where to open?",
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, yesNoCancel, yesNoCancel[0]);
                        if (info == JOptionPane.CLOSED_OPTION || info == JOptionPane.CANCEL_OPTION)
                            return;
                        else if (info == JOptionPane.YES_OPTION) {
                            nonModifiedTextTab.add(stringBuilder.toString());
                            listOfDocuments.add(textAreaTmp);
                            currentWindow = listOfDocuments.size() - 1;
                            ++numberOfDocuments;
                            isButton.add(currentWindow, false);
                            new Thread(new AddButtons(listOfDocuments, listOfButtons, tabs, this, isButton, currentWindow, fco.getFile().getName())).start();
                            //SwingUtilities.invokeLater(new AddButtons(listOfDocuments, listOfButtons, tabs, this, isButton, currentWindow));
                        } else {
                            nonModifiedTextTab.set(currentWindow, stringBuilder.toString());
                            listOfDocuments.set(currentWindow, textAreaTmp);
                            listOfButtons.get(currentWindow).setText(fco.getFile().getName());
                        }
                    }
                    textArea.setText(listOfDocuments.get(currentWindow).getText());
                } catch (IOException ee) {
                    ee.printStackTrace();
                } finally {
                    try {
                        if (br != null)
                            br.close();
                    } catch (IOException ee) {
                        ee.printStackTrace();
                    }
                }
            }
            fco.setVisible(true);
        }
        else if(sCommand == "fileSave") {
            save();
        }
        else if(sCommand == "fileExit") {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); // or System.exit(0);
        }
        else if(sCommand == "editClear") {
            textArea.setText("");
        }
        for(int i = 0; i < listOfButtons.size(); ++i)
            if(e.getSource() == listOfButtons.get(i)) {
                JTextArea textAreaTmp = new JTextArea();
                textAreaTmp.setText(textArea.getText());
                listOfDocuments.set(currentWindow, textAreaTmp);
                textArea.setText(listOfDocuments.get(i).getText());
                currentWindow = i;
                for(JButton b : listOfButtons)
                    b.setBackground(null);
                listOfButtons.get(currentWindow).setBackground(Color.red);
                break;
            }
        if(e.getSource() == jButton1) {
            if(!nonModifiedTextTab.get(currentWindow).equals(textArea.getText()) /*&& numberOfDocuments == 1*/) {
                int info = JOptionPane.showOptionDialog(textArea, "Do you want to save your document before closing?", "Close", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, yesNoCancel, yesNoCancel[0]);
                if (info == JOptionPane.CLOSED_OPTION || info == JOptionPane.CANCEL_OPTION)
                    return;
                else if (info == JOptionPane.YES_OPTION) {
                    if (!save())
                        return;
                }
            }
            if(numberOfDocuments == 1) {
                textArea.setText("");
                JTextArea textAreaTmp = new JTextArea();
                textAreaTmp.setText("");
                listOfDocuments.set(0, textAreaTmp);
            }
            else {
                --numberOfDocuments;
                listOfButtons.get(currentWindow).setVisible(false);
                for(int i = 0; i < listOfButtons.size(); ++i) {
                    if(listOfButtons.get(i).isVisible()) {
                        currentWindow = i;
                        textArea.setText(listOfDocuments.get(currentWindow).getText());
                        listOfButtons.get(currentWindow).setBackground(Color.red);
                        break;
                    }
                }
            }
        }
        if(e.getSource() == italicButton) {
            int info;
            Font font = textArea.getFont();
            if (italicButton.isSelected()) {
                if (font.getStyle() == Font.ITALIC)
                    info = Font.PLAIN;
                else
                    info = Font.BOLD;
            } else {
                if (font.getStyle() == Font.PLAIN)
                    info = Font.ITALIC;
                else
                    info = Font.BOLD + Font.ITALIC;
            }
            new Thread(new ItalicAndBoldSetter(font.getName(), font.getSize(), info, italicButton, textArea)).start();
            //SwingUtilities.invokeLater(new ItalicAndBoldSetter(font.getName(), font.getSize(), info, italicButton, textArea));
        }

        if(e.getSource() == boldButton) {
            int info;
            Font font = textArea.getFont();
            if (boldButton.isSelected()) {
                if (font.getStyle() == Font.BOLD)
                    info = Font.PLAIN;
                else
                    info = Font.ITALIC;
            } else {
                if (font.getStyle() == Font.PLAIN)
                    info = Font.BOLD;
                else
                    info = Font.BOLD + Font.ITALIC;
            }
            new Thread(new ItalicAndBoldSetter(font.getName(), font.getSize(), info, boldButton, textArea)).start();
            //SwingUtilities.invokeLater(new ItalicAndBoldSetter(font.getName(), font.getSize(), info, boldButton, textArea));
        }
    }

    public static void main(String[] args) throws Exception {
        /*SwingUtilities.invokeLater*/new Thread(new Runnable() {
            @Override
            public void run() {
                //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                Notatnik n = new Notatnik();
                n.setVisible(true);
                /*EventQueue.invokeLater*/new Thread(new Runnable() {
                    @Override
                    public void run() {
                        n.textArea.grabFocus();
                        n.textArea.requestFocus();
                    }
                }).start();
            }
        }).start();
    }
}


class FileChooserSave extends JPanel {
    private JFileChooser fc = new JFileChooser();
    private File file;
    FileChooserSave() {
        fc.setCurrentDirectory(new java.io.File("."));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
    }

    public File getFile() {
        return file;
    }

}

class FileChooserOpen extends JPanel {
    private JFileChooser fc = new JFileChooser();
    private File file;
    FileChooserOpen() {
        fc.setCurrentDirectory(new java.io.File("."));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
        }
    }

    public File getFile() {
        return file;
    }

}

class CursorPlacement implements Runnable {
    private JTextArea textArea;
    private int numberOfLine;
    private int cursorPosition;
    private int startLine;
    private JLabel cursorPositionLabel;
    CursorPlacement(JTextArea textArea, int numberOfLine, int cursorPostion, int startLine, JLabel cursorPositionLabel) {
        this.textArea = textArea;
        this.numberOfLine = numberOfLine;
        this.cursorPosition = cursorPostion;
        this.startLine = startLine;
        this.cursorPositionLabel = cursorPositionLabel;
    }
    @Override
    public void run() {
        try {
            numberOfLine = textArea.getLineOfOffset(cursorPosition);
            startLine = textArea.getLineStartOffset(numberOfLine);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        cursorPositionLabel.setText("Line: " + (numberOfLine + 1) + ",  position: " + (cursorPosition + 1 - startLine));
    }
}

class AddButtons implements Runnable {
    private LinkedList<JTextArea> listOfDocuments;
    private LinkedList<JButton> listOfButtons;
    private JPanel tabs;
    private Notatnik n;
    private LinkedList<Boolean> isButton;
    private int currentWindow;
    private String name;
    AddButtons(LinkedList<JTextArea> listOfDocuments, LinkedList<JButton> listOfButtons, JPanel tabs, Notatnik n, LinkedList<Boolean> isButton, int currentWindow, String name) {
        this.listOfDocuments = listOfDocuments;
        this.listOfButtons = listOfButtons;
        this.tabs = tabs;
        this.n = n;
        this.isButton = isButton;
        this.currentWindow = currentWindow;
        this.name = name;
    }
    @Override
    public void run() {
        for(int i = 0; i < listOfDocuments.size(); ++i) {
            JButton jButton;
            if(!isButton.get(i)) {
                if(name.equals(""))
                    jButton = new JButton("Untitled" + i);
                else
                    jButton = new JButton(name);
                tabs.add(jButton, BorderLayout.WEST);
                jButton.addActionListener(n);
                listOfButtons.add(jButton);
                isButton.set(i, true);
            }
        }
        for(JButton b : listOfButtons)
            b.setBackground(null);
        listOfButtons.get(currentWindow).setBackground(Color.red);
        tabs.repaint();
        tabs.revalidate();
    }
}

class ItalicAndBoldSetter implements Runnable {
    private String name;
    private int info;
    private int size;
    private JButton b;
    private JTextArea textArea;
    ItalicAndBoldSetter(String name, int size, int info, JButton b, JTextArea textArea) {
        this.name = name;
        this.info = info;
        this.size = size;
        this.b = b;
        this.textArea = textArea;
    }
    @Override
    public void run() {
        if (b.isSelected())
            b.setSelected(false);
        else
            b.setSelected(true);

        textArea.setFont(new Font(name, info, size));
    }
}