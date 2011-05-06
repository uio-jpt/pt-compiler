template T {
    class A implements java.util.Iterator<Integer> {
        public boolean hasNext() {return false;}
        public Integer next() { return 42;}
        public void remove() { }
    }
}

package P {
    inst T;
}
