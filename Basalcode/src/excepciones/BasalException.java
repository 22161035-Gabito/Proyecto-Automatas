package excepciones;

public class BasalException extends Exception {

    private final int linea;

    public BasalException(String mensaje, int linea) {
        super("[Línea " + linea + "] " + mensaje);
        this.linea = linea;
    }

    public int getLinea() {
        return linea;
    }

    public static class SintaxisException extends BasalException {
        public SintaxisException(String detalle, int linea) {
            super("Error de Sintaxis: " + detalle, linea);
        }
    }
    
    public static class GabException extends BasalException {
        public GabException(String detalle, int linea) {
            super("Error en Gab (entero): " + detalle, linea);
        }
    }

    public static class LitException extends BasalException {
        public LitException(String detalle, int linea) {
            super("Error en Lit (decimal): " + detalle, linea);
        }
    }

    public static class MarException extends BasalException {
        public MarException(String detalle, int linea) {
            super("Error en Mar (texto): " + detalle, linea);
        }
    }

    public static class OperacionException extends BasalException {
        public OperacionException(String detalle, int linea) {
            super("Error de Operación: " + detalle, linea);
        }
    }
}
