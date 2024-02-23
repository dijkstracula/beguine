package beguine.runtime

import beguine.{ConjectureFailure, Error, GeneratorLivelock}
import com.typesafe.scalalogging.Logger
import org.slf4j.{Marker, MarkerFactory}

import scala.collection.mutable
import java.lang.Runnable
import java.util.function.BooleanSupplier
import scala.util.{Failure, Success, Try}

case class ActionCall(name: String, args: List[Any], ret: Option[Any])

abstract class Protocol(a: Arbitrary):
  protected val conjectures: mutable.Map[String, () => Option[ConjectureFailure]] = mutable.Map.empty
  protected val exportedActions: mutable.Map[String, () => Unit] = mutable.Map.empty
  protected val history: mutable.ArrayBuffer[ActionCall] = mutable.ArrayBuffer.empty

  protected val logger = Logger(getClass.getName)

  private val invokingActionMarker = MarkerFactory.getMarker("invoking-action")
  private val debugMarker = MarkerFactory.getMarker("ivy-debug")

  def apply(): Either[Error, Unit] =
    a.collection(exportedActions.values.toSeq) match {
      case None => Left(GeneratorLivelock())
      case Some(f) =>
        Try(f()) match {
          case Success(()) => ()
          case Failure(failed: ConjectureFailure) => return Left(failed)
          case Failure(e) => throw e
        }
        conjectures.collectFirst((_, conj) => conj()).getOrElse(None) match {
          case None => Right(())
          case Some(failed) => Left(failed)
        }
    }

  def getHistory = history.toSeq

  def getActions = exportedActions.keys.toSeq

  // Conjecture registration

  def conjectured(name: String, file: String, lineno: Int, conj: BooleanSupplier) =
    conjectures.addOne(name, () => {
      if conj.getAsBoolean() then
        None
      else
        Some(ConjectureFailure(file, name, lineno))
    })

  // Exported action registration

  def exported[Z](name: String, f: Runnable): Unit =
    exportedActions.addOne(name, () => {
      debug(invokingActionMarker, name)
      history.append(ActionCall(name, List.empty, Option(f.run())))
      ()
    })


  def exported[A, Z](name: String, f: A => Z, gena: => A): Unit =
    exportedActions.addOne(name, () => {
      val a = gena

      debug(invokingActionMarker, name)
      val z = f(gena)
      history.append(ActionCall(name, List(a), Option(z)))
      ()
    })

  def exported[A, B, Z](name: String, f: (A, B) => Z, gena: => A, genb: => B): Unit =
    exportedActions.addOne(name, () => {
      val a = gena
      val b = genb

      debug(invokingActionMarker, name)
      val z = f(gena, genb)
      history.append(ActionCall(name, List(a, b), Option(z)))
      ()
    })

  // TODO: maybe the code generator shouldn't format the arguments
  // and we just consume it directly?  Maybe having semi-structured
  // debug input might be nice (log to JSON or something?)
  def debug(marker: Marker, msg: String): Unit = logger.info(marker, msg)

  def debug(msg: String): Unit = debug(debugMarker, msg)

  def assertThat(file: String, lineno: Int, cond: Boolean) = if !cond then throw ConjectureFailure("", file, lineno) else ()