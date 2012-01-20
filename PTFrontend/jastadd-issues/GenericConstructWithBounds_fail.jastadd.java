import java.util.*;

class Animal { } 
class Dog extends Animal { }
class Poodle extends Dog { }

class Cat extends Animal { } 

class GenericSuper {

        public static void main (String [] args) {
            List<? super Dog> l;

            l = new LinkedList<Dog>();
            l.add(new Dog());
            l.add(new Poodle());

            l = new LinkedList<Animal>();
            //l.add(new Animal());           // ulovlig pga typeskranken til l
            l.add(new Dog());
            l.add(new Poodle());

            l = new LinkedList<Object>();
            //l.add(new Object());           // ulovlig pga typeskranken til l
            //l.add(new Animal());           // ulovlig pga typeskranken til l
            l.add(new Dog());
            l.add(new Poodle());

            //l = new LinkedList<Cat>();     // ulovlig pga typeskranken til l

            // illegal:
            l = new LinkedList<? super Dog>();
            l.add(new Dog());
            l.add(new Poodle());
            // (detected by javac but not jastadd-checker --> Somebody Else's Problem)
        }
        
    }

