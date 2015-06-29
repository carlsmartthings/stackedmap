package stackedcollections;

import static org.junit.Assert.*

import org.junit.Test

class StackedSetTest {

  @Test
  public void testGroovyIntegration() {

    // test the constructor / "as"
    Set stacked = ["ball"] as StackedSet
    assert stacked == ["ball"] as TreeSet


    Set setA = ["1", "A", "B"] as HashSet
    Set setB = ["11", "aa", "A", "bb"] as HashSet

    StackedSet ss = new StackedSet()
    ss.push(setA, "setA")
    ss.push(["A"] as Set)
    ss.push(["A"] as Set)
    ss.push([] as Set)
    ss.push(setB)

    assert(ss.contains("B"))
    assert(ss.contains("bb"))

    ss.add("cc")

    assert(ss.contains("cc"))

    ss.getScope("setA").add("dd")

    assert(ss.contains("dd"))

    Set setLit = [
      "11",
      "aa",
      "bb",
      "cc",
      "A",
      "dd",
      "1",
      "B"] as Set


    Set setTree = [
      "11",
      "aa",
      "bb",
      "cc",
      "A",
      "dd",
      "1",
      "B"] as TreeSet

    Set setHash = [
      "11",
      "aa",
      "bb",
      "cc",
      "A",
      "dd",
      "1",
      "B"] as HashSet

    Set empty = [] as Set

    Set addedSet = empty + ss

    // test comparisons and serialization
    println ss
    println addedSet
    println addedSet.getClass().name
    println setLit
    println setTree
    println setHash
    assert (ss == setLit)
    assert (ss == addedSet)
    assert (ss == setTree)
    assert (ss == setHash)
    assert (setTree == setLit)
    assert (setTree == setHash)
    assert (setHash == setLit)
  }

  @Test
  public void testGroovyIntegrationPlusEqual() {
    Set setA = ["1", "A", "B"] as HashSet
    Set setB = ["11", "aa", "A", "bb"] as HashSet

    StackedSet ss = new StackedSet()
    ss.push(setA, "setA")
    ss.push(["A"] as Set)
    ss.push(["A"] as Set)
    ss.push([] as Set)
    ss.push(setB)

    ss += "aaa"

    assert "aaa" in ss
  }
}
