package codigo;

import operaciones.Operaciones;
import excepciones.BasalException;
import excepciones.BasalException.SintaxisException;
import excepciones.BasalException.GabException;
import excepciones.BasalException.LitException;
import excepciones.BasalException.MarException;
import excepciones.BasalException.OperacionException;
import tipos.*;
import java.math.BigDecimal;
import java.util.*;

public class Interprete {

    private final Map<String, Object> variables = new HashMap<>();
    private final Map<String, String> tiposVars = new HashMap<>(); 
    private List<Token> tokens;
    private int cursor;
    private final StringBuilder salida = new StringBuilder();

    public String ejecutar(String codigo) {
        salida.setLength(0);
        variables.clear();
        tiposVars.clear();
        try {
            Lexer lexer = new Lexer(codigo);
            tokens = lexer.tokenizar();
            cursor = 0;
            while (cursor < tokens.size() && !actual().tipo.equals(Token.Tipo.FIN)) {
                instruccion();
            }
        } catch (BasalException e) {
            salida.append("\n❌ ").append(e.getMessage());
        } catch (Exception e) {
            salida.append("\n❌ Error crítico en ejecución: ").append(e.getMessage());
        }
        return salida.toString();
    }

    private Token actual() { 
        if (cursor >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(cursor); 
    }
    
    private Token siguiente() { 
        if (cursor + 1 < tokens.size()) return tokens.get(cursor + 1);
        return tokens.get(tokens.size() - 1); 
    }
    
    private Token consumir() { 
        Token t = actual();
        if (cursor < tokens.size()) cursor++;
        return t;
    }

    private Token esperar(Token.Tipo tipo) throws SintaxisException {
        Token t = actual();
        if (!t.tipo.equals(tipo))
            throw new SintaxisException("Se esperaba " + tipo + " pero se encontró '" + t.valor + "'", t.linea);
        return consumir();
    }

    private void instruccion() throws BasalException {
        Token t = actual();
        switch (t.tipo) {
            case MOSTRAR  -> instruccionMostrar();
            case IF       -> instruccionIf();     
            case WHILE    -> instruccionWhile();  
            case IDENTIFICADOR -> {
                Token sig = siguiente();
                if (sig.tipo == Token.Tipo.TIPO_GAB) {
                    Token id = consumir(); 
                    consumir();            
                    declararNueva(id, "Gab");
                } else if (sig.tipo == Token.Tipo.TIPO_LIT) {
                    Token id = consumir(); 
                    consumir();            
                    declararNueva(id, "Lit");
                } else if (sig.tipo == Token.Tipo.TIPO_MAR) {
                    Token id = consumir(); 
                    consumir();            
                    declararNueva(id, "Mar");
                } else {
                    asignar();
                }
            }
            case LLAVE_A, LLAVE_C -> consumir(); 
            default -> {
                consumir(); 
            }
        }
    }

    private void instruccionIf() throws BasalException {
        consumir(); // Consume 'si' / 'if'
        
        // Soporte unificado de paréntesis de apertura: por Tipo o por Valor Texto
        if (actual().tipo == Token.Tipo.PARENTESIS_A || actual().valor.equals("(")) {
            consumir(); 
        }
        
        Object condicion = evaluarExpresionLibre(actual().linea);
        
        // Soporte unificado de paréntesis de cierre: por Tipo o por Valor Texto
        if (actual().tipo == Token.Tipo.PARENTESIS_C || actual().valor.equals(")")) {
            consumir(); 
        }
        
        boolean esVerdadero = evaluarBooleano(condicion, actual().linea);
        esperar(Token.Tipo.LLAVE_A); 
        
        if (esVerdadero) {
            while (!actual().tipo.equals(Token.Tipo.LLAVE_C) && !actual().tipo.equals(Token.Tipo.FIN)) {
                instruccion();
            }
            esperar(Token.Tipo.LLAVE_C); 
            
            if (actual().tipo.equals(Token.Tipo.ELSE)) {
                consumir(); 
                esperar(Token.Tipo.LLAVE_A); 
                saltarBloqueLlaves();
            }
        } else {
            saltarBloqueLlaves();
            if (actual().tipo.equals(Token.Tipo.ELSE)) {
                consumir(); 
                esperar(Token.Tipo.LLAVE_A); 
                while (!actual().tipo.equals(Token.Tipo.LLAVE_C) && !actual().tipo.equals(Token.Tipo.FIN)) {
                    instruccion();
                }
                esperar(Token.Tipo.LLAVE_C); 
            }
        }
    }

    private void instruccionWhile() throws BasalException {
        Token tokWhile = actual();
        int posicionCondicion = cursor; 
        
        while (true) {
            cursor = posicionCondicion; 
            consumir(); // Consume 'while'
            
            if (actual().tipo == Token.Tipo.PARENTESIS_A || actual().valor.equals("(")) { 
                consumir(); 
            }
            
            Object condicion = evaluarExpresionLibre(tokWhile.linea);
            
            if (actual().tipo == Token.Tipo.PARENTESIS_C || actual().valor.equals(")")) { 
                consumir(); 
            }
            
            boolean esVerdadero = evaluarBooleano(condicion, tokWhile.linea);
            esperar(Token.Tipo.LLAVE_A); 
            
            if (esVerdadero) {
                while (!actual().tipo.equals(Token.Tipo.LLAVE_C) && !actual().tipo.equals(Token.Tipo.FIN)) {
                    instruccion();
                }
                esperar(Token.Tipo.LLAVE_C); 
            } else {
                saltarBloqueLlaves();
                break; 
            }
        }
    }

    private void saltarBloqueLlaves() throws BasalException {
        int llaves = 1;
        while (llaves > 0 && !actual().tipo.equals(Token.Tipo.FIN)) {
            Token t = consumir();
            if (t.tipo.equals(Token.Tipo.LLAVE_A)) llaves++;
            if (t.tipo.equals(Token.Tipo.LLAVE_C)) llaves--;
        }
    }

    private boolean evaluarBooleano(Object valor, int linea) throws BasalException {
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Gab g) return g.getValor() != 0;
        if (valor instanceof Lit l) return l.getValor().compareTo(BigDecimal.ZERO) != 0;
        if (valor instanceof Mar m) return !m.getValor().replace("\"", "").isEmpty();
        return false;
    }

