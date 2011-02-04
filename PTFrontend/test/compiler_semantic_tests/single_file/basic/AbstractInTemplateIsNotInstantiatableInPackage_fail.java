template T {

    abstract class Test {
        int k;
    }
}

package P  {
    inst T;

    class InstTest {

        void errorHere(){
            Test x = new Test();
        }
    }
}
    
