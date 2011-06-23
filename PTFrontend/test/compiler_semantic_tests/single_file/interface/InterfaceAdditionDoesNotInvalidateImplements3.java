template T {
	interface I { }

    class X implements I {
    }
}

package P {
	inst T with I => J, X => V;

	interface J adds { void f(); }
    class V adds { public void f() {} }
}
