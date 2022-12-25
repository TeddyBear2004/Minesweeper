package de.teddy.minesweeper.util;

public record Tuple2<A, B>(A a, B b) {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;

        if (a() != null ? !a().equals(tuple2.a()) : tuple2.a() != null)
            return false;
        return b() != null ? b().equals(tuple2.b()) : tuple2.b() == null;
    }

    @Override
    public int hashCode() {
        int result = a() != null ? a().hashCode() : 0;
        result = 31 * result + (b() != null ? b().hashCode() : 0);
        return result;
    }
}