    private void declararNueva(Token id, String tipo) throws BasalException {
        verificarNoReservada(id);
        esperar(Token.Tipo.RESERVADA_IGUAL); 
        Object valor = evaluarExpresion(tipo, id.linea);
        esperar(Token.Tipo.PUNTO_COMA);
        variables.put(id.valor, valor);
        tiposVars.put(id.valor, tipo);
    }

    private void asignar() throws BasalException {
        Token id = consumir();
        if (!tiposVars.containsKey(id.valor))
            throw new SintaxisException("Variable '" + id.valor + "' no declarada.", id.linea);
        
        esperar(Token.Tipo.RESERVADA_IGUAL);
        String tipo = tiposVars.get(id.valor);
        Object valor = evaluarExpresion(tipo, id.linea);
        esperar(Token.Tipo.PUNTO_COMA);
        variables.put(id.valor, valor);
    }

    private void instruccionMostrar() throws BasalException {
        consumir(); 
        Object val = evaluarExpresionLibre(actual().linea);
        esperar(Token.Tipo.PUNTO_COMA);
        
        String representacion = String.valueOf(val);
        if (representacion.startsWith("\"") && representacion.endsWith("\"")) {
            representacion = representacion.substring(1, representacion.length() - 1);
        }
        salida.append(representacion).append("\n");
    }

