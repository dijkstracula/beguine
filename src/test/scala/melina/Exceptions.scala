package melina

object Exceptions {
  case class NonMelinaEntity[A](a: A, expected: Class[_]) extends RuntimeException {
    override def getMessage: String =
      f"Expected class $expected but got $a, an instance of ${a.getClass}"
  }
}
