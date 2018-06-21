package com.electronwill.collections

import scala.collection.mutable

/**
 * A resizeable collection whose remove(i) method moves the last element to index i, and is
 * therefore O(1).
 *
 * @author TheElectronWill
 */
trait Bag[A] extends mutable.Iterable[A] with Compactable with PartialFunction[Int, A] {
  def apply(i: Int): A
  def remove(i: Int): Unit
  def -=(elem: A): this.type
  def +=(elem: A): this.type
  def ++=(array: Array[A], offset: Int, length: Int): this.type
  def clear(): Unit
  def contains(elem: A): Boolean = find(_ == elem).isDefined
  def iterator: MutableIterator[A]

  override final def hasDefiniteSize = true
  override final def isDefinedAt(i: Int): Boolean = i < size && i >= 0
}
