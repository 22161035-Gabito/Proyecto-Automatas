import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class BasalCodeIDE extends JFrame {

    // --- Paleta principal ---
    private static final Color BG_DARK    = new Color(15, 17, 26);
    private static final Color BG_EDITOR  = new Color(22, 25, 40);
    private static final Color BG_GUTTER  = new Color(12, 14, 22);
    private static final Color BG_CONSOLA = new Color(10, 12, 18);
    private static final Color ACCENT     = new Color(82, 190, 255);
    private static final Color FG_MAIN    = new Color(220, 225, 240);
    private static final Color FG_DIM     = new Color(100, 110, 140);
    private static final Color FG_LINENO  = new Color(70, 80, 110);
    private static final Color OK_COLOR   = new Color(80, 220, 140);
    private static final Color ERR_COLOR  = new Color(255, 90, 90);
    private static final Color BTN_BG     = new Color(40, 45, 70);

    // --- Paleta exclusiva ventana de tokens ---
    private static final Color TK_BG_DEEP    = new Color( 6, 11, 22);
    private static final Color TK_BG_ROW     = new Color(12, 20, 38);
    private static final Color TK_BG_ROW_ALT = new Color(10, 17, 33);
    private static final Color TK_ACCENT     = new Color(28, 120, 200);
    private static final Color TK_ACCENT2    = new Color(28, 168, 221);
    private static final Color TK_COL_TOKEN  = new Color(56, 178, 245);
    private static final Color TK_COL_LEX    = new Color(224, 168, 58);
    private static final Color TK_COL_PAT    = new Color(93, 201, 122);
    private static final Color TK_COL_RES    = new Color(155, 108, 232);
    private static final Color TK_BORDER     = new Color(14, 42, 69);
    private static final Color TK_HEADER_BG  = new Color( 8, 14, 26);

    private static final Font MONO    = new Font("Consolas", Font.PLAIN, 14);
    private static final Font MONO_SM = new Font("Consolas", Font.PLAIN, 12);
    private static final Font MONO_XS = new Font("Consolas", Font.PLAIN, 11);
    private static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    // ======================================================
    //  TABLA DE TOKENS
    // ======================================================
    private static final String[] COL_NAMES = {
        "Token", "Lexema", "Patron", "Pal. Res."
    };

    private static final Object[][] TOKEN_DATA = {
        { "TIPO_GAB",       "Gab",           "Gab",           "Si" },
        { "TIPO_LIT",       "Lit",           "Lit",           "Si" },
        { "TIPO_MAR",       "Mar",           "Mar",           "Si" },
        { "MOSTRAR",        "mostrar",       "mostrar",       "Si" },
        { "SUMA",           "+",             "\\+",           "No" },
        { "RESTA",          "-",             "\\-",           "No" },
        { "MULT",           "*",             "\\*",           "No" }, 
        { "DIV",            "/",             "/",             "No" },
        { "CONCAT",         "&",             "&",             "No" },
        { "ASIGNACION",     "¬",             "¬",             "No" },
        { "PUNTO_COMA",     ";",             ";",             "No" },
        { "NUMERO_ENTERO",  "[0-9]+",        "-?[0-9]+",      "No" },
        { "NUMERO_DECIMAL", "[0-9]+.[0-9]+", "-?[0-9]+\\.[0-9]+", "No" },
        { "CADENA",         "\"...\"",       "\"[^\"]*\"",    "No" },
        { "IDENTIFICADOR",  "nombre",        "[a-zA-Z][a-zA-Z0-9_]*", "No" },
        { "COMENTARIO",     "#...",          "#[^\\n]*",      "No" },
        { "FIN",            "",              "EOF",           "No" },
    };

    // --- Componentes principales ---
    private JTextArea         editorArea;
    private JTextArea         lineNumberArea;
    private JTextArea         consolaArea;
    private JLabel            statusLabel;
    private JButton           btnEjecutar;
    private JButton           btnLimpiar;

    // --- Dialogo de tokens ---
    private JDialog           tokenDialog;
    private JTable            tokenTable;
    private DefaultTableModel tokenModel;
    private JLabel            tokenCountLabel;

    public BasalCodeIDE() {
        setTitle("BasalCode IDE  v1.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));
        construirUI();
        construirDialogoTokens();
        setVisible(true);
    }

    private void construirUI() {
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearStatus(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_GUTTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel titulo = new JLabel("[*] BasalCode IDE");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(ACCENT);

        JLabel tipos = new JLabel("  Gab | Lit | Mar");
        tipos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipos.setForeground(FG_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(titulo);
        izq.add(tipos);

        btnEjecutar        = crearBoton("[>] Ejecutar",     ACCENT,    BG_DARK);
        btnLimpiar         = crearBoton("[C] Limpiar",      FG_DIM,    BTN_BG);
        JButton btnTokens  = crearBoton("[T] Tabla Tokens", TK_ACCENT2, new Color(8, 22, 42));

        btnEjecutar.addActionListener(e -> ejecutar());
        btnLimpiar .addActionListener(e -> limpiar());
        btnTokens  .addActionListener(e -> mostrarDialogoTokens());

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        der.setOpaque(false);
        der.add(btnLimpiar);
        der.add(btnTokens);
        der.add(btnEjecutar);

        panel.add(izq, BorderLayout.WEST);
        panel.add(der, BorderLayout.EAST);
        return panel;
    }

    private JButton crearBoton(String texto, Color fg, Color bg) {
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isArmed() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UI_BOLD);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 34));
        return b;
    }

    private JSplitPane crearCentro() {
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                crearPanelEditor(), crearPanelConsola());
        split.setDividerLocation(420);
        split.setDividerSize(5);
        split.setBackground(BG_DARK);
        split.setBorder(null);
        return split;
    }

    private JPanel crearPanelEditor() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_EDITOR);
        panel.add(etiquetaSeccion("--- Editor --- BasalCode"), BorderLayout.NORTH);

        editorArea = new JTextArea();
        editorArea.setFont(MONO);
        editorArea.setBackground(BG_EDITOR);
        editorArea.setForeground(FG_MAIN);
        editorArea.setCaretColor(ACCENT);
        editorArea.setSelectionColor(new Color(82, 190, 255, 60));
        editorArea.setTabSize(4);
        editorArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        lineNumberArea = new JTextArea("1");
        lineNumberArea.setFont(MONO);
        lineNumberArea.setBackground(BG_GUTTER);
        lineNumberArea.setForeground(FG_LINENO);
        lineNumberArea.setEditable(false);
        lineNumberArea.setFocusable(false);
        lineNumberArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        lineNumberArea.setPreferredSize(new Dimension(46, 0));

        editorArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { actualizarLineas(); }
            public void removeUpdate(DocumentEvent e)  { actualizarLineas(); }
            public void changedUpdate(DocumentEvent e) { actualizarLineas(); }
        });

        JScrollPane scroll = new JScrollPane(editorArea);
        scroll.setBorder(null);
        scroll.setRowHeaderView(lineNumberArea);
        editorArea.setText(codigoEjemplo());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void actualizarLineas() {
        int lineas = editorArea.getText().split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineas; i++) {
            if (i > 1) sb.append("\n");
            sb.append(i);
        }
        lineNumberArea.setText(sb.toString());
    }

    private JPanel crearPanelConsola() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONSOLA);
        panel.add(etiquetaSeccion("--- Consola --- Salida / Errores"), BorderLayout.NORTH);

        consolaArea = new JTextArea();
        consolaArea.setEditable(false);
        consolaArea.setFont(MONO_SM);
        consolaArea.setBackground(BG_CONSOLA);
        consolaArea.setForeground(OK_COLOR);
        consolaArea.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JScrollPane scroll = new JScrollPane(consolaArea);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearStatus() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(8, 10, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        statusLabel = new JLabel("Listo  -  BasalCode v1.0  -  Tipos: Gab | Lit | Mar");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(FG_DIM);

        JLabel hint = new JLabel("Ctrl+Enter = Ejecutar");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(FG_DIM);

        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(hint,        BorderLayout.EAST);
        return panel;
    }

    private JLabel etiquetaSeccion(String texto) {
        JLabel l = new JLabel("  " + texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(FG_DIM);
        l.setOpaque(true);
        l.setBackground(BG_GUTTER);
        l.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return l;
    }

    private void construirDialogoTokens() {
        tokenDialog = new JDialog(this, "Tabla de Tokens - BasalCode", true);
        tokenDialog.setSize(850, 550);
        tokenDialog.setMinimumSize(new Dimension(650, 420));
        tokenDialog.setLocationRelativeTo(this);
        tokenDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        tokenDialog.getContentPane().setBackground(TK_BG_DEEP);
        tokenDialog.setLayout(new BorderLayout(0, 0));

        tokenDialog.add(crearDialogoHeader(), BorderLayout.NORTH);
        tokenDialog.add(crearDialogoTabla(),  BorderLayout.CENTER);
        tokenDialog.add(crearDialogoFooter(), BorderLayout.SOUTH);

        tokenDialog.getRootPane().registerKeyboardAction(
            e -> tokenDialog.setVisible(false),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel crearDialogoHeader() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, getHeight() - 2, new Color(0, 0, 0, 0),
                    getWidth() / 2, getHeight() - 2, TK_ACCENT2, true
                );
                g2.setPaint(gp);
                g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                g2.dispose();
            }
        };
        panel.setBackground(TK_HEADER_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel icono = new JLabel("[*]");
        icono.setFont(new Font("Segoe UI", Font.BOLD, 22));
        icono.setForeground(TK_ACCENT2);

        JLabel titulo = new JLabel("  TABLA DE TOKENS");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(new Color(232, 244, 255));

        JLabel badge = crearBadge("BASALCODE", TK_ACCENT, new Color(10, 30, 55));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        izq.setOpaque(false);
        izq.add(icono);
        izq.add(titulo);
        izq.add(Box.createHorizontalStrut(12));
        izq.add(badge);

        JButton btnCerrar = new JButton("X");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setForeground(FG_DIM);
        btnCerrar.setBackground(new Color(20, 28, 46));
        btnCerrar.setBorder(BorderFactory.createLineBorder(new Color(22, 40, 65), 1));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setOpaque(true);
        btnCerrar.setPreferredSize(new Dimension(32, 28));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> tokenDialog.setVisible(false));
        btnCerrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnCerrar.setBackground(new Color(80, 20, 20));
                btnCerrar.setForeground(ERR_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                btnCerrar.setBackground(new Color(20, 28, 46));
                btnCerrar.setForeground(FG_DIM);
            }
        });

        panel.add(izq,       BorderLayout.WEST);
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearBarraAcciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 17, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JButton btnLlenar   = crearBotonDialogo("[+] Llenar Tabla",  TK_ACCENT2, new Color(10, 40, 75));
        JButton btnLimpiarT = crearBotonDialogo("[-] Limpiar Tabla", TK_COL_RES, new Color(25, 15, 45));

        btnLlenar  .addActionListener(e -> llenarTablaTokens());
        btnLimpiarT.addActionListener(e -> limpiarTablaTokens());

        tokenCountLabel = new JLabel("- tokens registrados");
        tokenCountLabel.setFont(MONO_XS);
        tokenCountLabel.setForeground(FG_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(btnLlenar);
        izq.add(btnLimpiarT);

        panel.add(izq,             BorderLayout.WEST);
        panel.add(tokenCountLabel, BorderLayout.EAST);
        return panel;
    }

    private JButton crearBotonDialogo(String texto, Color fg, Color bg) {
        JButton b = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.brighter() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(fg.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UI_BOLD);
        b.setForeground(fg);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(150, 30));
        return b;
    }

    private JPanel crearDialogoTabla() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(TK_BG_DEEP);
        wrapper.add(crearBarraAcciones(), BorderLayout.NORTH);

        tokenModel = new DefaultTableModel(COL_NAMES, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tokenTable = new JTable(tokenModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row);
                c.setBackground(sel ? new Color(15, 45, 85) : (row % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
                c.setForeground(sel ? Color.WHITE : colorPorColumna(col));
                if (c instanceof JComponent) {
                    ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                }
                return c;
            }
        };

        estilizarTabla();

        JScrollPane scroll = new JScrollPane(tokenTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(TK_BG_DEEP);
        scroll.getViewport().setBackground(TK_BG_DEEP);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(20, 50, 90);
                trackColor = TK_BG_DEEP;
            }
        });

        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void estilizarTabla() {
        tokenTable.setFont(MONO_XS);
        tokenTable.setRowHeight(30);
        tokenTable.setShowGrid(false);
        tokenTable.setIntercellSpacing(new Dimension(0, 3));
        tokenTable.setBackground(TK_BG_DEEP);
        tokenTable.setForeground(FG_MAIN);
        tokenTable.setSelectionBackground(new Color(15, 45, 85));
        tokenTable.setFocusable(false);

        JTableHeader header = tokenTable.getTableHeader();
        header.setBackground(TK_HEADER_BG);
        header.setForeground(FG_DIM);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TK_BORDER));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, col);
                lbl.setBackground(TK_HEADER_BG);
                lbl.setForeground(colorEncabezado(col));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setOpaque(true);
                return lbl;
            }
        });

        int[] anchos = { 155, 110, 200, 95 };
        for (int i = 0; i < anchos.length; i++)
            tokenTable.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        tokenTable.getColumnModel().getColumn(2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    lbl.setForeground(TK_COL_PAT);
                    lbl.setBackground(s ? new Color(15, 45, 85) : (r % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
                    lbl.setFont(MONO_XS);
                    lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                    lbl.setOpaque(true);
                    return lbl;
                }
            }
        );

        tokenTable.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    boolean es = "Si".equals(v);
                    lbl.setText(es ? "  Si  " : "  No  ");
                    lbl.setForeground(es ? TK_COL_RES : FG_DIM);
                    lbl.setBackground(s ? new Color(15, 45, 85) : (r % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    lbl.setFont(MONO_XS);
                    lbl.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
                    lbl.setOpaque(true);
                    return lbl;
                }
            }
        );
    }

    private Color colorPorColumna(int col) {
        switch (col) {
            case 0: return TK_COL_TOKEN;
            case 1: return TK_COL_LEX;
            case 2: return TK_COL_PAT;
            case 3: return TK_COL_RES;
            default: return FG_MAIN;
        }
    }

    private Color colorEncabezado(int col) {
        switch (col) {
            case 0: return TK_COL_TOKEN;
            case 1: return TK_COL_LEX;
            case 2: return TK_COL_PAT;
            case 3: return TK_COL_RES;
            default: return FG_DIM;
        }
    }

    private JPanel crearDialogoFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(7, 12, 22));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TK_BORDER),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));
        JLabel info = new JLabel("BasalCode  -  Tipos: Gab (entero)  |  Lit (decimal)  |  Mar (texto)");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(new Color(50, 80, 120));
        JLabel esc = new JLabel("ESC para cerrar");
        esc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        esc.setForeground(new Color(50, 80, 120));
        panel.add(info, BorderLayout.WEST);
        panel.add(esc,  BorderLayout.EAST);
        return panel;
    }

    private JLabel crearBadge(String texto, Color fg, Color bg) {
        JLabel l = new JLabel(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(fg.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("Consolas", Font.BOLD, 10));
        l.setForeground(fg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    private void mostrarDialogoTokens() {
        tokenDialog.setLocationRelativeTo(this);
        tokenDialog.setVisible(true);
    }

    private void llenarTablaTokens() {
        tokenModel.setRowCount(0);
        for (Object[] fila : TOKEN_DATA) {
            tokenModel.addRow(fila);
        }
        tokenCountLabel.setText(tokenModel.getRowCount() + " tokens registrados");
        tokenCountLabel.setForeground(TK_ACCENT2);
    }

    private void limpiarTablaTokens() {
        tokenModel.setRowCount(0);
        tokenCountLabel.setText("- tokens registrados");
        tokenCountLabel.setForeground(FG_DIM);
    }

    private void ejecutar() {
        String codigo = editorArea.getText().trim();
        if (codigo.isEmpty()) {
            consolaArea.setForeground(FG_DIM);
            consolaArea.setText("- Sin codigo para ejecutar -");
            return;
        }
        
        Interprete interprete = new Interprete();
        String resultado = interprete.ejecutar(codigo);
        
        boolean hayError = resultado.contains("ERROR");
        consolaArea.setForeground(hayError ? ERR_COLOR : OK_COLOR);
        consolaArea.setText(resultado.trim());
        statusLabel.setText(hayError ? "[!] Ejecucion con errores" : "[OK] Ejecucion exitosa");
        statusLabel.setForeground(hayError ? ERR_COLOR : OK_COLOR);
    }

    private void limpiar() {
        editorArea.setText("");
        consolaArea.setText("");
        lineNumberArea.setText("1");
        statusLabel.setText("Listo  -  BasalCode v1.0");
        statusLabel.setForeground(FG_DIM);
    }

    private String codigoEjemplo() {
        return "# --- Ejemplo BasalCode ---\n" +
               "# Tipos: Gab (entero), Lit (decimal), Mar (texto)\n\n" +
               "Gab a ¬ 500;\n" +
               "Gab b ¬ 300;\n" +
               "Gab suma ¬ a + b;\n" +
               "mostrar suma;\n\n" +
               "Lit precio ¬ 19.99;\n" +
               "Lit iva ¬ 1.16;\n" +
               "Lit total ¬ precio * iva;\n" +
               "mostrar total;\n\n" +
               "Mar saludo ¬ \"Hola\";\n" +
               "Mar nombre ¬ \" BasalCode\";\n" +
               "Mar mensaje ¬ saludo & nombre;\n" +
               "mostrar mensaje;\n";
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BasalCodeIDE();
            }
        });
    }
}