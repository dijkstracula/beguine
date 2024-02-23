package beguine.actions

class Action0[U](val name: String, val f: () => U) extends (() => U):

  override def apply(): U = f()


class Action1[A, Z](agen: => A)(val name: String, val f: A => Z) extends (() => (A, Z)):
  override def apply(): (A, Z) = {
    val a = agen
    (a, f(a))
  }

  def tee(g: A => Z): Action1[A, (Z, Z)] = Action1(agen)(name, a => (f(a), g(a)))
