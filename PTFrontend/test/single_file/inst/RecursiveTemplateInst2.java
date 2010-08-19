template T1 {
    class A1            { public String m() { return "T1.A1.m()"; } }
    class B1 extends A1 { }
    class C1 extends A1 { }
    class D1 extends C1 { public String m() { return "T1.D1.m()"; } }
}

template T2 {
    inst T1 with A1 => A2, C1 => C2, B1 => B2, D1 => D2;
    class A2 adds { public String m()  { return "T2.A2.m()"; } }
    class B2 adds { public String m()  { return tsuper[T1.B1].m(); /* this should return T1.A1.m(), right? Needs to be fixed. */ } 
                    public String m2() { return tsuper[T1.A1].m(); /* is this legal? */ }
                  }
    class C2 adds { }
    class D2 adds { }
}

package P3 {
    inst T2 with A2 => A3, B2 => B3, C2 => C3, D2 => D3;
    class A3 adds { public String m() { return tsuper[T1.A1].m(); /* should be illegal */ } }
    class B3 adds { }
    class C3 adds { }
    class D3 adds { }
}

