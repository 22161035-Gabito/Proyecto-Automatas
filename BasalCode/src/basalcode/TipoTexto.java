public class TipoTexto implements ManejadorTipo {
    @Override
    public String evaluarDeclaracion(String nombre, String valor) {
        return "   [Semantica] [OK] EXITO: Variable de texto '" + nombre + "' asignada.\n\n";
    }

    @Override
    public String evaluarOperacion(String nombre, String val1, String operador, String val2) {
        return "   [Semantica] [X] ERROR: El tipo 'mar' es texto. No permite aritmetica.\n\n";
    }
}