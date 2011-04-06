template T {
    enum AnyOldEnumName {
		FOO, BAR, KAKE, RISKOKER;
    }
}

package P  {
    inst T;

    class V {
        public static void main(String args[]) {
            AnyOldEnumName y = AnyOldEnumName.KAKE;
        }
    }

}
    
