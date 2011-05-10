template T<V extends MyClass> {
  class MyClass { }
}

package P {
  inst T<MySubClass> with MyClass => AnotherName;
  class MySubClass extends AnotherName { }
}
