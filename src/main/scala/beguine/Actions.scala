package beguine.actions


object Witness {

  trait Call

  case class Call0[Z](f: Action0[Z], ret: Z) extends Call

  case class Call1[A, Z](f: Action1[A, Z], a: A, ret: Z) extends Call
  case class Call2[A, B, Z](f: Action2[A, B, Z], a: A, b: B, ret: Z) extends Call
  case class Call3[A, B, C, Z](f: Action3[A, B, C, Z], a: A, b: B, c: C, ret: Z) extends Call
}

abstract class Action(val name: String) extends (() => Witness.Call)

case class Action0[Z](override val name: String, f: () => Z) extends Action(name) {

  override def apply(): Witness.Call0[Z] = Witness.Call0(this, f())

  def tee[Y](g: () => Y): Action0[(Z, Y)] = Action0(name, () => (f(), g()))
}

object Action {
  /*
  def TryTee(l: Action, r: Action) = (l, r) match {
    case (l @ Action0(lname, lf), Action0(rname, rf)) => Some(l.tee(rf))
    case (l @ Action1(zfroma, lname, lf), Action1(yfroma, rname, rf)) => Some(l.tee(rf))
  }

   */
}

case class Action1[A, Z](agen: () => A)(override val name: String, val f: A => Z) extends Action(name) {
  override def apply(): Witness.Call1[A, Z] = {
    val a = agen()
    Witness.Call1(this, a, f(a))
  }
  def tee[Y](g: A => Y): Action1[A, (Z, Y)] = Action1(agen)(name, a => (f(a), g(a)))
}

object Action1 {
  //def fromF[A, Z](name: String, f: A => Z)(using gena: => A) = Action1[A, Z](gena)(name, f)
  def unapply[A, Z](self: Action1[A,Z]): Option[(() => A, String, A => Z)] = Some(self.agen, self.name, self.f)
}

case class Action2[A, B, Z](agen: () => A, bgen: () => B)(override val name: String, val f: (A, B) => Z) extends Action(name) {
  override def apply(): Witness.Call2[A, B, Z] = {
    val a = agen()
    val b = bgen()
    Witness.Call2(this, a, b, f(a,b))
  }
  def tee[Y](g: (A, B) => Y): Action2[A, B, (Z, Y)] = Action2(agen, bgen)(name, (a, b) => (f(a, b), g(a, b)))
}

object Action2 {
  //def fromF[A, Z](name: String, f: A => Z)(using gena: => A) = Action1[A, Z](gena)(name, f)
  def unapply[A, B, Z](self: Action2[A, B, Z]): Option[(() => A, () => B, String, (A, B) => Z)] = Some(self.agen, self.bgen, self.name, self.f)
}

case class Action3[A, B, C, Z](agen: () => A, bgen: () => B, cgen: () => C)(override val name: String, val f: (A, B, C) => Z) extends Action(name) {
  override def apply(): Witness.Call3[A, B, C, Z] = {
    val a = agen()
    val b = bgen()
    val c = cgen()
    Witness.Call3(this, a, b, c, f(a,b,c))
  }
  def tee[Y](g: (A, B, C) => Y): Action3[A, B, C, (Z, Y)] = Action3(agen, bgen, cgen)(name, (a, b, c) => (f(a, b, c), g(a, b, c)))
}

object Action3 {
  //def fromF[A, Z](name: String, f: A => Z)(using gena: => A) = Action1[A, Z](gena)(name, f)
  def unapply[A, B, C, Z](self: Action3[A, B, C, Z]): Option[(() => A, () => B, () => C, String, (A, B, C) => Z)] = Some(self.agen, self.bgen, self.cgen, self.name, self.f)
}
