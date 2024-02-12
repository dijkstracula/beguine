package beguine.runtime

import beguine.{ConjectureFailure, Error, GeneratorLivelock}

import scala.collection.mutable
import java.lang.Runnable
import java.util.function.BooleanSupplier
import scala.util.{Failure, Success, Try}

case class ActionCall(name: String, args: List[Any], ret: Option[Any])

abstract class Protocol(a: Arbitrary):
  private val conjectures: mutable.ArrayBuffer[() => Option[ConjectureFailure]] = mutable.ArrayBuffer.empty
  private val exportedActions: mutable.ArrayBuffer[() => Unit] = mutable.ArrayBuffer.empty
  private val history: mutable.ArrayBuffer[ActionCall] = mutable.ArrayBuffer.empty

  def apply(): Either[Error, Unit] =
      a.collection(exportedActions.toSeq) match {
        case None => Left(GeneratorLivelock())
        case Some(f) =>
          Try(f()) match {
            case Success(()) => ()
            case Failure(failed: ConjectureFailure) => return Left(failed)
            case Failure(e) => throw e
          }
          conjectures.collectFirst(conj => conj()) match {
            case None | Some(None) => Right(())
            case Some(Some(failed)) => Left(failed)
          }
      }

  def getHistory = history.toSeq

  // Conjecture registration

  def conjectured(name: String, file: String, lineno: Int, conj: BooleanSupplier) =
    conjectures.append(() => {
      if conj.getAsBoolean() then
        None
      else
        Some(ConjectureFailure(file, name, lineno))
    })

  // Exported action registration

  def exported[Z](name: String, f: Runnable): Unit =
    exportedActions.append(() => {
      println(name)
      history.append(ActionCall(name, List.empty, Option(f.run())))
      ()
    })


  def exported[A, Z](name: String, f: A => Z, gena: => A): Unit =
    exportedActions.append(() => {
      val a = gena
      val z = f(gena)
      history.append(ActionCall(name, List(a), Option(z)))
      ()
    })

  def exported[A, B, Z](name: String, f: (A, B) => Z, gena: => A, genb: => B): Unit =
    exportedActions.append(() => {
      val a = gena
      val b = genb
      val z = f(gena, genb)
      history.append(ActionCall(name, List(a, b), Option(z)))
      ()
    })

  // TODO: maybe the code generator shouldn't format the arguments
  // and we just consume it directly?  Maybe having semi-structured
  // debug input might be nice (log to JSON or something?)
  def debug(msg: String): Unit = println(msg)

  def assertThat(file: String, lineno: Int, cond: Boolean) = if !cond then throw ConjectureFailure("", file, lineno) else ()