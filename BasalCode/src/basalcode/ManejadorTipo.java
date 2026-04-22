public interface ManejadorTipo {
    // Método para cuando escribes: gab x ¬ 10 ;
    String evaluarDeclaracion(String nombre, String valor);
    
    // Método para cuando escribes: gab x ¬ 10 + 5 ;
    String evaluarOperacion(String nombre, String val1, String operador, String val2);
}