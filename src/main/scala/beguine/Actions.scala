package beguine.actions



object Witness {
  case class Call0[Z](f: Action0[Z], ret: Z)
  case class Call1[A, Z](f: Action1[A, Z], a: A, ret: Z)
}

class Action0[Z](val name: String, val f: () => Z) extends (() => Witness.Call0[Z]):

  override def apply() = Witness.Call0(this, f())

  def tee[Y](g: () => Y): Action0[(Z, Y)] = Action0(name, () => (f(), g()))


class Action1[A, Z](agen: => A)(val name: String, val f: A => Z) extends (() => Witness.Call1[A,Z]):
  override def apply(): Witness.Call1[A, Z] = {
    val a = agen
    Witness.Call1(this, a, f(a))
  }

  def tee[Y](g: A => Y): Action1[A, (Z, Y)] = Action1(agen)(name, a => (f(a), g(a)))
