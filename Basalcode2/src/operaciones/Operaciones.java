package operaciones;


import excepciones.BasalException.*;
import tipos.*;
import java.math.BigDecimal;

public class Operaciones {

    // ── GAB (enteros) 
    public static Gab sumar(Gab a, Gab b, int linea) throws GabException, OperacionException {
        try {
            return new Gab(a.getValor() + b.getValor(), linea);
        } catch (GabException e) {
            throw new OperacionException("Resultado fuera de rango Gab en suma.", linea);
        }
    }

    public static Gab restar(Gab a, Gab b, int linea) throws GabException, OperacionException {
        try {
            return new Gab(a.getValor() - b.getValor(), linea);
        } catch (GabException e) {
            throw new OperacionException("Resultado fuera de rango Gab en resta.", linea);
        }
    }

    public static Gab multiplicar(Gab a, Gab b, int linea) throws GabException, OperacionException {
        try {
            return new Gab(a.getValor() * b.getValor(), linea);
        } catch (GabException e) {
            throw new OperacionException("Resultado fuera de rango Gab en multiplicación.", linea);
        }
    }

    public static Lit dividir(Gab a, Gab b, int linea) throws OperacionException, LitException {
        if (b.getValor() == 0)
            throw new OperacionException("División entre cero no permitida.", linea);
        return new Lit(new BigDecimal(a.getValor())
                .divide(new BigDecimal(b.getValor()), 8, java.math.RoundingMode.HALF_UP), linea);
    }

    // ── LIT (decimales) 
    public static Lit sumar(Lit a, Lit b, int linea) throws LitException {
        return new Lit(a.getValor().add(b.getValor()), linea);
    }

    public static Lit restar(Lit a, Lit b, int linea) throws LitException {
        return new Lit(a.getValor().subtract(b.getValor()), linea);
    }

    public static Lit multiplicar(Lit a, Lit b, int linea) throws LitException {
        return new Lit(a.getValor().multiply(b.getValor()), linea);
    }

    public static Lit dividir(Lit a, Lit b, int linea) throws OperacionException, LitException {
        if (b.getValor().compareTo(BigDecimal.ZERO) == 0)
            throw new OperacionException("División entre cero no permitida.", linea);
        return new Lit(a.getValor().divide(b.getValor(), 8, java.math.RoundingMode.HALF_UP), linea);
    }

    // ── MAR (strings) 
    public static Mar concatenar(Mar a, Mar b, int linea) throws MarException {
        return new Mar(a.getValor() + b.getValor(), linea);
    }
}
