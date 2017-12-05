// This is a generated file! Please edit source .ksy file and use kaitai-struct-compiler to rebuild

#include "dot11_ie_48_rsn_partial.h"

#include <iostream>
#include <fstream>

dot11_ie_48_rsn_partial_t::dot11_ie_48_rsn_partial_t(kaitai::kstream *p_io, kaitai::kstruct *p_parent, dot11_ie_48_rsn_partial_t *p_root) : kaitai::kstruct(p_io) {
    m__parent = p_parent;
    m__root = this;
    m_rsn_version = m__io->read_u2le();
    m_group_cipher = m__io->read_bytes(4);
    m_pairwise_count = m__io->read_u2le();
}

dot11_ie_48_rsn_partial_t::~dot11_ie_48_rsn_partial_t() {
}
