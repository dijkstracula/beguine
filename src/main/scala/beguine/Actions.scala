package beguine.actions

class Action0[Z](val name: String, val f: () => Z) extends (() => Z):

  override def apply(): Z = f()

  def tee[Y](g: () => Y): Action0[(Z, Y)] = Action0(name, () => (f(), g()))


class Action1[A, Z](agen: => A)(val name: String, val f: A => Z) extends (() => (A, Z)):
  override def apply(): (A, Z) = {
    val a = agen
    (a, f(a))
  }

  def tee[Y](g: A => Y): Action1[A, (Z, Y)] = Action1(agen)(name, a => (f(a), g(a)))
