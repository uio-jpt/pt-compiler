template TemplateTest1 {
    class T { // rename ok
        double ff;

        T() { int vital_code = 0xfeed; }
        T(float o) { }
        /*        
        T(double d) { }
        class C { double will_be_shadowed; }
        C cmemb_will_be_kept;
        public int minMetode() { return 99999; }
        public int minMetode(int x) { return x+99999; }
        public int minMetode(double x) { return 0xface; }
        public static int minMetode(int x) { return x +4569999999; }
        */
    }
    class Q {
        public T getT() { return null; }
    }//java.util.LinkedList {}    
}

package PackageTest1 {
    // error if next comment removed. dunno why. haven't looked closely.
    inst TemplateTest1 with T => F;
//     adds merge kriterier::
//     0. overskrive alt.
//     1. bare en tom konstruktur


     class F adds {
         F(int i) { }
         F() { }
         C cmemb;
         class C { int override; }
         public int minMetode(int x) { return x +123; }
         // static og vanlig med lik signatur er heller ikke lov i
         // vanlig java.
         // public static int minMetode(int x) { return x +456; }
         int ff;         
         int kjh;
     }
 }
