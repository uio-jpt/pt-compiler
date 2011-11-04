template T {
    required type Foo {
        String f();
    } 

    class X {
        public void g(Foo f) {
            System.out.println(f.f());
        }
    }
}

package P {
    inst T with  Foo <= Bar;

    class Bar {
        String f() { return "Traditional Bar"; }
    }

    class Main {
        public static void main(String args[]) {
            X x = new X();
            Bar b = new Bar();
            x.g(b);

            /*
           	   Her er alt ihvertfall i skjønneste orden siden det er etablert at det er
               Har som har tatt over for Foo, og dette nå faktisk er en ordentlig klasse
            */
            Bar bb = new Bar() { String f() { return "Anonymous Bar"; } };
            x.g(bb);

        }
    }
}
