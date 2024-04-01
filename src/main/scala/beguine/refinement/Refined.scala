package beguine.refinement

import beguine.runtime.{Arbitrary, Protocol}

import scala.collection.immutable.Set

class Refined[Spec <: Protocol, Impl <: Protocol](a: Arbitrary, spec: Spec, impl: Impl) extends Protocol(a) {
   if (!Set(spec.getActions).subsetOf(Set(impl.getActions))) {
     throw new Exception("Spec's action set is not a subset of the implementation's")
   }

  conjectured("final-histories-match", "Refined.scala", 0, () => {
        if (spec.getHistory.size == 0) true else spec.getHistory.last == impl.getHistory.last
   })
}
