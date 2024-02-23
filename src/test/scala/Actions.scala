
import beguine.actions.Action0
import beguine.actions.Action1
import beguine.runtime.RandomArbitrary
import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec

import scala.util.Random

class ActionTest extends AnyFunSpec with BeforeAndAfter:
  given Random(42)
  val r = RandomArbitrary()

  describe("Action0"):
    it("can be called"):
      val a = Action0("f", () => 99)
      assert(a() == 99)

  describe("Action1"):
    it("can be called with a constant generator"):
      val action = Action1[Int, Int](99)("f", n => n+1)
      val (a, fa) = action()
      assert(a == 99)
      assert(fa == 100)

    it("can be called with a generator explicitly from an Arbitrary"):
      val action = Action1[Int, Int](r.numeric(Some(0), Some(99)))("f", n => n+1)
      for (_ <- 0 to 100) do
        val (a, fa) = action()
        assert(0 <= a && a <= 99)
        assert(1 <= fa && fa <= 100)

