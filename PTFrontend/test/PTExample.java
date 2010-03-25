template TemplateTest1 {
    class T { // rename ok
        T temp; // ok
        public T a() { // ok
            T nytemp;
            System.out.println("TEST");
            double x = ff+1.0; // feil
            return null;
        }
        double ff;
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
        int ff;
    }
}


