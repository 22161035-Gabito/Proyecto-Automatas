package codigo;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.*;

public class BasalCodeIDE extends JFrame {

    // ── Paleta principal ──────────────────────────────────
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

    // ── Paleta exclusiva ventana de tokens ────────────────
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

    private static final Font MONO    = new Font("JetBrains Mono", Font.PLAIN, 14);
    private static final Font MONO_SM = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Font MONO_XS = new Font("JetBrains Mono", Font.PLAIN, 11);
    private static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    // ── Componentes principales ───────────────────────────
    private JTextArea         editorArea;
    private JTextArea         lineNumberArea;
    private JPanel            consolaPanel;
    private JLabel            statusLabel;
    private JButton           btnEjecutar;
    private JButton           btnLimpiar;

    // ── Diálogo de tokens ─────────────────────────────────
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

    // ─────────────────────────────────────────────────────
    private void construirUI() {
        add(crearHeader(), BorderLayout.NORTH);
        add(crearCentro(), BorderLayout.CENTER);
        add(crearStatus(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_GUTTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel titulo = new JLabel("⬡ BasalCode IDE");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(ACCENT);

        JLabel tipos = new JLabel("  Gab · Lit · Mar");
        tipos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipos.setForeground(FG_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(titulo);
        izq.add(tipos);

        btnEjecutar        = crearBoton("▶  Ejecutar",     ACCENT,    BG_DARK);
        btnLimpiar         = crearBoton("⊘  Limpiar",      FG_DIM,    BTN_BG);
        JButton btnTokens  = crearBoton("⊞  Tabla Tokens", TK_ACCENT2, new Color(8, 22, 42));

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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
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
        panel.add(etiquetaSeccion("📝  Editor  —  BasalCode"), BorderLayout.NORTH);

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

    // ═════════════════════════════════════════════════════
    //  PANEL CONSOLA — ahora con tabla semántica + resultados
    // ═════════════════════════════════════════════════════
    private JPanel crearPanelConsola() {
        consolaPanel = new JPanel(new BorderLayout());
        consolaPanel.setBackground(BG_CONSOLA);
        consolaPanel.add(etiquetaSeccion("⬡  Consola  —  Salida / Análisis Semántico"), BorderLayout.NORTH);
        // El contenido se llena al ejecutar
        mostrarMensajeInicial();
        return consolaPanel;
    }

    private void mostrarMensajeInicial() {
        // Remover contenido anterior
        if (consolaPanel.getComponentCount() > 1) {
            consolaPanel.remove(1);
        }
        JTextArea msg = new JTextArea("— Ejecuta el código para ver resultados y tabla semántica —");
        msg.setEditable(false);
        msg.setFont(MONO_SM);
        msg.setBackground(BG_CONSOLA);
        msg.setForeground(FG_DIM);
        msg.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        consolaPanel.add(new JScrollPane(msg) {{ setBorder(null); }}, BorderLayout.CENTER);
        consolaPanel.revalidate();
        consolaPanel.repaint();
    }

    /**
     * Construye el panel de consola con dos secciones:
     *  1. Tabla semántica (Token | Categoría) estilo imagen 2
     *  2. Resultados de ejecución
     */
    private void mostrarConsolaConTabla(List<TokenInfo> tokens, String resultadoEjecucion, boolean hayError) {
        // Remover contenido anterior (excepto la etiqueta norte)
        if (consolaPanel.getComponentCount() > 1) {
            consolaPanel.remove(1);
        }

        JSplitPane splitConsola = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitConsola.setDividerLocation(400);
        splitConsola.setDividerSize(4);
        splitConsola.setBackground(BG_CONSOLA);
        splitConsola.setBorder(null);

        // ── Panel izquierdo: tabla semántica ──────────────
        splitConsola.setLeftComponent(crearTablaSemantica(tokens));

        // ── Panel derecho: resultados ─────────────────────
        splitConsola.setRightComponent(crearPanelResultados(resultadoEjecucion, hayError));

        consolaPanel.add(splitConsola, BorderLayout.CENTER);
        consolaPanel.revalidate();
        consolaPanel.repaint();
    }

    private JPanel crearTablaSemantica(List<TokenInfo> tokens) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TK_BG_DEEP);

        // Encabezado
        JLabel lbl = new JLabel("  📋  Análisis Semántico");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TK_ACCENT2);
        lbl.setOpaque(true);
        lbl.setBackground(TK_HEADER_BG);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        panel.add(lbl, BorderLayout.NORTH);

        // Modelo con columnas: Token | Lexema | Categoría
        String[] cols = {"Token", "Lexema", "Categoría"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // Colores para categorías semánticas (estilo imagen 2)
        Color catPalabraRes  = new Color(155, 108, 232); // Palabra reservada → morado
        Color catIdentif     = new Color(56, 178, 245);  // Identificador → azul
        Color catConstante   = new Color(93, 201, 122);  // Constante → verde
        Color catOperador    = new Color(224, 168, 58);  // Operador/Signo → amarillo
        Color catSeparador   = new Color(200, 130, 80);  // Separador → naranja
        Color catCadena      = new Color(255, 160, 180); // Cadena → rosa
        Color catComentario  = new Color(100, 120, 100); // Comentario → gris verde
        Color catError       = ERR_COLOR;

        // Llenar con tokens únicos por lexema (sin repetir misma categoría)
        // Mostrar todos los tokens con su categoría semántica
        for (TokenInfo tk : tokens) {
            String categoria = obtenerCategoria(tk.token);
            model.addRow(new Object[]{tk.token, tk.lexema, categoria});
        }

        JTable tabla = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row);
                c.setBackground(sel
                    ? new Color(15, 45, 85)
                    : (row % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));

                if (!sel) {
                    if (col == 0) c.setForeground(TK_COL_TOKEN);
                    else if (col == 1) c.setForeground(TK_COL_LEX);
                    else {
                        // Color por categoría
                        String cat = (String) model.getValueAt(row, 2);
                        c.setForeground(colorCategoria(cat,
                            catPalabraRes, catIdentif, catConstante,
                            catOperador, catSeparador, catCadena,
                            catComentario, catError));
                    }
                } else {
                    c.setForeground(Color.WHITE);
                }

                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return c;
            }
        };

