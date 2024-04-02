package beguine.refinement

import beguine.runtime.{Arbitrary, Protocol}

import scala.collection.immutable.Set

case class UnimplementedRefinementAction(name: String) extends Exception

class Refined[Spec <: Protocol, Impl <: Protocol](a: Arbitrary, spec: Spec, impl: Impl) extends Protocol(a) {
  for (sa <- spec.getActions) {
    val ia = impl.getActions.find(_.name == sa.name)
    ia match {
      case None => throw UnimplementedRefinementAction(sa.name)
      case Some(ia) => throw new Exception("TODO")
    }
  }
  conjectured("final-histories-match", "Refined.scala", 0, () => {
    if (spec.getHistory.size == 0) true else spec.getHistory.last == impl.getHistory.last
  })
}