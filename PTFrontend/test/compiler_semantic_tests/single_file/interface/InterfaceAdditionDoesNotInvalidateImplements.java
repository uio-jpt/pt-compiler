template T {
	interface I { }

    class X implements I {
        public void f() {}
    }
}

package P {
	inst T;
	interface I adds { void f(); }
}
