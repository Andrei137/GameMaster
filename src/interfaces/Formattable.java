package interfaces;

public interface Formattable {
    default String format() {
        return this.toString();
    }
}
