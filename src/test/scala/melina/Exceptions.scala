package melina

object Exceptions {
  case class NonMelinaEntity[A](a: A) extends RuntimeException {
    override def getMessage: String =
      f"Got $a, an unexpected class"
  }
}
