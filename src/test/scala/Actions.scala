
import beguine.actions.{Action0, Action1, Witness}
import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class ActionTest extends AnyFunSpec with BeforeAndAfter:
  given Random(42)
  val r = RandomArbitrary()

  describe("Action0"):
    it("can be called"):
      val action = Action0("f", () => 99)
      val Witness.Call0(f, z) = action()
      assert(f == action)
      assert(z == 99)

    it("can be teed"):
      val a1 = Action0("f", () => 99)
      val a2 = Action0("g", () => 101)
      val teed = a1.tee(a2.f)
      val Witness.Call0(_, (a, b)) = teed()
      assert(a == 99)
      assert(b == 101)


  describe("Action1"):
    it("can be called with a constant generator"):
      val action = Action1[Int, Int](99)("f", n => n+1)
      val Witness.Call1(f, a, z) = action()
      assert(f == action)
      assert(a == 99)
      assert(z == 100)

    it("can be called with a generator explicitly from an Arbitrary"):
      val action = Action1[Int, Int](r.numeric(0, 99))("f", n => n+1)

      val seen = scala.collection.mutable.Set[Int]()
      for (_ <- 0 to 100) do
        val Witness.Call1(f, a, z) = action()
        seen.add(a)
        assert(f == action)
        assert(0 <= a && a <= 99)
        assert(1 <= z && z <= 100)

      // Confirm that we are passing our generator by name so we generate different values
      // each time we evaluate it.
      assert(seen.size > 1)