    private Object evaluarExpresion(String tipo, int linea) throws BasalException {
        return switch (tipo) {
            case "Gab" -> evaluarGab(linea);
            case "Lit" -> evaluarLit(linea);
            case "Mar" -> evaluarMar(linea);
            default    -> throw new SintaxisException("Tipo desconocido: " + tipo, linea);
        };
    }

    private Object evaluarExpresionLibre(int linea) throws BasalException {
        Object izquierda = evaluarTerminoPrimario(linea);

        Token tOp = actual();
        if (tOp.valor.equals(">") || tOp.valor.equals(">=") ||
            tOp.valor.equals("<") || tOp.valor.equals("<=") ||
            tOp.valor.equals("==") || tOp.valor.equals("!=")) {
            
            Token op = consumir(); 
            Object derecha = evaluarTerminoPrimario(linea);

            return ejecutarComparacion(izquierda, op, derecha, linea);
        }
        return izquierda;
    }

    private Object evaluarTerminoPrimario(int linea) throws BasalException {
        Token t = actual();
        if (t.tipo == Token.Tipo.CADENA) return evaluarMar(linea);
        if (t.tipo == Token.Tipo.NUMERO_DECIMAL) return evaluarLit(linea);
        if (t.tipo == Token.Tipo.NUMERO_ENTERO) return evaluarGab(linea);
        if (t.tipo == Token.Tipo.IDENTIFICADOR) {
            String tipo = tiposVars.get(t.valor);
            if (tipo == null) {
                throw new SintaxisException("Variable '" + t.valor + "' no inicializada o declarada.", t.linea);
            }
            return evaluarExpresion(tipo, linea);
        }
        throw new SintaxisException("Elemento no válido en la expresión: '" + t.valor + "'", t.linea);
    }

    private boolean ejecutarComparacion(Object izq, Token op, Object der, int linea) throws BasalException {
        BigDecimal numIzq = transformarABigDecimal(izq, linea);
        BigDecimal numDer = transformarABigDecimal(der, linea);

        int comp = numIzq.compareTo(numDer);

        return switch (op.valor) {
            case ">"  -> comp > 0;
            case ">=" -> comp >= 0;
            case "<"  -> comp < 0;
            case "<=" -> comp <= 0;
            case "==" -> comp == 0;
            case "!=" -> comp != 0;
            default   -> false;
        };
    }

    private BigDecimal transformarABigDecimal(Object obj, int linea) throws BasalException {
        if (obj instanceof Gab g) return new BigDecimal(g.getValor());
        if (obj instanceof Lit l) return l.getValor();
        if (obj instanceof Integer i) return new BigDecimal(i);
        if (obj instanceof Double d) return new BigDecimal(d);
        throw new SintaxisException("No se pueden realizar operaciones lógicas sobre objetos no numéricos.", linea);
    }

    // ── GAB ────────────────────────────────────────────────
    private Gab evaluarGab(int linea) throws BasalException {
        Gab resultado = termGab(linea);
        while (actual().tipo == Token.Tipo.SUMA || actual().tipo == Token.Tipo.RESTA) {
            Token op = consumir();
            Gab derecha = termGab(linea);
            resultado = (op.tipo == Token.Tipo.SUMA)
                ? Operaciones.sumar(resultado, derecha, op.linea)
                : Operaciones.restar(resultado, derecha, op.linea);
        }
        return resultado;
    }

    private Gab termGab(int linea) throws BasalException {
        Gab resultado = factorGab(linea);
        while (actual().tipo == Token.Tipo.MULT || actual().tipo == Token.Tipo.DIV) {
            Token op = consumir();
            Gab derecha = factorGab(linea);
            if (op.tipo == Token.Tipo.MULT) {
                resultado = Operaciones.multiplication(resultado, derecha, op.linea);
            } else {
                throw new OperacionException("Use Lit para guardar el resultado de una división.", op.linea);
            }
        }
        return resultado;
    }

