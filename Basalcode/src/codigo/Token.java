package codigo;

public class Token {
    public enum Tipo {
        TIPO_GAB, TIPO_LIT, TIPO_MAR,
        IDENTIFICADOR, NUMERO_ENTERO, NUMERO_DECIMAL, CADENA,
        ASIGNACION, MOSTRAR,
        SUMA, RESTA, MULT, DIV, CONCAT,
        PUNTO_COMA, FIN
    }

    public final Tipo tipo;
    public final String valor;
    public final int linea;

    public Token(Tipo tipo, String valor, int linea) {
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
    }

    @Override
    public String toString() {
        return "[" + tipo + " | " + valor + " | L" + linea + "]";
    }
}