template T {
	interface I {
		void j(int a, java.lang.Runnable b, B c);
	}

    class B {
    }

    class C {
        // this tests the ability to distinguish between "B" and "C".
        // in the bad old days lots of things got reduced to "Unknown"
    }
	
	class A implements I {
		public void j(int a, java.lang.Runnable b, B c) { }
	}
}

package P {
	inst T with I => J (j(int,Runnable,B) -> k), A => V (j(int,Runnable,B) -> k), B => Y;
	
	class X {
		public static void main(String[] args) {
			J j = new V();
		}
	}
}
