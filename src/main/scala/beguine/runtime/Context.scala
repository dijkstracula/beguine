package beguine.runtime

import beguine.sorts._

import scala.Enumeration
import scala.util.Random

import scala.jdk.StreamConverters._

trait Arbitrary:
  def bitvec(width: Int): Int
  def bool: Boolean
  def collection[T](s: Seq[T]): Option[T]
  def enumeration(e: Enumeration): e.Value
  def numeric(lo: Integer, hi: Integer): Integer

  def fromIvySort[J](s: IvySort[J]): () => J =
    s match {
      case Number(lo, hi) => () => this.numeric(lo, hi)
    }



class RandomArbitrary(using r: Random) extends Arbitrary:
  override def bitvec(width: Int): Int = r.between(0, 1 << width)

  override def bool: Boolean = r.nextBoolean

  def collection[T](s: Seq[T]) = s.size match {
    case 0 => None
    case n => Some(s(r.nextInt(n)))
  }

  // XXX: this is wrong, lol
  override def enumeration(e: Enumeration): e.Value = e.values.iterator.next()

  override def numeric(lo: Integer, hi: Integer): Integer = {
    r.between(lo, hi)
  }