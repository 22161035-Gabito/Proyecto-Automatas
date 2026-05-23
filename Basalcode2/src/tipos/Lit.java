package tipos;

import excepciones.BasalException.LitException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Lit {
    private BigDecimal valor;
    private static final int MAX_ENTEROS = 10;
    private static final int MAX_DECIMALES = 10;

    public Lit(BigDecimal valor, int linea) throws LitException {
        // Validar parte entera y decimal ANTES de cualquier modificación
        validarDigitos(valor, linea);
        
        // SOLO redondear si es necesario (si tiene más de 10 decimales)
        if (tieneMasDeDiezDecimales(valor)) {
            valor = valor.setScale(MAX_DECIMALES, RoundingMode.HALF_UP);
        }
        
        this.valor = valor.stripTrailingZeros(); // Quita ceros innecesarios al final
    }
    
    private boolean tieneMasDeDiezDecimales(BigDecimal valor) {
        String valorStr = valor.toPlainString();
        String[] partes = valorStr.split("\\.");
        if (partes.length > 1) {
            return partes[1].length() > MAX_DECIMALES;
        }
        return false;
    }
    
    private void validarDigitos(BigDecimal valor, int linea) throws LitException {
        String valorStr = valor.toPlainString();
        
        // Separar parte entera y decimal
        String[] partes = valorStr.split("\\.");
        
        String parteEntera = partes[0].replace("-", ""); // Quitar signo negativo si existe
        String parteDecimal = partes.length > 1 ? partes[1] : "";
        
        // Validar parte entera
        if (parteEntera.length() > MAX_ENTEROS) {
            throw new LitException(
                "❌ Número LIT inválido: La parte entera tiene " + parteEntera.length() + 
                " dígitos, pero el máximo permitido es " + MAX_ENTEROS + 
                " dígitos. (Valor: " + valor.toPlainString() + ")",
                linea
            );
        }
        
        // Validar parte decimal (solo si tiene más de 10 dígitos)
        if (parteDecimal.length() > MAX_DECIMALES) {
            throw new LitException(
                "❌ Número LIT inválido: La parte decimal tiene " + parteDecimal.length() + 
                " dígitos, pero el máximo permitido es " + MAX_DECIMALES + 
                " dígitos. (Valor: " + valor.toPlainString() + ")",
                linea
            );
        }
    }

    public BigDecimal getValor() { 
        return valor; 
    }
    
    public int getParteEnteraDigitos() {
        String parteEntera = valor.toBigInteger().toString().replace("-", "");
        return parteEntera.length();
    }
    
    public int getParteDecimalDigitos() {
        String valorStr = valor.toPlainString();
        String[] partes = valorStr.split("\\.");
        return partes.length > 1 ? partes[1].length() : 0;
    }

    public static Lit parsear(String texto, int linea) throws LitException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new LitException("❌ Número LIT inválido: El valor no puede estar vacío.", linea);
        }
        
        try {
            BigDecimal valor = new BigDecimal(texto.trim());
            return new Lit(valor, linea);
        } catch (NumberFormatException e) {
            throw new LitException(
                "❌ Número LIT inválido: '" + texto + "' no es un número decimal válido. " +
                "Formato correcto: [0-9]{1,10}.[0-9]{1,10} (máx. 10 dígitos enteros y 10 decimales)",
                linea
            );
        }
    }
    
    // Método para crear Lit a partir de un double (útil para operaciones aritméticas)
    public static Lit valueOf(double valor, int linea) throws LitException {
        BigDecimal bd = BigDecimal.valueOf(valor);
        return new Lit(bd, linea);
    }
    
    // Método para crear Lit a partir de un int
    public static Lit valueOf(int valor, int linea) throws LitException {
        return new Lit(new BigDecimal(valor), linea);
    }

    @Override
    public String toString() { 
        // Convertir a string sin ceros innecesarios
        String resultado = valor.toPlainString();
        
        // Si termina en .0, quitar el .0
        if (resultado.endsWith(".0")) {
            resultado = resultado.substring(0, resultado.length() - 2);
        }
        
        return resultado;
    }
    
    // Método para comparar dos valores Lit
    public int compareTo(Lit otro) {
        return this.valor.compareTo(otro.valor);
    }
    
    // Método para sumar dos Lit
    public Lit sumar(Lit otro, int linea) throws LitException {
        BigDecimal suma = this.valor.add(otro.valor);
        return new Lit(suma, linea);
    }
    
    // Método para restar dos Lit
    public Lit restar(Lit otro, int linea) throws LitException {
        BigDecimal resta = this.valor.subtract(otro.valor);
        return new Lit(resta, linea);
    }
    
    // Método para multiplicar dos Lit
    public Lit multiplicar(Lit otro, int linea) throws LitException {
        BigDecimal producto = this.valor.multiply(otro.valor);
        return new Lit(producto, linea);
    }
    
    // Método para dividir dos Lit
    public Lit dividir(Lit otro, int linea) throws LitException {
        if (otro.valor.compareTo(BigDecimal.ZERO) == 0) {
            throw new LitException("❌ División por cero en operación con LIT.", linea);
        }
        // Para división, usar la escala máxima para evitar resultados infinitos
        BigDecimal division = this.valor.divide(otro.valor, MAX_DECIMALES, RoundingMode.HALF_UP);
        return new Lit(division, linea);
    }
}