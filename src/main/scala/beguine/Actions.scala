package beguine.actions


object Witness {

  trait Call

  case class Call0[Z](f: Action0[Z], ret: Z) extends Call

  case class Call1[A, Z](f: Action1[A, Z], a: A, ret: Z) extends Call

}

abstract class Action(val name: String) extends (() => Witness.Call)

class Action0[Z](override val name: String, val f: () => Z) extends Action(name):

  override def apply(): Witness.Call0[Z] = Witness.Call0(this, f())

  def tee[Y](g: () => Y): Action0[(Z, Y)] = Action0(name, () => (f(), g()))


class Action1[A, Z](agen: => A)(override val name: String, val f: A => Z) extends Action(name):
  override def apply(): Witness.Call1[A, Z] = {
    val a = agen
    Witness.Call1(this, a, f(a))
  }

  def tee[Y](g: A => Y): Action1[A, (Z, Y)] = Action1(agen)(name, a => (f(a), g(a)))

object Action1 {
  def fromF[A, Z](name: String, f: A => Z)(using gena: => A) = Action1[A, Z](gena)(name, f)
}