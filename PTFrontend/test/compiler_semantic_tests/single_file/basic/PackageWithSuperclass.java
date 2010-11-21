package SimpleTemplate {

    class A {
        int k;

        A getA() {
            return new A();
        }
    }

    class B extends A {
        int x;

        public static void main(String[] args) {
            System.out.println("hello");
        }
    }

}
