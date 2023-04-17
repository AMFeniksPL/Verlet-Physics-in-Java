package addons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;

public class CollisionList implements Iterable<Integer>{
    public int[] list;
    public int size;
    private final int maxIndex;

    public CollisionList(){
        maxIndex = 4;
        size = 0;
        list = new int[maxIndex];
    }

    public int size(){
        return size;
    }

    public void add(int element){
        if (size < maxIndex) {
            list[size] = element;
            size++;
        }
    }

    public void clear(){
        size = 0;
    }


    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int index = 0;

            public boolean hasNext() {
                return index < size;
            }

            public Integer next() {
                return list[index++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void forEach(Consumer action) {
        Iterable.super.forEach(action);
    }

}
