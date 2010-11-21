template T {

    class TA {
        int x;

        TA(){
            x = 5;
        }
        
        int m () {
            return x;
        }
    }

    class TX {
        int x;

        TX(){
            x = 7;
        }
        
        int m () {
            return f();
        }
        int f() {
            return x;
        }
    }
}
