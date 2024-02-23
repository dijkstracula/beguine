package beguine.refinement

import beguine.runtime.{Arbitrary, Protocol}

import scala.collection.immutable.Set

class Refined[Spec <: Protocol, Impl <: Protocol](a: Arbitrary, spec: Spec, impl: Impl) extends Protocol(a) {
   if !Set(spec.getActions).contains(Set(impl.getActions)) then
     throw Exception("Spec's action set is not a subset of the implementation's")

   conjectured("final-histories-match", "Refined.scala", 0, () => {
        if spec.getHistory.size == 0 then
             true
        else
             spec.getHistory.last == impl.getHistory.last
   })
}
