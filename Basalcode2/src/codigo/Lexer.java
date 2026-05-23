package codigo;

import excepciones.BasalException.SintaxisException;
import java.util.*;

public class Lexer {
    private final String codigo;
    private int pos = 0;
    private int linea = 1;

    public Lexer(String codigo) {
        this.codigo = codigo;
    }

    public List<Token> tokenizar() throws SintaxisException {
        List<Token> tokens = new ArrayList<>();
        while (pos < codigo.length()) {
            char c = codigo.charAt(pos);

            if (c == '\n') { linea++; pos++; continue; }
            if (Character.isWhitespace(c)) { pos++; continue; }
            if (c == '#') { 
                while (pos < codigo.length() && codigo.charAt(pos) != '\n') pos++;
                continue;
            }
            if (c == '"') { tokens.add(leerCadena()); continue; }
            if (Character.isDigit(c) || (c == '-' && pos + 1 < codigo.length()
                    && Character.isDigit(codigo.charAt(pos + 1)))) {
                tokens.add(leerNumero()); continue;
            }
            if (Character.isLetter(c)) { tokens.add(leerIdentificador()); continue; }

            switch (c) {
                case '¬' -> { tokens.add(new Token(Token.Tipo.RESERVADA_IGUAL, "¬", linea)); pos++; }
                case '{' -> { tokens.add(new Token(Token.Tipo.LLAVE_A, "{", linea)); pos++; } 
                case '}' -> { tokens.add(new Token(Token.Tipo.LLAVE_C, "}", linea)); pos++; } 
                case '(' -> { tokens.add(new Token(Token.Tipo.PARENTESIS_A, "(", linea)); pos++; } 
                case ')' -> { tokens.add(new Token(Token.Tipo.PARENTESIS_C, ")", linea)); pos++; } 
                case '+' -> { tokens.add(new Token(Token.Tipo.SUMA, "+", linea)); pos++; }
                case '-' -> { tokens.add(new Token(Token.Tipo.RESTA, "-", linea)); pos++; }
                case '*' -> { tokens.add(new Token(Token.Tipo.MULT, "*", linea)); pos++; }
                case '/' -> { tokens.add(new Token(Token.Tipo.DIV, "/", linea)); pos++; }
                case '&' -> { tokens.add(new Token(Token.Tipo.CONCAT, "&", linea)); pos++; }
                case ';' -> { tokens.add(new Token(Token.Tipo.PUNTO_COMA, ";", linea)); pos++; }
                case '>' -> {
                    if (pos + 1 < codigo.length() && codigo.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Tipo.MAYOR_IGUAL, ">=", linea)); pos += 2;
                    } else { tokens.add(new Token(Token.Tipo.MAYOR, ">", linea)); pos++; }
                }
                case '<' -> {
                    if (pos + 1 < codigo.length() && codigo.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Tipo.MENOR_IGUAL, "<=", linea)); pos += 2;
                    } else { tokens.add(new Token(Token.Tipo.MENOR, "<", linea)); pos++; }
                }
                case '=' -> {
                    if (pos + 1 < codigo.length() && codigo.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Tipo.IGUAL_IGUAL, "==", linea)); pos += 2;
                    } else {
                        throw new SintaxisException("Carácter inesperado: '=' aislado. Use '¬' para asignar o '==' para comparar.", linea);
                    }
                }
                case '!' -> {
                    if (pos + 1 < codigo.length() && codigo.charAt(pos + 1) == '=') {
                        tokens.add(new Token(Token.Tipo.DIFERENTE, "!=", linea)); pos += 2;
                    } else {
                        throw new SintaxisException("Carácter inesperado: '!'", linea);
                    }
                }
                default -> throw new SintaxisException("Carácter inesperado: '" + c + "'", linea);
            }
        }
        tokens.add(new Token(Token.Tipo.FIN, "", linea));
        return tokens;
    }

    private Token leerCadena() throws SintaxisException {
        int inicio = linea;
        StringBuilder sb = new StringBuilder("\"");
        pos++; 
        while (pos < codigo.length() && codigo.charAt(pos) != '"') {
            if (codigo.charAt(pos) == '\n') throw new SintaxisException("Cadena sin cerrar.", inicio);
            sb.append(codigo.charAt(pos++));
        }
        if (pos >= codigo.length()) throw new SintaxisException("Cadena sin cerrar.", inicio);
        sb.append('"'); pos++;
        return new Token(Token.Tipo.CADENA, sb.toString(), inicio);
    }

    private Token leerNumero() {
        StringBuilder sb = new StringBuilder();
        boolean esDecimal = false;
        if (codigo.charAt(pos) == '-') sb.append(codigo.charAt(pos++));
        while (pos < codigo.length() && (Character.isDigit(codigo.charAt(pos)) || codigo.charAt(pos) == '.')) {
            if (codigo.charAt(pos) == '.') esDecimal = true;
            sb.append(codigo.charAt(pos++));
        }
        Token.Tipo tipo = esDecimal ? Token.Tipo.NUMERO_DECIMAL : Token.Tipo.NUMERO_ENTERO;
        return new Token(tipo, sb.toString(), linea);
    }

    private Token leerIdentificador() throws SintaxisException {
        StringBuilder sb = new StringBuilder();
        while (pos < codigo.length() && (Character.isLetterOrDigit(codigo.charAt(pos)) || codigo.charAt(pos) == '_')) {
            sb.append(codigo.charAt(pos++));
        }
        String palabra = sb.toString();

        for (int i = 0; i < palabra.length(); i++) {
            if (Character.isUpperCase(palabra.charAt(i))) {
                throw new SintaxisException("No se permiten letras mayúsculas en el código: '" + palabra + "'", linea);
            }
        }

        // Retornamos directamente los tipos específicos mapeados
        return switch (palabra) {
            case "gab"          -> new Token(Token.Tipo.TIPO_GAB,     palabra, linea);
            case "lit"          -> new Token(Token.Tipo.TIPO_LIT,     palabra, linea);
            case "mar"          -> new Token(Token.Tipo.TIPO_MAR,     palabra, linea);
            case "mostrar"      -> new Token(Token.Tipo.MOSTRAR,      palabra, linea);
            case "si", "if"     -> new Token(Token.Tipo.IF,          palabra, linea); 
            case "sino", "else" -> new Token(Token.Tipo.ELSE,        palabra, linea); 
            case "while"        -> new Token(Token.Tipo.WHILE,        palabra, linea); 
            default             -> new Token(Token.Tipo.IDENTIFICADOR, palabra, linea);
        };
    }
}