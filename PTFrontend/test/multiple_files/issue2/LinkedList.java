template LinkedList {
    class List {
        Elem first;
        Elem last;
        void addLast(Elem e) { /* ... */ }
        Elem removeFirst() { return null; }
    }
    
    class Elem {
        Elem nextElem;
        Elem next() { return nextElem; }
    }
}
