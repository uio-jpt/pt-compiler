template TemplateTest1 {
    class T {
        public T a() {
            System.out.println("TEST");
		return null;
        }
    }
    class Q {
        public void blah() {
            System.out.println("TEST");
        }
    }
}

package PackageTest1 {
    inst TemplateTest1 with T => F;
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
    class F adds {
	int ff;
    }

}


