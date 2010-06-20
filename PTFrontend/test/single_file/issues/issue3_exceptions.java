/*
 http://github.com/eivindgl/pt-compiler/issues#issue/3
 ant testsingle -Dname=test/single_file/issues/issue3_exceptions.java

 Det var ikke exception som feiler, men bruk av System.out (!).
 */

template TestExceptions {
    class Foo  {
        public void bar() {
            throw new RuntimeException("Hello");
        }
    }
}