        tabla.setFont(MONO_XS);
        tabla.setRowHeight(26);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 2));
        tabla.setBackground(TK_BG_DEEP);
        tabla.setForeground(FG_MAIN);
        tabla.setSelectionBackground(new Color(15, 45, 85));
        tabla.setFocusable(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(TK_HEADER_BG);
        header.setForeground(FG_DIM);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 30));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, TK_BORDER));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int row, int col) {
                JLabel l2 = (JLabel) super.getTableCellRendererComponent(t, v, s, f, row, col);
                l2.setBackground(TK_HEADER_BG);
                l2.setForeground(switch(col){
                    case 0 -> TK_COL_TOKEN;
                    case 1 -> TK_COL_LEX;
                    default -> TK_COL_PAT;
                });
                l2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                l2.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                l2.setHorizontalAlignment(SwingConstants.LEFT);
                l2.setOpaque(true);
                return l2;
            }
        });

        // Anchos columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(null);
        scroll.setBackground(TK_BG_DEEP);
        scroll.getViewport().setBackground(TK_BG_DEEP);
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(20, 50, 90);
                trackColor = TK_BG_DEEP;
            }
        });

        panel.add(scroll, BorderLayout.CENTER);

        // Footer con conteo
        JLabel footer = new JLabel("  " + model.getRowCount() + " tokens analizados");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(new Color(50, 80, 120));
        footer.setOpaque(true);
        footer.setBackground(new Color(7, 12, 22));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TK_BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private Color colorCategoria(String cat,
            Color palRes, Color identif, Color constante,
            Color operador, Color separador, Color cadena,
            Color comentario, Color error) {
        return switch (cat) {
            case "Palabra reservada" -> palRes;
            case "Identificador"     -> identif;
            case "Constante entera"  -> constante;
            case "Constante decimal" -> constante;
            case "Cadena"            -> cadena;
            case "Operador"          -> operador;
            case "Signo/Operador"    -> operador;
            case "Separador"         -> separador;
            case "Comentario"        -> comentario;
            default                  -> error;
        };
    }

    private String obtenerCategoria(String tokenType) {
        return switch (tokenType) {
            case "TIPO_GAB", "TIPO_LIT", "TIPO_MAR", "MOSTRAR" -> "Palabra reservada";
            case "IDENTIFICADOR"   -> "Identificador";
            case "NUMERO_ENTERO"   -> "Constante entera";
            case "NUMERO_DECIMAL"  -> "Constante decimal";
            case "CADENA"          -> "Cadena";
            case "SUMA", "RESTA", "MULT", "DIV", "CONCAT", "ASIGNACION" -> "Signo/Operador";
            case "PUNTO_COMA"      -> "Separador";
            case "COMENTARIO"      -> "Comentario";
            default                -> "Desconocido";
        };
    }

    private JPanel crearPanelResultados(String resultado, boolean hayError) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONSOLA);

        JLabel lbl = new JLabel("  ▶  Resultados de Ejecución");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(hayError ? ERR_COLOR : OK_COLOR);
        lbl.setOpaque(true);
        lbl.setBackground(BG_GUTTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        panel.add(lbl, BorderLayout.NORTH);

        JTextArea area = new JTextArea(resultado);
        area.setEditable(false);
        area.setFont(MONO_SM);
        area.setBackground(BG_CONSOLA);
        area.setForeground(hayError ? ERR_COLOR : OK_COLOR);
        area.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 14));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearStatus() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(8, 10, 16));
        panel.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));

        statusLabel = new JLabel("Listo  •  BasalCode v1.0  •  Tipos: Gab | Lit | Mar");
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

    // ═════════════════════════════════════════════════════
    //  DIÁLOGO TABLA DE TOKENS  (formato imagen 1)
    //  Columnas: Token | Lexema | Patrón | ¿Pal. Res.?
    // ═════════════════════════════════════════════════════
    private void construirDialogoTokens() {
        tokenDialog = new JDialog(this, "Tabla de Tokens — BasalCode", true);
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
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

        JLabel icono = new JLabel("⬡");
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

        JButton btnCerrar = new JButton("✕");
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

        JButton btnLlenar   = crearBotonDialogo("✎  Llenar con Código",  TK_ACCENT2, new Color(10, 40, 75));
        JButton btnLimpiarT = crearBotonDialogo("⊘  Limpiar Tabla", TK_COL_RES, new Color(25, 15, 45));

        btnLlenar  .addActionListener(e -> llenarTablaConCodigoActual());
        btnLimpiarT.addActionListener(e -> limpiarTablaTokens());

        tokenCountLabel = new JLabel("— tokens registrados");
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
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
        b.setPreferredSize(new Dimension(180, 30));
        return b;
    }

    private JPanel crearDialogoTabla() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(TK_BG_DEEP);
        wrapper.add(crearBarraAcciones(), BorderLayout.NORTH);

        // ── Columnas exactas como imagen 1: Token | Lexema | Patrón | ¿Pal. Res.? ──
        tokenModel = new DefaultTableModel(
            new String[]{"Token", "Lexema", "Patrón", "¿Pal. Res.?"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tokenTable = new JTable(tokenModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                boolean sel = isRowSelected(row);
                c.setBackground(sel
                    ? new Color(15, 45, 85)
                    : (row % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
                c.setForeground(sel ? Color.WHITE : colorPorColumna(col));
                if (c instanceof JComponent jc)
                    jc.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
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

        // Anchos de columna
        int[] anchos = { 155, 120, 220, 95 };
        for (int i = 0; i < anchos.length; i++)
            tokenTable.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Renderer columna Patrón
        tokenTable.getColumnModel().getColumn(2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    lbl.setForeground(TK_COL_PAT);
                    lbl.setBackground(s ? new Color(15, 45, 85)
                        : (r % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
                    lbl.setFont(MONO_XS);
                    lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                    lbl.setOpaque(true);
                    return lbl;
                }
            }
        );

        // Renderer columna ¿Pal. Res.? — badge Sí/No como imagen 1
        tokenTable.getColumnModel().getColumn(3).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean s, boolean f, int r, int c) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                    boolean es = "Sí".equals(v);
                    lbl.setText(es ? "  Sí  " : "  No  ");
                    lbl.setForeground(es ? TK_COL_RES : FG_DIM);
                    lbl.setBackground(s ? new Color(15, 45, 85)
                        : (r % 2 == 0 ? TK_BG_ROW : TK_BG_ROW_ALT));
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
        return switch (col) {
            case 0 -> TK_COL_TOKEN;
            case 1 -> TK_COL_LEX;
            case 2 -> TK_COL_PAT;
            case 3 -> TK_COL_RES;
            default -> FG_MAIN;
        };
    }

    private Color colorEncabezado(int col) {
        return switch (col) {
            case 0 -> TK_COL_TOKEN;
            case 1 -> TK_COL_LEX;
            case 2 -> TK_COL_PAT;
            case 3 -> TK_COL_RES;
            default -> FG_DIM;
        };
    }

    private JPanel crearDialogoFooter() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(7, 12, 22));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, TK_BORDER),
            BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));
        JLabel info = new JLabel(
            "BasalCode  •  Tipos: Gab (entero)  |  Lit (decimal)  |  Mar (texto)");
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
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(fg.darker());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("JetBrains Mono", Font.BOLD, 10));
        l.setForeground(fg);
        l.setOpaque(false);
        l.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return l;
    }

    // ═════════════════════════════════════════════════════
    //  ANALIZADOR LÉXICO PARA BASALCODE
    // ═════════════════════════════════════════════════════

    private static class TokenInfo {
        String token;
        String lexema;
        String patron;
        String esReservada;

        TokenInfo(String token, String lexema, String patron, String esReservada) {
            this.token      = token;
            this.lexema     = lexema;
            this.patron     = patron;
            this.esReservada = esReservada;
        }
    }

    private List<TokenInfo> analizarCodigo(String codigo) {
        List<TokenInfo> tokens = new ArrayList<>();

        Map<String, Pattern> patrones = new LinkedHashMap<>();

        // Palabras reservadas primero
        patrones.put("TIPO_GAB", Pattern.compile("^Gab\\b"));
        patrones.put("TIPO_LIT", Pattern.compile("^Lit\\b"));
        patrones.put("TIPO_MAR", Pattern.compile("^Mar\\b"));
        patrones.put("MOSTRAR",  Pattern.compile("^mostrar\\b"));

        // Operadores
        patrones.put("SUMA",       Pattern.compile("^\\+"));
        patrones.put("RESTA",      Pattern.compile("^\\-"));
        patrones.put("MULT",       Pattern.compile("^\\*"));
        patrones.put("DIV",        Pattern.compile("^/"));
        patrones.put("CONCAT",     Pattern.compile("^&"));
        patrones.put("ASIGNACION", Pattern.compile("^¬"));
        patrones.put("PUNTO_COMA", Pattern.compile("^;"));

        // Literales
        patrones.put("NUMERO_DECIMAL", Pattern.compile("^-?[0-9]+\\.[0-9]+"));
        patrones.put("NUMERO_ENTERO",  Pattern.compile("^-?[0-9]+(?!\\.[0-9])"));
        patrones.put("CADENA",         Pattern.compile("^\"[^\"\\n]*\""));

        // Identificadores
        patrones.put("IDENTIFICADOR", Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*"));

        // Comentarios
        patrones.put("COMENTARIO", Pattern.compile("^#[^\\n]*"));

        Pattern whitespace = Pattern.compile("^[ \\t\\n\\r]+");

        Set<String> palabrasReservadas = new HashSet<>(Arrays.asList("Gab", "Lit", "Mar", "mostrar"));

        int pos = 0;
        int len = codigo.length();

        while (pos < len) {
            Matcher wsMatcher = whitespace.matcher(codigo.substring(pos));
            if (wsMatcher.find()) {
                pos += wsMatcher.group().length();
                continue;
            }

            boolean matched = false;
            String remaining = codigo.substring(pos);

            for (Map.Entry<String, Pattern> entry : patrones.entrySet()) {
                String tokenType = entry.getKey();
                Pattern pattern  = entry.getValue();
                Matcher matcher  = pattern.matcher(remaining);

                if (matcher.find()) {
                    String lexema = matcher.group();
                    String esReservada = palabrasReservadas.contains(lexema) ? "Sí" : "No";

                    String patronMostrar = switch (tokenType) {
                        case "NUMERO_ENTERO"  -> "[0-9]+";
                        case "NUMERO_DECIMAL" -> "[0-9]+\\.[0-9]+";
                        case "CADENA"         -> "\"[^\"]*\"";
                        case "IDENTIFICADOR"  -> "[a-zA-Z][a-zA-Z0-9_]*";
                        case "COMENTARIO"     -> "#[^\\n]*";
                        case "ASIGNACION"     -> "¬";
                        case "PUNTO_COMA"     -> ";";
                        case "SUMA"           -> "\\+";
                        case "RESTA"          -> "\\-";
                        case "MULT"           -> "\\*";
                        case "DIV"            -> "/";
                        case "CONCAT"         -> "&";
                        default               -> pattern.pattern();
                    };

                    tokens.add(new TokenInfo(tokenType, lexema, patronMostrar, esReservada));
                    pos += lexema.length();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                String errorChar = remaining.substring(0, 1);
                tokens.add(new TokenInfo("ERROR", errorChar, "???", "No"));
                pos++;
            }
        }

        return tokens;
    }

    // ═════════════════════════════════════════════════════
    //  ACCIONES DE LA TABLA DE TOKENS (diálogo)
    // ═════════════════════════════════════════════════════
    private void mostrarDialogoTokens() {
        tokenDialog.setLocationRelativeTo(this);
        tokenDialog.setVisible(true);
    }

    private void llenarTablaConCodigoActual() {
        String codigo = editorArea.getText();
        if (codigo.trim().isEmpty()) {
            JOptionPane.showMessageDialog(tokenDialog,
                "No hay código en el editor para analizar.",
                "Código vacío",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<TokenInfo> tokens = analizarCodigo(codigo);

        tokenModel.setRowCount(0);
        for (TokenInfo token : tokens) {
            tokenModel.addRow(new Object[]{
                token.token, token.lexema, token.patron, token.esReservada
            });
        }

        tokenCountLabel.setText(tokenModel.getRowCount() + " tokens generados");
        tokenCountLabel.setForeground(TK_ACCENT2);
    }

    private void limpiarTablaTokens() {
        tokenModel.setRowCount(0);
        tokenCountLabel.setText("— tokens registrados");
        tokenCountLabel.setForeground(FG_DIM);
    }

    // ═════════════════════════════════════════════════════
    //  ACCIONES DEL EDITOR
    // ═════════════════════════════════════════════════════
    private void ejecutar() {
        String codigo = editorArea.getText().trim();
        if (codigo.isEmpty()) {
            mostrarMensajeInicial();
            return;
        }

        // Analizar tokens
        List<TokenInfo> tokens = analizarCodigo(codigo);

        // Llenar tabla de tokens (diálogo)
        tokenModel.setRowCount(0);
        for (TokenInfo token : tokens) {
            tokenModel.addRow(new Object[]{
                token.token, token.lexema, token.patron, token.esReservada
            });
        }
        if (tokenCountLabel != null) {
            tokenCountLabel.setText(tokenModel.getRowCount() + " tokens generados");
            tokenCountLabel.setForeground(TK_ACCENT2);
        }

        // Ejecutar intérprete
        Interprete interprete = new Interprete();
        String resultado = interprete.ejecutar(codigo);
        boolean hayError = resultado.contains("❌");

        // Actualizar consola con tabla semántica + resultados
        mostrarConsolaConTabla(tokens, resultado.trim(), hayError);

        statusLabel.setText(hayError ? "⚠  Ejecución con errores" : "✓  Ejecución exitosa");
        statusLabel.setForeground(hayError ? ERR_COLOR : OK_COLOR);
    }

    private void limpiar() {
        editorArea.setText("");
        lineNumberArea.setText("1");
        mostrarMensajeInicial();
        statusLabel.setText("Listo  •  BasalCode v1.0");
        statusLabel.setForeground(FG_DIM);
    }

    private String codigoEjemplo() {
        return """
               # ── Ejemplo BasalCode ──────────────────
               # Tipos: Gab (entero), Lit (decimal), Mar (texto)

               Gab a ¬ 500;
               Gab b ¬ 300;
               Gab suma ¬ a + b;
               mostrar suma;

               Lit precio ¬ 19.99;
               Lit iva ¬ 1.16;
               Lit total ¬ precio * iva;
               mostrar total;

               Mar saludo ¬ "Hola";
               Mar nombre ¬ " BasalCode";
               Mar mensaje ¬ saludo & nombre;
               mostrar mensaje;
               """;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(BasalCodeIDE::new);
    }
}

// ═══════════════════════════════════════════════════════════════
//  CLASE INTERPRETE
// ═══════════════════════════════════════════════════════════════
class Interprete {
    private Map<String, Object> variables = new HashMap<>();
    private StringBuilder output = new StringBuilder();

    public String ejecutar(String codigo) {
        output.setLength(0);
        variables.clear();

        String[] lineas = codigo.split("\\n");
        boolean error = false;

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;

            try {
                procesarLinea(linea);
            } catch (Exception e) {
                output.append("❌ Línea ").append(i + 1).append(": ").append(e.getMessage()).append("\n");
                error = true;
            }
        }

        if (!error && output.length() == 0) {
            output.append("✓ Código ejecutado sin errores");
        } else if (error && output.length() == 0) {
            output.append("❌ Error desconocido");
        }

        return output.toString();
    }

    private void procesarLinea(String linea) {
        if (linea.contains("mostrar")) {
            String expr = linea.substring(linea.indexOf("mostrar") + 7).trim();
            expr = expr.replace(";", "").trim();
            Object valor = evaluarExpresion(expr);
            output.append(valor).append("\n");
            return;
        }

        if (linea.contains("¬")) {
            String[] partes = linea.split("¬");
            if (partes.length != 2) throw new RuntimeException("Asignación inválida");

            String tipoYVar  = partes[0].trim();
            String valorExpr = partes[1].trim().replace(";", "").trim();

            String[] tipoVar = tipoYVar.split("\\s+");
            if (tipoVar.length != 2) throw new RuntimeException("Declaración inválida");

            String tipo    = tipoVar[0];
            String varName = tipoVar[1];

            Object valor = evaluarExpresion(valorExpr);

            if (tipo.equals("Gab")) {
                if (!(valor instanceof Integer)) {
                    if (valor instanceof Double) valor = ((Double) valor).intValue();
                    else throw new RuntimeException("Gab requiere entero");
                }
            } else if (tipo.equals("Lit")) {
                if (valor instanceof Integer) valor = ((Integer) valor).doubleValue();
                if (!(valor instanceof Double)) throw new RuntimeException("Lit requiere decimal");
            } else if (tipo.equals("Mar")) {
                if (!(valor instanceof String)) throw new RuntimeException("Mar requiere texto");
            }

            variables.put(varName, valor);
            return;
        }

        throw new RuntimeException("Sintaxis no reconocida");
    }

    private Object evaluarExpresion(String expr) {
        expr = expr.trim();

        if (expr.matches("^-?\\d+$"))         return Integer.parseInt(expr);
        if (expr.matches("^-?\\d+\\.\\d+$"))  return Double.parseDouble(expr);
        if (expr.matches("^\".*\"$"))          return expr.substring(1, expr.length() - 1);
        if (variables.containsKey(expr))       return variables.get(expr);

        if (expr.contains("+")) {
            String[] p = expr.split("\\+", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            if (i instanceof Number && d instanceof Number)
                return ((Number)i).doubleValue() + ((Number)d).doubleValue();
            return i.toString() + d.toString();
        }
        if (expr.contains("-")) {
            String[] p = expr.split("\\-", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            return ((Number)i).doubleValue() - ((Number)d).doubleValue();
        }
        if (expr.contains("*")) {
            String[] p = expr.split("\\*", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            return ((Number)i).doubleValue() * ((Number)d).doubleValue();
        }
        if (expr.contains("/")) {
            String[] p = expr.split("/", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            return ((Number)i).doubleValue() / ((Number)d).doubleValue();
        }
        if (expr.contains("&")) {
            String[] p = expr.split("&", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            return i.toString() + d.toString();
        }

        throw new RuntimeException("Expresión inválida: " + expr);
    }
}