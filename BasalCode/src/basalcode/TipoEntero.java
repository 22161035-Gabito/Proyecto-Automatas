public class TipoEntero implements ManejadorTipo {
    @Override
    public String evaluarDeclaracion(String nombre, String valor) {
        if (valor.contains(".")) {
            return "   [Semantica] [X] ERROR: No puedes meter decimales en 'gab'.\n\n";
        }
        return "   [Semantica] [OK] EXITO: Declaracion simple reconocida (" + valor + ").\n\n";
    }

    @Override
    public String evaluarOperacion(String nombre, String val1, String operador, String val2) {
        if (val1.contains(".") || val2.contains(".")) {
            return "   [Semantica] [X] ERROR: 'gab' es entero, no acepta valores decimales.\n\n";
        }
        try {
            double n1 = Double.parseDouble(val1);
            double n2 = Double.parseDouble(val2);
            double res = 0;

            switch (operador) {
                case "+": res = n1 + n2; break;
                case "-": res = n1 - n2; break;
                case "*": res = n1 * n2; break;
                case "/":
                    if (n2 == 0) return "   [Error] [X] Division por cero.\n\n";
                    res = n1 / n2; break;
                default: return "   [Sintaxis] [X] Operador no valido.\n\n";
            }

            if (res % 1 != 0) {
                return "   [Semantica] [X] ERROR: El resultado es decimal. No cabe en 'gab'.\n\n";
            }
            return "   [Semantica] [OK] EXITO: Variable " + nombre + " asignada con " + (int)res + "\n\n";
        } catch (Exception e) {
            return "   [Error] [X] Error de tipos: Los valores no son numericos.\n\n";
        }
    }
}