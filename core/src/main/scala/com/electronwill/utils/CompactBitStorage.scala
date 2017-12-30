package com.electronwill.utils

/**
 * @author TheElectronWill
 */
object CompactStorage {
	def apply(bitsPerValue: Int, size: Int): CompactStorage = {
		assert(bitsPerValue > 0 && bitsPerValue < 32, "it is required that 0 < bitsPerValue < 32")
		val byteSize = Math.ceil(bitsPerValue * size / 8).toInt
		bitsPerValue match {
			case 4 => new CompactStorage4(size, byteSize)
			case 8 => new CompactStorage8(size, byteSize)
			case 16 => new CompactStorage16(size, byteSize)
			case _ => new CompactStorageN(bitsPerValue, size, byteSize)
		}
	}
}
sealed abstract class CompactStorage(final val size: Int, bs: Int) {
	final val bytes = new Array[Byte](bs)

	final def byteSize: Int = bytes.length
	def bitsPerValue: Int

	def apply(idx: Int): Int
	def update(idx: Int, value: Int): Unit
}

final class CompactStorageN private[utils](val bitsPerValue: Int, s: Int, bs: Int) extends CompactStorage(s, bs) {
	override def apply(idx: Int): Int = {
		var value = 0
		var i = 0
		while (i < bitsPerValue) {
			val bitIdx = idx * bitsPerValue + i
			val byteIdx = bitIdx >> 3 // (>> 3) divides by 8
			val bitInByte = bitIdx & 7 // (x & 7) is the same as (x % 8)
			if ((bytes(byteIdx) & (1 << bitInByte)) != 0) { // if bit is 1 then update the result
				value |= (1 << i)
			}
			i += 1
		}
		value
	}
	override def update(idx: Int, value: Int): Unit = {
		var i = 0
		while (i < bitsPerValue) {
			val bitIdx = idx * bitsPerValue + i
			val byteIdx = bitIdx >> 3
			val bitInByte = bitIdx & 7
			val mask = 1 << i
			if ((value & mask) != 0) {
				bytes(byteIdx) = (bytes(byteIdx) | (1 << bitInByte)).toByte // set bit to 1
			} else {
				bytes(byteIdx) = (bytes(byteIdx) & ~mask).toByte // set bit to 0
			}
			i += 1
		}
	}
}

final class CompactStorage4 private[utils](s: Int, bs: Int) extends CompactStorage(s, bs) {
	override def bitsPerValue: Int = 4
	override def apply(idx: Int): Int = {
		val byteIdx = idx >> 1 // (>> 1) divides by 2
		if ((byteIdx & 1) == 0) { // (x & 1) is the same as (x % 2)
			bytes(byteIdx) >> 4
		} else {
			bytes(byteIdx) & 0xf
		}
	}
	override def update(idx: Int, value: Int): Unit = {
		val byteIdx = idx >> 1
		if ((byteIdx & 1) == 0) {
			bytes(byteIdx) = (((value & 0xf) << 4) | bytes(byteIdx) & 0xf).toByte
		} else {
			bytes(byteIdx) = ((value & 0xf) | (bytes(byteIdx) & 0xf0)).toByte
		}
	}
}

final class CompactStorage8 private[utils](s: Int, bs: Int) extends CompactStorage(s, bs) {
	override def bitsPerValue: Int = 8
	override def apply(idx: Int): Int = {
		bytes(idx)
	}
	override def update(idx: Int, value: Int): Unit = {
		bytes(idx) = value.toByte
	}
}

final class CompactStorage16 private[utils](s: Int, bs: Int) extends CompactStorage(s, bs) {
	override def bitsPerValue: Int = 16
	override def apply(idx: Int): Int = {
		val firstIdx = idx << 1 // (<< 1) multiplies by 2
		val secondIdx = firstIdx + 1
		bytes(firstIdx) << 8 | bytes(secondIdx)
	}
	override def update(idx: Int, value: Int): Unit = {
		val firstIdx = idx << 1
		val secondIdx = firstIdx + 1
		bytes(firstIdx) = (value >> 8).toByte
		bytes(secondIdx) = (value & 0xff).toByte
	}
}