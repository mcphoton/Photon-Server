package org.tuubes.core.network

/** Trait for packet builders, in particular those generated by DataTractor */
trait PacketBuilder[P <: Packet, C] {
  def build()(implicit evidence: P =:= C): P
}