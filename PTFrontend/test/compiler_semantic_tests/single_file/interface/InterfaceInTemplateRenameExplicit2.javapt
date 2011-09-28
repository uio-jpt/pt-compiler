template T {
	interface I {
		void j(int a, java.lang.Runnable b, Integer c);
	}

    class B {
    }
	
	class A implements I {
		public void j(int a, java.lang.Runnable b, Integer c) { }
	}
}

package P {
	inst T with I => J (j(int,Runnable,Integer) -> k), A => V (j(int,Runnable,Integer) -> k), B => Y;
	
	class X {
		public static void main(String[] args) {
			J j = new V();
		}
	}
}
