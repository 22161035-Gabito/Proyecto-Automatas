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
    private static final Color BG_DARK = new Color(15, 17, 26);
    private static final Color BG_EDITOR = new Color(22, 25, 40);
    private static final Color BG_GUTTER = new Color(12, 14, 22);
    private static final Color BG_CONSOLA = new Color(10, 12, 18);
    private static final Color ACCENT = new Color(82, 190, 255);
    private static final Color FG_MAIN = new Color(220, 225, 240);
    private static final Color FG_DIM = new Color(100, 110, 140);
    private static final Color FG_LINENO = new Color(70, 80, 110);
    private static final Color OK_COLOR = new Color(80, 220, 140);
    private static final Color ERR_COLOR = new Color(255, 90, 90);
    private static final Color BTN_BG = new Color(40, 45, 70);

    // ── Paleta exclusiva ventana de tokens ────────────────
    private static final Color TK_BG_DEEP = new Color( 6, 11, 22);
    private static final Color TK_BG_ROW = new Color(12, 20, 38);
    private static final Color TK_BG_ROW_ALT = new Color(10, 17, 33);
    private static final Color TK_ACCENT = new Color(28, 120, 200);
    private static final Color TK_ACCENT2 = new Color(28, 168, 221);
    private static final Color TK_COL_TOKEN = new Color(56, 178, 245);
    private static final Color TK_COL_LEX = new Color(224, 168, 58);
    private static final Color TK_COL_PAT = new Color(93, 201, 122);
    private static final Color TK_COL_RES = new Color(155, 108, 232);
    private static final Color TK_BORDER = new Color(14, 42, 69);
    private static final Color TK_HEADER_BG = new Color( 8, 14, 26);
    private static final Font MONO = new Font("JetBrains Mono", Font.PLAIN, 14);
    private static final Font MONO_SM = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Font MONO_XS = new Font("JetBrains Mono", Font.PLAIN, 11);
    private static final Font UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    // ── Componentes principales ───────────────────────────
    private JTextArea editorArea;
    private JTextArea lineNumberArea;
    private JPanel consolaPanel;
    private JLabel statusLabel;
    private JButton btnEjecutar;
    private JButton btnLimpiar;
    private JTextArea areaGramatica; // <- Variable para la gramática dinámica

    // ── Diálogo de tokens ─────────────────────────────────
    private JDialog tokenDialog;
    private JTable tokenTable;
    private DefaultTableModel tokenModel;
    private JLabel tokenCountLabel;

    public BasalCodeIDE() {
        setTitle("BasalCode IDE v1.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720); // Un poco más ancho para acomodar el nuevo panel
        setMinimumSize(new Dimension(900, 500));
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
        add(crearPanelGramatica(), BorderLayout.EAST); // <- El panel de la derecha
        add(crearStatus(), BorderLayout.SOUTH);
    }

    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_GUTTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel titulo = new JLabel("⬡ BasalCode IDE");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(ACCENT);
        JLabel tipos = new JLabel(" gab · lit · mar");
        tipos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipos.setForeground(FG_DIM);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(titulo);
        izq.add(tipos);
        btnEjecutar = crearBoton("▶ Ejecutar", ACCENT, BG_DARK);
        btnLimpiar = crearBoton("⊘ Limpiar", FG_DIM, BTN_BG);
        JButton btnTokens = crearBoton("⊞ Tabla Tokens", TK_ACCENT2, new Color(8, 22, 42));
        btnEjecutar.addActionListener(e -> ejecutar());
        btnLimpiar .addActionListener(e -> limpiar());
        btnTokens .addActionListener(e -> mostrarDialogoTokens());
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
        panel.add(etiquetaSeccion("📝 Editor --- BasalCode"), BorderLayout.NORTH);
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
        
        // ✅ LISTENER CORREGIDO: Ya no analiza la gramática al escribir
        editorArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarLineas(); }
            public void removeUpdate(DocumentEvent e) { actualizarLineas(); }
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
        int lineas = editorArea.getText().split("\\n", -1).length;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lineas; i++) {
            if (i > 1) sb.append("\n");
            sb.append(i);
        }
        lineNumberArea.setText(sb.toString());
    }

    // ═════════════════════════════════════════════════════
    // NUEVO PANEL: GRAMÁTICA DINÁMICA
    // ═════════════════════════════════════════════════════
    private JPanel crearPanelGramatica() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_EDITOR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 0, BG_DARK)); 
        panel.setPreferredSize(new Dimension(340, 0)); 
        
        panel.add(etiquetaSeccion("🧩 Gramática Dinámica (If-Else)"), BorderLayout.NORTH);
                
        areaGramatica = new JTextArea();
        areaGramatica.setEditable(false);
        areaGramatica.setFont(MONO_SM);
        areaGramatica.setBackground(BG_CONSOLA);
        areaGramatica.setForeground(FG_MAIN);
        areaGramatica.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // ✅ MENSAJE DE ESPERA POR DEFECTO
        areaGramatica.setText("--- Esperando ejecución ---\n\nPresiona 'Ejecutar' para mapear\nlas reglas gramaticales de\ntus condicionales.");
        
        JScrollPane scroll = new JScrollPane(areaGramatica);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        
        // ✅ ELIMINAMOS la llamada inicial a actualizarGramaticaDinamica()
        
        return panel;
    }

    // ═════════════════════════════════════════════════════
    // PANEL CONSOLA
    // ═════════════════════════════════════════════════════

    private JPanel crearPanelConsola() {
        consolaPanel = new JPanel(new BorderLayout());
        consolaPanel.setBackground(BG_CONSOLA);
        consolaPanel.add(etiquetaSeccion("⬡ Consola --- Salida / Análisis Semántico"), BorderLayout.NORTH);
        mostrarMensajeInicial();
        return consolaPanel;
    }

    private void mostrarMensajeInicial() {
        if (consolaPanel.getComponentCount() > 1) {
            consolaPanel.remove(1);
        }
        JTextArea msg = new JTextArea("--- Ejecuta el código para ver resultados y tabla semántica ---\n\n--- Gramática BasalCode ---\n" +
        "Programa → (Sentencia)*\n" +
        "Sentencia → Declaracion | Mostrar | IfElse\n" +
        "Declaracion → (gab | lit | mar) identificador ¬ (Expresion | constante) ;\n" +
        "Mostrar → mostrar Expresion ;\n" +
        "IfElse → si ( ExpresionComparacion ) { (Sentencia)* } [sino { (Sentencia)* }]\n" +
        "ExpresionComparacion → Expresion (== | != | < | > | <= | >=) Expresion\n" +
        "Expresion → Termino (('+' | '&') Termino)*\n" +
        "Termino → Factor (('*' | '/') Factor)*\n" +
        "Factor → numero | cadena | identificador | '(' Expresion ')'\n" +
        "Tipos: gab (entero), lit (decimal), mar (texto)\n" +
        "Operadores: + (suma/concatenacion), - , * , / , & (concatenacion explicita), ¬ (asignacion)");
        msg.setEditable(false);
        msg.setFont(MONO_SM);
        msg.setBackground(BG_CONSOLA);
        msg.setForeground(FG_DIM);
        msg.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));
        consolaPanel.add(new JScrollPane(msg) {{ setBorder(null); }}, BorderLayout.CENTER);
        consolaPanel.revalidate();
        consolaPanel.repaint();
    }

    private void mostrarConsolaConTabla(List<TokenInfo> tokens, String resultadoEjecucion, boolean hayError) {
        if (consolaPanel.getComponentCount() > 1) {
            consolaPanel.remove(1);
        }
        JSplitPane splitConsola = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitConsola.setDividerLocation(450);
        splitConsola.setDividerSize(4);
        splitConsola.setBackground(BG_CONSOLA);
        splitConsola.setBorder(null);
        splitConsola.setLeftComponent(crearTablaSemantica(tokens));
        splitConsola.setRightComponent(crearPanelResultados(resultadoEjecucion, hayError));
        consolaPanel.add(splitConsola, BorderLayout.CENTER);
        consolaPanel.revalidate();
        consolaPanel.repaint();
    }

    private JPanel crearTablaSemantica(List<TokenInfo> tokens) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TK_BG_DEEP);
        JLabel lbl = new JLabel(" 📋 Análisis Semántico");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TK_ACCENT2);
        lbl.setOpaque(true);
        lbl.setBackground(TK_HEADER_BG);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        panel.add(lbl, BorderLayout.NORTH);
        String[] cols = {"Token", "Lexema", "Categoría"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        Color catPalabraRes = new Color(155, 108, 232);
        Color catIdentif = new Color(56, 178, 245);
        Color catConstante = new Color(93, 201, 122);
        Color catOperador = new Color(224, 168, 58);
        Color catSeparador = new Color(200, 130, 80);
        Color catCadena = new Color(255, 160, 180);
        Color catComentario = new Color(100, 120, 100);
        Color catError = ERR_COLOR;
        for (TokenInfo tk : tokens) {
            String categoria = obtenerCategoria(tk.token); 
            
            model.addRow(new Object[]{tk.lexema, tk.lexema, categoria});
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
        tabla.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
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
        JLabel footer = new JLabel(" " + model.getRowCount() + " tokens analizados");
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
            case "Identificador" -> identif;
            case "Constante entera" -> constante;
            case "Constante decimal" -> constante;
            case "Cadena" -> cadena;
            case "Operador" -> operador;
            case "Signo/Operador" -> operador;
            case "Separador" -> separador;
            case "Comentario" -> comentario;
            default -> error;
        };
    }

    private String obtenerCategoria(String tokenType) {
        return switch (tokenType) {
            case "TIPO_GAB", "TIPO_LIT", "TIPO_MAR", "MOSTRAR", "SI", "SINO" -> "Palabra reservada";
            case "IDENTIFICADOR" -> "Identificador";
            case "NUMERO_ENTERO" -> "Constante entera";
            case "NUMERO_DECIMAL" -> "Constante decimal";
            case "CADENA" -> "Cadena";
            case "SUMA", "RESTA", "MULT", "DIV", "CONCAT", "ASIGNACION" -> "Signo/Operador";
            case "PUNTO_COMA", "PAREN_IZQ", "PAREN_DER", "LLAVE_IZQ", "LLAVE_DER" -> "Separador";
            case "COMPARACION" -> "Operador de comparación";
            case "COMENTARIO" -> "Comentario";
            default -> "Desconocido";
        };
    }

    private JPanel crearPanelResultados(String resultado, boolean hayError) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_CONSOLA);
        JLabel lbl = new JLabel(" ▶ Resultados de Ejecución");
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
        statusLabel = new JLabel("Listo • BasalCode v1.0 • Tipos: gab | lit | mar");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(FG_DIM);
        JLabel hint = new JLabel("Ctrl+Enter = Ejecutar");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(FG_DIM);
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(hint, BorderLayout.EAST);
        return panel;
    }

    private JLabel etiquetaSeccion(String texto) {
        JLabel l = new JLabel(" " + texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(FG_DIM);
        l.setOpaque(true);
        l.setBackground(BG_GUTTER);
        l.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return l;
    }

    // ═════════════════════════════════════════════════════
    // DIÁLOGO TABLA DE TOKENS
    // ═════════════════════════════════════════════════════

    private void construirDialogoTokens() {
        tokenDialog = new JDialog(this, "Tabla de Tokens --- BasalCode", true);
        tokenDialog.setSize(850, 550);
        tokenDialog.setMinimumSize(new Dimension(650, 420));
        tokenDialog.setLocationRelativeTo(this);
        tokenDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        tokenDialog.getContentPane().setBackground(TK_BG_DEEP);
        tokenDialog.setLayout(new BorderLayout(0, 0));
        tokenDialog.add(crearDialogoHeader(), BorderLayout.NORTH);
        tokenDialog.add(crearDialogoTabla(), BorderLayout.CENTER);
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
        JLabel titulo = new JLabel(" TABLA DE TOKENS");
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
        panel.add(izq, BorderLayout.WEST);
        panel.add(btnCerrar, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearBarraAcciones() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 17, 32));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JButton btnLlenar = crearBotonDialogo("✎ Llenar con Código", TK_ACCENT2, new Color(10, 40, 75));
        JButton btnLimpiarT = crearBotonDialogo("⊘ Limpiar Tabla", TK_COL_RES, new Color(25, 15, 45));
        btnLlenar .addActionListener(e -> llenarTablaConCodigoActual());
        btnLimpiarT.addActionListener(e -> limpiarTablaTokens());
        tokenCountLabel = new JLabel("--- tokens registrados");
        tokenCountLabel.setFont(MONO_XS);
        tokenCountLabel.setForeground(FG_DIM);
        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izq.setOpaque(false);
        izq.add(btnLlenar);
        izq.add(btnLimpiarT);
        panel.add(izq, BorderLayout.WEST);
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
        int[] anchos = { 155, 120, 220, 95 };
        for (int i = 0; i < anchos.length; i++)
        tokenTable.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
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
        tokenTable.getColumnModel().getColumn(3).setCellRenderer(
        new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
            JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                boolean es = "Sí".equals(v);
                lbl.setText(es ? " Sí " : " No ");
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
        "BasalCode • Tipos: gab (entero) | lit (decimal) | mar (texto)");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(new Color(50, 80, 120));
        JLabel esc = new JLabel("ESC para cerrar");
        esc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        esc.setForeground(new Color(50, 80, 120));
        panel.add(info, BorderLayout.WEST);
        panel.add(esc, BorderLayout.EAST);
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
    // ANALIZADOR LÉXICO PARA BASALCODE (TODO EN MINÚSCULAS)
    // ═════════════════════════════════════════════════════

    private static class TokenInfo {
        String token;
        String lexema;
        String patron;
        String esReservada;
        TokenInfo(String token, String lexema, String patron, String esReservada) {
            this.token = token;
            this.lexema = lexema;
            this.patron = patron;
            this.esReservada = esReservada;
        }
    }

    private List<TokenInfo> analizarCodigo(String codigo) {
        List<TokenInfo> tokens = new ArrayList<>();
        Map<String, Pattern> patrones = new LinkedHashMap<>();
        
        // Palabras reservadas en minúsculas
        patrones.put("TIPO_GAB", Pattern.compile("^gab\\b"));
        patrones.put("TIPO_LIT", Pattern.compile("^lit\\b"));
        patrones.put("TIPO_MAR", Pattern.compile("^mar\\b"));
        patrones.put("MOSTRAR", Pattern.compile("^mostrar\\b"));
        
        // ✅ CORRECCIÓN: Agregamos soporte explícito para si/if y sino/else
        patrones.put("SI", Pattern.compile("^(si|if)\\b"));
        patrones.put("SINO", Pattern.compile("^(sino|else)\\b"));
        
        // Operadores
        patrones.put("SUMA", Pattern.compile("^\\+"));
        patrones.put("RESTA", Pattern.compile("^-"));
        patrones.put("MULT", Pattern.compile("^\\*"));
        patrones.put("DIV", Pattern.compile("^/"));
        patrones.put("CONCAT", Pattern.compile("^&"));
        patrones.put("ASIGNACION", Pattern.compile("^¬"));  
        // Comparadores
        patrones.put("COMPARACION", Pattern.compile("^(==|!=|<=|>=|<|>)"));
        patrones.put("PUNTO_COMA", Pattern.compile("^;"));
        patrones.put("PAREN_IZQ", Pattern.compile("^\\("));
        patrones.put("PAREN_DER", Pattern.compile("^\\)"));
        patrones.put("LLAVE_IZQ", Pattern.compile("^\\{"));
        patrones.put("LLAVE_DER", Pattern.compile("^\\}"));
        // Literales
        patrones.put("NUMERO_DECIMAL", Pattern.compile("^-?[0-9]+\\.[0-9]+"));
        patrones.put("NUMERO_ENTERO", Pattern.compile("^-?[0-9]+(?!\\.[0-9])"));
        patrones.put("CADENA", Pattern.compile("^\"[^\"\\n]*\""));
        // Identificadores
        patrones.put("IDENTIFICADOR", Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*"));
        // Comentarios
        patrones.put("COMENTARIO", Pattern.compile("^#[^\\n]*"));
        Pattern whitespace = Pattern.compile("^[ \\t\\n\\r]+");
        
        // ✅ CORRECCIÓN: Añadimos "if" y "else" a la lista oficial de validación
        Set<String> palabrasReservadas = new HashSet<>(Arrays.asList("gab", "lit", "mar", "mostrar", "si", "sino", "if", "else", "¬"));
        
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
                Pattern pattern = entry.getValue();
                Matcher matcher = pattern.matcher(remaining);
                if (matcher.find()) {
                    String lexema = matcher.group();
                    String esReservada = palabrasReservadas.contains(lexema) ? "Sí" : "No";
                    String patronMostrar = switch (tokenType) {
                        case "NUMERO_ENTERO" -> "[0-9]+";
                        case "NUMERO_DECIMAL" -> "[0-9]+\\.[0-9]+";
                        case "CADENA" -> "\"[^\"]*\"";
                        case "IDENTIFICADOR" -> "[a-zA-Z][a-zA-Z0-9_]*";
                        case "COMENTARIO" -> "#[^\\n]*";
                        case "ASIGNACION" -> "¬";
                        case "PUNTO_COMA" -> ";";
                        case "SUMA" -> "\\+";
                        case "RESTA" -> "-";
                        case "MULT" -> "\\*";
                        case "DIV" -> "/";
                        case "CONCAT" -> "&";
                        case "COMPARACION" -> "==|!=|<=|>=|<|>";
                        case "PAREN_IZQ" -> "\\(";
                        case "PAREN_DER" -> "\\)";
                        case "LLAVE_IZQ" -> "\\{";
                        case "LLAVE_DER" -> "\\}";
                        case "SI" -> "si|if";       // ✅ Actualizado para mostrar el patrón correcto
                        case "SINO" -> "sino|else"; // ✅ Actualizado para mostrar el patrón correcto
                        default -> pattern.pattern();
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
    // ACCIONES DE LA TABLA DE TOKENS
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
            // ✅ CAMBIO AQUÍ: Usamos token.lexema en las dos primeras posiciones
            tokenModel.addRow(new Object[]{
                token.lexema, token.lexema, token.patron, token.esReservada
            });
        }
        tokenCountLabel.setText(tokenModel.getRowCount() + " tokens generados");
        tokenCountLabel.setForeground(TK_ACCENT2);
    }

    private void limpiarTablaTokens() {
        tokenModel.setRowCount(0);
        tokenCountLabel.setText("--- tokens registrados");
        tokenCountLabel.setForeground(FG_DIM);
    }

    // ═════════════════════════════════════════════════════
    // ACCIONES DEL EDITOR
    // ═════════════════════════════════════════════════════

    private void ejecutar() {
        String codigo = editorArea.getText().trim();
        if (codigo.isEmpty()) {
            mostrarMensajeInicial();
            return;
        }
        
        actualizarGramaticaDinamica();
        
        List<TokenInfo> tokens = analizarCodigo(codigo);
        tokenModel.setRowCount(0);
        for (TokenInfo token : tokens) {
            // ✅ CAMBIO AQUÍ TAMBIÉN: token.lexema se repite
            tokenModel.addRow(new Object[]{
                token.lexema, token.lexema, token.patron, token.esReservada
            });
        }
        if (tokenCountLabel != null) {
            tokenCountLabel.setText(tokenModel.getRowCount() + " tokens generados");
            tokenCountLabel.setForeground(TK_ACCENT2);
        }
        Interprete interprete = new Interprete();
        String resultado = interprete.ejecutar(codigo);
        boolean hayError = resultado.contains("❌");
        mostrarConsolaConTabla(tokens, resultado.trim(), hayError);
        statusLabel.setText(hayError ? "⚠ Ejecución con errores" : "✓ Ejecución exitosa");
        statusLabel.setForeground(hayError ? ERR_COLOR : OK_COLOR);
    }

    private void limpiar() {
        editorArea.setText("");
        lineNumberArea.setText("1");
        mostrarMensajeInicial();
        
        // ✅ AL LIMPIAR, DEVOLVEMOS LA GRAMÁTICA AL ESTADO DE ESPERA
        if (areaGramatica != null) {
            areaGramatica.setText("--- Esperando ejecución ---\n\nPresiona 'Ejecutar' para mapear\nlas reglas gramaticales de\ntus condicionales.");
        }
        
        statusLabel.setText("Listo • BasalCode v1.0");
        statusLabel.setForeground(FG_DIM);
    }

    private String codigoEjemplo() {
        return """
                # ── Ejemplo BasalCode (Sintaxis Actualizada) ──────────────────
                # Tipos: gab (entero), lit (decimal), mar (texto)
                # Formato: identificador tipo ¬ valor;
                a gab ¬ 500;
                b gab ¬ 300;
                suma gab ¬ a + b;
                mostrar suma;
                
                precio lit ¬ 19.99;
                iva lit ¬ 1.16;
                total lit ¬ precio * iva;
                mostrar total;
                
                saludo mar ¬ "Hola";
                nombre mar ¬ " BasalCode";
                mensaje mar ¬ saludo & nombre;
                mostrar mensaje;
                
                # Estructuras de control condicional (if / else)
                edad gab ¬ 18;
                if (edad >= 18) {
                    mostrar "Eres mayor de edad";
                } else {
                    mostrar "Eres menor de edad";
                }
                
                x gab ¬ 10;
                if (x == 10) {
                    mostrar "x es igual a 10";
                } else {
                    mostrar "x no es 10";
                }
                """;
    }

    private void actualizarGramaticaDinamica() {
        if (editorArea == null || areaGramatica == null) return;
        
        String[] lineas = editorArea.getText().split("\\n");
        StringBuilder sb = new StringBuilder();
        
        sb.append("--- ANALIZADOR SINTÁCTICO GENERAL ---\n");
        sb.append("<Programa> -> (<Sentencia>)*\n\n");
        
        int lineNum = 1;
        for (String lineaOriginal : lineas) {
            String linea = lineaOriginal.trim();
            
            // Ignorar líneas completamente vacías en el mapeo
            if (linea.isEmpty()) {
                lineNum++;
                continue;
            }
            
            // Indicador de línea actual
            sb.append(String.format("[%02d] ", lineNum));
            
            // 1. Regla para Comentarios
            if (linea.startsWith("#")) {
                sb.append("<Comentario> -> ").append(linea).append("\n");
            } 
            
            // 2. Regla para Declaraciones y Asignaciones
            else if (linea.contains("¬") && linea.endsWith(";")) {
                String[] partes = linea.split("¬", 2);
                String izq = partes[0].trim();
                String der = partes[1].replace(";", "").trim();
                String[] elementos = izq.split("\\s+");
                
                sb.append("<Sentencia> -> <Declaración>\n");
                if (elementos.length == 2) {
                    sb.append("     <identificador> -> ").append(elementos[0]).append("\n")
                      .append("     <tipo_dato>     -> ").append(elementos[1]).append("\n");
                } else {
                    sb.append("     <definición>    -> ").append(izq).append("\n");
                }
                sb.append("     <asignación>    -> ¬\n")
                  .append("     <expresión>     -> ").append(der).append("\n")
                  .append("     <fin_sentencia> -> ;\n");
            } 
            
            // 3. Regla para Mostrar en Consola
            else if (linea.startsWith("mostrar ")) {
                String expr = linea.substring(7).replace(";", "").trim();
                sb.append("<Sentencia> -> <Mostrar>\n")
                  .append("     <pal_reservada> -> mostrar\n")
                  .append("     <expresión>     -> ").append(expr).append("\n")
                  .append("     <fin_sentencia> -> ;\n");
            } 
            
            // 4. Regla para el Inicio de Condicionales (si / if)
            else if (linea.matches("^(si|if)\\s*\\(.*\\)\\s*\\{?$")) {
                Pattern p = Pattern.compile("^(si|if)\\s*\\((.*)\\)\\s*\\{?$");
                Matcher m = p.matcher(linea);
                if (m.find()) {
                    String condicion = m.group(2).trim();
                    sb.append("<Sentencia> -> <IfElse> (Inicio)\n")
                      .append("     <pal_reservada> -> ").append(m.group(1)).append("\n")
                      .append("     <condición>     -> ( ").append(condicion).append(" )\n");
                    if (linea.endsWith("{")) {
                        sb.append("     <apertura_bloque>-> {\n");
                    }
                }
            } 
            
            // 5. Regla para la Alternativa Condicional (sino / else)
            else if (linea.matches("^(sino|else)\\s*\\{?$")) {
                sb.append("<Sentencia> -> <IfElse> (Sino)\n")
                  .append("     <pal_reservada> -> ").append(linea.replace("{", "").trim()).append("\n");
                if (linea.endsWith("{")) {
                    sb.append("     <apertura_bloque>-> {\n");
                }
            } 
            
            // 6. Regla para Llaves de Cierre Sueltas
            else if (linea.equals("{") || linea.equals("}")) {
                sb.append("<Delimitador> -> ").append(linea).append("\n");
            } 
            
            // 7. Cualquier otra estructura no identificada formalmente
            else {
                sb.append("<Sentencia> -> ").append(linea).append("\n");
            }
            
            sb.append("\n");
            lineNum++;
        }
        
        areaGramatica.setText(sb.toString());
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(BasalCodeIDE::new);
    }
}

// ═══════════════════════════════════════════════════════════════
// CLASE INTERPRETE (CON SOPORTE PARA IF-ELSE)
// ═══════════════════════════════════════════════════════════════

class Interprete {
    private Map<String, Object> variables = new HashMap<>();
    private StringBuilder output = new StringBuilder();
    private int posicion = 0;
    private String[] lineasPrograma;
    private List<String> lineasOriginales = new ArrayList<>();

    public String ejecutar(String codigo) {
        output.setLength(0);
        variables.clear();
        lineasOriginales.clear();
        String[] lineas = codigo.split("\\n");
        for (String linea : lineas) {
            lineasOriginales.add(linea);
        }
        lineasPrograma = lineas;
        boolean error = false;
        posicion = 0;
        ejecutarBloque(0, lineas.length - 1);
        return output.toString();
    }

    private void ejecutarBloque(int inicio, int fin) {
        int i = inicio;
        while (i <= fin) {
            String lineaOriginal = lineasOriginales.get(i);
            String linea = lineaOriginal.trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                i++;
                continue;
            }
            try {
                if (linea.startsWith("si") || linea.startsWith("if")) { // Modificado para aceptar 'if' o 'si' en la ejecución
                    i = procesarIfElse(i);
                } else {
                    procesarLinea(linea);
                    i++;
                }
            } catch (Exception e) {
                output.append("❌ Línea ").append(i + 1).append(": ").append(e.getMessage()).append("\n");
                i++;
            }
        }
    }

    private int procesarIfElse(int indice) throws Exception {
        String lineaIzq = lineasOriginales.get(indice).trim();
        if (!lineaIzq.startsWith("si") && !lineaIzq.startsWith("if")) {
            throw new RuntimeException("Se esperaba 'si' o 'if'");
        }
        // Ajustamos dinámicamente si empieza con "si" o "if"
        int offset = lineaIzq.startsWith("si") ? 2 : 2; 
        String contenidoCond = lineaIzq.substring(offset).trim();
        if (!contenidoCond.startsWith("(")) {
            throw new RuntimeException("Se esperaba '(' después de 'si'/'if'");
        }
        int parentesisCierre = encontrarParentesisCierre(contenidoCond);
        if (parentesisCierre == -1) {
            throw new RuntimeException("Paréntesis no cerrado en condición");
        }
        String condicionStr = contenidoCond.substring(1, parentesisCierre).trim();
        boolean condicion = evaluarComparacion(condicionStr);
        int cuerpoInicio = indice + 1;
        int cuerpoFin = -1;
        int contadorLlaves = 0;
        boolean cuerpoEncontrado = false;
        for (int i = cuerpoInicio; i < lineasOriginales.size(); i++) {
            String lineaActual = lineasOriginales.get(i).trim();
            for (int j = 0; j < lineaActual.length(); j++) {
                char c = lineaActual.charAt(j);
                if (c == '{') contadorLlaves++;
                else if (c == '}') contadorLlaves--;
            }
            if (contadorLlaves > 0 && !cuerpoEncontrado) {
                cuerpoEncontrado = true;
                cuerpoInicio = i;
            }
            if (cuerpoEncontrado && contadorLlaves == 0) {
                cuerpoFin = i;
                break;
            }
        }
        if (cuerpoFin == -1) {
            throw new RuntimeException("No se encontró el cuerpo del if");
        }
        int sinoIndice = -1;
        int sinoCuerpoInicio = -1;
        int sinoCuerpoFin = -1;
        for (int i = cuerpoFin + 1; i < lineasOriginales.size(); i++) {
            String lineaActual = lineasOriginales.get(i).trim();
            if (lineaActual.startsWith("sino") || lineaActual.startsWith("else")) {
                sinoIndice = i;
                int llavesSino = 0;
                boolean sinoCuerpoEncontrado = false;
                for (int j = i + 1; j < lineasOriginales.size(); j++) {
                    String lineaSino = lineasOriginales.get(j).trim();
                    for (int k = 0; k < lineaSino.length(); k++) {
                        char c = lineaSino.charAt(k);
                        if (c == '{') llavesSino++;
                        else if (c == '}') llavesSino--;
                    }
                    if (llavesSino > 0 && !sinoCuerpoEncontrado) {
                        sinoCuerpoEncontrado = true;
                        sinoCuerpoInicio = j;
                    }
                    if (sinoCuerpoEncontrado && llavesSino == 0) {
                        sinoCuerpoFin = j;
                        break;
                    }
                }
                break;
            }
        }
        if (condicion) {
            ejecutarBloqueInterno(cuerpoInicio, cuerpoFin);
        } else if (sinoIndice != -1 && sinoCuerpoInicio != -1) {
            ejecutarBloqueInterno(sinoCuerpoInicio, sinoCuerpoFin);
        }
        return Math.max(cuerpoFin, sinoCuerpoFin) + 1;
    }

    private void ejecutarBloqueInterno(int inicio, int fin) {
        for (int i = inicio; i <= fin; i++) {
            String linea = lineasOriginales.get(i).trim();
            if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("{") || linea.startsWith("}")) {
                continue;
            }
            if (linea.startsWith("si") || linea.startsWith("if")) {
                try {
                    i = procesarIfElse(i) - 1;
                } catch (Exception e) {
                    output.append("❌ Error en bloque: ").append(e.getMessage()).append("\n");
                }
                continue;
            }
            try {
                procesarLinea(linea);
            } catch (Exception e) {
                output.append("❌ Error en bloque: ").append(e.getMessage()).append("\n");
            }
        }
    }

    private int encontrarParentesisCierre(String str) {
        int contador = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '(') contador++;
            else if (c == ')') {
                contador--;
                if (contador == 0) return i;
            }
        }
        return -1;
    }

    private boolean evaluarComparacion(String expr) throws Exception {
        String[] operadores = {"==", "!=", "<=", ">=", "<", ">"};
        for (String op : operadores) {
            if (expr.contains(op)) {
                String[] partes = expr.split(op, 2);
                Object izquierdo = evaluarExpresion(partes[0].trim());
                Object derecho = evaluarExpresion(partes[1].trim());
                if (izquierdo instanceof Number && derecho instanceof Number) {
                    double izqNum = ((Number) izquierdo).doubleValue();
                    double derNum = ((Number) derecho).doubleValue();
                    return switch (op) {
                        case "==" -> izqNum == derNum;
                        case "!=" -> izqNum != derNum;
                        case "<=" -> izqNum <= derNum;
                        case ">=" -> izqNum >= derNum;
                        case "<" -> izqNum < derNum;
                        case ">" -> izqNum > derNum;
                        default -> false;
                    };
                } else if (izquierdo instanceof String && derecho instanceof String) {
                    String izqStr = izquierdo.toString();
                    String derStr = derecho.toString();
                    return switch (op) {
                        case "==" -> izqStr.equals(derStr);
                        case "!=" -> !izqStr.equals(derStr);
                        default -> throw new RuntimeException("Operador " + op + " no soportado para strings");
                    };
                } else {
                    throw new RuntimeException("Tipos incompatibles en comparación");
                }
            }
        }
        throw new RuntimeException("Expresión de comparación inválida: " + expr);
    }

    private void procesarLinea(String linea) throws Exception {
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
            String tipoYVar = partes[0].trim();
            String valorExpr = partes[1].trim().replace(";", "").trim();
            String[] tipoVar = tipoYVar.split("\\s+");
            if (tipoVar.length != 2) throw new RuntimeException("Declaración inválida");
            String tipo = tipoVar[0];
            String varName = tipoVar[1];
            Object valor = evaluarExpresion(valorExpr);
            if (tipo.equals("gab")) {
                if (!(valor instanceof Integer)) {
                    if (valor instanceof Double) valor = ((Double) valor).intValue();
                    else throw new RuntimeException("gab requiere entero");
                }
            } else if (tipo.equals("lit")) {
                if (valor instanceof Integer) valor = ((Integer) valor).doubleValue();
                if (!(valor instanceof Double)) throw new RuntimeException("lit requiere decimal");
            } else if (tipo.equals("mar")) {
                if (!(valor instanceof String)) throw new RuntimeException("mar requiere texto");
            }
            variables.put(varName, valor);
            return;
        }
        throw new RuntimeException("Sintaxis no reconocida: " + linea);
    }

    private Object evaluarExpresion(String expr) throws Exception {
        expr = expr.trim();
        if (expr.matches("^-?\\d+$")) return Integer.parseInt(expr);
        if (expr.matches("^-?\\d+\\.\\d+$")) return Double.parseDouble(expr);
        if (expr.matches("^\".*\"$")) return expr.substring(1, expr.length() - 1);
        if (variables.containsKey(expr)) return variables.get(expr);
        if (expr.contains("+")) {
            String[] p = expr.split("\\+", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            if (i instanceof Number && d instanceof Number) {
                double resultado = ((Number) i).doubleValue() + ((Number) d).doubleValue();
                if (resultado == (int) resultado && !(i instanceof Double) && !(d instanceof Double)) {
                    return (int) resultado;
                }
                return resultado;
            }
            return i.toString() + d.toString();
        }
        if (expr.contains("-")) {
            String[] p = expr.split("-", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            double resultado = ((Number) i).doubleValue() - ((Number) d).doubleValue();
            if (resultado == (int) resultado) return (int) resultado;
            return resultado;
        }
        if (expr.contains("*")) {
            String[] p = expr.split("\\*", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            double resultado = ((Number) i).doubleValue() * ((Number) d).doubleValue();
            if (resultado == (int) resultado) return (int) resultado;
            return resultado;
        }
        if (expr.contains("/")) {
            String[] p = expr.split("/", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            double resultado = ((Number) i).doubleValue() / ((Number) d).doubleValue();
            if (resultado == (int) resultado) return (int) resultado;
            return resultado;
        }
        if (expr.contains("&")) {
            String[] p = expr.split("&", 2);
            Object i = evaluarExpresion(p[0].trim()), d = evaluarExpresion(p[1].trim());
            return i.toString() + d.toString();
        }
        throw new RuntimeException("Expresión inválida: " + expr);
    }
}
