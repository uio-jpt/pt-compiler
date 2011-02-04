package SparseMatrix { 
    inst LinkedList with 
       List => ListOfLists, 
       Elem => ElemList; 
    inst LinkedList with List => ElemList, Elem => Person;
    
    class ListOfLists adds{ /* ... */ } 
    class ElemList adds{ /* ... */ }

    class Person adds { /* ... */ }
}
