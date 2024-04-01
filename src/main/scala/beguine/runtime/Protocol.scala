package beguine.runtime

import beguine.actions.{Action, Action0, Action1, Witness}
import beguine.{ConjectureFailure, Error, GeneratorLivelock}
import com.typesafe.scalalogging.Logger
import org.slf4j.{Marker, MarkerFactory}

import scala.collection.mutable
import java.lang.Runnable
import java.util.function.BooleanSupplier
import scala.util.{Failure, Success, Try}

abstract class Protocol(a: Arbitrary) {
  protected val conjectures: mutable.Map[String, () => Option[ConjectureFailure]] = mutable.Map.empty
  protected val exportedActions: mutable.ArrayBuffer[Action] = mutable.ArrayBuffer.empty
  protected val history: mutable.ArrayBuffer[Witness.Call] = mutable.ArrayBuffer.empty

  protected val logger = Logger(getClass.getName)

  private val invokingActionMarker = MarkerFactory.getMarker("invoking-action")
  private val debugMarker = MarkerFactory.getMarker("ivy-debug")

  def apply(): Either[Error, Unit] =
    a.collection(exportedActions.toSeq) match {
      case None => Left(GeneratorLivelock())
      case Some(action) =>
        logger.info(invokingActionMarker, action.name)
        Try(action()) match {
          case Success(call) => history.append(call)
          case Failure(failed: ConjectureFailure) => return Left(failed)
          case Failure(e) => throw e
        }
        conjectures.collectFirst({case (_, conj) => conj() }).getOrElse(None) match {
          case None => Right(())
          case Some(failed) => Left(failed)
        }
    }

  def getHistory = history.toSeq

  def getActions = exportedActions.toSeq

  // Conjecture registration

  def conjectured(name: String, file: String, lineno: Int, conj: BooleanSupplier) =
    conjectures.addOne(name, () => {
      if (conj.getAsBoolean())
        None
      else
        Some(ConjectureFailure(file, name, lineno))
    })

  // Exported action registration

  def exported(name: String, f: java.lang.Runnable): Unit = exported[Unit](name, () => f.run())

  def exported[Z](name: String, f: () => Z): Unit = exportedActions.append(Action0[Z](name, f))

  def exported[A, Z](name: String, f: A => Z, gena: () => A): Unit = exportedActions.append(Action1[A, Z](gena)(name, f))


  // TODO: maybe the code generator shouldn't format the arguments
  // and we just consume it directly?  Maybe having semi-structured
  // debug input might be nice (log to JSON or something?)
  def debug(marker: Marker, msg: String): Unit = logger.info(marker, msg)

  def debug(msg: String): Unit = debug(debugMarker, msg)

  def assertThat(file: String, lineno: Int, cond: Boolean) = if (!cond) throw ConjectureFailure("", file, lineno) else ()

}