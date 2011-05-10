template T<V extends MyClass> {
  class MyClass { }
}

package P {
  inst T<MySubClass>;
  class MySubClass extends MyClass { }
}
