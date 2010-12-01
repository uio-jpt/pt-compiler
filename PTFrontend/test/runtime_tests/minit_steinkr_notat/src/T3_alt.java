template T3_alt {
    inst T1 with
        B => U;
    inst T2 with
        C => U,
        D => V;
    
    class U adds {
        U () { 
            System.out.println("T3.U");
        }
    }
    class V adds {
        V () {
            System.out.println("T3.V");
        }
    }
    class W extends V {
        W () {
            System.out.println("T3.W");
        }
    }
}
