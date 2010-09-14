template T1 {

    class B  {
        int b;
    }
}

package T3 {
    inst T1 with B => U;
    
    class V extends U {
        int v;
    }
}
