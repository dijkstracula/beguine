package beguine

sealed trait Error extends Throwable

case class ConjectureFailure(conj: String, file: String, lineno: Integer) extends Error

case class GeneratorLivelock() extends Error

case class RetryValueGeneration() extends Error
