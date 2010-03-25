template TemplateTest1 {
    class T { // rename ok
        double ff;
        T() { int vital_code = 0xfeed; }
        T(float o) { }
        T(double d) { }
    }
}

package PackageTest1 {
    inst TemplateTest1 with T => F;
    /* adds merge kriterier::
     * 0. overskrive alt.
     * 1. bare en tom konstruktur
     *
     */
    class F adds {
        F(int i) { }
        F(double ny_d) { }
        int ff;
    }
}

/* gir
 class F {
   int ff; // shadows double ff

   F() { // non-empty arv fra T
     super();
     int vital_code = 0xfeed;
   }

   F(float o) { // arv fra T
     super();
   }

   F(int i) {  // direkte fra F adds
     super();
   }

   F(double d) { // override fra F adds
     super();
   }
 }
 */


