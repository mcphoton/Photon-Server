package org.tuubes.core.network

/** Trait for packet builders, in particular those generated by DataTractor */
trait PacketBuilder[P <: Packet, Evidence] {
  /** Builds a new instance of the packet */
  def build()(implicit evidence: Evidence): P
}
