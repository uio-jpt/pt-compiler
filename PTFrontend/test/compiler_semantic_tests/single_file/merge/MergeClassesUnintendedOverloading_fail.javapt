template T {
    class A1 {
	int m() { return 0; }
    }
    
    class B1 extends A1 {
	int n() { return 0; }
    }

    class A2 {
	int n() { return 0; }
    }

    class B2 extends A2 {
	int m() { return 0; }
    }
}


package P {
    inst T with A1 => A3, A2 => A3; 
}