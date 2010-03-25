template TemplateTest1 {
    class T {
        T temp;
        public T a() {
            T nytemp;
            System.out.println("TEST");
            return null;
        }
    }
}

package PackageTest1 {
    inst TemplateTest1 with T => F;
    //inst TemplateTest1;
    // T => F, Q => Q;
/*
    class F {
	int ff;
        public F a() {
            System.out.println("TEST");
		return null;
        }
    }
    class Q {
        public void blah() {
            System.out.println("TEST");
        }
    }
*/
    /*
    class F adds {
        int ff;
    }
    */
}


