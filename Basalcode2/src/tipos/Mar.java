package tipos;

import excepciones.BasalException.MarException;

public class Mar {
    private String valor;

    public Mar(String valor, int linea) throws MarException {
        if (valor == null)
            throw new MarException("El valor de Mar no puede ser nulo.", linea);
        this.valor = valor;
    }

    public String getValor() { return valor; }

    public static Mar parsear(String texto, int linea) throws MarException {
        // Espera comillas: "hola"
        String t = texto.trim();
        if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
            return new Mar(t.substring(1, t.length() - 1), linea);
        }
        throw new MarException("El texto '" + texto + "' debe estar entre comillas dobles.", linea);
    }

    @Override
    public String toString() { return valor; }
}