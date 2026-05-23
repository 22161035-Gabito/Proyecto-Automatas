package codigo;

public class Token {
    public enum Tipo {
        TIPO_GAB, TIPO_LIT, TIPO_MAR,
        MOSTRAR, IF, ELSE, WHILE,
        IDENTIFICADOR, NUMERO_ENTERO, NUMERO_DECIMAL, CADENA,
        RESERVADA_IGUAL, PUNTO_COMA, LLAVE_A, LLAVE_C,
        SUMA, RESTA, MULT, DIV, CONCAT,
        MAYOR, MAYOR_IGUAL, MENOR, MENOR_IGUAL, IGUAL_IGUAL, DIFERENTE,
        PARENTESIS_A, // (  <- ASEGÚRATE DE QUE ESTÉ AQUÍ
        PARENTESIS_C, // )  <- ASEGÚRATE DE QUE ESTÉ AQUÍ
        FIN
    }

    public final Tipo tipo;
    public final String valor;
    public final int linea;

    public Token(Tipo tipo, String valor, int linea) {
        this.tipo = tipo;
        this.valor = valor;
        this.linea = linea;
    }
}