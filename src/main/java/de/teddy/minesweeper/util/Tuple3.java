package de.teddy.minesweeper.util;

public class Tuple3<A, B, C> extends Tuple2<A, B> {
    private final C c;

    public Tuple3(A a, B b, C c){
        super(a, b);
        this.c = c;
    }

    public C getC(){
        return c;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>)o;

        return getC() != null ? getC().equals(tuple3.getC()) : tuple3.getC() == null;
    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        result = 31 * result + (getC() != null ? getC().hashCode() : 0);
        return result;
    }
}
