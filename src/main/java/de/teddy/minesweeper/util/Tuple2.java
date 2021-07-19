package de.teddy.minesweeper.util;

public class Tuple2<A, B> {
    private final A a;
    private final B b;

    public Tuple2(A a, B b){
        this.a = a;
        this.b = b;
    }

    public A getA(){
        return a;
    }

    public B getB(){
        return b;
    }

    @Override
    public boolean equals(Object o){
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>)o;

        if(getA() != null ? !getA().equals(tuple2.getA()) : tuple2.getA() != null)
            return false;
        return getB() != null ? getB().equals(tuple2.getB()) : tuple2.getB() == null;
    }

    @Override
    public int hashCode(){
        int result = getA() != null ? getA().hashCode() : 0;
        result = 31 * result + (getB() != null ? getB().hashCode() : 0);
        return result;
    }
}