template T<V extends MyInterface> {
  interface MyInterface {
      String getString();
  }
}

package P {
  inst T<MyImplementing> with MyInterface => AnotherName;
  class MyImplementing implements AnotherName {
      public String getString() {
          return "Hello world!";
      }
  }
}
