package stackedcollections;

import static org.junit.Assert.*

import org.junit.Test

class StackedMapTest {

  @Test
  public void testGroovy() {
    Map one = ["a":"AA","b":"BB"]
    Map two = ["1":"aaa", "2":"aaa"]
    Map three = ["1":"baa", "2":"baa"]
    Map four = ["1":"aaz"]

    StackedMap sm = new StackedMap(one);
    sm.push(two)
    assert(sm["1"] == "aaa")
    sm.push(three)
    assert(sm["1"] == "baa")
    sm.push(four)
    assert(sm["1"] == "aaz")
    assert(sm.keySet() == ["a", "b", "1", "2"] as Set)

    assert( sm == ["a":"AA","b":"BB","1":"aaz","2":"baa"])

    sm["z"] = "ZZ"

    assert( sm == ["a":"AA","b":"BB","1":"aaz","2":"baa","z":"ZZ"])

    sm.pop()
    assert( sm == ["a":"AA","b":"BB","1":"baa","2":"baa"])
  }
}