    private Gab factorGab(int linea) throws BasalException {
        Token t = actual();
        if (t.tipo == Token.Tipo.NUMERO_ENTERO) {
            consumir();
            return Gab.parsear(t.valor, t.linea);
        }
        if (t.tipo == Token.Tipo.IDENTIFICADOR) {
            consumir();
            Object v = obtenerVariable(t);
            if (v instanceof Gab g) return g;
            throw new GabException("'" + t.valor + "' no es de tipo Gab.", t.linea);
        }
        throw new SintaxisException("Se esperaba un valor Gab.", t.linea);
    }

    // ── LIT ────────────────────────────────────────────────
    private Lit evaluarLit(int linea) throws BasalException {
        Lit resultado = termLit(linea);
        while (actual().tipo == Token.Tipo.SUMA || actual().tipo == Token.Tipo.RESTA) {
            Token op = consumir();
            Lit derecha = termLit(linea);
            resultado = (op.tipo == Token.Tipo.SUMA)
                ? Operaciones.sumar(resultado, derecha, op.linea)
                : Operaciones.restar(resultado, derecha, op.linea);
        }
        return resultado;
    }

    private Lit termLit(int linea) throws BasalException {
        Lit resultado = factorLit(linea);
        while (actual().tipo == Token.Tipo.MULT || actual().tipo == Token.Tipo.DIV) {
            Token op = consumir();
            Lit derecha = factorLit(linea);
            resultado = (op.tipo == Token.Tipo.MULT)
                ? Operaciones.multiplicar(resultado, derecha, op.linea)
                : Operaciones.dividir(resultado, derecha, op.linea);
        }
        return resultado;
    }

    private Lit factorLit(int linea) throws BasalException {
        Token t = actual();
        if (t.tipo == Token.Tipo.NUMERO_DECIMAL || t.tipo == Token.Tipo.NUMERO_ENTERO) {
            consumir();
            return Lit.parsear(t.valor, t.linea);
        }
        if (t.tipo == Token.Tipo.IDENTIFICADOR) {
            consumir();
            Object v = obtenerVariable(t);
            if (v instanceof Lit l) return l;
            if (v instanceof Gab g) return new Lit(new BigDecimal(g.getValor()), t.linea);
            throw new LitException("'" + t.valor + "' no es de tipo Lit.", t.linea);
        }
        throw new SintaxisException("Se esperaba un valor Lit.", t.linea);
    }

    // ── MAR ────────────────────────────────────────────────
    private Mar evaluarMar(int linea) throws BasalException {
        Mar resultado = factorMar(linea);
        while (actual().tipo == Token.Tipo.CONCAT) {
            Token op = consumir(); 
            Mar derecha = factorMar(op.linea);
            resultado = Operaciones.concatenar(resultado, derecha, op.linea);
        }
        return resultado;
    }

    private Mar factorMar(int linea) throws BasalException {
        Token t = actual();
        if (t.tipo == Token.Tipo.CADENA) {
            consumir();
            return Mar.parsear(t.valor, t.linea);
        }
        if (t.tipo == Token.Tipo.IDENTIFICADOR) {
            consumir();
            Object v = obtenerVariable(t);
            if (v instanceof Mar m) return m;
            throw new MarException("'" + t.valor + "' no es de tipo Mar.", t.linea);
        }
        throw new SintaxisException("Se esperaba un valor Mar.", t.linea);
    }

    private Object obtenerVariable(Token t) throws SintaxisException {
        if (!variables.containsKey(t.valor))
            throw new SintaxisException("Variable '" + t.valor + "' no declarada.", t.linea);
        return variables.get(t.valor);
    }

    private void verificarNoReservada(Token t) throws SintaxisException {
        Set<String> reservadas = Set.of("gab", "lit", "mar", "mostrar", "if", "si", "else", "sino", "while");
        if (reservadas.contains(t.valor))
            throw new SintaxisException("'" + t.valor + "' es palabra reservada.", t.linea);
    }
}