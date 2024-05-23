import beguine.sorts
import beguine.Maps
import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class MapTest extends AnyFunSpec with BeforeAndAfter {
  describe ("Map1") {
   it("an empty map can be iterated through") {
     val sort = sorts.Number(0, 10)
     val m = new Maps.Map1(sort)(() => 42)
  m
     assert(m.iterator.toMap.size == sort.inhabitants.get.size)
     // This would likely involve a logical variable (i.e. `m(X) == 42`),
     m.iterator collect { case (_x, fx) => assert(fx == 42) }
   }

    it("point values can be accessed, or fall back to a default") {
      val sort = sorts.Number(0, 10)
      val m = new Maps.Map1(sort)(() => 42)

      assert(m(4) == 42)

      m(2) = 1
      assert(m(2) == 1)

      // This would be a bad, but valid, way to make a point query (i.e. `m(2) == 1`),
      m.iterator collect { case (2, m2) => assert(m2 == 1) }
      m.iterator collect { case (3, m3) => assert(m3 == 42) }
    }

    it("falls back to default values for infinite sort cardinalities") {
      val sort = sorts.Uninterpreted()
      val m = new Maps.Map1(sort)(() => 42)

      assert(m(4) == 42)
      assert(m.iterator.toMap.size == 0)

      m(2) = 1
      assert(m.iterator.toMap.size == 1)
    }
  }

  describe("Map2") {
    it("an empty Map2 can be iterated through") {
      val s1 = sorts.Number(0, 10)
      val s2 = sorts.Number(0, 3)
      val m = new Maps.Map2(s1, s2)(() => 42)

      assert(m.iterator.toMap.size == s1.inhabitants.get.size * s2.inhabitants.get.size)
      // This would likely involve a logical variable (i.e. `m(X, Y) == 42`),
      m.iterator collect { case ((_x, _y), fxy) => assert(fxy == 42) }
    }

    it("point values can be accessed, or fall back to a default") {
      val sort = sorts.Number(0, 10)
      val m = new Maps.Map1(sort)(() => 42)

      assert(m(4) == 42)

      m(2) = 1
      assert(m(2) == 1)

      // This would be a bad, but valid, way to make a point query (i.e. `m(2) == 1`),
      m.iterator collect { case (2, m2) => assert(m2 == 1) }
      m.iterator collect { case (3, m3) => assert(m3 == 42) }
    }
  }
}
