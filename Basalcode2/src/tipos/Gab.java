package tipos;


import excepciones.BasalException.GabException;

public class Gab {
    private long valor;
    private static final long MAX = 9999999999L;
    private static final long MIN = -9999999999L;

    public Gab(long valor, int linea) throws GabException {
        if (valor > MAX || valor < MIN)
            throw new GabException(
                "El valor " + valor + " excede los 10 dígitos permitidos.", linea);
        this.valor = valor;
    }

    public long getValor() { return valor; }

    public static Gab parsear(String texto, int linea) throws GabException {
        try {
            long v = Long.parseLong(texto.trim());
            return new Gab(v, linea);
        } catch (NumberFormatException e) {
            throw new GabException("'" + texto + "' no es un entero válido.", linea);
        }
    }

    @Override
    public String toString() { return String.valueOf(valor); }
}
