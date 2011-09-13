template T {
	interface I {
		void j();
	}
	
	class A implements I {
		public void j() { }
	}
}

package P {
	inst T with I => J (j() -> k), A => V (j() -> k);
	
	class X {
		public static void main(String[] args) {
			J j = new V();
			j.k();
		}
	}
}
