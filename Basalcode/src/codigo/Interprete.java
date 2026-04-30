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

    private final Map<String, Object>  variables  = new HashMap<>();
    private final Map<String, String>  tiposVars  = new HashMap<>(); // nombre -> "Gab"|"Lit"|"Mar"
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
            while (!actual().tipo.equals(Token.Tipo.FIN)) {
                instruccion();
            }
        } catch (BasalException e) {
            salida.append("\n❌ ").append(e.getMessage());
        }
        return salida.toString();
    }

    // ── NAVEGACIÓN ─────────────────────────────────────────
    private Token actual() { return tokens.get(cursor); }
    private Token consumir() { return tokens.get(cursor++); }

    private Token esperar(Token.Tipo tipo) throws SintaxisException {
        Token t = actual();
        if (!t.tipo.equals(tipo))
            throw new SintaxisException(
                "Se esperaba " + tipo + " pero se encontró '" + t.valor + "'", t.linea);
        return consumir();
    }

    // ── INSTRUCCIONES ──────────────────────────────────────
    private void instruccion() throws BasalException {
        Token t = actual();
        switch (t.tipo) {
            case TIPO_GAB -> declarar("Gab");
            case TIPO_LIT -> declarar("Lit");
            case TIPO_MAR -> declarar("Mar");
            case MOSTRAR  -> instruccionMostrar();
            case IDENTIFICADOR -> asignar();
            default -> throw new SintaxisException(
                "Instrucción inesperada: '" + t.valor + "'", t.linea);
        }
    }

    // ── DECLARACIÓN ────────────────────────────────────────
    private void declarar(String tipo) throws BasalException {
        consumir(); // consume Gab/Lit/Mar
        Token id = esperar(Token.Tipo.IDENTIFICADOR);
        verificarNoReservada(id);
        esperar(Token.Tipo.ASIGNACION);
        Object valor = evaluarExpresion(tipo, id.linea);
        esperar(Token.Tipo.PUNTO_COMA);
        variables.put(id.valor, valor);
        tiposVars.put(id.valor, tipo);
    }

    // ── ASIGNACIÓN ─────────────────────────────────────────
    private void asignar() throws BasalException {
        Token id = consumir();
        if (!tiposVars.containsKey(id.valor))
            throw new SintaxisException(
                "Variable '" + id.valor + "' no declarada.", id.linea);
        esperar(Token.Tipo.ASIGNACION);
        String tipo = tiposVars.get(id.valor);
        Object valor = evaluarExpresion(tipo, id.linea);
        esperar(Token.Tipo.PUNTO_COMA);
        variables.put(id.valor, valor);
    }

    // ── MOSTRAR ────────────────────────────────────────────
    private void instruccionMostrar() throws BasalException {
        Token tk = consumir(); // consume 'mostrar'
        Object val = evaluarExpresionLibre(tk.linea);
        esperar(Token.Tipo.PUNTO_COMA);
        salida.append(val).append("\n");
    }

    // ── EVALUACIÓN DE EXPRESIONES ──────────────────────────
    private Object evaluarExpresion(String tipo, int linea) throws BasalException {
        return switch (tipo) {
            case "Gab" -> evaluarGab(linea);
            case "Lit" -> evaluarLit(linea);
            case "Mar" -> evaluarMar(linea);
            default    -> throw new SintaxisException("Tipo desconocido: " + tipo, linea);
        };
    }

    // Para 'mostrar' sin tipo explícito
    private Object evaluarExpresionLibre(int linea) throws BasalException {
        Token t = actual();
        if (t.tipo == Token.Tipo.CADENA) return evaluarMar(linea);
        if (t.tipo == Token.Tipo.NUMERO_DECIMAL) return evaluarLit(linea);
        if (t.tipo == Token.Tipo.NUMERO_ENTERO) return evaluarGab(linea);
        if (t.tipo == Token.Tipo.IDENTIFICADOR) {
            String tipo = tiposVars.get(t.valor);
            if (tipo == null)
                throw new SintaxisException("Variable '" + t.valor + "' no declarada.", t.linea);
            return evaluarExpresion(tipo, linea);
        }
        throw new SintaxisException("Expresión inválida.", t.linea);
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
            if (op.tipo == Token.Tipo.MULT)
                resultado = Operaciones.multiplicar(resultado, derecha, op.linea);
            else {
                // División Gab/Gab devuelve Lit, no aplica en contexto Gab
                throw new OperacionException(
                    "Use Lit para guardar el resultado de una división.", op.linea);
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
        throw new SintaxisException("Se esperaba un valor Mar (cadena entre comillas).", t.linea);
    }

    // ── UTILIDADES ─────────────────────────────────────────
    private Object obtenerVariable(Token t) throws SintaxisException {
        if (!variables.containsKey(t.valor))
            throw new SintaxisException("Variable '" + t.valor + "' no declarada.", t.linea);
        return variables.get(t.valor);
    }

    private void verificarNoReservada(Token t) throws SintaxisException {
        Set<String> reservadas = Set.of("Gab", "Lit", "Mar", "mostrar");
        if (reservadas.contains(t.valor))
            throw new SintaxisException(
                "'" + t.valor + "' es una palabra reservada de BasalCode.", t.linea);
    }
}