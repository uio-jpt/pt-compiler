package merge_simple {
    inst T with TA => X(x->ta_x),
        TX => X(x->tx_x,f()->ff);

    class X adds {
        int m(){
            return tsuper[TA].m() + tsuper[TX].m();
        }

        public static void main(String[] args) {
            X x = new X();
            System.out.println("x.m(): " + x.m());
        }
    }
}
