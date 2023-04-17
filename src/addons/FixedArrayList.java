package addons;

import java.util.Iterator;
import java.util.function.Consumer;

public class FixedArrayList implements Iterable<Integer>{
    public int[] list;
    public short size;
    private final short maxIndex;

    public FixedArrayList(){
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
