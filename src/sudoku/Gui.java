package sudoku;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Gui extends Frame {
    private static final long serialVersionUID = 1L;

    protected SudokuPanel sdkPanel;
    protected Panel buttonPanel;
    protected Button loadButton;
    protected Button checkButton;
    protected Button solveButton;
    protected PossiblePanel possiblePanel;
    protected File file;
    protected boolean fileChosen = false;
    protected Cell[][] sdkPuzzle;
    protected Solver sdkSolver;
    protected int lastPoss = -1;

    public Gui(Cell sdk[][], Solver solver) {
        super("Sudoku Solver (ΤΕΧΝΗΤΗ ΝΟΗΜΟΣΥΝΗ www.ece.upatras.gr)");
        this.sdkPuzzle = sdk;
        this.sdkSolver = solver;

        addWindowListener(new CloseAndExit());
        setLayout(new BorderLayout(10, 10));
        add(sdkPanel = new SudokuPanel(sdk), BorderLayout.CENTER);
        add(buttonPanel = new Panel(), BorderLayout.WEST);
        add(possiblePanel = new PossiblePanel(sdkPanel), BorderLayout.EAST);
        buttonPanel.setLayout(new GridLayout(0, 1));
        buttonPanel.add(loadButton = new Button("Load Sudoku from File"));
        buttonPanel.add(checkButton = new Button("Check Sudoku"));
        buttonPanel.add(solveButton = new Button("Solve Sudoku"));
        solveButton.setEnabled(false);
        checkButton.setEnabled(false);

        /* Assign functionalities to the GUI buttons */
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (!chooseFile()) return;

                readFile();
                solveButton.setEnabled(true);
                checkButton.setEnabled(true);
                repaint();
                sdkPanel.requestFocus();
            }
        });

        checkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sdkSolver.checkSDK();
                repaint();
            }
        });

        solveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int cellsFound = sdkSolver.solveSDK();

                repaint();

                if (cellsFound < 0)
                    JOptionPane.showMessageDialog(Gui.this, "Το sudoku έχει λάθη. ");
                else if (cellsFound == 0)
                    JOptionPane.showMessageDialog(Gui.this, "Δεν βρέθηκε κανένα κελί. ");
                else
                    JOptionPane.showMessageDialog(Gui.this, "Βρέθηκαν " + cellsFound + " κελιά. ");

            }
        });

        pack();
        setResizable(false);
        setVisible(true);
        sdkPanel.requestFocus();
    }

    public void paint(Graphics g) {
        super.paint(g);

        sdkPanel.repaint();

        if (lastPoss != sdkPuzzle[sdkPanel.selectedLine][sdkPanel.selectedColumn].poss) {
            lastPoss = sdkPuzzle[sdkPanel.selectedLine][sdkPanel.selectedColumn].poss;
            possiblePanel.repaint();
        }

    }

    protected boolean chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showDialog(this, "open Sudoku") == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            fileChosen = true;
            return true;
        } else
            return false;
    }

    protected int readFile() {
        if (fileChosen) {
            int digitCounter = 0;
            FileReader inputStream = null;

            int column = 1;
            int line = 1;
            int ch;
            char c;

            try {
                inputStream = new FileReader(file);

                for (line = 1; line < 10; line++) {
                    for (column = 1; column < 10; column++) {
                        while ((c = (char) (ch = inputStream.read())) != 'x' && !(c >= '1' && c <= '9') && ch != -1)
                            ;
                        if (ch == -1) {
                            System.out.println("reached end of file");
                            throw new IOException();
                        }
                        if (c == 'x') {
                            sdkPuzzle[line][column] = new Cell();
                        } else {
                            c -= '0';
                            sdkPuzzle[line][column] = new Cell(c);
                            digitCounter++;
                        }
                    }
                }

                sdkSolver.checkSDK();

            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            fileChosen = false;

            return digitCounter;
        }
        return -1;
    }

    class CloseAndExit extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    class SdkKeyListener extends KeyAdapter {
        SudokuPanel panel;

        public SdkKeyListener(SudokuPanel panel) {
            super();
            this.panel = panel;
        }

        public void keyPressed(KeyEvent evt) {
            switch (evt.getKeyCode()) {
                case KeyEvent.VK_UP:
                    panel.moveSelectionUp();
                    break;
                case KeyEvent.VK_DOWN:
                    panel.moveSelectionDown();
                    break;
                case KeyEvent.VK_LEFT:
                    panel.moveSelectionLeft();
                    break;
                case KeyEvent.VK_RIGHT:
                    panel.moveSelectionRight();
                    break;
                case KeyEvent.VK_1:
                case KeyEvent.VK_2:
                case KeyEvent.VK_3:
                case KeyEvent.VK_4:
                case KeyEvent.VK_5:
                case KeyEvent.VK_6:
                case KeyEvent.VK_7:
                case KeyEvent.VK_8:
                case KeyEvent.VK_9:
                    int newCell = evt.getKeyChar() - '0';
                    ((Gui) (panel.getParent())).checkButton.setEnabled(true);
                    ((Gui) (panel.getParent())).solveButton.setEnabled(true);
                    panel.SDK[panel.selectedLine][panel.selectedColumn] = new Cell(newCell);
                    sdkSolver.checkSDK();
                    panel.getParent().repaint();
                    break;
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_0:
                    panel.SDK[panel.selectedLine][panel.selectedColumn] = new Cell();
                    sdkSolver.checkSDK();
                    panel.getParent().repaint();
                    break;
            }
        }
    }

    class SudokuPanel extends Panel {
        private static final long serialVersionUID = 1L;

        protected BufferedImage bfImg = null;
        public static final int cellSize = 40;
        public int selectedLine = 1;
        public int selectedColumn = 1;
        Cell[][] SDK;

        public SudokuPanel(Cell SDK[][]) {
            this.SDK = SDK;
            this.setPreferredSize(new Dimension(cellSize * 9 + 3, cellSize * 9 + 3));
            this.setBackground(Color.WHITE);

            addKeyListener(new SdkKeyListener(this));
            addMouseListener(new SdkMouseListener(this));
        }

        public void paint(Graphics g) {
            if (bfImg == null) {
                bfImg = (BufferedImage) createImage(getWidth(), getHeight());
                System.out.println("Created BufferedImage");
            }

            Graphics gScreen = g;

            if (bfImg != null)
                g = bfImg.getGraphics();
            else
                System.out.println("Buffered image = null --> Drawing to the screen :(");

            g.clearRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            drawLines(g);
            drawNumbers(g);
            fillCellBg(selectedLine, selectedColumn, new Color(10, 10, 10, 80), g);

            gScreen.drawImage(bfImg, 0, 0, null);
        }

        private void drawLines(Graphics g) {
            int x, y;
            for (x = 0; x < 10; x++) {
                if (x % 3 != 0)
                    g.fillRect(cellSize * x, 0, 1, cellSize * 9);
                else
                    g.fillRect(cellSize * x, 0, 3, cellSize * 9 + 3);
            }

            for (y = 0; y < 10; y++) {
                if (y % 3 != 0)
                    g.fillRect(0, cellSize * y, cellSize * 9, 1);
                else
                    g.fillRect(0, cellSize * y, cellSize * 9 + 3, 3);
            }
        }

        private void drawNumbers(Graphics g) {
            int line, column;
            g.setFont(new Font("Calibri", Font.BOLD, 18));
            for (line = 1; line < 10; line++) {
                for (column = 1; column < 10; column++) {
                    if (SDK[line][column].conflict) fillCellBg(line, column, Color.PINK, g);
                    drawCellNum(line, column, g);
                }
            }
        }

        private void fillCellBg(int x, int y, Color bg, Graphics g) {
            Color oldColor = g.getColor();
            g.setColor(bg);
            Rectangle rect = getCellBoundingBox(x, y);
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
            g.setColor(oldColor);
        }

        private void drawCellNum(int x, int y, Graphics g) {
            if (SDK[x][y].value != 0) {
                g.drawString("" + SDK[x][y].value, cellSize * (y - 1) + 16, cellSize * (x - 1) + 28);
            }
        }

        public Rectangle getCellBoundingBox(int x, int y) {
            return new Rectangle(cellSize * (y - 1) + 6, cellSize * (x - 1) + 6, cellSize - 9, cellSize - 9);
        }

        public void moveSelectionUp() {
            if (selectedLine > 1) {
                selectedLine--;
                getParent().repaint();
            }
        }

        public void moveSelectionDown() {
            if (selectedLine < 9) {
                selectedLine++;
                getParent().repaint();
            }
        }

        public void moveSelectionLeft() {
            if (selectedColumn > 1) {
                selectedColumn--;
                getParent().repaint();
            }
        }

        public void moveSelectionRight() {
            if (selectedColumn < 9) {
                selectedColumn++;
                getParent().repaint();
            }
        }
    }

    class PossiblePanel extends Panel {
        private static final long serialVersionUID = 1L;

        SudokuPanel sdkPanel;
        int lastPoss = -2;

        public PossiblePanel(SudokuPanel sdkPanel) {
            super();
            this.sdkPanel = sdkPanel;
            setLayout(null);
            Label lb = new Label("ee6609@upnet.gr");
            lb.setFont(new Font("Calibri", Font.BOLD, 13));
            lb.setBounds(0, 320, 180, 50);
            add(lb);
            setPreferredSize(new Dimension(150, 120));
        }

        public void paint(Graphics g) {
            super.paint(g);

            g.setFont(new Font("Calibri", Font.BOLD, 15));
            g.drawString("ΠΙΘΑΝΕΣ ΤΙΜΕΣ", 0, 15);
            g.translate(0, 20);
            int[] xPoints = { 3, 123, 123, 3 };
            int[] yPoints = { 3, 3, 123, 123 };
            g.drawPolygon(xPoints, yPoints, 4);
            g.setFont(new Font("Calibri", Font.BOLD, 28));
            Vector<Integer> v = sdkPanel.SDK[sdkPanel.selectedLine][sdkPanel.selectedColumn].getPossibleValueArray();
            for (int x : v) {
                g.drawString("" + x + "", (Cell.sthlh(1, x)) * 40 - 25, (Cell.grammh(1, x)) * 40 - 8);
            }

        }
    }

    class SdkMouseListener extends MouseAdapter {
        SudokuPanel panel;

        public SdkMouseListener(SudokuPanel panel) {
            super();
            this.panel = panel;
        }

        public void mouseClicked(MouseEvent evt) {
            Point whereClicked;
            whereClicked = evt.getPoint();
            int x, y;
            for (y = 1; y < 10; y++) {
                for (x = 1; x < 10; x++) {
                    if (panel.getCellBoundingBox(x, y).contains(whereClicked)) {
                        panel.selectedLine = x;
                        panel.selectedColumn = y;
                        panel.getParent().repaint();
                        return;
                    }
                }
            }

        }
    }

}
