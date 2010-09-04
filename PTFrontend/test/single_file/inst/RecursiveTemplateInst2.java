template T1 {
    class A1            { public String m() { return "T1.A1.m()"; } }
    class B1 extends A1 { }
    class C1 extends A1 { public String m() { return "T1.C1.m()"; } }
    class D1 extends C1 { public String m() { return "T1.D1.m()"; } }
}

template T2 {
    inst T1 with A1 => A2, C1 => C2, B1 => B2, D1 => D2;
    class A2 adds { public String m()  { return "T2.A2.m()"; } }
    class B2 adds { public String m()  { return tsuper[T1.B1].m(); /* this should give a compile error
                                                                      because m() is not defined in B1 
                                                                      or in it's adds parent classes. Correct? */
                                       }
                    public String m2() { return tsuper[T1.A1].m(); /* Is this legal? */ }
                  }
    class C2 adds { public String m() { return "T2.C2.m() :: " + tsuper[T1.C1].m(); } }
    class D2 adds { }
}

package P3 {
    inst T2 with A2 => A3, B2 => B3, C2 => C3, D2 => D3;
    class A3 adds { 
        //public String m() { return tsuper[T1.A1].m(); /* should be illegal because template is not visible */ } 
    }
    class B3 adds { }
    class C3 adds { public String m() { return tsuper[T2.C2].m(); } }
    class D3 adds { public String x() { return m(); } /* Should return T1.D1.m() */ }
}

