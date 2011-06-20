template T {
	class A implements java.util.Iterator<Integer> {
		public boolean hasNext() {return true;}
		public Integer next() { return 42;}
		public void remove() { }
	}
	
	class B implements java.lang.Iterable<Integer> {
		public java.util.Iterator<Integer> iterator() { return new A(); }
	}
}

package P {
	inst T;
	class C {
		public static void main(String[] args) {
			java.lang.Iterable<String> b = new B();
			for(String s : b)
			{
				// kommer ikke til å ende godt...
			}
		}
	}
}
