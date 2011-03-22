template AlphaTemplate {
    interface Alpha {
        public abstract String getString();
    }
}

template BetaTemplate {
    inst AlphaTemplate with Alpha => Beta;
}

template GammaTemplate {
    inst BetaTemplate with Beta => Gamma;
}

template DeltaTemplate {
    inst GammaTemplate with Gamma => Delta;
}

package TestPackage {
    inst DeltaTemplate with Delta => X;

    class MyX implements X {
        public String getString() { return "hello world"; }
    }
}
