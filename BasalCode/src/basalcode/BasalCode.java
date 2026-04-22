package basalcode;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class BasalCode extends JFrame {

    private JTextField campoEntrada;
    private JTextArea areaResultado;
    private HashMap<String, String> palabrasReservadas;

    public BasalCode() {
      
        setTitle("COMPILADOR PROFESIONAL - BasaldCode");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("gab", "ENTERO");
        palabrasReservadas.put("lit", "DECIMAL");
        palabrasReservadas.put("mar", "TEXTO");

        // Panel de entrada
        JPanel panelSuperior = new JPanel(new GridLayout(3, 1, 5, 5));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel etiqueta = new JLabel("Ingrese sentencia (Ej: gab x ¬ 10 + 5 ;)");
        etiqueta.setFont(new Font("Arial", Font.BOLD, 12));
        campoEntrada = new JTextField();
        JButton botonAnalizar = new JButton("Analizar y Ejecutar");
        botonAnalizar.setBackground(new Color(0, 120, 215));
        botonAnalizar.setForeground(Color.WHITE);
        
        panelSuperior.add(etiqueta);
        panelSuperior.add(campoEntrada);
        panelSuperior.add(botonAnalizar);

        // Área de consola
        areaResultado = new JTextArea();
        areaResultado.setEditable(false);
        areaResultado.setBackground(new Color(20, 20, 20)); // Negro oscuro
        areaResultado.setForeground(new Color(50, 255, 50)); // Verde neon
        areaResultado.setFont(new Font("Consolas", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(areaResultado);

        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        
        botonAnalizar.addActionListener(e -> analizar(campoEntrada.getText().trim()));
    }

    private void analizar(String entrada) {
        if (entrada.isEmpty()) return;
        
        areaResultado.append(">>> ANALIZANDO: " + entrada + "\n");

        // 1. ANÁLISIS LÉXICO: Separar por espacios y signos aunque estén pegados
        String[] tokensRaw = entrada.split("(?<=[\\+\\-\\*/;¬])|(?=[\\+\\-\\*/;¬])|\\s+");
        List<String> tokens = new ArrayList<>();
        for (String t : tokensRaw) { if (!t.trim().isEmpty()) tokens.add(t.trim()); }

        // 2. ANÁLISIS SINTÁCTICO: Verificar estructura básica
        if (tokens.size() < 5) {
            areaResultado.append("   [Sintaxis] ❌ ERROR: Sentencia incompleta o falta el ';'\n\n");
            return;
        }

        String tipo = tokens.get(0);
        String nombre = tokens.get(1);
        String asignacion = tokens.get(2);
        String ultimo = tokens.get(tokens.size() - 1);

        if (!palabrasReservadas.containsKey(tipo)) {
            areaResultado.append("   [Sintaxis] ❌ ERROR: Tipo '" + tipo + "' no reconocido.\n\n");
            return;
        }
        if (!asignacion.equals("¬")) {
            areaResultado.append("   [Sintaxis] ❌ ERROR: Se esperaba el signo '¬'.\n\n");
            return;
        }
        if (!ultimo.equals(";")) {
            areaResultado.append("   [Sintaxis] ❌ ERROR: Falta el ';' al final.\n\n");
            return;
        }

        areaResultado.append("   [Sintaxis] ✔ Estructura correcta.\n");

        // 3. ANÁLISIS SEMÁNTICO Y CÁLCULO
        try {
            if (tokens.size() >= 7) { // Es una operación aritmética
                
                // BLOQUEO DE MAR: Si el tipo es texto, no puede hacer matemáticas
                if (tipo.equals("mar")) {
                    areaResultado.append("   [Semántica] ❌ ERROR: El tipo 'mar' es texto. No permite aritmetica.\n\n");
                    return;
                }

                String val1 = tokens.get(3);
                String operador = tokens.get(4);
                String val2 = tokens.get(5);

                // BLOQUEO DE GAB: No permite valores con punto decimal
                if (tipo.equals("gab") && (val1.contains(".") || val2.contains("."))) {
                    areaResultado.append("   [Semántica] ❌ ERROR: 'gab' es entero, no acepta valores decimales.\n\n");
                    return;
                }

                double n1 = Double.parseDouble(val1);
                double n2 = Double.parseDouble(val2);
                double res = 0;

                switch (operador) {
                    case "+": res = n1 + n2; break;
                    case "-": res = n1 - n2; break;
                    case "*": res = n1 * n2; break;
                    case "/": 
                        if (n2 == 0) { areaResultado.append("   [Error] ❌ Division por cero.\n\n"); return; }
                        res = n1 / n2; 
                        break;
                    default: 
                        areaResultado.append("   [Sintaxis] ❌ Operador no valido.\n\n");
                        return;
                }

                // VALIDACIÓN DE RESULTADO PARA GAB
                if (tipo.equals("gab") && res % 1 != 0) {
                    areaResultado.append("   [Semántica] ❌ ERROR: El resultado es decimal. No cabe en 'gab'.\n\n");
                } else {
                    String salida = (tipo.equals("gab")) ? String.valueOf((int)res) : String.valueOf(res);
                    areaResultado.append("   [Semántica] ✔ EXITO: Variable " + nombre + " asignada con " + salida + "\n\n");
                }

            } else { // Declaración simple (gab x ¬ 10 ;)
                String valor = tokens.get(3);
                if (tipo.equals("gab") && valor.contains(".")) {
                    areaResultado.append("   [Semántica] ❌ ERROR: No puedes meter decimales en 'gab'.\n\n");
                } else {
                    areaResultado.append("   [Semántica] ✔ EXITO: Declaracion simple reconocida.\n\n");
                }
            }
        } catch (Exception ex) {
            areaResultado.append("   [Error] ❌ Error de tipos: Los valores no son numericos.\n\n");
        }
        
        campoEntrada.setText(""); 
    }

    public static void main(String[] args) {
       
        SwingUtilities.invokeLater(() -> {
            new BasalCode().setVisible(true);
        });
    }
}
