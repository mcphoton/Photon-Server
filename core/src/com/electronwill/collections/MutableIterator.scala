package com.electronwill.collections

/**
 * @author TheElectronWill
 */
trait MutableIterator[A] extends Iterator[A] {

  /**
	 * Removes the current element from the underlying collection. This method must be called at
	 * most once per `next()` call.
	 */
  def remove(): Unit

  /**
	 * Adds an element that will not be visible to this iterator.
	 *
	 * @param elem the element to insert
	 */
  def insert(elem: A): Unit
}
