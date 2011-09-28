template T1 {
    class A1 {
		int m() { return 0; }
    }
    
    class B1 extends A1 {
		int n() { return 0; }
    }
}

template T2 {
	inst T1;

    class C2 {
		int n() { return 0; }
    }

    class D2 extends C2 {
		int m() { return 0; }
    }
}


template T3 {
    inst T1 with A1 => A3, B1 => B3; 
	inst T2 with C2 => C3, D2 => D3; 
}

package P {
	inst T3 with
		A3 => A4, B3 => B4,
		C3 => A4, D3 => B4;
}
